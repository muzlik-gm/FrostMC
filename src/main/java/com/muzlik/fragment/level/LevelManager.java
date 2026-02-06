package com.muzlik.fragment.level;

import com.muzlik.fragment.FragmentDefinition;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.rank.RankManager;
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
 * Fragment Level now affects (ALL CAPPED AT 30%):
 * - Cooldown reduction (up to 30% at max level) - 100s becomes 70s minimum
 * - Mana cost reduction (up to 30% at max level)
 * - Damage bonus (up to 30% at max level)
 * 
 * Max level is based on Fragment's base rank:
 * - Base Rank 2-3: Max Level 5
 * - Base Rank 4-5: Max Level 8
 * - Base Rank 6-7: Max Level 10
 * - Base Rank 8+:  Max Level 12
 * 
 * AUTO RANK-UP: Every 2 levels, fragment rank increases automatically
 */
public class LevelManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<FragmentType, FragmentLevelData>> playerLevelData;
    private RankManager rankManager;
    
    // Default fallback max level (if Fragment not registered)
    private static final int DEFAULT_MAX_LEVEL = 5;
    
    // XP requirements - INCREASED significantly
    private static final double BASE_XP = 500.0; // Was 50, now 500
    
    public LevelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerLevelData = new ConcurrentHashMap<>();
    }
    
    /**
     * Set RankManager reference (called after initialization)
     */
    public void setRankManager(RankManager rankManager) {
        this.rankManager = rankManager;
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
     * Formula: BASE_XP * level^1.5 (steeper scaling)
     */
    public double getXPForNextLevel(Player player, FragmentType type) {
        int currentLevel = getLevel(player, type);
        int maxLevel = getMaxLevel(type);
        
        if (currentLevel >= maxLevel) {
            return 0; // Already max level
        }
        
        // Steeper XP curve: 500, 1414, 2598, 4000, etc.
        return BASE_XP * Math.pow(currentLevel, 1.5);
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
        setLevel(player, type, level, true);
    }
    
    /**
     * Set player's level for a Fragment with sync control
     */
    public void setLevel(Player player, FragmentType type, int level, boolean syncRank) {
        FragmentLevelData data = getLevelData(player, type);
        int maxLevel = getMaxLevel(type);
        int clampedLevel = Math.min(level, maxLevel);
        
        int oldLevel = data.getLevel();
        data.setLevel(clampedLevel);
        
        // CRITICAL FIX: Trigger rank progression when level is set manually (both directions)
        if (syncRank && rankManager != null) {
            // Check if the new level should trigger rank changes
            int currentRank = rankManager.getRank(player, type);
            int expectedRank = calculateExpectedRank(clampedLevel, type);
            
            // Auto-rank up or down to match the level
            int rankDifference = expectedRank - currentRank;
            
            if (rankDifference != 0) {
                // Set rank directly without triggering level sync to prevent circular calls
                rankManager.setRankDirect(player, type, expectedRank);
                player.sendMessage("§6★ " + type.getDisplayName() + " Fragment rank synced to " + expectedRank);
            }
        }
        
        // Notify player about level change
        player.sendMessage("§a⬆ " + type.getDisplayName() + " Fragment level set to §e" + clampedLevel + "§8/§7" + maxLevel);
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
            player.sendMessage(com.muzlik.util.Typography.formatError("Must be max level to prestige"));
            return;
        }
        
        FragmentLevelData data = getLevelData(player, type);
        data.setLevel(1);
        data.setXp(0);
        data.incrementPrestige();
        
        player.sendMessage(com.muzlik.util.Typography.COLOR_ACCENT + "✦ §lPRESTIGE ✦");
        player.sendMessage(com.muzlik.util.Typography.COLOR_SECONDARY + type.getDisplayName() + " Fragment has been prestiged!");
        player.sendMessage(com.muzlik.util.Typography.formatLabel("Prestige Level: ") + com.muzlik.util.Typography.formatValue(String.valueOf(data.getPrestigeLevel())));
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
            player.sendMessage(com.muzlik.util.Typography.formatError("Lost " + String.format("%.0f", penalty) + " XP (" + type.getDisplayName() + ")"));
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
     * Calculate expected rank based on level for a specific fragment type
     * Uses the fragment's specific max level and max rank for calculation
     */
    private int calculateExpectedRank(int level, FragmentType type) {
        int maxLevel = getMaxLevel(type);
        int baseRank = rankManager != null ? rankManager.getBaseRank(type) : 1;
        int maxRank = rankManager != null ? rankManager.getMaxRank(type) : 8;
        
        // Calculate rank based on level progression
        // Formula: baseRank + floor((level - 1) * (maxRank - baseRank) / (maxLevel - 1))
        if (maxLevel <= 1) return baseRank;
        
        int rank = baseRank + ((level - 1) * (maxRank - baseRank)) / (maxLevel - 1);
        return Math.min(rank, maxRank);
    }
    
    /**
     * Calculate expected level based on rank for a specific fragment type
     * Uses the fragment's specific max level and max rank for calculation
     */
    private int calculateExpectedLevel(int rank, FragmentType type) {
        int maxLevel = getMaxLevel(type);
        int baseRank = rankManager != null ? rankManager.getBaseRank(type) : 1;
        int maxRank = rankManager != null ? rankManager.getMaxRank(type) : 8;
        
        // Calculate minimum level for this rank
        // Formula: 1 + floor((rank - baseRank) * (maxLevel - 1) / (maxRank - baseRank))
        if (maxRank <= baseRank) return 1;
        
        int level = 1 + ((rank - baseRank) * (maxLevel - 1)) / (maxRank - baseRank);
        return Math.min(level, maxLevel);
    }
    
    /**
     * Get total XP required to reach a specific level
     */
    public double getXPRequired(int targetLevel) {
        if (targetLevel <= 1) {
            return 0;
        }
        
        double totalXP = 0;
        for (int level = 1; level < targetLevel; level++) {
            totalXP += BASE_XP * Math.pow(level, 1.5);
        }
        return totalXP;
    }
    
    /**
     * Synchronize level to match rank (called by RankManager when rank is set manually)
     */
    public void syncLevelToRank(Player player, FragmentType type, int newRank) {
        FragmentLevelData data = getLevelData(player, type);
        int currentLevel = data.getLevel();
        int expectedLevel = calculateExpectedLevel(newRank, type);
        int maxLevel = getMaxLevel(type);
        
        // Sync level to match rank (both up and down) within max level bounds
        if (expectedLevel != currentLevel && expectedLevel <= maxLevel && expectedLevel >= 1) {
            // Use setLevel with syncRank=false to prevent circular calls
            setLevel(player, type, expectedLevel, false);
            
            // Also set appropriate XP for the new level (total XP accumulated to reach this level)
            double xpForLevel = getXPRequired(expectedLevel);
            data.setXp(xpForLevel);
        }
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
        
        double xpRequired = BASE_XP * Math.pow(currentLevel, 1.5);
        
        if (currentXP >= xpRequired) {
            // Level up!
            int newLevel = currentLevel + 1;
            data.setLevel(newLevel);
            data.setXp(currentXP - xpRequired); // Carry over excess XP
            
            triggerLevelUpNotification(player, type, newLevel, maxLevel);
            
            // AUTO RANK-UP: Every 2 levels, increase rank (silent, shown in rank-up notification)
            if (rankManager != null && newLevel % 2 == 0) {
                if (rankManager.canRankUp(player, type)) {
                    rankManager.rankUp(player, type);
                }
            }
            
            // Check for another level up (in case of large XP gain)
            checkLevelUp(player, type, data);
        }
    }
    private void triggerLevelUpNotification(Player player, FragmentType type, int newLevel, int maxLevel) {
        // Minimal clean notification
        player.sendMessage(
            com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
            com.muzlik.util.Typography.COLOR_SECONDARY + type.getDisplayName() + " " +
            com.muzlik.util.Typography.COLOR_TEXT_DARK + com.muzlik.util.Typography.SYMBOL_ARROW + " " +
            com.muzlik.util.Typography.COLOR_HIGHLIGHT + com.muzlik.util.Typography.toSmallCaps("level") + " " +
            com.muzlik.util.Typography.COLOR_PRIMARY + newLevel
        );
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
    }

    /**
     * Get cooldown reduction multiplier based on level (0 to 0.30 = 0% to 30%)
     * CAPPED AT 30% - Even at max level, 100s cooldown becomes 70s minimum
     */
    public double getCooldownReduction(int level, int maxLevel) {
        if (level <= 1 || maxLevel <= 1) return 0.0;
        // Linear scaling: Level 1 = 0%, Max Level = 30%
        return 0.30 * ((double)(level - 1) / (maxLevel - 1));
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
     * Get mana cost reduction multiplier based on level (0 to 0.30 = 0% to 30%)
     * CAPPED AT 30% - Consistent with cooldown reduction
     */
    public double getManaCostReduction(int level, int maxLevel) {
        if (level <= 1 || maxLevel <= 1) return 0.0;
        // Linear scaling: Level 1 = 0%, Max Level = 30%
        return 0.30 * ((double)(level - 1) / (maxLevel - 1));
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
     * Get damage bonus multiplier based on level (0 to 0.30 = 0% to 30%)
     * CAPPED AT 30% - Consistent with other bonuses
     */
    public double getDamageBonus(int level, int maxLevel) {
        if (level <= 1 || maxLevel <= 1) return 0.0;
        // Linear scaling: Level 1 = 0%, Max Level = 30%
        return 0.30 * ((double)(level - 1) / (maxLevel - 1));
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
