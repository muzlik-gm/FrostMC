package com.muzlik.vfx.environment;

import com.muzlik.vfx.ActiveEffectRegistry;
import com.muzlik.vfx.EffectEntry;
import com.muzlik.vfx.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages temporary block changes and environment manipulation.
 * Handles auto-revert, packet visuals, and armor-stand hitboxes.
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
public class EnvironmentManager {
    private final JavaPlugin plugin;
    private final ActiveEffectRegistry effectRegistry;
    
    // Temporary blocks map: BlockPosition -> BlockPosition
    private final Map<BlockPosition, BlockPosition> temporaryBlocks;
    
    // Active manipulations
    private final Set<UUID> activeManipulations;
    
    // Revert task
    private BukkitTask revertTask;
    
    public EnvironmentManager(JavaPlugin plugin, ActiveEffectRegistry effectRegistry) {
        this.plugin = plugin;
        this.effectRegistry = effectRegistry;
        this.temporaryBlocks = new ConcurrentHashMap<>();
        this.activeManipulations = ConcurrentHashMap.newKeySet();
    }
    
    /**
     * Start the revert task
     * Checks for blocks that need to be reverted
     */
    public void startRevertTask() {
        if (revertTask != null) {
            revertTask.cancel();
        }
        
        // Run every tick to ensure blocks revert within 1 tick of expiration
        revertTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            revertTemporaryBlocks();
        }, 1L, 1L);
        
        plugin.getLogger().info("EnvironmentManager revert task started");
    }
    
    /**
     * Stop the revert task
     */
    public void stopRevertTask() {
        if (revertTask != null) {
            revertTask.cancel();
            revertTask = null;
        }
    }
    
    /**
     * Place a temporary block that will auto-revert
     * 
     * @param location Location to place the block
     * @param material Material to place
     * @param durationTicks Duration in ticks before revert
     * @param owner Player who owns this block change
     * @return UUID of the manipulation
     */
    public UUID placeTemporaryBlock(Location location, Material material, int durationTicks, Player owner) {
        if (location == null || location.getWorld() == null || material == null) {
            return null;
        }
        
        Block block = location.getBlock();
        long revertTime = System.currentTimeMillis() + (durationTicks * 50L);
        
        // Store original state
        BlockPosition blockPos = new BlockPosition(block, revertTime);
        temporaryBlocks.put(blockPos, blockPos);
        
        // Change the block
        block.setType(material);
        
        // Register in effect registry
        UUID manipId = UUID.randomUUID();
        activeManipulations.add(manipId);
        
        if (owner != null) {
            EffectEntry entry = new EffectEntry(
                manipId,
                owner.getUniqueId(),
                EffectType.BLOCK,
                location,
                System.currentTimeMillis(),
                revertTime,
                blockPos
            );
            effectRegistry.registerEffect(entry);
        }
        
        plugin.getLogger().log(Level.FINE, 
                "Placed temporary block: " + material + " at " + location + 
                " (revert in " + durationTicks + " ticks)");
        
        return manipId;
    }
    
    /**
     * Create a packet-only visual (client-side block)
     * Note: This is a simplified version - full packet implementation would require ProtocolLib
     * 
     * @param location Location for the visual
     * @param material Material to show
     * @param viewers Players who should see this visual
     */
    public void createPacketVisual(Location location, Material material, Player... viewers) {
        // This would require ProtocolLib for true packet-only visuals
        // For now, we'll use a placeholder implementation
        plugin.getLogger().log(Level.FINE, 
                "Packet visual requested: " + material + " at " + location + 
                " (requires ProtocolLib for full implementation)");
    }
    
    /**
     * Spawn an armor stand hitbox for collision detection
     * 
     * @param location Location to spawn the armor stand
     * @param box Bounding box for the hitbox
     * @param durationTicks Duration in ticks
     * @param owner Player who owns this hitbox
     * @return UUID of the armor stand
     */
    public UUID spawnArmorStandHitbox(Location location, BoundingBox box, int durationTicks, Player owner) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        
        // Spawn invisible armor stand
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        armorStand.setCollidable(true);
        
        UUID hitboxId = armorStand.getUniqueId();
        long removalTime = System.currentTimeMillis() + (durationTicks * 50L);
        
        // Register in effect registry
        if (owner != null) {
            EffectEntry entry = new EffectEntry(
                hitboxId,
                owner.getUniqueId(),
                EffectType.ARMOR_STAND,
                location,
                System.currentTimeMillis(),
                removalTime,
                armorStand
            );
            effectRegistry.registerEffect(entry);
        }
        
        // Schedule removal
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (armorStand.isValid()) {
                armorStand.remove();
            }
        }, durationTicks);
        
        plugin.getLogger().log(Level.FINE, 
                "Spawned armor stand hitbox at " + location + 
                " (duration: " + durationTicks + " ticks)");
        
        return hitboxId;
    }
    
    /**
     * Revert temporary blocks that have expired
     * Called every tick by the revert task
     */
    public void revertTemporaryBlocks() {
        if (temporaryBlocks.isEmpty()) {
            return;
        }
        
        List<BlockPosition> toRevert = new ArrayList<>();
        
        for (BlockPosition blockPos : temporaryBlocks.keySet()) {
            if (blockPos.shouldRevert()) {
                toRevert.add(blockPos);
            }
        }
        
        // Revert blocks
        for (BlockPosition blockPos : toRevert) {
            try {
                blockPos.revert();
                temporaryBlocks.remove(blockPos);
                
                plugin.getLogger().log(Level.FINE, 
                        "Reverted temporary block at " + blockPos.getLocation());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                        "Error reverting block at " + blockPos.getLocation(), e);
                temporaryBlocks.remove(blockPos);
            }
        }
    }
    
    /**
     * Revert all temporary blocks immediately
     * Called on server shutdown
     */
    public void revertAllBlocks() {
        plugin.getLogger().info("Reverting all temporary blocks...");
        
        for (BlockPosition blockPos : temporaryBlocks.keySet()) {
            try {
                blockPos.revert();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                        "Error reverting block at " + blockPos.getLocation(), e);
            }
        }
        
        temporaryBlocks.clear();
        plugin.getLogger().info("All temporary blocks reverted");
    }
    
    /**
     * Get the number of active temporary blocks
     */
    public int getTemporaryBlockCount() {
        return temporaryBlocks.size();
    }
    
    /**
     * Shutdown the environment manager
     */
    public void shutdown() {
        stopRevertTask();
        revertAllBlocks();
        activeManipulations.clear();
        plugin.getLogger().info("EnvironmentManager shutdown complete");
    }
}
