package com.muzlik.ui;

import com.muzlik.config.ConfigManager;
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
import com.muzlik.util.Typography;

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
    private final ConfigManager configManager;
    private final Map<UUID, UIMode> playerUIMode;
    
    // New GUI instances
    private ControlSchemeGUI controlSchemeGUI;
    private FragmentActivateGUI fragmentActivateGUI;
    private FragmentGiveGUI fragmentGiveGUI;

    public UIManager(JavaPlugin plugin, FragmentManager fragmentManager,
                    ManaManager manaManager, LevelManager levelManager, RankManager rankManager,
                    CooldownManager cooldownManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
        this.cooldownManager = cooldownManager;
        this.configManager = configManager;
        this.playerUIMode = new HashMap<>();
    }
    
    /**
     * Initialize GUI instances (call after preferences manager is available)
     */
    public void initializeGUIs(com.muzlik.player.PlayerPreferencesManager preferencesManager) {
        this.controlSchemeGUI = new ControlSchemeGUI(preferencesManager);
        this.fragmentActivateGUI = new FragmentActivateGUI(fragmentManager, levelManager, rankManager, configManager);
        this.fragmentGiveGUI = new FragmentGiveGUI(fragmentManager);
        
        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(controlSchemeGUI, plugin);
        plugin.getServer().getPluginManager().registerEvents(fragmentActivateGUI, plugin);
        plugin.getServer().getPluginManager().registerEvents(fragmentGiveGUI, plugin);
    }
    
    /**
     * Open control scheme selection GUI
     */
    public void openControlSchemeGUI(Player player) {
        if (controlSchemeGUI != null) {
            controlSchemeGUI.openGUI(player);
        } else {
            player.sendMessage("§cControl scheme GUI not initialized");
        }
    }
    
    /**
     * Open fragment activation GUI
     */
    public void openFragmentActivateGUI(Player player) {
        if (fragmentActivateGUI != null) {
            fragmentActivateGUI.openGUI(player);
        } else {
            player.sendMessage("§cFragment activation GUI not initialized");
        }
    }
    
    /**
     * Open fragment give GUI (admin)
     */
    public void openFragmentGiveGUI(Player player) {
        if (fragmentGiveGUI != null) {
            fragmentGiveGUI.openPlayerSelection(player);
        } else {
            player.sendMessage("§cFragment give GUI not initialized");
        }
    }

    /**
     * Check if mana system is enabled
     */
    private boolean isManaSystemEnabled() {
        if (plugin instanceof com.muzlik.FrostSMPPlugin) {
            com.muzlik.FrostSMPPlugin frostPlugin = (com.muzlik.FrostSMPPlugin) plugin;
            com.muzlik.config.ConfigManager configManager = frostPlugin.getConfigManager();
            if (configManager != null) {
                return configManager.isManaSystemEnabled();
            }
        }
        return true; // Default to enabled if can't check
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
        
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(Typography.formatTitle("fragments")));
        
        Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(player);
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        // Fill border with decorative glass panes
        fillBorder(inv);
        
        // Place Fragment icons in center area (slots 10-16, 19-25, 28-34)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        int slotIndex = 0;
        
        for (FragmentType type : FragmentType.values()) {
            // HIDE ADMIN FRAGMENT FROM GUI
            if (type == FragmentType.ADMIN) continue;
            
            if (slotIndex >= slots.length) break;
            
            boolean isOwned = ownedFragments.contains(type);
            boolean isActive = type.equals(activeFragment);
            boolean isCharged = fragmentManager.isCharged(player, type);
            
            // Use FragmentIconBuilder for enhanced icons with charged state
            ItemStack item = new FragmentIconBuilder(type, player, levelManager, rankManager, manaManager)
                    .fragmentManager(fragmentManager)
                    .owned(isOwned)
                    .active(isActive)
                    .charged(isCharged)
                    .build();
            
            inv.setItem(slots[slotIndex++], item);
        }
        
        // Bottom row buttons (slots 45-53)
        // Slot 47: Activate Fragment button
        ItemStack activateButton = createActivateFragmentButton();
        inv.setItem(47, activateButton);
        
        // Slot 48: Controls button
        ItemStack controlsButton = createControlsButton();
        inv.setItem(48, controlsButton);
        
        // Slot 49: Info button (center)
        ItemStack infoButton = createInfoButton();
        inv.setItem(49, infoButton);
        
        // Slot 50: Mana Status button (only if mana system enabled)
        if (isManaSystemEnabled()) {
            ItemStack manaButton = createManaStatusButton(player);
            inv.setItem(50, manaButton);
        }
        
        // Slot 51: Admin Give button (if admin)
        if (player.hasPermission("fragment.admin")) {
            ItemStack giveButton = createAdminGiveButton();
            inv.setItem(51, giveButton);
        }
        
        player.openInventory(inv);
    }

    /**
     * Open Ability Detail View GUI for a specific Fragment
     * Enhanced layout with better spacing and typography
     */
    public void openAbilityDetails(Player player, FragmentType type) {
        // Play CLICK sound on Fragment selection
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        
        FragmentDefinition fragment = fragmentManager.getFragment(type);
        if (fragment == null) {
            player.sendMessage(Typography.formatError("Fragment not found"));
            return;
        }
        
        // Create inventory with uppercase title using Typography
        String title = type.getDisplayName().toUpperCase() + " ABILITIES";
        Inventory inv = Bukkit.createInventory(null, 54, 
                Component.text(Typography.formatMenuTitle(title)));
        
        // Fill border with decorative glass
        fillBorder(inv);
        
        // ═══════════════════════════════════════════════════════════
        // TOP AREA: Fragment icon + Stats panels
        // ═══════════════════════════════════════════════════════════
        
        // Fragment icon (slot 4 - top center)
        ItemStack fragmentIcon = new FragmentIconBuilder(type, player, levelManager, rankManager, manaManager)
                .fragmentManager(fragmentManager)
                .owned(fragmentManager.hasFragment(player, type))
                .active(type.equals(fragmentManager.getActiveFragment(player)))
                .charged(fragmentManager.isCharged(player, type))
                .build();
        inv.setItem(4, fragmentIcon);
        
        // Stats panel (slot 2)
        inv.setItem(2, createStatsPanel(player, type));
        
        // Level bonus panel (slot 6)
        inv.setItem(6, createLevelBonusPanel(player, type));
        
        // ═══════════════════════════════════════════════════════════
        // MIDDLE AREA: Abilities in a clear row
        // ═══════════════════════════════════════════════════════════
        
        // Get abilities
        java.util.List<AbilityDefinition> abilities = fragment.getAbilities();
        
        // Use middle row (row 3) centered - slots 19-25
        // For 5 abilities: 19, 20, 21, 22, 23 or spread: 19, 21, 22, 23, 25
        int numAbilities = Math.min(abilities.size(), 5);
        
        // Calculate starting slot for centered layout
        int[] abilitySlots;
        if (numAbilities <= 3) {
            // Center 3 abilities: 20, 22, 24
            abilitySlots = new int[]{20, 22, 24};
        } else if (numAbilities == 4) {
            // 4 abilities: 19, 21, 23, 25
            abilitySlots = new int[]{19, 21, 23, 25};
        } else {
            // 5 abilities: 19, 20, 22, 24, 25
            abilitySlots = new int[]{19, 20, 22, 24, 25};
        }
        
        int slotIndex = 0;
        for (AbilityDefinition ability : abilities) {
            if (slotIndex >= abilitySlots.length) break;
            
            ItemStack abilityIcon = new AbilityIconBuilder(ability, player, type, 
                    rankManager, levelManager, manaManager, cooldownManager)
                    .build();
            
            inv.setItem(abilitySlots[slotIndex++], abilityIcon);
        }
        
        // ═══════════════════════════════════════════════════════════
        // BOTTOM: Back button
        // ═══════════════════════════════════════════════════════════
        
        ItemStack backButton = createBackButton();
        inv.setItem(49, backButton); // Bottom center
        
        player.openInventory(inv);
    }
    
    /**
     * Create stats panel for ability view
     */
    private ItemStack createStatsPanel(Player player, FragmentType type) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§l" + toSmallCaps("Stats"));
        
        int rank = rankManager.getRank(player, type);
        int maxRank = rankManager.getMaxRank(type);
        int level = levelManager.getLevel(player, type);
        int maxLevel = levelManager.getMaxLevel(type);
        double xp = levelManager.getXP(player, type);
        double xpReq = levelManager.getXPForNextLevel(player, type);
        
        meta.setLore(Arrays.asList(
                "",
                "§7ʀᴀɴᴋ §f" + rank + "§8/§f" + maxRank,
                "§7ʟᴇᴠᴇʟ §f" + level + "§8/§f" + maxLevel,
                "",
                "§7xᴘ §f" + String.format("%.0f", xp) + "§8/§f" + String.format("%.0f", xpReq),
                ""
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create level bonus panel for ability view
     */
    private ItemStack createLevelBonusPanel(Player player, FragmentType type) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§l" + toSmallCaps("Level Bonuses"));
        
        double cdReduction = levelManager.getCooldownReduction(player, type) * 100;
        double manaReduction = levelManager.getManaCostReduction(player, type) * 100;
        double dmgBonus = levelManager.getDamageBonus(player, type) * 100;
        
        meta.setLore(Arrays.asList(
                "",
                "§7⏱ ᴄᴏᴏʟᴅᴏᴡɴ §a-" + String.format("%.0f", cdReduction) + "%",
                "§7⚡ ᴍᴀɴᴀ ᴄᴏsᴛ §a-" + String.format("%.0f", manaReduction) + "%",
                "§7⚔ ᴅᴀᴍᴀɢᴇ §a+" + String.format("%.0f", dmgBonus) + "%",
                "",
                "§8ʟᴇᴠᴇʟ ᴜᴘ ᴛᴏ ɪɴᴄʀᴇᴀsᴇ!"
        ));
        
        item.setItemMeta(meta);
        return item;
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
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1005); // Custom model for info icon
        meta.setDisplayName(Typography.formatTitle("Fragment Guide"));
        meta.setLore(Arrays.asList(
                "",
                Typography.formatLabel("Left-Click ") + Typography.SYMBOL_ARROW + Typography.formatValue(" View Abilities"),
                Typography.formatLabel("Right-Click ") + Typography.SYMBOL_ARROW + Typography.formatValue(" Activate"),
                "",
                Typography.COLOR_TEXT + "§m                    ",
                "",
                Typography.COLOR_SUCCESS + Typography.SYMBOL_CHECK + " Activated " + Typography.COLOR_TEXT_DARK + "- Currently using",
                Typography.COLOR_HIGHLIGHT + Typography.SYMBOL_DOT + " Owned " + Typography.COLOR_TEXT_DARK + "- Can switch to",
                Typography.COLOR_ACCENT + Typography.SYMBOL_LIGHTNING + " Charged " + Typography.COLOR_TEXT_DARK + "- Ready to activate",
                Typography.COLOR_TEXT_DARK + Typography.SYMBOL_CROSS + " Locked " + Typography.COLOR_TEXT_DARK + "- Need ritual"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create back button
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1006); // Custom model for back arrow icon
        meta.setDisplayName(Typography.formatTitle("Back"));
        meta.setLore(Arrays.asList(Typography.COLOR_TEXT + "Return to Fragment overview"));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create controls button
     */
    private ItemStack createControlsButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1001); // Custom model for controls icon
        meta.setDisplayName("§e§l⚙ " + toSmallCaps("Controls"));
        meta.setLore(Arrays.asList(
                "",
                Typography.COLOR_TEXT + "Change control scheme",
                Typography.COLOR_TEXT + "and ability settings",
                "",
                Typography.COLOR_HIGHLIGHT + "§l▶ CLICK TO OPEN"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create activate fragment button
     */
    private ItemStack createActivateFragmentButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1002); // Custom model for switch fragment icon
        meta.setDisplayName("§b§l⚡ " + toSmallCaps("Switch Fragment"));
        meta.setLore(Arrays.asList(
                "",
                Typography.COLOR_TEXT + "Activate a different",
                Typography.COLOR_TEXT + "fragment from your collection",
                "",
                Typography.COLOR_HIGHLIGHT + "§l▶ CLICK TO OPEN"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create mana status button
     */
    private ItemStack createManaStatusButton(Player player) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1003); // Custom model for mana status icon
        meta.setDisplayName("§b§l⚡ " + toSmallCaps("Mana Status"));
        
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        double regenRate = manaManager.getManaRegenRate(player);
        
        meta.setLore(Arrays.asList(
                "",
                Typography.COLOR_TEXT + "Current: §b" + String.format("%.0f", currentMana),
                Typography.COLOR_TEXT + "Maximum: §b" + String.format("%.0f", maxMana),
                Typography.COLOR_TEXT + "Regen: §b" + String.format("%.1f", regenRate) + "/s",
                "",
                Typography.COLOR_HIGHLIGHT + "§l▶ CLICK TO VIEW"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create admin give button
     */
    private ItemStack createAdminGiveButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1004); // Custom model for admin give icon
        meta.setDisplayName("§d§l⚡ " + toSmallCaps("Give Fragment"));
        meta.setLore(Arrays.asList(
                "",
                Typography.COLOR_TEXT + "§dAdmin: §7Give fragments",
                Typography.COLOR_TEXT + "to other players",
                "",
                Typography.COLOR_HIGHLIGHT + "§l▶ CLICK TO OPEN"
        ));
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
            case DARK -> Particle.SMOKE_LARGE;
            case LIGHT -> Particle.END_ROD;
            case VOID -> Particle.PORTAL;
            case DRAGON -> Particle.DRAGON_BREATH;
            case STORM -> Particle.ELECTRIC_SPARK;
            case TIME -> Particle.END_ROD; // Yellow particles for time
            case LUCK -> Particle.VILLAGER_HAPPY; // Green particles for luck
            case ADMIN -> Particle.SMOKE_LARGE; // Admin fragment particle
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
