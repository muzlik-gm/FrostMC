package com.muzlik.data;

import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced data persistence system with multiple storage backends
 * Supports JSON, SQLite, and MySQL with automatic migration and repair
 */
public class DataPersistence {
    private final JavaPlugin plugin;
    private final DataStorageManager storageManager;

    public DataPersistence(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageManager = new DataStorageManager(plugin);
    }
    
    /**
     * Initialize the data persistence system
     */
    public CompletableFuture<Void> initialize() {
        return storageManager.initialize();
    }

    /**
     * Save player data asynchronously
     */
    public CompletableFuture<Void> savePlayerDataAsync(UUID playerId, PlayerDataContainer data) {
        if (storageManager.getStorage() == null) {
            plugin.getLogger().warning("Storage not yet initialized, cannot save data for player " + playerId);
            return CompletableFuture.completedFuture(null);
        }
        return storageManager.getStorage().savePlayerData(playerId, data);
    }

    /**
     * Load player data asynchronously
     */
    public CompletableFuture<PlayerDataContainer> loadPlayerDataAsync(UUID playerId) {
        if (storageManager.getStorage() == null) {
            // CRITICAL FIX: Storage not yet initialized, return default data with proper error handling
            plugin.getLogger().warning("Storage not yet initialized for player " + playerId + ", returning default data");
            PlayerDataContainer defaultData = new PlayerDataContainer();
            return CompletableFuture.completedFuture(defaultData);
        }
        
        // CRITICAL FIX: Add error handling for storage operations
        return storageManager.getStorage().loadPlayerData(playerId)
            .exceptionally(throwable -> {
                plugin.getLogger().severe("Failed to load player data for " + playerId + ": " + throwable.getMessage());
                throwable.printStackTrace();
                // Return default data on error to prevent crashes
                return new PlayerDataContainer();
            });
    }

    /**
     * Check if player data exists
     */
    public CompletableFuture<Boolean> hasPlayerData(UUID playerId) {
        if (storageManager.getStorage() == null) {
            return CompletableFuture.completedFuture(false);
        }
        return storageManager.getStorage().hasPlayerData(playerId);
    }

    /**
     * Delete player data
     */
    public CompletableFuture<Void> deletePlayerData(UUID playerId) {
        return storageManager.getStorage().deletePlayerData(playerId);
    }
    
    /**
     * Create manual backup
     */
    public CompletableFuture<Void> createBackup() {
        return storageManager.createBackup();
    }
    
    /**
     * Migrate storage from one type to another
     */
    public CompletableFuture<Void> migrateStorage(StorageType fromType, StorageType toType) {
        return storageManager.migrateStorage(fromType, toType);
    }
    
    /**
     * Get storage statistics
     */
    public CompletableFuture<DataStorage.StorageStats> getStorageStats() {
        return storageManager.getStats();
    }
    
    /**
     * Repair corrupted data entries
     */
    public CompletableFuture<Integer> repairCorruptedData() {
        return storageManager.repairCorruptedData();
    }
    
    /**
     * Check if storage is healthy
     */
    public CompletableFuture<Boolean> isStorageHealthy() {
        return storageManager.isHealthy();
    }
    
    /**
     * Get current storage type
     */
    public StorageType getCurrentStorageType() {
        return storageManager.getStorage().getType();
    }

    /**
     * Shutdown the data persistence system gracefully
     */
    public CompletableFuture<Void> shutdown() {
        return storageManager.shutdown();
    }

    /**
     * Container class for player data
     */
    public static class PlayerDataContainer {
        public String playerId;
        public String activeFragment;
        public String uiMode;
        public long lastFragmentChange;
        public Map<String, FragmentDataContainer> fragments;
        public java.util.List<String> completedRituals; // One-time fragment creation tracking
        
        // Character Level progression (separate from Fragment level)
        public int characterLevel = 1;
        public double characterXp = 0;
        
        // Ability Slot Unlocks
        public Map<String, java.util.Set<Integer>> unlockedAbilitySlots;
        
        // Data version for migration compatibility
        public int dataVersion = 1;
        
        // Timestamp for tracking data age
        public long lastUpdated = System.currentTimeMillis();
    }

    /**
     * Container class for Fragment data
     */
    public static class FragmentDataContainer {
        public boolean unlocked;
        public int rank;
        public int level;
        public double xp;
        public int prestigeLevel;
        public double currentMana;
        public Map<String, Long> abilityCooldowns;
        
        // Statistics tracking
        public long totalDamageDealt = 0;
        public int abilitiesUsed = 0;
        public long timeActive = 0; // milliseconds
        public long firstUnlocked = System.currentTimeMillis();
    }
}
