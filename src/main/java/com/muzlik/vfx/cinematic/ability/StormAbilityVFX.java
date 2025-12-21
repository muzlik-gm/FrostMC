package com.muzlik.vfx.cinematic.ability;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.cinematic.CinematicVFXEngine;
import com.muzlik.vfx.cinematic.MagicCircle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Cinematic VFX for Storm Fragment abilities
 * Inspired by: Lightning strikes, thunder clouds, Zeus
 */
public class StormAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public StormAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Lightning Bolt VFX - Branching lightning with spark-burst impact
     */
    public void lightningBolt(Player player, Location target, int rank) {
        Location skyLoc = target.clone().add(0, 20, 0);
        
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.STORM, skyLoc, rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 10);
        
        target.getWorld().playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
        
        // Lightning bolt path
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 10) {
                    // Impact burst
                    vfxEngine.getExplosionEffect().createExplosion(target, 2.0, Particle.ELECTRIC_SPARK, 50, player);
                    cancel();
                    return;
                }
                
                double progress = ticks / 10.0;
                Location boltLoc = skyLoc.clone().add(
                    0,
                    -(20 * progress),
                    0
                );
                
                vfxEngine.spawnParticle(boltLoc, Particle.ELECTRIC_SPARK, 5, 0.2, 0.2, 0.2, 0.1, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Storm Surge VFX - Storm magic circle with coordinated strikes
     */
    public void stormSurge(Player player, Location center, double radius, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.STORM, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 100);
        
        center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 0.8f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                
                // Random lightning strikes
                if (ticks % 10 == 0) {
                    double angle = Math.random() * Math.PI * 2;
                    double r = Math.random() * radius;
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    
                    Location strikeLoc = center.clone().add(x, 0, z);
                    lightningBolt(player, strikeLoc, rank);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
