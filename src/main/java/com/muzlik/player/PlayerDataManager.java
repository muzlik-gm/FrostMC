package com.muzlik.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all player power data globally with persistence
 */
public class PlayerDataManager implements Listener {

    private final Map<String, PlayerPowerData> playerData = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final File dataFolder;

    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Get or create player power data
     * @param player The player
     * @return The player's power data
     */
    public PlayerPowerData getOrCreatePlayerData(Player player) {
        return playerData.computeIfAbsent(
                player.getUniqueId().toString(),
                k -> new PlayerPowerData(player)
        );
    }

    /**
     * Get player power data
     * @param player The player
     * @return The player's power data, or null if not loaded
     */
    public PlayerPowerData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId().toString());
    }

    /**
     * Check if we have data for a player
     * @param player The player
     */
    public boolean hasPlayerData(Player player) {
        return playerData.containsKey(player.getUniqueId().toString());
    }

    /**
     * Save player data to file
     */
    public void savePlayerData(Player player) {
        PlayerPowerData data = getPlayerData(player);
        if (data == null) return;

        File file = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        if (data.getCurrentPower() != null) {
            config.set("active_power", data.getCurrentPower().getId());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Load player data from file
     */
    public String loadPlayerData(Player player) {
        File file = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        if (!file.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getString("active_power");
    }

    /**
     * Remove player data (call when player leaves)
     * @param player The player
     */
    public void removePlayerData(Player player) {
        playerData.remove(player.getUniqueId().toString());
    }

    /**
     * Handle player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Data will be loaded by PowerManager
    }

    /**
     * Handle player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerData(event.getPlayer());
        removePlayerData(event.getPlayer());
    }

    /**
     * Clear all player data (emergency cleanup)
     */
    public void clearAll() {
        playerData.clear();
    }
}


