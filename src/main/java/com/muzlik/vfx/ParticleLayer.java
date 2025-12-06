package com.muzlik.vfx;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Represents a single particle layer in a VFX effect.
 * Can be combined with other layers for complex effects.
 * 
 * Requirements: 1.1, 1.3
 */
public class ParticleLayer {
    private final Particle particleType;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final Object data;
    private final ParticlePattern pattern;
    private final Color color;
    
    /**
     * Private constructor - use Builder
     */
    private ParticleLayer(Particle particleType, int count, double offsetX, double offsetY,
                         double offsetZ, double speed, Object data, ParticlePattern pattern, Color color) {
        this.particleType = particleType;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.data = data;
        this.pattern = pattern;
        this.color = color;
    }
    
    /**
     * Render this particle layer at the given location
     * 
     * @param location The location to spawn particles
     * @param tick Current tick in the animation
     */
    public void render(Location location, int tick) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        World world = location.getWorld();
        
        // Spawn particles based on pattern
        switch (pattern) {
            case LINEAR:
                renderLinear(world, location);
                break;
            case SPIRAL:
                renderSpiral(world, location, tick);
                break;
            case CIRCLE:
                renderCircle(world, location, tick);
                break;
            case WAVE:
                renderWave(world, location, tick);
                break;
            case BURST:
                renderBurst(world, location);
                break;
            case RING:
                renderRing(world, location);
                break;
            default:
                renderLinear(world, location);
                break;
        }
    }
    
    /**
     * Render particles in a linear pattern
     */
    private void renderLinear(World world, Location location) {
        if (data != null) {
            world.spawnParticle(particleType, location, count, offsetX, offsetY, offsetZ, speed, data);
        } else {
            world.spawnParticle(particleType, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }
    
    /**
     * Render particles in a spiral pattern
     */
    private void renderSpiral(World world, Location location, int tick) {
        double angle = tick * 0.3;
        double radius = 0.5;
        double height = tick * 0.05;
        
        for (int i = 0; i < count; i++) {
            double currentAngle = angle + (i * Math.PI * 2 / count);
            double x = Math.cos(currentAngle) * radius;
            double z = Math.sin(currentAngle) * radius;
            double y = height + (i * 0.1);
            
            Location particleLoc = location.clone().add(x, y, z);
            world.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, speed, data);
        }
    }
    
    /**
     * Render particles in a circular pattern
     */
    private void renderCircle(World world, Location location, int tick) {
        double radius = 1.0;
        
        for (int i = 0; i < count; i++) {
            double angle = (i * Math.PI * 2 / count) + (tick * 0.1);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = location.clone().add(x, 0, z);
            world.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, speed, data);
        }
    }
    
    /**
     * Render particles in a wave pattern
     */
    private void renderWave(World world, Location location, int tick) {
        for (int i = 0; i < count; i++) {
            double x = i * 0.2;
            double y = Math.sin((x + tick * 0.1)) * 0.5;
            
            Location particleLoc = location.clone().add(x - (count * 0.1), y, 0);
            world.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, speed, data);
        }
    }
    
    /**
     * Render particles in a burst pattern
     */
    private void renderBurst(World world, Location location) {
        world.spawnParticle(particleType, location, count, offsetX, offsetY, offsetZ, speed, data);
    }
    
    /**
     * Render particles in a ring pattern
     */
    private void renderRing(World world, Location location) {
        double radius = 1.5;
        int points = count;
        
        for (int i = 0; i < points; i++) {
            double angle = i * Math.PI * 2 / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = location.clone().add(x, 0, z);
            world.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, speed, data);
        }
    }
    
    // Getters
    
    public Particle getParticleType() {
        return particleType;
    }
    
    public int getCount() {
        return count;
    }
    
    public ParticlePattern getPattern() {
        return pattern;
    }
    
    /**
     * Builder for ParticleLayer
     */
    public static class Builder {
        private Particle particleType = Particle.FLAME;
        private int count = 10;
        private double offsetX = 0.5;
        private double offsetY = 0.5;
        private double offsetZ = 0.5;
        private double speed = 0.1;
        private Object data = null;
        private ParticlePattern pattern = ParticlePattern.LINEAR;
        private Color color = null;
        
        public Builder particleType(Particle particleType) {
            this.particleType = particleType;
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
        
        public Builder pattern(ParticlePattern pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder color(Color color) {
            this.color = color;
            return this;
        }
        
        public ParticleLayer build() {
            return new ParticleLayer(particleType, count, offsetX, offsetY, offsetZ, 
                                    speed, data, pattern, color);
        }
    }
}
