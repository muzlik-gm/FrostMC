package com.muzlik.ui;

import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityDefinition;
import com.muzlik.fragment.ability.AbilityScalingEngine;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating ability icons with status indicators.
 * Shows unlock status, cooldowns, and detailed ability information.
 */
public class AbilityIconBuilder {
    private final AbilityDefinition ability;
    private final Player player;
    private final FragmentType fragmentType;
    private final RankManager rankManager;
    private final LevelManager levelManager;
    private final CooldownManager cooldownManager;
    private final AbilityScalingEngine scalingEngine;
    
    private Material customMaterial;
    private Integer customModelData;

    public AbilityIconBuilder(AbilityDefinition ability, Player player, FragmentType fragmentType,
                              RankManager rankManager, LevelManager levelManager,
                              CooldownManager cooldownManager) {
        this.ability = ability;
        this.player = player;
        this.fragmentType = fragmentType;
        this.rankManager = rankManager;
        this.levelManager = levelManager;
        this.cooldownManager = cooldownManager;
        this.scalingEngine = new AbilityScalingEngine();
    }

    /**
     * Set custom material (overrides default)
     */
    public AbilityIconBuilder material(Material material) {
        this.customMaterial = material;
        return this;
    }

    /**
     * Set custom model data for resource pack support
     */
    public AbilityIconBuilder customModelData(int modelData) {
        this.customModelData = modelData;
        return this;
    }

    /**
     * Build the ItemStack with all configured properties
     */
    public ItemStack build() {
        // Check unlock status
        int playerRank = rankManager.getRank(player, fragmentType);
        int playerLevel = levelManager.getLevel(player, fragmentType);
        boolean isUnlocked = playerRank >= ability.getRankRequirement() && 
                            playerLevel >= ability.getLevelRequirement();
        boolean isOnCooldown = cooldownManager.isOnCooldown(player, ability.getId());
        
        // Use texture system: single base material with custom model data
        Material material = com.muzlik.texture.TextureRegistry.getBaseMaterial();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Set display name with status indicator
        String displayName = buildDisplayName(isUnlocked, isOnCooldown);
        meta.setDisplayName(displayName);

        // Build lore with detailed information
        List<String> lore = buildLore(isUnlocked, isOnCooldown, playerRank, playerLevel);
        meta.setLore(lore);

        // Add enchantment glow for unlocked abilities
        if (isUnlocked && !isOnCooldown) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Set custom model data from texture registry
        int textureId = customModelData != null ? customModelData : 
                com.muzlik.texture.TextureRegistry.getAbilityTexture(ability.getId());
        meta.setCustomModelData(textureId);

        // Hide all other flags
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Build display name with status indicator and slot symbol
     */
    private String buildDisplayName(boolean isUnlocked, boolean isOnCooldown) {
        String statusSymbol;
        String color;
        
        if (!isUnlocked) {
            statusSymbol = "§c" + com.muzlik.texture.FragmentSymbols.LOCKED + " ";
            color = "§7";
        } else if (isOnCooldown) {
            statusSymbol = "§e" + com.muzlik.texture.FragmentSymbols.COOLDOWN + " ";
            color = "§7";
        } else {
            statusSymbol = "§a" + com.muzlik.texture.FragmentSymbols.UNLOCKED + " ";
            color = "§b";
        }
        
        String slotSymbol = com.muzlik.texture.FragmentSymbols.getAbilitySlotSymbol(ability.getSlot());
        return statusSymbol + color + slotSymbol + " " + ability.getDisplayName();
    }

    /**
     * Build comprehensive lore with ability details and status
     */
    private List<String> buildLore(boolean isUnlocked, boolean isOnCooldown, 
                                   int playerRank, int playerLevel) {
        List<String> lore = new ArrayList<>();
        
        // Ability description
        lore.add("§7" + ability.getDescription());
        lore.add("");
        
        // Slot information
        lore.add("§7Slot: §e" + ability.getSlot().getDisplayName() + " §8(Slot " + 
                ability.getSlot().getSlotIndex() + ")");
        lore.add("");
        
        if (isUnlocked) {
            // Show scaled stats for unlocked abilities
            addUnlockedAbilityStats(lore, playerRank);
            
            // Cooldown status
            if (isOnCooldown) {
                double remaining = cooldownManager.getRemainingCooldownSeconds(player, ability.getId());
                lore.add("");
                lore.add("§c⏱ Cooldown: §e" + formatTime(remaining));
                lore.add("§7Please wait before using again");
            } else {
                lore.add("");
                lore.add("§a§l✓ READY");
                lore.add("§7This ability is ready to use");
            }
            
        } else {
            // Show unlock requirements for locked abilities
            addLockedAbilityInfo(lore, playerRank, playerLevel);
        }
        
        return lore;
    }

    /**
     * Add stats for unlocked abilities
     */
    private void addUnlockedAbilityStats(List<String> lore, int playerRank) {
        // Base mana cost
        double baseMana = ability.getManaCost();
        double scaledMana = scalingEngine.scaleManaCost(baseMana, playerRank);
        
        lore.add("§7Mana Cost:");
        lore.add("  §b" + String.format("%.0f", scaledMana) + " §7mana");
        
        // Cooldown
        double cooldownSeconds = ability.getCooldown() / 1000.0;
        lore.add("§7Cooldown:");
        lore.add("  §e" + String.format("%.1f", cooldownSeconds) + "s");
        
        // Rank scaling info
        if (playerRank > 1) {
            lore.add("");
            lore.add("§7Rank Scaling: §6" + playerRank);
            lore.add("§7Power increased by rank bonuses");
        }
    }

    /**
     * Add unlock requirements for locked abilities
     */
    private void addLockedAbilityInfo(List<String> lore, int playerRank, int playerLevel) {
        int rankReq = ability.getRankRequirement();
        int levelReq = ability.getLevelRequirement();
        
        lore.add("§c§l✗ LOCKED");
        lore.add("");
        lore.add("§7Requirements:");
        
        // Rank requirement
        if (playerRank < rankReq) {
            int ranksAway = rankReq - playerRank;
            String rankColor = ranksAway == 1 ? "§e⚠" : "§c✗";
            lore.add("  " + rankColor + " §7Rank §6" + rankReq + " §7(you are §6" + playerRank + "§7)");
            
            if (ranksAway == 1) {
                lore.add("    §e§l⚠ Only 1 rank away!");
            } else {
                lore.add("    §7" + ranksAway + " ranks away");
            }
        } else {
            lore.add("  §a✓ §7Rank §6" + rankReq);
        }
        
        // Level requirement
        if (levelReq > 0) {
            if (playerLevel < levelReq) {
                int levelsAway = levelReq - playerLevel;
                String levelColor = levelsAway <= 2 ? "§e⚠" : "§c✗";
                lore.add("  " + levelColor + " §7Level §e" + levelReq + " §7(you are §e" + playerLevel + "§7)");
                
                if (levelsAway == 1) {
                    lore.add("    §e§l⚠ Only 1 level away!");
                } else if (levelsAway == 2) {
                    lore.add("    §e§l⚠ Only 2 levels away!");
                } else {
                    lore.add("    §7" + levelsAway + " levels away");
                }
            } else {
                lore.add("  §a✓ §7Level §e" + levelReq);
            }
        }
    }

    /**
     * Format time in human-readable format
     */
    private String formatTime(double seconds) {
        if (seconds < 60) {
            return String.format("%.1fs", seconds);
        } else if (seconds < 3600) {
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            return String.format("%dm %ds", minutes, secs);
        } else {
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            return String.format("%dh %dm", hours, minutes);
        }
    }

    /**
     * Get default material for ability based on slot
     */
    private Material getAbilityMaterial() {
        return switch (ability.getSlot()) {
            case PRIMARY -> Material.IRON_SWORD;
            case SECONDARY -> Material.BOW;
            case ULTIMATE -> Material.DIAMOND_SWORD;
            case ADVANCED -> Material.GOLDEN_SWORD;
            case MASTERY -> Material.NETHERITE_SWORD;
        };
    }
}
