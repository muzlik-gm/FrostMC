package com.muzlik.ui;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles Fragment GUI interactions with enhanced visual effects.
 * 
 * Fragment Overview GUI:
 * - LEFT-CLICK: Open ability details for the fragment
 * - RIGHT-CLICK: Activate the fragment (if charged/owned)
 * 
 * Ability Details GUI:
 * - BACK button: Return to fragment overview
 */
public class FragmentGUIListener implements Listener {
    private final FragmentManager fragmentManager;
    private final UIManager uiManager;

    public FragmentGUIListener(FragmentManager fragmentManager, UIManager uiManager) {
        this.fragmentManager = fragmentManager;
        this.uiManager = uiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Check if it's a Fragment GUI
        // We check for "FRAGMENT" (small caps or normal) and "ABILITIES" (uppercase or small caps)
        boolean isFragmentOverview = title.contains("ꜰʀᴀɢᴍᴇɴᴛs") || title.contains("Fragment Overview");
        boolean isAbilityDetails = title.contains("ABILITIES") || title.contains("ᴀʙɪʟɪᴛɪᴇ") || title.contains("Abilities");
        
        if (!isFragmentOverview && !isAbilityDetails) {
            return;
        }
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        String displayName = meta.getDisplayName();
        ClickType clickType = event.getClick();
        
        // Handle Fragment Overview clicks
        if (isFragmentOverview) {
            handleFragmentOverviewClick(player, displayName, clickType, event);
        }
        // Handle Ability Detail View clicks
        else if (isAbilityDetails) {
            handleAbilityDetailClick(player, displayName);
        }
    }
    
    /**
     * Handle clicks in Fragment Overview GUI
     * LEFT-CLICK: View abilities (for ANY fragment, even locked)
     * RIGHT-CLICK: Activate fragment (requires charged/owned AND Fragment Changer)
     */
    private void handleFragmentOverviewClick(Player player, String displayName, ClickType clickType, InventoryClickEvent event) {
        // Remove the Switch Fragment button functionality - force ritual usage
        if (displayName.contains("sᴡɪᴛᴄʜ") || displayName.contains("Switch Fragment")) {
            player.closeInventory();
            player.sendMessage(com.muzlik.util.Typography.formatError("Use Fragment Changer ritual to switch fragments"));
            player.sendMessage(com.muzlik.util.Typography.formatTitle("Craft a Fragment Changer and perform the ritual"));
            return;
        }
        
        // Check for controls button
        if (displayName.contains("ᴄᴏɴᴛʀᴏʟ") || displayName.contains("Control")) {
            player.closeInventory();
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                () -> uiManager.openControlSchemeGUI(player),
                2L
            );
            return;
        }
        
        // Check for Mana Status button
        if (displayName.contains("ᴍᴀɴᴀ") || displayName.contains("Mana Status")) {
            // In-place refresh UX improvement: play a distinct "pling" sound
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);

            // Create a new version of the button with updated lore
            ItemStack updatedManaButton = uiManager.createManaStatusButton(player, true);

            // Replace the item in the inventory
            event.getInventory().setItem(event.getSlot(), updatedManaButton);

            // Schedule a task to revert the button back after 1 second (20 ticks)
            final int slot = event.getSlot();
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"), () -> {
                // Check if the inventory is still open to prevent errors
                if (player.getOpenInventory().getTopInventory().equals(event.getInventory())) {
                    ItemStack originalManaButton = uiManager.createManaStatusButton(player, false);
                    event.getInventory().setItem(slot, originalManaButton);
                }
            }, 20L);
            return;
        }
        
        // Check for Admin Give button
        if (displayName.contains("ɢɪᴠᴇ") || displayName.contains("Give Fragment")) {
            if (player.hasPermission("fragment.admin")) {
                player.closeInventory();
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                    () -> uiManager.openFragmentGiveGUI(player),
                    2L
                );
            } else {
                player.sendMessage("§cYou don't have permission to use this");
            }
            return;
        }
        
        // Check for info button / glass panes
        if (displayName.contains("ɢᴜɪᴅᴇ") || displayName.contains("Information") || 
            displayName.trim().isEmpty() || displayName.equals(" ")) {
            return;
        }
        
        // Extract Fragment type from display name
        FragmentType clickedType = findFragmentType(displayName);
        
        if (clickedType == null) {
            return;
        }
        
        boolean isOwned = fragmentManager.hasFragment(player, clickedType);
        boolean isCharged = fragmentManager.isCharged(player, clickedType);
        boolean isActive = clickedType.equals(fragmentManager.getActiveFragment(player));
        
        if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT) {
            // LEFT-CLICK: View abilities for ANY fragment (even locked ones!)
            player.closeInventory();
            // Small delay to prevent inventory glitch
            final FragmentType finalType = clickedType;
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                () -> uiManager.openAbilityDetails(player, finalType),
                2L
            );

            
        } else if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            // RIGHT-CLICK: Activate fragment
            if (isActive) {
                player.sendMessage(com.muzlik.util.Typography.formatError("This fragment is already active"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.9f); // Neutral feedback
                return;
            }
            
            // Get config manager to check settings
            com.muzlik.config.ConfigManager configManager = ((com.muzlik.FrostSMPPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP")).getConfigManager();
            boolean fragmentChangerRequired = configManager.isFragmentChangerRequired();
            
            if (isCharged) {
                // Charged fragments: Need Fragment Changer if required
                if (fragmentChangerRequired) {
                    if (hasFragmentChanger(player)) {
                        // Activate the charged fragment
                        boolean success = fragmentManager.activateChargedFragment(player, clickedType);
                        if (success) {
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.2f);
                            player.sendMessage(
                                com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                                com.muzlik.util.Typography.COLOR_SECONDARY + clickedType.getDisplayName() + " " +
                                com.muzlik.util.Typography.COLOR_TEXT_DARK + com.muzlik.util.Typography.toSmallCaps("activated")
                            );
                            uiManager.spawnFragmentSwitchParticles(player, clickedType);
                            
                            // Refresh the GUI
                            uiManager.refreshFragmentOverview(player);
                        }
                    } else {
                        uiManager.playLockedAbilitySound(player);
                        player.sendMessage(com.muzlik.util.Typography.formatError("You need a Fragment Changer in your inventory"));
                        player.sendMessage(com.muzlik.util.Typography.formatTitle("Craft one or complete the ritual"));
                    }
                } else {
                    // Fragment Changer not required - free activation
                    boolean success = fragmentManager.activateChargedFragment(player, clickedType);
                    if (success) {
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.2f);
                        player.sendMessage(
                            com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                            com.muzlik.util.Typography.COLOR_SECONDARY + clickedType.getDisplayName() + " " +
                            com.muzlik.util.Typography.COLOR_TEXT_DARK + com.muzlik.util.Typography.toSmallCaps("activated")
                        );
                        uiManager.spawnFragmentSwitchParticles(player, clickedType);
                        
                        // Refresh the GUI
                        uiManager.refreshFragmentOverview(player);
                    }
                }
                
            } else if (isOwned) {
                // Owned fragments: Can switch if Fragment Changer not required OR if player has it
                if (fragmentChangerRequired) {
                    if (hasFragmentChanger(player)) {
                        fragmentManager.setActiveFragment(player, clickedType);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                        player.sendMessage(
                            com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                            com.muzlik.util.Typography.COLOR_SECONDARY + clickedType.getDisplayName()
                        );
                        uiManager.spawnFragmentSwitchParticles(player, clickedType);
                        
                        // Refresh the GUI
                        uiManager.refreshFragmentOverview(player);
                    } else {
                        uiManager.playLockedAbilitySound(player);
                        player.sendMessage(com.muzlik.util.Typography.formatError("You need a Fragment Changer in your inventory"));
                        player.sendMessage(com.muzlik.util.Typography.formatTitle("Craft one or complete the ritual"));
                    }
                } else {
                    // Fragment Changer not required - free switching
                    fragmentManager.setActiveFragment(player, clickedType);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    player.sendMessage(
                        com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                        com.muzlik.util.Typography.COLOR_SECONDARY + clickedType.getDisplayName()
                    );
                    uiManager.spawnFragmentSwitchParticles(player, clickedType);
                    
                    // Refresh the GUI
                    uiManager.refreshFragmentOverview(player);
                }
                
            } else {
                // Locked
                uiManager.playLockedAbilitySound(player);
                player.sendMessage(com.muzlik.util.Typography.formatError("Complete ritual to unlock this fragment"));
            }
        }
    }
    
    /**
     * Check if player has a Fragment Changer in their inventory
     */
    private boolean hasFragmentChanger(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    String name = meta.getDisplayName().toLowerCase();
                    if (name.contains("fragment changer") || name.contains("ꜰʀᴀɢᴍᴇɴᴛ ᴄʜᴀɴɢᴇʀ")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Find FragmentType from display name
     */
    private FragmentType findFragmentType(String displayName) {
        // Strip color codes
        String strippedName = displayName.replaceAll("§[0-9a-fk-or]", "").toLowerCase();
        // Convert small caps
        String normalizedStripped = com.muzlik.util.Typography.fromSmallCaps(strippedName);
        
        for (FragmentType type : FragmentType.values()) {
            // HIDE ADMIN FRAGMENT FROM GUI
            if (type == FragmentType.ADMIN) continue;
            
            String normalName = type.getDisplayName().toLowerCase();
            if (strippedName.contains(normalName) || normalizedStripped.contains(normalName)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Handle clicks in Ability Detail View GUI
     */
    private void handleAbilityDetailClick(Player player, String displayName) {
        // Check for back button
        if (displayName.contains("Back") || displayName.contains("ʙᴀᴄᴋ") || displayName.contains("←")) {
            player.closeInventory();
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                () -> uiManager.openFragmentOverview(player),
                2L
            );
            return;
        }
        
        // Check for close button
        if (displayName.contains("Close") || displayName.contains("ᴄʟᴏsᴇ")) {
            player.closeInventory();
            return;
        }
        
        // Check if clicking on locked ability
        if (displayName.contains("✗") && displayName.contains("§c")) {
            uiManager.playLockedAbilitySound(player);
            player.sendMessage(com.muzlik.util.Typography.formatError("This ability is locked"));
            return;
        }
        
        // Unlocked ability clicked - show usage hint
        if (displayName.contains("✓") || displayName.contains("§a")) {
            player.sendMessage(com.muzlik.util.Typography.formatTitle("Use via hotbar (Sneak + Click)"));
        }
    }

}
