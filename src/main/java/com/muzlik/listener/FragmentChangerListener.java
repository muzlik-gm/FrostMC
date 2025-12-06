package com.muzlik.listener;

import com.muzlik.ui.UIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener for Fragment Changer item interactions.
 * 
 * Right-clicking a Fragment Changer opens the Fragment GUI,
 * where players can right-click charged/owned fragments to activate them.
 */
public class FragmentChangerListener implements Listener {
    private final UIManager uiManager;
    
    public FragmentChangerListener(UIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();
        
        // Only handle right-clicks
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Check if holding Fragment Changer
        if (!isFragmentChanger(item)) {
            return;
        }
        
        // Cancel the event to prevent block placement
        event.setCancelled(true);
        
        // Open the Fragment GUI
        player.sendMessage("§e⚡ ᴏᴘᴇɴɪɴɢ ꜰʀᴀɢᴍᴇɴᴛ sᴇʟᴇᴄᴛᴏʀ...");
        uiManager.openFragmentOverview(player);
    }
    
    /**
     * Check if an item is a Fragment Changer
     */
    private boolean isFragmentChanger(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        
        String name = meta.getDisplayName().toLowerCase();
        // Check for both normal and small caps versions
        return name.contains("fragment changer") || 
               name.contains("ꜰʀᴀɢᴍᴇɴᴛ ᴄʜᴀɴɢᴇʀ");
    }
}
