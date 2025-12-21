package com.muzlik.fragment.ability.executors.time;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents a snapshot of a player's state at a specific moment in time.
 * Used by Temporal Rewind to restore player state from the past.
 */
public class PlayerState {
    private final Location location;
    private final double health;
    private final long timestamp;
    
    /**
     * Create a snapshot of the player's current state
     * @param player The player to snapshot
     */
    public PlayerState(Player player) {
        this.location = player.getLocation().clone();
        this.health = player.getHealth();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the stored location
     * @return The location snapshot
     */
    public Location getLocation() {
        return location.clone();
    }
    
    /**
     * Get the stored health value
     * @return The health snapshot
     */
    public double getHealth() {
        return health;
    }
    
    /**
     * Get the timestamp when this state was recorded
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
}
