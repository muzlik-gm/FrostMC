package com.muzlik.fragment.ability.executors.light;

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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Beam - Light Fragment Primary Ability
 * Shoots concentrated light beam that pierces through enemies
 * Base: 6 hearts damage + Blindness 4s, scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: END_ROD beam
 * - Secondary: ELECTRIC_SPARK trail
 * - Ambient: GLOW particles
 * - Impact: FLASH burst
 * - Cinematic: Bright flash at rank 3+
 */
public class BeamExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        
        // Scale damage
        double baseDamage = 6.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Beam range increases with rank
        double range = 20.0 + (rank * 5.0);
        
        // Cast beam and find all entities in path
        Location currentLoc = eyeLoc.clone();
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create beam particles along path
        for (double d = 0; d < range; d += 0.5) {
            currentLoc.add(direction.clone().multiply(0.5));
            
            // Check for block collision
            if (!currentLoc.getBlock().isPassable()) {
                break;
            }
            
            // Beam VFX at each point
            int coreCount = 3 + rank;
            int secondaryCount = 2 + rank;
            
            VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, currentLoc.clone(), rank, player)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .core(Particle.END_ROD, coreCount, ParticlePattern.POINT, 0.1, 0.1, 0.1, 0.02, null)
                .secondary(Particle.ELECTRIC_SPARK, secondaryCount, ParticlePattern.POINT, 0.15, 0.15, 0.15, 0.01, null);
            
            vfxBuilder.spawn();
            
            // Check for entity hits
            for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;
                    
                    // Deal damage
                    target.damage(damage, player);
                    
                    // Apply blindness
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0)); // 4 seconds
                    
                    // Impact VFX
                    int impactCount = 15 + (rank * 5);
                    VFXLayerBuilder impactVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .impact(Particle.FLASH, impactCount, ParticlePattern.BURST, 0.8, 0.8, 0.8, 0.1, null)
                        .ambient(Particle.GLOW, impactCount / 2, ParticlePattern.SPHERE, 0.5, 0.5, 0.5, 0.05, null);
                    
                    if (rank >= 3) {
                        impactVfx.cinematic(0.2 + (rank * 0.05), CinematicEffect.LIGHT_BLOOM);
                    }
                    
                    impactVfx.spawn();
                }
            }
        }
        
        // Sound effect
        float pitch = 1.2f + (rank * 0.1f);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_GUARDIAN_ATTACK, 1.0f + (rank * 0.2f), pitch);
    }
}