package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Eternal Flame - Fire Fragment Ultimate Transformation
 * Become living fire with massive buffs
 * 
 * VFX: 5-Layer System with color tint cinematic
 * - Core: FLAME aura
 * - Secondary: END_ROD radiance
 * - Ambient: SMOKE_LARGE heat waves
 * - Impact: LAVA explosion
 * - Cinematic: Heat shimmer color tint
 */
public class EternalFlameExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 15.0;
        int duration = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: FLAME aura
            .core(Particle.FLAME, 200, ParticlePattern.SPHERE, 2.0, 2.0, 2.0, 0.2, null)
            // Secondary: END_ROD radiance
            .secondary(Particle.END_ROD, 100, ParticlePattern.RING, 1.5, 1.0, 1.5, 0.1, null)
            // Ambient: SMOKE_LARGE heat waves
            .ambient(Particle.SMOKE_LARGE, 80, ParticlePattern.SPIRAL, 2.5, 3.0, 2.5, 0.05, null)
            // Impact: LAVA explosion
            .impact(Particle.LAVA, 60, ParticlePattern.BURST, 2.0, 2.0, 2.0, 0.15, null)
            // Cinematic: Heat shimmer color tint
            .cinematic(0.3, CinematicEffect.HEAT_SHIMMER);
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 2.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 0.8f);
        
    }
}
