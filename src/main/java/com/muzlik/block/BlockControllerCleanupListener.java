package com.muzlik.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Listener for cleaning up block controllers when players log out.
 * 
 * Ensures all block controllers owned by a player are removed when they disconnect,
 * preventing orphaned entities and memory leaks.
 * 
 * Per SYSTEM.md specification:
 * - Implement cleanup on player logout
 * - Add automatic removal on expiration
 * 
 * Requirements: 2.6
 */
public class BlockControllerCleanupListener implements Listener {
    private final JavaPlugin plugin;
    private final BlockManipulationEngine engine;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     * @param engine Block manipulation engine
     */
    public BlockControllerCleanupListener(JavaPlugin plugin, BlockManipulationEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }
    
    /**
     * Handle player quit event
     * Removes all block controllers owned by the player
     * 
     * @param event The player quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (engine == null) {
            return;
        }
        
        int removed = engine.removePlayerControllers(event.getPlayer().getUniqueId());
        
        if (removed > 0) {
            plugin.getLogger().log(Level.FINE, 
                "Cleaned up " + removed + " block controllers for player " + event.getPlayer().getName());
        }
    }
}
