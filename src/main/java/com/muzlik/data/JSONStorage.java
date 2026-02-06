package com.muzlik.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * JSON file implementation of DataStorage
 * Suitable for small servers (< 50 players)
 * Simple but doesn't scale well
 */
public class JSONStorage implements DataStorage {
    
    private final JavaPlugin plugin;
    private final AsyncExecutor asyncExecutor;
    private final Gson gson;
    private final File dataFolder;
    
    public JSONStorage(JavaPlugin plugin, AsyncExecutor asyncExecutor) {
        this.plugin = plugin;
        this.asyncExecutor = asyncExecutor;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
    }
    
    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            // Create data folder if it doesn't exist
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            plugin.getLogger().info("JSON storage initialized successfully");
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(UUID playerId, DataPersistence.PlayerDataContainer data) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, playerId.toString() + ".json");
            
            try {
                // Create backup if file exists
                if (playerFile.exists()) {
                    File backup = new File(dataFolder, playerId.toString() + ".json.backup");
                    if (backup.exists()) {
                        backup.delete();
                    }
                    java.nio.file.Files.copy(playerFile.toPath(), backup.toPath());
                }
                
                // Write new data
                try (FileWriter writer = new FileWriter(playerFile)) {
                    gson.toJson(data, writer);
                }
                
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save data for player " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<DataPersistence.PlayerDataContainer> loadPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = new File(dataFolder, playerId.toString() + ".json");
            
            if (!playerFile.exists()) {
                return createDefaultData(playerId);
            }
            
            try (FileReader reader = new FileReader(playerFile)) {
                DataPersistence.PlayerDataContainer data = gson.fromJson(reader, DataPersistence.PlayerDataContainer.class);
                
                if (data == null) {
                    plugin.getLogger().warning("Corrupted data for player " + playerId + ", using defaults");
                    createCorruptedBackup(playerFile);
                    return createDefaultData(playerId);
                }
                
                // Validate and repair data
                if (!validateData(data)) {
                    plugin.getLogger().warning("Invalid data detected for player " + playerId + ", attempting recovery");
                    createCorruptedBackup(playerFile);
                    data = repairData(data, playerId);
                    // Save repaired data
                    savePlayerData(playerId, data).join();
                    plugin.getLogger().info("Data recovered and saved for player " + playerId);
                }
                
                return data;
                
            } catch (Exception e) {
                plugin.getLogger().warning("Error parsing data for player " + playerId + ": " + e.getMessage());
                createCorruptedBackup(playerFile);
                return createDefaultData(playerId);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Boolean> hasPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = new File(dataFolder, playerId.toString() + ".json");
            return playerFile.exists();
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> deletePlayerData(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, playerId.toString() + ".json");
            if (playerFile.exists()) {
                playerFile.delete();
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> createBackup() {
        return CompletableFuture.runAsync(() -> {
            try {
                File backupDir = new File(plugin.getDataFolder(), "backups");
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                
                String timestamp = String.valueOf(System.currentTimeMillis());
                File backupFolder = new File(backupDir, "playerdata_" + timestamp);
                backupFolder.mkdirs();
                
                // Copy all JSON files
                File[] jsonFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.endsWith(".backup"));
                if (jsonFiles != null) {
                    for (File jsonFile : jsonFiles) {
                        File backupFile = new File(backupFolder, jsonFile.getName());
                        java.nio.file.Files.copy(jsonFile.toPath(), backupFile.toPath());
                    }
                }
                
                plugin.getLogger().info("JSON backup created: " + backupFolder.getName());
                
                // Clean old backups (keep last 10)
                File[] backups = backupDir.listFiles(File::isDirectory);
                if (backups != null && backups.length > 10) {
                    java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
                    for (int i = 0; i < backups.length - 10; i++) {
                        deleteDirectory(backups[i]);
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create JSON backup: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> migrateFrom(DataStorage fromStorage) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting migration from " + fromStorage.getType() + " to JSON...");
                
                Set<UUID> playerIds = fromStorage.getAllPlayerIds().join();
                int migrated = 0;
                int failed = 0;
                
                for (UUID playerId : playerIds) {
                    try {
                        DataPersistence.PlayerDataContainer data = fromStorage.loadPlayerData(playerId).join();
                        savePlayerData(playerId, data).join();
                        migrated++;
                        
                        if (migrated % 50 == 0) {
                            plugin.getLogger().info("Migrated " + migrated + "/" + playerIds.size() + " players...");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to migrate player " + playerId + ": " + e.getMessage());
                        failed++;
                    }
                }
                
                plugin.getLogger().info("Migration completed: " + migrated + " successful, " + failed + " failed");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Migration failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Set<UUID>> getAllPlayerIds() {
        return CompletableFuture.supplyAsync(() -> {
            Set<UUID> playerIds = new HashSet<>();
            
            plugin.getLogger().info("Scanning for JSON player data files in: " + dataFolder.getAbsolutePath());
            
            // Check if data folder exists
            if (!dataFolder.exists()) {
                plugin.getLogger().info("Player data folder doesn't exist, returning empty set");
                return playerIds;
            }
            
            // Check if folder is readable
            if (!dataFolder.canRead()) {
                plugin.getLogger().warning("Cannot read player data folder: " + dataFolder.getAbsolutePath());
                return playerIds;
            }
            
            File[] jsonFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.endsWith(".backup"));
            
            if (jsonFiles == null) {
                plugin.getLogger().warning("Failed to list files in directory: " + dataFolder.getAbsolutePath());
                return playerIds;
            }
            
            plugin.getLogger().info("Found " + jsonFiles.length + " JSON files to process");
            
            for (File jsonFile : jsonFiles) {
                String fileName = jsonFile.getName();
                String uuidStr = fileName.substring(0, fileName.length() - 5); // Remove .json
                
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    playerIds.add(playerId);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in filename: " + fileName);
                }
            }
            
            plugin.getLogger().info("Successfully processed " + playerIds.size() + " valid player IDs");
            return playerIds;
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Integer> repairCorruptedData() {
        return CompletableFuture.supplyAsync(() -> {
            int repaired = 0;
            
            File[] jsonFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.endsWith(".backup"));
            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    try {
                        String fileName = jsonFile.getName();
                        String uuidStr = fileName.substring(0, fileName.length() - 5);
                        UUID playerId = UUID.fromString(uuidStr);
                        
                        // Try to load and validate data
                        try (FileReader reader = new FileReader(jsonFile)) {
                            DataPersistence.PlayerDataContainer data = gson.fromJson(reader, DataPersistence.PlayerDataContainer.class);
                            
                            if (data == null || !validateData(data)) {
                                plugin.getLogger().info("Repairing corrupted data for player " + playerId);
                                
                                // Create backup of corrupted file
                                createCorruptedBackup(jsonFile);
                                
                                // Repair or create default data
                                if (data != null) {
                                    data = repairData(data, playerId);
                                } else {
                                    data = createDefaultData(playerId);
                                }
                                
                                // Save repaired data
                                savePlayerData(playerId, data).join();
                                repaired++;
                            }
                        }
                        
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to repair file " + jsonFile.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            if (repaired > 0) {
                plugin.getLogger().info("Repaired " + repaired + " corrupted JSON files");
            }
            
            return repaired;
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<StorageStats> getStats() {
        return CompletableFuture.supplyAsync(() -> {
            int totalPlayers = 0;
            long totalSize = 0;
            int corruptedEntries = 0;
            
            File[] jsonFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.endsWith(".backup"));
            if (jsonFiles != null) {
                totalPlayers = jsonFiles.length;
                
                for (File jsonFile : jsonFiles) {
                    totalSize += jsonFile.length();
                    
                    // Check if file is corrupted
                    try (FileReader reader = new FileReader(jsonFile)) {
                        DataPersistence.PlayerDataContainer data = gson.fromJson(reader, DataPersistence.PlayerDataContainer.class);
                        if (data == null || !validateData(data)) {
                            corruptedEntries++;
                        }
                    } catch (Exception e) {
                        corruptedEntries++;
                    }
                }
            }
            
            long lastBackup = getLastBackupTime();
            boolean healthy = isHealthy().join();
            
            return new StorageStats(totalPlayers, totalSize, corruptedEntries, lastBackup, healthy);
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.completedFuture(null); // No cleanup needed for JSON
    }
    
    @Override
    public StorageType getType() {
        return StorageType.JSON;
    }
    
    @Override
    public CompletableFuture<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            return dataFolder.exists() && dataFolder.canRead() && dataFolder.canWrite();
        }, asyncExecutor.getExecutor());
    }
    
    // Helper methods
    
    private boolean validateData(DataPersistence.PlayerDataContainer data) {
        if (data == null) return false;
        if (data.playerId == null || data.playerId.isEmpty()) return false;
        if (data.fragments == null) return false;
        if (data.characterLevel < 1 || data.characterLevel > 100) return false;
        if (data.characterXp < 0) return false;
        return true;
    }
    
    private DataPersistence.PlayerDataContainer repairData(DataPersistence.PlayerDataContainer data, UUID playerId) {
        if (data == null) {
            return createDefaultData(playerId);
        }
        
        // Repair basic fields
        if (data.playerId == null || data.playerId.isEmpty()) {
            data.playerId = playerId.toString();
        }
        
        if (data.fragments == null) {
            data.fragments = new java.util.HashMap<>();
        }
        
        // Clamp values to valid ranges
        data.characterLevel = Math.max(1, Math.min(100, data.characterLevel));
        data.characterXp = Math.max(0, data.characterXp);
        
        // Initialize missing fields
        if (data.completedRituals == null) {
            data.completedRituals = new java.util.ArrayList<>();
        }
        
        if (data.unlockedAbilitySlots == null) {
            data.unlockedAbilitySlots = new java.util.HashMap<>();
        }
        
        if (data.uiMode == null) {
            data.uiMode = "STANDARD";
        }
        
        return data;
    }
    
    private DataPersistence.PlayerDataContainer createDefaultData(UUID playerId) {
        DataPersistence.PlayerDataContainer data = new DataPersistence.PlayerDataContainer();
        data.playerId = playerId.toString();
        data.activeFragment = null;
        data.uiMode = "STANDARD";
        data.lastFragmentChange = 0;
        data.fragments = new java.util.HashMap<>();
        data.completedRituals = new java.util.ArrayList<>();
        data.characterLevel = 1;
        data.characterXp = 0;
        data.unlockedAbilitySlots = new java.util.HashMap<>();
        return data;
    }
    
    private void createCorruptedBackup(File originalFile) {
        try {
            File backupFile = new File(dataFolder, originalFile.getName() + ".corrupted." + System.currentTimeMillis());
            java.nio.file.Files.copy(originalFile.toPath(), backupFile.toPath());
            plugin.getLogger().info("Created backup of corrupted data: " + backupFile.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create corrupted backup: " + e.getMessage());
        }
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    private long getLastBackupTime() {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) return 0;
        
        File[] backups = backupDir.listFiles(File::isDirectory);
        if (backups == null || backups.length == 0) return 0;
        
        long latest = 0;
        for (File backup : backups) {
            latest = Math.max(latest, backup.lastModified());
        }
        
        return latest;
    }
}