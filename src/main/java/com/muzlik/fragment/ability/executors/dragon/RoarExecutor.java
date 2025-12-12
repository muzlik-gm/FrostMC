package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Roar - Dragon Fragment Primary Ability
 * Unleashes devastating dragon breath in 12-block cone, ignites enemies
 * Base: 9 hearts damage, scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: DRAGON_BREATH cone
 * - Secondary: FLAME particles
 * - Ambient: SMOKE_LARGE cloud
 * - Impact: LAVA burst on enemies
 * - Cinematic: Dragon roar at rank 9+
 */
public class RoarExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        
        // Scale damage and range
        double baseDamage = 18.0; // 9 hearts
        double damage = baseDamage + (rank * 3.0); // +1.5 hearts per rank
        double range = 12.0 + (rank * 2.0);
        double coneWidth = 3.0 + (rank * 0.5);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create dragon breath cone
        for (double d = 1.0; d < range; d += 0.5) {
            for (double angle = -Math.PI/6; angle <= Math.PI/6; angle += Math.PI/24) {
                // Calculate cone position
                Vector coneDir = direction.clone();
                coneDir.rotateAroundY(angle);
                
                Location breathLoc = eyeLoc.clone().add(coneDir.multiply(d));
                
                // Add some spread based on distance
                double spread = (d / range) * coneWidth;
                breathLoc.add((Math.random() - 0.5) * spread, (Math.random() - 0.5) * spread, (Math.random() - 0.5) * spread);
                
                // Check for block collision
                if (!breathLoc.getBlock().isPassable()) {
                    continue;
                }
                
                // Dragon breath VFX
                int coreCount = 3 + (rank / 2);
                int secondaryCount = 2 + (rank / 3);
                
                VFXLayerBuilder breathVfx = new VFXLayerBuilder(plugin, breathLoc, rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .core(Particle.DRAGON_BREATH, coreCount, ParticlePattern.POINT, 0.3, 0.3, 0.3, 0.02, null)
                    .secondary(Particle.FLAME, secondaryCount, ParticlePattern.POINT, 0.4, 0.4, 0.4, 0.03, null);
                
                breathVfx.spawn();
                
                // Check for entity hits
                for (Entity entity : breathLoc.getWorld().getNearbyEntities(breathLoc, 1.0, 1.0, 1.0)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        
                        // Deal damage
                        target.damage(damage, player);
                        
                        // Ignite target
                        target.setFireTicks(100 + (rank * 20)); // 5s + 1s per rank
                        
                        // Impact VFX
                        int impactCount = 15 + (rank * 3);
                        VFXLayerBuilder impactVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, player)
                            .withPerformanceManager(plugin.getVFXPerformanceManager())
                            .impact(Particle.LAVA, impactCount, ParticlePattern.BURST, 0.8, 0.8, 0.8, 0.1, null)
                            .ambient(Particle.SMOKE_LARGE, impactCount / 2, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.05, null);
                        
                        impactVfx.spawn();
                    }
                }
            }
        }
        
        // Ambient smoke cloud at player location
        VFXLayerBuilder smokeVfx = new VFXLayerBuilder(plugin, eyeLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .ambient(Particle.SMOKE_LARGE, 20 + (rank * 4), ParticlePattern.CONE, 2.0, 2.0, 2.0, 0.08, null);
        
        // Cinematic effect at rank 9+
        if (rank >= 9) {
            smokeVfx.cinematic(0.3 + (rank * 0.05), CinematicEffect.DRAGON_ROAR);
        }
        
        smokeVfx.spawn();
        
        // Sound effects
        float pitch = 0.6f + (rank * 0.05f);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f + (rank * 0.3f), pitch);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, pitch);
    }
}