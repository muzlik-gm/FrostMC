package com.muzlik.vfx.cinematic.animation;

/**
 * Handles consistent angular velocity for magic circles.
 * Supports counter-rotation for multi-ring circles.
 * 
 * Requirements: 6.3
 */
public class RotationAnimator {
    
    private double currentRotation;
    private final double rotationSpeed;
    private final boolean counterRotate;
    
    public RotationAnimator(double rotationSpeed) {
        this(rotationSpeed, false);
    }
    
    public RotationAnimator(double rotationSpeed, boolean counterRotate) {
        this.rotationSpeed = rotationSpeed;
        this.counterRotate = counterRotate;
        this.currentRotation = 0;
    }
    
    /**
     * Update rotation (call every tick)
     */
    public void tick() {
        if (counterRotate) {
            currentRotation -= rotationSpeed;
        } else {
            currentRotation += rotationSpeed;
        }
        
        // Normalize to 0-2π range
        while (currentRotation > 2 * Math.PI) {
            currentRotation -= 2 * Math.PI;
        }
        while (currentRotation < 0) {
            currentRotation += 2 * Math.PI;
        }
    }
    
    /**
     * Get current rotation in radians
     */
    public double getCurrentRotation() {
        return currentRotation;
    }
    
    /**
     * Get rotation speed
     */
    public double getRotationSpeed() {
        return rotationSpeed;
    }
    
    /**
     * Check if counter-rotating
     */
    public boolean isCounterRotate() {
        return counterRotate;
    }
    
    /**
     * Reset rotation to zero
     */
    public void reset() {
        currentRotation = 0;
    }
    
    /**
     * Set rotation to specific value
     */
    public void setRotation(double rotation) {
        this.currentRotation = rotation;
    }
}
