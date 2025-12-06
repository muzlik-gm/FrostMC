package com.muzlik.fragment.ability.executors.air;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Tempest Barrage - Air Fragment Ultimate Ability
 * Rapid-fire wind projectiles
 * 
 * VFX: 5-Layer System with slow-motion for final strike
 * - Core: CLOUD rapid bursts
 * - Secondary: END_ROD trails
 * - Ambient: Dust swirls
 * - Impact: SWEEP_ATTACK hits
 * - Cinematic: Slow-motion on final strike
 */
public class TempestBarrageExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 3.0; // EXTREME NERF: 6.0 → 3.0 (1.5 hearts per blade, ultimate multi-hit)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 8) {
                    cancel();
                    return;
                }
                Vector direction = player.getLocation().getDirection();
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(direction.multiply(3.0));
                arrow.setDamage(damage);
                
                // VFX for each shot
                VFXLayerBuilder shotVFX = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .core(Particle.CLOUD, 10, ParticlePattern.POINT, 0.2, 0.2, 0.2, 0.05, null)
                    .secondary(Particle.END_ROD, 5, ParticlePattern.POINT, 0.1, 0.1, 0.1, 0.02, null)
                    .ambient(Particle.SMOKE_NORMAL, 8, ParticlePattern.POINT, 0.3, 0.3, 0.3, 0.02, null);
                
                // Final strike gets cinematic effect
                if (count == 7) {
                    shotVFX.impact(Particle.SWEEP_ATTACK, 15, ParticlePattern.BURST, 0.5, 0.5, 0.5, 0.1, null)
                           .cinematic(0.2, CinematicEffect.SLOW_MOTION);
                }
                
                shotVFX.spawn();
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
    }
}
