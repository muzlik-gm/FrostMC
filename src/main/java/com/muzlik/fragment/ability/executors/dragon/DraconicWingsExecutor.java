package com.muzlik.fragment.ability.executors.dragon;

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
 * Draconic Wings - Dragon Fragment Flight Ability
 * Grants 30 minutes of flight with dragon breath carpet effects
 */
public class DraconicWingsExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Start flight via FlightManager (30 minutes for Dragon)
        plugin.getFlightManager().startFlight(player, FragmentType.DRAGON, rank);
        
        // Add speed buff and fall damage immunity for full duration
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1800 * 20, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1800 * 20, 0, false, false));
        
        // 5-Layer VFX System for activation
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: DRAGON_BREATH
            .core(Particle.DRAGON_BREATH, 150, ParticlePattern.SPHERE, 2, 2, 2, 0.15, null)
            // Secondary: FLAME wings
            .secondary(Particle.FLAME, 100, ParticlePattern.SPIRAL, 2.5, 3.0, 2.5, 0.12, null)
            // Ambient: END_ROD aura
            .ambient(Particle.END_ROD, 80, ParticlePattern.RING, 3, 2, 3, 0.08, null)
            // Impact: LAVA burst
            .impact(Particle.LAVA, 60, ParticlePattern.BURST, 2, 2, 2, 0.15, null)
            // Cinematic: Screen shake
            .cinematic(0.4, CinematicEffect.SCREEN_SHAKE);
        
        vfxBuilder.spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.2f);
        player.sendMessage("§5§l🐉 ᴅʀᴀᴄᴏɴɪᴄ ᴡɪɴɢs! §7ᴛʜᴇ sᴋʏ ɪs ʏᴏᴜʀs!");
    }
}
