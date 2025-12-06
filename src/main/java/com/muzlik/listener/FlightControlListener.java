package com.muzlik.listener;

import com.muzlik.fragment.ability.FlightManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Handles flight cancellation via sneak + left click hold
 */
public class FlightControlListener implements Listener {
    private final FlightManager flightManager;
    
    public FlightControlListener(FlightManager flightManager) {
        this.flightManager = flightManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is flying
        if (!flightManager.isFlying(player)) {
            return;
        }
        
        // Check for left click while sneaking
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                flightManager.attemptCancel(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        // If player stops sneaking, stop cancel attempt
        if (!event.isSneaking() && flightManager.isFlying(player)) {
            flightManager.stopCancelAttempt(player);
        }
    }
}
