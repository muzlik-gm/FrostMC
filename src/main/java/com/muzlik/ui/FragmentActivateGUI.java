package com.muzlik.ui;

import com.muzlik.config.ConfigManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
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

import java.util.*;

/**
 * GUI for activating/switching fragments
 */
public class FragmentActivateGUI implements Listener {
    
    private final FragmentManager fragmentManager;
    private final LevelManager levelManager;
    private final RankManager rankManager;
    private final ConfigManager configManager;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    
    private static final Component GUI_TITLE = Component.text("⚡ ACTIVATE FRAGMENT")
            .color(NamedTextColor.DARK_GRAY)
            .decorate(TextDecoration.BOLD);
    
    public FragmentActivateGUI(FragmentManager fragmentManager, LevelManager levelManager, RankManager rankManager, ConfigManager configManager) {
        this.fragmentManager = fragmentManager;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
        this.configManager = configManager;
    }
    
    /**
     * Open fragment activation GUI
     */
    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        
        Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(player);
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        // Fill border
        fillBorder(inv);
        
        // Add owned and charged fragments
        Set<FragmentType> allFragments = new HashSet<>(ownedFragments);
        for (FragmentType type : FragmentType.values()) {
            if (fragmentManager.isCharged(player, type)) {
                allFragments.add(type);
            }
        }

        int slot = 19;
        for (FragmentType type : allFragments) {
            if (slot >= 26) {
                slot = 28; // Move to next row
            }
            if (slot >= 35) break; // Max 16 fragments (we have 10)
            
            boolean isActive = type.equals(activeFragment);
            ItemStack item = createFragmentButton(player, type, isActive);
            inv.setItem(slot, item);
            slot++;
        }
        
        // Info button
        ItemStack infoButton = createInfoButton(player);
        inv.setItem(49, infoButton);
        
        openInventories.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }
    
    /**
     * Create fragment button with custom model data
     */
    private ItemStack createFragmentButton(Player player, FragmentType type, boolean isActive) {
        Material material = getFragmentMaterial(type);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Add custom model data for resource pack textures
        int customModelData = getFragmentCustomModelData(type);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        int rank = rankManager.getRank(player, type);
        int level = levelManager.getLevel(player, type);
        
        if (isActive) {
            meta.setDisplayName("§a§l✓ " + type.getDisplayName() + " Fragment");
        } else {
            meta.setDisplayName("§f§l" + type.getDisplayName() + " Fragment");
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + type.getDescription());
        lore.add("");
        lore.add("§7Rank: §6" + rank + " §8/ §68");
        lore.add("§7Level: §e" + level + " §8/ §e50");
        lore.add("");
        
        if (isActive) {
            lore.add("§a§l✓ CURRENTLY ACTIVE");
        } else if (fragmentManager.isCharged(player, type)) {
            lore.add("§e§l▶ CLICK TO ACTIVATE");
        } else {
            // Check cooldown
            if (configManager.isFragmentChangerRequired() && !fragmentManager.canSwitchFragment(player)) {
                long cooldown = fragmentManager.getFragmentSwitchCooldown(player);
                long minutes = cooldown / 60000;
                long seconds = (cooldown % 60000) / 1000;
                lore.add("§c§l✗ COOLDOWN: " + minutes + "m " + seconds + "s");
                lore.add("§8Use Fragment Changer ritual");
            } else {
                lore.add("§e§l▶ CLICK TO ACTIVATE");
            }
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create info button
     */
    private ItemStack createInfoButton(Player player) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lFragment Switching");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Click a fragment to activate it");
        lore.add("");
        
        if (fragmentManager.canSwitchFragment(player)) {
            lore.add("§a✓ Ready to switch");
        } else {
            long cooldown = fragmentManager.getFragmentSwitchCooldown(player);
            long minutes = cooldown / 60000;
            long seconds = (cooldown % 60000) / 1000;
            lore.add("§c⏱ Cooldown: " + minutes + "m " + seconds + "s");
            lore.add("");
            lore.add("§7Complete a §dFragment Changer");
            lore.add("§7ritual to switch immediately");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Get material for fragment type
     */
    private Material getFragmentMaterial(FragmentType type) {
        // Use PAPER as base material for custom model data
        return Material.PAPER;
    }
    
    /**
     * Get custom model data for fragment type (for resource pack)
     */
    private int getFragmentCustomModelData(FragmentType type) {
        return switch (type) {
            case FIRE -> 1000;      // fragment_fire.png
            case WATER -> 1001;     // fragment_water.png
            case AIR -> 1002;       // fragment_air.png
            case DARK -> 1003;      // fragment_dark.png
            case LIGHT -> 1004;     // fragment_light.png
            case VOID -> 1005;      // fragment_void.png
            case DRAGON -> 1006;    // fragment_dragon.png
            case STORM -> 1007;     // fragment_storm.png
            case TIME -> 1008;      // fragment_time.png
            case LUCK -> 1009;      // fragment_luck.png
            case ADMIN -> 1099;     // fragment_admin.png (hidden)
        };
    }
    
    /**
     * Fill border with glass panes
     */
    private void fillBorder(Inventory inv) {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
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
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if this is our GUI by title
        if (!event.getView().title().equals(GUI_TITLE)) return;
        
        // CANCEL IMMEDIATELY - prevents ALL item movement
        event.setCancelled(true);
        
        // Only process clicks in the GUI inventory, not player inventory
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        handleClick(player, clicked);
    }
    
    /**
     * Handle click logic
     */
    private void handleClick(Player player, ItemStack item) {
        String displayName = item.getItemMeta().getDisplayName();
        
        // Check if it's a fragment button
        for (FragmentType type : FragmentType.values()) {
            if (displayName.contains(type.getDisplayName())) {
                // Check if already active
                if (type.equals(fragmentManager.getActiveFragment(player))) {
                    player.sendMessage("§e⚡ This fragment is already active");
                    return;
                }
                
                if (fragmentManager.isCharged(player, type)) {
                    if (!configManager.isFragmentChangerRequired()) {
                        fragmentManager.activateChargedFragment(player, type);
                    } else {
                        player.sendMessage("§c✗ You need a Fragment Changer to activate this Fragment");
                    }
                    player.closeInventory();
                    return;
                }

                // Check cooldown
                if (configManager.isFragmentChangerRequired() && !fragmentManager.canSwitchFragment(player)) {
                    long cooldown = fragmentManager.getFragmentSwitchCooldown(player);
                    long minutes = cooldown / 60000;
                    long seconds = (cooldown % 60000) / 1000;
                    player.sendMessage("§c✗ Fragment switch cooldown: " + minutes + "m " + seconds + "s");
                    player.sendMessage("§7Complete a Fragment Changer ritual to switch immediately");
                    player.closeInventory();
                    return;
                }
                
                // Activate fragment
                fragmentManager.setActiveFragment(player, type);
                fragmentManager.recordFragmentSwitch(player);
                player.closeInventory();
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
        
        // Check if this is our GUI
        if (!event.getView().title().equals(GUI_TITLE)) return;
        
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
