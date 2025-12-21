package com.muzlik.fragment.ability.executors.luck;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.PlayerFragmentData;
import com.muzlik.util.PotionEffectHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages passive abilities for the Luck Fragment
 * 
 * Passive Effects:
 * - Permanent Luck VI effect (hidden particles)
 * - Temporary Fortune V on held tools (pickaxe, axe, shovel, hoe)
 * - Original enchantment levels are stored and restored
 */
public class LuckFragmentPassiveManager {
    
    private final Plugin plugin;
    private final FragmentManager fragmentManager;
    
    // UUID -> (Material -> Original Fortune Level)
    private final Map<UUID, Map<Material, Integer>> originalEnchantments;
    
    // Track last held item to detect changes
    private final Map<UUID, Material> lastHeldItem;
    
    private BukkitRunnable updateTask;
    
    // Tools that receive Fortune V
    private static final Material[] FORTUNE_TOOLS = {
        Material.DIAMOND_PICKAXE,
        Material.IRON_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.STONE_PICKAXE,
        Material.WOODEN_PICKAXE,
        Material.NETHERITE_PICKAXE,
        Material.DIAMOND_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.STONE_AXE,
        Material.WOODEN_AXE,
        Material.NETHERITE_AXE,
        Material.DIAMOND_SHOVEL,
        Material.IRON_SHOVEL,
        Material.GOLDEN_SHOVEL,
        Material.STONE_SHOVEL,
        Material.WOODEN_SHOVEL,
        Material.NETHERITE_SHOVEL,
        Material.DIAMOND_HOE,
        Material.IRON_HOE,
        Material.GOLDEN_HOE,
        Material.STONE_HOE,
        Material.WOODEN_HOE,
        Material.NETHERITE_HOE
    };
    
    public LuckFragmentPassiveManager(Plugin plugin, FragmentManager fragmentManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.originalEnchantments = new HashMap<>();
        this.lastHeldItem = new HashMap<>();
    }
    
    /**
     * Start the passive manager
     */
    public void start() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updatePlayer(player);
                }
            }
        };
        
        // Check every 20 ticks (1 second)
        updateTask.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Stop the passive manager
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // Restore all enchantments
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            deactivatePassives(player);
        }
    }
    
    /**
     * Update a player's passive effects
     * @param player The player to update
     */
    private void updatePlayer(Player player) {
        PlayerFragmentData fragmentData = fragmentManager.getPlayerData(player);
        
        if (fragmentData == null) {
            return;
        }
        
        // Check if Luck fragment is active
        boolean hasLuckActive = fragmentData.getActiveFragment() == FragmentType.LUCK;
        
        if (hasLuckActive) {
            activatePassives(player);
        } else {
            deactivatePassives(player);
        }
    }
    
    /**
     * Activate passive effects for a player
     * @param player The player
     */
    private void activatePassives(Player player) {
        // Apply permanent Luck VI effect with hidden particles
        if (!player.hasPotionEffect(PotionEffectType.LUCK) || 
            player.getPotionEffect(PotionEffectType.LUCK).getAmplifier() < 5) {
            player.addPotionEffect(PotionEffectHelper.createHiddenEffect(
                PotionEffectType.LUCK,
                Integer.MAX_VALUE, // Permanent
                5 // Luck VI (amplifier 5 = level 6)
            ));
        }
        
        // Apply Fortune V to held tool
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        if (heldItem != null && isFortuneTool(heldItem.getType())) {
            Material currentMaterial = heldItem.getType();
            Material lastMaterial = lastHeldItem.get(player.getUniqueId());
            
            // Check if player switched tools
            if (lastMaterial != null && lastMaterial != currentMaterial) {
                // Restore previous tool if it's still in inventory
                restoreToolEnchantment(player, lastMaterial);
            }
            
            // Update last held item
            lastHeldItem.put(player.getUniqueId(), currentMaterial);
            
            // Apply Fortune V
            applyFortuneV(player, heldItem);
        } else {
            // Player is not holding a tool, restore if they were
            Material lastMaterial = lastHeldItem.get(player.getUniqueId());
            if (lastMaterial != null) {
                restoreToolEnchantment(player, lastMaterial);
                lastHeldItem.remove(player.getUniqueId());
            }
        }
    }
    
    /**
     * Deactivate passive effects for a player
     * @param player The player
     */
    private void deactivatePassives(Player player) {
        // Remove Luck effect
        player.removePotionEffect(PotionEffectType.LUCK);
        
        // Restore all tool enchantments
        Material lastMaterial = lastHeldItem.get(player.getUniqueId());
        if (lastMaterial != null) {
            restoreToolEnchantment(player, lastMaterial);
            lastHeldItem.remove(player.getUniqueId());
        }
        
        // Clear stored enchantments
        originalEnchantments.remove(player.getUniqueId());
    }
    
    /**
     * Apply Fortune V to a tool
     * @param player The player
     * @param tool The tool item
     */
    private void applyFortuneV(Player player, ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }
        
        // Store original Fortune level if not already stored
        UUID playerId = player.getUniqueId();
        Material material = tool.getType();
        
        if (!originalEnchantments.containsKey(playerId)) {
            originalEnchantments.put(playerId, new HashMap<>());
        }
        
        Map<Material, Integer> playerEnchants = originalEnchantments.get(playerId);
        
        if (!playerEnchants.containsKey(material)) {
            int originalLevel = meta.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            playerEnchants.put(material, originalLevel);
        }
        
        // Apply Fortune V (only if not already at V or higher)
        if (meta.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS) < 5) {
            meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 5, true);
            tool.setItemMeta(meta);
        }
    }
    
    /**
     * Restore original enchantment to a tool
     * @param player The player
     * @param material The tool material
     */
    private void restoreToolEnchantment(Player player, Material material) {
        UUID playerId = player.getUniqueId();
        
        if (!originalEnchantments.containsKey(playerId)) {
            return;
        }
        
        Map<Material, Integer> playerEnchants = originalEnchantments.get(playerId);
        Integer originalLevel = playerEnchants.get(material);
        
        if (originalLevel == null) {
            return;
        }
        
        // Find the tool in inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Remove Fortune enchantment
                    meta.removeEnchant(Enchantment.LOOT_BONUS_BLOCKS);
                    
                    // Restore original level if it was > 0
                    if (originalLevel > 0) {
                        meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, originalLevel, true);
                    }
                    
                    item.setItemMeta(meta);
                }
                break;
            }
        }
        
        // Remove from stored enchantments
        playerEnchants.remove(material);
    }
    
    /**
     * Check if a material is a Fortune tool
     * @param material The material to check
     * @return True if it's a Fortune tool
     */
    private boolean isFortuneTool(Material material) {
        for (Material tool : FORTUNE_TOOLS) {
            if (tool == material) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Handle player logout - restore enchantments
     * @param player The player logging out
     */
    public void onPlayerLogout(Player player) {
        deactivatePassives(player);
    }
    
    /**
     * Store original enchantment level
     * @param playerId The player's UUID
     * @param material The tool material
     * @param level The original Fortune level
     */
    public void storeOriginalEnchantment(UUID playerId, Material material, int level) {
        originalEnchantments
            .computeIfAbsent(playerId, k -> new HashMap<>())
            .put(material, level);
    }
    
    /**
     * Get original enchantment level
     * @param playerId The player's UUID
     * @param material The tool material
     * @return The original Fortune level, or 0 if not stored
     */
    public int getOriginalEnchantment(UUID playerId, Material material) {
        return originalEnchantments
            .getOrDefault(playerId, new HashMap<>())
            .getOrDefault(material, 0);
    }
}
