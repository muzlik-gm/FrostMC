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

/**
 * Cyclone Armor - Air Fragment Defensive Ability
 * Protective wind shield
 * 
 * VFX: 5-Layer System with rotating wind
 * - Core: CLOUD cyclone
 * - Secondary: END_ROD shimmer
 * - Ambient: Dust swirls
 * - Impact: SWEEP_ATTACK deflection
 * - Cinematic: Wind distortion at rank 5+
 */
public class CycloneArmorExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 15.0;
        int duration = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 1));
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: CLOUD cyclone
            .core(Particle.CLOUD, 100, ParticlePattern.SPIRAL, 1.5, 2.0, 1.5, 0.1, null)
            // Secondary: END_ROD shimmer
            .secondary(Particle.END_ROD, 50, ParticlePattern.RING, 1.5, 1.0, 1.5, 0.05, null)
            // Ambient: Dust swirls
            .ambient(Particle.SMOKE_NORMAL, 60, ParticlePattern.SPHERE, 1.8, 1.5, 1.8, 0.05, null)
            // Impact: SWEEP_ATTACK deflection
            .impact(Particle.SWEEP_ATTACK, 40, ParticlePattern.BURST, 1.5, 1.5, 1.5, 0.1, null);
        
        // Cinematic: Wind distortion at rank 5+
        if (rank >= 5) {
            vfxBuilder.cinematic(0.2, CinematicEffect.WIND_DISTORTION);
        }
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 1.0f);
    }
}
