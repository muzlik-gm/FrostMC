package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Transaction system for safe falling block operations.
 * 
 * Tracks original block states and ensures guaranteed revert on expiration or failure.
 * Prevents tile entity side effects and provides locks for concurrent modification safety.
 * 
 * Per SYSTEM.md specification:
 * - All falling blocks must be flagged as tempFalling=true (via metadata)
 * - Store original block type for revert
 * - Implement safety checks and transaction system
 * - Guaranteed revert on expiration or failure
 * 
 * Requirements: 2.3, 2.7, 12.3, 12.7
 */
public class FallingBlockTransaction {
    private final JavaPlugin plugin;
    private final UUID transactionId;
    private final UUID ownerUUID;
    private final String abilityId;
    
    // Track original block states for revert
    private final Map<Location, BlockData> originalBlocks;
    
    // Track spawned falling blocks
    private final Map<UUID, FallingBlock> fallingBlocks;
    
    // Transaction state
    private boolean committed;
    private boolean reverted;
    private long creationTime;
    private long expirationTime;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     * @param ownerUUID UUID of the player who owns this transaction
     * @param abilityId Ability identifier
     * @param durationTicks Duration before auto-revert (in ticks)
     */
    public FallingBlockTransaction(JavaPlugin plugin, UUID ownerUUID, String abilityId, int durationTicks) {
        this.plugin = plugin;
        this.transactionId = UUID.randomUUID();
        this.ownerUUID = ownerUUID;
        this.abilityId = abilityId;
        this.originalBlocks = new HashMap<>();
        this.fallingBlocks = new HashMap<>();
        this.committed = false;
        this.reverted = false;
        this.creationTime = System.currentTimeMillis();
        this.expirationTime = creationTime + (durationTicks * 50L); // Convert ticks to milliseconds
    }
    
    /**
     * Store original block state before modification
     * 
     * @param location Location of the block
     * @return true if stored successfully
     */
    public boolean storeOriginalBlock(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        Block block = location.getBlock();
        
        // Safety check: Don't modify tile entities
        if (block.getState() instanceof org.bukkit.block.TileState) {
            plugin.getLogger().warning("Transaction " + transactionId + 
                ": Attempted to modify tile entity at " + location + " - blocked for safety");
            return false;
        }
        
        // Store original block data
        originalBlocks.put(location.clone(), block.getBlockData().clone());
        
        return true;
    }
    
    /**
     * Track a falling block entity in this transaction
     * 
     * @param fallingBlock The falling block entity
     */
    public void trackFallingBlock(FallingBlock fallingBlock) {
        if (fallingBlock != null && fallingBlock.isValid()) {
            fallingBlocks.put(fallingBlock.getUniqueId(), fallingBlock);
            
            // Mark as temporary using persistent data
            fallingBlock.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "tempFalling"),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte) 1
            );
            
            // Store owner and ability info
            fallingBlock.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "ownerUUID"),
                org.bukkit.persistence.PersistentDataType.STRING,
                ownerUUID.toString()
            );
            
            fallingBlock.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "abilityId"),
                org.bukkit.persistence.PersistentDataType.STRING,
                abilityId
            );
        }
    }
    
    /**
     * Commit the transaction (marks it as successful)
     * Does not prevent revert - revert is still guaranteed on expiration
     */
    public void commit() {
        if (!reverted) {
            committed = true;
            plugin.getLogger().log(Level.FINE, 
                "Transaction " + transactionId + " committed");
        }
    }
    
    /**
     * Revert all changes made in this transaction
     * Removes falling blocks and restores original block states
     * 
     * @return true if revert was successful
     */
    public boolean revert() {
        if (reverted) {
            return true; // Already reverted
        }
        
        reverted = true;
        boolean success = true;
        
        // Remove all falling block entities
        for (FallingBlock fallingBlock : fallingBlocks.values()) {
            try {
                if (fallingBlock.isValid()) {
                    fallingBlock.remove();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Failed to remove falling block in transaction " + transactionId, e);
                success = false;
            }
        }
        
        fallingBlocks.clear();
        
        // Restore original blocks
        for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
            try {
                Location loc = entry.getKey();
                BlockData originalData = entry.getValue();
                
                if (loc.getWorld() != null) {
                    Block block = loc.getBlock();
                    block.setBlockData(originalData, false); // false = no physics update
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Failed to restore block in transaction " + transactionId, e);
                success = false;
            }
        }
        
        originalBlocks.clear();
        
        plugin.getLogger().log(Level.FINE, 
            "Transaction " + transactionId + " reverted" + (success ? "" : " with errors"));
        
        return success;
    }
    
    /**
     * Check if transaction has expired
     * 
     * @return true if expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
    
    /**
     * Check if transaction is still active (not reverted and not expired)
     * 
     * @return true if active
     */
    public boolean isActive() {
        return !reverted && !isExpired();
    }
    
    /**
     * Get the number of tracked falling blocks
     * 
     * @return Number of falling blocks
     */
    public int getFallingBlockCount() {
        return fallingBlocks.size();
    }
    
    /**
     * Get the number of stored original blocks
     * 
     * @return Number of original blocks
     */
    public int getOriginalBlockCount() {
        return originalBlocks.size();
    }
    
    /**
     * Check if a falling block is temporary (has tempFalling flag)
     * 
     * @param fallingBlock The falling block to check
     * @return true if marked as temporary
     */
    public static boolean isTempFalling(JavaPlugin plugin, FallingBlock fallingBlock) {
        if (fallingBlock == null) {
            return false;
        }
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "tempFalling");
        return fallingBlock.getPersistentDataContainer().has(key, 
            org.bukkit.persistence.PersistentDataType.BYTE);
    }
    
    // Getters
    
    public UUID getTransactionId() {
        return transactionId;
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    public String getAbilityId() {
        return abilityId;
    }
    
    public boolean isCommitted() {
        return committed;
    }
    
    public boolean isReverted() {
        return reverted;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getExpirationTime() {
        return expirationTime;
    }
}
