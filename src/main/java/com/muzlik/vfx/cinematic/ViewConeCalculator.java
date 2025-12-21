package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Utility for calculating player's view cone.
 * Used to determine which particles are in the player's immediate field of view.
 * 
 * Requirements: 2.1, 2.2
 */
public class ViewConeCalculator {
    
    private static final double VIEW_CONE_ANGLE = 30.0; // degrees
    private static final double VIEW_CONE_RADIUS = 2.0; // blocks
    
    /**
     * Check if a location is within the player's view cone
     * 
     * @param player The player
     * @param location Location to check
     * @return true if location is in view cone
     */
    public boolean isInViewCone(Player player, Location location) {
        if (player == null || location == null) {
            return false;
        }
        
        Location eyeLocation = player.getEyeLocation();
        
        // Check distance first (quick rejection)
        double distance = eyeLocation.distance(location);
        if (distance > VIEW_CONE_RADIUS) {
            return false;
        }
        
        // Check if within vertical range (2 blocks of eye level)
        double verticalDistance = Math.abs(location.getY() - eyeLocation.getY());
        if (verticalDistance > 2.0) {
            return false;
        }
        
        // Check angle from look direction
        Vector toLocation = location.toVector().subtract(eyeLocation.toVector()).normalize();
        Vector lookDirection = eyeLocation.getDirection().normalize();
        
        double angle = Math.toDegrees(Math.acos(toLocation.dot(lookDirection)));
        
        return angle <= VIEW_CONE_ANGLE;
    }
    
    /**
     * Check if a location is within the visibility corridor
     * (directly in front of player's look direction)
     * 
     * @param player The player
     * @param location Location to check
     * @return true if location is in visibility corridor
     */
    public boolean isInVisibilityCorridor(Player player, Location location) {
        if (player == null || location == null) {
            return false;
        }
        
        Location eyeLocation = player.getEyeLocation();
        Vector toLocation = location.toVector().subtract(eyeLocation.toVector());
        Vector lookDirection = eyeLocation.getDirection();
        
        // Project toLocation onto look direction
        double projection = toLocation.dot(lookDirection);
        
        // Check if location is in front of player
        if (projection <= 0) {
            return false;
        }
        
        // Check if location is within corridor width (1 block radius)
        Vector projectedPoint = lookDirection.clone().multiply(projection);
        double lateralDistance = toLocation.clone().subtract(projectedPoint).length();
        
        return lateralDistance <= 1.0 && projection <= 5.0; // 5 blocks forward
    }
}
