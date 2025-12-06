package com.muzlik.fragment.ability;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages flight abilities with duration limits, recharge, and visual effects
 */
public class FlightManager {
    private final FrostSMPPlugin plugin;
    
    // Active flight sessions
    private final Map<UUID, FlightSession> activeSessions = new HashMap<>();
    
    // Recharging sessions (cancelled early, no cooldown)
    private final Map<UUID, RechargeSession> rechargeSessions = new HashMap<>();
    
    // Track when flight ended to prevent immediate reactivation
    private final Map<UUID, Long> reactivationLockout = new HashMap<>();
    private static final long LOCKOUT_DURATION = 2000L; // 2 seconds
    
    public FlightManager(FrostSMPPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start a flight session
     */
    public void startFlight(Player player, FragmentType fragmentType, int rank) {
        UUID uuid = player.getUniqueId();
        
        // Check reactivation lockout (prevents immediate reactivation)
        Long lockoutEnd = reactivationLockout.get(uuid);
        if (lockoutEnd != null && System.currentTimeMillis() < lockoutEnd) {
            // Silently ignore - prevents spam messages
            return;
        }
        
        // Check if already flying
        if (activeSessions.containsKey(uuid)) {
            player.sendMessage("§8ᴀʟʀᴇᴀᴅʏ ꜰʟʏɪɴɢ");
            return;
        }
        
        // Check if recharging - if so, resume flight
        if (rechargeSessions.containsKey(uuid)) {
            RechargeSession recharge = rechargeSessions.remove(uuid);
            recharge.cancel();
            
            // Resume with remaining time
            FlightSession session = new FlightSession(player, fragmentType, rank, recharge.remainingTime);
            activeSessions.put(uuid, session);
            session.start();
            
            player.sendMessage("§aꜰʟɪɢʜᴛ ʀᴇsᴜᴍᴇᴅ!");
            return;
        }
        
        // Start new flight session
        FlightSession session = new FlightSession(player, fragmentType, rank, getMaxDuration(fragmentType));
        activeSessions.put(uuid, session);
        session.start();
    }
    
    /**
     * Check if player is currently flying
     */
    public boolean isFlying(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Check if player is recharging
     */
    public boolean isRecharging(Player player) {
        return rechargeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Get remaining flight time in seconds
     */
    public int getRemainingTime(Player player) {
        FlightSession session = activeSessions.get(player.getUniqueId());
        return session != null ? session.remainingTime : 0;
    }
    
    /**
     * Attempt to cancel flight (requires 10s hold)
     */
    public void attemptCancel(Player player) {
        FlightSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.startCancelAttempt();
        }
    }
    
    /**
     * Stop cancel attempt
     */
    public void stopCancelAttempt(Player player) {
        FlightSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.stopCancelAttempt();
        }
    }
    
    /**
     * Force end flight (for external events)
     */
    public void endFlight(Player player, boolean startCooldown) {
        UUID uuid = player.getUniqueId();
        FlightSession session = activeSessions.remove(uuid);
        if (session != null) {
            session.end(startCooldown);
        }
    }
    
    /**
     * Get max duration for fragment type
     */
    private int getMaxDuration(FragmentType type) {
        return switch (type) {
            case DRAGON -> 1800; // 30 minutes
            case AIR -> 600;     // 10 minutes
            default -> 300;      // 5 minutes (fallback)
        };
    }
    
    /**
     * Cleanup on plugin disable
     */
    public void shutdown() {
        activeSessions.values().forEach(session -> session.end(false));
        activeSessions.clear();
        rechargeSessions.values().forEach(RechargeSession::cancel);
        rechargeSessions.clear();
        reactivationLockout.clear();
    }
    
    /**
     * Flight session for a player
     */
    private class FlightSession {
        private final Player player;
        private final FragmentType fragmentType;
        private final int rank;
        private int remainingTime; // in seconds
        private BukkitTask updateTask;
        private BukkitTask visualTask;
        private BukkitTask cancelTask;
        private int cancelProgress = 0;
        
        public FlightSession(Player player, FragmentType fragmentType, int rank, int duration) {
            this.player = player;
            this.fragmentType = fragmentType;
            this.rank = rank;
            this.remainingTime = duration;
        }
        
        public void start() {
            // Enable flight
            player.setAllowFlight(true);
            player.setFlying(true);
            
            // Start update task (every second)
            updateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    remainingTime--;
                    
                    // Show time remaining every 60 seconds
                    if (remainingTime % 60 == 0 && remainingTime > 0) {
                        int minutes = remainingTime / 60;
                        player.sendMessage("§7ꜰʟɪɢʜᴛ ᴛɪᴍᴇ: §f" + minutes + "ᴍ §7ʀᴇᴍᴀɪɴɪɴɢ");
                    }
                    
                    // Warning at 1 minute
                    if (remainingTime == 60) {
                        player.sendMessage("§c⚠ ꜰʟɪɢʜᴛ ᴇɴᴅɪɴɢ ɪɴ 1 ᴍɪɴᴜᴛᴇ!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
                    }
                    
                    // End flight when time runs out
                    if (remainingTime <= 0) {
                        end(true); // Start cooldown
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
            
            // Start visual effects task (every 2 ticks for faster response)
            visualTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Stop if player is offline or session ended
                    if (!player.isOnline() || !activeSessions.containsKey(player.getUniqueId())) {
                        cancel();
                        return;
                    }
                    
                    // Only spawn particles if actually flying
                    if (player.isFlying()) {
                        spawnFlightCarpet();
                    }
                }
            }.runTaskTimer(plugin, 0L, 2L);
            
            // Play start sound
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.2f);
            
            String fragmentName = fragmentType == FragmentType.DRAGON ? "§5ᴅʀᴀɢᴏɴ" : "§fᴀɪʀ";
            int minutes = remainingTime / 60;
            player.sendMessage(fragmentName + " §7ꜰʟɪɢʜᴛ ᴀᴄᴛɪᴠᴀᴛᴇᴅ! §8(" + minutes + "ᴍ)");
            player.sendMessage("§8ʜᴏʟᴅ sɴᴇᴀᴋ + ʟᴇꜰᴛ ᴄʟɪᴄᴋ 10s ᴛᴏ ᴄᴀɴᴄᴇʟ");
        }
        
        public void startCancelAttempt() {
            if (cancelTask != null) return; // Already cancelling
            
            cancelProgress = 0;
            player.sendMessage("§eᴄᴀɴᴄᴇʟʟɪɴɢ ꜰʟɪɢʜᴛ... §7(ʜᴏʟᴅ 10s)");
            
            cancelTask = new BukkitRunnable() {
                @Override
                public void run() {
                    cancelProgress++;
                    
                    // Progress indicator every 2 seconds
                    if (cancelProgress % 2 == 0) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f);
                        player.sendMessage("§7ᴄᴀɴᴄᴇʟʟɪɴɢ... §e" + cancelProgress + "§7/§e10");
                    }
                    
                    // Cancel complete after 10 seconds
                    if (cancelProgress >= 10) {
                        cancel();
                        player.sendMessage("§aꜰʟɪɢʜᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ! §7ʀᴇᴄʜᴀʀɢɪɴɢ...");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        
        public void stopCancelAttempt() {
            if (cancelTask != null) {
                cancelTask.cancel();
                cancelTask = null;
                cancelProgress = 0;
                player.sendMessage("§7ᴄᴀɴᴄᴇʟ sᴛᴏᴘᴘᴇᴅ");
            }
        }
        
        public void end(boolean startCooldown) {
            // Stop tasks
            if (updateTask != null) updateTask.cancel();
            if (visualTask != null) visualTask.cancel();
            if (cancelTask != null) cancelTask.cancel();
            
            // Disable flight (only for survival/adventure mode)
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && 
                player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            
            // Remove from active sessions
            activeSessions.remove(player.getUniqueId());
            
            // Add reactivation lockout to prevent immediate reactivation
            reactivationLockout.put(player.getUniqueId(), System.currentTimeMillis() + LOCKOUT_DURATION);
            
            // Play end sound
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 0.8f);
            
            if (startCooldown) {
                player.sendMessage("§cꜰʟɪɢʜᴛ ᴇɴᴅᴇᴅ! §8ᴄᴏᴏʟᴅᴏᴡɴ sᴛᴀʀᴛᴇᴅ");
                // Cooldown is handled by CooldownManager in the ability system
            } else {
                player.sendMessage("§7ꜰʟɪɢʜᴛ ᴇɴᴅᴇᴅ");
            }
        }
        
        private void cancel() {
            // Stop tasks
            if (updateTask != null) updateTask.cancel();
            if (visualTask != null) visualTask.cancel();
            if (cancelTask != null) cancelTask.cancel();
            
            // Disable flight (only for survival/adventure mode)
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && 
                player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            
            // Remove from active sessions
            activeSessions.remove(player.getUniqueId());
            
            // Add reactivation lockout to prevent immediate reactivation
            reactivationLockout.put(player.getUniqueId(), System.currentTimeMillis() + LOCKOUT_DURATION);
            
            // Start recharge (no cooldown)
            RechargeSession recharge = new RechargeSession(player, fragmentType, remainingTime);
            rechargeSessions.put(player.getUniqueId(), recharge);
            recharge.start();
        }
        
        private void spawnFlightCarpet() {
            // Position carpet directly at player's feet (0.2 blocks below)
            org.bukkit.Location loc = player.getLocation().subtract(0, 0.2, 0);
            
            // Different particles for different fragments
            Particle particle = fragmentType == FragmentType.DRAGON ? Particle.DRAGON_BREATH : Particle.CLOUD;
            
            // Create denser carpet pattern below player (tighter grid)
            for (double x = -1.0; x <= 1.0; x += 0.25) {
                for (double z = -1.0; z <= 1.0; z += 0.25) {
                    org.bukkit.Location particleLoc = loc.clone().add(x, 0, z);
                    player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
                }
            }
            
            // Add trail particles closer to player
            if (fragmentType == FragmentType.DRAGON) {
                player.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0.4, 0.05, 0.4, 0.01);
                player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 2, 0.3, 0.05, 0.3, 0.01);
            } else {
                player.getWorld().spawnParticle(Particle.END_ROD, loc, 3, 0.4, 0.05, 0.4, 0.01);
                player.getWorld().spawnParticle(Particle.CLOUD, loc.clone().subtract(0, 0.1, 0), 2, 0.5, 0.05, 0.5, 0);
            }
        }
    }
    
    /**
     * Recharge session (cancelled flight, no cooldown)
     */
    private class RechargeSession {
        private final Player player;
        private final FragmentType fragmentType;
        private int remainingTime; // Time left when cancelled
        private int rechargeTime; // Time to fully recharge
        private BukkitTask rechargeTask;
        
        public RechargeSession(Player player, FragmentType fragmentType, int remainingTime) {
            this.player = player;
            this.fragmentType = fragmentType;
            this.remainingTime = remainingTime;
            this.rechargeTime = getMaxDuration(fragmentType) - remainingTime;
        }
        
        public void start() {
            int minutes = rechargeTime / 60;
            player.sendMessage("§7ʀᴇᴄʜᴀʀɢɪɴɢ... §e" + minutes + "ᴍ §7ᴜɴᴛɪʟ ꜰᴜʟʟ");
            
            rechargeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    rechargeTime--;
                    remainingTime++;
                    
                    // Notify every 5 minutes
                    if (rechargeTime % 300 == 0 && rechargeTime > 0) {
                        int mins = rechargeTime / 60;
                        player.sendMessage("§7ʀᴇᴄʜᴀʀɢɪɴɢ... §e" + mins + "ᴍ §7ʀᴇᴍᴀɪɴɪɴɢ");
                    }
                    
                    // Fully recharged
                    if (rechargeTime <= 0) {
                        rechargeSessions.remove(player.getUniqueId());
                        player.sendMessage("§aꜰʟɪɢʜᴛ ꜰᴜʟʟʏ ʀᴇᴄʜᴀʀɢᴇᴅ!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
        
        public void cancel() {
            if (rechargeTask != null) {
                rechargeTask.cancel();
            }
        }
    }
}
