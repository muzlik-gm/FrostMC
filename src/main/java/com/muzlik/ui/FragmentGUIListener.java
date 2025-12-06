package com.muzlik.ui;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles Fragment GUI interactions with enhanced visual effects
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
        
        // Check if it's a Fragment GUI - FIXED: Check for small caps titles
        if (!title.contains("ꜰʀᴀɢᴍᴇɴᴛ") && 
            !title.contains("ᴀʙɪʟɪᴛɪᴇ") && 
            !title.contains("ᴍᴀɴᴀ") &&
            !title.contains("Fragment") && // Fallback for old titles
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
        
        // Handle Fragment Overview clicks - FIXED: Check for small caps
        if (title.contains("ꜰʀᴀɢᴍᴇɴᴛs") || title.contains("Fragment Overview")) {
            handleFragmentOverviewClick(player, displayName);
        }
        // Handle Ability Detail View clicks - FIXED: Check for small caps
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
     */
    private void handleFragmentOverviewClick(Player player, String displayName) {
        // DEBUG
        org.bukkit.Bukkit.getLogger().info("[GUI] Click detected: " + displayName);
        
        // Check for info button
        if (displayName.contains("Information") || displayName.contains("ɪɴꜰᴏ")) {
            return; // Just informational, no action
        }
        
        // Check for close button
        if (displayName.contains("Close") || displayName.contains("ᴄʟᴏsᴇ")) {
            player.closeInventory();
            return;
        }
        
        // Extract Fragment type from display name - FIXED: Strip color codes and match
        FragmentType clickedType = null;
        
        // Strip color codes from display name
        String strippedName = displayName.replaceAll("§[0-9a-fk-or]", "").toLowerCase();
        org.bukkit.Bukkit.getLogger().info("[GUI] Stripped name: " + strippedName);
        
        // Convert small caps back to normal for comparison
        String normalizedStripped = convertSmallCapsToNormal(strippedName);
        org.bukkit.Bukkit.getLogger().info("[GUI] Normalized name: " + normalizedStripped);
        
        for (FragmentType type : FragmentType.values()) {
            String normalName = type.getDisplayName().toLowerCase();
            
            // Check if stripped name contains the fragment name
            if (strippedName.contains(normalName) || normalizedStripped.contains(normalName)) {
                clickedType = type;
                org.bukkit.Bukkit.getLogger().info("[GUI] Matched fragment: " + type.name());
                break;
            }
        }
        
        if (clickedType == null) {
            org.bukkit.Bukkit.getLogger().warning("[GUI] No fragment matched for: " + displayName);
            org.bukkit.Bukkit.getLogger().warning("[GUI] Stripped: " + strippedName);
            return;
        }
        
        // Check if player owns this Fragment
        if (!fragmentManager.hasFragment(player, clickedType)) {
            // Play ERROR sound for locked Fragment (Requirement 4.5)
            uiManager.playLockedAbilitySound(player);
            player.sendMessage("§8ʏᴏᴜ ᴅᴏɴ'ᴛ ᴏᴡɴ ᴛʜɪs ꜰʀᴀɢᴍᴇɴᴛ");
            return;
        }
        
        // Open ability details
        org.bukkit.Bukkit.getLogger().info("[GUI] Opening abilities for: " + clickedType.name());
        player.closeInventory();
        
        // Small delay to prevent inventory glitch
        final FragmentType finalType = clickedType;
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.Bukkit.getPluginManager().getPlugin("FrostSMP"),
            () -> uiManager.openAbilityDetails(player, finalType),
            2L
        );
    }
    
    /**
     * Handle clicks in Ability Detail View GUI
     */
    private void handleAbilityDetailClick(Player player, String displayName) {
        // Check for back button
        if (displayName.contains("Back") || displayName.contains("ʙᴀᴄᴋ")) {
            player.closeInventory();
            uiManager.openFragmentOverview(player);
            return;
        }
        
        // Check for close button
        if (displayName.contains("Close") || displayName.contains("ᴄʟᴏsᴇ")) {
            player.closeInventory();
            return;
        }
        
        // Check if clicking on locked ability
        if (displayName.contains("ʟᴏᴄᴋᴇᴅ") || displayName.contains("§c")) {
            // Play ERROR sound for locked ability (Requirement 4.5)
            uiManager.playLockedAbilitySound(player);
            player.sendMessage("§8ᴛʜɪs ᴀʙɪʟɪᴛʏ ɪs ʟᴏᴄᴋᴇᴅ");
            return;
        }
        
        // Check if clicking on ability on cooldown
        if (displayName.contains("cooldown") || displayName.contains("ᴄᴏᴏʟᴅᴏᴡɴ")) {
            player.sendMessage("§8ᴀʙɪʟɪᴛʏ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ");
            return;
        }
        
        // Unlocked ability clicked - just show info (abilities are used via hotbar, not GUI)
        if (displayName.contains("ᴀᴄᴛɪᴠᴇ") || displayName.contains("§a")) {
            player.sendMessage("§7ᴜsᴇ ᴠɪᴀ ʜᴏᴛʙᴀʀ §8(sɴᴇᴀᴋ + ᴄʟɪᴄᴋ)");
        }
    }
}
