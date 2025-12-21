package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.FragmentType;
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
 * Inferno - Fire Fragment Ultimate Ability
 * Creates a massive vortex of fire that pulls enemies in
 * 
 * VFX: 5-Layer System with cinematic screen tremor
 * - Core: FLAME spiral vortex
 * - Secondary: END_ROD swirling
 * - Ambient: SMOKE_LARGE rising
 * - Impact: LAVA bursts
 * - Cinematic: Screen tremor at rank 5+
 */
public class InfernoExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        int rank = context.getRank();
        double baseRadius = 6.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 5.0; // EXTREME NERF: 8.0 → 5.0 (2.5 hearts, ultimate DoT vortex)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        EnvironmentManager envManager = plugin.getEnvironmentManager();
        
        // QUALITY-FOCUSED initial VFX - dramatic but controlled
        // These are reduced counts that still look impressive through pattern design
        int coreCount = 35 + (rank * 12);      // 35 → 71 at rank 3 (was 100 → 220)
        int secondaryCount = 18 + (rank * 8);  // 18 → 42 at rank 3 (was 50 → 140)
        int ambientCount = 12 + (rank * 6);    // 12 → 30 at rank 3 (was 40 → 115)
        int impactCount = 10 + (rank * 5);     // 10 → 25 at rank 3 (was 30 → 90)
        
        // Initial dramatic burst
        VFXLayerBuilder initialVFX = new VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, coreCount, ParticlePattern.SPIRAL, radius * 0.8, 2.0, radius * 0.8, 0.08, null)
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.SPIRAL, radius * 0.6, 1.5, radius * 0.6, 0.06, null)
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.SPHERE, radius * 0.4, 3.0, radius * 0.4, 0.02, null)
            .impact(Particle.LAVA, impactCount, ParticlePattern.BURST, radius, 0.5, radius, 0.1, null)
            // Magic circle - fire vortex theme, larger for ultimate
            .withMagicCircle(FragmentType.FIRE, radius * 0.8, 100);
        
        // Cinematic effects at higher ranks
        if (rank >= 3) {
            initialVFX.cinematic(0.2 + (rank * 0.05), CinematicEffect.SCREEN_SHAKE);
        }
        
        initialVFX.spawn();
        
        // Create sustained vortex effect
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 100; // 5 seconds
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    cancel();
                    return;
                }
                
                // Continuous vortex VFX - REDUCED for performance
                if (ticks % 4 == 0) { // Every 4 ticks instead of every tick
                    // Minimal sustained counts - just enough to maintain the effect
                    int sustainedCore = 8 + (rank * 2);     // 8 → 14 at rank 3 (was 25 constant)
                    int sustainedSecondary = 4 + rank;      // 4 → 7 at rank 3 (was 12 constant)
                    int sustainedAmbient = 3 + rank;        // 3 → 6 at rank 3 (was 8 constant)
                    
                    new VFXLayerBuilder(plugin, center, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, sustainedCore, ParticlePattern.SPIRAL, radius * 0.6, 1.0, radius * 0.6, 0.04, null)
                        .secondary(Particle.END_ROD, sustainedSecondary, ParticlePattern.SPIRAL, radius * 0.4, 0.8, radius * 0.4, 0.03, null)
                        .ambient(Particle.SMOKE_LARGE, sustainedAmbient, ParticlePattern.SPHERE, radius * 0.3, 2.0, radius * 0.3, 0.01, null)
                        .spawn();
                }
                
                // Pull and damage enemies
                for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        
                        // Pull towards center
                        Vector pullVector = center.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.3);
                        target.setVelocity(pullVector);
                        
                        // Damage every 20 ticks (1 second)
                        if (ticks % 20 == 0) {
                            target.damage(damage, player);
                            target.setFireTicks(60);
                        }
                    }
                }
                
                // Environment effects - ignite blocks occasionally
                if (ticks % 10 == 0 && envManager != null) {
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            if (Math.random() < 0.3) {
                                Block block = center.clone().add(x, 0, z).getBlock();
                                if (block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT) {
                                    // Temporarily set block to fire (simplified)
                                    block.setType(Material.FIRE);
                                }
                            }
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effects
        center.getWorld().playSound(center, Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 0.8f);
        center.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 1.5f, 1.0f);
    }
}