package com.muzlik.ui;

import com.muzlik.player.ControlScheme;
import com.muzlik.player.PlayerPreferencesManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI for selecting control schemes
 */
public class ControlSchemeGUI implements Listener {
    
    private final PlayerPreferencesManager preferencesManager;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    
    private static final String GUI_TITLE = "§8§l⚙ ᴄᴏɴᴛʀᴏʟ sᴄʜᴇᴍᴇs";
    
    public ControlSchemeGUI(PlayerPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }
    
    /**
     * Open control scheme selection GUI
     */
    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);
        
        ControlScheme current = preferencesManager.getControlScheme(player);
        boolean abilitiesEnabled = preferencesManager.areAbilitiesEnabled(player);
        
        // Fill border
        fillBorder(inv);
        
        // Control scheme buttons (slots 10-16)
        int slot = 10;
        for (ControlScheme scheme : ControlScheme.values()) {
            boolean isActive = scheme == current;
            ItemStack item = createSchemeButton(scheme, isActive);
            inv.setItem(slot, item);
            slot++;
        }
        
        // Toggle abilities button (slot 22)
        ItemStack toggleButton = createToggleButton(abilitiesEnabled);
        inv.setItem(22, toggleButton);
        
        openInventories.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }
    
    /**
     * Create control scheme button
     */
    private ItemStack createSchemeButton(ControlScheme scheme, boolean isActive) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data based on scheme
        int customModelData;
        switch (scheme) {
            case SNEAK_CLICK:
                customModelData = 2001; // Custom model for sneak+click
                break;
            case DOUBLE_SNEAK:
                customModelData = 2002; // Custom model for double sneak
                break;
            case SWAP_HANDS:
                customModelData = 2003; // Custom model for swap hands
                break;
            case CLICK_ONLY:
                customModelData = 2004; // Custom model for click only
                break;
            default:
                customModelData = 2000; // Fallback
        }
        meta.setCustomModelData(customModelData);
        
        if (isActive) {
            meta.setDisplayName("§a§l✓ " + scheme.getDisplayName());
        } else {
            meta.setDisplayName("§f" + scheme.getDisplayName());
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + scheme.getDescription());
        lore.add("");
        
        for (String instruction : scheme.getInstructions()) {
            lore.add(instruction);
        }
        
        lore.add("");
        if (isActive) {
            lore.add("§a§l✓ CURRENTLY ACTIVE");
        } else {
            lore.add("§e§l▶ CLICK TO SELECT");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create toggle abilities button
     */
    private ItemStack createToggleButton(boolean enabled) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data based on enabled state
        meta.setCustomModelData(enabled ? 2005 : 2006); // 2005 = enabled, 2006 = disabled
        
        if (enabled) {
            meta.setDisplayName("§a§l✓ Abilities Enabled");
        } else {
            meta.setDisplayName("§c§l✗ Abilities Disabled");
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (enabled) {
            lore.add("§7You can use fragment abilities");
            lore.add("");
            lore.add("§c§l▶ CLICK TO DISABLE");
        } else {
            lore.add("§7Fragment abilities are disabled");
            lore.add("");
            lore.add("§a§l▶ CLICK TO ENABLE");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Fill border with glass panes
     */
    private void fillBorder(Inventory inv) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pane);
            inv.setItem(18 + i, pane);
        }
        
        // Side columns
        inv.setItem(9, pane);
        inv.setItem(17, pane);
    }
    
    /**
     * Handle inventory clicks
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if this is our GUI by title
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        
        // CANCEL IMMEDIATELY - prevents ALL item movement
        event.setCancelled(true);
        
        // Only process clicks in the GUI inventory, not player inventory
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        handleClick(player, clicked, event.getSlot());
    }
    
    /**
     * Handle click logic
     */
    private void handleClick(Player player, ItemStack item, int slot) {
        // Get ItemMeta and check for CustomModelData
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return; // Not a button we care about
        }

        int modelData = item.getItemMeta().getCustomModelData();

        // Handle toggle button click
        if (modelData == 2005 || modelData == 2006) {
            preferencesManager.toggleAbilities(player);
            openGUI(player); // Refresh GUI
            return;
        }

        // Handle control scheme selection
        Optional<ControlScheme> selectedScheme = getSchemeFromModelData(modelData);

        if (selectedScheme.isPresent()) {
            preferencesManager.setControlScheme(player, selectedScheme.get());
            openGUI(player); // Refresh GUI
        }
    }

    /**
     * Get ControlScheme from CustomModelData
     */
    private Optional<ControlScheme> getSchemeFromModelData(int modelData) {
        return switch (modelData) {
            case 2001 -> Optional.of(ControlScheme.SNEAK_CLICK);
            case 2002 -> Optional.of(ControlScheme.DOUBLE_SNEAK);
            case 2003 -> Optional.of(ControlScheme.SWAP_HANDS);
            case 2004 -> Optional.of(ControlScheme.CLICK_ONLY);
            default -> Optional.empty();
        };
    }
    
    /**
     * Handle inventory drag to prevent item dragging
     */
    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        // Check if this is our GUI
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        
        // Cancel ALL drag events
        event.setCancelled(true);
    }
    
    /**
     * Handle inventory close
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            openInventories.remove(player.getUniqueId());
        }
    }
}
