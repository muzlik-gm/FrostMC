package com.muzlik.fragment.ability;

import org.bukkit.entity.Player;

/**
 * Interface for Fragment abilities.
 * Defines the contract for all Fragment ability implementations.
 */
public interface IFragmentAbility {
    
    /**
     * Get the ability's unique identifier
     */
    String getName();
    
    /**
     * Get the ability's display name
     */
    String getDisplayName();
    
    /**
     * Get the ability's description
     */
    String getDescription();
    
    /**
     * Get the mana cost to use this ability
     */
    double getManaCost();
    
    /**
     * Get the cooldown in milliseconds
     */
    long getCooldown();
    
    /**
     * Execute the ability
     * @param player The player using the ability
     * @return true if the ability executed successfully, false otherwise
     */
    boolean execute(Player player);
}
