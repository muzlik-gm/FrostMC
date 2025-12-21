package com.muzlik.vfx.cinematic.miniblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Main manager for mini block system.
 * Wires together spawner, impact handler, and scatter effects.
 * Enforces max 100 active mini blocks server-wide.
 * 
 * Requirements: 3.1-3.5, 9.4
 */
public class MiniBlockManager {
    
    private static final int MAX_MINI_BLOCKS = 100;
    
    private final MiniBlockSpawner spawner;
    private final ImpactVFXHandler impactHandler;
    private final FragmentScatterEffect scatterEffect;
    private final Plugin plugin;
    
    public MiniBlockManager(Plugin plugin) {
        this.plugin = plugin;
        this.spawner = new MiniBlockSpawner();
        this.impactHandler = new ImpactVFXHandler();
        this.scatterEffect = new FragmentScatterEffect();
    }
    
    /**
     * Spawn a mini block projectile
     * 
     * @param config Configuration for the mini block
     * @return UUID of spawned mini block, or null if at capacity
     */
    public UUID spawnMiniBlock(MiniBlockConfig config) {
        // Check capacity
        if (spawner.getActiveCount() >= MAX_MINI_BLOCKS) {
            return null; // At max capacity
        }
        
        return spawner.spawnMiniBlock(config);
    }
    
    /**
     * Handle ground impact - VFX only, no block placement
     * 
     * @param miniBlockId The mini block that impacted
     * @param impactLocation Where it hit
     */
    public void handleGroundImpact(UUID miniBlockId, Location impactLocation) {
        MiniBlockSpawner.MiniBlockProjectile projectile = spawner.getProjectile(miniBlockId);
        if (projectile == null) {
            return;
        }
        
        // Get block type from armor stand helmet
        Material blockType = Material.STONE;
        if (projectile.getArmorStand().getEquipment() != null && 
            projectile.getArmorStand().getEquipment().getHelmet() != null) {
            blockType = projectile.getArmorStand().getEquipment().getHelmet().getType();
        }
        
        // Spawn impact VFX (no block placement)
        impactHandler.handleGroundImpact(impactLocation, blockType);
        
        // Spawn fragment scatter
        scatterEffect.spawnFragmentScatter(impactLocation, blockType);
        
        // Remove projectile
        spawner.removeProjectile(miniBlockId);
    }
    
    /**
     * Handle entity impact - damage and knockback, no block placement
     * 
     * @param miniBlockId The mini block that impacted
     * @param target The entity hit
     * @param impactLocation Where it hit
     */
    public void handleEntityImpact(UUID miniBlockId, Entity target, Location impactLocation) {
        MiniBlockSpawner.MiniBlockProjectile projectile = spawner.getProjectile(miniBlockId);
        if (projectile == null) {
            return;
        }
        
        // Get block type from armor stand helmet
        Material blockType = Material.STONE;
        if (projectile.getArmorStand().getEquipment() != null && 
            projectile.getArmorStand().getEquipment().getHelmet() != null) {
            blockType = projectile.getArmorStand().getEquipment().getHelmet().getType();
        }
        
        // Apply damage and knockback (no block placement)
        impactHandler.handleEntityImpact(target, impactLocation, projectile.getDamage(), blockType);
        
        // Spawn fragment scatter
        scatterEffect.spawnFragmentScatter(impactLocation, blockType);
        
        // Remove projectile
        spawner.removeProjectile(miniBlockId);
    }
    
    /**
     * Spawn fragment scatter effect when mini block is destroyed
     * 
     * @param location Destruction location
     * @param blockType Type of block for appropriate fragments
     */
    public void spawnFragmentScatter(Location location, Material blockType) {
        scatterEffect.spawnFragmentScatter(location, blockType);
    }
    
    /**
     * Update all mini blocks (call every tick)
     */
    public void tick() {
        spawner.tick();
    }
    
    /**
     * Clean up all mini blocks (call on plugin disable)
     */
    public void cleanup() {
        spawner.removeAll();
    }
    
    /**
     * Get count of active mini blocks
     */
    public int getActiveCount() {
        return spawner.getActiveCount();
    }
    
    /**
     * Get max mini blocks allowed
     */
    public int getMaxMiniBlocks() {
        return MAX_MINI_BLOCKS;
    }
}
