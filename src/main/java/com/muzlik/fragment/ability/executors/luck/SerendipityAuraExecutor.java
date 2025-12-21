package com.muzlik.fragment.ability.executors.luck;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Serendipity Aura - Luck Fragment Ultimate Ability
 * Shares luck effects with nearby allies
 * 
 * Stats:
 * - Mana Cost: 65 - 5*rank
 * - Cooldown: 70 - 5*rank seconds
 * - Duration: 12 + 2*rank seconds
 * - Radius: 10 + rank blocks
 * - Shared Luck Amplifier: rank/3 (rounded down)
 * - Shared Critical Chance: 15% + 3%*rank
 * 
 * VFX: Green particle ring boundary
 * Sound: BLOCK_BEACON_DEACTIVATE on expiration
 */
public class SerendipityAuraExecutor implements AbilityExecutor {
    
    private static final Set<UUID> auraPlayers = new HashSet<>();
    private static final java.util.Map<UUID, Integer> auraRanks = new java.util.HashMap<>();
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate parameters based on rank
        int durationTicks = (12 + (2 * rank)) * 20; // Convert to ticks
        double radius = 10.0 + rank;
        int sharedLuckAmplifier = rank / 3; // Rounded down
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Store field center location
        Location centerLoc = player.getLocation().clone();
        
        // Create BukkitRunnable to check players in radius every tick
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Clear aura players
                    auraPlayers.clear();
                    
                    // Play expiration sound
                    centerLoc.getWorld().playSound(
                        centerLoc,
                        Sound.BLOCK_BEACON_DEACTIVATE,
                        1.5f,
                        1.2f
                    );
                    cancel();
                    return;
                }
                
                // Clear previous aura players
                auraPlayers.clear();
                
                // Check all players in radius
                for (Entity entity : centerLoc.getWorld().getNearbyEntities(centerLoc, radius, radius, radius)) {
                    if (entity instanceof Player) {
                        Player ally = (Player) entity;
                        
                        // Apply Luck effect with hidden particles
                        ally.addPotionEffect(PotionEffectHelper.createHiddenEffect(
                            PotionEffectType.LUCK,
                            40, // 2 seconds (refreshed every tick)
                            sharedLuckAmplifier
                        ));
                        
                        // Store in aura set for shared Fortune Strike/Lucky Dodge
                        auraPlayers.add(ally.getUniqueId());
                        auraRanks.put(ally.getUniqueId(), rank);
                    }
                }
                
                // Spawn green particle ring boundary every 5 ticks
                if (ticks % 5 == 0) {
                    spawnRingBoundary(centerLoc, radius);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick
        
        // Play activation sound
        player.getWorld().playSound(
            centerLoc,
            Sound.BLOCK_BEACON_ACTIVATE,
            1.5f,
            1.3f
        );
        
        player.sendMessage("§aSerendipity Aura activated!");
    }
    
    /**
     * Spawn particle ring boundary
     * @param center The center location
     * @param radius The radius of the ring
     */
    private void spawnRingBoundary(Location center, double radius) {
        int points = (int) (radius * 16); // More points for larger radius
        
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            Location particleLoc = new Location(
                center.getWorld(),
                x,
                center.getY() + 0.2,
                z
            );
            
            // Green dust particles (RGB: 0, 255, 0)
            center.getWorld().spawnParticle(
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
            
            // Add villager happy particles for luck effect
            if (i % 8 == 0) {
                center.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    particleLoc.clone().add(0, 0.5, 0),
                    1,
                    0.1, 0.3, 0.1,
                    0.1
                );
            }
        }
        
        // Add vertical pillars at cardinal directions
        for (int dir = 0; dir < 4; dir++) {
            double angle = (Math.PI / 2) * dir;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            for (double y = 0; y <= 3.0; y += 0.5) {
                Location pillarLoc = new Location(
                    center.getWorld(),
                    x,
                    center.getY() + y,
                    z
                );
                
                center.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    pillarLoc,
                    1,
                    0, 0, 0,
                    0,
                    new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(0, 255, 0),
                        0.8f
                    )
                );
            }
        }
    }
    
    /**
     * Check if a player is in a Serendipity Aura
     * @param playerId The player's UUID
     * @return True if in aura
     */
    public static boolean isInAura(UUID playerId) {
        return auraPlayers.contains(playerId);
    }
    
    /**
     * Get the rank of the aura affecting a player
     * @param playerId The player's UUID
     * @return The rank, or 0 if not in aura
     */
    public static int getAuraRank(UUID playerId) {
        return auraRanks.getOrDefault(playerId, 0);
    }
    
    /**
     * Get the shared critical chance for players in the aura
     * @param rank The aura rank
     * @return The critical chance
     */
    public static double getSharedCriticalChance(int rank) {
        return 0.15 + (0.03 * rank);
    }
}
