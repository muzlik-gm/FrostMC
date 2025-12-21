package com.muzlik.vfx.cinematic;

/**
 * Easing functions for smooth animations.
 * Used by the animation controller for natural motion.
 * 
 * Requirements: 6.4
 */
public enum EasingFunction {
    LINEAR,
    EASE_IN_QUAD,
    EASE_OUT_QUAD,
    EASE_IN_OUT_QUAD,
    EASE_IN_CUBIC,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    EASE_IN_SINE,
    EASE_OUT_SINE,
    EASE_IN_OUT_SINE;
    
    /**
     * Apply the easing function to a progress value (0.0 to 1.0)
     * 
     * @param t Progress value (0.0 to 1.0)
     * @return Eased value (0.0 to 1.0)
     */
    public double apply(double t) {
        // Clamp to 0-1 range
        t = Math.max(0.0, Math.min(1.0, t));
        
        switch (this) {
            case LINEAR:
                return t;
                
            case EASE_IN_QUAD:
                return t * t;
                
            case EASE_OUT_QUAD:
                return t * (2 - t);
                
            case EASE_IN_OUT_QUAD:
                return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
                
            case EASE_IN_CUBIC:
                return t * t * t;
                
            case EASE_OUT_CUBIC:
                return (--t) * t * t + 1;
                
            case EASE_IN_OUT_CUBIC:
                return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
                
            case EASE_IN_SINE:
                return 1 - Math.cos((t * Math.PI) / 2);
                
            case EASE_OUT_SINE:
                return Math.sin((t * Math.PI) / 2);
                
            case EASE_IN_OUT_SINE:
                return -(Math.cos(Math.PI * t) - 1) / 2;
                
            default:
                return t;
        }
    }
}
