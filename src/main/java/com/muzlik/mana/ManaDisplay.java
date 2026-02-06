package com.muzlik.mana;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles visual mana feedback via action bar or boss bar.
 */
public class ManaDisplay {
    private final JavaPlugin plugin;
    private final Map<UUID, BossBar> playerBossBars;
    private DisplayMode displayMode;

    public enum DisplayMode {
        ACTION_BAR,
        BOSS_BAR
    }

    public ManaDisplay(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerBossBars = new ConcurrentHashMap<>();
        this.displayMode = DisplayMode.ACTION_BAR; // Default
    }

    /**
     * Update mana display for player
     * DISABLED: Mana is now shown in FragmentActionBarHUD instead of boss bar
     */
    public void updateDisplay(Player player, double currentMana, double maxMana) {
        // DISABLED - Mana is now displayed in the action bar HUD
        // Do NOT create or update boss bars
    }



    /**
     * Create visual mana bar
     */
    private String createManaBar(double currentMana, double maxMana) {
        int barLength = 10;
        int filledBars = (int) ((currentMana / maxMana) * barLength);
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        
        return bar.toString();
    }

    /**
     * Set display mode
     */
    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode;
    }

    /**
     * Remove display for player
     */
    public void removeDisplay(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }
}
