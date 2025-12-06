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
 * Per-rank ability configuration system.
 * 
 * Loads and manages ability parameters for each rank from YAML configuration.
 * Falls back to scaling formulas when rank not explicitly configured.
 * Supports hot-reload for configuration changes.
 * 
 * Per SYSTEM.md specification:
 * - Create AbilityConfig class for per-rank parameters
 * - Add YAML loading for per-rank configuration tables
 * - Implement fallback to scaling formulas when rank not configured
 * - Add hot-reload support for configuration changes
 * - Add validation and error handling for invalid configs
 * 
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5
 */
public class AbilityConfig {
    private final JavaPlugin plugin;
    private final String abilityId;
    private final int baseRank;
    
    // Per-rank parameters
    private final Map<Integer, RankParameters> rankConfigs;
    
    // Base parameters (used for scaling fallback)
    private double baseDamage;
    private double baseMana;
    private double baseCooldown;
    private double baseRange;
    private double baseDuration;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     * @param abilityId Ability identifier
     * @param baseRank Base rank for the ability
     */
    public AbilityConfig(JavaPlugin plugin, String abilityId, int baseRank) {
        this.plugin = plugin;
        this.abilityId = abilityId;
        this.baseRank = baseRank;
        this.rankConfigs = new HashMap<>();
        
        // Default base parameters
        this.baseDamage = 5.0;
        this.baseMana = 20.0;
        this.baseCooldown = 10.0;
        this.baseRange = 10.0;
        this.baseDuration = 5.0;
    }
    
    /**
     * Load configuration from YAML
     * 
     * @param config Configuration section for this ability
     * @return true if loaded successfully
     */
    public boolean loadFromConfig(ConfigurationSection config) {
        if (config == null) {
            plugin.getLogger().warning("No configuration found for ability: " + abilityId);
            return false;
        }
        
        try {
            // Load base parameters
            if (config.contains("base")) {
                ConfigurationSection baseSection = config.getConfigurationSection("base");
                if (baseSection != null) {
                    baseDamage = baseSection.getDouble("damage", baseDamage);
                    baseMana = baseSection.getDouble("mana", baseMana);
                    baseCooldown = baseSection.getDouble("cooldown", baseCooldown);
                    baseRange = baseSection.getDouble("range", baseRange);
                    baseDuration = baseSection.getDouble("duration", baseDuration);
                }
            }
            
            // Load per-rank configurations
            if (config.contains("ranks")) {
                ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
                if (ranksSection != null) {
                    for (String rankKey : ranksSection.getKeys(false)) {
                        try {
                            int rank = Integer.parseInt(rankKey);
                            ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankKey);
                            
                            if (rankSection != null) {
                                RankParameters params = new RankParameters();
                                params.damage = rankSection.getDouble("damage", -1);
                                params.mana = rankSection.getDouble("mana", -1);
                                params.cooldown = rankSection.getDouble("cooldown", -1);
                                params.range = rankSection.getDouble("range", -1);
                                params.duration = rankSection.getDouble("duration", -1);
                                
                                rankConfigs.put(rank, params);
                            }
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid rank key in config: " + rankKey);
                        }
                    }
                }
            }
            
            plugin.getLogger().log(Level.FINE, 
                "Loaded configuration for ability " + abilityId + " with " + 
                rankConfigs.size() + " rank overrides");
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, 
                "Failed to load configuration for ability: " + abilityId, e);
            return false;
        }
    }
    
    /**
     * Get damage for a specific rank
     * Uses configured value if available, otherwise uses scaling formula
     * 
     * @param rank Current rank
     * @return Damage value
     */
    public double getDamage(int rank) {
        RankParameters params = rankConfigs.get(rank);
        if (params != null && params.damage >= 0) {
            return params.damage;
        }
        
        // Fallback to scaling formula
        return AbilityScaling.scaleDamage(baseDamage, rank, baseRank);
    }
    
    /**
     * Get mana cost for a specific rank
     */
    public double getMana(int rank) {
        RankParameters params = rankConfigs.get(rank);
        if (params != null && params.mana >= 0) {
            return params.mana;
        }
        
        return AbilityScaling.scaleMana(baseMana, rank, baseRank);
    }
    
    /**
     * Get cooldown for a specific rank
     */
    public double getCooldown(int rank) {
        RankParameters params = rankConfigs.get(rank);
        if (params != null && params.cooldown >= 0) {
            return params.cooldown;
        }
        
        // Cooldown doesn't scale by default
        return baseCooldown;
    }
    
    /**
     * Get range for a specific rank
     */
    public double getRange(int rank) {
        RankParameters params = rankConfigs.get(rank);
        if (params != null && params.range >= 0) {
            return params.range;
        }
        
        return AbilityScaling.scaleRange(baseRange, rank, baseRank);
    }
    
    /**
     * Get duration for a specific rank
     */
    public double getDuration(int rank) {
        RankParameters params = rankConfigs.get(rank);
        if (params != null && params.duration >= 0) {
            return params.duration;
        }
        
        return AbilityScaling.scaleDuration(baseDuration, rank, baseRank);
    }
    
    /**
     * Set base parameters
     */
    public void setBaseParameters(double damage, double mana, double cooldown, double range, double duration) {
        this.baseDamage = damage;
        this.baseMana = mana;
        this.baseCooldown = cooldown;
        this.baseRange = range;
        this.baseDuration = duration;
    }
    
    /**
     * Add rank-specific override
     */
    public void addRankOverride(int rank, double damage, double mana, double cooldown, 
                               double range, double duration) {
        RankParameters params = new RankParameters();
        params.damage = damage;
        params.mana = mana;
        params.cooldown = cooldown;
        params.range = range;
        params.duration = duration;
        
        rankConfigs.put(rank, params);
    }
    
    /**
     * Check if rank has explicit configuration
     */
    public boolean hasRankConfig(int rank) {
        return rankConfigs.containsKey(rank);
    }
    
    /**
     * Validate configuration
     * 
     * @return true if configuration is valid
     */
    public boolean validate() {
        // Check base parameters are positive
        if (baseDamage < 0 || baseMana < 0 || baseCooldown < 0 || 
            baseRange < 0 || baseDuration < 0) {
            plugin.getLogger().warning("Invalid base parameters for ability: " + abilityId);
            return false;
        }
        
        // Check rank configs are valid
        for (Map.Entry<Integer, RankParameters> entry : rankConfigs.entrySet()) {
            int rank = entry.getKey();
            RankParameters params = entry.getValue();
            
            if (rank < 1 || rank > 10) {
                plugin.getLogger().warning("Invalid rank " + rank + " for ability: " + abilityId);
                return false;
            }
            
            // Negative values are allowed (means "use scaling formula")
            // But if specified, should be reasonable
            if (params.damage > 1000 || params.mana > 1000 || 
                params.cooldown > 300 || params.range > 100 || params.duration > 300) {
                plugin.getLogger().warning("Unreasonable parameters at rank " + rank + 
                    " for ability: " + abilityId);
                return false;
            }
        }
        
        return true;
    }
    
    // Getters
    
    public String getAbilityId() {
        return abilityId;
    }
    
    public int getBaseRank() {
        return baseRank;
    }
    
    public double getBaseDamage() {
        return baseDamage;
    }
    
    public double getBaseMana() {
        return baseMana;
    }
    
    public double getBaseCooldown() {
        return baseCooldown;
    }
    
    public double getBaseRange() {
        return baseRange;
    }
    
    public double getBaseDuration() {
        return baseDuration;
    }
    
    /**
     * Rank-specific parameters
     */
    private static class RankParameters {
        double damage = -1;    // -1 means "use scaling formula"
        double mana = -1;
        double cooldown = -1;
        double range = -1;
        double duration = -1;
    }
}
