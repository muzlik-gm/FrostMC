package com.muzlik.vfx.cinematic;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single ring in a magic circle.
 * Supports rotation, rune segments, and counter-rotation for multi-ring circles.
 * 
 * Requirements: 4.1, 4.3, 10.1-10.5
 */
public class MagicCircleRing {
    private final double radius;
    private final int particleCount;
    private final double rotationSpeed; // Can be negative for counter-rotation
    private final boolean hasRuneSegments;
    private final List<RuneGlyph> runes;
    
    public MagicCircleRing(double radius, int particleCount, double rotationSpeed, boolean hasRuneSegments) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        if (particleCount < 3) {
            throw new IllegalArgumentException("Particle count must be at least 3");
        }
        
        this.radius = radius;
        this.particleCount = particleCount;
        this.rotationSpeed = rotationSpeed;
        this.hasRuneSegments = hasRuneSegments;
        this.runes = new ArrayList<>();
        
        // Generate rune segments if enabled (typically 4-8 runes around the ring)
        if (hasRuneSegments) {
            int runeCount = 6;
            for (int i = 0; i < runeCount; i++) {
                double angle = (i * 2 * Math.PI) / runeCount;
                runes.add(new RuneGlyph(angle, 0.3)); // Small runes at 0.3 block size
            }
        }
    }
    
    // Getters
    public double getRadius() {
        return radius;
    }
    
    public int getParticleCount() {
        return particleCount;
    }
    
    public double getRotationSpeed() {
        return rotationSpeed;
    }
    
    public boolean hasRuneSegments() {
        return hasRuneSegments;
    }
    
    public List<RuneGlyph> getRunes() {
        return runes;
    }
    
    /**
     * Simple rune glyph representation
     */
    public static class RuneGlyph {
        private final double angle; // Position on ring
        private final double size;
        
        public RuneGlyph(double angle, double size) {
            this.angle = angle;
            this.size = size;
        }
        
        public double getAngle() {
            return angle;
        }
        
        public double getSize() {
            return size;
        }
    }
}
