package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Draconic Wings - Dragon Fragment Flight Ability
 * Grants 30 minutes of flight with dragon breath carpet effects
 */
public class DraconicWingsExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Start flight via FlightManager (30 seconds for Dragon)
        plugin.getFlightManager().startFlight(player, FragmentType.DRAGON, rank);
        
        // Add speed buff for flight duration only (30 seconds)
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SPEED, 600, 3));
        // REMOVED: SLOW_FALLING effect - fall damage immunity is handled by FlightManager only while flying
        
        // Reduced VFX for activation
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: SOUL_FIRE_FLAME (replaces DRAGON_BREATH)
            .core(Particle.SOUL_FIRE_FLAME, 30, ParticlePattern.SPHERE, 2, 2, 2, 0.15, null)
            // Secondary: FLAME wings
            .secondary(Particle.FLAME, 20, ParticlePattern.SPIRAL, 2.5, 3.0, 2.5, 0.12, null)
            // Ambient: END_ROD aura
            .ambient(Particle.END_ROD, 15, ParticlePattern.RING, 3, 2, 3, 0.08, null);
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.2f);
    }
}
