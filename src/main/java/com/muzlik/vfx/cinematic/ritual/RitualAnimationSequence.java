package com.muzlik.vfx.cinematic.ritual;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.ritual.RitualStage;
import com.muzlik.vfx.cinematic.CinematicVFXEngine;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles ritual animation sequences for different phases
 * Provides smooth transitions and phase-specific effects
 */
public class RitualAnimationSequence {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public RitualAnimationSequence(FrostSMPPlugin plugin, CinematicVFXEngine vfxEngine) {
        this.plugin = plugin;
        this.vfxEngine = vfxEngine;
    }
    
    /**
     * Helper method to spawn particles with fallback
     */
    private void spawnParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, Player player) {
        if (vfxEngine != null) {
            vfxEngine.spawnParticle(location, particle, count, offsetX, offsetY, offsetZ, speed, player);
        } else {
            // Fallback to direct world particle spawning
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }
    
    /**
     * Charging phase animation
     * DISABLED - Magic circles handle all visuals now
     */
    public void playChargingAnimation(Location center, FragmentType fragmentType, int progressPercent, Player player) {
        // Disabled - magic circles provide all visuals
        // This method intentionally does nothing to avoid particle overlap
    }
    
    /**
     * 50% completion pulse effect
     * Expanding pulse with sound and visual feedback
     */
    public void playPulseAnimation(Location center, FragmentType fragmentType, Player player) {
        // Large expanding pulse
        new BukkitRunnable() {
            double radius = 0;
            int ticks = 0;
            
            @Override
            public void run() {
                if (radius > 6.0 || ticks >= 30) {
                    cancel();
                    return;
                }
                
                // Outer ring
                int points = 48;
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location pulseLoc = center.clone().add(x, 0.2, z);
                    spawnParticle(pulseLoc, Particle.END_ROD, 2, 0.05, 0.05, 0.05, 0.01, player);
                }
                
                // Inner glow
                if (radius < 2.0) {
                    spawnParticle(center.clone().add(0, 0.5, 0), Particle.GLOW, 10, radius * 0.5, 0.2, radius * 0.5, 0.02, player);
                }
                
                radius += 0.3;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effect
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.2f);
        
        // Vertical burst
        for (int i = 0; i < 20; i++) {
            double height = i * 0.2;
            spawnParticle(center.clone().add(0, height, 0), Particle.END_ROD, 3, 0.1, 0.1, 0.1, 0.02, player);
        }
    }
    
    /**
     * Final phase intensification
     * DISABLED - Magic circles handle all visuals now
     */
    public void playIntensificationAnimation(Location center, FragmentType fragmentType, Player player) {
        // Disabled - magic circles provide all visuals
        // This method intentionally does nothing to avoid particle overlap
    }
    
    /**
     * Successful completion animation
     * Upward energy burst with magic circle dissolution and reward materialization
     */
    public void playCompletionAnimation(Location center, FragmentType fragmentType, Player player) {
        // Upward energy burst
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }
                
                // Expanding upward burst
                double height = ticks * 0.3;
                double radius = ticks * 0.2;
                
                int points = 24;
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location burstLoc = center.clone().add(x, height, z);
                    spawnParticle(burstLoc, Particle.END_ROD, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Magic circle dissolution from center outward
        new BukkitRunnable() {
            double radius = 0;
            
            @Override
            public void run() {
                if (radius > 5.0) {
                    cancel();
                    return;
                }
                
                int points = 32;
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location dissolveLoc = center.clone().add(x, 0.1, z);
                    spawnParticle(dissolveLoc, Particle.SMOKE_NORMAL, 1, 0, 0, 0, 0.01, player);
                }
                
                radius += 0.3;
            }
        }.runTaskTimer(plugin, 5L, 2L);
        
        // Fragment/reward materialization effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                double height = 1.0 + Math.sin(ticks * 0.2) * 0.3;
                Location rewardLoc = center.clone().add(0, height, 0);
                
                // Rotating particles around reward
                double angle = ticks * 0.3;
                for (int i = 0; i < 8; i++) {
                    double particleAngle = angle + (i * Math.PI * 2 / 8);
                    double x = Math.cos(particleAngle) * 0.5;
                    double z = Math.sin(particleAngle) * 0.5;
                    
                    Location particleLoc = rewardLoc.clone().add(x, 0, z);
                    spawnParticle(particleLoc, Particle.ENCHANTMENT_TABLE, 1, 0, 0, 0, 0, player);
                }
                
                // Central glow
                spawnParticle(rewardLoc, Particle.GLOW, 3, 0.1, 0.1, 0.1, 0.01, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 1L);
        
        // Completion sounds
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.0f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            center.getWorld().playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 1.2f);
        }, 10L);
    }
    
    /**
     * Failure/interruption animation
     * Magic circle fracture with fragment scattering and fade-to-smoke dissolution
     */
    public void playFailureAnimation(Location center, FragmentType fragmentType, Player player) {
        // Magic circle fracture effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                // Fracture lines radiating outward
                int lines = 12;
                for (int i = 0; i < lines; i++) {
                    double angle = (i * Math.PI * 2 / lines);
                    double length = ticks * 0.3;
                    
                    for (double d = 0; d < length; d += 0.2) {
                        double x = Math.cos(angle) * d;
                        double z = Math.sin(angle) * d;
                        
                        Location fractureLoc = center.clone().add(x, 0.1, z);
                        spawnParticle(fractureLoc, Particle.SMOKE_LARGE, 1, 0, 0, 0, 0.01, player);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Fragment scattering
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }
                
                // Scattered fragments flying outward
                for (int i = 0; i < 16; i++) {
                    double angle = (i * Math.PI * 2 / 16);
                    double distance = ticks * 0.2;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;
                    double height = 0.5 - (ticks * 0.02);
                    
                    Location scatterLoc = center.clone().add(x, height, z);
                    spawnParticle(scatterLoc, Particle.ASH, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);
        
        // Fade-to-smoke dissolution
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                // Rising smoke
                double height = ticks * 0.1;
                spawnParticle(center.clone().add(0, height, 0), Particle.SMOKE_LARGE, 5, 0.5, 0.2, 0.5, 0.02, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 2L);
        
        // Failure sounds
        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
        }, 10L);
    }
    
    /**
     * Get animation for specific ritual stage
     */
    public void playStageAnimation(Location center, FragmentType fragmentType, RitualStage stage, int progressPercent, Player player) {
        switch (stage) {
            case CHARGING:
                playChargingAnimation(center, fragmentType, progressPercent, player);
                break;
            case ACTIVATION:
                playIntensificationAnimation(center, fragmentType, player);
                break;
            case COMPLETION:
                playCompletionAnimation(center, fragmentType, player);
                break;
        }
    }
}
