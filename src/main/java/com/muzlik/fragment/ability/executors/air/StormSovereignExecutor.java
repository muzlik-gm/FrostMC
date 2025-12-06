package com.muzlik.fragment.ability.executors.air;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Storm Sovereign - Air Fragment Ultimate Transformation
 * Grants 10 minutes of flight with visual carpet effects
 * 
 * VFX: 5-Layer System with wind ribbons cinematic
 * - Core: CLOUD storm
 * - Secondary: END_ROD wind streams
 * - Ambient: Dust vortex
 * - Impact: SWEEP_ATTACK burst
 * - Cinematic: Wind ribbons effect
 */
public class StormSovereignExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Start flight via FlightManager (10 minutes for Air)
        plugin.getFlightManager().startFlight(player, FragmentType.AIR, rank);
        
        // Add speed buff for the full duration
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600 * 20, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600 * 20, 1, false, false));
        
        // 5-Layer VFX System for activation
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: CLOUD storm
            .core(Particle.CLOUD, 200, ParticlePattern.SPHERE, 2, 2, 2, 0.2, null)
            // Secondary: END_ROD wind streams
            .secondary(Particle.END_ROD, 120, ParticlePattern.SPIRAL, 2.5, 3.0, 2.5, 0.15, null)
            // Ambient: Dust vortex
            .ambient(Particle.SMOKE_NORMAL, 100, ParticlePattern.RING, 3, 2, 3, 0.1, null)
            // Impact: SWEEP_ATTACK burst
            .impact(Particle.SWEEP_ATTACK, 80, ParticlePattern.BURST, 2, 2, 2, 0.2, null)
            // Cinematic: Wind ribbons effect
            .cinematic(0.3, CinematicEffect.WIND_DISTORTION);
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.5f);
    }
}
