package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for composing multiple particle layers.
 * Supports 1-5 layers based on rank.
 * Varies particle size and opacity by layer.
 * 
 * Requirements: 5.1, 5.2, 8.3
 */
public class LayeredEffectBuilder {
    
    private final List<ParticleLayer> layers;
    private final int maxLayers;
    
    public LayeredEffectBuilder(int maxLayers) {
        if (maxLayers < 1 || maxLayers > 5) {
            throw new IllegalArgumentException("Max layers must be between 1 and 5");
        }
        this.maxLayers = maxLayers;
        this.layers = new ArrayList<>();
    }
    
    /**
     * Add a particle layer
     * 
     * @param layerIndex Layer index (0-4)
     * @param yOffset Y offset from base location
     * @param particleType Particle type for this layer
     * @param particleCount Number of particles
     * @param size Particle size
     * @param opacity Particle opacity
     * @return This builder
     */
    public LayeredEffectBuilder addLayer(int layerIndex, double yOffset, Particle particleType,
                                          int particleCount, double size, double opacity) {
        if (layers.size() >= maxLayers) {
            throw new IllegalStateException("Cannot add more than " + maxLayers + " layers");
        }
        
        layers.add(new ParticleLayer(layerIndex, yOffset, particleType, particleCount, size, opacity));
        return this;
    }
    
    /**
     * Build layered effect at location
     * 
     * @param baseLocation Base location for effect
     * @param radius Effect radius
     * @return List of particle spawn data for all layers
     */
    public List<ParticleSpawnData> build(Location baseLocation, double radius) {
        if (baseLocation == null) {
            throw new IllegalArgumentException("Base location cannot be null");
        }
        
        List<ParticleSpawnData> particles = new ArrayList<>();
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        
        for (ParticleLayer layer : layers) {
            Location layerLocation = baseLocation.clone().add(0, layer.yOffset, 0);
            
            // Generate circle pattern for this layer
            List<Location> layerPoints = shapeRenderer.renderCircle(
                layerLocation,
                radius * (1.0 - layer.layerIndex * 0.1), // Smaller radius for higher layers
                layer.particleCount,
                0
            );
            
            // Create particle spawn data for each point
            for (Location point : layerPoints) {
                particles.add(new ParticleSpawnData.Builder()
                    .location(point)
                    .particleType(layer.particleType)
                    .count(1)
                    .size(layer.size)
                    .opacity(layer.opacity)
                    .layer(layer.layerIndex)
                    .build());
            }
        }
        
        return particles;
    }
    
    /**
     * Get number of layers
     */
    public int getLayerCount() {
        return layers.size();
    }
    
    /**
     * Clear all layers
     */
    public void clear() {
        layers.clear();
    }
    
    /**
     * Particle layer data
     */
    private static class ParticleLayer {
        final int layerIndex;
        final double yOffset;
        final Particle particleType;
        final int particleCount;
        final double size;
        final double opacity;
        
        ParticleLayer(int layerIndex, double yOffset, Particle particleType,
                     int particleCount, double size, double opacity) {
            this.layerIndex = layerIndex;
            this.yOffset = yOffset;
            this.particleType = particleType;
            this.particleCount = particleCount;
            this.size = size;
            this.opacity = opacity;
        }
    }
}
