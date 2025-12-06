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
 * Clean minimal design matching the Fragment GUI style.
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
     * Build display name with status indicator - CLEAN MINIMAL STYLE
     */
    private String buildDisplayName(boolean isUnlocked, boolean isOnCooldown) {
        String name = toSmallCaps(ability.getDisplayName());
        String slotNum = String.valueOf(ability.getSlot().getSlotIndex() + 1);
        
        if (!isUnlocked) {
            return "§8§l" + slotNum + " §c✗ §8" + name;
        } else if (isOnCooldown) {
            return "§8§l" + slotNum + " §e⏱ §7" + name;
        } else {
            return "§8§l" + slotNum + " §a✓ §f" + name;
        }
    }

    /**
     * Build comprehensive lore with ability details - CLEAN MINIMAL STYLE
     */
    private List<String> buildLore(boolean isUnlocked, boolean isOnCooldown, 
                                   int playerRank, int playerLevel) {
        List<String> lore = new ArrayList<>();
        
        // Description
        lore.add("§8" + ability.getDescription());
        lore.add("");
        
        // Slot info
        lore.add("§7sʟᴏᴛ §f" + ability.getSlot().getDisplayName());
        lore.add("");
        
        if (isUnlocked) {
            // Stats for unlocked abilities
            double baseMana = ability.getManaCost();
            double scaledMana = scalingEngine.scaleManaCost(baseMana, playerRank);
            double cooldownSeconds = ability.getCooldown() / 1000.0;
            
            // Apply level bonuses
            double manaReduction = levelManager.getManaCostReduction(player, fragmentType);
            double cdReduction = levelManager.getCooldownReduction(player, fragmentType);
            double finalMana = scaledMana * (1 - manaReduction);
            double finalCooldown = cooldownSeconds * (1 - cdReduction);
            
            lore.add("§7ᴍᴀɴᴀ §b" + String.format("%.0f", finalMana));
            if (manaReduction > 0) {
                lore.add("  §8(-" + String.format("%.0f", manaReduction * 100) + "% from level)");
            }
            
            lore.add("§7ᴄᴏᴏʟᴅᴏᴡɴ §e" + String.format("%.1f", finalCooldown) + "s");
            if (cdReduction > 0) {
                lore.add("  §8(-" + String.format("%.0f", cdReduction * 100) + "% from level)");
            }
            
            // Show current cooldown if on cooldown
            if (isOnCooldown) {
                double remaining = cooldownManager.getRemainingCooldownSeconds(player, ability.getId());
                lore.add("");
                lore.add("§c⏱ " + formatTime(remaining) + " §7ʀᴇᴍᴀɪɴɪɴɢ");
            } else {
                lore.add("");
                lore.add("§a▸ ʀᴇᴀᴅʏ ᴛᴏ ᴜsᴇ");
            }
            
            // Usage hint
            lore.add("");
            lore.add("§8sɴᴇᴀᴋ + ᴄʟɪᴄᴋ ᴏɴ ʜᴏᴛʙᴀʀ");
            
        } else {
            // Requirements for locked abilities
            lore.add("§c✗ ʟᴏᴄᴋᴇᴅ");
            lore.add("");
            
            int rankReq = ability.getRankRequirement();
            int levelReq = ability.getLevelRequirement();
            
            // Rank requirement
            if (playerRank < rankReq) {
                int ranksAway = rankReq - playerRank;
                if (ranksAway == 1) {
                    lore.add("§e⚠ ʀᴀɴᴋ " + rankReq + " §8(1 ᴀᴡᴀʏ)");
                } else {
                    lore.add("§c✗ ʀᴀɴᴋ " + rankReq + " §8(" + ranksAway + " ᴀᴡᴀʏ)");
                }
            } else {
                lore.add("§a✓ ʀᴀɴᴋ " + rankReq);
            }
            
            // Level requirement
            if (levelReq > 0) {
                if (playerLevel < levelReq) {
                    int levelsAway = levelReq - playerLevel;
                    if (levelsAway <= 2) {
                        lore.add("§e⚠ ʟᴇᴠᴇʟ " + levelReq + " §8(" + levelsAway + " ᴀᴡᴀʏ)");
                    } else {
                        lore.add("§c✗ ʟᴇᴠᴇʟ " + levelReq + " §8(" + levelsAway + " ᴀᴡᴀʏ)");
                    }
                } else {
                    lore.add("§a✓ ʟᴇᴠᴇʟ " + levelReq);
                }
            }
        }
        
        return lore;
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
