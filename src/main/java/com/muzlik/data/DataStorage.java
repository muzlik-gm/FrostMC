package com.muzlik.data;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for different data storage backends
 * Provides async operations for all storage types
 */
public interface DataStorage {
    
    /**
     * Initialize the storage backend
     * @return CompletableFuture that completes when initialization is done
     */
    CompletableFuture<Void> initialize();
    
    /**
     * Save player data asynchronously
     * @param playerId Player UUID
     * @param data Player data container
     * @return CompletableFuture that completes when save is done
     */
    CompletableFuture<Void> savePlayerData(UUID playerId, DataPersistence.PlayerDataContainer data);
    
    /**
     * Load player data asynchronously
     * @param playerId Player UUID
     * @return CompletableFuture with player data (or default data if not found)
     */
    CompletableFuture<DataPersistence.PlayerDataContainer> loadPlayerData(UUID playerId);
    
    /**
     * Check if player data exists
     * @param playerId Player UUID
     * @return CompletableFuture with boolean result
     */
    CompletableFuture<Boolean> hasPlayerData(UUID playerId);
    
    /**
     * Delete player data
     * @param playerId Player UUID
     * @return CompletableFuture that completes when deletion is done
     */
    CompletableFuture<Void> deletePlayerData(UUID playerId);
    
    /**
     * Create backup of all data
     * @return CompletableFuture that completes when backup is done
     */
    CompletableFuture<Void> createBackup();
    
    /**
     * Migrate data from another storage type
     * @param fromStorage Source storage to migrate from
     * @return CompletableFuture that completes when migration is done
     */
    CompletableFuture<Void> migrateFrom(DataStorage fromStorage);
    
    /**
     * Get all player UUIDs in storage
     * @return CompletableFuture with set of all player UUIDs
     */
    CompletableFuture<java.util.Set<UUID>> getAllPlayerIds();
    
    /**
     * Repair corrupted data entries
     * @return CompletableFuture with number of repaired entries
     */
    CompletableFuture<Integer> repairCorruptedData();
    
    /**
     * Get storage statistics
     * @return CompletableFuture with storage stats
     */
    CompletableFuture<StorageStats> getStats();
    
    /**
     * Shutdown the storage backend gracefully
     * @return CompletableFuture that completes when shutdown is done
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Get the storage type
     */
    StorageType getType();
    
    /**
     * Check if storage is healthy
     */
    CompletableFuture<Boolean> isHealthy();
    
    /**
     * Storage statistics container
     */
    class StorageStats {
        public final int totalPlayers;
        public final long totalSize;
        public final int corruptedEntries;
        public final long lastBackup;
        public final boolean healthy;
        
        public StorageStats(int totalPlayers, long totalSize, int corruptedEntries, long lastBackup, boolean healthy) {
            this.totalPlayers = totalPlayers;
            this.totalSize = totalSize;
            this.corruptedEntries = corruptedEntries;
            this.lastBackup = lastBackup;
            this.healthy = healthy;
        }
    }
}