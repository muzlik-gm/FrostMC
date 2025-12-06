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
     * Always uses boss bar to avoid conflict with power HUD
     */
    public void updateDisplay(Player player, double currentMana, double maxMana) {
        updateBossBar(player, currentMana, maxMana);
    }

    /**
     * Update boss bar display
     */
    private void updateBossBar(Player player, double currentMana, double maxMana) {
        BossBar bossBar = playerBossBars.computeIfAbsent(player.getUniqueId(), uuid -> {
            BossBar bar = BossBar.bossBar(
                Component.text("Mana"),
                1.0f,
                BossBar.Color.BLUE,
                BossBar.Overlay.PROGRESS
            );
            player.showBossBar(bar);
            return bar;
        });

        float progress = (float) Math.max(0.0, Math.min(1.0, currentMana / maxMana));
        bossBar.progress(progress);
        bossBar.name(Component.text("⚡ Mana: ", NamedTextColor.AQUA)
            .append(Component.text(String.format("%.0f/%.0f", currentMana, maxMana), NamedTextColor.BLUE)));
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
