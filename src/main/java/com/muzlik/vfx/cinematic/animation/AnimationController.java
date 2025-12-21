package com.muzlik.vfx.cinematic.animation;

import com.muzlik.vfx.cinematic.EasingFunction;
import com.muzlik.vfx.cinematic.Keyframe;
import com.muzlik.vfx.cinematic.ParticleSpawnData;
import org.bukkit.Location;

import java.util.List;

/**
 * Main animation controller.
 * Wires together keyframes, easing, blending, and rotation.
 * 
 * Requirements: 6.1-6.5, 8.4
 */
public class AnimationController {
    
    private final TransitionBlender transitionBlender;
    
    public AnimationController() {
        this.transitionBlender = new TransitionBlender();
    }
    
    /**
     * Create keyframe animation
     * 
     * @param keyframes List of keyframes with positions and timing
     * @return Animation instance
     */
    public KeyframeAnimation createAnimation(List<Keyframe> keyframes) {
        return new KeyframeAnimation(keyframes);
    }
    
    /**
     * Create looping keyframe animation
     * 
     * @param keyframes List of keyframes
     * @param loop Whether to loop
     * @return Animation instance
     */
    public KeyframeAnimation createAnimation(List<Keyframe> keyframes, boolean loop) {
        return new KeyframeAnimation(keyframes, loop);
    }
    
    /**
     * Interpolate between two positions with easing
     * 
     * @param start Start position
     * @param end End position
     * @param progress Progress 0.0 to 1.0
     * @param easing Easing function to apply
     * @return Interpolated position
     */
    public Location interpolate(Location start, Location end, double progress, EasingFunction easing) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end locations cannot be null");
        }
        if (easing == null) {
            easing = EasingFunction.LINEAR;
        }
        
        // Apply easing
        double easedProgress = easing.apply(progress);
        
        // Interpolate
        double x = start.getX() + (end.getX() - start.getX()) * easedProgress;
        double y = start.getY() + (end.getY() - start.getY()) * easedProgress;
        double z = start.getZ() + (end.getZ() - start.getZ()) * easedProgress;
        
        return new Location(start.getWorld(), x, y, z);
    }
    
    /**
     * Blend between two particle patterns
     * 
     * @param patternA First pattern
     * @param patternB Second pattern
     * @param blendFactor Blend factor 0.0 (all A) to 1.0 (all B)
     * @param minBlendTicks Minimum ticks for blend (at least 5)
     * @return Blended particle list
     */
    public List<ParticleSpawnData> blendPatterns(List<ParticleSpawnData> patternA,
                                                  List<ParticleSpawnData> patternB,
                                                  double blendFactor,
                                                  int minBlendTicks) {
        return transitionBlender.blendPatterns(patternA, patternB, blendFactor, minBlendTicks);
    }
    
    /**
     * Create a rotation animator
     * 
     * @param rotationSpeed Rotation speed in radians per tick
     * @return Rotation animator
     */
    public RotationAnimator createRotationAnimator(double rotationSpeed) {
        return new RotationAnimator(rotationSpeed);
    }
    
    /**
     * Create a counter-rotating animator
     * 
     * @param rotationSpeed Rotation speed in radians per tick
     * @return Counter-rotating animator
     */
    public RotationAnimator createCounterRotationAnimator(double rotationSpeed) {
        return new RotationAnimator(rotationSpeed, true);
    }
}
