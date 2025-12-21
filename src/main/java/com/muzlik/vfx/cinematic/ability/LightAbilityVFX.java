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
 * Cinematic VFX for Light Fragment abilities
 * Inspired by: Divine light, halos, angel wings
 */
public class LightAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public LightAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Radiant Lance VFX - Golden beam with star-burst trail
     */
    public void radiantLance(Player player, Location start, org.bukkit.util.Vector direction, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.LIGHT, start.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 20);
        
        start.getWorld().playSound(start, Sound.BLOCK_BELL_USE, 1.5f, 1.5f);
        
        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = start.clone();
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                currentLoc.add(direction.clone().multiply(0.7));
                
                // Golden beam
                vfxEngine.spawnParticle(currentLoc, Particle.END_ROD, 3, 0.1, 0.1, 0.1, 0.02, player);
                vfxEngine.spawnParticle(currentLoc, Particle.GLOW, 2, 0.15, 0.15, 0.15, 0.01, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Holy Sanctuary VFX - Cathedral-window pattern dome
     */
    public void holySanctuary(Player player, Location center, double radius, int durationTicks, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.LIGHT, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, durationTicks);
        
        center.getWorld().playSound(center, Sound.BLOCK_BELL_RESONATE, 2.0f, 1.2f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Dome edges
                int segments = 8 + rank;
                for (int i = 0; i < segments; i++) {
                    double angle = (i * Math.PI * 2 / segments);
                    for (double h = 0; h < radius; h += 0.4) {
                        double currentRadius = radius * Math.sin(Math.acos(h / radius));
                        double x = Math.cos(angle) * currentRadius;
                        double z = Math.sin(angle) * currentRadius;
                        
                        Location domeLoc = center.clone().add(x, h, z);
                        vfxEngine.spawnParticle(domeLoc, Particle.END_ROD, 1, 0.05, 0.05, 0.05, 0.01, player);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
