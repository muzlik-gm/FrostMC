package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Phoenix Rebirth - Fire Fragment Ultimate Ability
 * Grants resurrection upon death
 * 
 * VFX: 5-Layer System with slow-motion cinematic
 * - Core: FLAME rising phoenix
 * - Secondary: END_ROD wings
 * - Ambient: SMOKE_LARGE aura
 * - Impact: LAVA burst
 * - Cinematic: Slow-motion effect
 */
public class PhoenixRebirthExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: FLAME rising phoenix
            .core(Particle.FLAME, 100, ParticlePattern.SPIRAL, 1.0, 2.0, 1.0, 0.1, null)
            // Secondary: END_ROD wings
            .secondary(Particle.END_ROD, 60, ParticlePattern.SPHERE, 1.5, 1.0, 1.5, 0.05, null)
            // Ambient: SMOKE_LARGE aura
            .ambient(Particle.SMOKE_LARGE, 40, ParticlePattern.RING, 1.2, 0.5, 1.2, 0.02, null)
            // Impact: LAVA burst
            .impact(Particle.LAVA, 50, ParticlePattern.BURST, 1.0, 1.0, 1.0, 0.1, null)
            // Cinematic: Slow-motion effect
            .cinematic(0.3, CinematicEffect.SLOW_MOTION);
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.8f);
        
        player.sendMessage("§6🔥 Phoenix Rebirth activated! You will revive upon death.");
    }
}
