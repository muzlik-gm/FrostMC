package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates 3D explosion expansion effects with proper depth.
 * Animates expansion in 3D space.
 * 
 * Requirements: 5.5
 */
public class ExplosionEffect {
    
    private final Random random;
    
    public ExplosionEffect() {
        this.random = new Random();
    }
    
    /**
     * Create 3D explosion expansion
     * 
     * @param center Explosion center
     * @param maxRadius Maximum explosion radius
     * @param particleType Particle type
     * @param particleCount Number of particles
     * @param currentRadius Current expansion radius (for animation)
     * @return List of particle spawn data
     */
    public List<ParticleSpawnData> create3DExplosion(Location center, double maxRadius,
                                                      Particle particleType, int particleCount,
                                                      double currentRadius) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        
        List<ParticleSpawnData> particles = new ArrayList<>();
        
        // Create spherical explosion using Fibonacci sphere algorithm
        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;
        
        for (int i = 0; i < particleCount; i++) {
            double t = i / (double) particleCount;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            
            // Convert spherical to Cartesian coordinates
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            
            // Scale by current radius
            Vector direction = new Vector(x, y, z).normalize().multiply(currentRadius);
            Location particleLocation = center.clone().add(direction);
            
            // Calculate opacity based on expansion progress
            double progress = currentRadius / maxRadius;
            double opacity = 1.0 - progress; // Fade as it expands
            
            // Calculate size based on distance from center
            double size = 0.5 + progress * 0.5;
            
            particles.add(new ParticleSpawnData.Builder()
                .location(particleLocation)
                .particleType(particleType)
                .count(1)
                .offset(0.1, 0.1, 0.1)
                .speed(0.05)
                .size(size)
                .opacity(opacity)
                .layer((int) (progress * 5)) // 0-5 layers based on expansion
                .build());
        }
        
        return particles;
    }
    
    /**
     * Create layered explosion with multiple shells
     * 
     * @param center Explosion center
     * @param maxRadius Maximum radius
     * @param particleType Particle type
     * @param shells Number of concentric shells
     * @return List of particle spawn data
     */
    public List<ParticleSpawnData> createLayeredExplosion(Location center, double maxRadius,
                                                           Particle particleType, int shells) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        
        List<ParticleSpawnData> particles = new ArrayList<>();
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        
        for (int shell = 0; shell < shells; shell++) {
            double shellRadius = maxRadius * (shell + 1) / (double) shells;
            double opacity = 1.0 - (shell / (double) shells);
            double size = 0.5 + (shell / (double) shells) * 0.5;
            
            // Create sphere at this radius using multiple circles at different heights
            int heightLevels = 8;
            for (int level = 0; level < heightLevels; level++) {
                double heightFraction = (level / (double) (heightLevels - 1)) * 2 - 1; // -1 to 1
                double yOffset = shellRadius * heightFraction;
                double levelRadius = shellRadius * Math.sqrt(1 - heightFraction * heightFraction);
                
                if (levelRadius > 0.1) {
                    Location levelCenter = center.clone().add(0, yOffset, 0);
                    List<Location> circlePoints = shapeRenderer.renderCircle(
                        levelCenter,
                        levelRadius,
                        Math.max(8, (int) (levelRadius * 8)),
                        0
                    );
                    
                    for (Location point : circlePoints) {
                        particles.add(new ParticleSpawnData.Builder()
                            .location(point)
                            .particleType(particleType)
                            .count(1)
                            .size(size)
                            .opacity(opacity)
                            .layer(shell)
                            .build());
                    }
                }
            }
        }
        
        return particles;
    }
    
    /**
     * Create an instant explosion effect
     * 
     * @param location Explosion center
     * @param radius Explosion radius
     * @param particleType Particle type
     * @param particleCount Number of particles
     * @param player Player to show particles to (null for all)
     */
    public void createExplosion(Location location, double radius, Particle particleType, int particleCount, org.bukkit.entity.Player player) {
        if (location == null) {
            return;
        }
        
        // Create instant explosion at full radius
        List<ParticleSpawnData> particles = create3DExplosion(location, radius, particleType, particleCount, radius);
        
        // Spawn all particles
        for (ParticleSpawnData particle : particles) {
            Location loc = particle.getLocation();
            if (player != null) {
                player.spawnParticle(
                    particle.getParticle(),
                    loc,
                    particle.getCount(),
                    particle.getOffsetX(),
                    particle.getOffsetY(),
                    particle.getOffsetZ(),
                    particle.getSpeed()
                );
            } else {
                loc.getWorld().spawnParticle(
                    particle.getParticle(),
                    loc,
                    particle.getCount(),
                    particle.getOffsetX(),
                    particle.getOffsetY(),
                    particle.getOffsetZ(),
                    particle.getSpeed()
                );
            }
        }
    }
}
