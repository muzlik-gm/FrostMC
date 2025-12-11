package com.muzlik.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Utility class for safe teleportation
 * Prevents teleporting into blocks or too high
 */
public class SafeTeleport {
    
    /**
     * Teleport player to a safe location
     * Adjusts Y coordinate to prevent suffocation or fall damage
     */
    public static boolean teleportSafely(Player player, Location target) {
        Location safeLoc = findSafeLocation(target);
        
        if (safeLoc != null) {
            player.teleport(safeLoc);
            return true;
        }
        
        return false;
    }
    
    /**
     * Find a safe location near the target
     * Checks for solid ground and air space
     */
    public static Location findSafeLocation(Location target) {
        Location loc = target.clone();
        
        // Check current location first
        if (isSafeLocation(loc)) {
            return loc;
        }
        
        // Try adjusting Y coordinate (up to 5 blocks up or down)
        for (int yOffset = 0; yOffset <= 5; yOffset++) {
            // Try going down first
            Location downLoc = loc.clone().subtract(0, yOffset, 0);
            if (isSafeLocation(downLoc)) {
                return downLoc;
            }
            
            // Then try going up
            if (yOffset > 0) {
                Location upLoc = loc.clone().add(0, yOffset, 0);
                if (isSafeLocation(upLoc)) {
                    return upLoc;
                }
            }
        }
        
        // If no safe location found, return original with slight adjustment
        return loc.clone().add(0, 1, 0);
    }
    
    /**
     * Check if a location is safe for teleportation
     * Must have solid ground below and air space for player
     */
    public static boolean isSafeLocation(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block ground = loc.clone().subtract(0, 1, 0).getBlock();
        
        // Check if feet and head are passable (air or non-solid)
        boolean feetSafe = feet.isPassable() || feet.getType() == Material.AIR;
        boolean headSafe = head.isPassable() || head.getType() == Material.AIR;
        
        // Check if ground is solid (not air, not lava, not void)
        boolean groundSolid = ground.getType().isSolid() && 
                             ground.getType() != Material.LAVA &&
                             ground.getType() != Material.MAGMA_BLOCK;
        
        // Check not too high (prevent fall damage)
        boolean notTooHigh = loc.getY() < loc.getWorld().getMaxHeight() - 10;
        
        // Check not in void
        boolean notInVoid = loc.getY() > loc.getWorld().getMinHeight() + 5;
        
        return feetSafe && headSafe && groundSolid && notTooHigh && notInVoid;
    }
    
    /**
     * Get safe dash location for velocity-based movement
     * Ensures player won't hit ceiling or go too high
     */
    public static Location getSafeDashTarget(Player player, org.bukkit.util.Vector direction, double distance) {
        Location start = player.getLocation();
        Location target = start.clone().add(direction.multiply(distance));
        
        // Keep reasonable Y level (don't go too high)
        double maxYChange = 5.0;
        if (target.getY() - start.getY() > maxYChange) {
            target.setY(start.getY() + maxYChange);
        }
        
        // Ensure not going into ground
        if (target.getY() < start.getY() - 2) {
            target.setY(start.getY());
        }
        
        return findSafeLocation(target);
    }
    
    /**
     * Get safe velocity for dash abilities
     * Limits vertical component to prevent going too high or into ground
     */
    public static org.bukkit.util.Vector getSafeVelocity(org.bukkit.util.Vector velocity, Player player) {
        org.bukkit.util.Vector safeVel = velocity.clone();
        
        // Limit upward velocity to prevent going too high
        if (safeVel.getY() > 1.5) {
            safeVel.setY(1.5);
        }
        
        // Limit downward velocity to prevent going into ground
        if (safeVel.getY() < -0.5) {
            safeVel.setY(-0.5);
        }
        
        // Check if player is near ground - reduce downward velocity
        Location below = player.getLocation().subtract(0, 1, 0);
        if (!below.getBlock().isPassable()) {
            if (safeVel.getY() < 0) {
                safeVel.setY(0.2); // Small upward boost to avoid clipping
            }
        }
        
        return safeVel;
    }
}
