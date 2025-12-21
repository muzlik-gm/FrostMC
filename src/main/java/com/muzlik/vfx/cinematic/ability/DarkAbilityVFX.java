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
 * Cinematic VFX for Dark Fragment abilities
 * Inspired by: Shadow manipulation, void energy, eclipse
 */
public class DarkAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public DarkAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Shadow Strike VFX - Dark slash with shadow tendrils
     */
    public void shadowStrike(Player player, Location start, org.bukkit.util.Vector direction, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.DARK, start.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 15);
        
        start.getWorld().playSound(start, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.8f);
        
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
                
                // Shadow slash
                vfxEngine.spawnParticle(currentLoc, Particle.SMOKE_LARGE, 5, 0.3, 0.3, 0.3, 0.02, player);
                
                if (rank >= 7) {
                    vfxEngine.spawnParticle(currentLoc, Particle.SCULK_SOUL, 2, 0.2, 0.2, 0.2, 0.01, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Abyssal Void VFX - Void-eye magic circle with shadow tendrils
     */
    public void abyssalVoid(Player player, Location center, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.DARK, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 100);
        
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_AMBIENT, 1.5f, 0.6f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                
                // Shadow tendrils
                for (int i = 0; i < 6; i++) {
                    double angle = (i * Math.PI * 2 / 6) + (ticks * 0.1);
                    double radius = 2.0 + Math.sin(ticks * 0.1) * 0.5;
                    
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location tendrilLoc = center.clone().add(x, Math.sin(ticks * 0.2) * 0.5, z);
                    vfxEngine.spawnParticle(tendrilLoc, Particle.SMOKE_LARGE, 2, 0.1, 0.1, 0.1, 0.01, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
