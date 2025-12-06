package com.muzlik.listener;

import com.muzlik.vfx.ActiveEffectRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for cleaning up player effects when they log out.
 * Ensures all effects are cleaned up within 5 ticks of logout.
 * 
 * Requirements: 3.3, 41.2
 */
public class EffectCleanupListener implements Listener {
    private final JavaPlugin plugin;
    private final ActiveEffectRegistry effectRegistry;
    
    public EffectCleanupListener(JavaPlugin plugin, ActiveEffectRegistry effectRegistry) {
        this.plugin = plugin;
        this.effectRegistry = effectRegistry;
    }
    
    /**
     * Clean up all effects when player quits
     * Scheduled to run within 5 ticks to ensure cleanup
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Schedule cleanup to run within 5 ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                int cleaned = effectRegistry.cleanupPlayerEffects(player);
                if (cleaned > 0) {
                    plugin.getLogger().fine("Cleaned up " + cleaned + 
                            " effects for " + player.getName() + " on logout");
                }
            }
        }.runTaskLater(plugin, 5L);
    }
}
