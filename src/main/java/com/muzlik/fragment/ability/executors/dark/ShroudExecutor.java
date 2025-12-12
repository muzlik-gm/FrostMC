package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Shroud - Dark Fragment Mastery Ability
 * Become shadow incarnate: invisible, +75% damage, life steal
 */
public class ShroudExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        double baseDuration = 15.0;
        int durationTicks = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial transformation VFX
        VFXLayerBuilder transformVFX = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.SMOKE_LARGE, 80, ParticlePattern.SPHERE, 2, 3, 2, 0.15, null)
            .secondary(Particle.SQUID_INK, 60, ParticlePattern.SPIRAL, 1.5, 4, 1.5, 0.1, null)
            .ambient(Particle.PORTAL, 40, ParticlePattern.RING, 2.5, 2, 2.5, 0.08, null)
            .impact(Particle.SMOKE_NORMAL, 50, ParticlePattern.BURST, 2, 1, 2, 0.05, null)
            .cinematic(0.35, CinematicEffect.DARKNESS_PULSE);
        
        transformVFX.spawn();
        
        // Apply shadow effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, durationTicks, 1)); // +75% damage
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 0)); // Bonus speed
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, 0)); // See in darkness
        
        // Continuous shadow aura
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    player.sendMessage("§5🌫 §dYou emerge from the shadows");
                    cancel();
                    return;
                }
                
                // Subtle shadow aura every 30 ticks
                if (ticks % 30 == 0) {
                    Location playerLoc = player.getLocation();
                    
                    new VFXLayerBuilder(plugin, playerLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.SMOKE_LARGE, 6, ParticlePattern.SPHERE, 0.8, 1, 0.8, 0.02, null)
                        .secondary(Particle.SQUID_INK, 4, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.01, null)
                        .spawn();
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.6f);
        loc.getWorld().playSound(loc, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 0.8f);
        
        player.sendMessage("§5🌫 §dYou become one with the shadows!");
    }
}