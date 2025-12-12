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
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Flight - Air Fragment Mastery Ability
 * Fly freely for 10 minutes with +100% speed
 * 
 * VFX: Continuous wind effects while flying
 */
public class FlightExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 600.0; // 10 minutes
        int durationTicks = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Dramatic activation VFX
        VFXLayerBuilder activationVFX = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.CLOUD, 100, ParticlePattern.SPHERE, 3, 4, 3, 0.2, null)
            .secondary(Particle.END_ROD, 60, ParticlePattern.SPIRAL, 2, 5, 2, 0.15, null)
            .ambient(Particle.WHITE_ASH, 80, ParticlePattern.BURST, 3, 3, 3, 0.1, null)
            .impact(Particle.SWEEP_ATTACK, 40, ParticlePattern.RING, 2.5, 1, 2.5, 0.2, null)
            .cinematic(0.3, CinematicEffect.WIND_DISTORTION);
        
        activationVFX.spawn();
        
        // Enable flight
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1)); // +100% speed
        
        // Flight duration tracker
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    // End flight
                    if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                    player.sendMessage("§f💨 §7Your flight ability has ended");
                    cancel();
                    return;
                }
                
                // Flight VFX every 40 ticks (2 seconds) when flying
                if (ticks % 40 == 0 && player.isFlying()) {
                    Location playerLoc = player.getLocation();
                    
                    new VFXLayerBuilder(plugin, playerLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.CLOUD, 12, ParticlePattern.RING, 1, 0.3, 1, 0.03, null)
                        .secondary(Particle.WHITE_ASH, 8, ParticlePattern.POINT, 0.8, 0.5, 0.8, 0.02, null)
                        .spawn();
                }
                
                // Time warnings
                int remainingMinutes = (durationTicks - ticks) / 1200; // 1200 ticks = 1 minute
                if (remainingMinutes == 2 && ticks % 1200 == 0) {
                    player.sendMessage("§f💨 §e2 minutes of flight remaining");
                } else if (remainingMinutes == 1 && ticks % 1200 == 0) {
                    player.sendMessage("§f💨 §c1 minute of flight remaining");
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 2.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
        
        player.sendMessage("§f💨 §aFlight activated! You can fly for 10 minutes");
    }
}