package com.muzlik.fragment.ability.executors.air;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Barrage - Air Fragment Ultimate Ability
 * Unleashes multiple wind blades in rapid succession
 * 
 * VFX: 5-Layer System with multiple projectiles
 * - Core: CLOUD blades
 * - Secondary: END_ROD trails
 * - Ambient: Dust storms
 * - Impact: SWEEP_ATTACK slashes
 * - Cinematic: Wind distortion
 */
public class BarrageExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector baseDirection = context.getDirection().clone().normalize();
        int rank = context.getRank();
        double baseDamage = 2.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial dramatic VFX
        VFXLayerBuilder initialVFX = new VFXLayerBuilder(plugin, eyeLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.CLOUD, 80, ParticlePattern.CONE, 2, 2, 2, 0.3, null)
            .secondary(Particle.END_ROD, 50, ParticlePattern.BURST, 1.5, 1.5, 1.5, 0.2, null)
            .ambient(Particle.WHITE_ASH, 60, ParticlePattern.SPHERE, 2, 2, 2, 0.1, null)
            .cinematic(0.25, CinematicEffect.WIND_DISTORTION);
        
        initialVFX.spawn();
        
        // Launch 8 wind blades with slight delays
        int bladeCount = 8;
        for (int i = 0; i < bladeCount; i++) {
            final int bladeIndex = i; // Make final for inner class
            int delay = i * 3; // 3 ticks between each blade
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Slight spread for each blade
                    Vector direction = baseDirection.clone();
                    direction.add(new Vector(
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.3
                    )).normalize();
                    
                    // Launch arrow
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(direction.multiply(3.5));
                    arrow.setDamage(damage);
                    arrow.setCritical(true);
                    arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    
                    // VFX for each blade
                    new VFXLayerBuilder(plugin, eyeLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.CLOUD, 15, ParticlePattern.LINE, 0.3, 0.3, 0.3, 0.08, null)
                        .secondary(Particle.END_ROD, 8, ParticlePattern.POINT, 0.2, 0.2, 0.2, 0.05, null)
                        .impact(Particle.SWEEP_ATTACK, 5, ParticlePattern.BURST, 0.3, 0.3, 0.3, 0.06, null)
                        .spawn();
                    
                    // Sound for each blade
                    eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f + (bladeIndex * 0.1f));
                }
            }.runTaskLater(plugin, delay);
        }
        
        // Initial sound
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);
    }
}