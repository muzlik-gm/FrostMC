package com.muzlik.vfx;

import org.bukkit.Sound;
import org.bukkit.util.Vector;

/**
 * Defines a sound effect with volume, pitch, spatial offset, and timing.
 * 
 * Requirements: 2.1
 */
public class SoundDefinition {
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final Vector offset;
    private final int delay; // in ticks
    
    /**
     * Private constructor - use factory methods
     */
    private SoundDefinition(Sound sound, float volume, float pitch, Vector offset, int delay) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.offset = offset != null ? offset : new Vector(0, 0, 0);
        this.delay = delay;
    }
    
    /**
     * Create a simple sound definition
     */
    public static SoundDefinition create(Sound sound, float volume, float pitch) {
        return new SoundDefinition(sound, volume, pitch, null, 0);
    }
    
    /**
     * Create a sound definition with spatial offset
     */
    public static SoundDefinition create(Sound sound, float volume, float pitch, Vector offset) {
        return new SoundDefinition(sound, volume, pitch, offset, 0);
    }
    
    /**
     * Create a sound definition with delay
     */
    public static SoundDefinition create(Sound sound, float volume, float pitch, int delayTicks) {
        return new SoundDefinition(sound, volume, pitch, null, delayTicks);
    }
    
    /**
     * Create a sound definition with offset and delay
     */
    public static SoundDefinition create(Sound sound, float volume, float pitch, Vector offset, int delayTicks) {
        return new SoundDefinition(sound, volume, pitch, offset, delayTicks);
    }
    
    // Getters
    
    public Sound getSound() {
        return sound;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public Vector getOffset() {
        return offset.clone();
    }
    
    public int getDelay() {
        return delay;
    }
    
    @Override
    public String toString() {
        return "SoundDefinition{" +
                "sound=" + sound +
                ", volume=" + volume +
                ", pitch=" + pitch +
                ", offset=" + offset +
                ", delay=" + delay +
                '}';
    }
}
