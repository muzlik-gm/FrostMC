package com.muzlik.fragment.ability.executors.earth;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Stone Fist - Earth Fragment Primary Ability
 * Powerful melee strike
 * 
 * VFX: 5-Layer System
 * - Core: BLOCK_DUST impact
 * - Secondary: SWEEP_ATTACK strike
 * - Ambient: Ground rumble
 * - Impact: Debris burst
 * - Cinematic: Ground shake at rank 4+
 */
public class StoneFistExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 4.0;  // Reduced from 14.0 to 4.0 (2 hearts)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // FIXED: Use proper radius scaling instead of hardcoded 3x3x3
        double baseRadius = 4.0; // Base 4 block radius
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                target.setVelocity(new Vector(0, 1, 0));
            }
        }
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // FOCUSED IMPACT VFX - clean, satisfying punch effect
        // Reduced counts + VFXLayerBuilder multiplier = proportional VFX to ability power
        int coreCount = 20 + (rank * 5);       // 20 → 35 at rank 3 (was 50)
        int secondaryCount = 8 + (rank * 3);   // 8 → 17 at rank 3 (was 20)
        int ambientCount = 10 + (rank * 3);    // 10 → 19 at rank 3 (was 30)
        int impactCount = 15 + (rank * 4);     // 15 → 27 at rank 3 (was 40)
        
        // 5-Layer VFX System - Satisfying impact aesthetic
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: BLOCK_DUST - focused ground crack
            .core(Particle.BLOCK_DUST, coreCount, ParticlePattern.BURST, 0.6, 0.4, 0.6, 0.06, Material.STONE.createBlockData())
            // Secondary: SWEEP_ATTACK - single strike visual
            .secondary(Particle.SWEEP_ATTACK, secondaryCount, ParticlePattern.RING, 1.0, 0.3, 1.0, 0.03, null)
            // Ambient: Ground rumble - subtle dust
            .ambient(Particle.SMOKE_NORMAL, ambientCount, ParticlePattern.POINT, 0.8, 0.3, 0.8, 0.01, null)
            // Impact: Debris - few satisfying rock chunks
            .impact(Particle.BLOCK_CRACK, impactCount, ParticlePattern.BURST, 0.8, 0.5, 0.8, 0.08, Material.STONE.createBlockData());
        
        // Cinematic: Ground shake at rank 4+
        if (rank >= 4) {
            vfxBuilder.cinematic(0.15, CinematicEffect.SCREEN_SHAKE);
        }
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2.0f, 0.5f);
        player.sendMessage("§7⛰ Stone Fist!");
    }
}
