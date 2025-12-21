package com.muzlik.listener;

import com.muzlik.fragment.ability.FlightManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles flight-related events
 */
public class FlightControlListener implements Listener {
    private final FlightManager flightManager;
    
    public FlightControlListener(FlightManager flightManager) {
        this.flightManager = flightManager;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // End flight when player quits (no cooldown)
        if (flightManager.isFlying(player)) {
            flightManager.endFlight(player, false);
        }
    }
}
