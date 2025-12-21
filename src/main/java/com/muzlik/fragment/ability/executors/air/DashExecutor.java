package com.muzlik.fragment.ability.executors.air;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.SafeTeleport;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Dash - Air Fragment Secondary Ability
 * Dash forward with wind speed
 * 
 * VFX: 5-Layer System with gust effects
 * - Core: CLOUD burst
 * - Secondary: END_ROD trail
 * - Ambient: Dust swirls
 * - Impact: SWEEP_ATTACK gust
 * - Cinematic: Wind distortion at rank 5+
 */
public class DashExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = context.getDirection();
        int rank = context.getRank();
        double baseRange = 10.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        
        // Calculate velocity and make it safe
        Vector velocity = direction.multiply(range / 5.0);
        Vector safeVelocity = SafeTeleport.getSafeVelocity(velocity, player);
        
        player.setVelocity(safeVelocity);
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SPEED, 80, 2));
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: CLOUD burst
            .core(Particle.CLOUD, 50, ParticlePattern.BURST, 1, 1, 1, 0.2, null)
            // Secondary: END_ROD trail
            .secondary(Particle.END_ROD, 30, ParticlePattern.SPHERE, 0.8, 0.8, 0.8, 0.1, null)
            // Ambient: Dust swirls
            .ambient(Particle.WHITE_ASH, 20, ParticlePattern.RING, 1.2, 0.5, 1.2, 0.05, null)
            // Impact: SWEEP_ATTACK gust
            .impact(Particle.SWEEP_ATTACK, 15, ParticlePattern.BURST, 1, 1, 1, 0.15, null)
            // Magic circle - air dash theme
            .withMagicCircle(FragmentType.AIR, 1.8 + (rank * 0.15), 20 + (rank * 3));
        
        // Cinematic: Wind distortion at rank 5+
        if (rank >= 5) {
            vfxBuilder.cinematic(0.15, CinematicEffect.WIND_DISTORTION);
        }
        
        vfxBuilder.spawn();
        
        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.8f, 2.0f);
    }
}