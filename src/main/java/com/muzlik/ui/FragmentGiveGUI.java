package com.muzlik.ui;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.texture.TextureRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUI for giving fragments to players (admin)
 * Two-stage: First select player, then select fragment
 * FIXED: Player selection and online status issues
 */
public class FragmentGiveGUI implements Listener {
    
    private final FragmentManager fragmentManager;
    private final Map<UUID, String> selectedPlayerNames = new HashMap<>(); // Store player names instead of UUIDs to avoid stale references
    
    private static final Component PLAYER_SELECT_TITLE = Component.text("⚡ SELECT PLAYER")
            .color(NamedTextColor.DARK_GRAY)
            .decorate(TextDecoration.BOLD);
    private static final Component FRAGMENT_SELECT_TITLE = Component.text("⚡ SELECT FRAGMENT")
            .color(NamedTextColor.DARK_GRAY)
            .decorate(TextDecoration.BOLD);
    
    public FragmentGiveGUI(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
    
    /**
     * Open player selection GUI
     */
    public void openPlayerSelection(Player admin) {
        // Clear any previous selection
        selectedPlayerNames.remove(admin.getUniqueId());
        
        Inventory inv = Bukkit.createInventory(null, 54, PLAYER_SELECT_TITLE);
        
        // Fill border
        fillBorder(inv);
        
        // Add online players
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int slot = 10;
        
        for (Player target : onlinePlayers) {
            if (slot >= 44) break; // Max slots
            
            // Skip border slots
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                continue;
            }
            
            // Skip the admin themselves
            if (target.getUniqueId().equals(admin.getUniqueId())) {
                continue;
            }
            
            ItemStack playerHead = createPlayerHead(target);
            inv.setItem(slot, playerHead);
            slot++;
        }
        
        // Info button
        ItemStack infoButton = createInfoButton("§e§lSelect a Player", 
            "§7Click a player head to", 
            "§7select who receives the fragment");
        inv.setItem(49, infoButton);
        
        admin.openInventory(inv);
    }
    
    /**
     * Open fragment selection GUI
     */
    public void openFragmentSelection(Player admin, String targetPlayerName) {
        // Verify target is still online using fresh lookup
        Player target = Bukkit.getPlayerExact(targetPlayerName);
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§c✗ Player '" + targetPlayerName + "' is no longer online");
            selectedPlayerNames.remove(admin.getUniqueId());
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, FRAGMENT_SELECT_TITLE);
        
        // Store the target player's name for reliable lookup
        selectedPlayerNames.put(admin.getUniqueId(), targetPlayerName);
        
        // Fill border
        fillBorder(inv);
        
        // Add all fragment types (skip ADMIN fragment)
        int slot = 19;
        for (FragmentType type : FragmentType.values()) {
            if (type == FragmentType.ADMIN) continue; // Skip admin fragment
            
            boolean hasFragment = fragmentManager.hasFragment(target, type);
            ItemStack item = createFragmentButton(type, target, hasFragment);
            inv.setItem(slot, item);
            slot++;
            
            if (slot == 26) slot = 28; // Skip to next row
        }
        
        // Info button
        ItemStack infoButton = createInfoButton("§e§lGiving to: §f" + target.getName(), 
            "§7Click a fragment to give it", 
            "§7to " + target.getName());
        inv.setItem(49, infoButton);
        
        // Back button
        ItemStack backButton = createBackButton();
        inv.setItem(48, backButton);
        
        admin.openInventory(inv);
    }
    
    /**
     * Create player head
     */
    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        meta.setOwningPlayer(player);
        meta.setDisplayName("§f§l" + player.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        
        Collection<FragmentType> fragments = fragmentManager.getPlayerFragments(player);
        if (fragments.isEmpty()) {
            lore.add("§7No fragments");
        } else {
            lore.add("§7Fragments: §e" + fragments.size());
            for (FragmentType type : fragments) {
                lore.add("  §8• §7" + type.getDisplayName());
            }
        }
        
        lore.add("");
        lore.add("§e§l▶ CLICK TO SELECT");
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
    
    /**
     * Create fragment button using TextureRegistry (new system)
     */
    private ItemStack createFragmentButton(FragmentType type, Player target, boolean hasFragment) {
        // Use TextureRegistry for consistent textures across the plugin
        ItemStack item = new ItemStack(TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data from TextureRegistry
        meta.setCustomModelData(TextureRegistry.getFragmentTexture(type));
        meta.setDisplayName("§f§l" + type.getDisplayName() + " Fragment");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + type.getDescription());
        lore.add("");
        
        if (hasFragment) {
            lore.add("§a✓ " + target.getName() + " already has this");
            lore.add("");
            lore.add("§e§l▶ CLICK TO GIVE ANYWAY");
        } else {
            lore.add("§7" + target.getName() + " doesn't have this");
            lore.add("");
            lore.add("§e§l▶ CLICK TO GIVE");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create info button
     */
    private ItemStack createInfoButton(String title, String... lore) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        
        List<String> loreList = new ArrayList<>();
        loreList.add("");
        loreList.addAll(Arrays.asList(lore));
        
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create back button
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§l← Back");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Return to player selection");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Fill border with glass panes
     */
    private void fillBorder(Inventory inv) {
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pane);
            inv.setItem(45 + i, pane);
        }
        
        // Side columns
        for (int i = 1; i < 5; i++) {
            inv.setItem(i * 9, pane);
            inv.setItem(i * 9 + 8, pane);
        }
    }
    
    /**
     * Handle inventory clicks
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player admin = (Player) event.getWhoClicked();
        
        Component title = event.getView().title();
        
        // Check if this is one of our GUIs
        if (!title.equals(PLAYER_SELECT_TITLE) && !title.equals(FRAGMENT_SELECT_TITLE)) return;
        
        // CANCEL IMMEDIATELY - prevents ALL item movement
        event.setCancelled(true);
        
        // Only process clicks in the GUI inventory, not player inventory
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (title.equals(PLAYER_SELECT_TITLE)) {
            handlePlayerSelection(admin, clicked);
        } else if (title.equals(FRAGMENT_SELECT_TITLE)) {
            handleFragmentSelection(admin, clicked);
        }
    }
    
    /**
     * Handle player selection - FIXED to avoid stale references
     */
    private void handlePlayerSelection(Player admin, ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;
        
        // Get player name from the skull meta display name (more reliable)
        String displayName = meta.getDisplayName();
        if (displayName == null || displayName.isEmpty()) return;
        
        // Extract player name from display name (remove formatting)
        String playerName = displayName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Verify player is still online using exact name lookup
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§c✗ Player '" + playerName + "' is no longer online");
            return;
        }
        
        // Open fragment selection with the verified player name
        openFragmentSelection(admin, target.getName());
    }
    
    /**
     * Handle fragment selection - FIXED to use reliable player lookup
     */
    private void handleFragmentSelection(Player admin, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String displayName = meta.getDisplayName();
        
        // Check for back button
        if (displayName.contains("Back")) {
            selectedPlayerNames.remove(admin.getUniqueId()); // Clear selection when going back
            openPlayerSelection(admin);
            return;
        }
        
        // Get stored target player name
        String targetPlayerName = selectedPlayerNames.get(admin.getUniqueId());
        if (targetPlayerName == null || targetPlayerName.isEmpty()) {
            admin.sendMessage("§c✗ No player selected. Please try again.");
            admin.closeInventory();
            return;
        }
        
        // Get fresh player reference using exact name lookup
        Player target = Bukkit.getPlayerExact(targetPlayerName);
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§c✗ Target player '" + targetPlayerName + "' is no longer online");
            selectedPlayerNames.remove(admin.getUniqueId());
            admin.closeInventory();
            return;
        }
        
        // Check for fragment selection
        for (FragmentType type : FragmentType.values()) {
            if (displayName.contains(type.getDisplayName())) {
                try {
                    fragmentManager.grantFragment(target, type);
                    admin.sendMessage("§a✓ Gave " + type.getDisplayName() + " Fragment to " + target.getName());
                    target.sendMessage("§a✓ You received the " + type.getDisplayName() + " Fragment!");
                    selectedPlayerNames.remove(admin.getUniqueId()); // Clear selection after giving
                    admin.closeInventory();
                    return;
                } catch (Exception e) {
                    admin.sendMessage("§c✗ Failed to give fragment: " + e.getMessage());
                    return;
                }
            }
        }
        
        // If we get here, no fragment was matched
        admin.sendMessage("§c✗ Could not identify the selected fragment. Please try again.");
    }
    
    /**
     * Handle inventory drag to prevent item dragging
     */
    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Component title = event.getView().title();
        
        // Check if this is one of our GUIs
        if (!title.equals(PLAYER_SELECT_TITLE) && !title.equals(FRAGMENT_SELECT_TITLE)) return;
        
        // Cancel ALL drag events
        event.setCancelled(true);
    }
    
    /**
     * Handle inventory close - FIXED to properly clean up
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            UUID uuid = player.getUniqueId();
            
            Component title = event.getView().title();
            
            // Only clear selection if closing the player selection GUI
            // Keep selection when moving from player selection to fragment selection
            if (title.equals(PLAYER_SELECT_TITLE)) {
                // Don't clear selection here - let it persist for fragment selection
            } else if (title.equals(FRAGMENT_SELECT_TITLE)) {
                // Clear selection when closing fragment selection
                selectedPlayerNames.remove(uuid);
            }
        }
    }
}
