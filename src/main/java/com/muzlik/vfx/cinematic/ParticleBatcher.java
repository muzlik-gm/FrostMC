package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Batches particle spawning to minimize network overhead.
 * Enforces configurable max particles per player.
 * 
 * Requirements: 9.1, 9.5
 */
public class ParticleBatcher {
    
    private static final int DEFAULT_MAX_PARTICLES_PER_PLAYER = 500;
    
    private final Map<UUID, Integer> playerParticleCounts;
    private final int maxParticlesPerPlayer;
    
    public ParticleBatcher() {
        this(DEFAULT_MAX_PARTICLES_PER_PLAYER);
    }
    
    public ParticleBatcher(int maxParticlesPerPlayer) {
        this.playerParticleCounts = new HashMap<>();
        this.maxParticlesPerPlayer = maxParticlesPerPlayer;
    }
    
    /**
     * Spawn particles in batches for a player
     * 
     * @param player The player to spawn particles for
     * @param particles List of particles to spawn
     * @return Number of particles actually spawned
     */
    public int spawnParticles(Player player, List<ParticleSpawnData> particles) {
        if (player == null || particles == null || particles.isEmpty()) {
            return 0;
        }
        
        UUID playerId = player.getUniqueId();
        int currentCount = playerParticleCounts.getOrDefault(playerId, 0);
        
        // Calculate how many particles we can spawn
        int remainingCapacity = maxParticlesPerPlayer - currentCount;
        if (remainingCapacity <= 0) {
            return 0; // Player at max capacity
        }
        
        int toSpawn = Math.min(particles.size(), remainingCapacity);
        World world = player.getWorld();
        
        // Spawn particles
        int spawned = 0;
        for (int i = 0; i < toSpawn; i++) {
            ParticleSpawnData particle = particles.get(i);
            Location loc = particle.getLocation();
            
            if (loc.getWorld() == null || !loc.getWorld().equals(world)) {
                continue;
            }
            
            try {
                world.spawnParticle(
                    particle.getParticleType(),
                    loc,
                    particle.getCount(),
                    particle.getOffsetX(),
                    particle.getOffsetY(),
                    particle.getOffsetZ(),
                    particle.getSpeed(),
                    particle.getData()
                );
                spawned++;
            } catch (Exception e) {
                // Silently skip particles that fail to spawn
            }
        }
        
        // Update count
        playerParticleCounts.put(playerId, currentCount + spawned);
        
        return spawned;
    }
    
    /**
     * Reset particle count for a player (call at end of tick)
     */
    public void resetPlayerCount(UUID playerId) {
        playerParticleCounts.remove(playerId);
    }
    
    /**
     * Reset all particle counts (call at end of tick)
     */
    public void resetAllCounts() {
        playerParticleCounts.clear();
    }
    
    /**
     * Get current particle count for a player
     */
    public int getPlayerParticleCount(UUID playerId) {
        return playerParticleCounts.getOrDefault(playerId, 0);
    }
    
    /**
     * Get max particles per player
     */
    public int getMaxParticlesPerPlayer() {
        return maxParticlesPerPlayer;
    }
    
    /**
     * Check if player can spawn more particles
     */
    public boolean canSpawnParticles(Player player, int count) {
        if (player == null) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        int currentCount = playerParticleCounts.getOrDefault(playerId, 0);
        return (currentCount + count) <= maxParticlesPerPlayer;
    }
    
    /**
     * Add particles to player's count
     */
    public void addParticles(Player player, int count) {
        if (player == null) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        int currentCount = playerParticleCounts.getOrDefault(playerId, 0);
        playerParticleCounts.put(playerId, currentCount + count);
    }
}
