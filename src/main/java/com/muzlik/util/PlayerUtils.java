package com.muzlik.util;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Utility class for player-related operations
 */
public class PlayerUtils {

    /**
     * Get player's direction vector (where they're looking)
     * @param player The player
     * @return The normalized direction vector
     */
    public static Vector getPlayerDirection(Player player) {
        return player.getLocation().getDirection().normalize();
    }

    /**
     * Get player's forward vector (horizontal direction only)
     * @param player The player
     * @return The normalized horizontal direction vector
     */
    public static Vector getPlayerHorizontalDirection(Player player) {
        Vector direction = player.getLocation().getDirection();
        direction.setY(0); // Remove vertical component
        return direction.normalize();
    }

    /**
     * Check if player has line of sight to location
     * @param player The player
     * @param location The location to check
     * @return true if player has line of sight
     */
    public static boolean hasLineOfSight(Player player, Location location) {
        return player.hasLineOfSight(location);
    }
}


