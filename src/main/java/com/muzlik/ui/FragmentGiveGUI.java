package com.muzlik.ui;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
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
 */
public class FragmentGiveGUI implements Listener {
    
    private final FragmentManager fragmentManager;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<UUID, Player> selectedPlayers = new HashMap<>();
    
    private static final String PLAYER_SELECT_TITLE = "§8§l⚡ sᴇʟᴇᴄᴛ ᴘʟᴀʏᴇʀ";
    private static final String FRAGMENT_SELECT_TITLE = "§8§l⚡ sᴇʟᴇᴄᴛ ꜰʀᴀɢᴍᴇɴᴛ";
    
    public FragmentGiveGUI(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
    
    /**
     * Open player selection GUI
     */
    public void openPlayerSelection(Player admin) {
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
            
            ItemStack playerHead = createPlayerHead(target);
            inv.setItem(slot, playerHead);
            slot++;
        }
        
        // Info button
        ItemStack infoButton = createInfoButton("§e§lSelect a Player", 
            "§7Click a player head to", 
            "§7select who receives the fragment");
        inv.setItem(49, infoButton);
        
        openInventories.put(admin.getUniqueId(), inv);
        admin.openInventory(inv);
    }
    
    /**
     * Open fragment selection GUI
     */
    public void openFragmentSelection(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, FRAGMENT_SELECT_TITLE);
        
        selectedPlayers.put(admin.getUniqueId(), target);
        
        // Fill border
        fillBorder(inv);
        
        // Add all fragment types
        int slot = 19;
        for (FragmentType type : FragmentType.values()) {
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
        
        openInventories.put(admin.getUniqueId(), inv);
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
     * Create fragment button
     */
    private ItemStack createFragmentButton(FragmentType type, Player target, boolean hasFragment) {
        Material material = getFragmentMaterial(type);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
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
     * Get material for fragment type
     */
    private Material getFragmentMaterial(FragmentType type) {
        switch (type) {
            case FIRE: return Material.FIRE_CHARGE;
            case WATER: return Material.HEART_OF_THE_SEA;
            case AIR: return Material.FEATHER;
            case DARK: return Material.WITHER_SKELETON_SKULL;
            case LIGHT: return Material.GLOWSTONE;
            case VOID: return Material.ENDER_PEARL;
            case DRAGON: return Material.DRAGON_HEAD;
            case STORM: return Material.LIGHTNING_ROD;
            default: return Material.BARRIER;
        }
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
        
        String title = event.getView().getTitle();
        
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
     * Handle player selection
     */
    private void handlePlayerSelection(Player admin, ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta.getOwningPlayer() == null) return;
        
        Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§c✗ Player is no longer online");
            admin.closeInventory();
            return;
        }
        
        openFragmentSelection(admin, target);
    }
    
    /**
     * Handle fragment selection
     */
    private void handleFragmentSelection(Player admin, ItemStack item) {
        String displayName = item.getItemMeta().getDisplayName();
        
        // Check for back button
        if (displayName.contains("Back")) {
            openPlayerSelection(admin);
            return;
        }
        
        // Check for fragment selection
        Player target = selectedPlayers.get(admin.getUniqueId());
        if (target == null || !target.isOnline()) {
            admin.sendMessage("§c✗ Target player is no longer online");
            admin.closeInventory();
            return;
        }
        
        for (FragmentType type : FragmentType.values()) {
            if (displayName.contains(type.getDisplayName())) {
                fragmentManager.grantFragment(target, type);
                admin.sendMessage("§a✓ Gave " + type.getDisplayName() + " Fragment to " + target.getName());
                admin.closeInventory();
                return;
            }
        }
    }
    
    /**
     * Handle inventory drag to prevent item dragging
     */
    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        String title = event.getView().getTitle();
        
        // Check if this is one of our GUIs
        if (!title.equals(PLAYER_SELECT_TITLE) && !title.equals(FRAGMENT_SELECT_TITLE)) return;
        
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
            UUID uuid = player.getUniqueId();
            openInventories.remove(uuid);
            
            // Clean up selected player if closing
            if (!event.getView().getTitle().equals(FRAGMENT_SELECT_TITLE)) {
                selectedPlayers.remove(uuid);
            }
        }
    }
}
