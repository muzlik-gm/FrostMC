package com.muzlik.listener;

import com.muzlik.character.CharacterLevelManager;
import com.muzlik.data.DataPersistence;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import com.muzlik.FrostSMPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles loading and saving player data on join/quit
 * FIXES: Mana resetting to 165/185 on server restart
 * NOW INCLUDES: Character Level persistence
 */
public class PlayerDataListener implements Listener {
    private final JavaPlugin plugin;
    private final DataPersistence dataPersistence;
    private final FragmentManager fragmentManager;
    private final ManaManager manaManager;
    private final LevelManager levelManager;
    private final RankManager rankManager;
    private CharacterLevelManager characterLevelManager;
    private com.muzlik.fragment.ability.AbilitySlotManager abilitySlotManager;

    public PlayerDataListener(JavaPlugin plugin, DataPersistence dataPersistence,
                             FragmentManager fragmentManager, ManaManager manaManager,
                             LevelManager levelManager, RankManager rankManager) {
        this.plugin = plugin;
        this.dataPersistence = dataPersistence;
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
        
        // Get CharacterLevelManager from main plugin
        if (plugin instanceof FrostSMPPlugin) {
            this.characterLevelManager = ((FrostSMPPlugin) plugin).getCharacterLevelManager();
            this.abilitySlotManager = ((FrostSMPPlugin) plugin).getAbilitySlotManager();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerData(event.getPlayer());
        
        // Task 11.1: Unlock fragment recipes in recipe book
        unlockFragmentRecipes(event.getPlayer());
        
        // Interactive tutorial auto-triggers on join (no manual activation needed)
    }
    
    /**
     * Unlock all fragment recipes in the player's recipe book (Task 11.1)
     */
    private void unlockFragmentRecipes(Player player) {
        try {
            // Unlock all 10 fragment creation recipes with null checks
            String[] recipeKeys = {
                "frostsmp:fire_fragment_creation",
                "frostsmp:water_fragment_creation", 
                "frostsmp:air_fragment_creation",
                "frostsmp:dark_fragment_creation",
                "frostsmp:light_fragment_creation",
                "frostsmp:void_fragment_creation",
                "frostsmp:dragon_fragment_creation",
                "frostsmp:storm_fragment_creation",
                "frostsmp:time_fragment_creation",
                "frostsmp:luck_fragment_creation"
            };
            
            for (String recipeKey : recipeKeys) {
                try {
                    org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(recipeKey);
                    if (key != null && plugin.getServer().getRecipe(key) != null) {
                        player.discoverRecipe(key);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to unlock recipe " + recipeKey + " for " + player.getName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock fragment recipes for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Load player data (public for reload support)
     */
    public void loadPlayerData(Player player) {
        // Load player data asynchronously
        dataPersistence.loadPlayerDataAsync(player.getUniqueId()).thenAccept(data -> {
            // Run on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // CRITICAL FIX: Check if player is still online before processing
                if (!player.isOnline()) {
                    plugin.getLogger().warning("Player " + player.getName() + " disconnected before data loading completed");
                    return;
                }
                
                // CRITICAL FIX: Load ALL owned fragments first
                if (data.fragments != null && !data.fragments.isEmpty()) {
                    for (java.util.Map.Entry<String, DataPersistence.FragmentDataContainer> entry : data.fragments.entrySet()) {
                        try {
                            FragmentType fragmentType = FragmentType.valueOf(entry.getKey());
                            DataPersistence.FragmentDataContainer fragmentData = entry.getValue();
                            
                            // Grant the fragment to the player
                            if (fragmentData.unlocked) {
                                // Directly add to player's owned fragments without triggering grant message
                                com.muzlik.fragment.PlayerFragmentData playerData = fragmentManager.getPlayerData(player);
                                if (playerData == null) {
                                    playerData = new com.muzlik.fragment.PlayerFragmentData();
                                    // Store it in FragmentManager (need to access private field, so we'll use grantFragment)
                                }
                                
                                // Use hasFragment check to avoid duplicate grants
                                if (!fragmentManager.hasFragment(player, fragmentType)) {
                                    fragmentManager.grantFragment(player, fragmentType);
                                }
                                
                                // Load rank, level, and XP
                                rankManager.setRank(player, fragmentType, fragmentData.rank);
                                levelManager.setLevel(player, fragmentType, fragmentData.level);
                                levelManager.setXP(player, fragmentType, fragmentData.xp);
                                
                                /* plugin.getLogger().info("Loaded Fragment " + fragmentType.name() + " for " + player.getName() + 
                                    ": Rank=" + fragmentData.rank + ", Level=" + fragmentData.level + 
                                    ", XP=" + fragmentData.xp); */
                            }
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid Fragment type for " + player.getName() + ": " + entry.getKey());
                        }
                    }
                }
                
                // Now activate the saved active fragment
                if (data.activeFragment != null) {
                    try {
                        FragmentType activeType = FragmentType.valueOf(data.activeFragment);
                        
                        // Set as active fragment
                        fragmentManager.setActiveFragment(player, activeType);
                        
                        // Load Fragment-specific data for active fragment
                        if (data.fragments != null && data.fragments.containsKey(data.activeFragment)) {
                            DataPersistence.FragmentDataContainer fragmentData = data.fragments.get(data.activeFragment);
                            
                            // Load rank and level (already set above, but ensure they're current)
                            int rank = fragmentData.rank;
                            int level = fragmentData.level;
                            
                            // Load mana for active fragment
                            double savedMana = fragmentData.currentMana;
                            manaManager.loadPlayerMana(player, savedMana, rank, level);
                            
                            /* plugin.getLogger().info("Activated Fragment " + activeType.name() + " for " + player.getName() + 
                                " with Mana=" + savedMana); */
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid active Fragment type for " + player.getName() + ": " + data.activeFragment);
                    }
                }
                
                // Load Character Level data
                if (characterLevelManager != null) {
                    int charLevel = data.characterLevel > 0 ? data.characterLevel : 1;
                    double charXP = data.characterXp >= 0 ? data.characterXp : 0;
                    characterLevelManager.loadCharacterData(player, charLevel, charXP);
                    /* plugin.getLogger().info("Loaded Character Level for " + player.getName() + 
                        ": Level=" + charLevel + ", XP=" + String.format("%.1f", charXP)); */
                }
                
                // Load Ability Slot data (Task 2.4)
                if (abilitySlotManager != null) {
                    abilitySlotManager.loadPlayerData(player);
                }
                
                // Load completed rituals (one-time fragment creation tracking)
                if (data.completedRituals != null && !data.completedRituals.isEmpty()) {
                    com.muzlik.fragment.PlayerFragmentData playerData = fragmentManager.getPlayerData(player);
                    if (playerData != null) {
                        for (String ritualName : data.completedRituals) {
                            try {
                                FragmentType fragmentType = FragmentType.valueOf(ritualName);
                                playerData.markRitualCompleted(fragmentType);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid completed ritual type for " + player.getName() + ": " + ritualName);
                            }
                        }
                        /* plugin.getLogger().info("Loaded " + data.completedRituals.size() + 
                            " completed rituals for " + player.getName()); */
                    }
                }
                
                /* plugin.getLogger().info("Loaded " + (data.fragments != null ? data.fragments.size() : 0) + 
                    " fragments for " + player.getName()); */
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player data
        DataPersistence.PlayerDataContainer data = new DataPersistence.PlayerDataContainer();
        data.playerId = player.getUniqueId().toString();
        
        // Save active Fragment
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        if (activeFragment != null) {
            data.activeFragment = activeFragment.name();
        }
        
        // CRITICAL FIX: Save ALL owned fragments, not just the active one!
        java.util.Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(player);
        data.fragments = new java.util.HashMap<>();
        
        for (FragmentType fragmentType : ownedFragments) {
            DataPersistence.FragmentDataContainer fragmentData = new DataPersistence.FragmentDataContainer();
            
            fragmentData.unlocked = true;
            fragmentData.rank = rankManager.getRank(player, fragmentType);
            fragmentData.level = levelManager.getLevel(player, fragmentType);
            fragmentData.xp = levelManager.getXP(player, fragmentType);
            
            // Save current mana only for active fragment
            if (fragmentType == activeFragment) {
                fragmentData.currentMana = manaManager.getCurrentManaForSave(player);
            } else {
                // For inactive fragments, save max mana (based on character level now)
                fragmentData.currentMana = manaManager.getMaxMana(player);
            }
            
            fragmentData.abilityCooldowns = new java.util.HashMap<>();
            
            data.fragments.put(fragmentType.name(), fragmentData);
            
            /* plugin.getLogger().info("Saving Fragment " + fragmentType.name() + " for " + player.getName() + 
                ": Rank=" + fragmentData.rank + ", Level=" + fragmentData.level + 
                ", XP=" + fragmentData.xp + ", Mana=" + fragmentData.currentMana); */
        }
        
        data.uiMode = "STANDARD";
        data.lastFragmentChange = 0;
        
        // Save Character Level data
        if (characterLevelManager != null) {
            data.characterLevel = characterLevelManager.getCharacterLevelForSave(player);
            data.characterXp = characterLevelManager.getCharacterXPForSave(player);
            /* plugin.getLogger().info("Saving Character Level for " + player.getName() + 
                ": Level=" + data.characterLevel + ", XP=" + String.format("%.1f", data.characterXp)); */
        }
        
        // Save completed rituals (one-time fragment creation tracking)
        com.muzlik.fragment.PlayerFragmentData playerData = fragmentManager.getPlayerData(player);
        if (playerData != null) {
            java.util.Collection<FragmentType> completedRituals = playerData.getCompletedRituals();
            if (!completedRituals.isEmpty()) {
                data.completedRituals = new java.util.ArrayList<>();
                for (FragmentType fragmentType : completedRituals) {
                    data.completedRituals.add(fragmentType.name());
                }
                /* plugin.getLogger().info("Saving " + completedRituals.size() + 
                    " completed rituals for " + player.getName()); */
            }
        }
        
        // Save asynchronously
        dataPersistence.savePlayerDataAsync(player.getUniqueId(), data);
        
        // Save Ability Slot data (Task 2.4)
        if (abilitySlotManager != null) {
            abilitySlotManager.savePlayerData(player);
        }
        
        /* plugin.getLogger().info("Saved " + ownedFragments.size() + " fragments for " + player.getName() + 
            " (Active: " + (activeFragment != null ? activeFragment.name() : "none") + ")"); */
        
        // Clean up managers
        manaManager.removePlayer(player);
        fragmentManager.removePlayer(player);
        if (characterLevelManager != null) {
            characterLevelManager.removePlayer(player);
        }
        if (abilitySlotManager != null) {
            abilitySlotManager.removePlayer(player);
        }
    }
}
