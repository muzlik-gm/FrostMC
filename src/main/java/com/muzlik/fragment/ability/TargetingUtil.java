package com.muzlik.fragment.ability;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Utility class for targeting entities and locations
 * Prioritizes entities over blocks for better ability targeting
 */
public class TargetingUtil {
    
    /**
     * Get the target entity the player is looking at
     * Uses raycast with entity priority
     * 
     * @param player The player
     * @param range Maximum range to check
     * @return The targeted entity, or null if none found
     */
    public static LivingEntity getTargetEntity(Player player, double range) {
        // First try: Use Bukkit's rayTraceEntities (most accurate)
        RayTraceResult rayTrace = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getLocation().getDirection(),
            range,
            0.5, // entity bounding box expansion
            entity -> entity instanceof LivingEntity && entity != player
        );
        
        if (rayTrace != null && rayTrace.getHitEntity() instanceof LivingEntity) {
            return (LivingEntity) rayTrace.getHitEntity();
        }
        
        // Fallback: Check nearby entities in cone
        LivingEntity closest = null;
        double closestDist = range;
        
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                // Check if entity is in player's line of sight
                if (player.hasLineOfSight(entity)) {
                    // Check if entity is in front of player (cone check)
                    Vector toEntity = entity.getLocation().toVector()
                        .subtract(player.getEyeLocation().toVector())
                        .normalize();
                    Vector playerDirection = player.getLocation().getDirection();
                    
                    double dot = toEntity.dot(playerDirection);
                    if (dot > 0.7) { // Within ~45 degree cone
                        double dist = player.getLocation().distance(entity.getLocation());
                        if (dist < closestDist) {
                            closest = (LivingEntity) entity;
                            closestDist = dist;
                        }
                    }
                }
            }
        }
        
        return closest;
    }
    
    /**
     * Get the target location the player is looking at
     * Prioritizes entities, then falls back to blocks
     * 
     * @param player The player
     * @param range Maximum range to check
     * @return The target location (entity location or block location)
     */
    public static Location getTargetLocation(Player player, double range) {
        // First try to target an entity directly in crosshair
        LivingEntity targetEntity = getTargetEntity(player, range);
        if (targetEntity != null) {
            return targetEntity.getLocation();
        }
        
        // Fallback to block targeting - NO CORRECTION, just use the block
        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            player.getLocation().getDirection(),
            range
        );
        
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            return rayTrace.getHitBlock().getLocation().add(0.5, 0, 0.5);
        }
        
        // Last resort: location in front of player
        return player.getEyeLocation().add(player.getLocation().getDirection().multiply(range));
    }
    
    /**
     * Get the target location for ground-based abilities
     * Prioritizes entity feet location, then ground blocks
     * SIMPLE: No correction, just what you're looking at
     * 
     * @param player The player
     * @param range Maximum range to check
     * @return The target location at ground level
     */
    public static Location getGroundTargetLocation(Player player, double range) {
        // First try to target an entity directly in crosshair
        LivingEntity targetEntity = getTargetEntity(player, range);
        if (targetEntity != null) {
            // Return entity's feet location
            return targetEntity.getLocation();
        }
        
        // Fallback to block targeting - NO CORRECTION
        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            player.getLocation().getDirection(),
            range
        );
        
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Location blockLoc = rayTrace.getHitBlock().getLocation();
            // Center the location and return - NO ENTITY CORRECTION
            return blockLoc.add(0.5, 0, 0.5);
        }
        
        // Last resort: location in front of player at ground level
        Location fallback = player.getEyeLocation().add(player.getLocation().getDirection().multiply(range));
        // Try to find ground below
        RayTraceResult groundTrace = player.getWorld().rayTraceBlocks(
            fallback,
            new Vector(0, -1, 0),
            50
        );
        if (groundTrace != null && groundTrace.getHitBlock() != null) {
            return groundTrace.getHitBlock().getLocation().add(0.5, 0, 0.5);
        }
        
        return fallback;
    }
}
