package com.muzlik.vfx.cinematic.miniblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Creates fragment scatter effects when mini blocks are destroyed.
 * Particle type is appropriate to the block's Material type.
 * 
 * Requirements: 3.5
 */
public class FragmentScatterEffect {
    
    private final Random random;
    
    public FragmentScatterEffect() {
        this.random = new Random();
    }
    
    /**
     * Spawn fragment scatter effect when mini block is destroyed
     * 
     * @param location Destruction location
     * @param blockType Type of block for appropriate fragments
     */
    public void spawnFragmentScatter(Location location, Material blockType) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        // Determine particle type based on block material
        Particle particleType = getFragmentParticle(blockType);
        Object particleData = null;
        
        if (particleType == Particle.BLOCK_CRACK || particleType == Particle.FALLING_DUST) {
            if (blockType != null && blockType.isBlock()) {
                particleData = blockType.createBlockData();
            }
        }
        
        // Scatter fragments in multiple directions
        int fragmentCount = 8 + random.nextInt(5); // 8-12 fragments
        
        for (int i = 0; i < fragmentCount; i++) {
            // Random direction
            Vector direction = new Vector(
                random.nextDouble() * 2 - 1,
                random.nextDouble() * 0.5 + 0.3, // Mostly upward
                random.nextDouble() * 2 - 1
            ).normalize().multiply(0.3 + random.nextDouble() * 0.3);
            
            Location fragmentLoc = location.clone().add(direction.multiply(0.2));
            
            // Spawn fragment particles
            if (particleData != null) {
                location.getWorld().spawnParticle(
                    particleType,
                    fragmentLoc,
                    3,
                    0.1, 0.1, 0.1,
                    0.05,
                    particleData
                );
            } else {
                location.getWorld().spawnParticle(
                    particleType,
                    fragmentLoc,
                    3,
                    0.1, 0.1, 0.1,
                    0.05
                );
            }
        }
        
        // Central burst
        if (particleData != null) {
            location.getWorld().spawnParticle(
                particleType,
                location,
                20,
                0.3, 0.3, 0.3,
                0.1,
                particleData
            );
        } else {
            location.getWorld().spawnParticle(
                particleType,
                location,
                20,
                0.3, 0.3, 0.3,
                0.1
            );
        }
        
        // Smoke puff
        location.getWorld().spawnParticle(
            Particle.CLOUD,
            location.clone().add(0, 0.3, 0),
            5,
            0.2, 0.2, 0.2,
            0.02
        );
    }
    
    /**
     * Get appropriate fragment particle for a block material
     */
    private Particle getFragmentParticle(Material blockType) {
        if (blockType == null) {
            return Particle.BLOCK_CRACK;
        }
        
        String name = blockType.name();
        
        // Stone-like blocks
        if (name.contains("STONE") || name.contains("COBBLESTONE") || 
            name.contains("ANDESITE") || name.contains("DIORITE") || 
            name.contains("GRANITE")) {
            return Particle.BLOCK_CRACK;
        }
        
        // Dirt/grass blocks
        if (name.contains("DIRT") || name.contains("GRASS") || name.contains("PODZOL")) {
            return Particle.BLOCK_CRACK;
        }
        
        // Sand/gravel
        if (name.contains("SAND") || name.contains("GRAVEL")) {
            return Particle.FALLING_DUST;
        }
        
        // Wood blocks
        if (name.contains("WOOD") || name.contains("LOG") || name.contains("PLANKS")) {
            return Particle.BLOCK_CRACK;
        }
        
        // Ice blocks
        if (name.contains("ICE")) {
            return Particle.SNOWFLAKE;
        }
        
        // Default
        return Particle.BLOCK_CRACK;
    }
}
