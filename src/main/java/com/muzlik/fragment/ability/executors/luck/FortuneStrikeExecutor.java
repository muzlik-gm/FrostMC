package com.muzlik.fragment.ability.executors.luck;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Fortune Strike - Luck Fragment Primary Ability
 * Grants increased critical hit chance and damage
 * 
 * Stats:
 * - Mana Cost: 35 - 4*rank
 * - Cooldown: 15 - 1*rank seconds
 * - Duration: 10 + rank seconds
 * - Critical Chance: 30% + 5%*rank
 * - Critical Multiplier: 1.5x + 0.1x*rank
 * 
 * VFX: Green clover particles around player
 * Sound: BLOCK_AMETHYST_BLOCK_CHIME on critical hit
 */
public class FortuneStrikeExecutor implements AbilityExecutor {
    
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final java.util.Map<UUID, Integer> playerRanks = new java.util.HashMap<>();
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate duration based on rank
        int durationTicks = (10 + rank) * 20; // Convert to ticks
        
        // Store player in active set with rank
        activePlayers.add(player.getUniqueId());
        playerRanks.put(player.getUniqueId(), rank);
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn green clover particles around player
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline() || player.isDead()) {
                    // Remove player from active set
                    activePlayers.remove(player.getUniqueId());
                    playerRanks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                
                // Spawn green clover particles in a circle around player
                Location playerLoc = player.getLocation().add(0, 1, 0);
                double angle = (ticks * 0.2) % (2 * Math.PI);
                double radius = 1.2;
                
                for (int i = 0; i < 4; i++) {
                    double currentAngle = angle + (i * Math.PI / 2);
                    double x = playerLoc.getX() + Math.cos(currentAngle) * radius;
                    double z = playerLoc.getZ() + Math.sin(currentAngle) * radius;
                    
                    Location particleLoc = new Location(playerLoc.getWorld(), x, playerLoc.getY(), z);
                    
                    // Green dust particles (RGB: 0, 255, 0)
                    playerLoc.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        particleLoc,
                        1,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(
                            org.bukkit.Color.fromRGB(0, 255, 0),
                            1.0f
                        )
                    );
                }
                
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
        
        // Play activation sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.BLOCK_AMETHYST_BLOCK_CHIME,
            0.8f,
            1.2f
        );
        
        player.sendMessage("§aFortune Strike activated!");
    }
    
    /**
     * Listener for handling critical hit mechanics
     */
    public static class FortuneStrikeListener implements Listener {
        
        @EventHandler
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getDamager();
            
            // Check if player has Fortune Strike active
            if (!activePlayers.contains(player.getUniqueId())) {
                return;
            }
            
            int rank = playerRanks.getOrDefault(player.getUniqueId(), 1);
            
            // Calculate critical chance
            double critChance = 0.30 + (0.05 * rank);
            
            // Roll for critical hit
            if (Math.random() < critChance) {
                // Calculate critical multiplier
                double critMultiplier = 1.5 + (0.1 * rank);
                
                // Apply critical damage
                double originalDamage = event.getDamage();
                event.setDamage(originalDamage * critMultiplier);
                
                // Spawn critical hit particles
                Location targetLoc = event.getEntity().getLocation().add(0, 1, 0);
                targetLoc.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    targetLoc,
                    20,
                    0.5, 0.5, 0.5,
                    0,
                    new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(0, 255, 0),
                        1.5f
                    )
                );
                
                targetLoc.getWorld().spawnParticle(
                    Particle.CRIT,
                    targetLoc,
                    10,
                    0.3, 0.3, 0.3,
                    0.1
                );
                
                // Play critical hit sound
                targetLoc.getWorld().playSound(
                    targetLoc,
                    Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                    1.0f,
                    1.5f
                );
                
                player.sendMessage("§a§lCRITICAL HIT! §7(" + String.format("%.1f", critMultiplier) + "x damage)");
            }
        }
    }
    
    /**
     * Check if a player has Fortune Strike active
     * @param playerId The player's UUID
     * @return True if active
     */
    public static boolean isActive(UUID playerId) {
        return activePlayers.contains(playerId);
    }
    
    /**
     * Get the rank of a player's active Fortune Strike
     * @param playerId The player's UUID
     * @return The rank, or 0 if not active
     */
    public static int getRank(UUID playerId) {
        return playerRanks.getOrDefault(playerId, 0);
    }
}
