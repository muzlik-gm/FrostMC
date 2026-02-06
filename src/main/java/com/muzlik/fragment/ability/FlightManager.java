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
    
    // Cooldown sessions (after time limit expires)
    private final Map<UUID, CooldownSession> cooldownSessions = new HashMap<>();
    
    // Track when flight ended to prevent immediate reactivation
    private final Map<UUID, Long> reactivationLockout = new HashMap<>();
    private static final long LOCKOUT_DURATION = 2000L; // 2 seconds
    
    // Flight time limits (in seconds)
    private static final int AIR_FLIGHT_DURATION = 60; // 1 minute
    private static final int DRAGON_FLIGHT_DURATION = 600; // 10 minutes
    
    // Cooldown duration (in seconds)
    private static final int FLIGHT_COOLDOWN = 90; // 1.5 minutes
    
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
        
        // Check if on cooldown
        if (cooldownSessions.containsKey(uuid)) {
            CooldownSession cooldown = cooldownSessions.get(uuid);
            int remaining = cooldown.getRemainingSeconds();
            player.sendMessage("§cꜰʟɪɢʜᴛ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ! §7" + remaining + "s ʀᴇᴍᴀɪɴɪɴɢ");
            return;
        }
        
        // Start new flight session
        int duration = getFlightDuration(fragmentType);
        FlightSession session = new FlightSession(player, fragmentType, rank, duration);
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
    public boolean isOnCooldown(Player player) {
        return cooldownSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Get remaining flight time in seconds
     */
    public int getRemainingTime(Player player) {
        FlightSession session = activeSessions.get(player.getUniqueId());
        return session != null ? session.remainingTime : 0;
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
     * Get flight duration for fragment type
     */
    private int getFlightDuration(FragmentType type) {
        return switch (type) {
            case DRAGON -> DRAGON_FLIGHT_DURATION; // 10 minutes
            case AIR -> AIR_FLIGHT_DURATION;       // 1 minute
            default -> AIR_FLIGHT_DURATION;        // Default to 1 minute
        };
    }
    

    
    /**
     * Cleanup on plugin disable
     */
    public void shutdown() {
        // Create a copy to avoid ConcurrentModificationException
        new java.util.ArrayList<>(activeSessions.values()).forEach(session -> session.end(false));
        activeSessions.clear();
        new java.util.ArrayList<>(cooldownSessions.values()).forEach(CooldownSession::cancel);
        cooldownSessions.clear();
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
                    
                    // Show time remaining periodically
                    if (remainingTime == 30 || remainingTime == 15 || remainingTime == 10 || remainingTime == 5) {
                        player.sendMessage("§7ꜰʟɪɢʜᴛ ᴛɪᴍᴇ: §f" + remainingTime + "s §7ʀᴇᴍᴀɪɴɪɴɢ");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
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
            player.sendMessage(fragmentName + " §7ꜰʟɪɢʜᴛ ᴀᴄᴛɪᴠᴀᴛᴇᴅ! §8(" + remainingTime + "s)");
        }
        
        public void end(boolean startCooldown) {
            // Stop tasks
            if (updateTask != null) updateTask.cancel();
            if (visualTask != null) visualTask.cancel();
            
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
                player.sendMessage("§cꜰʟɪɢʜᴛ ᴇɴᴅᴇᴅ! §7ᴄᴏᴏʟᴅᴏᴡɴ: §e" + FLIGHT_COOLDOWN + "s");
                
                // Start cooldown
                CooldownSession cooldown = new CooldownSession(player, fragmentType);
                cooldownSessions.put(player.getUniqueId(), cooldown);
                cooldown.start();
            } else {
                player.sendMessage("§7ꜰʟɪɢʜᴛ ᴇɴᴅᴇᴅ");
            }
        }
        
        private void spawnFlightCarpet() {
            // Position carpet directly at player's feet (0.2 blocks below)
            org.bukkit.Location loc = player.getLocation().subtract(0, 0.2, 0);
            
            // Different particles for different fragments
            Particle particle = fragmentType == FragmentType.DRAGON ? Particle.FLAME : Particle.CLOUD;
            
            // Create denser carpet pattern below player (tighter grid)
            for (double x = -1.0; x <= 1.0; x += 0.25) {
                for (double z = -1.0; z <= 1.0; z += 0.25) {
                    org.bukkit.Location particleLoc = loc.clone().add(x, 0, z);
                    try {
                        player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
                    } catch (Exception ignored) {}
                }
            }
            
            // Add trail particles closer to player
            try {
                if (fragmentType == FragmentType.DRAGON) {
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0.4, 0.05, 0.4, 0.01);
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 2, 0.3, 0.05, 0.3, 0.01);
                } else {
                    player.getWorld().spawnParticle(Particle.END_ROD, loc, 3, 0.4, 0.05, 0.4, 0.01);
                    player.getWorld().spawnParticle(Particle.CLOUD, loc.clone().subtract(0, 0.1, 0), 2, 0.5, 0.05, 0.5, 0);
                }
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Cooldown session (after flight time limit expires)
     */
    private class CooldownSession {
        private final Player player;
        private final FragmentType fragmentType;
        private int remainingSeconds;
        private BukkitTask cooldownTask;
        
        public CooldownSession(Player player, FragmentType fragmentType) {
            this.player = player;
            this.fragmentType = fragmentType;
            this.remainingSeconds = FLIGHT_COOLDOWN;
        }
        
        public int getRemainingSeconds() {
            return remainingSeconds;
        }
        
        public void start() {
            cooldownTask = new BukkitRunnable() {
                @Override
                public void run() {
                    remainingSeconds--;
                    
                    // Notify at specific intervals
                    if (remainingSeconds == 60 || remainingSeconds == 30 || remainingSeconds == 10) {
                        player.sendMessage("§7ꜰʟɪɢʜᴛ ᴄᴏᴏʟᴅᴏᴡɴ: §e" + remainingSeconds + "s");
                    }
                    
                    // Cooldown complete
                    if (remainingSeconds <= 0) {
                        cooldownSessions.remove(player.getUniqueId());
                        player.sendMessage("§aꜰʟɪɢʜᴛ ʀᴇᴀᴅʏ!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
        
        public void cancel() {
            if (cooldownTask != null) {
                cooldownTask.cancel();
            }
        }
    }
}
