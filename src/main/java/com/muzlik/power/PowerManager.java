package com.muzlik.power;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import com.muzlik.player.PlayerDataManager;
import com.muzlik.player.PlayerPowerData;
import com.muzlik.cooldown.CooldownManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main manager for all powers in the plugin
 * Handles power registration, activation, and player management
 */
public class PowerManager {

    private final JavaPlugin plugin;
    private final Map<String, IPower> registeredPowers = new ConcurrentHashMap<>();
    private final PlayerDataManager playerDataManager;
    private final CooldownManager cooldownManager;

    /**
     * Create the power manager
     * @param plugin The plugin instance
     */
    public PowerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = new PlayerDataManager(plugin);
        this.cooldownManager = new CooldownManager();
        
        // Register the player data manager as a listener
        plugin.getServer().getPluginManager().registerEvents(playerDataManager, plugin);

        plugin.getLogger().info("PowerManager initialized");
    }

    /**
     * Register a new power
     * @param power The power to register
     */
    public void registerPower(IPower power) {
        if (power == null) {
            throw new IllegalArgumentException("Power cannot be null");
        }

        String powerId = power.getId();
        if (registeredPowers.containsKey(powerId)) {
            plugin.getLogger().warning("Power with ID '" + powerId + "' is already registered!");
            return;
        }

        registeredPowers.put(powerId, power);
        plugin.getLogger().info("Power registered: " + power.getDisplayName() + " (" + powerId + ")");
    }

    /**
     * Unregister a power
     * @param powerId The ID of the power to unregister
     */
    public void unregisterPower(String powerId) {
        IPower power = registeredPowers.remove(powerId);
        if (power != null) {
            power.cleanup();
            plugin.getLogger().info("Power unregistered: " + power.getDisplayName());
        }
    }

    /**
     * Get a registered power
     * @param powerId The ID of the power
     * @return The power, or null if not found
     */
    public IPower getPower(String powerId) {
        return registeredPowers.get(powerId);
    }

    /**
     * Get all registered powers
     */
    public Collection<IPower> getAllPowers() {
        return new ArrayList<>(registeredPowers.values());
    }

    /**
     * Get the number of registered powers
     */
    public int getPowerCount() {
        return registeredPowers.size();
    }

    /**
     * Activate a power for a player
     * @param player The player
     * @param powerId The ID of the power to activate
     * @return true if power was activated successfully
     */
    public boolean activatePower(Player player, String powerId) {
        IPower power = getPower(powerId);
        if (power == null) {
            plugin.getLogger().warning("Attempted to activate non-existent power: " + powerId);
            return false;
        }

        PlayerPowerData data = playerDataManager.getOrCreatePlayerData(player);
        data.setCurrentPower(power);
        
        // Save to disk
        playerDataManager.savePlayerData(player);

        player.sendMessage("§a✓ Power activated: §b" + power.getDisplayName());
        return true;
    }
    
    /**
     * Assign a random power to a player
     * @param player The player
     * @return true if power was assigned successfully
     */
    public boolean assignRandomPower(Player player) {
        if (registeredPowers.isEmpty()) {
            player.sendMessage("§c✗ No powers available");
            return false;
        }
        
        List<IPower> powerList = new ArrayList<>(registeredPowers.values());
        IPower randomPower = powerList.get(new Random().nextInt(powerList.size()));
        
        return activatePower(player, randomPower.getId());
    }
    
    /**
     * Load player's saved power on join
     * @param player The player
     */
    public void loadPlayerPower(Player player) {
        String savedPowerId = playerDataManager.loadPlayerData(player);
        
        if (savedPowerId != null) {
            IPower power = getPower(savedPowerId);
            if (power != null) {
                PlayerPowerData data = playerDataManager.getOrCreatePlayerData(player);
                data.setCurrentPower(power);
                player.sendMessage("§a✓ Your power has been restored: §b" + power.getDisplayName());
            }
        }
    }

    /**
     * Deactivate the player's current power
     * @param player The player
     */
    public void deactivatePower(Player player) {
        PlayerPowerData data = playerDataManager.getPlayerData(player);
        if (data != null) {
            data.setCurrentPower(null);
            playerDataManager.savePlayerData(player);
            player.sendMessage("§c✗ Power deactivated");
        }
    }

    /**
     * Get the player's currently active power
     * @param player The player
     * @return The active power, or null if none
     */
    public IPower getPlayerActivePower(Player player) {
        PlayerPowerData data = playerDataManager.getPlayerData(player);
        if (data == null) {
            return null;
        }
        return data.getCurrentPower();
    }

    /**
     * Get player power data
     * @param player The player
     * @return The player's power data
     */
    public PlayerPowerData getPlayerData(Player player) {
        return playerDataManager.getOrCreatePlayerData(player);
    }

    /**
     * Get the cooldown manager
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * Get the player data manager
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * Cleanup and shutdown
     */
    public void shutdown() {
        // Save all player data before shutdown
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playerDataManager.savePlayerData(player);
        }
        
        // Cleanup all powers
        for (IPower power : registeredPowers.values()) {
            power.cleanup();
        }
        registeredPowers.clear();

        // Cleanup player data
        playerDataManager.clearAll();

        // Cleanup cooldowns
        cooldownManager.clearAll();

        plugin.getLogger().info("PowerManager shutdown complete");
    }
}


