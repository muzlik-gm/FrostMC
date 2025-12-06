package com.muzlik.ui;

import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.IFragmentAbility;
import com.muzlik.mana.ManaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Displays Fragment ability information on the action bar
 * Shows: Fragment name, current slot, all abilities, cooldowns, lock status
 */
public class FragmentActionBarHUD {
    
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final ManaManager manaManager;
    private final CooldownManager cooldownManager;
    private BukkitRunnable updateTask;
    
    public FragmentActionBarHUD(JavaPlugin plugin, FragmentManager fragmentManager, 
                                ManaManager manaManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.cooldownManager = cooldownManager;
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
        
        // Update every 10 ticks (0.5 seconds)
        updateTask.runTaskTimer(plugin, 0L, 10L);
        
        plugin.getLogger().info("FragmentActionBarHUD started");
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
     * Build the HUD message component - CLEAN MINIMAL STYLE
     */
    private Component buildHUDMessage(Player player, FragmentType fragmentType, int currentSlot) {
        Component message = Component.empty();
        
        // Fragment name with color - CLEAN STYLE
        TextColor fragmentColor = getFragmentColor(fragmentType);
        String fragmentName = toSmallCaps(fragmentType.getDisplayName());
        message = message.append(Component.text(fragmentName + " ", fragmentColor));
        
        // Mana display - CLEAN STYLE
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        message = message.append(Component.text(String.format("%.0f", currentMana), NamedTextColor.WHITE));
        message = message.append(Component.text("/", NamedTextColor.DARK_GRAY));
        message = message.append(Component.text(String.format("%.0f", maxMana), NamedTextColor.GRAY));
        message = message.append(Component.text(" ", NamedTextColor.DARK_GRAY));
        
        // Ability slots (0-4) - CLEAN STYLE
        for (int slot = 0; slot <= 4; slot++) {
            IFragmentAbility ability = fragmentManager.getAbility(player, fragmentType, slot);
            
            boolean isCurrentSlot = (slot == currentSlot);
            
            if (ability != null) {
                // Ability exists
                String abilityId = fragmentType.name() + "_" + ability.getName();
                boolean onCooldown = cooldownManager.isOnCooldown(player, abilityId);
                
                if (isCurrentSlot) {
                    // Current slot - highlighted with [■]
                    message = message.append(Component.text("[", NamedTextColor.WHITE));
                    
                    if (onCooldown) {
                        double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                        message = message.append(Component.text(String.format("%.0f", remaining), NamedTextColor.DARK_GRAY));
                    } else {
                        message = message.append(Component.text("■", NamedTextColor.GREEN));
                    }
                    
                    message = message.append(Component.text("]", NamedTextColor.WHITE));
                } else {
                    // Other slots
                    if (onCooldown) {
                        double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
                        message = message.append(Component.text(String.format("%.0f", remaining), NamedTextColor.DARK_GRAY));
                    } else {
                        message = message.append(Component.text("■", NamedTextColor.DARK_GRAY));
                    }
                }
            } else {
                // Locked slot
                if (isCurrentSlot) {
                    message = message.append(Component.text("[", NamedTextColor.WHITE));
                    message = message.append(Component.text("□", NamedTextColor.DARK_GRAY));
                    message = message.append(Component.text("]", NamedTextColor.WHITE));
                } else {
                    message = message.append(Component.text("□", NamedTextColor.DARK_GRAY));
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
     * Get color for fragment type
     */
    private TextColor getFragmentColor(FragmentType type) {
        return switch (type) {
            case FIRE -> NamedTextColor.RED;
            case WATER -> NamedTextColor.BLUE;
            case AIR -> NamedTextColor.WHITE;
            case EARTH -> NamedTextColor.GOLD;
            case DARK -> NamedTextColor.DARK_PURPLE;
            case LIGHT -> NamedTextColor.YELLOW;
            case VOID -> NamedTextColor.DARK_GRAY;
            case MOB -> NamedTextColor.GREEN;
            case DRAGON -> NamedTextColor.DARK_RED;
            case STORM -> NamedTextColor.AQUA;
        };
    }
}
