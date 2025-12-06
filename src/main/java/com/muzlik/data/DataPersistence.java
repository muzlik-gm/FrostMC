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

    public DataPersistence(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Save player data asynchronously
     */
    public CompletableFuture<Void> savePlayerDataAsync(UUID playerId, PlayerDataContainer data) {
        return CompletableFuture.runAsync(() -> {
            try {
                savePlayerData(playerId, data);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save data for player " + playerId + ": " + e.getMessage());
            }
        });
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
     * Load player data asynchronously
     */
    public CompletableFuture<PlayerDataContainer> loadPlayerDataAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadPlayerData(playerId);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load data for player " + playerId + ": " + e.getMessage());
                return createDefaultData(playerId);
            }
        });
    }

    /**
     * Load player data synchronously
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
            return data;
        } catch (Exception e) {
            plugin.getLogger().warning("Error parsing data for player " + playerId + ", using defaults");
            return createDefaultData(playerId);
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
}
