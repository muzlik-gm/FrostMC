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
 * Cinematic VFX for Dragon Fragment abilities
 * Inspired by: Dragon breath, dragon wings, draconic power
 */
public class DragonAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public DragonAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
    }
    
    /**
     * Dragons Roar VFX - Cone breath with dragon-head magic circle
     */
    public void dragonsRoar(Player player, Location start, org.bukkit.util.Vector direction, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.DRAGON, start.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 30);
        
        start.getWorld().playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }
                
                // Cone breath
                double distance = ticks * 0.5;
                double coneWidth = distance * 0.3;
                
                for (int i = -3; i <= 3; i++) {
                    double offset = i * coneWidth / 3;
                    Location breathLoc = start.clone().add(direction.clone().multiply(distance));
                    breathLoc.add(offset, 0, offset);
                    
                    vfxEngine.spawnParticle(breathLoc, Particle.DRAGON_BREATH, 3, 0.2, 0.2, 0.2, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Draconic Wings VFX - Wing-outline particles with scale texture
     */
    public void draconicWings(Player player, int durationTicks, int rank) {
        Location playerLoc = player.getLocation();
        
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.DRAGON, playerLoc.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 20);
        
        playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.0f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                Location currentLoc = player.getLocation();
                double wingSpan = 2.0 + (rank * 0.2);
                double wingAngle = Math.sin(ticks * 0.2) * 0.3;
                
                // Wing particles
                for (int side = -1; side <= 1; side += 2) {
                    for (double w = 0; w < wingSpan; w += 0.3) {
                        double x = side * w * Math.cos(wingAngle);
                        double y = 1.0 - (w * 0.2);
                        
                        Location wingLoc = currentLoc.clone().add(x, y, 0);
                        vfxEngine.spawnParticle(wingLoc, Particle.DRAGON_BREATH, 1, 0.05, 0.05, 0.05, 0.01, player);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
    
    /**
     * Cataclysm VFX - Dragon-aura with scale particles
     */
    public void cataclysm(Player player, Location center, double radius, int rank) {
        MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.DRAGON, center.clone().subtract(0, 0.5, 0), rank, player
        );
        vfxEngine.spawnMagicCircle(circle, 100);
        
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 2.0f, 0.6f);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                
                // Dragon aura
                int points = 20;
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points) + (ticks * 0.1);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(ticks * 0.1 + i) * 2.0;
                    
                    Location auraLoc = center.clone().add(x, y, z);
                    vfxEngine.spawnParticle(auraLoc, Particle.DRAGON_BREATH, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
