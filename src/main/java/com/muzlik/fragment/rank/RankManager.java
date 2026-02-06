package com.muzlik.fragment.rank;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.PassiveAbility;
import com.muzlik.fragment.level.LevelManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Fragment ranks for players.
 * Handles rank progression and rank-locked passives.
 */
public class RankManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<FragmentType, FragmentRankData>> playerRankData;
    private final Map<FragmentType, Integer> baseRanks;
    private final Map<FragmentType, Map<Integer, PassiveAbility>> rankPassives;
    private LevelManager levelManager;

    public RankManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerRankData = new ConcurrentHashMap<>();
        this.baseRanks = new ConcurrentHashMap<>();
        this.rankPassives = new ConcurrentHashMap<>();
    }
    
    /**
     * Set LevelManager reference (called after initialization)
     */
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    /**
     * Get player's current rank for a Fragment
     */
    public int getRank(Player player, FragmentType type) {
        FragmentRankData data = getRankData(player, type);
        return data.getCurrentRank();
    }

    /**
     * Get base rank for a Fragment
     */
    public int getBaseRank(FragmentType type) {
        return baseRanks.getOrDefault(type, 1);
    }

    /**
     * Get maximum rank for a Fragment (Base Rank + 2)
     */
    public int getMaxRank(FragmentType type) {
        return getBaseRank(type) + 2;
    }

    /**
     * Set base rank for a Fragment
     */
    public void setBaseRank(FragmentType type, int baseRank) {
        if (baseRank < 1 || baseRank > 10) {
            throw new IllegalArgumentException("Base rank must be between 1 and 10");
        }
        baseRanks.put(type, baseRank);
    }

    /**
     * Set player's rank for a Fragment
     */
    public void setRank(Player player, FragmentType type, int rank) {
        setRank(player, type, rank, true);
    }
    
    /**
     * Set player's rank for a Fragment without triggering level sync (used by LevelManager)
     */
    public void setRankDirect(Player player, FragmentType type, int rank) {
        setRank(player, type, rank, false);
    }
    
    /**
     * Set player's rank for a Fragment with sync control
     */
    private void setRank(Player player, FragmentType type, int rank, boolean syncLevel) {
        int maxRank = getMaxRank(type);
        if (rank > maxRank) {
            rank = maxRank;
        }
        
        FragmentRankData data = getRankData(player, type);
        int oldRank = data.getCurrentRank();
        data.setCurrentRank(rank);
        
        // CRITICAL FIX: Trigger level synchronization when syncLevel is true AND rank actually changed
        if (syncLevel && rank != oldRank && levelManager != null) {
            levelManager.syncLevelToRank(player, type, rank);
            player.sendMessage("§6★ " + type.getDisplayName() + " Fragment level synced to match rank " + rank);
        }
        
        // Handle passive abilities (only when ranking up)
        if (rank > oldRank) {
            activateRankPassive(player, type, rank);
        }
        
        // Notify player about rank change
        if (rank != oldRank) {
            player.sendMessage("§a⬆ " + type.getDisplayName() + " Fragment rank set to §6" + rank + "§8/§7" + maxRank);
        }
    }

    /**
     * Check if player can rank up
     */
    public boolean canRankUp(Player player, FragmentType type) {
        int currentRank = getRank(player, type);
        int maxRank = getMaxRank(type);
        return currentRank < maxRank;
    }

    /**
     * Rank up a Fragment
     */
    public void rankUp(Player player, FragmentType type) {
        if (!canRankUp(player, type)) {
            player.sendMessage("§c✗ Already at maximum rank");
            return;
        }
        
        int currentRank = getRank(player, type);
        int newRank = currentRank + 1;
        
        setRank(player, type, newRank);
        triggerRankUpNotification(player, type, newRank);
    }

    /**
     * Get rank passive for a Fragment at a specific rank
     */
    public PassiveAbility getRankPassive(FragmentType type, int rank) {
        Map<Integer, PassiveAbility> passives = rankPassives.get(type);
        if (passives == null) {
            return null;
        }
        return passives.get(rank);
    }

    /**
     * Register a rank passive for a Fragment
     */
    public void registerRankPassive(FragmentType type, int rank, PassiveAbility passive) {
        rankPassives.computeIfAbsent(type, t -> new ConcurrentHashMap<>())
            .put(rank, passive);
    }

    /**
     * Activate rank passive for a player
     */
    private void activateRankPassive(Player player, FragmentType type, int rank) {
        PassiveAbility passive = getRankPassive(type, rank);
        if (passive != null) {
            passive.onActivate(player);
            player.sendMessage("§a✓ Unlocked passive: §b" + passive.getDisplayName());
        }
    }

    /**
     * Deactivate all passives for a Fragment
     */
    public void deactivatePassives(Player player, FragmentType type) {
        int currentRank = getRank(player, type);
        Map<Integer, PassiveAbility> passives = rankPassives.get(type);
        
        if (passives != null) {
            for (int rank = 1; rank <= currentRank; rank++) {
                PassiveAbility passive = passives.get(rank);
                if (passive != null) {
                    passive.onDeactivate(player);
                }
            }
        }
    }

    /**
     * Activate all passives for a Fragment
     */
    public void activatePassives(Player player, FragmentType type) {
        int currentRank = getRank(player, type);
        Map<Integer, PassiveAbility> passives = rankPassives.get(type);
        
        if (passives != null) {
            for (int rank = 1; rank <= currentRank; rank++) {
                PassiveAbility passive = passives.get(rank);
                if (passive != null) {
                    passive.onActivate(player);
                }
            }
        }
    }

    /**
     * Initialize player rank for a Fragment (set to base rank)
     */
    public void initializeRank(Player player, FragmentType type) {
        int baseRank = getBaseRank(type);
        FragmentRankData data = getRankData(player, type);
        data.setCurrentRank(baseRank);
    }

    /**
     * Trigger rank-up notification
     */
    private void triggerRankUpNotification(Player player, FragmentType type, int newRank) {
        // Minimal clean notification
        player.sendMessage(
            com.muzlik.util.Typography.COLOR_ACCENT + "★ " +
            com.muzlik.util.Typography.COLOR_SECONDARY + type.getDisplayName() + " " +
            com.muzlik.util.Typography.COLOR_TEXT_DARK + com.muzlik.util.Typography.SYMBOL_ARROW + " " +
            com.muzlik.util.Typography.COLOR_HIGHLIGHT + com.muzlik.util.Typography.toSmallCaps("rank") + " " +
            com.muzlik.util.Typography.COLOR_ACCENT + newRank
        );
        
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
    }

    /**
     * Get or create rank data for player and Fragment
     */
    private FragmentRankData getRankData(Player player, FragmentType type) {
        Map<FragmentType, FragmentRankData> fragmentData = playerRankData.computeIfAbsent(
            player.getUniqueId(),
            uuid -> new ConcurrentHashMap<>()
        );
        
        return fragmentData.computeIfAbsent(type, t -> {
            FragmentRankData data = new FragmentRankData();
            data.setCurrentRank(getBaseRank(type));
            return data;
        });
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        playerRankData.remove(player.getUniqueId());
    }

    /**
     * Get all rank data for a player
     */
    public Map<FragmentType, FragmentRankData> getPlayerRankData(Player player) {
        return playerRankData.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>());
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        playerRankData.clear();
    }
}
