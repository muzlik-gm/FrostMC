package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.environment.EnvironmentManager;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Inferno Maelstrom - Fire Fragment Ultimate Ability
 * Creates a massive expanding ring of fire that damages and knocks back enemies
 * 
 * VFX: 5-Layer System with cinematic screen tremor
 * - Core: FLAME expanding ring
 * - Secondary: END_ROD spiral
 * - Ambient: SMOKE_LARGE rising
 * - Impact: LAVA bursts
 * - Cinematic: Screen tremor at rank 5+
 */
public class InfernoMaelstromExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        int rank = context.getRank();
        double baseRadius = 6.0;
        double maxRadius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 5.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        EnvironmentManager envManager = plugin.getEnvironmentManager();
        
        // Use cinematic VFX system
        com.muzlik.vfx.cinematic.ability.FireAbilityVFX fireVFX = new com.muzlik.vfx.cinematic.ability.FireAbilityVFX(plugin);
        
        // Inferno Maelstrom cinematic VFX: rotating magic circle with flame tornado
        fireVFX.infernoMaelstrom(player, center, maxRadius, 100, rank);
        
        // Expanding ring of fire
        new BukkitRunnable() {
            int ticks = 0;
            double currentRadius = 1.0;
            
            @Override
            public void run() {
                if (ticks >= 100 || currentRadius > maxRadius) {
                    cancel();
                    return;
                }
                
                // Expand ring
                currentRadius += 0.15;
                
                // Create fire ring at current radius
                for (double angle = 0; angle < 360; angle += 15) {
                    double radians = Math.toRadians(angle);
                    double x = Math.cos(radians) * currentRadius;
                    double z = Math.sin(radians) * currentRadius;
                    
                    Location fireLoc = center.clone().add(x, 0, z);
                    
                    // Spawn fire particles
                    fireLoc.getWorld().spawnParticle(Particle.FLAME, fireLoc, 3, 0.1, 0.3, 0.1, 0.02);
                    fireLoc.getWorld().spawnParticle(Particle.LAVA, fireLoc, 1, 0, 0, 0, 0);
                    
                    // Place temporary fire blocks
                    Block block = fireLoc.getBlock();
                    if (block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType().isSolid()) {
                        envManager.placeTemporaryBlock(fireLoc, Material.FIRE, 60, player);
                    }
                }
                
                // Damage and knockback entities in ring
                for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, currentRadius + 1, 3, currentRadius + 1)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        double distance = target.getLocation().distance(center);
                        
                        // Check if entity is near the ring edge
                        if (Math.abs(distance - currentRadius) < 1.5) {
                            // Knockback away from center
                            Vector knockback = target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.6);
                            knockback.setY(0.3);
                            target.setVelocity(knockback);
                            
                            // Damage
                            target.damage(damage / 5, player);
                            target.setFireTicks(60);
                        }
                    }
                }
                
                // Sound effect
                if (ticks % 10 == 0) {
                    center.getWorld().playSound(center, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.8f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Initial explosion sound
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
    }
}
