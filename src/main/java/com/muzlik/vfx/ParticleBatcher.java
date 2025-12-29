package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Batches particle spawns to reduce packet spam and improve performance.
 * Groups nearby particles and flushes them every tick.
 */
public class ParticleBatcher {
    
    private final JavaPlugin plugin;
    private final Map<ParticleKey, ParticleData> particleQueue;
    private BukkitRunnable flushTask;
    private static final double GROUPING_DISTANCE = 1.0; // Group particles within 1 block
    
    public ParticleBatcher(JavaPlugin plugin) {
        this.plugin = plugin;
        this.particleQueue = new ConcurrentHashMap<>();
        startFlushTask();
    }
    
    /**
     * Queue a particle for batched spawning
     */
    public void queueParticle(Location location, Particle particle, int count, 
                             double offsetX, double offsetY, double offsetZ, 
                             double extra, Object data, Player viewer) {
        
        // Find or create key for grouping
        ParticleKey key = findOrCreateKey(location, particle, viewer);
        
        // Add to existing batch or create new
        particleQueue.compute(key, (k, existing) -> {
            if (existing == null) {
                return new ParticleData(location.clone(), particle, count, 
                                       offsetX, offsetY, offsetZ, extra, data, viewer);
            } else {
                // Merge with existing batch
                existing.count += count;
                return existing;
            }
        });
    }
    
    /**
     * Find existing key for nearby particles or create new one
     */
    private ParticleKey findOrCreateKey(Location location, Particle particle, Player viewer) {
        // Check for nearby particles of same type
        for (ParticleKey existingKey : particleQueue.keySet()) {
            if (existingKey.particle == particle && 
                existingKey.viewer == viewer &&
                existingKey.location.getWorld() == location.getWorld() &&
                existingKey.location.distanceSquared(location) < GROUPING_DISTANCE * GROUPING_DISTANCE) {
                return existingKey;
            }
        }
        
        // Create new key
        return new ParticleKey(location.clone(), particle, viewer);
    }
    
    /**
     * Start automatic flush task (runs every tick)
     */
    private void startFlushTask() {
        flushTask = new BukkitRunnable() {
            @Override
            public void run() {
                flush();
            }
        };
        flushTask.runTaskTimer(plugin, 1L, 1L);
    }
    
    /**
     * Flush all queued particles
     */
    public void flush() {
        if (particleQueue.isEmpty()) {
            return;
        }
        
        // Spawn all batched particles
        for (Map.Entry<ParticleKey, ParticleData> entry : particleQueue.entrySet()) {
            ParticleData data = entry.getValue();
            
            if (data.viewer != null) {
                // Spawn for specific player
                data.viewer.spawnParticle(data.particle, data.location, data.count,
                                         data.offsetX, data.offsetY, data.offsetZ, 
                                         data.extra, data.data);
            } else {
                // Spawn for all players
                data.location.getWorld().spawnParticle(data.particle, data.location, 
                                                      data.count, data.offsetX, 
                                                      data.offsetY, data.offsetZ, 
                                                      data.extra, data.data);
            }
        }
        
        // Clear queue
        particleQueue.clear();
    }
    
    /**
     * Shutdown and flush remaining particles
     */
    public void shutdown() {
        if (flushTask != null) {
            flushTask.cancel();
        }
        flush();
    }
    
    /**
     * Get current queue size
     */
    public int getQueueSize() {
        return particleQueue.size();
    }
    
    /**
     * Key for grouping particles
     */
    private static class ParticleKey {
        final Location location;
        final Particle particle;
        final Player viewer;
        
        ParticleKey(Location location, Particle particle, Player viewer) {
            this.location = location;
            this.particle = particle;
            this.viewer = viewer;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParticleKey)) return false;
            ParticleKey that = (ParticleKey) o;
            return particle == that.particle && 
                   Objects.equals(viewer, that.viewer) &&
                   location.getWorld() == that.location.getWorld() &&
                   Math.abs(location.getX() - that.location.getX()) < GROUPING_DISTANCE &&
                   Math.abs(location.getY() - that.location.getY()) < GROUPING_DISTANCE &&
                   Math.abs(location.getZ() - that.location.getZ()) < GROUPING_DISTANCE;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(particle, viewer, 
                              (int)(location.getX() / GROUPING_DISTANCE),
                              (int)(location.getY() / GROUPING_DISTANCE),
                              (int)(location.getZ() / GROUPING_DISTANCE));
        }
    }
    
    /**
     * Data for batched particles
     */
    private static class ParticleData {
        final Location location;
        final Particle particle;
        int count;
        final double offsetX;
        final double offsetY;
        final double offsetZ;
        final double extra;
        final Object data;
        final Player viewer;
        
        ParticleData(Location location, Particle particle, int count,
                    double offsetX, double offsetY, double offsetZ,
                    double extra, Object data, Player viewer) {
            this.location = location;
            this.particle = particle;
            this.count = count;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.extra = extra;
            this.data = data;
            this.viewer = viewer;
        }
    }
}
