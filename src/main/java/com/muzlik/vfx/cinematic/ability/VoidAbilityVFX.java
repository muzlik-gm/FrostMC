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
 * Cinematic VFX for Void Fragment abilities
 * Inspired by: Doctor Strange portals, reality warping, dimensional rifts
 */
public class VoidAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public VoidAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Void Slash VFX - Purple rift line with dimension fragments
     */
    public void voidSlash(Player player, Location start, org.bukkit.util.Vector direction, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.VOID, start.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 15);
        
        start.getWorld().playSound(start, Sound.BLOCK_PORTAL_AMBIENT, 1.5f, 1.5f);
        
        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = start.clone();
            
            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }
                
                currentLoc.add(direction.clone().multiply(0.5));
                
                // Purple rift
                vfxEngine.spawnParticle(currentLoc, Particle.PORTAL, 5, 0.2, 0.2, 0.2, 0.5, player);
                vfxEngine.spawnParticle(currentLoc, Particle.REVERSE_PORTAL, 3, 0.15, 0.15, 0.15, 0.3, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Blink Step VFX - Doctor Strange style rotating mandala portals
     */
    public void blinkStep(Player player, Location from, Location to, int rank) {
        // Entry portal
        MagicCircle entryCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.VOID, from.clone(), rank, player
        );
        vfxEngine.spawnMagicCircle(entryCircle, 20);
        
        // Exit portal
        MagicCircle exitCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.VOID, to.clone(), rank, player
        );
        vfxEngine.spawnMagicCircle(exitCircle, 20);
        
        from.getWorld().playSound(from, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.5f);
        to.getWorld().playSound(to, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.5f);
        
        // Portal tunnel effect
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                double progress = ticks / 20.0;
                Location midLoc = from.clone().add(
                    (to.getX() - from.getX()) * progress,
                    (to.getY() - from.getY()) * progress,
                    (to.getZ() - from.getZ()) * progress
                );
                
                vfxEngine.spawnParticle(midLoc, Particle.PORTAL, 10, 0.3, 0.3, 0.3, 1.0, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Dimensional Collapse VFX - Imploding sphere with black-hole visual
     */
    public void dimensionalCollapse(Player player, Location center, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.VOID, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 60);
        
        center.getWorld().playSound(center, Sound.BLOCK_PORTAL_TRIGGER, 2.0f, 0.5f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }
                
                // Imploding particles
                double radius = 3.0 * (1.0 - ticks / 60.0);
                int points = 20;
                
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location implosionLoc = center.clone().add(x, 0, z);
                    vfxEngine.spawnParticle(implosionLoc, Particle.PORTAL, 3, 0.1, 0.1, 0.1, 0.5, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
