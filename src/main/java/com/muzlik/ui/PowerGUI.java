package com.muzlik.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import com.muzlik.power.PowerManager;
import com.muzlik.power.IPower;
import com.muzlik.power.IAbility;
import com.muzlik.cooldown.CooldownManager;
import java.util.*;

/**
 * Clean, minimalistic GUI for power management
 * Single chest interface (54 slots) for viewing and managing powers
 */
public class PowerGUI implements Listener {
    
    private final PowerManager powerManager;
    private final Map<String, Inventory> openInventories = new HashMap<>();
    
    private static final String GUI_TITLE = "§8ᴘᴏᴡᴇʀ ᴍᴀɴᴀɢᴇʀ";
    
    /**
     * Convert text to small caps unicode
     */
    private static String toSmallCaps(String text) {
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
        String normal = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toLowerCase().toCharArray()) {
            int index = normal.indexOf(c);
            if (index >= 0) {
                result.append(smallCaps.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    public PowerGUI(PowerManager powerManager) {
        this.powerManager = powerManager;
    }
    
    /**
     * Open power GUI for player
     */
    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        
        IPower activePower = powerManager.getPlayerActivePower(player);
        
        // Fill with glass panes (border)
        fillBorder(inv);
        
        if (activePower != null) {
            // Show active power info
            setupActivePowerView(inv, player, activePower);
        } else {
            // Show power selection
            setupPowerSelectionView(inv, player);
        }
        
        openInventories.put(player.getUniqueId().toString(), inv);
        player.openInventory(inv);
    }
    
    /**
     * Setup active power view
     */
    private void setupActivePowerView(Inventory inv, Player player, IPower power) {
        // Power icon (center top)
        ItemStack powerIcon = createPowerIcon(power);
        inv.setItem(13, powerIcon);
        
        // Ability slots (row 3) - Only 2 abilities
        for (int i = 0; i < 2; i++) {
            IAbility ability = power.getAbilityManager().getAbility(i);
            if (ability != null) {
                ItemStack abilityItem = createAbilityIcon(player, ability, i);
                inv.setItem(21 + (i * 2), abilityItem); // Centered: slots 21 and 23
            }
        }
        
        // Info panel (right side)
        ItemStack infoItem = createInfoIcon(player, power);
        inv.setItem(25, infoItem);
        
        // Action buttons (bottom row)
        inv.setItem(48, createButton(Material.RED_CONCRETE, "§c§lDeactivate Power", 
            "§7Click to deactivate", "§7your current power"));
        inv.setItem(49, createButton(Material.BOOK, "§e§lPower Info", 
            "§7View detailed", "§7power information"));
        inv.setItem(50, createButton(Material.BARRIER, "§c§lClose", 
            "§7Close this menu"));
    }
    
    /**
     * Setup power selection view
     */
    private void setupPowerSelectionView(Inventory inv, Player player) {
        // Title
        inv.setItem(13, createButton(Material.BARRIER, "§c§lNo Power Active", 
            "§7You don't have an", "§7active power"));
        
        // Available powers
        Collection<IPower> powers = powerManager.getAllPowers();
        int slot = 19;
        
        for (IPower power : powers) {
            if (slot >= 26) break;
            ItemStack powerItem = createPowerSelectionIcon(power);
            inv.setItem(slot, powerItem);
            slot++;
        }
        
        // Random power button
        inv.setItem(49, createButton(Material.ENDER_PEARL, "§d§lRandom Power", 
            "§7Click to get a", "§7random power"));
        
        // Close button
        inv.setItem(50, createButton(Material.BARRIER, "§c§lClose", 
            "§7Close this menu"));
    }
    
    /**
     * Create power icon
     */
    private ItemStack createPowerIcon(IPower power) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§l⚡ " + power.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + power.getTheme());
        lore.add("");
        lore.add("§8ID: §7" + power.getId());
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create ability icon
     */
    private ItemStack createAbilityIcon(Player player, IAbility ability, int slot) {
        Material material = getAbilityMaterial(slot);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String slotName = getSlotName(slot);
        meta.setDisplayName("§b§l" + slotName + ": §f" + ability.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + ability.getDescription());
        lore.add("");
        
        CooldownManager cooldownManager = powerManager.getCooldownManager();
        if (cooldownManager.isOnCooldown(player, ability.getId())) {
            double remaining = cooldownManager.getRemainingCooldownSeconds(player, ability.getId());
            lore.add("§c⏱ Cooldown: " + String.format("%.1f", remaining) + "s");
        } else {
            lore.add("§a✓ Ready");
        }
        
        lore.add("");
        lore.add("§8Cooldown: §7" + (ability.getCooldown() / 1000.0) + "s");
        lore.add("§8Slot: §7" + (slot + 1));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create info icon
     */
    private ItemStack createInfoIcon(Player player, IPower power) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lPower Information");
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Power: §b" + power.getDisplayName());
        lore.add("§7Theme: §f" + power.getTheme());
        lore.add("");
        int abilityCount = 0;
        for (int i = 0; i < 2; i++) { // Only 2 abilities
            if (power.getAbilityManager().getAbility(i) != null) {
                abilityCount++;
            }
        }
        lore.add("§7Abilities: §a" + abilityCount);
        lore.add("");
        lore.add("§8Use slots 1-2 while");
        lore.add("§8sneaking to activate");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create power selection icon
     */
    private ItemStack createPowerSelectionIcon(IPower power) {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§l" + power.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7" + power.getTheme());
        lore.add("");
        lore.add("§e§lClick to activate");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create button
     */
    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        
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
        String uuid = player.getUniqueId().toString();
        
        if (!openInventories.containsKey(uuid)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        handleClick(player, clicked);
    }
    
    /**
     * Handle click logic
     */
    private void handleClick(Player player, ItemStack item) {
        String displayName = item.getItemMeta().getDisplayName();
        
        if (displayName.contains("Close")) {
            player.closeInventory();
        } else if (displayName.contains("Deactivate")) {
            powerManager.deactivatePower(player);
            player.closeInventory();
        } else if (displayName.contains("Random Power")) {
            powerManager.assignRandomPower(player);
            player.closeInventory();
        } else if (item.getType() == Material.BLAZE_POWDER) {
            // Power selection
            for (IPower power : powerManager.getAllPowers()) {
                if (displayName.contains(power.getDisplayName())) {
                    powerManager.activatePower(player, power.getId());
                    player.closeInventory();
                    break;
                }
            }
        }
    }
    
    /**
     * Handle inventory close
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            openInventories.remove(player.getUniqueId().toString());
        }
    }
    
    /**
     * Get ability material
     */
    private Material getAbilityMaterial(int slot) {
        switch (slot) {
            case 0: return Material.FIRE_CHARGE;
            case 1: return Material.MAGMA_CREAM;
            case 2: return Material.BLAZE_ROD;
            default: return Material.BARRIER;
        }
    }
    
    /**
     * Get slot name
     */
    private String getSlotName(int slot) {
        switch (slot) {
            case 0: return "Primary";
            case 1: return "Secondary";
            default: return "Unknown";
        }
    }
}
