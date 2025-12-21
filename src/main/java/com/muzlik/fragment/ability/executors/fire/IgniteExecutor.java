package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ignite - Fire Fragment Mastery Ability
 * Become living flame for 15s: immune to damage, all fire abilities cost 50% less, +50% damage
 * 
 * VFX: Continuous flame aura around player
 */
public class IgniteExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial transformation VFX
        int coreCount = 30 + (rank * 10);
        int secondaryCount = 20 + (rank * 6);
        int ambientCount = 15 + (rank * 5);
        
        VFXLayerBuilder transformVFX = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, coreCount, ParticlePattern.SPHERE, 1.5, 2.0, 1.5, 0.08, null)
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.SPHERE, 1.0, 3.0, 1.0, 0.06, null)
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.POINT, 0.8, 1.0, 0.8, 0.02, null);
        
        if (rank >= 6) {
            transformVFX.cinematic(0.25, CinematicEffect.HEAT_SHIMMER);
        }
        
        transformVFX.spawn();
        
        // Apply effects
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0)); // 15 seconds
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 3)); // Near immunity
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0)); // +50% damage
        
        // Continuous flame aura
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 300; // 15 seconds
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    // End transformation
                    player.sendMessage("§6🔥 §eYour flame form fades away");
                    cancel();
                    return;
                }
                
                // Continuous flame aura every 10 ticks
                if (ticks % 10 == 0) {
                    Location playerLoc = player.getLocation();
                    
                    // Minimal continuous aura
                    int auraCore = 5 + (rank / 2);
                    int auraSecondary = 3 + (rank / 3);
                    
                    new VFXLayerBuilder(plugin, playerLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, auraCore, ParticlePattern.SPHERE, 0.8, 1.0, 0.8, 0.03, null)
                        .secondary(Particle.END_ROD, auraSecondary, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.02, null)
                        .spawn();
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effects
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.2f);
        loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.5f);
        
        player.sendMessage("§6🔥 §eYou become one with the flames!");
    }
}