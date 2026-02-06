package com.muzlik.texture;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating ItemStacks with custom textures (CustomModelData).
 * Simplifies the process of creating items that use the texture system.
 */
public class TextureItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private final List<String> lore;
    
    /**
     * Create a new TextureItemBuilder with the base material and custom model data
     */
    public TextureItemBuilder(int customModelData) {
        this.item = new ItemStack(TextureRegistry.getBaseMaterial());
        this.meta = item.getItemMeta();
        this.lore = new ArrayList<>();
        
        // Set custom model data
        meta.setCustomModelData(customModelData);
    }
    
    /**
     * Set the display name
     */
    public TextureItemBuilder name(String name) {
        meta.setDisplayName(name);
        return this;
    }
    
    /**
     * Add a line to the lore
     */
    public TextureItemBuilder addLore(String line) {
        lore.add(line);
        return this;
    }
    
    /**
     * Add multiple lines to the lore
     */
    public TextureItemBuilder addLore(List<String> lines) {
        lore.addAll(lines);
        return this;
    }
    
    /**
     * Add an empty line to the lore
     */
    public TextureItemBuilder addEmptyLine() {
        lore.add("");
        return this;
    }
    
    /**
     * Add enchantment glow effect
     */
    public TextureItemBuilder glow() {
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }
    
    /**
     * Hide all item flags
     */
    public TextureItemBuilder hideFlags() {
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        return this;
    }
    
    /**
     * Make the item unbreakable
     */
    public TextureItemBuilder unbreakable() {
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }
    
    /**
     * Set the amount
     */
    public TextureItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }
    
    /**
     * Build the final ItemStack
     */
    public ItemStack build() {
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    // ==================== CONVENIENCE FACTORY METHODS ====================
    
    /**
     * Create a Fragment icon item
     */
    public static ItemStack createFragmentIcon(com.muzlik.fragment.FragmentType type, String displayName, List<String> lore, boolean glow) {
        int customModelData = TextureRegistry.getFragmentTexture(type);
        
        TextureItemBuilder builder = new TextureItemBuilder(customModelData)
                .name(displayName)
                .addLore(lore)
                .hideFlags();
        
        if (glow) {
            // DO NoTHING
        }
        
        return builder.build();
    }
    
    /**
     * Create an ability icon item
     */
    public static ItemStack createAbilityIcon(String abilityId, String displayName, List<String> lore, boolean glow) {
        TextureItemBuilder builder = new TextureItemBuilder(TextureRegistry.getAbilityTexture(abilityId))
                .name(displayName)
                .addLore(lore)
                .hideFlags();
        
        if (glow) {
            // DO NOTHING
        }
        
        return builder.build();
    }
    
    /**
     * Create a mana indicator item
     */
    public static ItemStack createManaIcon(double percentage, String displayName, List<String> lore) {
        return new TextureItemBuilder(TextureRegistry.getManaTextureByPercentage(percentage))
                .name(displayName)
                .addLore(lore)
                .hideFlags()
                .build();
    }
    
    /**
     * Create a UI element item
     */
    public static ItemStack createUIElement(String element, String displayName, List<String> lore) {
        return new TextureItemBuilder(TextureRegistry.getUITexture(element))
                .name(displayName)
                .addLore(lore)
                .hideFlags()
                .build();
    }
    
    /**
     * Create a mana flask item
     */
    public static ItemStack createManaFlask() {
        return new TextureItemBuilder(TextureRegistry.getManaTexture("mana_flask"))
                .name("§b§lMana Flask")
                .addLore("§7Right-click to consume")
                .addLore("§7Restores §b50 §7mana instantly")
                .addEmptyLine()
                .addLore("§e§lCONSUMABLE")
                .hideFlags()
                .build();
    }
    
    /**
     * Create an actual Fragment item that players can hold
     * Right-click to activate the fragment
     */
    public static ItemStack createFragmentItem(com.muzlik.fragment.FragmentType type) {
        ItemStack item = new ItemStack(TextureRegistry.getBaseMaterial()); // Still use PAPER for fragment items
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data for texture pack compatibility
        meta.setCustomModelData(TextureRegistry.getFragmentTexture(type));
        
        meta.setDisplayName("§5§l" + type.getDisplayName() + " Fragment Activator");
        
        // Build lore list first, then set it
        List<String> lore = new ArrayList<>();
        lore.add("§7A powerful elemental fragment");
        lore.add("§7containing " + type.getDisplayName().toLowerCase() + " energy");
        lore.add("");
        lore.add("§e➤ Right-click to activate this fragment!");
        lore.add("");
        
        // Add passive effects based on fragment type
        lore.add("§6§lPassive Effects:");
        switch (type) {
            case FIRE:
                lore.add("§7• Fire Resistance");
                break;
            case WATER:
                lore.add("§7• Water Breathing");
                break;
            case AIR:
                lore.add("§7• Slow Falling (while sneaking)");
                break;
            case DARK:
                lore.add("§7• Invisibility (while standing still)");
                break;
            case LIGHT:
                lore.add("§7• Health Regeneration (out of combat)");
                break;
            case VOID:
                lore.add("§7• Void Protection (teleport to spawn)");
                break;
            case STORM:
                lore.add("§7• Speed Boost (during rain/storms)");
                break;
            case LUCK:
                lore.add("§7• Luck VI Effect");
                break;
            case TIME:
                lore.add("§7• Slowness Immunity");
                break;
            case DRAGON:
                lore.add("§7• Fire Immunity");
                lore.add("§7• Knockback Resistance");
                break;
            default:
                lore.add("§7• None");
                break;
        }
        
        lore.add("");
        lore.add("§8Fragment Type: §5" + type.name());
        lore.add("§5§lFRAGMENT_ACTIVATOR");
        
        // Set the complete lore
        meta.setLore(lore);
        
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        
        item.setItemMeta(meta);
        return item;
    }
}
