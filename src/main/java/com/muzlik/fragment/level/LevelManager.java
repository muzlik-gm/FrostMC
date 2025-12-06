package com.muzlik.fragment.level;

import com.muzlik.fragment.FragmentDefinition;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.FrostSMPPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Fragment-specific leveling and XP.
 * Each Fragment has independent progression.
 * 
 * Fragment Level now affects:
 * - Cooldown reduction (up to 25% at max level)
 * - Mana cost reduction (up to 20% at max level)
 * - Small damage bonus (up to 15% at max level)
 * 
 * Max level is based on Fragment's base rank:
 * - Base Rank 2-3: Max Level 5
 * - Base Rank 4-5: Max Level 8
 * - Base Rank 6-7: Max Level 10
 * - Base Rank 8+:  Max Level 12
 */
public class LevelManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<FragmentType, FragmentLevelData>> playerLevelData;
    
    // Default fallback max level (if Fragment not registered)
    private static final int DEFAULT_MAX_LEVEL = 5;
    
    // XP requirements - MUCH LOWER than before
    private static final double BASE_XP = 50.0; // Was 100, now 50

    public LevelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerLevelData = new ConcurrentHashMap<>();
    }

    /**
     * Get max level for a specific Fragment type
     */
    public int getMaxLevel(FragmentType type) {
        if (plugin instanceof FrostSMPPlugin) {
            FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
            FragmentManager fragmentManager = frostPlugin.getFragmentManager();
            if (fragmentManager != null) {
                FragmentDefinition def = fragmentManager.getFragment(type);
                if (def != null) {
                    return def.getMaxLevel();
                }
            }
        }
        return DEFAULT_MAX_LEVEL;
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
     * Formula: BASE_XP * level * 1.2 (mild scaling)
     */
    public double getXPForNextLevel(Player player, FragmentType type) {
        int currentLevel = getLevel(player, type);
        int maxLevel = getMaxLevel(type);
        
        if (currentLevel >= maxLevel) {
            return 0; // Already max level
        }
        
        // Simple, achievable XP curve: 50, 72, 103, 149, 215, etc.
        return BASE_XP * Math.pow(1.2, currentLevel);
    }

    /**
     * Award XP to a Fragment
     */
    public void awardXP(Player player, FragmentType type, double amount) {
        int maxLevel = getMaxLevel(type);
        FragmentLevelData data = getLevelData(player, type);
        
        // Don't award XP if already at max level
        if (data.getLevel() >= maxLevel) {
            return;
        }
        
        double currentXP = data.getXp();
        double newXP = currentXP + amount;
        
        data.setXp(newXP);
        
        // Check for level up
        checkLevelUp(player, type, data);
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
        int maxLevel = getMaxLevel(type);
        data.setLevel(Math.min(level, maxLevel));
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
        return getLevel(player, type) >= getMaxLevel(type);
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
        int maxLevel = getMaxLevel(type);
        
        if (currentLevel >= maxLevel) {
            return; // Already max level
        }
        
        double xpRequired = BASE_XP * Math.pow(1.2, currentLevel);
        
        if (currentXP >= xpRequired) {
            // Level up!
            data.setLevel(currentLevel + 1);
            data.setXp(currentXP - xpRequired); // Carry over excess XP
            
            triggerLevelUpNotification(player, type, currentLevel + 1, maxLevel);
            
            // Check for another level up (in case of large XP gain)
            checkLevelUp(player, type, data);
        }
    }

    /**
     * Trigger level-up notification with bonuses shown
     */
    private void triggerLevelUpNotification(Player player, FragmentType type, int newLevel, int maxLevel) {
        // Calculate bonuses at this level
        double cooldownReduction = getCooldownReduction(newLevel, maxLevel) * 100;
        double manaCostReduction = getManaCostReduction(newLevel, maxLevel) * 100;
        double damageBonus = getDamageBonus(newLevel, maxLevel) * 100;
        
        player.sendMessage("");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l   ⬆ FRAGMENT LEVEL UP!");
        player.sendMessage("");
        player.sendMessage("§e" + type.getDisplayName() + " §7→ §bLevel " + newLevel + "/" + maxLevel);
        player.sendMessage("");
        player.sendMessage("§7Bonuses:");
        player.sendMessage("  §b⏱ §fCooldown: §a-" + String.format("%.0f", cooldownReduction) + "%");
        player.sendMessage("  §b⚡ §fMana Cost: §a-" + String.format("%.0f", manaCostReduction) + "%");
        player.sendMessage("  §b⚔ §fDamage: §a+" + String.format("%.0f", damageBonus) + "%");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    /**
     * Get cooldown reduction multiplier based on level (0 to 0.25 = 0% to 25%)
     */
    public double getCooldownReduction(int level, int maxLevel) {
        if (level <= 1 || maxLevel <= 1) return 0.0;
        // Linear scaling: Level 1 = 0%, Max Level = 25%
        return 0.25 * ((double)(level - 1) / (maxLevel - 1));
    }
    
    /**
     * Get cooldown reduction for a player's Fragment
     */
    public double getCooldownReduction(Player player, FragmentType type) {
        int level = getLevel(player, type);
        int maxLevel = getMaxLevel(type);
        return getCooldownReduction(level, maxLevel);
    }

    /**
     * Get mana cost reduction multiplier based on level (0 to 0.20 = 0% to 20%)
     */
    public double getManaCostReduction(int level, int maxLevel) {
        if (level <= 1 || maxLevel <= 1) return 0.0;
        // Linear scaling: Level 1 = 0%, Max Level = 20%
        return 0.20 * ((double)(level - 1) / (maxLevel - 1));
    }
    
    /**
     * Get mana cost reduction for a player's Fragment
     */
    public double getManaCostReduction(Player player, FragmentType type) {
        int level = getLevel(player, type);
        int maxLevel = getMaxLevel(type);
        return getManaCostReduction(level, maxLevel);
    }

    /**
     * Get damage bonus multiplier based on level (0 to 0.15 = 0% to 15%)
     */
    public double getDamageBonus(int level, int maxLevel) {
        if (level <= 1 || maxLevel <= 1) return 0.0;
        // Linear scaling: Level 1 = 0%, Max Level = 15%
        return 0.15 * ((double)(level - 1) / (maxLevel - 1));
    }
    
    /**
     * Get damage bonus for a player's Fragment
     */
    public double getDamageBonus(Player player, FragmentType type) {
        int level = getLevel(player, type);
        int maxLevel = getMaxLevel(type);
        return getDamageBonus(level, maxLevel);
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
