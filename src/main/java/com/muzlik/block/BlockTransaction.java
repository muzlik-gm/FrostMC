package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Transaction system for safe real block placement.
 * 
 * Tracks block changes and ensures guaranteed revert on expiration or failure.
 * Provides locks to prevent concurrent modifications and prevents tile entity side effects.
 * 
 * Per SYSTEM.md specification:
 * - Track all block changes in a transaction
 * - Guaranteed revert on expiration or failure
 * - Locks to prevent concurrent modifications
 * - Prevent tile entity side effects
 * 
 * Requirements: 12.3, 12.7
 */
public class BlockTransaction {
    private final JavaPlugin plugin;
    private final UUID transactionId;
    private final UUID ownerUUID;
    private final String abilityId;
    
    // Track original block states
    private final Map<Location, BlockData> originalBlocks;
    
    // Track modified blocks
    private final Map<Location, BlockData> modifiedBlocks;
    
    // Transaction lock
    private final ReentrantLock lock;
    
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
    public BlockTransaction(JavaPlugin plugin, UUID ownerUUID, String abilityId, int durationTicks) {
        this.plugin = plugin;
        this.transactionId = UUID.randomUUID();
        this.ownerUUID = ownerUUID;
        this.abilityId = abilityId;
        this.originalBlocks = new HashMap<>();
        this.modifiedBlocks = new HashMap<>();
        this.lock = new ReentrantLock();
        this.committed = false;
        this.reverted = false;
        this.creationTime = System.currentTimeMillis();
        this.expirationTime = creationTime + (durationTicks * 50L);
    }
    
    /**
     * Set a block with transaction tracking
     * 
     * @param location Location of the block
     * @param blockData Block data to set
     * @return true if set successfully
     */
    public boolean setBlock(Location location, BlockData blockData) {
        if (location == null || location.getWorld() == null || blockData == null) {
            return false;
        }
        
        lock.lock();
        try {
            if (reverted) {
                plugin.getLogger().warning("Cannot modify reverted transaction: " + transactionId);
                return false;
            }
            
            Block block = location.getBlock();
            
            // Safety check: Don't modify tile entities
            if (block.getState() instanceof org.bukkit.block.TileState) {
                plugin.getLogger().warning("Transaction " + transactionId + 
                    ": Attempted to modify tile entity at " + location + " - blocked for safety");
                return false;
            }
            
            // Store original block data if not already stored
            Location key = location.clone();
            if (!originalBlocks.containsKey(key)) {
                originalBlocks.put(key, block.getBlockData().clone());
            }
            
            // Set the block
            block.setBlockData(blockData, false); // false = no physics update
            
            // Track modified block
            modifiedBlocks.put(key, blockData.clone());
            
            return true;
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Set a block to a specific material
     * 
     * @param location Location of the block
     * @param material Material to set
     * @return true if set successfully
     */
    public boolean setBlock(Location location, Material material) {
        if (material == null) {
            return false;
        }
        
        return setBlock(location, material.createBlockData());
    }
    
    /**
     * Commit the transaction (marks it as successful)
     * Does not prevent revert - revert is still guaranteed on expiration
     */
    public void commit() {
        lock.lock();
        try {
            if (!reverted) {
                committed = true;
                plugin.getLogger().log(Level.FINE, 
                    "Transaction " + transactionId + " committed");
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Revert all changes made in this transaction
     * Restores all blocks to their original state
     * 
     * @return true if revert was successful
     */
    public boolean revert() {
        lock.lock();
        try {
            if (reverted) {
                return true; // Already reverted
            }
            
            reverted = true;
            boolean success = true;
            
            // Restore all original blocks
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
            modifiedBlocks.clear();
            
            plugin.getLogger().log(Level.FINE, 
                "Transaction " + transactionId + " reverted" + (success ? "" : " with errors"));
            
            return success;
            
        } finally {
            lock.unlock();
        }
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
     * Get the number of modified blocks
     * 
     * @return Number of modified blocks
     */
    public int getModifiedBlockCount() {
        lock.lock();
        try {
            return modifiedBlocks.size();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get the number of original blocks stored
     * 
     * @return Number of original blocks
     */
    public int getOriginalBlockCount() {
        lock.lock();
        try {
            return originalBlocks.size();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Try to acquire the transaction lock
     * 
     * @return true if lock acquired
     */
    public boolean tryLock() {
        return lock.tryLock();
    }
    
    /**
     * Release the transaction lock
     */
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
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
