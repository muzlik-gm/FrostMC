package com.muzlik.fragment.ability;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Cooldown API shared by all Fragments.
 * Tracks and enforces ability cooldowns.
 */
public class CooldownAPI {
    private final Map<UUID, Map<String, Long>> playerCooldowns;

    public CooldownAPI() {
        this.playerCooldowns = new ConcurrentHashMap<>();
    }

    /**
     * Set a cooldown for a player and ability
     * @param player The player
     * @param abilityId The ability ID
     * @param cooldownMillis The cooldown duration in milliseconds
     */
    public void setCooldown(Player player, String abilityId, long cooldownMillis) {
        long cooldownEnd = System.currentTimeMillis() + cooldownMillis;
        
        Map<String, Long> cooldowns = playerCooldowns.computeIfAbsent(
            player.getUniqueId(),
            uuid -> new ConcurrentHashMap<>()
        );
        
        cooldowns.put(abilityId, cooldownEnd);
    }

    /**
     * Check if an ability is on cooldown
     * @param player The player
     * @param abilityId The ability ID
     * @return true if on cooldown, false otherwise
     */
    public boolean isOnCooldown(Player player, String abilityId) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return false;
        }
        
        Long cooldownEnd = cooldowns.get(abilityId);
        if (cooldownEnd == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= cooldownEnd) {
            // Cooldown expired, remove it
            cooldowns.remove(abilityId);
            return false;
        }
        
        return true;
    }

    /**
     * Get remaining cooldown time in milliseconds
     * @param player The player
     * @param abilityId The ability ID
     * @return Remaining cooldown in milliseconds, or 0 if not on cooldown
     */
    public long getRemainingCooldown(Player player, String abilityId) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return 0;
        }
        
        Long cooldownEnd = cooldowns.get(abilityId);
        if (cooldownEnd == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long remaining = cooldownEnd - currentTime;
        
        if (remaining <= 0) {
            cooldowns.remove(abilityId);
            return 0;
        }
        
        return remaining;
    }

    /**
     * Get remaining cooldown time in seconds
     * @param player The player
     * @param abilityId The ability ID
     * @return Remaining cooldown in seconds
     */
    public double getRemainingCooldownSeconds(Player player, String abilityId) {
        return getRemainingCooldown(player, abilityId) / 1000.0;
    }

    /**
     * Clear a specific cooldown
     * @param player The player
     * @param abilityId The ability ID
     */
    public void clearCooldown(Player player, String abilityId) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns != null) {
            cooldowns.remove(abilityId);
        }
    }

    /**
     * Clear all cooldowns for a player
     * @param player The player
     */
    public void clearAllCooldowns(Player player) {
        playerCooldowns.remove(player.getUniqueId());
    }

    /**
     * Get all active cooldowns for a player
     * @param player The player
     * @return Map of ability ID to cooldown end time
     */
    public Map<String, Long> getActiveCooldowns(Player player) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return new ConcurrentHashMap<>();
        }
        
        // Clean up expired cooldowns
        long currentTime = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        
        return new ConcurrentHashMap<>(cooldowns);
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        playerCooldowns.remove(player.getUniqueId());
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        playerCooldowns.clear();
    }
}
