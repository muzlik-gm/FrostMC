package com.muzlik.cooldown;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cooldowns for abilities per player
 */
public class CooldownManager {

    // Map of player UUID -> ability ID -> cooldown end time
    private final Map<String, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Start a cooldown for an ability
     * @param player The player using the ability
     * @param abilityId The ability's unique ID
     * @param cooldownMs The cooldown duration in milliseconds
     */
    public void startCooldown(Player player, String abilityId, long cooldownMs) {
        String playerUUID = player.getUniqueId().toString();
        long endTime = System.currentTimeMillis() + cooldownMs;
        
        cooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                .put(abilityId, endTime);
    }

    /**
     * Check if an ability is on cooldown
     * @param player The player using the ability
     * @param abilityId The ability's unique ID
     * @return true if the ability is on cooldown
     */
    public boolean isOnCooldown(Player player, String abilityId) {
        String playerUUID = player.getUniqueId().toString();
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        
        if (playerCooldowns == null) {
            return false;
        }
        
        Long endTime = playerCooldowns.get(abilityId);
        if (endTime == null) {
            return false;
        }
        
        boolean onCooldown = System.currentTimeMillis() < endTime;
        if (!onCooldown) {
            // Cooldown expired, remove it
            playerCooldowns.remove(abilityId);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerUUID);
            }
        }
        
        return onCooldown;
    }

    /**
     * Get remaining cooldown time in milliseconds
     * @param player The player using the ability
     * @param abilityId The ability's unique ID
     * @return Remaining cooldown in milliseconds, or 0 if not on cooldown
     */
    public long getRemainingCooldown(Player player, String abilityId) {
        String playerUUID = player.getUniqueId().toString();
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        
        if (playerCooldowns == null) {
            return 0;
        }
        
        Long endTime = playerCooldowns.get(abilityId);
        if (endTime == null) {
            return 0;
        }
        
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Get remaining cooldown time in seconds
     * @param player The player using the ability
     * @param abilityId The ability's unique ID
     * @return Remaining cooldown in seconds, or 0 if not on cooldown
     */
    public double getRemainingCooldownSeconds(Player player, String abilityId) {
        return getRemainingCooldown(player, abilityId) / 1000.0;
    }

    /**
     * Reset cooldown for an ability
     * @param player The player
     * @param abilityId The ability's unique ID
     */
    public void resetCooldown(Player player, String abilityId) {
        String playerUUID = player.getUniqueId().toString();
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        
        if (playerCooldowns != null) {
            playerCooldowns.remove(abilityId);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerUUID);
            }
        }
    }

    /**
     * Reset all cooldowns for a player
     * @param player The player
     */
    public void resetAllCooldowns(Player player) {
        String playerUUID = player.getUniqueId().toString();
        cooldowns.remove(playerUUID);
    }

    /**
     * Cleanup player data (call when player leaves server)
     * @param player The player
     */
    public void cleanupPlayer(Player player) {
        String playerUUID = player.getUniqueId().toString();
        cooldowns.remove(playerUUID);
    }

    /**
     * Clear all cooldowns (emergency cleanup)
     */
    public void clearAll() {
        cooldowns.clear();
    }
}


