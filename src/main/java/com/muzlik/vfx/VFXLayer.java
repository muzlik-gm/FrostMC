package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Represents a single layer in the Five-Layer VFX System.
 * 
 * Each layer has specific particle types, counts, patterns, and rendering properties.
 * Layers are rendered in order: Core → Secondary → Ambient → Impact → Cinematic
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
public class VFXLayer {
    private final String layerName;
    private final Particle particleType;
    private final int baseCount;
    private final ParticlePattern pattern;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final Object particleData;
    private final boolean rankScaling;
    private final boolean perfScaling;
    
    // Scaled count (after rank and performance multipliers)
    private int scaledCount;
    
    /**
     * Constructor
     */
    public VFXLayer(String layerName, Particle particleType, int baseCount, 
                   ParticlePattern pattern, double offsetX, double offsetY, double offsetZ,
                   double speed, Object particleData, boolean rankScaling, boolean perfScaling) {
        this.layerName = layerName;
        this.particleType = particleType;
        this.baseCount = baseCount;
        this.pattern = pattern;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.particleData = particleData;
        this.rankScaling = rankScaling;
        this.perfScaling = perfScaling;
        this.scaledCount = baseCount;
    }
    
    /**
     * Render this layer at the given location
     * 
     * @param location Location to render particles
     */
    public void render(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        // Apply pattern-based rendering
        pattern.render(location, particleType, scaledCount, offsetX, offsetY, offsetZ, speed, particleData);
    }
    
    /**
     * Apply scaling to particle count
     * 
     * @param rankMultiplier Rank-based multiplier
     * @param perfMultiplier Performance-based multiplier
     */
    public void applyScaling(double rankMultiplier, double perfMultiplier) {
        double multiplier = 1.0;
        
        if (rankScaling) {
            multiplier *= rankMultiplier;
        }
        
        if (perfScaling) {
            multiplier *= perfMultiplier;
        }
        
        this.scaledCount = (int) Math.max(1, Math.floor(baseCount * multiplier));
    }
    
    // Getters
    
    public String getLayerName() {
        return layerName;
    }
    
    public Particle getParticleType() {
        return particleType;
    }
    
    public int getBaseCount() {
        return baseCount;
    }
    
    public int getScaledCount() {
        return scaledCount;
    }
    
    public ParticlePattern getPattern() {
        return pattern;
    }
    
    public double getOffsetX() {
        return offsetX;
    }
    
    public double getOffsetY() {
        return offsetY;
    }
    
    public double getOffsetZ() {
        return offsetZ;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public Object getParticleData() {
        return particleData;
    }
    
    public boolean isRankScaling() {
        return rankScaling;
    }
    
    public boolean isPerfScaling() {
        return perfScaling;
    }
}
