package com.muzlik.ritual.structure;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Protects ritual structures from being modified during rituals
 * Simple version - just tracks active structures
 */
public class RitualStructureProtectionListener implements Listener {
    private final Map<UUID, SimpleRitualStructure> activeStructures;
    
    public RitualStructureProtectionListener() {
        this.activeStructures = new HashMap<>();
    }
    
    /**
     * Register an active structure
     */
    public void registerStructure(UUID ritualId, SimpleRitualStructure structure) {
        activeStructures.put(ritualId, structure);
    }
    
    /**
     * Unregister a structure and despawn it
     */
    public void unregisterStructure(UUID ritualId) {
        SimpleRitualStructure structure = activeStructures.remove(ritualId);
        if (structure != null) {
            structure.despawn();
        }
    }
    
    /**
     * Check if location is protected
     */
    private boolean isProtected(org.bukkit.Location location) {
        for (SimpleRitualStructure structure : activeStructures.values()) {
            if (structure.isProtected(location)) {
                return true;
            }
        }
        return false;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c✗ You cannot break blocks inside an active ritual!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c✗ You cannot place blocks inside an active ritual!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isProtected(block.getLocation()));
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isProtected(block.getLocation()));
    }
    
    /**
     * Cleanup all structures
     */
    public void cleanup() {
        for (SimpleRitualStructure structure : activeStructures.values()) {
            structure.despawn();
        }
        activeStructures.clear();
    }
}
