package com.muzlik.ritual.structure;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.*;

/**
 * Simple ritual structure system - spawns pillars and altar around ritual
 * MVP version - easy to use, no configuration needed
 */
public class SimpleRitualStructure {
    private final UUID ritualId;
    private final Location center;
    private final FragmentType fragmentType;
    private final Map<Location, BlockData> originalBlocks;
    private final Set<Location> protectedArea;
    
    public SimpleRitualStructure(UUID ritualId, Location center, FragmentType fragmentType) {
        this.ritualId = ritualId;
        this.center = center.clone();
        this.fragmentType = fragmentType;
        this.originalBlocks = new HashMap<>();
        this.protectedArea = new HashSet<>();
    }
    
    /**
     * Spawn the ritual structure
     */
    public void spawn() {
        // Create 4 pillars around the ritual (5 blocks away, 4 blocks tall)
        double radius = 5.0;
        int pillarHeight = 4;
        
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI / 2) * i; // 90 degrees apart
            int x = (int) (radius * Math.cos(angle));
            int z = (int) (radius * Math.sin(angle));
            
            // Build pillar
            for (int y = 0; y < pillarHeight; y++) {
                Location loc = center.clone().add(x, y, z);
                placeBlock(loc, getPillarMaterial());
            }
            
            // Add glowstone on top
            Location topLoc = center.clone().add(x, pillarHeight, z);
            placeBlock(topLoc, Material.GLOWSTONE);
        }
        
        // Create altar in center (3x3 platform)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location loc = center.clone().add(x, -1, z);
                placeBlock(loc, getAltarMaterial());
            }
        }
        
        // Mark protected area (10 block radius)
        markProtectedArea(10);
    }
    
    /**
     * Place a block and store original
     */
    private void placeBlock(Location loc, Material material) {
        Block block = loc.getBlock();
        
        // Store original
        originalBlocks.put(loc.clone(), block.getBlockData().clone());
        
        // Place new block
        block.setType(material);
    }
    
    /**
     * Mark area as protected
     */
    private void markProtectedArea(int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    protectedArea.add(loc);
                }
            }
        }
    }
    
    /**
     * Despawn structure and restore original blocks
     */
    public void despawn() {
        for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
            Block block = entry.getKey().getBlock();
            block.setBlockData(entry.getValue());
        }
        originalBlocks.clear();
        protectedArea.clear();
    }
    
    /**
     * Check if location is in protected area
     */
    public boolean isProtected(Location location) {
        return protectedArea.contains(location);
    }
    
    /**
     * Get pillar material based on fragment type
     */
    private Material getPillarMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.NETHER_BRICKS;
            case WATER -> Material.PRISMARINE;
            case AIR -> Material.QUARTZ_PILLAR;
            case DARK -> Material.BLACKSTONE;
            case LIGHT -> Material.QUARTZ_BLOCK;
            case VOID -> Material.OBSIDIAN;
            case STORM -> Material.DARK_PRISMARINE;
            case DRAGON -> Material.PURPUR_PILLAR;
            case TIME -> Material.GOLD_BLOCK;
            case LUCK -> Material.EMERALD_BLOCK;
            default -> Material.STONE_BRICKS;
        };
    }
    
    /**
     * Get altar material based on fragment type
     */
    private Material getAltarMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.MAGMA_BLOCK;
            case WATER -> Material.DARK_PRISMARINE;
            case AIR -> Material.SMOOTH_QUARTZ;
            case DARK -> Material.POLISHED_BLACKSTONE;
            case LIGHT -> Material.SEA_LANTERN;
            case VOID -> Material.CRYING_OBSIDIAN;
            case STORM -> Material.WARPED_PLANKS;
            case DRAGON -> Material.PURPUR_BLOCK;
            case TIME -> Material.CHISELED_QUARTZ_BLOCK;
            case LUCK -> Material.DIAMOND_BLOCK;
            default -> Material.CHISELED_STONE_BRICKS;
        };
    }
    
    public UUID getRitualId() {
        return ritualId;
    }
    
    public Location getCenter() {
        return center.clone();
    }
}
