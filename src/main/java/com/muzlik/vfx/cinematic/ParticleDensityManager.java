package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages particle density to prevent view blocking.
 * Filters and redistributes particles to maintain visibility.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 9.2, 9.3
 */
public class ParticleDensityManager {
    
    private static final int MAX_PARTICLES_IN_VIEW_CONE = 50;
    private static final double VIEW_CONE_RADIUS = 2.0;
    private static final double VISIBILITY_CORRIDOR_ANGLE = 30.0; // degrees
    
    private final ViewConeCalculator viewConeCalculator;
    
    public ParticleDensityManager() {
        this.viewConeCalculator = new ViewConeCalculator();
    }
    
    /**
     * Filter and redistribute particles to prevent view blocking
     * 
     * @param player The player whose view to protect
     * @param particles Original particle list
     * @return Filtered/redistributed particle list
     */
    public List<ParticleSpawnData> filterForVisibility(Player player, List<ParticleSpawnData> particles) {
        if (player == null || particles == null || particles.isEmpty()) {
            return particles;
        }
        
        // Count particles in view cone
        int inViewCone = countInViewCone(player, particles);
        
        // If within limit, return as-is
        if (inViewCone <= MAX_PARTICLES_IN_VIEW_CONE) {
            return particles;
        }
        
        // Otherwise, filter and redistribute
        List<ParticleSpawnData> filtered = new ArrayList<>();
        List<ParticleSpawnData> inCone = new ArrayList<>();
        List<ParticleSpawnData> outCone = new ArrayList<>();
        
        // Separate particles
        for (ParticleSpawnData particle : particles) {
            if (viewConeCalculator.isInViewCone(player, particle.getLocation())) {
                inCone.add(particle);
            } else {
                outCone.add(particle);
            }
        }
        
        // Keep all particles outside cone
        filtered.addAll(outCone);
        
        // Sample particles from cone to stay under limit
        int keepCount = Math.min(inCone.size(), MAX_PARTICLES_IN_VIEW_CONE);
        double samplingRate = keepCount / (double) inCone.size();
        
        for (int i = 0; i < inCone.size(); i++) {
            if (i % (int)(1.0 / samplingRate) == 0 && filtered.size() < particles.size()) {
                filtered.add(inCone.get(i));
            }
        }
        
        return filtered;
    }
    
    /**
     * Calculate particles within player's immediate view cone
     * 
     * @param player The player
     * @param particles Particles to check
     * @return Count of particles in view cone
     */
    public int countInViewCone(Player player, List<ParticleSpawnData> particles) {
        if (player == null || particles == null) {
            return 0;
        }
        
        int count = 0;
        for (ParticleSpawnData particle : particles) {
            if (viewConeCalculator.isInViewCone(player, particle.getLocation())) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Redistribute particles from dense areas to outer regions
     * 
     * @param particles Original particles
     * @param center Effect center
     * @param maxInCenter Maximum allowed in center
     * @return Redistributed particles
     */
    public List<ParticleSpawnData> redistributeParticles(List<ParticleSpawnData> particles, 
                                                          Location center, int maxInCenter) {
        if (particles == null || center == null) {
            return particles;
        }
        
        List<ParticleSpawnData> redistributed = new ArrayList<>();
        List<ParticleSpawnData> centerParticles = new ArrayList<>();
        List<ParticleSpawnData> outerParticles = new ArrayList<>();
        
        // Separate center and outer particles
        double centerRadius = 1.5; // blocks
        for (ParticleSpawnData particle : particles) {
            double distance = particle.getLocation().distance(center);
            if (distance <= centerRadius) {
                centerParticles.add(particle);
            } else {
                outerParticles.add(particle);
            }
        }
        
        // Keep all outer particles
        redistributed.addAll(outerParticles);
        
        // Sample center particles if over limit
        if (centerParticles.size() <= maxInCenter) {
            redistributed.addAll(centerParticles);
        } else {
            double samplingRate = maxInCenter / (double) centerParticles.size();
            for (int i = 0; i < centerParticles.size(); i++) {
                if (i % (int)(1.0 / samplingRate) == 0) {
                    redistributed.add(centerParticles.get(i));
                }
            }
        }
        
        return redistributed;
    }
    
    /**
     * Create visibility corridor in player's look direction
     * 
     * @param player The player
     * @param particles Particles to filter
     * @return Particles with corridor cleared
     */
    public List<ParticleSpawnData> createVisibilityCorridor(Player player, List<ParticleSpawnData> particles) {
        if (player == null || particles == null) {
            return particles;
        }
        
        List<ParticleSpawnData> filtered = new ArrayList<>();
        
        for (ParticleSpawnData particle : particles) {
            // Keep particles outside the visibility corridor
            if (!viewConeCalculator.isInVisibilityCorridor(player, particle.getLocation())) {
                filtered.add(particle);
            }
        }
        
        return filtered;
    }
    
    /**
     * Scale particle counts based on current server TPS
     * 
     * @param particles Original particles
     * @param currentTPS Current server TPS
     * @return Scaled particle list
     */
    public List<ParticleSpawnData> scaleForPerformance(List<ParticleSpawnData> particles, double currentTPS) {
        if (particles == null || particles.isEmpty()) {
            return particles;
        }
        
        // No scaling needed if TPS is good
        if (currentTPS >= 18.0) {
            return particles;
        }
        
        // Calculate scaling factor
        double scaleFactor = 1.0;
        if (currentTPS < 10.0) {
            scaleFactor = 0.25; // 75% reduction
        } else if (currentTPS < 15.0) {
            scaleFactor = 0.25; // 75% reduction
        } else if (currentTPS < 18.0) {
            scaleFactor = 0.5; // 50% reduction
        }
        
        // Sample particles based on scale factor
        List<ParticleSpawnData> scaled = new ArrayList<>();
        int keepCount = (int) (particles.size() * scaleFactor);
        
        if (keepCount >= particles.size()) {
            return particles;
        }
        
        double step = particles.size() / (double) keepCount;
        for (int i = 0; i < keepCount; i++) {
            int index = (int) (i * step);
            if (index < particles.size()) {
                scaled.add(particles.get(index));
            }
        }
        
        return scaled;
    }
}
