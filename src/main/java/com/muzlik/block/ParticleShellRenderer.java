package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

/**
 * Particle Shell Renderer for Block Controllers.
 * 
 * Renders BLOCK_CRACK particles at controller locations to simulate moving blocks.
 * Optimizes particle count based on distance from players.
 * Supports different block materials (stone, dirt, etc.).
 * 
 * Per SYSTEM.md specification:
 * - Prefer particle shells over real falling blocks
 * - Optimize based on player distance
 * - Support various block materials
 * 
 * Requirements: 2.3
 */
public class ParticleShellRenderer {
    private final JavaPlugin plugin;
    
    // Distance thresholds for particle optimization
    private static final double DISTANCE_CLOSE = 10.0;      // Full particles
    private static final double DISTANCE_MEDIUM = 25.0;     // Reduced particles
    private static final double DISTANCE_FAR = 40.0;        // Minimal particles
    
    // Particle counts by distance tier
    private static final int PARTICLES_CLOSE = 12;
    private static final int PARTICLES_MEDIUM = 6;
    private static final int PARTICLES_FAR = 3;
    private static final int PARTICLES_VERY_FAR = 1;
    
    // Particle spread
    private static final double SPREAD_CLOSE = 0.25;
    private static final double SPREAD_MEDIUM = 0.20;
    private static final double SPREAD_FAR = 0.15;
    
    /**
     * Constructor
     * 
     * @param plugin The plugin instance
     */
    public ParticleShellRenderer(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Render particle shell at the given location
     * 
     * @param location Location to render particles
     * @param blockType Material type for BLOCK_CRACK particles
     */
    public void renderShell(Location location, Material blockType) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        if (blockType == null || !blockType.isBlock()) {
            blockType = Material.STONE; // Fallback to stone
        }
        
        // Get nearby players for distance optimization
        Collection<Player> nearbyPlayers = location.getWorld().getPlayers();
        
        if (nearbyPlayers.isEmpty()) {
            return; // No players to see the particles
        }
        
        // Find closest player distance
        double closestDistance = Double.MAX_VALUE;
        for (Player player : nearbyPlayers) {
            double distance = player.getLocation().distance(location);
            if (distance < closestDistance) {
                closestDistance = distance;
            }
        }
        
        // Determine particle count and spread based on distance
        int particleCount;
        double spread;
        
        if (closestDistance <= DISTANCE_CLOSE) {
            particleCount = PARTICLES_CLOSE;
            spread = SPREAD_CLOSE;
        } else if (closestDistance <= DISTANCE_MEDIUM) {
            particleCount = PARTICLES_MEDIUM;
            spread = SPREAD_MEDIUM;
        } else if (closestDistance <= DISTANCE_FAR) {
            particleCount = PARTICLES_FAR;
            spread = SPREAD_FAR;
        } else {
            particleCount = PARTICLES_VERY_FAR;
            spread = SPREAD_FAR;
        }
        
        // Spawn particles
        try {
            location.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                location,
                particleCount,
                spread, spread, spread,
                0.0,
                blockType.createBlockData()
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn particle shell: " + e.getMessage());
        }
    }
    
    /**
     * Render particle shell with custom particle count (ignores distance optimization)
     * 
     * @param location Location to render particles
     * @param blockType Material type for BLOCK_CRACK particles
     * @param particleCount Number of particles to spawn
     */
    public void renderShell(Location location, Material blockType, int particleCount) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        if (blockType == null || !blockType.isBlock()) {
            blockType = Material.STONE;
        }
        
        try {
            location.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                location,
                particleCount,
                SPREAD_CLOSE, SPREAD_CLOSE, SPREAD_CLOSE,
                0.0,
                blockType.createBlockData()
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn particle shell: " + e.getMessage());
        }
    }
    
    /**
     * Render particle shell with custom spread
     * 
     * @param location Location to render particles
     * @param blockType Material type for BLOCK_CRACK particles
     * @param particleCount Number of particles to spawn
     * @param spread Particle spread radius
     */
    public void renderShell(Location location, Material blockType, int particleCount, double spread) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        if (blockType == null || !blockType.isBlock()) {
            blockType = Material.STONE;
        }
        
        try {
            location.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                location,
                particleCount,
                spread, spread, spread,
                0.0,
                blockType.createBlockData()
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn particle shell: " + e.getMessage());
        }
    }
    
    /**
     * Render impact particles when block controller collides
     * 
     * @param location Impact location
     * @param blockType Material type for particles
     */
    public void renderImpact(Location location, Material blockType) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        if (blockType == null || !blockType.isBlock()) {
            blockType = Material.STONE;
        }
        
        try {
            // Larger burst of particles for impact
            location.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                location,
                30,
                0.5, 0.5, 0.5,
                0.1,
                blockType.createBlockData()
            );
            
            // Add some dust particles for extra effect
            location.getWorld().spawnParticle(
                Particle.BLOCK_DUST,
                location,
                20,
                0.6, 0.6, 0.6,
                0.05,
                blockType.createBlockData()
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn impact particles: " + e.getMessage());
        }
    }
    
    /**
     * Get optimized particle count for a given distance
     * Useful for external callers to determine particle budget
     * 
     * @param distance Distance from nearest player
     * @return Recommended particle count
     */
    public static int getOptimizedParticleCount(double distance) {
        if (distance <= DISTANCE_CLOSE) {
            return PARTICLES_CLOSE;
        } else if (distance <= DISTANCE_MEDIUM) {
            return PARTICLES_MEDIUM;
        } else if (distance <= DISTANCE_FAR) {
            return PARTICLES_FAR;
        } else {
            return PARTICLES_VERY_FAR;
        }
    }
    
    /**
     * Check if a material is valid for particle rendering
     * 
     * @param material Material to check
     * @return true if material can be used for BLOCK_CRACK particles
     */
    public static boolean isValidBlockMaterial(Material material) {
        return material != null && material.isBlock() && material.isSolid();
    }
    
    /**
     * Get a fallback material if the provided material is invalid
     * 
     * @param material Material to validate
     * @return The material if valid, otherwise STONE
     */
    public static Material getValidMaterial(Material material) {
        if (isValidBlockMaterial(material)) {
            return material;
        }
        return Material.STONE;
    }
}
