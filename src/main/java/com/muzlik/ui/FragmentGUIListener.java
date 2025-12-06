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
        
        // Check if it's a Fragment GUI - Check for small caps titles
        if (!title.contains("ꜰʀᴀɢᴍᴇɴᴛ") && 
            !title.contains("ᴀʙɪʟɪᴛɪᴇ") && 
            !title.contains("ᴍᴀɴᴀ") &&
            !title.contains("Fragment") && // Fallback
            !title.contains("Abilities") && 
            !title.contains("Mana")) {
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
        if (title.contains("ꜰʀᴀɢᴍᴇɴᴛs") || title.contains("Fragment Overview")) {
            handleFragmentOverviewClick(player, displayName, clickType);
        }
        // Handle Ability Detail View clicks
        else if (title.contains("ᴀʙɪʟɪᴛɪᴇ") || title.contains("Abilities")) {
            handleAbilityDetailClick(player, displayName);
        }
    }
    
    /**
     * Convert small caps unicode back to normal characters
     */
    private String convertSmallCapsToNormal(String smallCaps) {
        String smallCapsChars = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
        String normalChars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        
        for (char c : smallCaps.toCharArray()) {
            int index = smallCapsChars.indexOf(c);
            if (index >= 0) {
                result.append(normalChars.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * Handle clicks in Fragment Overview GUI
     * LEFT-CLICK: View abilities
     * RIGHT-CLICK: Activate fragment
     */
    private void handleFragmentOverviewClick(Player player, String displayName, ClickType clickType) {
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
            // LEFT-CLICK: View abilities (for owned, charged, or active fragments)
            if (isOwned || isCharged || isActive) {
                player.closeInventory();
                // Small delay to prevent inventory glitch
                final FragmentType finalType = clickedType;
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                    () -> uiManager.openAbilityDetails(player, finalType),
                    2L
                );
            } else {
                // Locked fragment
                uiManager.playLockedAbilitySound(player);
                player.sendMessage("§8ᴄᴏᴍᴘʟᴇᴛᴇ ʀɪᴛᴜᴀʟ ᴛᴏ ᴜɴʟᴏᴄᴋ ᴛʜɪs ꜰʀᴀɢᴍᴇɴᴛ");
            }
            
        } else if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            // RIGHT-CLICK: Activate fragment
            if (isActive) {
                player.sendMessage("§7ᴛʜɪs ꜰʀᴀɢᴍᴇɴᴛ ɪs ᴀʟʀᴇᴀᴅʏ ᴀᴄᴛɪᴠᴇ");
                return;
            }
            
            if (isCharged) {
                // Check if player has a Fragment Changer
                if (hasFragmentChanger(player)) {
                    // Activate the charged fragment
                    boolean success = fragmentManager.activateChargedFragment(player, clickedType);
                    if (success) {
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                        player.sendMessage("§a§l✓ " + clickedType.getDisplayName() + " Fragment ACTIVATED!");
                        uiManager.spawnFragmentSwitchParticles(player, clickedType);
                        
                        // Refresh the GUI
                        player.closeInventory();
                        org.bukkit.Bukkit.getScheduler().runTaskLater(
                            org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                            () -> uiManager.openFragmentOverview(player),
                            3L
                        );
                    }
                } else {
                    uiManager.playLockedAbilitySound(player);
                    player.sendMessage("§c✗ ʏᴏᴜ ɴᴇᴇᴅ ᴀ ꜰʀᴀɢᴍᴇɴᴛ ᴄʜᴀɴɢᴇʀ");
                    player.sendMessage("§7ᴄʀᴀꜰᴛ ᴏɴᴇ ᴛᴏ ᴀᴄᴛɪᴠᴀᴛᴇ ᴄʜᴀʀɢᴇᴅ ꜰʀᴀɢᴍᴇɴᴛs");
                }
                
            } else if (isOwned) {
                // Can switch to owned fragments with changer
                if (hasFragmentChanger(player)) {
                    fragmentManager.setActiveFragment(player, clickedType);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.sendMessage("§a✓ sᴡɪᴛᴄʜᴇᴅ ᴛᴏ " + clickedType.getDisplayName());
                    uiManager.spawnFragmentSwitchParticles(player, clickedType);
                    
                    // Refresh the GUI
                    player.closeInventory();
                    org.bukkit.Bukkit.getScheduler().runTaskLater(
                        org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
                        () -> uiManager.openFragmentOverview(player),
                        3L
                    );
                } else {
                    uiManager.playLockedAbilitySound(player);
                    player.sendMessage("§c✗ ʏᴏᴜ ɴᴇᴇᴅ ᴀ ꜰʀᴀɢᴍᴇɴᴛ ᴄʜᴀɴɢᴇʀ");
                }
                
            } else {
                // Locked
                uiManager.playLockedAbilitySound(player);
                player.sendMessage("§8ᴄᴏᴍᴘʟᴇᴛᴇ ʀɪᴛᴜᴀʟ ᴛᴏ ᴜɴʟᴏᴄᴋ ᴛʜɪs ꜰʀᴀɢᴍᴇɴᴛ");
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
        String normalizedStripped = convertSmallCapsToNormal(strippedName);
        
        for (FragmentType type : FragmentType.values()) {
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
            player.sendMessage("§8ᴛʜɪs ᴀʙɪʟɪᴛʏ ɪs ʟᴏᴄᴋᴇᴅ");
            return;
        }
        
        // Unlocked ability clicked - show usage hint
        if (displayName.contains("✓") || displayName.contains("§a")) {
            player.sendMessage("§7ᴜsᴇ ᴠɪᴀ ʜᴏᴛʙᴀʀ §8(sɴᴇᴀᴋ + ᴄʟɪᴄᴋ)");
        }
    }
}
