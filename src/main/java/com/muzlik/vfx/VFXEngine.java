package com.muzlik.vfx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Central VFX Engine for managing all visual effects.
 * Handles particle spawning, animation, rank scaling, and cleanup.
 * 
 * Requirements: 1.1, 1.2, 1.5
 */
public class VFXEngine {
    private final JavaPlugin plugin;
    private final ActiveEffectRegistry effectRegistry;
    
    // Active effects map: effectId -> ActiveEffect
    private final Map<UUID, ActiveEffect> activeEffects;
    
    // Update task
    private BukkitTask updateTask;
    
    // Rank scaling multiplier (15% per rank)
    private static final double RANK_PARTICLE_MULTIPLIER = 0.15;
    private static final double RANK_INTENSITY_MULTIPLIER = 0.10;
    
    /**
     * Constructor
     * 
     * @param plugin The plugin instance
     * @param effectRegistry The effect registry for tracking
     */
    public VFXEngine(JavaPlugin plugin, ActiveEffectRegistry effectRegistry) {
        this.plugin = plugin;
        this.effectRegistry = effectRegistry;
        this.activeEffects = new ConcurrentHashMap<>();
    }
    
    /**
     * Start the update task
     * Runs every tick to update all active effects
     */
    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Run every tick
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateEffects();
        }, 1L, 1L);
        
        plugin.getLogger().info("VFXEngine update task started");
    }
    
    /**
     * Stop the update task
     */
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    /**
     * Spawn a new effect
     * 
     * @param effect The effect definition
     * @param location The location to spawn the effect
     * @param owner The player who owns this effect
     * @param rank The Fragment rank for scaling
     * @return The UUID of the spawned effect
     */
    public UUID spawnEffect(EffectDefinition effect, Location location, Player owner, int rank) {
        if (effect == null || location == null || owner == null) {
            throw new IllegalArgumentException("Effect, location, and owner cannot be null");
        }
        
        UUID effectId = UUID.randomUUID();
        
        // Apply rank scaling if enabled
        int scaledRank = effect.isRankScaling() ? rank : 1;
        
        // Create active effect
        ActiveEffect activeEffect = new ActiveEffect(effectId, owner.getUniqueId(), 
                                                    effect, location, scaledRank);
        
        // Store in active effects
        activeEffects.put(effectId, activeEffect);
        
        // Register in effect registry
        long lifetimeMillis = effect.getLifetime() * 50L; // Convert ticks to milliseconds
        EffectEntry entry = new EffectEntry(
            effectId,
            owner.getUniqueId(),
            EffectType.PARTICLE,
            location,
            System.currentTimeMillis(),
            System.currentTimeMillis() + lifetimeMillis,
            activeEffect
        );
        effectRegistry.registerEffect(entry);
        
        plugin.getLogger().log(Level.FINE, 
                "Spawned effect: " + effect.getId() + " at " + location + 
                " for " + owner.getName() + " (rank " + rank + ")");
        
        return effectId;
    }
    
    /**
     * Spawn an effect without rank scaling
     */
    public UUID spawnEffect(EffectDefinition effect, Location location, Player owner) {
        return spawnEffect(effect, location, owner, 1);
    }
    
    /**
     * Update all active effects
     * Called every tick by the update task
     */
    public void updateEffects() {
        if (activeEffects.isEmpty()) {
            return;
        }
        
        // Update all effects
        List<UUID> completedEffects = new ArrayList<>();
        
        for (Map.Entry<UUID, ActiveEffect> entry : activeEffects.entrySet()) {
            ActiveEffect effect = entry.getValue();
            
            try {
                // Update the effect
                effect.update();
                
                // Check if complete
                if (effect.isComplete()) {
                    completedEffects.add(entry.getKey());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                        "Error updating effect " + entry.getKey(), e);
                completedEffects.add(entry.getKey());
            }
        }
        
        // Clean up completed effects
        for (UUID effectId : completedEffects) {
            removeEffect(effectId);
        }
    }
    
    /**
     * Remove an effect
     * 
     * @param effectId The UUID of the effect to remove
     */
    public void removeEffect(UUID effectId) {
        ActiveEffect effect = activeEffects.remove(effectId);
        
        if (effect != null) {
            effectRegistry.unregisterEffect(effectId);
            
            plugin.getLogger().log(Level.FINE, 
                    "Removed effect: " + effectId);
        }
    }
    
    /**
     * Scale effect by rank
     * Increases particle count and intensity based on rank
     * 
     * @param effect The active effect
     * @param rank The Fragment rank
     */
    public void scaleEffectByRank(ActiveEffect effect, int rank) {
        if (effect == null || rank <= 1) {
            return;
        }
        
        // Rank scaling is applied during effect creation
        // This method can be used for dynamic scaling if needed
        
        plugin.getLogger().log(Level.FINE, 
                "Scaled effect " + effect.getEffectId() + " to rank " + rank);
    }
    
    /**
     * Clean up expired effects
     * Called periodically to remove old effects
     */
    public void cleanupExpiredEffects() {
        List<UUID> expiredIds = new ArrayList<>();
        
        for (Map.Entry<UUID, ActiveEffect> entry : activeEffects.entrySet()) {
            if (entry.getValue().isComplete()) {
                expiredIds.add(entry.getKey());
            }
        }
        
        for (UUID effectId : expiredIds) {
            removeEffect(effectId);
        }
        
        if (!expiredIds.isEmpty()) {
            plugin.getLogger().log(Level.FINE, 
                    "Cleaned up " + expiredIds.size() + " expired effects");
        }
    }
    
    /**
     * Get the number of active effects
     */
    public int getActiveEffectCount() {
        return activeEffects.size();
    }
    
    /**
     * Get an active effect by ID
     */
    public ActiveEffect getEffect(UUID effectId) {
        return activeEffects.get(effectId);
    }
    
    /**
     * Clear all effects
     */
    public void clearAll() {
        activeEffects.clear();
        plugin.getLogger().info("Cleared all VFX effects");
    }
    
    /**
     * Shutdown the VFX engine
     */
    public void shutdown() {
        stopUpdateTask();
        clearAll();
        plugin.getLogger().info("VFXEngine shutdown complete");
    }
    
    /**
     * Calculate scaled particle count based on rank
     * 
     * @param baseCount Base particle count
     * @param rank Fragment rank
     * @return Scaled particle count
     */
    public static int calculateScaledParticleCount(int baseCount, int rank) {
        if (rank <= 1) {
            return baseCount;
        }
        
        double multiplier = 1.0 + ((rank - 1) * RANK_PARTICLE_MULTIPLIER);
        return (int) Math.ceil(baseCount * multiplier);
    }
    
    /**
     * Calculate scaled intensity based on rank
     * 
     * @param baseIntensity Base intensity
     * @param rank Fragment rank
     * @return Scaled intensity
     */
    public static double calculateScaledIntensity(double baseIntensity, int rank) {
        if (rank <= 1) {
            return baseIntensity;
        }
        
        return baseIntensity * (1.0 + ((rank - 1) * RANK_INTENSITY_MULTIPLIER));
    }
}
