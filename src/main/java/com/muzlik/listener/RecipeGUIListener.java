package com.muzlik.listener;

import com.muzlik.fragment.FragmentType;
import com.muzlik.ui.RecipeDiscoveryGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles clicks in Recipe Discovery GUI
 */
public class RecipeGUIListener implements Listener {

    private static final String MAIN_TITLE = "§6§lFragment Recipes";
    private static final String DETAIL_TITLE_PREFIX = "§6§lRecipe: ";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        
        // Check if it's our GUI
        if (!title.equals(MAIN_TITLE) && !title.startsWith(DETAIL_TITLE_PREFIX)) {
            return;
        }

        // Cancel all clicks in our GUIs
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();

        // Handle main GUI clicks
        if (title.equals(MAIN_TITLE)) {
            handleMainGUIClick(player, displayName);
        }
        // Handle detail GUI clicks
        else if (title.startsWith(DETAIL_TITLE_PREFIX)) {
            handleDetailGUIClick(player, displayName);
        }
    }

    /**
     * Handle clicks in the main recipe list GUI
     */
    private void handleMainGUIClick(Player player, String displayName) {
        // Check which fragment was clicked
        for (FragmentType type : FragmentType.values()) {
            if (type == FragmentType.ADMIN) continue; // Skip admin fragment
            
            // Match by fragment display name (color codes are stripped from display names in comparison)
            if (displayName.contains(type.getDisplayName() + " Fragment")) {
                RecipeDiscoveryGUI.openDetailGUI(player, type);
                return;
            }
        }
    }

    /**
     * Handle clicks in the detail recipe view
     */
    private void handleDetailGUIClick(Player player, String displayName) {
        // Check for back button
        if (displayName.equals("§c← Back")) {
            RecipeDiscoveryGUI.openMainGUI(player);
        }
    }
}
