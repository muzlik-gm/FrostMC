package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Interface for handling block controller impacts.
 * 
 * Defines how block controllers should handle collisions with entities and blocks.
 * Implementations can customize damage calculation, knockback, and VFX.
 * 
 * Per SYSTEM.md specification:
 * - Damage formula: baseDamage * (1 + 0.15 * rank)
 * - Knockback vector: normalize(targetLoc - impactLoc) * strength
 * - Spawn impact VFX on collision
 * - Integrate with DamageAttributionSystem
 * 
 * Requirements: 2.4, 2.5
 */
public interface ImpactHandler {
    
    /**
     * Handle impact with an entity
     * 
     * @param controller The block controller that impacted
     * @param target The entity that was hit
     * @param impactLocation The location of impact
     */
    void onEntityImpact(BlockController controller, Entity target, Location impactLocation);
    
    /**
     * Handle impact with a block
     * 
     * @param controller The block controller that impacted
     * @param impactLocation The location of impact
     */
    void onBlockImpact(BlockController controller, Location impactLocation);
    
    /**
     * Calculate damage for an impact
     * Uses formula: baseDamage * (1 + 0.15 * rank)
     * 
     * @param baseDamage Base damage value
     * @param rank Fragment rank
     * @return Calculated damage
     */
    default double calculateDamage(double baseDamage, int rank) {
        return baseDamage * (1.0 + 0.15 * rank);
    }
    
    /**
     * Calculate knockback vector
     * Uses formula: normalize(targetLoc - impactLoc) * strength
     * 
     * @param impactLocation Location of impact
     * @param targetLocation Location of target
     * @param strength Knockback strength multiplier
     * @return Knockback vector
     */
    default org.bukkit.util.Vector calculateKnockback(Location impactLocation, 
                                                       Location targetLocation, 
                                                       double strength) {
        org.bukkit.util.Vector knockback = targetLocation.toVector()
            .subtract(impactLocation.toVector());
        
        // Check if vector is zero or too small (prevents NaN from normalize())
        double lengthSquared = knockback.lengthSquared();
        if (lengthSquared < 0.0001) {
            // Use a default upward knockback if locations are identical
            knockback = new org.bukkit.util.Vector(0, 1, 0).multiply(strength);
        } else {
            knockback = knockback.normalize().multiply(strength);
            // Add upward component for better feel
            knockback.setY(Math.max(knockback.getY(), 0.3));
        }
        
        return knockback;
    }
    
    /**
     * Get the owner player of the controller
     * 
     * @param controller The block controller
     * @return The owner player, or null if offline
     */
    default Player getOwner(BlockController controller) {
        return org.bukkit.Bukkit.getPlayer(controller.getOwnerUUID());
    }
}
