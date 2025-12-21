package com.muzlik.fragment.ability.executors.luck;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lucky Dodge - Luck Fragment Secondary Ability
 * Grants chance to evade incoming attacks
 * 
 * Stats:
 * - Mana Cost: 30 - 3*rank
 * - Cooldown: 18 - 1.5*rank seconds
 * - Duration: 8 + rank seconds
 * - Evasion Chance: 25% + 5%*rank
 * - Max Evasion Chance: 75%
 * 
 * VFX: Green shimmer particles around player
 * Sound: ENTITY_PLAYER_ATTACK_SWEEP on dodge
 */
public class LuckyDodgeExecutor implements AbilityExecutor {
    
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final java.util.Map<UUID, Integer> playerRanks = new java.util.HashMap<>();
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate duration based on rank
        int durationTicks = (8 + rank) * 20; // Convert to ticks
        
        // Store player in active set with rank
        activePlayers.add(player.getUniqueId());
        playerRanks.put(player.getUniqueId(), rank);
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn green shimmer particles around player
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
                
                // Spawn green shimmer particles around player
                Location playerLoc = player.getLocation().add(0, 1, 0);
                
                // Create shimmer effect with random particles
                for (int i = 0; i < 3; i++) {
                    double offsetX = (Math.random() - 0.5) * 1.5;
                    double offsetY = (Math.random() - 0.5) * 1.5;
                    double offsetZ = (Math.random() - 0.5) * 1.5;
                    
                    Location particleLoc = playerLoc.clone().add(offsetX, offsetY, offsetZ);
                    
                    // Green dust particles (RGB: 0, 255, 0)
                    playerLoc.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        particleLoc,
                        1,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(
                            org.bukkit.Color.fromRGB(0, 255, 0),
                            0.8f
                        )
                    );
                    
                    // Add villager happy particles for shimmer
                    if (Math.random() < 0.3) {
                        playerLoc.getWorld().spawnParticle(
                            Particle.VILLAGER_HAPPY,
                            particleLoc,
                            1,
                            0.1, 0.1, 0.1,
                            0
                        );
                    }
                }
                
                ticks += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
        
        // Play activation sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP,
            0.5f,
            1.5f
        );
        
        player.sendMessage("§aLucky Dodge activated!");
    }
    
    /**
     * Listener for handling evasion mechanics
     */
    public static class LuckyDodgeListener implements Listener {
        
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getEntity();
            
            // Check if player has Lucky Dodge active
            if (!activePlayers.contains(player.getUniqueId())) {
                return;
            }
            
            int rank = playerRanks.getOrDefault(player.getUniqueId(), 1);
            
            // Calculate evasion chance (capped at 75%)
            double evasionChance = Math.min(0.75, 0.25 + (0.05 * rank));
            
            // Roll for evasion
            if (Math.random() < evasionChance) {
                // Cancel damage event
                event.setCancelled(true);
                
                // Spawn dodge particles
                Location playerLoc = player.getLocation().add(0, 1, 0);
                playerLoc.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    playerLoc,
                    30,
                    0.5, 0.5, 0.5,
                    0,
                    new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(0, 255, 0),
                        1.2f
                    )
                );
                
                playerLoc.getWorld().spawnParticle(
                    Particle.SWEEP_ATTACK,
                    playerLoc,
                    3,
                    0.5, 0.5, 0.5,
                    0
                );
                
                playerLoc.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    playerLoc,
                    10,
                    0.5, 0.5, 0.5,
                    0.1
                );
                
                // Play evasion sound
                playerLoc.getWorld().playSound(
                    playerLoc,
                    Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                    1.0f,
                    1.5f
                );
                
                player.sendMessage("§a§lDODGED! §7(Lucky evasion)");
            }
        }
    }
    
    /**
     * Check if a player has Lucky Dodge active
     * @param playerId The player's UUID
     * @return True if active
     */
    public static boolean isActive(UUID playerId) {
        return activePlayers.contains(playerId);
    }
    
    /**
     * Get the rank of a player's active Lucky Dodge
     * @param playerId The player's UUID
     * @return The rank, or 0 if not active
     */
    public static int getRank(UUID playerId) {
        return playerRanks.getOrDefault(playerId, 0);
    }
}
