package com.muzlik.ui;

import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentDefinition;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityDefinition;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manages all UI screens and displays with enhanced visual effects.
 */
public class UIManager {
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final ManaManager manaManager;
    private final LevelManager levelManager;
    private final RankManager rankManager;
    private final CooldownManager cooldownManager;
    private final Map<UUID, UIMode> playerUIMode;

    public UIManager(JavaPlugin plugin, FragmentManager fragmentManager, 
                    ManaManager manaManager, LevelManager levelManager, RankManager rankManager,
                    CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
        this.cooldownManager = cooldownManager;
        this.playerUIMode = new HashMap<>();
    }

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
    
    /**
     * Open Enhanced Fragment Overview GUI with visual effects
     */
    public void openFragmentOverview(Player player) {
        // Play LEVEL_UP sound on GUI open (Requirement 4.5)
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("§8" + toSmallCaps("fragments"), NamedTextColor.DARK_GRAY));
        
        Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(player);
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        // Fill border with decorative glass panes
        fillBorder(inv);
        
        // Place Fragment icons in center area (slots 10-16, 19-25, 28-34)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        int slotIndex = 0;
        
        for (FragmentType type : FragmentType.values()) {
            if (slotIndex >= slots.length) break;
            
            boolean isOwned = ownedFragments.contains(type);
            boolean isActive = type.equals(activeFragment);
            
            // Use FragmentIconBuilder for enhanced icons
            ItemStack item = new FragmentIconBuilder(type, player, levelManager, rankManager, manaManager)
                    .owned(isOwned)
                    .active(isActive)
                    .build();
            
            inv.setItem(slots[slotIndex++], item);
        }
        
        // Add info button
        ItemStack infoButton = createInfoButton();
        inv.setItem(49, infoButton);
        
        player.openInventory(inv);
    }

    /**
     * Open Ability Detail View GUI for a specific Fragment
     */
    public void openAbilityDetails(Player player, FragmentType type) {
        // Play CLICK sound on Fragment selection (Requirement 4.5)
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        
        FragmentDefinition fragment = fragmentManager.getFragment(type);
        if (fragment == null) {
            player.sendMessage("§c✗ Fragment not found");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, 
                Component.text("§8" + toSmallCaps(type.getDisplayName() + " abilities")));
        
        // Fill border
        fillBorder(inv);
        
        // Fragment info icon (top center)
        ItemStack fragmentIcon = new FragmentIconBuilder(type, player, levelManager, rankManager, manaManager)
                .owned(fragmentManager.hasFragment(player, type))
                .active(type.equals(fragmentManager.getActiveFragment(player)))
                .build();
        inv.setItem(13, fragmentIcon);
        
        // Ability icons (center area)
        int[] abilitySlots = {19, 20, 21, 22, 23, 24, 25};
        int slotIndex = 0;
        
        for (AbilityDefinition ability : fragment.getAbilities()) {
            if (slotIndex >= abilitySlots.length) break;
            
            ItemStack abilityIcon = new AbilityIconBuilder(ability, player, type, 
                    rankManager, levelManager, cooldownManager)
                    .build();
            
            inv.setItem(abilitySlots[slotIndex++], abilityIcon);
        }
        
        // Back button
        ItemStack backButton = createBackButton();
        inv.setItem(45, backButton);
        
        player.openInventory(inv);
    }
    
    /**
     * Play error sound for locked ability click (Requirement 4.5)
     */
    public void playLockedAbilitySound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
    
    /**
     * Spawn particle burst at player location on Fragment switch (Requirement 4.5)
     */
    public void spawnFragmentSwitchParticles(Player player, FragmentType newFragment) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();
        
        // Spawn particle burst based on Fragment type
        Particle particleType = getFragmentParticle(newFragment);
        world.spawnParticle(particleType, loc, 30, 0.5, 0.5, 0.5, 0.1);
        
        // Play sound effect
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }
    
    /**
     * Show rank-up animation with cinematic effects (Requirement 6.2)
     */
    public void showRankUpAnimation(Player player, FragmentType type, int newRank) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();
        
        // Spawn dramatic particle burst
        Particle particleType = getFragmentParticle(type);
        world.spawnParticle(particleType, loc, 50, 0.5, 1.0, 0.5, 0.2);
        world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 30, 0.5, 1.0, 0.5, 0.1);
        
        // Play dramatic sound
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
        
        // Send title/subtitle
        player.sendTitle("§6§l✦ RANK UP ✦", "§e" + type.getDisplayName() + " §7→ §6Rank " + newRank, 
                10, 60, 20);
    }
    
    /**
     * Show ability unlock notification (Requirement 7.4)
     */
    public void showAbilityUnlockNotification(Player player, String abilityName) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();
        
        // Spawn particle burst
        world.spawnParticle(Particle.TOTEM, loc, 20, 0.3, 0.5, 0.3, 0.1);
        world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.5, 0.3, 0.05);
        
        // Play sound
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        
        // Send title/subtitle
        player.sendTitle("§a§lABILITY UNLOCKED", "§e" + abilityName, 10, 50, 20);
        
        // Send chat message
        player.sendMessage("§a§l✓ §eNew ability unlocked: §b" + abilityName);
    }
    
    /**
     * Fill border with decorative glass panes
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
     * Create info button
     */
    private ItemStack createInfoButton() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lFragment Information");
        meta.setLore(Arrays.asList(
                "§7Click on a Fragment to view",
                "§7detailed ability information",
                "",
                "§7Green checkmark §a✓ §7= Unlocked",
                "§7Red lock §c✗ §7= Locked",
                "§7Yellow warning §e⚠ §7= Nearly unlocked"
        ));
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
        meta.setLore(Arrays.asList("§7Return to Fragment overview"));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Get particle type for Fragment
     */
    private Particle getFragmentParticle(FragmentType type) {
        return switch (type) {
            case FIRE -> Particle.FLAME;
            case WATER -> Particle.WATER_SPLASH;
            case AIR -> Particle.CLOUD;
            case EARTH -> Particle.BLOCK_CRACK;
            case DARK -> Particle.SMOKE_LARGE;
            case LIGHT -> Particle.END_ROD;
            case VOID -> Particle.PORTAL;
            case MOB -> Particle.VILLAGER_HAPPY;
            case DRAGON -> Particle.DRAGON_BREATH;
            case STORM -> Particle.ELECTRIC_SPARK;
        };
    }

    /**
     * Open Mana Status GUI
     */
    public void openManaStatus(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text("Mana Status", NamedTextColor.AQUA));
        
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        double regenRate = manaManager.getManaRegenRate(player);
        
        ItemStack manaItem = new ItemStack(Material.LAPIS_LAZULI);
        ItemMeta meta = manaItem.getItemMeta();
        meta.setDisplayName("§b§lMana Status");
        meta.setLore(Arrays.asList(
            "§7Current: §b" + String.format("%.0f", currentMana),
            "§7Maximum: §b" + String.format("%.0f", maxMana),
            "§7Regen Rate: §b" + String.format("%.1f", regenRate) + " §7per second",
            "",
            "§7Percentage: §b" + String.format("%.0f", (currentMana / maxMana) * 100) + "%"
        ));
        manaItem.setItemMeta(meta);
        
        inv.setItem(4, manaItem);
        
        player.openInventory(inv);
    }

    /**
     * Set UI mode for player
     */
    public void setUIMode(Player player, UIMode mode) {
        playerUIMode.put(player.getUniqueId(), mode);
        player.sendMessage("§a✓ UI Mode: §b" + mode.getDisplayName());
    }

    /**
     * Get UI mode for player
     */
    public UIMode getUIMode(Player player) {
        return playerUIMode.getOrDefault(player.getUniqueId(), UIMode.STANDARD);
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        playerUIMode.remove(player.getUniqueId());
    }
}
