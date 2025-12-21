package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Void - Dark Fragment Ultimate Ability
 * Creates dark zone that debuffs enemies
 */
public class VoidExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        int rank = context.getRank();
        double baseRadius = 8.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDuration = 10.0;
        int durationTicks = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial dramatic void creation
        VFXLayerBuilder initialVFX = new VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.SMOKE_LARGE, 80, ParticlePattern.SPHERE, radius * 0.8, 3, radius * 0.8, 0.1, null)
            .secondary(Particle.SQUID_INK, 60, ParticlePattern.RING, radius * 0.6, 2, radius * 0.6, 0.08, null)
            .ambient(Particle.SMOKE_NORMAL, 40, ParticlePattern.BURST, radius, 1, radius, 0.05, null)
            .impact(Particle.PORTAL, 50, ParticlePattern.SPIRAL, radius * 0.4, 4, radius * 0.4, 0.15, null)
            .cinematic(0.3, CinematicEffect.DARKNESS_PULSE);
        
        initialVFX.spawn();
        
        // Sustained void effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks) {
                    cancel();
                    return;
                }
                
                // Continuous void VFX every 20 ticks
                if (ticks % 20 == 0) {
                    new VFXLayerBuilder(plugin, center, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.SMOKE_LARGE, 15, ParticlePattern.RING, radius * 0.6, 1, radius * 0.6, 0.03, null)
                        .secondary(Particle.SQUID_INK, 10, ParticlePattern.SPHERE, radius * 0.4, 0.5, radius * 0.4, 0.02, null)
                        .ambient(Particle.PORTAL, 8, ParticlePattern.POINT, radius * 0.3, 2, radius * 0.3, 0.05, null)
                        .spawn();
                }
                
                // Apply debuffs to enemies in the void
                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        
                        // Apply debuffs every 20 ticks (1 second)
                        if (ticks % 20 == 0) {
                            target.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.WITHER, 60, 2)); // Wither III
                            target.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SLOW, 60, 2)); // Slowness III
                            target.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.WEAKNESS, 60, 1)); // Weakness II
                            target.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.BLINDNESS, 40, 0)); // Brief blindness
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.5f);
        center.getWorld().playSound(center, Sound.BLOCK_PORTAL_AMBIENT, 1.5f, 0.8f);
        
        player.sendMessage("§5🌀 §dVoid zone created!");
    }
}