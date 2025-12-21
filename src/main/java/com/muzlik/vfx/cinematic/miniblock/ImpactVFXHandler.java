package com.muzlik.vfx.cinematic.miniblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Handles impact VFX and effects for mini blocks.
 * Ground impacts show VFX only (no block placement).
 * Entity impacts apply damage and knockback (no block placement).
 * 
 * Requirements: 3.2, 3.3
 */
public class ImpactVFXHandler {
    
    /**
     * Handle ground impact - VFX only, no block placement
     * 
     * @param impactLocation Where the mini block hit
     * @param blockType Type of block for appropriate VFX
     */
    public void handleGroundImpact(Location impactLocation, Material blockType) {
        if (impactLocation == null || impactLocation.getWorld() == null) {
            return;
        }
        
        // Spawn impact particles
        Particle particleType = getParticleForBlock(blockType);
        
        // Impact burst
        impactLocation.getWorld().spawnParticle(
            particleType,
            impactLocation,
            20, // count
            0.3, 0.3, 0.3, // offset
            0.1 // speed
        );
        
        // Dust cloud
        impactLocation.getWorld().spawnParticle(
            Particle.CLOUD,
            impactLocation.clone().add(0, 0.2, 0),
            10,
            0.2, 0.1, 0.2,
            0.05
        );
        
        // Block crack particles
        if (blockType.isBlock()) {
            impactLocation.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                impactLocation,
                15,
                0.2, 0.2, 0.2,
                0.1,
                blockType.createBlockData()
            );
        }
    }
    
    /**
     * Handle entity impact - damage and knockback, no block placement
     * 
     * @param target The entity hit
     * @param impactLocation Where it hit
     * @param damage Damage to apply
     * @param blockType Type of block for appropriate VFX
     */
    public void handleEntityImpact(Entity target, Location impactLocation, double damage, Material blockType) {
        if (target == null || impactLocation == null) {
            return;
        }
        
        // Apply damage if living entity
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            living.damage(damage);
            
            // Apply knockback
            Vector knockback = target.getLocation().toVector()
                .subtract(impactLocation.toVector())
                .normalize()
                .multiply(0.5)
                .setY(0.3);
            
            target.setVelocity(knockback);
        }
        
        // Spawn impact VFX
        handleGroundImpact(impactLocation, blockType);
        
        // Additional hit particles
        if (impactLocation.getWorld() != null) {
            impactLocation.getWorld().spawnParticle(
                Particle.CRIT,
                target.getLocation().add(0, target.getHeight() / 2, 0),
                10,
                0.3, 0.3, 0.3,
                0.1
            );
        }
    }
    
    /**
     * Get appropriate particle type for a block material
     */
    private Particle getParticleForBlock(Material blockType) {
        if (blockType == null) {
            return Particle.BLOCK_CRACK;
        }
        
        String name = blockType.name();
        
        if (name.contains("STONE") || name.contains("COBBLESTONE")) {
            return Particle.BLOCK_CRACK;
        } else if (name.contains("DIRT") || name.contains("GRASS")) {
            return Particle.BLOCK_CRACK;
        } else if (name.contains("SAND")) {
            return Particle.FALLING_DUST;
        } else if (name.contains("WOOD") || name.contains("LOG")) {
            return Particle.BLOCK_CRACK;
        } else {
            return Particle.BLOCK_CRACK;
        }
    }
}
