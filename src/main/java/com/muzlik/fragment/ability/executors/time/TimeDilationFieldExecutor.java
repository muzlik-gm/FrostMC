package com.muzlik.fragment.ability.executors.time;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Time Dilation Field - Time Fragment Ultimate Ability
 * Creates a spherical area where time flows differently for allies and enemies
 * 
 * Stats:
 * - Mana Cost: 70 - 5*rank
 * - Cooldown: 60 - 4*rank seconds
 * - Duration: 10 + 2*rank seconds
 * - Radius: 8 + rank blocks
 * - Ally Speed Amplifier: rank/3 (rounded down)
 * - Enemy Slowness Amplifier: rank/3 (rounded down)
 * 
 * VFX: Yellow particle sphere boundary
 * Sound: BLOCK_BEACON_DEACTIVATE on expiration
 */
public class TimeDilationFieldExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate parameters based on rank
        int durationTicks = (10 + (2 * rank)) * 20; // Convert to ticks
        double radius = 8.0 + rank;
        int allySpeedAmplifier = rank / 3; // Rounded down
        int enemySlownessAmplifier = rank / 3; // Rounded down
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Store field center location
        Location centerLoc = player.getLocation().clone();
        
        // Create BukkitRunnable to check entities in radius every tick
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Play expiration sound
                    centerLoc.getWorld().playSound(
                        centerLoc,
                        Sound.BLOCK_BEACON_DEACTIVATE,
                        1.5f,
                        1.0f
                    );
                    cancel();
                    return;
                }
                
                // Check all entities in radius
                for (Entity entity : centerLoc.getWorld().getNearbyEntities(centerLoc, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity living = (LivingEntity) entity;
                        
                        // Determine if ally or enemy
                        boolean isAlly = isAlly(player, living);
                        
                        if (isAlly) {
                            // Apply Speed to allies with hidden particles
                            living.addPotionEffect(PotionEffectHelper.createHiddenEffect(
                                PotionEffectType.SPEED,
                                40, // 2 seconds (refreshed every tick)
                                allySpeedAmplifier
                            ));
                        } else {
                            // Apply Slowness to enemies with hidden particles
                            living.addPotionEffect(PotionEffectHelper.createHiddenEffect(
                                PotionEffectType.SLOW,
                                40, // 2 seconds (refreshed every tick)
                                enemySlownessAmplifier
                            ));
                        }
                    }
                }
                
                // Spawn yellow particle sphere boundary every 5 ticks
                if (ticks % 5 == 0) {
                    spawnSphereBoundary(centerLoc, radius);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick
        
        // Play activation sound
        player.getWorld().playSound(
            centerLoc,
            Sound.BLOCK_BEACON_ACTIVATE,
            1.5f,
            1.2f
        );
        
        player.sendMessage("§eTime Dilation Field activated!");
    }
    
    /**
     * Determine if an entity is an ally of the player
     * @param player The player
     * @param entity The entity to check
     * @return True if ally, false if enemy
     */
    private boolean isAlly(Player player, LivingEntity entity) {
        // Players are allies
        if (entity instanceof Player) {
            return true;
        }
        
        // Tamed entities owned by the player are allies
        if (entity instanceof org.bukkit.entity.Tameable) {
            org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) entity;
            if (tameable.isTamed() && tameable.getOwner() != null) {
                return tameable.getOwner().getUniqueId().equals(player.getUniqueId());
            }
        }
        
        // All other entities are enemies
        return false;
    }
    
    /**
     * Spawn particle sphere boundary
     * @param center The center location
     * @param radius The radius of the sphere
     */
    private void spawnSphereBoundary(Location center, double radius) {
        int points = (int) (radius * 20); // More points for larger radius
        
        for (int i = 0; i < points; i++) {
            // Spherical coordinates
            double theta = Math.random() * 2 * Math.PI; // Azimuthal angle
            double phi = Math.random() * Math.PI; // Polar angle
            
            double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + radius * Math.cos(phi);
            double z = center.getZ() + radius * Math.sin(phi) * Math.sin(theta);
            
            Location particleLoc = new Location(center.getWorld(), x, y, z);
            
            // Yellow dust particles (RGB: 255, 255, 0)
            center.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0,
                new Particle.DustOptions(
                    org.bukkit.Color.fromRGB(255, 255, 0),
                    0.8f
                )
            );
            
            // Add some portal particles for time effect
            if (Math.random() < 0.3) {
                center.getWorld().spawnParticle(
                    Particle.PORTAL,
                    particleLoc,
                    1,
                    0.1, 0.1, 0.1,
                    0.1
                );
            }
        }
    }
}
