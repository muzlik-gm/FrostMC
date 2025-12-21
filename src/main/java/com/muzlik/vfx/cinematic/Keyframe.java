package com.muzlik.vfx.cinematic;

import org.bukkit.Location;

/**
 * Represents a keyframe in an animation sequence.
 * Contains position, timing, and interpolation information.
 * 
 * Requirements: 6.1, 8.4
 */
public class Keyframe {
    private final Location position;
    private final int tick;
    private final EasingFunction easingToNext;
    private final double scale;
    private final double opacity;
    
    public Keyframe(Location position, int tick, EasingFunction easingToNext, double scale, double opacity) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (tick < 0) {
            throw new IllegalArgumentException("Tick must be non-negative");
        }
        
        this.position = position;
        this.tick = tick;
        this.easingToNext = easingToNext != null ? easingToNext : EasingFunction.LINEAR;
        this.scale = scale;
        this.opacity = opacity;
    }
    
    public Keyframe(Location position, int tick) {
        this(position, tick, EasingFunction.LINEAR, 1.0, 1.0);
    }
    
    // Getters
    public Location getPosition() { return position; }
    public int getTick() { return tick; }
    public EasingFunction getEasingToNext() { return easingToNext; }
    public double getScale() { return scale; }
    public double getOpacity() { return opacity; }
}
