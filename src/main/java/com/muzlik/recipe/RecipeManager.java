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
        
        // LIGHT Fragment - Custom pattern: Beacon center, Glowstone corners, Netherite Ingots on sides
        registerLightFragmentCreation();
        
        // VOID Fragment - Custom pattern: Netherite Block center, Echo Shards corners, Sculk Catalyst on sides
        registerVoidFragmentCreation();
        
        // MOB Fragment - Custom pattern: Nether Star center, Totems corners, Netherite Ingots on sides
        registerMobFragmentCreation();
        
        // DRAGON Fragment - Custom pattern: Dragon Egg center, Dragon Breath corners, Netherite Ingots on sides
        registerDragonFragmentCreation();
        
        // STORM Fragment
        registerFragmentCreation(FragmentType.STORM, Material.LIGHTNING_ROD, Material.TRIDENT, Material.DIAMOND);
    }

    /**
     * Register Light Fragment Creation recipe with custom pattern
     * Pattern: Beacon center, Glowstone corners, Netherite Ingots on sides
     * G N G
     * N B N
     * G N G
     */
    private void registerLightFragmentCreation() {
        ItemStack result = createFragmentCreationItem(FragmentType.LIGHT);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_light");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("GNG", "NBN", "GNG");
        recipe.setIngredient('G', Material.GLOWSTONE);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('B', Material.BEACON);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_light", key);
    }

    /**
     * Register Dragon Fragment Creation recipe with custom pattern
     * Pattern: Dragon Egg center, Dragon Head corners, Netherite Ingots on sides
     * D N D
     * N E N
     * D N D
     */
    private void registerDragonFragmentCreation() {
        ItemStack result = createFragmentCreationItem(FragmentType.DRAGON);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_dragon");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("DND", "NEN", "DND");
        recipe.setIngredient('D', Material.DRAGON_HEAD);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('E', Material.DRAGON_EGG);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_dragon", key);
    }

    /**
     * Register Mob Fragment Creation recipe with custom pattern
     * Pattern: Netherite Ingot center, Bones on sides, Totems in corners
     * T B T
     * B N B
     * T B T
     */
    private void registerMobFragmentCreation() {
        ItemStack result = createFragmentCreationItem(FragmentType.MOB);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_mob");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("TBT", "BNB", "TBT");
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('B', Material.BONE);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_mob", key);
    }

    /**
     * Register Void Fragment Creation recipe with custom pattern
     * Pattern: Sculk Catalyst center, Echo Shards corners, Netherite Ingot on sides
     * E S E
     * S N S
     * E S E
     */
    private void registerVoidFragmentCreation() {
        ItemStack result = createFragmentCreationItem(FragmentType.VOID);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_void");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("ESE", "SNS", "ESE");
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('S', Material.NETHERITE_INGOT);
        recipe.setIngredient('N', Material.SCULK_CATALYST);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_void", key);
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
