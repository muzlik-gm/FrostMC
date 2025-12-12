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
 * Armor - Air Fragment Advanced Ability
 * Surrounds self with wind barrier that reflects projectiles and damages melee attackers
 * 
 * VFX: Continuous wind aura around player
 */
public class ArmorExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 12.0;
        int durationTicks = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial activation VFX
        VFXLayerBuilder activationVFX = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.CLOUD, 60, ParticlePattern.SPHERE, 2, 2, 2, 0.15, null)
            .secondary(Particle.END_ROD, 40, ParticlePattern.RING, 1.5, 1, 1.5, 0.1, null)
            .ambient(Particle.WHITE_ASH, 30, ParticlePattern.SPHERE, 2.5, 2, 2.5, 0.05, null)
            .cinematic(0.2, CinematicEffect.WIND_DISTORTION);
        
        activationVFX.spawn();
        
        // Apply protective effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, durationTicks, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 0));
        
        // Continuous wind armor effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    player.sendMessage("§f💨 §7Your wind armor fades away");
                    cancel();
                    return;
                }
                
                // Continuous aura every 20 ticks (1 second)
                if (ticks % 20 == 0) {
                    Location playerLoc = player.getLocation();
                    
                    new VFXLayerBuilder(plugin, playerLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.CLOUD, 8, ParticlePattern.RING, 1.5, 0.5, 1.5, 0.05, null)
                        .secondary(Particle.WHITE_ASH, 5, ParticlePattern.SPHERE, 1.2, 1, 1.2, 0.02, null)
                        .spawn();
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1.0f, 1.5f);
        
        player.sendMessage("§f💨 §7Wind armor activated!");
    }
}