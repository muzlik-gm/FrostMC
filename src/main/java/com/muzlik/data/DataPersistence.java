package com.muzlik.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles data persistence for player Fragment data.
 * Uses JSON format with async save/load operations.
 */
public class DataPersistence {
    private final JavaPlugin plugin;
    private final File dataFolder;
    private final Gson gson;
    private final AsyncExecutor asyncExecutor;

    public DataPersistence(JavaPlugin plugin) {
        this(plugin, new AsyncExecutor(plugin));
    }
    
    public DataPersistence(JavaPlugin plugin, AsyncExecutor asyncExecutor) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.asyncExecutor = asyncExecutor;
        
        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Save player data asynchronously using AsyncExecutor
     */
    public CompletableFuture<Void> savePlayerDataAsync(UUID playerId, PlayerDataContainer data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        asyncExecutor.executeAsync(() -> {
            try {
                savePlayerData(playerId, data);
                future.complete(null);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save data for player " + playerId + ": " + e.getMessage());
                future.completeExceptionally(e);
            }
        }, e -> future.completeExceptionally(e));
        
        return future;
    }

    /**
     * Save player data synchronously
     */
    public void savePlayerData(UUID playerId, PlayerDataContainer data) throws IOException {
        File playerFile = new File(dataFolder, playerId.toString() + ".json");
        
        // Create backup if file exists
        if (playerFile.exists()) {
            File backup = new File(dataFolder, playerId.toString() + ".json.backup");
            if (backup.exists()) {
                backup.delete();
            }
            playerFile.renameTo(backup);
        }
        
        // Write new data
        try (FileWriter writer = new FileWriter(playerFile)) {
            gson.toJson(data, writer);
        }
    }

    /**
     * Load player data asynchronously using AsyncExecutor
     */
    public CompletableFuture<PlayerDataContainer> loadPlayerDataAsync(UUID playerId) {
        CompletableFuture<PlayerDataContainer> future = new CompletableFuture<>();
        
        asyncExecutor.executeAsync(() -> {
            try {
                PlayerDataContainer data = loadPlayerData(playerId);
                future.complete(data);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load data for player " + playerId + ": " + e.getMessage());
                future.complete(createDefaultData(playerId));
            }
        }, e -> {
            plugin.getLogger().warning("Error loading data for player " + playerId + ": " + e.getMessage());
            future.complete(createDefaultData(playerId));
        });
        
        return future;
    }

    /**
     * Load player data synchronously (Task 16: Enhanced with validation and recovery)
     */
    public PlayerDataContainer loadPlayerData(UUID playerId) throws IOException {
        File playerFile = new File(dataFolder, playerId.toString() + ".json");
        
        if (!playerFile.exists()) {
            return createDefaultData(playerId);
        }
        
        try (FileReader reader = new FileReader(playerFile)) {
            PlayerDataContainer data = gson.fromJson(reader, PlayerDataContainer.class);
            if (data == null) {
                plugin.getLogger().warning("Corrupted data for player " + playerId + ", using defaults");
                return createDefaultData(playerId);
            }
            
            // Task 16.1 & 16.2: Validate and recover data
            if (!validatePlayerData(data)) {
                plugin.getLogger().warning("Invalid data detected for player " + playerId + ", attempting recovery");
                
                // Create backup of corrupted data
                createBackup(playerFile);
                
                // Attempt recovery
                data = recoverPlayerData(data, playerId);
                
                // Save recovered data
                savePlayerData(playerId, data);
                plugin.getLogger().info("Data recovered and saved for player " + playerId);
            }
            
            return data;
        } catch (Exception e) {
            plugin.getLogger().warning("Error parsing data for player " + playerId + ", using defaults");
            return createDefaultData(playerId);
        }
    }
    
    /**
     * Validate player data (Task 16.1)
     */
    private boolean validatePlayerData(PlayerDataContainer data) {
        if (data == null) return false;
        
        // Validate playerId
        if (data.playerId == null || data.playerId.isEmpty()) {
            return false;
        }
        
        // Validate activeFragment
        if (data.activeFragment != null) {
            try {
                com.muzlik.fragment.FragmentType.valueOf(data.activeFragment);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid active fragment type: " + data.activeFragment);
                return false;
            }
        }
        
        // Validate fragments map
        if (data.fragments != null) {
            for (Map.Entry<String, FragmentDataContainer> entry : data.fragments.entrySet()) {
                // Validate fragment type
                try {
                    com.muzlik.fragment.FragmentType.valueOf(entry.getKey());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid fragment type in data: " + entry.getKey());
                    return false;
                }
                
                FragmentDataContainer fragmentData = entry.getValue();
                if (fragmentData == null) continue;
                
                // Validate rank (1-8 for most fragments, up to 12 for some)
                if (fragmentData.rank < 1 || fragmentData.rank > 15) {
                    plugin.getLogger().warning("Invalid rank: " + fragmentData.rank);
                    return false;
                }
                
                // Validate level (1-50)
                if (fragmentData.level < 1 || fragmentData.level > 50) {
                    plugin.getLogger().warning("Invalid level: " + fragmentData.level);
                    return false;
                }
                
                // Validate XP (non-negative)
                if (fragmentData.xp < 0) {
                    plugin.getLogger().warning("Invalid XP: " + fragmentData.xp);
                    return false;
                }
            }
        }
        
        // Validate ability slots
        if (data.unlockedAbilitySlots != null) {
            for (Map.Entry<String, java.util.Set<Integer>> entry : data.unlockedAbilitySlots.entrySet()) {
                // Validate fragment type
                try {
                    com.muzlik.fragment.FragmentType.valueOf(entry.getKey());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid fragment type in ability slots: " + entry.getKey());
                    return false;
                }
                
                // Validate slot indices (0-4)
                if (entry.getValue() != null) {
                    for (Integer slot : entry.getValue()) {
                        if (slot < 0 || slot > 4) {
                            plugin.getLogger().warning("Invalid ability slot index: " + slot);
                            return false;
                        }
                    }
                }
            }
        }
        
        // Validate character level (1-100)
        if (data.characterLevel < 1 || data.characterLevel > 100) {
            plugin.getLogger().warning("Invalid character level: " + data.characterLevel);
            return false;
        }
        
        // Validate character XP (non-negative)
        if (data.characterXp < 0) {
            plugin.getLogger().warning("Invalid character XP: " + data.characterXp);
            return false;
        }
        
        return true;
    }
    
    /**
     * Recover corrupted player data (Task 16.2)
     */
    private PlayerDataContainer recoverPlayerData(PlayerDataContainer data, UUID playerId) {
        if (data == null) {
            return createDefaultData(playerId);
        }
        
        // Recover playerId
        if (data.playerId == null || data.playerId.isEmpty()) {
            data.playerId = playerId.toString();
        }
        
        // Recover activeFragment
        if (data.activeFragment != null) {
            try {
                com.muzlik.fragment.FragmentType.valueOf(data.activeFragment);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Removing invalid active fragment: " + data.activeFragment);
                data.activeFragment = null;
            }
        }
        
        // Recover fragments map
        if (data.fragments == null) {
            data.fragments = new HashMap<>();
        } else {
            // Remove invalid fragment types and clamp values
            Map<String, FragmentDataContainer> validFragments = new HashMap<>();
            for (Map.Entry<String, FragmentDataContainer> entry : data.fragments.entrySet()) {
                try {
                    com.muzlik.fragment.FragmentType.valueOf(entry.getKey());
                    FragmentDataContainer fragmentData = entry.getValue();
                    
                    if (fragmentData != null) {
                        // Clamp rank (1-15)
                        fragmentData.rank = Math.max(1, Math.min(15, fragmentData.rank));
                        
                        // Clamp level (1-50)
                        fragmentData.level = Math.max(1, Math.min(50, fragmentData.level));
                        
                        // Clamp XP (non-negative)
                        fragmentData.xp = Math.max(0, fragmentData.xp);
                        
                        // Initialize missing fields
                        if (fragmentData.abilityCooldowns == null) {
                            fragmentData.abilityCooldowns = new HashMap<>();
                        }
                        
                        validFragments.put(entry.getKey(), fragmentData);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Removing invalid fragment type: " + entry.getKey());
                }
            }
            data.fragments = validFragments;
        }
        
        // Recover ability slots
        if (data.unlockedAbilitySlots == null) {
            data.unlockedAbilitySlots = new HashMap<>();
        } else {
            // Remove invalid fragment types and slot indices
            Map<String, java.util.Set<Integer>> validSlots = new HashMap<>();
            for (Map.Entry<String, java.util.Set<Integer>> entry : data.unlockedAbilitySlots.entrySet()) {
                try {
                    com.muzlik.fragment.FragmentType.valueOf(entry.getKey());
                    
                    if (entry.getValue() != null) {
                        java.util.Set<Integer> validSlotIndices = new java.util.HashSet<>();
                        for (Integer slot : entry.getValue()) {
                            // Only keep valid slot indices (0-4)
                            if (slot >= 0 && slot <= 4) {
                                validSlotIndices.add(slot);
                            }
                        }
                        validSlots.put(entry.getKey(), validSlotIndices);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Removing invalid fragment type from ability slots: " + entry.getKey());
                }
            }
            data.unlockedAbilitySlots = validSlots;
        }
        
        // Recover character level (1-100)
        data.characterLevel = Math.max(1, Math.min(100, data.characterLevel));
        
        // Recover character XP (non-negative)
        data.characterXp = Math.max(0, data.characterXp);
        
        // Initialize missing fields
        if (data.completedRituals == null) {
            data.completedRituals = new java.util.ArrayList<>();
        }
        
        if (data.uiMode == null) {
            data.uiMode = "STANDARD";
        }
        
        return data;
    }
    
    /**
     * Create backup of corrupted data file (Task 16.3)
     */
    private void createBackup(File originalFile) {
        try {
            File backupFile = new File(dataFolder, originalFile.getName() + ".corrupted." + System.currentTimeMillis());
            java.nio.file.Files.copy(originalFile.toPath(), backupFile.toPath());
            plugin.getLogger().info("Created backup of corrupted data: " + backupFile.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create backup: " + e.getMessage());
        }
    }

    /**
     * Create default player data
     */
    private PlayerDataContainer createDefaultData(UUID playerId) {
        PlayerDataContainer data = new PlayerDataContainer();
        data.playerId = playerId.toString();
        data.activeFragment = null;
        data.uiMode = "STANDARD";
        data.lastFragmentChange = 0;
        data.fragments = new HashMap<>();
        return data;
    }

    /**
     * Delete player data
     */
    public void deletePlayerData(UUID playerId) {
        File playerFile = new File(dataFolder, playerId.toString() + ".json");
        if (playerFile.exists()) {
            playerFile.delete();
        }
    }

    /**
     * Check if player data exists
     */
    public boolean hasPlayerData(UUID playerId) {
        File playerFile = new File(dataFolder, playerId.toString() + ".json");
        return playerFile.exists();
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
        
        // Ability Slot Unlocks (Task 2.1)
        public Map<String, java.util.Set<Integer>> unlockedAbilitySlots;
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
    }
    
    /**
     * Shutdown AsyncExecutor gracefully
     */
    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
    }
}
