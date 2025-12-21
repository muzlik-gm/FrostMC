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
 * Cinematic VFX for Water Fragment abilities
 * Inspired by: Avatar water bending, ocean waves, koi fish
 */
public class WaterAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public WaterAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Aqua Pulse VFX - Expanding ripple rings with droplet fountains
     */
    public void aquaPulse(Player player, Location center, int rank) {
        // Create water magic circle
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.WATER, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 40);
        
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_SPLASH, 1.5f, 1.2f);
        
        // Expanding ripples
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                double radius = ticks * 0.3;
                int points = 16 + (rank * 2);
                
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location rippleLoc = center.clone().add(x, 0.1, z);
                    vfxEngine.spawnParticle(rippleLoc, Particle.WATER_SPLASH, 2, 0.1, 0.1, 0.1, 0.02, player);
                    
                    if (rank >= 5 && ticks % 5 == 0) {
                        vfxEngine.spawnParticle(rippleLoc, Particle.DOLPHIN, 1, 0.05, 0.05, 0.05, 0.01, player);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Tidal Shield VFX - Curved water wall with flow lines
     */
    public void tidalShield(Player player, Location center, double radius, int durationTicks, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.WATER, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, durationTicks);
        
        center.getWorld().playSound(center, Sound.ITEM_BUCKET_EMPTY, 1.5f, 1.0f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Water wall particles
                int segments = 8 + rank;
                for (int i = 0; i < segments; i++) {
                    double angle = (i * Math.PI * 2 / segments);
                    for (double h = 0; h < radius; h += 0.3) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        
                        Location wallLoc = center.clone().add(x, h, z);
                        vfxEngine.spawnParticle(wallLoc, Particle.WATER_DROP, 1, 0.05, 0.05, 0.05, 0.01, player);
                        
                        if (rank >= 7 && ticks % 10 == 0) {
                            vfxEngine.spawnParticle(wallLoc, Particle.DOLPHIN, 1, 0.1, 0.1, 0.1, 0.02, player);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
    
    /**
     * Tsunami Wave VFX - Crescent wave with foam crest
     */
    public void tsunamiWave(Player player, Location start, org.bukkit.util.Vector direction, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.WATER, start.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 20);
        
        start.getWorld().playSound(start, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.8f);
        
        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = start.clone();
            
            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }
                
                currentLoc.add(direction.clone().multiply(0.5));
                
                // Wave crest
                double waveHeight = 2.0 + (rank * 0.3);
                for (double h = 0; h < waveHeight; h += 0.2) {
                    Location waveLoc = currentLoc.clone().add(0, h, 0);
                    vfxEngine.spawnParticle(waveLoc, Particle.WATER_SPLASH, 3, 0.3, 0.1, 0.3, 0.05, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
