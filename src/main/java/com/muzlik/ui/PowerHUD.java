package com.muzlik.ui;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import com.muzlik.power.PowerManager;
import com.muzlik.power.IPower;
import com.muzlik.power.IAbility;
import com.muzlik.cooldown.CooldownManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clean, minimalistic HUD system for displaying power information
 * Shows: Token name, ability cooldowns, and status messages
 */
public class PowerHUD {
    
    private final Plugin plugin;
    private final PowerManager powerManager;
    private final Map<String, BossBar> playerBossBars = new ConcurrentHashMap<>();
    private BukkitRunnable hudTask;
    
    public PowerHUD(Plugin plugin, PowerManager powerManager) {
        this.plugin = plugin;
        this.powerManager = powerManager;
    }
    
    /**
     * Start the HUD update task
     */
    public void start() {
        hudTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateHUD(player);
                }
            }
        };
        hudTask.runTaskTimer(plugin, 0L, 20L); // Update every second
    }
    
    /**
     * Stop the HUD system
     */
    public void stop() {
        if (hudTask != null) {
            hudTask.cancel();
        }
        
        // Remove all boss bars
        for (BossBar bar : playerBossBars.values()) {
            bar.removeAll();
        }
        playerBossBars.clear();
    }
    
    /**
     * Update HUD for a player
     */
    private void updateHUD(Player player) {
        IPower power = powerManager.getPlayerActivePower(player);
        
        if (power == null) {
            // No power active, clear HUD
            clearHUD(player);
            return;
        }
        
        // Build actionbar message with cooldowns
        String actionBar = buildActionBar(player, power);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBar));
    }
    
    /**
     * Build clean actionbar display with modern UI
     */
    private String buildActionBar(Player player, IPower power) {
        StringBuilder sb = new StringBuilder();
        
        // Power name with modern styling
        sb.append("§8[§6⚡§8] §e§l").append(power.getDisplayName()).append(" §r§8▏ ");
        
        // Ability cooldowns with visual indicators
        CooldownManager cooldownManager = powerManager.getCooldownManager();
        int currentSlot = player.getInventory().getHeldItemSlot();
        
        for (int i = 0; i < 2; i++) { // Only show 2 abilities since tertiary doesn't exist
            IAbility ability = power.getAbilityManager().getAbility(i);
            if (ability != null) {
                // Highlight current slot
                boolean isCurrentSlot = (currentSlot == i);
                String slotSymbol = getSlotSymbol(i);
                String abilityShortName = getAbilityShortName(ability.getDisplayName());
                
                if (isCurrentSlot) {
                    sb.append("§f§l");
                }
                
                sb.append(slotSymbol).append(" ");
                
                if (cooldownManager.isOnCooldown(player, ability.getId())) {
                    double remaining = cooldownManager.getRemainingCooldownSeconds(player, ability.getId());
                    sb.append("§c").append(abilityShortName);
                    sb.append(" §c").append(String.format("%.0f", remaining)).append("s");
                } else {
                    sb.append("§a").append(abilityShortName).append(" §a✓");
                }
                
                if (isCurrentSlot) {
                    sb.append(" §8◀");
                }
                
                sb.append(" §8▏ ");
            }
        }
        
        // Sneak indicator
        if (player.isSneaking()) {
            sb.append("§b§l⇧ READY");
        } else {
            sb.append("§7⇧ Sneak");
        }
        
        return sb.toString();
    }
    
    /**
     * Get slot symbol with color
     */
    private String getSlotSymbol(int slot) {
        switch (slot) {
            case 0: return "§6❶";
            case 1: return "§d❷";
            default: return "§7?";
        }
    }
    
    /**
     * Get shortened ability name
     */
    private String getAbilityShortName(String fullName) {
        // Shorten long names for cleaner display
        if (fullName.length() > 12) {
            return fullName.substring(0, 10) + "..";
        }
        return fullName;
    }
    
    // Removed cooldown bar to prevent action bar clutter
    
    /**
     * Clear HUD for player
     */
    private void clearHUD(Player player) {
        String uuid = player.getUniqueId().toString();
        BossBar bar = playerBossBars.remove(uuid);
        if (bar != null) {
            bar.removePlayer(player);
        }
    }
    
    /**
     * Show temporary message to player
     */
    public void showMessage(Player player, String message, MessageType type) {
        switch (type) {
            case SUCCESS:
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent("§a✓ " + message));
                break;
            case ERROR:
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent("§c✗ " + message));
                break;
            case INFO:
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent("§7ℹ " + message));
                break;
        }
    }
    
    /**
     * Show ability ready notification
     */
    public void showAbilityReady(Player player, String abilityName) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
            new TextComponent("§a✓ §b" + abilityName + " §ais ready!"));
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2.0f);
    }
    
    public enum MessageType {
        SUCCESS, ERROR, INFO
    }
}
