package com.muzlik.fragment.ability.executors.air;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Gale Step - Air Fragment Secondary Ability
 * Dash forward with wind speed
 * 
 * VFX: 5-Layer System with gust effects
 * - Core: CLOUD burst
 * - Secondary: END_ROD trail
 * - Ambient: Dust swirls
 * - Impact: SWEEP_ATTACK gust
 * - Cinematic: Wind distortion at rank 5+
 */
public class GaleStepExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = context.getDirection();
        int rank = context.getRank();
        double baseRange = 10.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        
        player.setVelocity(direction.multiply(range / 5.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 2));
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: CLOUD burst
            .core(Particle.CLOUD, 50, ParticlePattern.BURST, 1, 1, 1, 0.2, null)
            // Secondary: END_ROD trail
            .secondary(Particle.END_ROD, 30, ParticlePattern.SPHERE, 0.8, 0.8, 0.8, 0.1, null)
            // Ambient: Dust swirls
            .ambient(Particle.SMOKE_NORMAL, 25, ParticlePattern.SPIRAL, 1.2, 1.2, 1.2, 0.05, null)
            // Impact: SWEEP_ATTACK gust
            .impact(Particle.SWEEP_ATTACK, 20, ParticlePattern.BURST, 1, 1, 1, 0.15, null);
        
        // Cinematic: Wind distortion at rank 5+
        if (rank >= 5) {
            vfxBuilder.cinematic(0.2, CinematicEffect.WIND_DISTORTION);
        }
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);
        player.sendMessage("§f💨 Gale Step!");
    }
}
