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
        plugin.getLogger().info("Starting recipe registration...");
        try {
            registerFragmentCreationRecipes();
            registerFragmentChangerRecipe();
            registerRitualCatalystRecipe();
            registerManaFlaskRecipe();
            plugin.getLogger().info("Successfully registered all recipes");
            
            // Debug: List all registered recipes
            plugin.getLogger().info("Registered recipe keys: " + recipeKeys.keySet());
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register Fragment Creation recipes for all 10 types
     */
    private void registerFragmentCreationRecipes() {
        plugin.getLogger().info("Registering Fragment Creation recipes...");
        
        // FIRE Fragment
        registerFragmentCreation(FragmentType.FIRE, Material.NETHERRACK, Material.FIRE_CHARGE, Material.DIAMOND);
        plugin.getLogger().info("Registered Fire Fragment creation recipe");
        
        // WATER Fragment
        registerFragmentCreation(FragmentType.WATER, Material.PRISMARINE, Material.WATER_BUCKET, Material.DIAMOND);
        plugin.getLogger().info("Registered Water Fragment creation recipe");
        
        // AIR Fragment
        registerFragmentCreation(FragmentType.AIR, Material.FEATHER, Material.PHANTOM_MEMBRANE, Material.DIAMOND);
        plugin.getLogger().info("Registered Air Fragment creation recipe");
        
        // DARK Fragment
        registerFragmentCreation(FragmentType.DARK, Material.OBSIDIAN, Material.WITHER_SKELETON_SKULL, Material.DIAMOND);
        plugin.getLogger().info("Registered Dark Fragment creation recipe");
        
        // LIGHT Fragment - Custom pattern: Beacon center, Glowstone corners, Netherite Ingots on sides
        registerLightFragmentCreation();
        
        // VOID Fragment - Custom pattern: Netherite Block center, Echo Shards corners, Sculk Catalyst on sides
        registerVoidFragmentCreation();
        
        // DRAGON Fragment - Custom pattern: Dragon Egg center, Dragon Breath corners, Netherite Ingots on sides
        registerDragonFragmentCreation();
        
        // STORM Fragment
        registerFragmentCreation(FragmentType.STORM, Material.LIGHTNING_ROD, Material.TRIDENT, Material.DIAMOND);
        plugin.getLogger().info("Registered Storm Fragment creation recipe");
        
        // TIME Fragment - Custom pattern: Clock center, Amethyst Shard corners, Diamond on sides
        registerTimeFragmentCreation();
        
        // LUCK Fragment - Custom pattern: Rabbit's Foot center, Gold Ingot corners, Diamond on sides
        registerLuckFragmentCreation();
        
        plugin.getLogger().info("All 10 Fragment Creation recipes registered successfully");
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
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC); // Commented out for compatibility
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_light", key);
        plugin.getLogger().info("Registered Light Fragment creation recipe");
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
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_dragon", key);
        plugin.getLogger().info("Registered Dragon Fragment creation recipe");
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
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_void", key);
        plugin.getLogger().info("Registered Void Fragment creation recipe");
    }

    /**
     * Register Time Fragment Creation recipe with custom pattern
     * Pattern: Clock center, Amethyst Shard corners, Diamond on sides
     * C D C
     * D K D
     * C D C
     */
    private void registerTimeFragmentCreation() {
        ItemStack result = createFragmentCreationItem(FragmentType.TIME);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_time");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("CDC", "DKD", "CDC");
        recipe.setIngredient('C', Material.AMETHYST_SHARD);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('K', Material.CLOCK);
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_time", key);
        plugin.getLogger().info("Registered Time Fragment creation recipe");
    }

    /**
     * Register Luck Fragment Creation recipe with custom pattern
     * Pattern: Rabbit's Foot center, Gold Ingot corners, Diamond on sides
     * G D G
     * D R D
     * G D G
     */
    private void registerLuckFragmentCreation() {
        ItemStack result = createFragmentCreationItem(FragmentType.LUCK);
        NamespacedKey key = new NamespacedKey(plugin, "fragment_creation_luck");
        
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("GDG", "DRD", "GDG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('R', Material.RABBIT_FOOT);
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("fragment_creation_luck", key);
        plugin.getLogger().info("Registered Luck Fragment creation recipe");
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
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
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
        recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
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
        // recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        
        plugin.getServer().addRecipe(recipe);
        recipeKeys.put("mana_flask", key);
    }

    /**
     * Create Fragment Creation item using appropriate base material with custom model data
     */
    private ItemStack createFragmentCreationItem(FragmentType type) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data for texture pack compatibility
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getFragmentTexture(type));
        
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
     * Create Fragment Changer item using appropriate base material with custom model data
     */
    private ItemStack createFragmentChangerItem() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data for texture pack compatibility
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_info_button"));
        
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
     * Create Ritual Catalyst item using appropriate base material with custom model data
     */
    private ItemStack createRitualCatalystItem() {
        // Use DIAMOND as base - makes sense since it's crafted with diamond and is a refined/enhanced item
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data for texture pack compatibility
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getUITexture("ui_bonus"));
        
        meta.setDisplayName("§6§lRitual Catalyst");
        meta.setLore(Arrays.asList(
            "§7A refined magical catalyst",
            "§7Enhances ritual power and stability",
            "§7Consumed when starting rituals",
            "",
            "§e§lRITUAL COMPONENT"
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create Mana Flask item using appropriate base material with custom model data
     */
    private ItemStack createManaFlaskItem() {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        
        // Set custom model data for texture pack compatibility
        meta.setCustomModelData(com.muzlik.texture.TextureRegistry.getManaTexture("mana_flask"));
        
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
    
    /**
     * Create an actual Fragment item (not creation item)
     */
    public ItemStack createFragmentItem(FragmentType type) {
        return com.muzlik.texture.TextureItemBuilder.createFragmentItem(type);
    }
    
    /**
     * Check if item is an actual Fragment item (not creation item)
     */
    public boolean isFragmentItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        return displayName.contains("Fragment") && !displayName.contains("Creation") && !displayName.contains("Changer");
    }
    
    /**
     * Get Fragment type from actual Fragment item
     */
    public FragmentType getFragmentTypeFromFragmentItem(ItemStack item) {
        if (!isFragmentItem(item)) {
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
     * Test if recipes are properly registered by checking server recipe registry
     */
    public void testRecipeRegistration() {
        plugin.getLogger().info("Testing recipe registration...");
        
        int foundRecipes = 0;
        for (String recipeKey : recipeKeys.keySet()) {
            NamespacedKey key = recipeKeys.get(recipeKey);
            if (plugin.getServer().getRecipe(key) != null) {
                foundRecipes++;
                plugin.getLogger().info("✓ Recipe found: " + recipeKey);
            } else {
                plugin.getLogger().warning("✗ Recipe missing: " + recipeKey);
            }
        }
        
        plugin.getLogger().info("Recipe test complete: " + foundRecipes + "/" + recipeKeys.size() + " recipes found");
    }
}
