package com.muzlik.vfx.environment;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * Represents a block position with original state for reversion
 * 
 * Requirements: 5.1, 5.3, 5.4
 */
public class BlockPosition {
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final BlockData originalState;
    private final long revertTime;
    
    public BlockPosition(Block block, long revertTime) {
        this.world = block.getWorld();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.originalState = block.getBlockData().clone();
        this.revertTime = revertTime;
    }
    
    /**
     * Revert this block to its original state
     */
    public void revert() {
        if (world == null) {
            return;
        }
        
        Block block = world.getBlockAt(x, y, z);
        block.setBlockData(originalState);
    }
    
    /**
     * Check if this block should be reverted
     */
    public boolean shouldRevert() {
        return System.currentTimeMillis() >= revertTime;
    }
    
    /**
     * Get the location of this block
     */
    public Location getLocation() {
        return new Location(world, x, y, z);
    }
    
    // Getters
    
    public World getWorld() {
        return world;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public BlockData getOriginalState() {
        return originalState;
    }
    
    public long getRevertTime() {
        return revertTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BlockPosition that = (BlockPosition) o;
        
        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        return world != null ? world.equals(that.world) : that.world == null;
    }
    
    @Override
    public int hashCode() {
        int result = world != null ? world.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
