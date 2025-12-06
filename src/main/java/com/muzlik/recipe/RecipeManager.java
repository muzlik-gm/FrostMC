package com.muzlik.recipe;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages custom recipes for Fragment items.
 */
public class RecipeManager {
    private final JavaPlugin plugin;
    private final Map<String, NamespacedKey> recipeKeys;

    public RecipeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeKeys = new HashMap<>();
    }

    /**
     * Register all recipes
     */
    public void registerRecipes() {
        registerFragmentCreationRecipes();
        registerFragmentChangerRecipe();
        registerRitualCatalystRecipe();
        registerManaFlaskRecipe();
        
        plugin.getLogger().info("All recipes registered successfully");
    }

    /**
     * Register Fragment Creation recipes for all 10 types
     */
    private void registerFragmentCreationRecipes() {
        // FIRE Fragment
        registerFragmentCreation(FragmentType.FIRE, Material.NETHERRACK, Material.FIRE_CHARGE, Material.DIAMOND);
        
        // WATER Fragment
        registerFragmentCreation(FragmentType.WATER, Material.PRISMARINE, Material.WATER_BUCKET, Material.DIAMOND);
        
        // AIR Fragment
        registerFragmentCreation(FragmentType.AIR, Material.FEATHER, Material.PHANTOM_MEMBRANE, Material.DIAMOND);
        
        // EARTH Fragment
        registerFragmentCreation(FragmentType.EARTH, Material.STONE, Material.MOSS_BLOCK, Material.DIAMOND);
        
        // DARK Fragment
        registerFragmentCreation(FragmentType.DARK, Material.OBSIDIAN, Material.WITHER_SKELETON_SKULL, Material.DIAMOND);
        
        // LIGHT Fragment
        registerFragmentCreation(FragmentType.LIGHT, Material.GLOWSTONE, Material.BEACON, Material.DIAMOND);
        
        // VOID Fragment
        registerFragmentCreation(FragmentType.VOID, Material.END_STONE, Material.ENDER_PEARL, Material.DIAMOND);
        
        // MOB Fragment
        registerFragmentCreation(FragmentType.MOB, Material.SPAWNER, Material.TOTEM_OF_UNDYING, Material.DIAMOND);
        
        // DRAGON Fragment
        registerFragmentCreation(FragmentType.DRAGON, Material.DRAGON_EGG, Material.DRAGON_HEAD, Material.DIAMOND);
        
        // STORM Fragment
        registerFragmentCreation(FragmentType.STORM, Material.LIGHTNING_ROD, Material.TRIDENT, Material.DIAMOND);
    }

    /**
     * Register a Fragment Creation recipe
     */
    private void registerFragmentCreation(FragmentType type, Material outer, Material inner, Material center) {
        ItemStack result = createFragmentCreationItem(type);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_" + type.name().toLowerCase());
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("OIO", "ICI", "OIO");
        recipe.setIngredient('O', outer);
        recipe.setIngredient('I', inner);
        recipe.setIngredient('C', center);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_" + type.name(), key);
    }

    /**
     * Register Fragment Changer recipe
     */
    private void registerFragmentChangerRecipe() {
        ItemStack result = createFragmentChangerItem();
        NamespacedKey key = new NamespacedKey(plugin, "fragment_changer");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("ESE", "SDS", "ESE");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_changer", key);
    }

    /**
     * Register Ritual Catalyst recipe
     */
    private void registerRitualCatalystRecipe() {
        ItemStack result = createRitualCatalystItem();
        NamespacedKey key = new NamespacedKey(plugin, "ritual_catalyst");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(" G ", "GDG", " G ");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("ritual_catalyst", key);
    }

    /**
     * Register Mana Flask recipe
     */
    private void registerManaFlaskRecipe() {
        ItemStack result = createManaFlaskItem();
        NamespacedKey key = new NamespacedKey(plugin, "mana_flask");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(" L ", "LBL", " L ");
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("mana_flask", key);
    }

    /**
     * Create Fragment Creation item
     */
    private ItemStack createFragmentCreationItem(FragmentType type) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§b§l" + type.getDisplayName() + " Fragment Creation");
        meta.setLore(Arrays.asList(
            "§7Place this item to start ritual",
            "§7Duration: 10-15 minutes",
            "§7Grants: §b" + type.getDisplayName() + " Fragment",
            "§7Stay within 5 blocks!",
            "",
            "§e§lFRAGMENT CREATION RITUAL"
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create Fragment Changer item
     */
    private ItemStack createFragmentChangerItem() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§d§lFragment Changer");
        meta.setLore(Arrays.asList(
            "§7Place this item to start ritual",
            "§7Duration: 5 minutes",
            "§7Allows switching active Fragment",
            "§7Stay within 5 blocks!",
            "",
            "§e§lFRAGMENT CHANGER RITUAL"
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create Ritual Catalyst item
     */
    private ItemStack createRitualCatalystItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6§lRitual Catalyst");
        meta.setLore(Arrays.asList(
            "§7Used in various rituals",
            "§7Enhances ritual power",
            "",
            "§e§lRITUAL COMPONENT"
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create Mana Flask item
     */
    private ItemStack createManaFlaskItem() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§b§lMana Flask");
        meta.setLore(Arrays.asList(
            "§7Right-click to consume",
            "§7Restores §b50 §7mana instantly",
            "",
            "§e§lCONSUMABLE"
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Check if item is a Fragment Creation item
     */
    public boolean isFragmentCreationItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        return displayName.contains("Fragment Creation");
    }

    /**
     * Get Fragment type from Fragment Creation item
     */
    public FragmentType getFragmentTypeFromItem(ItemStack item) {
        if (!isFragmentCreationItem(item)) {
            return null;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        for (FragmentType type : FragmentType.values()) {
            if (displayName.contains(type.getDisplayName())) {
                return type;
            }
        }
        
        return null;
    }

    /**
     * Check if item is a Fragment Changer
     */
    public boolean isFragmentChanger(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().contains("Fragment Changer");
    }

    /**
     * Check if item is a Ritual Catalyst
     */
    public boolean isRitualCatalyst(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().contains("Ritual Catalyst");
    }

    /**
     * Check if item is a Mana Flask
     */
    public boolean isManaFlask(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().contains("Mana Flask");
    }
}
