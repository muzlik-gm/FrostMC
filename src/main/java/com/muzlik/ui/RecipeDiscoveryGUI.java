package com.muzlik.ui;

import com.muzlik.fragment.FragmentType;
import com.muzlik.texture.TextureRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for discovering fragment creation recipes
 */
public class RecipeDiscoveryGUI {

    private static final Component MAIN_TITLE = Component.text("Fragment Recipes")
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD);
    private static final int MAIN_SIZE = 54; // 6 rows
    private static final int DETAIL_SIZE = 54; // 6 rows

    /**
     * Open the main recipe list GUI
     */
    public static void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, MAIN_SIZE, MAIN_TITLE);

        // Add all 10 fragment recipes
        inv.setItem(10, createRecipeItem(FragmentType.FIRE));
        inv.setItem(11, createRecipeItem(FragmentType.WATER));
        inv.setItem(12, createRecipeItem(FragmentType.AIR));
        inv.setItem(13, createRecipeItem(FragmentType.DARK));
        inv.setItem(14, createRecipeItem(FragmentType.LIGHT));
        inv.setItem(15, createRecipeItem(FragmentType.VOID));
        inv.setItem(16, createRecipeItem(FragmentType.DRAGON));
        inv.setItem(19, createRecipeItem(FragmentType.STORM));
        inv.setItem(20, createRecipeItem(FragmentType.TIME));
        inv.setItem(21, createRecipeItem(FragmentType.LUCK));

        // Add decorative items
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        // Fill empty slots with glass
        for (int i = 0; i < MAIN_SIZE; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Open detailed recipe view for a specific fragment
     */
    public static void openDetailGUI(Player player, FragmentType fragmentType) {
        Component detailTitle = Component.text("Recipe: " + fragmentType.getDisplayName())
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD);
        Inventory inv = Bukkit.createInventory(null, DETAIL_SIZE, detailTitle);

        // Display 3x3 crafting pattern (centered in GUI)
        RecipePattern pattern = getRecipePattern(fragmentType);
        
        // Row 1 of crafting grid (slots 11-13)
        inv.setItem(11, pattern.topLeft);
        inv.setItem(12, pattern.topCenter);
        inv.setItem(13, pattern.topRight);
        
        // Row 2 of crafting grid (slots 20-22)
        inv.setItem(20, pattern.middleLeft);
        inv.setItem(21, pattern.middleCenter);
        inv.setItem(22, pattern.middleRight);
        
        // Row 3 of crafting grid (slots 29-31)
        inv.setItem(29, pattern.bottomLeft);
        inv.setItem(30, pattern.bottomCenter);
        inv.setItem(31, pattern.bottomRight);

        // Result item (slot 24)
        inv.setItem(24, createFragmentItem(fragmentType));

        // Arrow indicator (slot 23)
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setDisplayName("§e→ Result");
            arrow.setItemMeta(arrowMeta);
        }
        inv.setItem(23, arrow);

        // Ritual instructions (slot 49)
        ItemStack instructions = new ItemStack(Material.BOOK);
        ItemMeta instructionsMeta = instructions.getItemMeta();
        if (instructionsMeta != null) {
            instructionsMeta.setDisplayName("§6§lRitual Instructions");
            List<String> lore = new ArrayList<>();
            lore.add("§71. Craft the fragment item");
            lore.add("§72. Build a ritual structure:");
            lore.add("§7   - Place beacon at center");
            lore.add("§7   - Surround with obsidian");
            lore.add("§73. Drop the fragment item on beacon");
            lore.add("§74. Wait for ritual to complete");
            instructionsMeta.setLore(lore);
            instructions.setItemMeta(instructionsMeta);
        }
        inv.setItem(49, instructions);

        // Back button (slot 45)
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c← Back");
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);

        // Fill empty slots with glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < DETAIL_SIZE; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Create a recipe item for the main GUI
     */
    private static ItemStack createRecipeItem(FragmentType fragmentType) {
        ItemStack item = createFragmentItem(fragmentType);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to view recipe");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create a fragment item
     */
    private static ItemStack createFragmentItem(FragmentType fragmentType) {
        // Use PAPER as base material (same as FragmentActivateGUI) for resource pack textures
        ItemStack item = new ItemStack(TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getFragmentColor(fragmentType) + "§l" + fragmentType.getDisplayName() + " Fragment");
            meta.setCustomModelData(TextureRegistry.getFragmentTexture(fragmentType));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Get color code for fragment type
     */
    private static String getFragmentColor(FragmentType type) {
        return switch (type) {
            case FIRE -> "§c";      // Red
            case WATER -> "§b";     // Aqua
            case AIR -> "§f";       // White
            case DARK -> "§5";      // Dark Purple
            case LIGHT -> "§e";     // Yellow
            case VOID -> "§d";      // Light Purple
            case DRAGON -> "§4";    // Dark Red
            case STORM -> "§9";     // Blue
            case TIME -> "§3";      // Dark Aqua
            case LUCK -> "§a";      // Green
            default -> "§7";        // Gray
        };
    }

    /**
     * Get the recipe pattern for a fragment type
     */
    private static RecipePattern getRecipePattern(FragmentType fragmentType) {
        return switch (fragmentType) {
            case FIRE -> new RecipePattern(
                new ItemStack(Material.NETHERRACK),
                new ItemStack(Material.FIRE_CHARGE),
                new ItemStack(Material.NETHERRACK),
                new ItemStack(Material.FIRE_CHARGE),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.FIRE_CHARGE),
                new ItemStack(Material.NETHERRACK),
                new ItemStack(Material.FIRE_CHARGE),
                new ItemStack(Material.NETHERRACK)
            );
            case WATER -> new RecipePattern(
                new ItemStack(Material.PRISMARINE),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.PRISMARINE),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.PRISMARINE),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.PRISMARINE)
            );
            case AIR -> new RecipePattern(
                new ItemStack(Material.FEATHER),
                new ItemStack(Material.PHANTOM_MEMBRANE),
                new ItemStack(Material.FEATHER),
                new ItemStack(Material.PHANTOM_MEMBRANE),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.PHANTOM_MEMBRANE),
                new ItemStack(Material.FEATHER),
                new ItemStack(Material.PHANTOM_MEMBRANE),
                new ItemStack(Material.FEATHER)
            );
            case DARK -> new RecipePattern(
                new ItemStack(Material.OBSIDIAN),
                new ItemStack(Material.WITHER_SKELETON_SKULL),
                new ItemStack(Material.OBSIDIAN),
                new ItemStack(Material.WITHER_SKELETON_SKULL),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.WITHER_SKELETON_SKULL),
                new ItemStack(Material.OBSIDIAN),
                new ItemStack(Material.WITHER_SKELETON_SKULL),
                new ItemStack(Material.OBSIDIAN)
            );
            case LIGHT -> new RecipePattern(
                new ItemStack(Material.GLOWSTONE),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.GLOWSTONE),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.BEACON),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.GLOWSTONE),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.GLOWSTONE)
            );
            case VOID -> new RecipePattern(
                new ItemStack(Material.ECHO_SHARD),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.ECHO_SHARD),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.SCULK_CATALYST),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.ECHO_SHARD),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.ECHO_SHARD)
            );
            case DRAGON -> new RecipePattern(
                new ItemStack(Material.DRAGON_HEAD),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.DRAGON_HEAD),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.DRAGON_EGG),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.DRAGON_HEAD),
                new ItemStack(Material.NETHERITE_INGOT),
                new ItemStack(Material.DRAGON_HEAD)
            );
            case STORM -> new RecipePattern(
                new ItemStack(Material.LIGHTNING_ROD),
                new ItemStack(Material.TRIDENT),
                new ItemStack(Material.LIGHTNING_ROD),
                new ItemStack(Material.TRIDENT),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.TRIDENT),
                new ItemStack(Material.LIGHTNING_ROD),
                new ItemStack(Material.TRIDENT),
                new ItemStack(Material.LIGHTNING_ROD)
            );
            case TIME -> new RecipePattern(
                new ItemStack(Material.AMETHYST_SHARD),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.AMETHYST_SHARD),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.CLOCK),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.AMETHYST_SHARD),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.AMETHYST_SHARD)
            );
            case LUCK -> new RecipePattern(
                new ItemStack(Material.GOLD_INGOT),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.GOLD_INGOT),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.RABBIT_FOOT),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.GOLD_INGOT),
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.GOLD_INGOT)
            );
            default -> new RecipePattern(
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER),
                new ItemStack(Material.BARRIER)
            );
        };
    }

    /**
     * Helper class to store recipe pattern
     */
    private static class RecipePattern {
        final ItemStack topLeft, topCenter, topRight;
        final ItemStack middleLeft, middleCenter, middleRight;
        final ItemStack bottomLeft, bottomCenter, bottomRight;

        RecipePattern(ItemStack topLeft, ItemStack topCenter, ItemStack topRight,
                     ItemStack middleLeft, ItemStack middleCenter, ItemStack middleRight,
                     ItemStack bottomLeft, ItemStack bottomCenter, ItemStack bottomRight) {
            this.topLeft = topLeft;
            this.topCenter = topCenter;
            this.topRight = topRight;
            this.middleLeft = middleLeft;
            this.middleCenter = middleCenter;
            this.middleRight = middleRight;
            this.bottomLeft = bottomLeft;
            this.bottomCenter = bottomCenter;
            this.bottomRight = bottomRight;
        }
    }
}
