package com.muzlik.vfx.cinematic.animation;

import com.muzlik.vfx.cinematic.Keyframe;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Keyframe-based animation with interpolation.
 * Stores keyframes with position, tick, easing, scale, opacity.
 * 
 * Requirements: 6.1, 8.4
 */
public class KeyframeAnimation {
    
    private final List<Keyframe> keyframes;
    private int currentTick;
    private boolean loop;
    
    public KeyframeAnimation(List<Keyframe> keyframes) {
        this(keyframes, false);
    }
    
    public KeyframeAnimation(List<Keyframe> keyframes, boolean loop) {
        if (keyframes == null || keyframes.isEmpty()) {
            throw new IllegalArgumentException("Keyframes cannot be null or empty");
        }
        
        // Sort keyframes by tick
        this.keyframes = new ArrayList<>(keyframes);
        this.keyframes.sort((a, b) -> Integer.compare(a.getTick(), b.getTick()));
        this.currentTick = 0;
        this.loop = loop;
    }
    
    /**
     * Update animation tick
     */
    public void tick() {
        currentTick++;
        
        // Loop if enabled
        if (loop && currentTick > getLastKeyframe().getTick()) {
            currentTick = 0;
        }
    }
    
    /**
     * Get interpolated location at current tick
     * 
     * @return Interpolated location
     */
    public Location getCurrentLocation() {
        return getLocationAtTick(currentTick);
    }
    
    /**
     * Get interpolated location at specific tick
     * 
     * @param tick Tick to get location for
     * @return Interpolated location
     */
    public Location getLocationAtTick(int tick) {
        // Find surrounding keyframes
        Keyframe before = null;
        Keyframe after = null;
        
        for (int i = 0; i < keyframes.size(); i++) {
            Keyframe kf = keyframes.get(i);
            
            if (kf.getTick() <= tick) {
                before = kf;
            }
            
            if (kf.getTick() >= tick && after == null) {
                after = kf;
                break;
            }
        }
        
        // If before start, return first keyframe
        if (before == null) {
            return keyframes.get(0).getPosition().clone();
        }
        
        // If after end, return last keyframe
        if (after == null) {
            return keyframes.get(keyframes.size() - 1).getPosition().clone();
        }
        
        // If exactly on keyframe, return it
        if (before.getTick() == tick) {
            return before.getPosition().clone();
        }
        
        // Interpolate between keyframes
        double progress = (tick - before.getTick()) / (double) (after.getTick() - before.getTick());
        progress = before.getEasingToNext().apply(progress);
        
        Location start = before.getPosition();
        Location end = after.getPosition();
        
        double x = start.getX() + (end.getX() - start.getX()) * progress;
        double y = start.getY() + (end.getY() - start.getY()) * progress;
        double z = start.getZ() + (end.getZ() - start.getZ()) * progress;
        
        return new Location(start.getWorld(), x, y, z);
    }
    
    /**
     * Get interpolated scale at current tick
     */
    public double getCurrentScale() {
        return getScaleAtTick(currentTick);
    }
    
    /**
     * Get interpolated scale at specific tick
     */
    public double getScaleAtTick(int tick) {
        Keyframe before = null;
        Keyframe after = null;
        
        for (Keyframe kf : keyframes) {
            if (kf.getTick() <= tick) before = kf;
            if (kf.getTick() >= tick && after == null) {
                after = kf;
                break;
            }
        }
        
        if (before == null) return keyframes.get(0).getScale();
        if (after == null) return keyframes.get(keyframes.size() - 1).getScale();
        if (before.getTick() == tick) return before.getScale();
        
        double progress = (tick - before.getTick()) / (double) (after.getTick() - before.getTick());
        progress = before.getEasingToNext().apply(progress);
        
        return before.getScale() + (after.getScale() - before.getScale()) * progress;
    }
    
    /**
     * Get interpolated opacity at current tick
     */
    public double getCurrentOpacity() {
        return getOpacityAtTick(currentTick);
    }
    
    /**
     * Get interpolated opacity at specific tick
     */
    public double getOpacityAtTick(int tick) {
        Keyframe before = null;
        Keyframe after = null;
        
        for (Keyframe kf : keyframes) {
            if (kf.getTick() <= tick) before = kf;
            if (kf.getTick() >= tick && after == null) {
                after = kf;
                break;
            }
        }
        
        if (before == null) return keyframes.get(0).getOpacity();
        if (after == null) return keyframes.get(keyframes.size() - 1).getOpacity();
        if (before.getTick() == tick) return before.getOpacity();
        
        double progress = (tick - before.getTick()) / (double) (after.getTick() - before.getTick());
        progress = before.getEasingToNext().apply(progress);
        
        return before.getOpacity() + (after.getOpacity() - before.getOpacity()) * progress;
    }
    
    /**
     * Check if animation is complete
     */
    public boolean isComplete() {
        return !loop && currentTick >= getLastKeyframe().getTick();
    }
    
    /**
     * Reset animation to start
     */
    public void reset() {
        currentTick = 0;
    }
    
    private Keyframe getLastKeyframe() {
        return keyframes.get(keyframes.size() - 1);
    }
    
    // Getters
    public int getCurrentTick() { return currentTick; }
    public List<Keyframe> getKeyframes() { return new ArrayList<>(keyframes); }
    public boolean isLoop() { return loop; }
}
