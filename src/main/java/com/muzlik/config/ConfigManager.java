package com.muzlik.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages plugin configuration.
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Load configuration
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        setDefaults();
        plugin.saveConfig();
        
        plugin.getLogger().info("Configuration loaded successfully");
    }

    /**
     * Set default configuration values
     */
    private void setDefaults() {
        // Ritual configuration
        config.addDefault("rituals.fragment_creation.duration", 600);
        config.addDefault("rituals.fragment_creation.proximity_distance", 5.0);
        config.addDefault("rituals.fragment_changer.duration", 300);
        config.addDefault("rituals.fragment_changer.cooldown", 3600);
        config.addDefault("rituals.fragment_changer.proximity_distance", 5.0);
        
        // Mana configuration
        config.addDefault("mana.base_max", 100.0);
        config.addDefault("mana.rank_bonus", 50.0);
        config.addDefault("mana.base_regen_rate", 1.0);
        config.addDefault("mana.level_regen_bonus", 0.1);
        
        // Leveling configuration
        config.addDefault("leveling.max_level", 50);
        config.addDefault("leveling.prestige_enabled", true);
        config.addDefault("leveling.death_xp_loss_enabled", true);
        
        // UI configuration
        config.addDefault("ui.default_mode", "STANDARD");
        config.addDefault("ui.mana_display", "ACTION_BAR");
        config.addDefault("ui.show_cooldowns", true);
        
        // FX configuration
        config.addDefault("fx.debug_mode", false);
        config.addDefault("fx.particle_density", 1.0);
        config.addDefault("fx.sound_volume", 1.0);
        
        // Fragment configuration
        config.addDefault("fragments.fire.base_rank", 3);
        config.addDefault("fragments.fire.xp_curve", "EXPONENTIAL");
        config.addDefault("fragments.fire.death_xp_penalty", 0.1);
        
        config.addDefault("fragments.water.base_rank", 2);
        config.addDefault("fragments.water.xp_curve", "LINEAR");
        config.addDefault("fragments.water.death_xp_penalty", 0.05);
        
        config.addDefault("fragments.air.base_rank", 3);
        config.addDefault("fragments.air.xp_curve", "LOGARITHMIC");
        config.addDefault("fragments.air.death_xp_penalty", 0.08);
        
        config.addDefault("fragments.earth.base_rank", 2);
        config.addDefault("fragments.earth.xp_curve", "LINEAR");
        config.addDefault("fragments.earth.death_xp_penalty", 0.05);
        
        config.addDefault("fragments.dark.base_rank", 4);
        config.addDefault("fragments.dark.xp_curve", "EXPONENTIAL");
        config.addDefault("fragments.dark.death_xp_penalty", 0.15);
        
        config.addDefault("fragments.light.base_rank", 4);
        config.addDefault("fragments.light.xp_curve", "EXPONENTIAL");
        config.addDefault("fragments.light.death_xp_penalty", 0.12);
        
        config.addDefault("fragments.void.base_rank", 7);
        config.addDefault("fragments.void.xp_curve", "EXPONENTIAL");
        config.addDefault("fragments.void.death_xp_penalty", 0.20);
        
        config.addDefault("fragments.mob.base_rank", 5);
        config.addDefault("fragments.mob.xp_curve", "LINEAR");
        config.addDefault("fragments.mob.death_xp_penalty", 0.10);
        
        config.addDefault("fragments.dragon.base_rank", 8);
        config.addDefault("fragments.dragon.xp_curve", "EXPONENTIAL");
        config.addDefault("fragments.dragon.death_xp_penalty", 0.25);
        
        config.addDefault("fragments.storm.base_rank", 6);
        config.addDefault("fragments.storm.xp_curve", "EXPONENTIAL");
        config.addDefault("fragments.storm.death_xp_penalty", 0.18);
        
        config.options().copyDefaults(true);
    }

    /**
     * Reload configuration
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getLogger().info("Configuration reloaded");
    }

    // Ritual getters
    public int getFragmentCreationDuration() {
        return config.getInt("rituals.fragment_creation.duration", 600);
    }

    public double getFragmentCreationProximity() {
        return config.getDouble("rituals.fragment_creation.proximity_distance", 5.0);
    }

    public int getFragmentChangerDuration() {
        return config.getInt("rituals.fragment_changer.duration", 300);
    }

    public int getFragmentChangerCooldown() {
        return config.getInt("rituals.fragment_changer.cooldown", 3600);
    }

    public double getFragmentChangerProximity() {
        return config.getDouble("rituals.fragment_changer.proximity_distance", 5.0);
    }

    // Mana getters
    public double getBaseMaxMana() {
        return config.getDouble("mana.base_max", 100.0);
    }

    public double getRankManaBonus() {
        return config.getDouble("mana.rank_bonus", 50.0);
    }

    public double getBaseRegenRate() {
        return config.getDouble("mana.base_regen_rate", 1.0);
    }

    public double getLevelRegenBonus() {
        return config.getDouble("mana.level_regen_bonus", 0.1);
    }

    // Leveling getters
    public int getMaxLevel() {
        return config.getInt("leveling.max_level", 50);
    }

    public boolean isPrestigeEnabled() {
        return config.getBoolean("leveling.prestige_enabled", true);
    }

    public boolean isDeathXPLossEnabled() {
        return config.getBoolean("leveling.death_xp_loss_enabled", true);
    }

    // UI getters
    public String getDefaultUIMode() {
        return config.getString("ui.default_mode", "STANDARD");
    }

    public String getManaDisplay() {
        return config.getString("ui.mana_display", "ACTION_BAR");
    }

    public boolean shouldShowCooldowns() {
        return config.getBoolean("ui.show_cooldowns", true);
    }

    // FX getters
    public boolean isFXDebugMode() {
        return config.getBoolean("fx.debug_mode", false);
    }

    public double getParticleDensity() {
        return config.getDouble("fx.particle_density", 1.0);
    }

    public double getSoundVolume() {
        return config.getDouble("fx.sound_volume", 1.0);
    }

    // Fragment getters
    public int getFragmentBaseRank(String fragmentName) {
        return config.getInt("fragments." + fragmentName.toLowerCase() + ".base_rank", 1);
    }

    public String getFragmentXPCurve(String fragmentName) {
        return config.getString("fragments." + fragmentName.toLowerCase() + ".xp_curve", "LINEAR");
    }

    public double getFragmentDeathXPPenalty(String fragmentName) {
        return config.getDouble("fragments." + fragmentName.toLowerCase() + ".death_xp_penalty", 0.1);
    }
}
