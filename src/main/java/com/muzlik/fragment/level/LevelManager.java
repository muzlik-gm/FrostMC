package com.muzlik.fragment.level;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Fragment-specific leveling and XP.
 * Each Fragment has independent progression.
 */
public class LevelManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<FragmentType, FragmentLevelData>> playerLevelData;
    
    private static final int MAX_LEVEL = 50;
    private static final double BASE_XP = 100.0;

    public LevelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerLevelData = new ConcurrentHashMap<>();
    }

    /**
     * Get player's level for a Fragment
     */
    public int getLevel(Player player, FragmentType type) {
        FragmentLevelData data = getLevelData(player, type);
        return data.getLevel();
    }

    /**
     * Get player's XP for a Fragment
     */
    public double getXP(Player player, FragmentType type) {
        FragmentLevelData data = getLevelData(player, type);
        return data.getXp();
    }

    /**
     * Get XP required for next level
     */
    public double getXPForNextLevel(Player player, FragmentType type) {
        int currentLevel = getLevel(player, type);
        FragmentLevelData data = getLevelData(player, type);
        return data.getXpCurve().calculateXP(currentLevel + 1, BASE_XP);
    }

    /**
     * Award XP to a Fragment
     */
    public void awardXP(Player player, FragmentType type, double amount) {
        FragmentLevelData data = getLevelData(player, type);
        double currentXP = data.getXp();
        double newXP = currentXP + amount;
        
        data.setXp(newXP);
        
        // Check for level up
        checkLevelUp(player, type, data);
        
        player.sendMessage("§a+§b" + String.format("%.0f", amount) + " §aXP §7(" + type.getDisplayName() + ")");
    }
    
    /**
     * Add XP to a Fragment (alias for awardXP)
     */
    public void addXP(Player player, FragmentType type, double amount) {
        awardXP(player, type, amount);
    }

    /**
     * Set player's level for a Fragment
     */
    public void setLevel(Player player, FragmentType type, int level) {
        FragmentLevelData data = getLevelData(player, type);
        data.setLevel(Math.min(level, MAX_LEVEL));
    }

    /**
     * Set player's XP for a Fragment (for data loading)
     */
    public void setXP(Player player, FragmentType type, double xp) {
        FragmentLevelData data = getLevelData(player, type);
        data.setXp(Math.max(0, xp));
    }

    /**
     * Check if player can prestige
     */
    public boolean canPrestige(Player player, FragmentType type) {
        return getLevel(player, type) >= MAX_LEVEL;
    }

    /**
     * Activate prestige for a Fragment
     */
    public void activatePrestige(Player player, FragmentType type) {
        if (!canPrestige(player, type)) {
            player.sendMessage("§c✗ Must be max level to prestige");
            return;
        }
        
        FragmentLevelData data = getLevelData(player, type);
        data.setLevel(1);
        data.setXp(0);
        data.incrementPrestige();
        
        player.sendMessage("§6✦ §lPRESTIGE §6✦");
        player.sendMessage("§e" + type.getDisplayName() + " Fragment has been prestiged!");
        player.sendMessage("§7Prestige Level: §b" + data.getPrestigeLevel());
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Get prestige level for a Fragment
     */
    public int getPrestigeLevel(Player player, FragmentType type) {
        FragmentLevelData data = getLevelData(player, type);
        return data.getPrestigeLevel();
    }

    /**
     * Apply death XP penalty
     */
    public void applyDeathPenalty(Player player, FragmentType type, double penaltyPercent) {
        FragmentLevelData data = getLevelData(player, type);
        double currentXP = data.getXp();
        double penalty = currentXP * penaltyPercent;
        double newXP = Math.max(0, currentXP - penalty);
        
        data.setXp(newXP);
        
        if (penalty > 0) {
            player.sendMessage("§c✗ Lost §4" + String.format("%.0f", penalty) + " §cXP §7(" + type.getDisplayName() + ")");
        }
    }

    /**
     * Set XP curve for a Fragment
     */
    public void setXPCurve(Player player, FragmentType type, XPCurve curve) {
        FragmentLevelData data = getLevelData(player, type);
        data.setXpCurve(curve);
    }

    /**
     * Check and handle level up
     */
    private void checkLevelUp(Player player, FragmentType type, FragmentLevelData data) {
        double currentXP = data.getXp();
        int currentLevel = data.getLevel();
        
        if (currentLevel >= MAX_LEVEL) {
            return; // Already max level
        }
        
        double xpRequired = data.getXpCurve().calculateXP(currentLevel + 1, BASE_XP);
        
        if (currentXP >= xpRequired) {
            // Level up!
            data.setLevel(currentLevel + 1);
            data.setXp(currentXP - xpRequired); // Carry over excess XP
            
            triggerLevelUpNotification(player, type, currentLevel + 1);
            
            // Check for another level up (in case of large XP gain)
            checkLevelUp(player, type, data);
        }
    }

    /**
     * Trigger level-up notification
     */
    private void triggerLevelUpNotification(Player player, FragmentType type, int newLevel) {
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l                    LEVEL UP!");
        player.sendMessage("");
        player.sendMessage("§e" + type.getDisplayName() + " Fragment §7→ §bLevel " + newLevel);
        player.sendMessage("");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    /**
     * Get or create level data for player and Fragment
     */
    private FragmentLevelData getLevelData(Player player, FragmentType type) {
        Map<FragmentType, FragmentLevelData> fragmentData = playerLevelData.computeIfAbsent(
            player.getUniqueId(), 
            uuid -> new ConcurrentHashMap<>()
        );
        
        return fragmentData.computeIfAbsent(type, t -> new FragmentLevelData(XPCurve.LINEAR));
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        playerLevelData.remove(player.getUniqueId());
    }

    /**
     * Get all level data for a player
     */
    public Map<FragmentType, FragmentLevelData> getPlayerLevelData(Player player) {
        return playerLevelData.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>());
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        playerLevelData.clear();
    }
}
