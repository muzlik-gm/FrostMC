package com.muzlik.ui;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating enhanced Fragment icons with rich visual information.
 * 
 * Fragment States:
 * - LOCKED (gray/dark) - Not available, no ritual completed
 * - CHARGED (gold/yellow) - Ritual complete, ready to activate
 * - ACTIVATED (green) - Currently active and usable
 * - OWNED (white) - Previously activated, can switch to with changer
 */
public class FragmentIconBuilder {
    private final FragmentType fragmentType;
    private final Player player;
    private final LevelManager levelManager;
    private final RankManager rankManager;
    private final ManaManager manaManager;
    private FragmentManager fragmentManager;
    
    private boolean isOwned;
    private boolean isActive;
    private boolean isCharged;
    private Material customMaterial;
    private Integer customModelData;

    public FragmentIconBuilder(FragmentType fragmentType, Player player,
                               LevelManager levelManager, RankManager rankManager,
                               ManaManager manaManager) {
        this.fragmentType = fragmentType;
        this.player = player;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
        this.manaManager = manaManager;
        this.isOwned = false;
        this.isActive = false;
        this.isCharged = false;
    }

    /**
     * Set FragmentManager reference for charge checking
     */
    public FragmentIconBuilder fragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        return this;
    }

    /**
     * Set whether the Fragment is owned by the player
     */
    public FragmentIconBuilder owned(boolean owned) {
        this.isOwned = owned;
        return this;
    }

    /**
     * Set whether the Fragment is currently active
     */
    public FragmentIconBuilder active(boolean active) {
        this.isActive = active;
        return this;
    }

    /**
     * Set whether the Fragment is charged (ritual complete)
     */
    public FragmentIconBuilder charged(boolean charged) {
        this.isCharged = charged;
        return this;
    }

    /**
     * Set custom material (overrides default)
     */
    public FragmentIconBuilder material(Material material) {
        this.customMaterial = material;
        return this;
    }

    /**
     * Set custom model data for resource pack support
     */
    public FragmentIconBuilder customModelData(int modelData) {
        this.customModelData = modelData;
        return this;
    }

    /**
     * Build the ItemStack with all configured properties
     */
    public ItemStack build() {
        // Use texture system: single base material with custom model data
        Material material = com.muzlik.texture.TextureRegistry.getBaseMaterial();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Set display name with color coding
        String displayName = buildDisplayName();
        meta.setDisplayName(displayName);

        // Build lore with all information
        List<String> lore = buildLore();
        meta.setLore(lore);

        // Add enchantment glow for active Fragment
        if (isActive) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Set custom model data from texture registry
        int textureId = customModelData != null ? customModelData : 
                com.muzlik.texture.TextureRegistry.getFragmentTexture(fragmentType);
        meta.setCustomModelData(textureId);
        
        // Hide all item flags for clean display
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
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
     * Build display name with appropriate color coding - CLEAN MINIMAL STYLE
     * Priority: ACTIVATED > OWNED > CHARGED > LOCKED
     */
    private String buildDisplayName() {
        String name = toSmallCaps(fragmentType.getDisplayName());
        
        if (isActive) {
            // Currently active - GREEN
            return "§a✓ §f" + name + " §a[ᴀᴄᴛɪᴠᴀᴛᴇᴅ]";
        } else if (isOwned) {
            // Owned but not active - WHITE
            return "§f" + name + " §7[ᴏᴡɴᴇᴅ]";
        } else if (isCharged) {
            // Charged, ready to activate - GOLD
            return "§6⚡ §e" + name + " §6[ᴄʜᴀʀɢᴇᴅ]";
        } else {
            // Locked - GRAY
            return "§8" + name + " §7[ʟᴏᴄᴋᴇᴅ]";
        }
    }

    /**
     * Build comprehensive lore - CLEAN MINIMAL STYLE
     */
    private List<String> buildLore() {
        List<String> lore = new ArrayList<>();

        // Description
        lore.add("§8" + fragmentType.getDescription());
        lore.add("");

        if (isActive) {
            // ACTIVATED state - show full stats
            int rank = rankManager.getRank(player, fragmentType);
            int maxRank = rankManager.getMaxRank(fragmentType);
            int level = levelManager.getLevel(player, fragmentType);
            int maxLevel = levelManager.getMaxLevel(fragmentType);
            double xp = levelManager.getXP(player, fragmentType);
            double xpRequired = levelManager.getXPForNextLevel(player, fragmentType);

            lore.add("§7ʀᴀɴᴋ §f" + rank + "§8/§f" + maxRank);
            lore.add("§7ʟᴇᴠᴇʟ §f" + level + "§8/§f" + maxLevel + " §8(" + String.format("%.0f", xp) + "/" + String.format("%.0f", xpRequired) + ")");

            // Mana display
            double currentMana = manaManager.getMana(player);
            double maxMana = manaManager.getMaxMana(player);
            lore.add("");
            lore.add("§7ᴍᴀɴᴀ §f" + String.format("%.0f", currentMana) + "§8/§f" + String.format("%.0f", maxMana));

            // Status
            lore.add("");
            lore.add("§a▸ ᴄᴜʀʀᴇɴᴛʟʏ ᴀᴄᴛɪᴠᴇ");
            lore.add("");
            lore.add("§7[ʟᴇꜰᴛ-ᴄʟɪᴄᴋ] §8ᴠɪᴇᴡ ᴀʙɪʟɪᴛɪᴇs");
            
        } else if (isOwned) {
            // OWNED state - can activate with changer
            int rank = rankManager.getRank(player, fragmentType);
            int maxRank = rankManager.getMaxRank(fragmentType);
            int level = levelManager.getLevel(player, fragmentType);

            lore.add("§7ʀᴀɴᴋ §f" + rank + "§8/§f" + maxRank);
            lore.add("§7ʟᴇᴠᴇʟ §f" + level);

            lore.add("");
            lore.add("§7[ʟᴇꜰᴛ-ᴄʟɪᴄᴋ] §8ᴠɪᴇᴡ ᴀʙɪʟɪᴛɪᴇs");
            lore.add("§7[ʀɪɢʜᴛ-ᴄʟɪᴄᴋ] §8ᴀᴄᴛɪᴠᴀᴛᴇ");
            
        } else if (isCharged) {
            // CHARGED state - ritual complete, ready to activate
            lore.add("§e§l⚡ ʀɪᴛᴜᴀʟ ᴄᴏᴍᴘʟᴇᴛᴇ!");
            lore.add("");
            lore.add("§7ᴛʜɪs ꜰʀᴀɢᴍᴇɴᴛ ɪs ᴄʜᴀʀɢᴇᴅ");
            lore.add("§7ᴀɴᴅ ʀᴇᴀᴅʏ ᴛᴏ ᴀᴄᴛɪᴠᴀᴛᴇ");
            lore.add("");
            lore.add("§7[ʟᴇꜰᴛ-ᴄʟɪᴄᴋ] §8ᴠɪᴇᴡ ᴀʙɪʟɪᴛɪᴇs");
            lore.add("§e[ʀɪɢʜᴛ-ᴄʟɪᴄᴋ] §6ᴀᴄᴛɪᴠᴀᴛᴇ!");
            
        } else {
            // LOCKED state
            lore.add("§c✗ ɴᴏᴛ ᴜɴʟᴏᴄᴋᴇᴅ");
            lore.add("");
            lore.add("§7ᴄᴏᴍᴘʟᴇᴛᴇ ᴛʜᴇ ꜰʀᴀɢᴍᴇɴᴛ");
            lore.add("§7ᴄʀᴇᴀᴛɪᴏɴ ʀɪᴛᴜᴀʟ ᴛᴏ ᴜɴʟᴏᴄᴋ");
        }

        return lore;
    }

    /**
     * Build rank badge with color coding
     */
    private String buildRankBadge(int rank, int maxRank) {
        String color;
        String symbol;
        
        if (rank >= maxRank) {
            color = "§6§l";
            symbol = "★";
        } else if (rank >= maxRank - 1) {
            color = "§6";
            symbol = "◆";
        } else if (rank >= (maxRank / 2)) {
            color = "§e";
            symbol = "◆";
        } else {
            color = "§7";
            symbol = "◆";
        }
        
        return color + symbol;
    }

    /**
     * Build progress bar for XP/Level display
     */
    private String buildProgressBar(double current, double max, int barLength) {
        String bar = com.muzlik.texture.FragmentSymbols.buildProgressBar(current, max, barLength);
        double percentage = max > 0 ? (current / max) * 100 : 0;
        return "§7[" + bar + "§7] §e" + String.format("%.0f", percentage) + "%";
    }

    /**
     * Get default material for Fragment type
     */
    private Material getDefaultMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.FIRE_CHARGE;
            case WATER -> Material.HEART_OF_THE_SEA;
            case AIR -> Material.FEATHER;
            case EARTH -> Material.MOSSY_COBBLESTONE;
            case DARK -> Material.OBSIDIAN;
            case LIGHT -> Material.GLOWSTONE;
            case VOID -> Material.ENDER_PEARL;
            case MOB -> Material.SPAWNER;
            case DRAGON -> Material.DRAGON_EGG;
            case STORM -> Material.LIGHTNING_ROD;
        };
    }
}
