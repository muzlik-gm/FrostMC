package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates ascending/descending particle motion effects.
 * Implements ground-level + elevated layer combinations.
 * 
 * Requirements: 5.3, 5.4
 */
public class VerticalMotionEffect {
    
    /**
     * Create ascending particle motion
     * 
     * @param baseLocation Starting location
     * @param height Maximum height
     * @param particleType Particle type
     * @param particleCount Particles per level
     * @param levels Number of height levels
     * @return List of particle spawn data
     */
    public List<ParticleSpawnData> createAscendingMotion(Location baseLocation, double height,
                                                          Particle particleType, int particleCount,
                                                          int levels) {
        if (baseLocation == null) {
            throw new IllegalArgumentException("Base location cannot be null");
        }
        
        List<ParticleSpawnData> particles = new ArrayList<>();
        double heightStep = height / levels;
        
        for (int i = 0; i < levels; i++) {
            double yOffset = i * heightStep;
            double opacity = 1.0 - (i / (double) levels); // Fade as it rises
            double size = 1.0 - (i / (double) levels) * 0.5; // Shrink as it rises
            
            Location levelLocation = baseLocation.clone().add(0, yOffset, 0);
            
            particles.add(new ParticleSpawnData.Builder()
                .location(levelLocation)
                .particleType(particleType)
                .count(particleCount)
                .offset(0.2, 0.1, 0.2)
                .speed(0.05)
                .size(size)
                .opacity(opacity)
                .layer(i)
                .build());
        }
        
        return particles;
    }
    
    /**
     * Create descending particle motion
     * 
     * @param baseLocation Starting location (top)
     * @param height Drop height
     * @param particleType Particle type
     * @param particleCount Particles per level
     * @param levels Number of height levels
     * @return List of particle spawn data
     */
    public List<ParticleSpawnData> createDescendingMotion(Location baseLocation, double height,
                                                           Particle particleType, int particleCount,
                                                           int levels) {
        if (baseLocation == null) {
            throw new IllegalArgumentException("Base location cannot be null");
        }
        
        List<ParticleSpawnData> particles = new ArrayList<>();
        double heightStep = height / levels;
        
        for (int i = 0; i < levels; i++) {
            double yOffset = -i * heightStep;
            double opacity = 0.5 + (i / (double) levels) * 0.5; // Intensify as it falls
            double size = 0.5 + (i / (double) levels) * 0.5; // Grow as it falls
            
            Location levelLocation = baseLocation.clone().add(0, yOffset, 0);
            
            particles.add(new ParticleSpawnData.Builder()
                .location(levelLocation)
                .particleType(particleType)
                .count(particleCount)
                .offset(0.2, 0.1, 0.2)
                .speed(0.1)
                .size(size)
                .opacity(opacity)
                .layer(i)
                .build());
        }
        
        return particles;
    }
    
    /**
     * Create ground-level + elevated layer combination
     * 
     * @param baseLocation Base location
     * @param radius Effect radius
     * @param groundParticle Ground level particle
     * @param elevatedParticle Elevated particle
     * @param elevatedHeight Height of elevated layer
     * @return List of particle spawn data
     */
    public List<ParticleSpawnData> createGroundAndElevatedLayers(Location baseLocation, double radius,
                                                                   Particle groundParticle,
                                                                   Particle elevatedParticle,
                                                                   double elevatedHeight) {
        if (baseLocation == null) {
            throw new IllegalArgumentException("Base location cannot be null");
        }
        
        List<ParticleSpawnData> particles = new ArrayList<>();
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        
        // Ground level layer
        List<Location> groundPoints = shapeRenderer.renderCircle(baseLocation, radius, 24, 0);
        for (Location point : groundPoints) {
            particles.add(new ParticleSpawnData.Builder()
                .location(point)
                .particleType(groundParticle)
                .count(1)
                .size(1.0)
                .opacity(1.0)
                .layer(0)
                .build());
        }
        
        // Elevated layer
        Location elevatedLocation = baseLocation.clone().add(0, elevatedHeight, 0);
        List<Location> elevatedPoints = shapeRenderer.renderCircle(elevatedLocation, radius * 0.8, 20, 0);
        for (Location point : elevatedPoints) {
            particles.add(new ParticleSpawnData.Builder()
                .location(point)
                .particleType(elevatedParticle)
                .count(1)
                .size(0.8)
                .opacity(0.8)
                .layer(1)
                .build());
        }
        
        return particles;
    }
}
