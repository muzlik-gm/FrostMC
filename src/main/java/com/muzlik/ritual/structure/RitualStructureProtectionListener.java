package com.muzlik.ritual.structure;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Protects ritual structures from being modified during rituals
 * Works with any RitualStructure implementation
 */
public class RitualStructureProtectionListener implements Listener {
    private final Map<UUID, RitualStructure> activeStructures;
    private final JavaPlugin plugin;
    
    public RitualStructureProtectionListener(JavaPlugin plugin) {
        this.activeStructures = new HashMap<>();
        this.plugin = plugin;
    }
    
    /**
     * Register an active structure
     */
    public void registerStructure(UUID ritualId, RitualStructure structure) {
        activeStructures.put(ritualId, structure);
    }
    
    /**
     * Unregister a structure and schedule delayed despawn with countdown
     */
    public void unregisterStructure(UUID ritualId) {
        RitualStructure structure = activeStructures.remove(ritualId);
        if (structure != null) {
            scheduleDelayedDespawn(structure);
        }
    }
    
    /**
     * Schedule delayed despawn with countdown timer (20 seconds)
     */
    private void scheduleDelayedDespawn(RitualStructure structure) {
        Location center = structure.getCenter();
        
        // Create countdown hologram
        Location hologramLoc = center.clone().add(0, 3.5, 0);
        ArmorStand countdown = (ArmorStand) center.getWorld().spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
        countdown.setVisible(false);
        countdown.setGravity(false);
        countdown.setInvulnerable(true);
        countdown.setMarker(true);
        countdown.setSmall(true);
        countdown.setCustomNameVisible(true);
        countdown.setCustomName("§e⏳ Ritual structure cleanup in 20s");
        
        // Countdown task
        new BukkitRunnable() {
            int secondsLeft = 20;
            
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    // Remove countdown hologram
                    if (!countdown.isDead()) {
                        countdown.remove();
                    }
                    
                    // Despawn structure
                    structure.despawn();
                    cancel();
                    return;
                }
                
                // Update countdown display
                String color = secondsLeft <= 5 ? "§c" : secondsLeft <= 10 ? "§6" : "§e";
                countdown.setCustomName(color + "⏳ Cleanup in " + secondsLeft + "s");
                
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Check if location is protected
     */
    private boolean isProtected(org.bukkit.Location location) {
        for (RitualStructure structure : activeStructures.values()) {
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
        for (RitualStructure structure : activeStructures.values()) {
            structure.despawn();
        }
        activeStructures.clear();
    }
}
