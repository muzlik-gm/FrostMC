package com.muzlik.ui;

import com.muzlik.character.CharacterLevelManager;
import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.IFragmentAbility;
import com.muzlik.mana.ManaManager;
import com.muzlik.texture.TextureRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Displays Fragment ability information on the action bar
 * Shows: Character Level, Fragment name, Mana, all abilities, cooldowns, lock status
 * 
 * Format: ✦ LVL: X/MAX | Fragment ⚡ Mana [abilities]
 */
public class FragmentActionBarHUD {
    
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final ManaManager manaManager;
    private final CooldownManager cooldownManager;
    private CharacterLevelManager characterLevelManager;
    private BukkitRunnable updateTask;
    
    public FragmentActionBarHUD(JavaPlugin plugin, FragmentManager fragmentManager, 
                                ManaManager manaManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.cooldownManager = cooldownManager;
    }
    
    /**
     * Set CharacterLevelManager reference
     */
    public void setCharacterLevelManager(CharacterLevelManager characterLevelManager) {
        this.characterLevelManager = characterLevelManager;
    }
    
    /**
     * Start the HUD update task
     */
    public void start() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateHUD(player);
                }
            }
        };
        
        // Update every 10 ticks (0.5 second) - OPTIMIZED FOR PERFORMANCE
        // Reduced from 5 ticks to reduce packet spam and improve ping
        updateTask.runTaskTimer(plugin, 0L, 10L);
    }
    
    /**
     * Stop the HUD update task
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    /**
     * Update HUD for a specific player
     */
    private void updateHUD(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            return; // No fragment active
        }
        
        int currentSlot = player.getInventory().getHeldItemSlot();
        
        // Build HUD message
        Component message = buildHUDMessage(player, activeFragment, currentSlot);
        
        // Send to action bar
        player.sendActionBar(message);
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
     * Build the HUD message component - PREMIUM STYLE WITH LEVEL + MANA
     * Format: [Icon] ✦ LVL: X/MAX | Fragment ⚡ Mana/Max [abilities]
     * ADMIN FRAGMENT: Custom format with warning symbols
     */
    private Component buildHUDMessage(Player player, FragmentType fragmentType, int currentSlot) {
        // CUSTOM ADMIN FRAGMENT ACTIONBAR
        if (fragmentType == FragmentType.ADMIN) {
            return buildAdminHUDMessage(player, currentSlot);
        }
        
        Component message = Component.empty();
        
        // ═══ FRAGMENT ICON ═══
        // Display fragment texture using Unicode private use area character
        // The resource pack maps this to the fragment texture
        String fragmentIcon = getFragmentIcon(fragmentType);
        TextColor fragmentColor = getFragmentColor(fragmentType);
        message = message.append(Component.text(fragmentIcon + " ", fragmentColor));
        
        // ═══ CHARACTER LEVEL SECTION ═══
        // Format: ✦ LVL: X/MAX
        // Only show if mana system is enabled (character level affects max mana)
        if (characterLevelManager != null && manaManager.isManaSystemEnabled()) {
            int characterLevel = characterLevelManager.getCharacterLevel(player);
            int maxLevel = characterLevelManager.getMaxCharacterLevel();
            
            // Star emoji for level indicator
            message = message.append(Component.text("✦ ", NamedTextColor.GOLD));
            message = message.append(Component.text("ʟᴠʟ ", NamedTextColor.GRAY));
            
            // Current level - gold if max, otherwise white
            if (characterLevel >= maxLevel) {
                message = message.append(Component.text(String.valueOf(characterLevel), NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD));
            } else {
                message = message.append(Component.text(String.valueOf(characterLevel), NamedTextColor.WHITE));
            }
            
            message = message.append(Component.text("/", NamedTextColor.DARK_GRAY));
            message = message.append(Component.text(String.valueOf(maxLevel), NamedTextColor.GRAY));
            
            // Separator
            message = message.append(Component.text(" │ ", NamedTextColor.DARK_GRAY));
        }
        
        // ═══ FRAGMENT NAME SECTION ═══
        String fragmentName = toSmallCaps(fragmentType.getDisplayName());
        message = message.append(Component.text(fragmentName + " ", fragmentColor));
        
        // ═══ MANA DISPLAY ═══
        // Only show if mana system is enabled
        if (manaManager.isManaSystemEnabled()) {
            // Format: ⚡ Current/Max
            double currentMana = manaManager.getMana(player);
            double maxMana = manaManager.getMaxMana(player);
            
            // Calculate mana percentage for color coding
            double manaPercent = currentMana / maxMana;
            TextColor manaColor = manaPercent >= 0.5 ? NamedTextColor.AQUA : 
                                 (manaPercent >= 0.25 ? NamedTextColor.YELLOW : NamedTextColor.RED);
            
            message = message.append(Component.text("⚡", NamedTextColor.AQUA));
            message = message.append(Component.text(String.format("%.0f", currentMana), manaColor));
            message = message.append(Component.text("/", NamedTextColor.DARK_GRAY));
            message = message.append(Component.text(String.format("%.0f", maxMana), NamedTextColor.GRAY));
            message = message.append(Component.text(" ", NamedTextColor.DARK_GRAY));
        }
        
        // Get Fragment definition to check ability slots
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(fragmentType);
        
        // Ability slots (0-4) - IMPROVED STYLE
        // ■ = unlocked & ready (green)
        // ○ = locked but CAN be unlocked (yellow) 
        // ✗ = cannot be unlocked / no ability (dark red)
        for (int slot = 0; slot <= 4; slot++) {
            IFragmentAbility ability = fragmentManager.getAbility(player, fragmentType, slot);
            
            boolean isCurrentSlot = (slot == currentSlot);
            
            if (ability != null) {
                // ═══ UNLOCKED ABILITY ═══
                String abilityId = fragmentType.name() + "_" + ability.getName();
                boolean onCooldown = cooldownManager.isOnCooldown(player, abilityId);
                
                if (isCurrentSlot) {
                    // Current slot - highlighted with [■]
                    message = message.append(Component.text("[", NamedTextColor.WHITE));
                    
                    if (onCooldown) {
                        double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                        message = message.append(Component.text(String.format("%.0f", remaining), NamedTextColor.GOLD));
                    } else {
                        message = message.append(Component.text("■", NamedTextColor.GREEN));
                    }
                    
                    message = message.append(Component.text("]", NamedTextColor.WHITE));
                } else {
                    // Other slots - show ready or cooldown
                    if (onCooldown) {
                        double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                        message = message.append(Component.text(String.format("%.0f", remaining), NamedTextColor.GRAY));
                    } else {
                        message = message.append(Component.text("■", NamedTextColor.DARK_GRAY));
                    }
                }
            } else {
                // ═══ LOCKED OR UNAVAILABLE SLOT ═══
                // Check if there's an ability defined for this slot that could be unlocked
                boolean canUnlock = false;
                boolean hasAbilityDefined = false;
                
                if (fragment != null) {
                    for (com.muzlik.fragment.ability.AbilityDefinition abilityDef : fragment.getAbilities()) {
                        if (abilityDef.getSlot().getSlotIndex() == slot) {
                            hasAbilityDefined = true;
                            // There's an ability here, check if it can eventually be unlocked
                            // (rank requirement is achievable)
                            int maxRank = fragmentManager.getRankManager().getMaxRank(fragmentType);
                            if (abilityDef.getRankRequirement() <= maxRank) {
                                canUnlock = true;
                            }
                            break;
                        }
                    }
                }
                
                if (hasAbilityDefined && canUnlock) {
                    // ○ = Locked but CAN be unlocked (show as yellow circle)
                    if (isCurrentSlot) {
                        message = message.append(Component.text("[", NamedTextColor.WHITE));
                        message = message.append(Component.text("○", NamedTextColor.YELLOW));
                        message = message.append(Component.text("]", NamedTextColor.WHITE));
                    } else {
                        message = message.append(Component.text("○", NamedTextColor.GOLD));
                    }
                } else {
                    // ✗ = Cannot unlock / No ability defined (show as dark red cross)
                    if (isCurrentSlot) {
                        message = message.append(Component.text("[", NamedTextColor.WHITE));
                        message = message.append(Component.text("✗", NamedTextColor.DARK_RED));
                        message = message.append(Component.text("]", NamedTextColor.WHITE));
                    } else {
                        message = message.append(Component.text("✗", NamedTextColor.DARK_GRAY));
                    }
                }
            }
            
            // Separator
            if (slot < 4) {
                message = message.append(Component.text(" ", NamedTextColor.DARK_GRAY));
            }
        }
        
        return message;
    }
    
    /**
     * Build ADMIN fragment HUD message - Custom format with warning symbols
     * Format: §4§l⚠ §c§lADMIN MODE §4§l⚠ §8| §7Ability: §c[NAME] §8| §7CD: §e[TIME]
     */
    private Component buildAdminHUDMessage(Player player, int currentSlot) {
        Component message = Component.empty();
        
        // Warning symbols and ADMIN MODE text
        message = message.append(Component.text("⚠ ", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        message = message.append(Component.text("ADMIN MODE", NamedTextColor.RED).decorate(TextDecoration.BOLD));
        message = message.append(Component.text(" ⚠", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        message = message.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
        
        // Get current ability name
        com.muzlik.fragment.ability.IFragmentAbility ability = fragmentManager.getAbility(player, FragmentType.ADMIN, currentSlot);
        
        if (ability != null) {
            message = message.append(Component.text("Ability: ", NamedTextColor.GRAY));
            message = message.append(Component.text(ability.getName(), NamedTextColor.RED));
            
            // Check cooldown
            String abilityId = "ADMIN_" + ability.getName();
            if (cooldownManager.isOnCooldown(player, abilityId)) {
                double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                message = message.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
                message = message.append(Component.text("CD: ", NamedTextColor.GRAY));
                message = message.append(Component.text(String.format("%.1fs", remaining), NamedTextColor.YELLOW));
            } else {
                message = message.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
                message = message.append(Component.text("READY", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
            }
        } else {
            message = message.append(Component.text("No Ability", NamedTextColor.DARK_GRAY));
        }
        
        // Show all 9 ability slots (0-8)
        message = message.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
        
        for (int slot = 0; slot <= 8; slot++) {
            com.muzlik.fragment.ability.IFragmentAbility slotAbility = fragmentManager.getAbility(player, FragmentType.ADMIN, slot);
            
            boolean isCurrentSlot = (slot == currentSlot);
            
            if (slotAbility != null) {
                String abilityId = "ADMIN_" + slotAbility.getName();
                boolean onCooldown = cooldownManager.isOnCooldown(player, abilityId);
                
                if (isCurrentSlot) {
                    message = message.append(Component.text("[", NamedTextColor.WHITE));
                    if (onCooldown) {
                        double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                        message = message.append(Component.text(String.format("%.0f", remaining), NamedTextColor.GOLD));
                    } else {
                        message = message.append(Component.text("■", NamedTextColor.RED));
                    }
                    message = message.append(Component.text("]", NamedTextColor.WHITE));
                } else {
                    if (onCooldown) {
                        double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                        message = message.append(Component.text(String.format("%.0f", remaining), NamedTextColor.GRAY));
                    } else {
                        message = message.append(Component.text("■", NamedTextColor.DARK_RED));
                    }
                }
            } else {
                if (isCurrentSlot) {
                    message = message.append(Component.text("[", NamedTextColor.WHITE));
                    message = message.append(Component.text("✗", NamedTextColor.DARK_RED));
                    message = message.append(Component.text("]", NamedTextColor.WHITE));
                } else {
                    message = message.append(Component.text("✗", NamedTextColor.DARK_GRAY));
                }
            }
            
            if (slot < 8) {
                message = message.append(Component.text(" ", NamedTextColor.DARK_GRAY));
            }
        }
        
        return message;
    }
    
    /**
     * Get color for fragment type
     */
    private TextColor getFragmentColor(FragmentType type) {
        return switch (type) {
            case FIRE -> NamedTextColor.RED;
            case WATER -> NamedTextColor.BLUE;
            case AIR -> NamedTextColor.WHITE;
            case DARK -> NamedTextColor.BLACK; // Changed from DARK_PURPLE
            case LIGHT -> NamedTextColor.YELLOW;
            case VOID -> NamedTextColor.DARK_PURPLE; // Changed from DARK_GRAY
            case DRAGON -> NamedTextColor.DARK_RED;
            case STORM -> NamedTextColor.AQUA;
            case TIME -> NamedTextColor.YELLOW; // Yellow for time theme
            case LUCK -> NamedTextColor.GREEN; // Green for luck theme
            case ADMIN -> NamedTextColor.DARK_RED; // Admin fragment color
        };
    }
    
    /**
     * Get fragment icon character for display in action bar
     * Uses Unicode private use area characters that the resource pack maps to textures
     * Format: \uE000 + fragment custom model data offset
     */
    private String getFragmentIcon(FragmentType type) {
        // Get the custom model data for this fragment
        int customModelData = TextureRegistry.getFragmentTexture(type);
        
        // Map to Unicode private use area (U+E000 to U+F8FF)
        // We use the last 3 digits of the custom model data as offset
        // 1000 -> \uE000, 1001 -> \uE001, etc.
        int offset = customModelData % 1000;
        char iconChar = (char) (0xE000 + offset);
        
        return String.valueOf(iconChar);
    }
}
