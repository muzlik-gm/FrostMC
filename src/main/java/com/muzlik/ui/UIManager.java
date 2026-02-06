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
     * Open Enhanced Fragment Overview GUI with visual effects
     */
    public void openFragmentOverview(Player player) {
        // Play LEVEL_UP sound on GUI open (Requirement 4.5)
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(Typography.formatTitle("fragments")));
        
        populateFragmentOverview(inv, player);

        player.openInventory(inv);
    }

    /**
     * Refreshes the Fragment Overview GUI without closing and reopening it.
     */
    public void refreshFragmentOverview(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();

        // Ensure it's the correct GUI before refreshing
        String title = player.getOpenInventory().getTitle();
        if (!title.contains("ꜰʀᴀɢᴍᴇɴᴛs") && !title.contains("Fragment Overview")) {
            return; // Not our GUI, do nothing
        }

        inv.clear();
        populateFragmentOverview(inv, player);
        player.updateInventory(); // Not strictly necessary but good practice
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.5f);
    }

    /**
     * Populates the Fragment Overview GUI with icons and buttons.
     * This is used for both creating and refreshing the GUI.
     */
    private void populateFragmentOverview(Inventory inv, Player player) {
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
        // Slot 48: Controls button
        ItemStack controlsButton = createControlsButton();
        inv.setItem(48, controlsButton);
        
        // Slot 49: Info button (center)
        ItemStack infoButton = createInfoButton();
        inv.setItem(49, infoButton);
        
        // Slot 50: Mana Status button (only if mana system enabled)
        if (isManaSystemEnabled()) {
            ItemStack manaButton = createManaStatusButton(player, false);
            inv.setItem(50, manaButton);
        }
        
        // Slot 51: Admin Give button (if admin)
        if (player.hasPermission("fragment.admin")) {
            ItemStack giveButton = createAdminGiveButton();
            inv.setItem(51, giveButton);
        }

        // Slot 53: Close button
        inv.setItem(53, createCloseButton());
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
        
        // Passive effects panel (slot 3)
        inv.setItem(3, createPassiveEffectsPanel(type));
        
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

        // Slot 50: Mana Status button (only if mana system enabled)
        if (isManaSystemEnabled()) {
            inv.setItem(50, createManaStatusButton(player, false));
        }

        // Slot 53: Close button
        inv.setItem(53, createCloseButton());
        
        player.openInventory(inv);
    }
    
    /**
     * Create stats panel for ability view
     */
    private ItemStack createStatsPanel(Player player, FragmentType type) {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_stats"));
        meta.setDisplayName(Typography.COLOR_SECONDARY + "§l" + Typography.toSmallCaps("Stats"));
        
        int rank = rankManager.getRank(player, type);
        int maxRank = rankManager.getMaxRank(type);
        int level = levelManager.getLevel(player, type);
        int maxLevel = levelManager.getMaxLevel(type);
        double xp = levelManager.getXP(player, type);
        double xpReq = levelManager.getXPForNextLevel(player, type);
        
        meta.setLore(Arrays.asList(
                "",
                Typography.formatLabel("Rank: ") + Typography.formatValue(rank + "/" + maxRank),
                Typography.formatLabel("Level: ") + Typography.formatValue(level + "/" + maxLevel),
                "",
                Typography.formatProgressBar(xp, xpReq, 10),
                Typography.formatLabel("XP: ") + Typography.formatValue(String.format("%.0f/%.0f", xp, xpReq)),
                ""
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create close button
     */
    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_close_button"));
        meta.setDisplayName(Typography.formatError("Close"));
        meta.setLore(Arrays.asList(Typography.COLOR_TEXT + "Exit the menu"));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create level bonus panel for ability view
     */
    private ItemStack createLevelBonusPanel(Player player, FragmentType type) {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_bonus"));
        meta.setDisplayName(Typography.COLOR_PRIMARY + "§l" + Typography.toSmallCaps("Level Bonuses"));
        
        double cdReduction = levelManager.getCooldownReduction(player, type) * 100;
        double manaReduction = levelManager.getManaCostReduction(player, type) * 100;
        double dmgBonus = levelManager.getDamageBonus(player, type) * 100;
        
        meta.setLore(Arrays.asList(
                "",
                Typography.COLOR_TEXT + Typography.SYMBOL_COOLDOWN + " " + Typography.toSmallCaps("Cooldown") + " " + Typography.COLOR_SUCCESS + "-" + String.format("%.0f", cdReduction) + "%",
                Typography.COLOR_TEXT + Typography.SYMBOL_LIGHTNING + " " + Typography.toSmallCaps("Mana Cost") + " " + Typography.COLOR_SUCCESS + "-" + String.format("%.0f", manaReduction) + "%",
                Typography.COLOR_TEXT + Typography.SYMBOL_SWORD + " " + Typography.toSmallCaps("Damage") + " " + Typography.COLOR_SUCCESS + "+" + String.format("%.0f", dmgBonus) + "%",
                "",
                Typography.COLOR_TEXT_DARK + Typography.toSmallCaps("Level up to increase!")
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create passive effects panel showing fragment-specific passive abilities
     */
    private ItemStack createPassiveEffectsPanel(FragmentType type) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Typography.COLOR_ACCENT + "§l" + Typography.toSmallCaps("Passive Effects"));
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("");
        lore.add("§7ᴀʟᴡᴀʏs ᴀᴄᴛɪᴠᴇ ᴡʜɪʟᴇ ᴛʜɪs");
        lore.add("§7ꜰʀᴀɢᴍᴇɴᴛ ɪs ᴇQᴜɪᴘᴘᴇᴅ:");
        lore.add("");
        
        switch (type) {
            case FIRE:
                lore.add("§c🔥 ꜰɪʀᴇ ʀᴇsɪsᴛᴀɴᴄᴇ");
                lore.add("§7ɪᴍᴍᴜɴᴇ ᴛᴏ ꜰɪʀᴇ ᴅᴀᴍᴀɢᴇ");
                break;
            case WATER:
                lore.add("§b💧 ᴡᴀᴛᴇʀ ʙʀᴇᴀᴛʜɪɴɢ");
                lore.add("§7ʙʀᴇᴀᴛʜᴇ ᴜɴᴅᴇʀᴡᴀᴛᴇʀ");
                break;
            case AIR:
                lore.add("§f🪶 sʟᴏᴡ ꜰᴀʟʟɪɴɢ");
                lore.add("§7ᴀᴄᴛɪᴠᴀᴛᴇ ʙʏ sɴᴇᴀᴋɪɴɢ");
                break;
            case DARK:
                lore.add("§8👁 ɪɴᴠɪsɪʙɪʟɪᴛʏ");
                lore.add("§7ᴡʜɪʟᴇ sᴛᴀɴᴅɪɴɢ sᴛɪʟʟ (3s)");
                break;
            case LIGHT:
                lore.add("§e❤ ʜᴇᴀʟᴛʜ ʀᴇɢᴇɴᴇʀᴀᴛɪᴏɴ");
                lore.add("§7ᴏᴜᴛ ᴏꜰ ᴄᴏᴍʙᴀᴛ (10s)");
                break;
            case VOID:
                lore.add("§5🌀 ᴠᴏɪᴅ ᴘʀᴏᴛᴇᴄᴛɪᴏɴ");
                lore.add("§7ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ sᴘᴀᴡɴ ɪɴ ᴠᴏɪᴅ");
                break;
            case STORM:
                lore.add("§3⚡ sᴘᴇᴇᴅ ʙᴏᴏsᴛ");
                lore.add("§7ᴅᴜʀɪɴɢ ʀᴀɪɴ/sᴛᴏʀᴍs");
                break;
            case LUCK:
                lore.add("§a🍀 ʟᴜᴄᴋ ᴠɪ");
                lore.add("§7ɪɴᴄʀᴇᴀsᴇᴅ ʟᴏᴏᴛ & ꜰᴏʀᴛᴜɴᴇ");
                break;
            case TIME:
                lore.add("§d⏰ sʟᴏᴡɴᴇss ɪᴍᴍᴜɴɪᴛʏ");
                lore.add("§7ɪᴍᴍᴜɴᴇ ᴛᴏ sʟᴏᴡɪɴɢ ᴇꜰꜰᴇᴄᴛs");
                break;
            case DRAGON:
                lore.add("§c🔥 ꜰɪʀᴇ ɪᴍᴍᴜɴɪᴛʏ");
                lore.add("§7ᴄᴏᴍᴘʟᴇᴛᴇ ꜰɪʀᴇ ᴘʀᴏᴛᴇᴄᴛɪᴏɴ");
                lore.add("§6🛡 ᴋɴᴏᴄᴋʙᴀᴄᴋ ʀᴇsɪsᴛᴀɴᴄᴇ");
                lore.add("§7ʀᴇᴅᴜᴄᴇᴅ ᴋɴᴏᴄᴋʙᴀᴄᴋ");
                break;
            default:
                lore.add("§7ɴᴏɴᴇ");
                break;
        }
        
        lore.add("");
        
        meta.setLore(lore);
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
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_info_button"));
        meta.setDisplayName(Typography.formatTitle("Fragment Guide"));
        
        // Dynamic lore based on configuration
        boolean fragmentChangerRequired = configManager.isFragmentChangerRequired();
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("");
        lore.add(Typography.formatLabel("Left-Click ") + Typography.SYMBOL_ARROW + Typography.formatValue(" View Abilities"));
        lore.add(Typography.formatLabel("Right-Click ") + Typography.SYMBOL_ARROW + Typography.formatValue(" Activate"));
        lore.add("");
        lore.add(Typography.COLOR_TEXT + "§m                    ");
        lore.add("");
        lore.add(Typography.COLOR_SUCCESS + Typography.SYMBOL_CHECK + " Activated " + Typography.COLOR_TEXT_DARK + "- Currently using");
        
        if (fragmentChangerRequired) {
            lore.add(Typography.COLOR_HIGHLIGHT + Typography.SYMBOL_DOT + " Owned " + Typography.COLOR_TEXT_DARK + "- Right-click with Fragment Changer");
            lore.add(Typography.COLOR_ACCENT + Typography.SYMBOL_LIGHTNING + " Charged " + Typography.COLOR_TEXT_DARK + "- Right-click with Fragment Changer");
        } else {
            lore.add(Typography.COLOR_HIGHLIGHT + Typography.SYMBOL_DOT + " Owned " + Typography.COLOR_TEXT_DARK + "- Right-click to switch");
            lore.add(Typography.COLOR_ACCENT + Typography.SYMBOL_LIGHTNING + " Charged " + Typography.COLOR_TEXT_DARK + "- Auto-activated from ritual");
        }
        lore.add(Typography.COLOR_TEXT_DARK + Typography.SYMBOL_CROSS + " Locked " + Typography.COLOR_TEXT_DARK + "- Need ritual");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create back button
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_back_button"));
        meta.setDisplayName(Typography.formatTitle("Back"));
        meta.setLore(Arrays.asList(Typography.COLOR_TEXT + "Return to Fragment overview"));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create controls button
     */
    private ItemStack createControlsButton() {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_controls"));
        meta.setDisplayName(Typography.COLOR_SECONDARY + "§l⚙ " + Typography.toSmallCaps("Controls"));
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
     * Create mana status button
     */
    public ItemStack createManaStatusButton(Player player, boolean isRefreshed) {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_mana_status"));
        meta.setDisplayName(Typography.COLOR_PRIMARY + "§l⚡ " + Typography.toSmallCaps("Mana Status"));

        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        double regenRate = manaManager.getManaRegenRate(player);

        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("");
        lore.add(com.muzlik.util.Typography.COLOR_TEXT + "Current: §b" + String.format("%.0f", currentMana));
        lore.add(com.muzlik.util.Typography.COLOR_TEXT + "Maximum: §b" + String.format("%.0f", maxMana));
        lore.add(com.muzlik.util.Typography.COLOR_TEXT + "Regen: §b" + String.format("%.1f", regenRate) + "/s");
        lore.add("");

        if (isRefreshed) {
            lore.add(com.muzlik.util.Typography.COLOR_SUCCESS + "§l✓ REFRESHED");
        } else {
            lore.add(com.muzlik.util.Typography.COLOR_HIGHLIGHT + "§l▶ CLICK TO REFRESH");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create admin give button
     */
    private ItemStack createAdminGiveButton() {
        ItemStack item = new ItemStack(com.muzlik.texture.TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_give"));
        meta.setDisplayName("§d§l⚡ " + Typography.toSmallCaps("Give Fragment"));
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
