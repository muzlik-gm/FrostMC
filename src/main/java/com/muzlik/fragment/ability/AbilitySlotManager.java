package com.muzlik.fragment.ability;

import com.muzlik.data.DataPersistence;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ability slot unlocking for players.
 * Tracks which slots are unlocked for each Fragment type per player.
 */
public class AbilitySlotManager {

    private final JavaPlugin plugin;
    private final DataPersistence dataPersistence;
    
    // Map: PlayerUUID -> FragmentType -> Set of unlocked slot indices
    private final Map<UUID, Map<FragmentType, Set<Integer>>> unlockedSlots;

    public AbilitySlotManager(JavaPlugin plugin, DataPersistence dataPersistence) {
        this.plugin = plugin;
        this.dataPersistence = dataPersistence;
        this.unlockedSlots = new ConcurrentHashMap<>();
    }

    /**
     * Check if a slot is unlocked for a player's Fragment
     * @param player The player
     * @param fragmentType The Fragment type
     * @param slotIndex The slot index (0-4)
     * @return true if unlocked, false otherwise
     */
    public boolean isSlotUnlocked(Player player, FragmentType fragmentType, int slotIndex) {
        // Slots 0-2 are always unlocked
        if (slotIndex >= 0 && slotIndex <= 2) {
            return true;
        }

        // Task 3.4: Fix map chain null safety - check each level for null
        UUID playerId = player.getUniqueId();
        Map<FragmentType, Set<Integer>> playerSlots = unlockedSlots.get(playerId);
        if (playerSlots == null) {
            return false;
        }
        
        Set<Integer> fragmentSlots = playerSlots.get(fragmentType);
        if (fragmentSlots == null) {
            return false;
        }
        
        return fragmentSlots.contains(slotIndex);
    }

    /**
     * Check if a slot is unlocked using AbilitySlot enum
     * @param player The player
     * @param fragmentType The Fragment type
     * @param slot The AbilitySlot
     * @return true if unlocked, false otherwise
     */
    public boolean isSlotUnlocked(Player player, FragmentType fragmentType, AbilitySlot slot) {
        return isSlotUnlocked(player, fragmentType, slot.getSlotIndex());
    }

    /**
     * Unlock a slot for a player's Fragment
     * @param player The player
     * @param fragmentType The Fragment type
     * @param slotIndex The slot index to unlock
     * @return true if successfully unlocked, false if already unlocked
     */
    public boolean unlockSlot(Player player, FragmentType fragmentType, int slotIndex) {
        UUID playerId = player.getUniqueId();
        
        unlockedSlots.putIfAbsent(playerId, new ConcurrentHashMap<>());
        unlockedSlots.get(playerId).putIfAbsent(fragmentType, ConcurrentHashMap.newKeySet());
        
        boolean added = unlockedSlots.get(playerId).get(fragmentType).add(slotIndex);
        
        if (added) {
            // Play unlock feedback (Task 10.2)
            AbilitySlotFeedback.playUnlockFeedback(player, fragmentType, slotIndex);
            savePlayerData(player);
        }
        
        return added;
    }

    /**
     * Unlock a slot using AbilitySlot enum
     * @param player The player
     * @param fragmentType The Fragment type
     * @param slot The AbilitySlot to unlock
     * @return true if successfully unlocked, false if already unlocked
     */
    public boolean unlockSlot(Player player, FragmentType fragmentType, AbilitySlot slot) {
        return unlockSlot(player, fragmentType, slot.getSlotIndex());
    }

    /**
     * Validate if a slot can be unlocked based on Fragment rank and base rank
     * @param fragmentType The Fragment type
     * @param currentRank The current Fragment rank
     * @param slotIndex The slot index to validate
     * @return true if can be unlocked, false otherwise
     */
    public boolean canUnlockSlot(FragmentType fragmentType, int currentRank, int slotIndex) {
        // Slots 0-2 are always available
        if (slotIndex >= 0 && slotIndex <= 2) {
            return true;
        }

        // Get the base rank of the Fragment
        int baseRank = getFragmentBaseRank(fragmentType);
        
        // Rank 2 Fragments cannot unlock slots 3-4
        if (baseRank == 2 && (slotIndex == 3 || slotIndex == 4)) {
            return false;
        }

        // Get unlock threshold for this slot
        int unlockThreshold = getSlotUnlockThreshold(fragmentType, slotIndex);
        
        return currentRank >= unlockThreshold;
    }

    /**
     * Get the unlock threshold rank for a specific slot
     * @param fragmentType The Fragment type
     * @param slotIndex The slot index
     * @return The rank required to unlock this slot
     */
    public int getSlotUnlockThreshold(FragmentType fragmentType, int slotIndex) {
        int baseRank = getFragmentBaseRank(fragmentType);
        
        if (slotIndex == 3) {
            // Slot 3 unlock thresholds based on base rank
            switch (baseRank) {
                case 3: return 5;
                case 4: return 6;
                case 5: return 7;
                case 6: return 8;
                case 7: return 9;
                case 8: return 10;
                default: return 999; // Cannot unlock
            }
        } else if (slotIndex == 4) {
            // Slot 4 unlock thresholds based on base rank
            switch (baseRank) {
                case 3: return 7;
                case 4: return 8;
                case 5: return 9;
                case 6: return 10;
                case 7: return 11;
                case 8: return 12;
                default: return 999; // Cannot unlock
            }
        }
        
        return 0; // Slots 0-2 are always unlocked
    }

    /**
     * Get the base rank of a Fragment type
     * @param fragmentType The Fragment type
     * @return The base rank
     */
    private int getFragmentBaseRank(FragmentType fragmentType) {
        switch (fragmentType) {
            case WATER:
                return 2;
            case FIRE:
            case AIR:
                return 3;
            case DARK:
            case LIGHT:
                return 4;
            case STORM:
                return 6;
            case VOID:
                return 7;
            case DRAGON:
                return 8;
            default:
                return 2;
        }
    }

    /**
     * Get all unlocked slots for a player's Fragment
     * @param player The player
     * @param fragmentType The Fragment type
     * @return Set of unlocked slot indices
     */
    public Set<Integer> getUnlockedSlots(Player player, FragmentType fragmentType) {
        UUID playerId = player.getUniqueId();
        
        // Always include slots 0-2
        Set<Integer> slots = new HashSet<>(Arrays.asList(0, 1, 2));
        
        if (unlockedSlots.containsKey(playerId) && 
            unlockedSlots.get(playerId).containsKey(fragmentType)) {
            slots.addAll(unlockedSlots.get(playerId).get(fragmentType));
        }
        
        return slots;
    }

    /**
     * Load player data from persistence
     * @param player The player
     */
    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        
        try {
            DataPersistence.PlayerDataContainer data = dataPersistence.loadPlayerDataAsync(playerId).join();
            
            // Initialize player's map if not exists
            unlockedSlots.putIfAbsent(playerId, new ConcurrentHashMap<>());
            
            // Load unlocked ability slots
            if (data.unlockedAbilitySlots != null) {
                for (Map.Entry<String, Set<Integer>> entry : data.unlockedAbilitySlots.entrySet()) {
                    try {
                        FragmentType type = FragmentType.valueOf(entry.getKey());
                        Set<Integer> slots = entry.getValue();
                        
                        // Initialize fragment's set if not exists
                        unlockedSlots.get(playerId).putIfAbsent(type, ConcurrentHashMap.newKeySet());
                        
                        // Add all unlocked slots
                        unlockedSlots.get(playerId).get(type).addAll(slots);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid fragment type in ability slots: " + entry.getKey());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load ability slot data for player " + playerId + ": " + e.getMessage());
            // Initialize empty data
            unlockedSlots.putIfAbsent(playerId, new ConcurrentHashMap<>());
        }
    }

    /**
     * Save player data to persistence (public for quit event)
     * @param player The player
     */
    public void savePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check if dataPersistence is null (can happen during shutdown)
        if (dataPersistence == null) {
            plugin.getLogger().warning("Cannot save ability slot data for player " + playerId + ": DataPersistence is null (likely during shutdown)");
            return;
        }
        
        try {
            // Load existing data
            DataPersistence.PlayerDataContainer data = dataPersistence.loadPlayerDataAsync(playerId).join();
            
            // Convert in-memory slot states to JSON format
            Map<String, Set<Integer>> slotsToSave = new HashMap<>();
            Map<FragmentType, Set<Integer>> playerSlots = unlockedSlots.get(playerId);
            
            if (playerSlots != null) {
                for (Map.Entry<FragmentType, Set<Integer>> entry : playerSlots.entrySet()) {
                    slotsToSave.put(entry.getKey().name(), new HashSet<>(entry.getValue()));
                }
            }
            
            // Update data container
            data.unlockedAbilitySlots = slotsToSave;
            
            // Save to disk
            dataPersistence.savePlayerDataAsync(playerId, data).join();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save ability slot data for player " + playerId + ": " + e.getMessage());
        }
    }

    /**
     * Remove player data
     * @param player The player
     */
    public void removePlayer(Player player) {
        unlockedSlots.remove(player.getUniqueId());
    }

    /**
     * Get error message for why a slot cannot be unlocked (Task 13.2: Enhanced)
     * @param fragmentType The Fragment type
     * @param currentRank The current Fragment rank
     * @param slotIndex The slot index
     * @return Error message explaining why slot cannot be unlocked
     */
    public String getUnlockErrorMessage(FragmentType fragmentType, int currentRank, int slotIndex) {
        int baseRank = getFragmentBaseRank(fragmentType);
        
        // Rank 2 Fragments cannot unlock slots 3-4
        if (baseRank == 2 && (slotIndex == 3 || slotIndex == 4)) {
            return "§c✗ Rank 2 Fragments cannot unlock additional ability slots!";
        }

        // Check rank requirement - Task 13.2: Show required vs current rank and how many ranks needed
        int requiredRank = getSlotUnlockThreshold(fragmentType, slotIndex);
        if (currentRank < requiredRank) {
            int ranksNeeded = requiredRank - currentRank;
            return "§c✗ Requires Rank §6" + requiredRank + " §7(Current: §6" + currentRank + "§7)\n" +
                   "§7You need §e" + ranksNeeded + " more rank" + (ranksNeeded > 1 ? "s" : "") + " §7to unlock this slot";
        }

        return "§c✗ Slot cannot be unlocked";
    }

    /**
     * Clear all data
     */
    public void clearAll() {
        unlockedSlots.clear();
    }
}
