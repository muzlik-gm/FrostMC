package com.muzlik.fragment.ability;

import org.bukkit.entity.Player;

/**
 * Interface for rank-locked passive abilities.
 * Passive abilities are automatically active when the Fragment is equipped
 * and the player has reached the required rank.
 */
public interface PassiveAbility {
    /**
     * Get the unique ID of this passive ability
     */
    String getId();

    /**
     * Get the display name of this passive ability
     */
    String getDisplayName();

    /**
     * Get the description of this passive ability
     */
    String getDescription();

    /**
     * Get the rank required to unlock this passive
     */
    int getRequiredRank();

    /**
     * Called when the passive is activated (Fragment equipped and rank met)
     * @param player The player who has this passive
     */
    void onActivate(Player player);

    /**
     * Called when the passive is deactivated (Fragment unequipped or rank lost)
     * @param player The player who had this passive
     */
    void onDeactivate(Player player);

    /**
     * Called periodically while the passive is active (optional)
     * @param player The player who has this passive
     */
    default void onTick(Player player) {
        // Optional implementation
    }
}
