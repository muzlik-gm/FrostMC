package com.muzlik.fragment.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manager for ability configurations.
 * 
 * Handles loading, caching, and hot-reloading of ability configurations.
 * 
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5
 */
public class AbilityConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, AbilityConfig> configs;
    private File configFile;
    private FileConfiguration config;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     */
    public AbilityConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
    }
    
    /**
     * Load all ability configurations
     */
    public void loadConfigurations() {
        // Create config file if it doesn't exist
        configFile = new File(plugin.getDataFolder(), "ability_configs.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("ability_configs.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Clear existing configs
        configs.clear();
        
        // Load each ability configuration
        ConfigurationSection abilitiesSection = config.getConfigurationSection("abilities");
        
        if (abilitiesSection == null) {
            plugin.getLogger().warning("No abilities section found in ability_configs.yml");
            return;
        }
        
        int loaded = 0;
        for (String abilityId : abilitiesSection.getKeys(false)) {
            ConfigurationSection abilitySection = abilitiesSection.getConfigurationSection(abilityId);
            
            if (abilitySection != null) {
                int baseRank = abilitySection.getInt("base_rank", 1);
                AbilityConfig abilityConfig = new AbilityConfig(plugin, abilityId, baseRank);
                
                if (abilityConfig.loadFromConfig(abilitySection)) {
                    if (abilityConfig.validate()) {
                        configs.put(abilityId, abilityConfig);
                        loaded++;
                    } else {
                        plugin.getLogger().warning("Invalid configuration for ability: " + abilityId);
                    }
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + loaded + " ability configurations");
    }
    
    /**
     * Reload all configurations
     */
    public void reloadConfigurations() {
        plugin.getLogger().info("Reloading ability configurations...");
        loadConfigurations();
        plugin.getLogger().info("Ability configurations reloaded");
    }
    
    /**
     * Get configuration for an ability
     * 
     * @param abilityId Ability identifier
     * @return Ability configuration, or null if not found
     */
    public AbilityConfig getConfig(String abilityId) {
        return configs.get(abilityId);
    }
    
    /**
     * Check if ability has configuration
     * 
     * @param abilityId Ability identifier
     * @return true if configuration exists
     */
    public boolean hasConfig(String abilityId) {
        return configs.containsKey(abilityId);
    }
    
    /**
     * Get the number of loaded configurations
     * 
     * @return Number of configurations
     */
    public int getConfigCount() {
        return configs.size();
    }
    
    /**
     * Save default configuration file
     */
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                
                // Create default configuration
                FileConfiguration defaultConfig = new YamlConfiguration();
                
                // Add example ability configuration
                defaultConfig.set("abilities.fire_flame_burst.base_rank", 1);
                defaultConfig.set("abilities.fire_flame_burst.base.damage", 8.0);
                defaultConfig.set("abilities.fire_flame_burst.base.mana", 15.0);
                defaultConfig.set("abilities.fire_flame_burst.base.cooldown", 5.0);
                defaultConfig.set("abilities.fire_flame_burst.base.range", 12.0);
                defaultConfig.set("abilities.fire_flame_burst.base.duration", 0.5);
                
                // Add rank 5 override example
                defaultConfig.set("abilities.fire_flame_burst.ranks.5.damage", 15.0);
                defaultConfig.set("abilities.fire_flame_burst.ranks.5.mana", 20.0);
                
                defaultConfig.save(configFile);
                
                plugin.getLogger().info("Created default ability_configs.yml");
                
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create default config", e);
            }
        }
    }
}
