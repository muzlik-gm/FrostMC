package com.muzlik.vfx;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Central registry for tracking all active effects in the Fragment system.
 * Provides fast lookup by owner and effect ID, automatic cleanup, and
 * resource management.
 * 
 * Requirements: 3.1, 3.3, 3.4, 3.5
 */
public class ActiveEffectRegistry {
    private final JavaPlugin plugin;
    
    // Main effect storage: effectId -> EffectEntry
    private final Map<UUID, EffectEntry> effects;
    
    // Index for fast lookup by owner: playerId -> Set<effectId>
    private final Map<UUID, Set<UUID>> playerEffects;
    
    // Cleanup task
    private BukkitTask cleanupTask;
    
    // Statistics
    private long totalEffectsSpawned = 0;
    private long totalEffectsCleaned = 0;
    
    /**
     * Constructor
     * 
     * @param plugin The plugin instance
     */
    public ActiveEffectRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.effects = new ConcurrentHashMap<>();
        this.playerEffects = new ConcurrentHashMap<>();
    }
    
    /**
     * Start the automatic cleanup task
     * Runs every 100 ticks (5 seconds) to clean up expired effects
     */
    public void startCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        // Run cleanup every 100 ticks (5 seconds)
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            cleanupExpiredEffects();
        }, 100L, 100L);
        
        plugin.getLogger().info("ActiveEffectRegistry cleanup task started");
    }
    
    /**
     * Stop the cleanup task
     */
    public void stopCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }
    
    /**
     * Register a new effect in the registry
     * 
     * @param entry The effect entry to register
     * @return The UUID of the registered effect
     */
    public UUID registerEffect(EffectEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("EffectEntry cannot be null");
        }
        
        UUID effectId = entry.getEffectId();
        UUID ownerId = entry.getOwnerId();
        
        // Store in main map
        effects.put(effectId, entry);
        
        // Index by owner
        playerEffects.computeIfAbsent(ownerId, k -> ConcurrentHashMap.newKeySet())
                    .add(effectId);
        
        totalEffectsSpawned++;
        
        plugin.getLogger().log(Level.FINE, "Registered effect: " + entry);
        
        return effectId;
    }
    
    /**
     * Unregister an effect from the registry
     * 
     * @param effectId The UUID of the effect to unregister
     * @return true if effect was found and removed
     */
    public boolean unregisterEffect(UUID effectId) {
        EffectEntry entry = effects.remove(effectId);
        
        if (entry == null) {
            return false;
        }
        
        // Remove from owner index
        UUID ownerId = entry.getOwnerId();
        Set<UUID> ownerEffects = playerEffects.get(ownerId);
        if (ownerEffects != null) {
            ownerEffects.remove(effectId);
            
            // Clean up empty sets
            if (ownerEffects.isEmpty()) {
                playerEffects.remove(ownerId);
            }
        }
        
        // Cleanup resources
        entry.cleanup();
        
        totalEffectsCleaned++;
        
        plugin.getLogger().log(Level.FINE, "Unregistered effect: " + effectId);
        
        return true;
    }
    
    /**
     * Clean up all effects owned by a specific player
     * Called when player logs out
     * 
     * @param playerId The UUID of the player
     * @return Number of effects cleaned up
     */
    public int cleanupPlayerEffects(UUID playerId) {
        Set<UUID> effectIds = playerEffects.remove(playerId);
        
        if (effectIds == null || effectIds.isEmpty()) {
            return 0;
        }
        
        int cleaned = 0;
        for (UUID effectId : effectIds) {
            EffectEntry entry = effects.remove(effectId);
            if (entry != null) {
                entry.cleanup();
                cleaned++;
                totalEffectsCleaned++;
            }
        }
        
        plugin.getLogger().info("Cleaned up " + cleaned + " effects for player " + playerId);
        
        return cleaned;
    }
    
    /**
     * Clean up all effects owned by a player (using Player object)
     * 
     * @param player The player
     * @return Number of effects cleaned up
     */
    public int cleanupPlayerEffects(Player player) {
        return cleanupPlayerEffects(player.getUniqueId());
    }
    
    /**
     * Get all effects owned by a specific player
     * 
     * @param ownerId The UUID of the owner
     * @return List of effect entries owned by this player
     */
    public List<EffectEntry> getEffectsByOwner(UUID ownerId) {
        Set<UUID> effectIds = playerEffects.get(ownerId);
        
        if (effectIds == null || effectIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return effectIds.stream()
                .map(effects::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Get an effect by its ID
     * 
     * @param effectId The UUID of the effect
     * @return The effect entry, or null if not found
     */
    public EffectEntry getEffect(UUID effectId) {
        return effects.get(effectId);
    }
    
    /**
     * Add a linked entity to an effect
     * 
     * @param effectId The UUID of the effect
     * @param entity The entity to link
     * @return true if entity was added
     */
    public boolean addLinkedEntity(UUID effectId, Entity entity) {
        EffectEntry entry = effects.get(effectId);
        if (entry != null) {
            entry.addLinkedEntity(entity);
            return true;
        }
        return false;
    }
    
    /**
     * Remove a linked entity from an effect
     * 
     * @param effectId The UUID of the effect
     * @param entity The entity to unlink
     * @return true if entity was removed
     */
    public boolean removeLinkedEntity(UUID effectId, Entity entity) {
        EffectEntry entry = effects.get(effectId);
        if (entry != null) {
            return entry.removeLinkedEntity(entity);
        }
        return false;
    }
    
    /**
     * Get all effects of a specific type
     * 
     * @param type The effect type to filter by
     * @return List of effects of the specified type
     */
    public List<EffectEntry> getEffectsByType(EffectType type) {
        return effects.values().stream()
                .filter(entry -> entry.getType() == type)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all effects owned by a player of a specific type
     * 
     * @param ownerId The UUID of the owner
     * @param type The effect type to filter by
     * @return List of effects owned by player of the specified type
     */
    public List<EffectEntry> getEffectsByOwnerAndType(UUID ownerId, EffectType type) {
        return getEffectsByOwner(ownerId).stream()
                .filter(entry -> entry.getType() == type)
                .collect(Collectors.toList());
    }
    
    /**
     * Clean up all expired effects
     * Called periodically by the cleanup task
     * 
     * @return Number of effects cleaned up
     */
    public int cleanupExpiredEffects() {
        long startTime = System.currentTimeMillis();
        int cleaned = 0;
        
        // Find all expired effects
        List<UUID> expiredIds = effects.values().stream()
                .filter(EffectEntry::isExpired)
                .map(EffectEntry::getEffectId)
                .collect(Collectors.toList());
        
        // Remove them
        for (UUID effectId : expiredIds) {
            if (unregisterEffect(effectId)) {
                cleaned++;
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        if (cleaned > 0) {
            plugin.getLogger().log(Level.FINE, 
                "Cleaned up " + cleaned + " expired effects in " + duration + "ms");
        }
        
        return cleaned;
    }
    
    /**
     * Get the total number of active effects
     * 
     * @return Number of active effects
     */
    public int getActiveEffectCount() {
        return effects.size();
    }
    
    /**
     * Get the number of effects owned by a specific player
     * 
     * @param playerId The UUID of the player
     * @return Number of effects owned by this player
     */
    public int getPlayerEffectCount(UUID playerId) {
        Set<UUID> effectIds = playerEffects.get(playerId);
        return effectIds != null ? effectIds.size() : 0;
    }
    
    /**
     * Get statistics about the registry
     * 
     * @return Map of statistic name to value
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("active_effects", (long) effects.size());
        stats.put("tracked_players", (long) playerEffects.size());
        stats.put("total_spawned", totalEffectsSpawned);
        stats.put("total_cleaned", totalEffectsCleaned);
        return stats;
    }
    
    /**
     * Clear all effects (for shutdown or testing)
     */
    public void clearAll() {
        plugin.getLogger().info("Clearing all effects from registry...");
        
        // Cleanup all effects
        for (EffectEntry entry : effects.values()) {
            entry.cleanup();
        }
        
        effects.clear();
        playerEffects.clear();
        
        plugin.getLogger().info("All effects cleared");
    }
    
    /**
     * Shutdown the registry
     * Stops cleanup task and clears all effects
     */
    public void shutdown() {
        stopCleanupTask();
        clearAll();
        
        plugin.getLogger().info("ActiveEffectRegistry shutdown complete. " +
                "Total effects spawned: " + totalEffectsSpawned + ", " +
                "Total cleaned: " + totalEffectsCleaned);
    }
}
