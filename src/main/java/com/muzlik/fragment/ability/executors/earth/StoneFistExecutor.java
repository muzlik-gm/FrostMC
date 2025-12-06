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
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: BLOCK_DUST impact
            .core(Particle.BLOCK_DUST, 50, ParticlePattern.BURST, 1, 1, 1, 0.1, Material.STONE.createBlockData())
            // Secondary: SWEEP_ATTACK strike
            .secondary(Particle.SWEEP_ATTACK, 20, ParticlePattern.RING, 1.5, 0.5, 1.5, 0.05, null)
            // Ambient: Ground rumble
            .ambient(Particle.SMOKE_NORMAL, 30, ParticlePattern.POINT, 1, 0.5, 1, 0.02, null)
            // Impact: Debris burst
            .impact(Particle.BLOCK_CRACK, 40, ParticlePattern.BURST, 1.5, 1, 1.5, 0.15, Material.STONE.createBlockData());
        
        // Cinematic: Ground shake at rank 4+
        if (rank >= 4) {
            vfxBuilder.cinematic(0.15, CinematicEffect.SCREEN_SHAKE);
        }
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2.0f, 0.5f);
        player.sendMessage("§7⛰ Stone Fist!");
    }
}
