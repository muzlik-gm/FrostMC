package com.muzlik.fragment.ability;

import com.muzlik.fragment.FragmentDefinition;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.ui.UIManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when abilities are newly unlocked and triggers notifications.
 * Monitors rank and level changes to detect unlock events.
 */
public class AbilityUnlockDetector {
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final RankManager rankManager;
    private final LevelManager levelManager;
    private final UIManager uiManager;
    
    // Track previous unlock states: UUID -> FragmentType -> Set<AbilityId>
    private final Map<UUID, Map<FragmentType, Set<String>>> previousUnlocks;
    
    public AbilityUnlockDetector(JavaPlugin plugin, FragmentManager fragmentManager,
                                RankManager rankManager, LevelManager levelManager,
                                UIManager uiManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.rankManager = rankManager;
        this.levelManager = levelManager;
        this.uiManager = uiManager;
        this.previousUnlocks = new ConcurrentHashMap<>();
    }
    
    /**
     * Check for newly unlocked abilities after a rank change
     */
    public void checkAfterRankChange(Player player, FragmentType fragmentType, int newRank) {
        checkForUnlocks(player, fragmentType);
    }
    
    /**
     * Check for newly unlocked abilities after a level change
     */
    public void checkAfterLevelChange(Player player, FragmentType fragmentType, int newLevel) {
        checkForUnlocks(player, fragmentType);
    }
    
    /**
     * Check for newly unlocked abilities
     */
    private void checkForUnlocks(Player player, FragmentType fragmentType) {
        FragmentDefinition fragment = fragmentManager.getFragment(fragmentType);
        if (fragment == null) {
            return;
        }
        
        // Get current unlock state
        Set<String> currentUnlocks = getCurrentUnlocks(player, fragmentType, fragment);
        
        // Get previous unlock state
        Set<String> previousUnlocksSet = getPreviousUnlocks(player, fragmentType);
        
        // Find newly unlocked abilities
        Set<String> newlyUnlocked = new HashSet<>(currentUnlocks);
        newlyUnlocked.removeAll(previousUnlocksSet);
        
        // Trigger notifications for newly unlocked abilities
        if (!newlyUnlocked.isEmpty()) {
            for (String abilityId : newlyUnlocked) {
                AbilityDefinition ability = findAbilityById(fragment, abilityId);
                if (ability != null) {
                    // Trigger unlock notification
                    uiManager.showAbilityUnlockNotification(player, ability.getDisplayName());
                }
            }
        }
        
        // Update previous unlock state
        updatePreviousUnlocks(player, fragmentType, currentUnlocks);
    }
    
    /**
     * Get currently unlocked abilities for a player and Fragment
     */
    private Set<String> getCurrentUnlocks(Player player, FragmentType fragmentType, FragmentDefinition fragment) {
        Set<String> unlocked = new HashSet<>();
        
        int playerRank = rankManager.getRank(player, fragmentType);
        int playerLevel = levelManager.getLevel(player, fragmentType);
        
        for (AbilityDefinition ability : fragment.getAbilities()) {
            if (playerRank >= ability.getRankRequirement() && 
                playerLevel >= ability.getLevelRequirement()) {
                unlocked.add(ability.getId());
            }
        }
        
        return unlocked;
    }
    
    /**
     * Get previous unlock state for a player and Fragment
     */
    private Set<String> getPreviousUnlocks(Player player, FragmentType fragmentType) {
        Map<FragmentType, Set<String>> playerUnlocks = previousUnlocks.get(player.getUniqueId());
        if (playerUnlocks == null) {
            return new HashSet<>();
        }
        
        Set<String> unlocks = playerUnlocks.get(fragmentType);
        return unlocks != null ? new HashSet<>(unlocks) : new HashSet<>();
    }
    
    /**
     * Update previous unlock state
     */
    private void updatePreviousUnlocks(Player player, FragmentType fragmentType, Set<String> unlocks) {
        Map<FragmentType, Set<String>> playerUnlocks = previousUnlocks.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>());
        playerUnlocks.put(fragmentType, new HashSet<>(unlocks));
    }
    
    /**
     * Find ability by ID in Fragment
     */
    private AbilityDefinition findAbilityById(FragmentDefinition fragment, String abilityId) {
        for (AbilityDefinition ability : fragment.getAbilities()) {
            if (ability.getId().equals(abilityId)) {
                return ability;
            }
        }
        return null;
    }
    
    /**
     * Initialize unlock state for a player (call on join)
     */
    public void initializePlayer(Player player) {
        // Initialize unlock state for all owned Fragments
        Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(player);
        
        for (FragmentType fragmentType : ownedFragments) {
            FragmentDefinition fragment = fragmentManager.getFragment(fragmentType);
            if (fragment != null) {
                Set<String> currentUnlocks = getCurrentUnlocks(player, fragmentType, fragment);
                updatePreviousUnlocks(player, fragmentType, currentUnlocks);
            }
        }
    }
    
    /**
     * Remove player data (call on quit)
     */
    public void removePlayer(Player player) {
        previousUnlocks.remove(player.getUniqueId());
    }
    
    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        previousUnlocks.clear();
    }
}
