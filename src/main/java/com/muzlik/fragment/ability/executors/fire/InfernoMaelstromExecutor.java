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
 * Creates a massive vortex of fire that pulls enemies in
 * 
 * VFX: 5-Layer System with cinematic screen tremor
 * - Core: FLAME spiral vortex
 * - Secondary: END_ROD swirling
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
        
        VFXLayerBuilder initialVFX = new VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: Flame ring - defines the vortex boundary clearly
            .core(Particle.FLAME, coreCount, ParticlePattern.RING, radius * 0.7, 0.3 + (rank * 0.15), radius * 0.7, 0.06 + (rank * 0.02), null)
            // Secondary: END_ROD spiral - creates upward motion feel
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.SPIRAL, radius * 0.5, 1.2 + (rank * 0.3), radius * 0.5, 0.03 + (rank * 0.01), null)
            // Ambient: Smoke - subtle base layer for depth
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.SPHERE, radius * 0.3, 0.6 + (rank * 0.2), radius * 0.3, 0.01, null)
            // Impact: Lava bursts - punctuation at key points
            .impact(Particle.LAVA, impactCount, ParticlePattern.BURST, radius * 0.4, 0.3 + (rank * 0.15), radius * 0.4, 0.05 + (rank * 0.02), null);
        
        // Cinematic shake at rank 2+ - indicates power level
        if (rank >= 2) {
            initialVFX.cinematic(0.15 + (rank * 0.02), CinematicEffect.SCREEN_SHAKE);
        }
        
        initialVFX.spawn();
        
        // Create ring of fire blocks
        for (double angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * (radius * 0.8);
            double z = Math.sin(radians) * (radius * 0.8);
            
            Location fireLoc = center.clone().add(x, 0, z);
            Block block = fireLoc.getBlock();
            
            if (block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType().isSolid()) {
                envManager.placeTemporaryBlock(fireLoc, Material.FIRE, 100, player); // 5 seconds
            }
        }
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                // More chaotic rotation at higher ranks
                double angle = ticks * (0.5 + rank * 0.1);
                
                // More spiral arms at higher ranks
                int spiralArms = 8 + (rank * 2); // 8 → 14 at rank 3
                
                // Spiral VFX every tick - MORE CHAOTIC AT HIGHER RANKS
                for (int i = 0; i < spiralArms; i++) {
                    double x = Math.cos(angle + i * Math.PI * 2 / spiralArms) * radius * (1 - ticks / 100.0);
                    double z = Math.sin(angle + i * Math.PI * 2 / spiralArms) * radius * (1 - ticks / 100.0);
                    Location particleLoc = center.clone().add(x, ticks * 0.05, z);
                    
                    // More particles per spiral at higher ranks
                    int spiralParticles = 3 + rank; // 3 → 6 at rank 3
                    
                    // Core and secondary particles
                    new VFXLayerBuilder(plugin, particleLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, spiralParticles, ParticlePattern.POINT, 0.1 + (rank * 0.05), 0.1 + (rank * 0.05), 0.1 + (rank * 0.05), 0.02 + (rank * 0.01), null)
                        .secondary(Particle.END_ROD, 1 + (rank / 2), ParticlePattern.POINT, 0.05, 0.05, 0.05, 0.01, null)
                        .spawn();
                }
                
                // Ambient smoke more frequently at higher ranks
                int smokeInterval = Math.max(5, 10 - rank); // Every 10 ticks → every 7 ticks at rank 3
                if (ticks % smokeInterval == 0) {
                    new VFXLayerBuilder(plugin, center.clone().add(0, 1, 0), rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .ambient(Particle.SMOKE_LARGE, 20 + (rank * 10), ParticlePattern.SPHERE, radius * 0.5, 1 + (rank * 0.3), radius * 0.5, 0.02 + (rank * 0.01), null)
                        .spawn();
                }
                
                for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        Vector pullVector = center.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.3);
                        target.setVelocity(pullVector);
                        if (ticks % 20 == 0) {
                            target.damage(damage / 5, player);
                            target.setFireTicks(100);
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        center.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 2.0f, 0.8f);
        player.sendMessage("§c🔥 Inferno Maelstrom! §8(Ring of fire)");
    }
}
