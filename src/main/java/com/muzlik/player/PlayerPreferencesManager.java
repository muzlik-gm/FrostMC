package com.muzlik.player;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player preferences for control schemes and ability toggles
 */
public class PlayerPreferencesManager {
    private final JavaPlugin plugin;
    private final Map<UUID, PlayerPreferences> preferences;
    
    public PlayerPreferencesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.preferences = new ConcurrentHashMap<>();
    }
    
    /**
     * Get player preferences (creates default if not exists)
     */
    public PlayerPreferences getPreferences(Player player) {
        return preferences.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerPreferences());
    }
    
    /**
     * Get control scheme for player
     */
    public ControlScheme getControlScheme(Player player) {
        return getPreferences(player).getControlScheme();
    }
    
    /**
     * Set control scheme for player
     */
    public void setControlScheme(Player player, ControlScheme scheme) {
        getPreferences(player).setControlScheme(scheme);
        player.sendMessage("§a✓ Control scheme changed to: §e" + scheme.getDisplayName());
        
        // Show instructions
        player.sendMessage("§7" + scheme.getDescription());
        for (String instruction : scheme.getInstructions()) {
            player.sendMessage("  " + instruction);
        }
    }
    
    /**
     * Check if player has abilities enabled
     */
    public boolean areAbilitiesEnabled(Player player) {
        return getPreferences(player).areAbilitiesEnabled();
    }
    
    /**
     * Toggle abilities for player
     */
    public void toggleAbilities(Player player) {
        PlayerPreferences prefs = getPreferences(player);
        boolean newState = !prefs.areAbilitiesEnabled();
        prefs.setAbilitiesEnabled(newState);
        
        if (newState) {
            player.sendMessage("§a✓ Fragment abilities §aENABLED");
            player.sendMessage("§7You can now use your Fragment powers");
        } else {
            player.sendMessage("§c✗ Fragment abilities §cDISABLED");
            player.sendMessage("§7Your Fragment powers are locked");
            player.sendMessage("§7Use §e/fragment toggle §7to re-enable");
        }
    }
    
    /**
     * Set abilities enabled state
     */
    public void setAbilitiesEnabled(Player player, boolean enabled) {
        getPreferences(player).setAbilitiesEnabled(enabled);
    }
    
    /**
     * Check if player should see tutorials
     */
    public boolean shouldShowTutorials(Player player) {
        return getPreferences(player).shouldShowTutorials();
    }
    
    /**
     * Set tutorial visibility
     */
    public void setShowTutorials(Player player, boolean show) {
        getPreferences(player).setShowTutorials(show);
    }
    
    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        preferences.remove(player.getUniqueId());
    }
    
    /**
     * Get all preferences (for data persistence)
     */
    public Map<UUID, PlayerPreferences> getAllPreferences() {
        return preferences;
    }
    
    /**
     * Load preferences for player (from data persistence)
     */
    public void loadPreferences(Player player, ControlScheme scheme, boolean abilitiesEnabled, boolean showTutorials) {
        PlayerPreferences prefs = getPreferences(player);
        prefs.setControlScheme(scheme);
        prefs.setAbilitiesEnabled(abilitiesEnabled);
        prefs.setShowTutorials(showTutorials);
    }
    
    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        preferences.clear();
    }
}
