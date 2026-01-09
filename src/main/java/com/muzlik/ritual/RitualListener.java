package com.muzlik.ritual;

import com.muzlik.fragment.FragmentType;
import com.muzlik.recipe.RecipeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles ritual item interactions and player quit cleanup.
 * Rituals are started by placing the ritual item as a block.
 */
public class RitualListener implements Listener {
    private final JavaPlugin plugin;
    private final RitualManager ritualManager;
    private final RecipeManager recipeManager;

    public RitualListener(JavaPlugin plugin, RitualManager ritualManager, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.ritualManager = ritualManager;
        this.recipeManager = recipeManager;
    }
    
    /**
     * Handle player quit - ritual will enter grace period if no other players are in the area
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ritualManager.removePlayer(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (item == null) {
            return;
        }
        
        // Check if item is a Fragment Creation item
        if (recipeManager.isFragmentCreationItem(item)) {
            event.setCancelled(true); // Cancel block placement
            FragmentType fragmentType = recipeManager.getFragmentTypeFromItem(item);
            
            if (fragmentType != null) {
                boolean started = ritualManager.startRitual(player, RitualType.FRAGMENT_CREATION, item, fragmentType);
                if (started) {
                    // Remove one item from stack
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                }
            }
            return;
        }
        
        // Check if item is a Fragment Changer
        if (recipeManager.isFragmentChanger(item)) {
            event.setCancelled(true); // Cancel block placement
            boolean started = ritualManager.startRitual(player, RitualType.FRAGMENT_CHANGER, item, null);
            if (started) {
                // Remove one item from stack
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            }
            return;
        }
    }
    
    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) {
            return;
        }
        
        // Only handle right-click actions
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Check if item is a Fragment Changer
        if (recipeManager.isFragmentChanger(item)) {
            event.setCancelled(true);
            boolean started = ritualManager.startRitual(player, RitualType.FRAGMENT_CHANGER, item, null);
            if (started) {
                // Remove one item from stack
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            }
            return;
        }
        
        // Check if item is a Fragment Creation item
        if (recipeManager.isFragmentCreationItem(item)) {
            event.setCancelled(true);
            FragmentType fragmentType = recipeManager.getFragmentTypeFromItem(item);
            
            if (fragmentType != null) {
                // Check if ritual was already completed BEFORE attempting to start
                com.muzlik.fragment.FragmentManager fragmentManager = ((com.muzlik.FrostSMPPlugin) plugin).getFragmentManager();
                com.muzlik.fragment.PlayerFragmentData fragmentData = fragmentManager.getPlayerData(player);
                
                if (fragmentData != null && fragmentData.hasCompletedRitual(fragmentType)) {
                    player.sendMessage("§c✗ You have already created the " + fragmentType.getDisplayName() + " Fragment!");
                    player.sendMessage("§7You cannot perform this ritual again.");
                    return;
                }
                
                boolean started = ritualManager.startRitual(player, RitualType.FRAGMENT_CREATION, item, fragmentType);
                if (started) {
                    // Remove one item from stack
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                }
            }
            return;
        }
    }
}
