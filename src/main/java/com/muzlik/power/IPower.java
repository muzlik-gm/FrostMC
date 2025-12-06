package com.muzlik.power;

import org.bukkit.entity.Player;
import com.muzlik.power.ability.AbilityManager;

/**
 * Base interface for all powers
 * Each power has 10 tokens with 3 abilities each
 */
public interface IPower {

    /**
     * Get the unique identifier for this power
     */
    String getId();

    /**
     * Get the display name of this power
     */
    String getDisplayName();

    /**
     * Get the power's theme/description
     */
    String getTheme();

    /**
     * Get the ability manager for this power
     */
    AbilityManager getAbilityManager();

    /**
     * Called when a player acquires this power
     */
    void onPowerGrant(Player player);

    /**
     * Called when a player loses this power
     */
    void onPowerRevoke(Player player);

    /**
     * Cleanup resources when power is being unregistered
     */
    void cleanup();
}


