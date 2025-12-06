package com.muzlik.vfx;

/**
 * Enum representing different animation easing curves
 */
public enum AnimationCurve {
    /**
     * Linear progression (no easing)
     */
    LINEAR,
    
    /**
     * Ease in (slow start, fast end)
     */
    EASE_IN,
    
    /**
     * Ease out (fast start, slow end)
     */
    EASE_OUT,
    
    /**
     * Ease in and out (slow start and end, fast middle)
     */
    EASE_IN_OUT,
    
    /**
     * Spiral motion curve
     */
    SPIRAL,
    
    /**
     * Circular motion curve
     */
    CIRCULAR,
    
    /**
     * Wave motion curve
     */
    WAVE,
    
    /**
     * Bounce curve
     */
    BOUNCE,
    
    /**
     * Elastic curve
     */
    ELASTIC
}
