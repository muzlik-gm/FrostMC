package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Enhanced sound layering system for abilities.
 * 
 * Provides spatial positioning, volume falloff, and layered sounds with timing offsets.
 * Supports primary, secondary, and impact sounds for rich audio feedback.
 * 
 * Per SYSTEM.md specification:
 * - Add primary sound at ability origin
 * - Add secondary accent sounds at offset positions
 * - Add impact sound at collision location
 * - Implement spatial positioning with volume falloff
 * - Add layered sounds for ultimate abilities (2-3 sounds with timing offsets)
 * 
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
public class SoundLayer {
    private final JavaPlugin plugin;
    
    // Volume falloff settings
    private static final double MAX_HEARING_DISTANCE = 32.0;
    private static final double MIN_VOLUME = 0.1;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     */
    public SoundLayer(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Play a primary sound at ability origin
     * 
     * @param location Origin location
     * @param sound Sound to play
     * @param volume Base volume
     * @param pitch Pitch
     */
    public void playPrimary(Location location, Sound sound, float volume, float pitch) {
        if (location == null || location.getWorld() == null || sound == null) {
            return;
        }
        
        location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
    }
    
    /**
     * Play secondary accent sounds at offset positions
     * 
     * @param origin Origin location
     * @param sound Sound to play
     * @param volume Base volume
     * @param pitch Pitch
     * @param offsets List of offset vectors
     */
    public void playSecondary(Location origin, Sound sound, float volume, float pitch, List<Vector> offsets) {
        if (origin == null || origin.getWorld() == null || sound == null || offsets == null) {
            return;
        }
        
        for (Vector offset : offsets) {
            Location soundLoc = origin.clone().add(offset);
            soundLoc.getWorld().playSound(soundLoc, sound, SoundCategory.PLAYERS, volume * 0.7f, pitch);
        }
    }
    
    /**
     * Play impact sound at collision location
     * 
     * @param location Impact location
     * @param sound Sound to play
     * @param volume Base volume
     * @param pitch Pitch
     */
    public void playImpact(Location location, Sound sound, float volume, float pitch) {
        if (location == null || location.getWorld() == null || sound == null) {
            return;
        }
        
        location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume * 1.2f, pitch);
    }
    
    /**
     * Play sound with spatial positioning and volume falloff
     * 
     * @param location Sound location
     * @param sound Sound to play
     * @param baseVolume Base volume
     * @param pitch Pitch
     * @param players Players to play sound for
     */
    public void playSpatial(Location location, Sound sound, float baseVolume, float pitch, Collection<Player> players) {
        if (location == null || location.getWorld() == null || sound == null) {
            return;
        }
        
        for (Player player : players) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // Calculate distance
            double distance = player.getLocation().distance(location);
            
            if (distance > MAX_HEARING_DISTANCE) {
                continue; // Too far to hear
            }
            
            // Calculate volume with falloff
            float volume = calculateVolumeWithFalloff(baseVolume, distance);
            
            // Play sound for this player
            player.playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
        }
    }
    
    /**
     * Play layered sounds for ultimate abilities
     * Plays 2-3 sounds with timing offsets
     * 
     * @param location Sound location
     * @param layers List of sound layers (sound, volume, pitch, delay in ticks)
     */
    public void playLayered(Location location, List<SoundLayerData> layers) {
        if (location == null || location.getWorld() == null || layers == null || layers.isEmpty()) {
            return;
        }
        
        for (SoundLayerData layer : layers) {
            if (layer.delayTicks <= 0) {
                // Play immediately
                location.getWorld().playSound(
                    location, 
                    layer.sound, 
                    SoundCategory.PLAYERS, 
                    layer.volume, 
                    layer.pitch
                );
            } else {
                // Play with delay
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (location.getWorld() != null) {
                            location.getWorld().playSound(
                                location, 
                                layer.sound, 
                                SoundCategory.PLAYERS, 
                                layer.volume, 
                                layer.pitch
                            );
                        }
                    }
                }.runTaskLater(plugin, layer.delayTicks);
            }
        }
    }
    
    /**
     * Calculate volume with distance falloff
     * 
     * @param baseVolume Base volume
     * @param distance Distance from sound source
     * @return Adjusted volume
     */
    private float calculateVolumeWithFalloff(float baseVolume, double distance) {
        if (distance <= 0) {
            return baseVolume;
        }
        
        // Linear falloff
        double falloff = 1.0 - (distance / MAX_HEARING_DISTANCE);
        falloff = Math.max(MIN_VOLUME, falloff);
        
        return (float) (baseVolume * falloff);
    }
    
    /**
     * Create standard offset positions for secondary sounds
     * Creates a circle of offset positions around the origin
     * 
     * @param radius Radius of the circle
     * @param count Number of positions
     * @return List of offset vectors
     */
    public static List<Vector> createCircleOffsets(double radius, int count) {
        List<Vector> offsets = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            
            offsets.add(new Vector(x, 0, z));
        }
        
        return offsets;
    }
    
    /**
     * Create standard layered sounds for ultimate abilities
     * 
     * @param primarySound Primary sound
     * @param secondarySound Secondary sound
     * @param accentSound Accent sound
     * @param baseVolume Base volume
     * @return List of sound layers
     */
    public static List<SoundLayerData> createUltimateLayers(Sound primarySound, Sound secondarySound, 
                                                            Sound accentSound, float baseVolume) {
        List<SoundLayerData> layers = new ArrayList<>();
        
        // Primary sound - immediate
        layers.add(new SoundLayerData(primarySound, baseVolume, 1.0f, 0));
        
        // Secondary sound - slight delay (0.05 seconds = 1 tick)
        layers.add(new SoundLayerData(secondarySound, baseVolume * 0.8f, 1.1f, 1));
        
        // Accent sound - longer delay (0.15 seconds = 3 ticks)
        layers.add(new SoundLayerData(accentSound, baseVolume * 0.6f, 0.9f, 3));
        
        return layers;
    }
    
    /**
     * Sound layer data
     */
    public static class SoundLayerData {
        public final Sound sound;
        public final float volume;
        public final float pitch;
        public final long delayTicks;
        
        public SoundLayerData(Sound sound, float volume, float pitch, long delayTicks) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.delayTicks = delayTicks;
        }
    }
}
