package com.muzlik.player;

import org.bukkit.entity.Player;
import com.muzlik.power.IPower;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages powers for individual players
 */
public class PlayerPowerData {

    private final Player player;
    private IPower currentPower; // Only one active power at a time

    /**
     * Create player power data
     * @param player The player
     */
    public PlayerPowerData(Player player) {
        this.player = player;
        this.currentPower = null;
    }

    /**
     * Set the player's current active power
     * @param power The power to activate, or null to deactivate
     */
    public void setCurrentPower(IPower power) {
        // Revoke old power if exists
        if (currentPower != null) {
            currentPower.onPowerRevoke(player);
        }

        this.currentPower = power;

        // Grant new power if exists
        if (power != null) {
            power.onPowerGrant(player);
        }
    }

    /**
     * Get the player's current active power
     */
    public IPower getCurrentPower() {
        return currentPower;
    }

    /**
     * Check if player has a power active
     */
    public boolean hasPowerActive() {
        return currentPower != null;
    }

    /**
     * Get the player
     */
    public Player getPlayer() {
        return player;
    }
}


