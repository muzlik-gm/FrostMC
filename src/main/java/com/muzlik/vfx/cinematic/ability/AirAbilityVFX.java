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
 * Cinematic VFX for Air Fragment abilities
 * Inspired by: Avatar airbending, wind ribbons, cyclones
 */
public class AirAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public AirAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Wind Blade VFX - Crescent arc with wind ribbons
     */
    public void windBlade(Player player, Location start, org.bukkit.util.Vector direction, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.AIR, start.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 15);
        
        start.getWorld().playSound(start, Sound.ENTITY_BREEZE_SHOOT, 1.5f, 1.2f);
        
        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = start.clone();
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                currentLoc.add(direction.clone().multiply(0.6));
                
                // Crescent blade
                for (int i = -3; i <= 3; i++) {
                    double offset = i * 0.3;
                    Location bladeLoc = currentLoc.clone().add(0, offset, 0);
                    vfxEngine.spawnParticle(bladeLoc, Particle.CLOUD, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Tempest Barrage VFX - Rotating magic circle with tornado
     */
    public void tempestBarrage(Player player, Location center, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.AIR, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 80);
        
        center.getWorld().playSound(center, Sound.ENTITY_BREEZE_SHOOT, 2.0f, 0.9f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 80) {
                    cancel();
                    return;
                }
                
                // Tornado spiral
                double height = ticks * 0.1;
                double angle = ticks * 0.3;
                double radius = 1.5 - (height * 0.05);
                
                for (int i = 0; i < 4; i++) {
                    double spiralAngle = angle + (i * Math.PI / 2);
                    double x = Math.cos(spiralAngle) * radius;
                    double z = Math.sin(spiralAngle) * radius;
                    
                    Location tornadoLoc = center.clone().add(x, height, z);
                    vfxEngine.spawnParticle(tornadoLoc, Particle.CLOUD, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
