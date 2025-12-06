package com.muzlik.power;

import org.bukkit.entity.Player;

/**
 * Base interface for all abilities
 * Each power has 3 abilities: Primary, Secondary, and Tertiary
 */
public interface IAbility {

    /**
     * Get the unique identifier for this ability
     */
    String getId();

    /**
     * Get the display name of this ability
     */
    String getDisplayName();

    /**
     * Get the description of this ability
     */
    String getDescription();

    /**
     * Get the cooldown in milliseconds for this ability
     */
    long getCooldown();

    /**
     * Check if this ability is currently active
     */
    boolean isActive();

    /**
     * Execute the ability for the given player
     * @param player The player executing the ability
     * @return true if ability was successfully executed, false otherwise
     */
    boolean execute(Player player);

    /**
     * Cancel the ability for the given player (if applicable)
     */
    void cancel(Player player);

    /**
     * Cleanup resources when ability is being removed
     */
    void cleanup();
}


