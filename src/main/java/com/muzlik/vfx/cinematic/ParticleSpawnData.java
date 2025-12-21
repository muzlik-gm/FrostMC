package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Data class for particle spawn information
 * Used by magic circles and other VFX systems to describe particle spawns
 */
public class ParticleSpawnData {
    
    private final Location location;
    private final Particle particle;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final Object data;
    
    public ParticleSpawnData(Location location, Particle particle, int count,
                            double offsetX, double offsetY, double offsetZ,
                            double speed, Object data) {
        this.location = location;
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.data = data;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Particle getParticle() {
        return particle;
    }
    
    public int getCount() {
        return count;
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
    
    public Object getData() {
        return data;
    }
    
    public Particle getParticleType() {
        return particle;
    }
    
    public double getOpacity() {
        return 1.0; // Default opacity
    }
    
    public double getSize() {
        return 1.0; // Default size
    }
    
    public int getLayer() {
        return 0; // Default layer
    }
    
    /**
     * Builder for ParticleSpawnData
     */
    public static class Builder {
        private Location location;
        private Particle particle;
        private int count = 1;
        private double offsetX = 0;
        private double offsetY = 0;
        private double offsetZ = 0;
        private double speed = 0;
        private Object data = null;
        private double opacity = 1.0;
        private double size = 1.0;
        private int layer = 0;
        
        public Builder location(Location location) {
            this.location = location;
            return this;
        }
        
        public Builder particle(Particle particle) {
            this.particle = particle;
            return this;
        }
        
        public Builder particleType(Particle particle) {
            this.particle = particle;
            return this;
        }
        
        public Builder count(int count) {
            this.count = count;
            return this;
        }
        
        public Builder offset(double x, double y, double z) {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            return this;
        }
        
        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }
        
        public Builder data(Object data) {
            this.data = data;
            return this;
        }
        
        public Builder opacity(double opacity) {
            this.opacity = opacity;
            return this;
        }
        
        public Builder size(double size) {
            this.size = size;
            return this;
        }
        
        public Builder layer(int layer) {
            this.layer = layer;
            return this;
        }
        
        public ParticleSpawnData build() {
            if (location == null || particle == null) {
                throw new IllegalStateException("Location and particle are required");
            }
            return new ParticleSpawnData(location, particle, count, offsetX, offsetY, offsetZ, speed, data);
        }
    }
}
