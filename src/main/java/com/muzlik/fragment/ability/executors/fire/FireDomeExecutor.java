package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Fire Dome - Fire Fragment Secondary Ability
 * Creates a protective dome of fire around the player
 * Burns enemies that get too close
 * 
 * VFX: 5-Layer System
 * - Core: FLAME dome shell
 * - Secondary: END_ROD shimmer
 * - Ambient: SMOKE_LARGE heat
 * - Impact: LAVA burst on contact
 * - Cinematic: Heat shimmer at rank 5+
 */
public class FireDomeExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Scale parameters
        double baseDuration = 6.0;
        double duration = context.getScalingEngine().scaleDuration(baseDuration, rank);
        int durationTicks = (int) (duration * 20);
        
        double baseRadius = 6.0;  // INCREASED from 3.0 to 6.0 for better protection
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank); // Proper scaling, no artificial cap
        
        Location center = player.getLocation();
        
        // Apply fire resistance to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, durationTicks, 0, false, false));
        
        // Create dome effect
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial VFX
        VFXLayerBuilder initialVFX = new VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, 100, ParticlePattern.SPHERE, radius, radius/2, radius, 0.1, null)
            .secondary(Particle.END_ROD, 50, ParticlePattern.RING, radius, radius/2, radius, 0.05, null)
            .ambient(Particle.SMOKE_LARGE, 30, ParticlePattern.SPHERE, radius * 0.8, radius/2, radius * 0.8, 0.02, null)
            .impact(Particle.LAVA, 40, ParticlePattern.BURST, radius, radius/2, radius, 0.1, null);
        
        if (rank >= 5) {
            initialVFX.cinematic(0.2, CinematicEffect.HEAT_SHIMMER);
        }
        
        initialVFX.spawn();
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                ticks++;
                
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                Location playerLoc = player.getLocation();
                
                // Spawn dome particles with VFX system every 5 ticks
                if (ticks % 5 == 0) {
                    new VFXLayerBuilder(plugin, playerLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, 20, ParticlePattern.SPHERE, radius, radius/2, radius, 0.05, null)
                        .secondary(Particle.END_ROD, 8, ParticlePattern.RING, radius, radius/2, radius, 0.02, null)
                        .spawn();
                }
                
                // Damage nearby enemies every 10 ticks (0.5 seconds)
                if (ticks % 10 == 0) {
                    for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;
                            double distance = target.getLocation().distance(playerLoc);
                            
                            if (distance <= radius) {
                                target.damage(1.0, player);  // 0.5 hearts per 0.5 seconds
                                target.setFireTicks(20);
                                
                                // Impact VFX on hit
                                new VFXLayerBuilder(plugin, target.getLocation(), rank, player)
                                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                                    .impact(Particle.LAVA, 10, ParticlePattern.BURST, 0.3, 0.3, 0.3, 0.1, null)
                                    .spawn();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effects
        player.getWorld().playSound(center, Sound.ITEM_FIRECHARGE_USE, 1.5f, 1.0f);
        

    }
}
