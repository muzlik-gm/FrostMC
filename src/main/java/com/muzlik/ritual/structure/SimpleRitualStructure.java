package com.muzlik.ritual.structure;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.*;

/**
 * Complex ritual structure system - spawns elaborate multi-layer structures
 * 10x more complex than MVP - beautiful, ritual-friendly designs
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
     * Spawn the ritual structure - Complex, beautiful design
     */
    public void spawn() {
        // LAYER 1: Outer ring of pillars (8 pillars, 7 blocks away, 6 blocks tall)
        spawnOuterPillars();
        
        // LAYER 2: Inner ring of smaller pillars (4 pillars, 5 blocks away, 4 blocks tall)
        spawnInnerPillars();
        
        // LAYER 3: Decorative arches between outer pillars
        spawnArches();
        
        // LAYER 4: Ground rune circle (radius 6)
        spawnRuneCircle();
        
        // LAYER 5: Central altar platform (5x5, elevated)
        spawnCentralAltar();
        
        // LAYER 6: Corner braziers with fire/light
        spawnBraziers();
        
        // LAYER 7: Floating crystals around the ritual
        spawnFloatingCrystals();
        
        // LAYER 8: Decorative ground patterns
        spawnGroundPatterns();
        
        // Mark protected area (12 block radius)
        markProtectedArea(12);
    }
    
    /**
     * Spawn outer ring of 8 tall pillars
     */
    private void spawnOuterPillars() {
        double radius = 7.0;
        int pillarHeight = 6;
        
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI / 4) * i; // 45 degrees apart
            int x = (int) Math.round(radius * Math.cos(angle));
            int z = (int) Math.round(radius * Math.sin(angle));
            
            // Build pillar with decorative base and top
            for (int y = 0; y < pillarHeight; y++) {
                Location loc = center.clone().add(x, y, z);
                if (y == 0 || y == pillarHeight - 1) {
                    placeBlock(loc, getPillarAccentMaterial());
                } else {
                    placeBlock(loc, getPillarMaterial());
                }
            }
            
            // Add glowing top
            Location topLoc = center.clone().add(x, pillarHeight, z);
            placeBlock(topLoc, getGlowMaterial());
            
            // Add decorative base (3x3 around pillar base)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue; // Skip center (pillar)
                    Location baseLoc = center.clone().add(x + dx, -1, z + dz);
                    placeBlock(baseLoc, getPillarBaseMaterial());
                }
            }
        }
    }
    
    /**
     * Spawn inner ring of 4 medium pillars
     */
    private void spawnInnerPillars() {
        double radius = 5.0;
        int pillarHeight = 4;
        
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI / 2) * i; // 90 degrees apart
            int x = (int) Math.round(radius * Math.cos(angle));
            int z = (int) Math.round(radius * Math.sin(angle));
            
            // Build pillar
            for (int y = 0; y < pillarHeight; y++) {
                Location loc = center.clone().add(x, y, z);
                placeBlock(loc, getPillarMaterial());
            }
            
            // Add lantern on top
            Location topLoc = center.clone().add(x, pillarHeight, z);
            placeBlock(topLoc, Material.LANTERN);
        }
    }
    
    /**
     * Spawn decorative arches between outer pillars
     */
    private void spawnArches() {
        double radius = 7.0;
        int archHeight = 5;
        
        // Create arches between adjacent outer pillars
        for (int i = 0; i < 8; i++) {
            double angle1 = (Math.PI / 4) * i;
            double angle2 = (Math.PI / 4) * ((i + 1) % 8);
            
            int x1 = (int) Math.round(radius * Math.cos(angle1));
            int z1 = (int) Math.round(radius * Math.sin(angle1));
            int x2 = (int) Math.round(radius * Math.cos(angle2));
            int z2 = (int) Math.round(radius * Math.sin(angle2));
            
            // Place arch blocks at the midpoint
            int midX = (x1 + x2) / 2;
            int midZ = (z1 + z2) / 2;
            
            Location archLoc = center.clone().add(midX, archHeight, midZ);
            placeBlock(archLoc, getArchMaterial());
        }
    }
    
    /**
     * Spawn ground rune circle
     */
    private void spawnRuneCircle() {
        double radius = 6.0;
        
        // Create circular pattern on ground
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
            int x = (int) Math.round(radius * Math.cos(angle));
            int z = (int) Math.round(radius * Math.sin(angle));
            
            Location loc = center.clone().add(x, -1, z);
            placeBlock(loc, getRuneMaterial());
            
            // Add inner circle
            int innerX = (int) Math.round((radius - 2) * Math.cos(angle));
            int innerZ = (int) Math.round((radius - 2) * Math.sin(angle));
            Location innerLoc = center.clone().add(innerX, -1, innerZ);
            placeBlock(innerLoc, getRuneAccentMaterial());
        }
        
        // Add rune symbols at cardinal directions
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI / 2) * i;
            int x = (int) Math.round((radius + 1) * Math.cos(angle));
            int z = (int) Math.round((radius + 1) * Math.sin(angle));
            
            Location runeLoc = center.clone().add(x, -1, z);
            placeBlock(runeLoc, getRuneSymbolMaterial());
        }
    }
    
    /**
     * Spawn central altar platform (5x5 elevated)
     */
    private void spawnCentralAltar() {
        // Create 5x5 platform at ground level
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location loc = center.clone().add(x, -1, z);
                
                // Outer ring of platform
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    placeBlock(loc, getAltarBorderMaterial());
                } else {
                    placeBlock(loc, getAltarMaterial());
                }
            }
        }
        
        // Create elevated center (3x3, 1 block up)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location loc = center.clone().add(x, 0, z);
                
                // Center block is special
                if (x == 0 && z == 0) {
                    placeBlock(loc, getAltarCenterMaterial());
                } else {
                    placeBlock(loc, getAltarMaterial());
                }
            }
        }
    }
    
    /**
     * Spawn corner braziers with fire/light
     */
    private void spawnBraziers() {
        int[][] corners = {{3, 3}, {3, -3}, {-3, 3}, {-3, -3}};
        
        for (int[] corner : corners) {
            int x = corner[0];
            int z = corner[1];
            
            // Build brazier stand (2 blocks tall)
            Location base = center.clone().add(x, 0, z);
            placeBlock(base, getBrazierMaterial());
            
            Location top = center.clone().add(x, 1, z);
            placeBlock(top, getBrazierMaterial());
            
            // Add fire/light on top
            Location fire = center.clone().add(x, 2, z);
            placeBlock(fire, getBrazierLightMaterial());
        }
    }
    
    /**
     * Spawn floating crystals around the ritual
     */
    private void spawnFloatingCrystals() {
        double radius = 4.0;
        int floatHeight = 3;
        
        // 4 floating crystals at diagonal positions
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI / 4) + (Math.PI / 2) * i; // 45, 135, 225, 315 degrees
            int x = (int) Math.round(radius * Math.cos(angle));
            int z = (int) Math.round(radius * Math.sin(angle));
            
            Location crystalLoc = center.clone().add(x, floatHeight, z);
            placeBlock(crystalLoc, getCrystalMaterial());
            
            // Add glowing base under crystal
            Location baseLoc = center.clone().add(x, floatHeight - 1, z);
            placeBlock(baseLoc, getGlowMaterial());
        }
    }
    
    /**
     * Spawn decorative ground patterns
     */
    private void spawnGroundPatterns() {
        // Create cross pattern from center
        for (int i = 1; i <= 3; i++) {
            // North-South line
            placeBlock(center.clone().add(0, -1, i), getPatternMaterial());
            placeBlock(center.clone().add(0, -1, -i), getPatternMaterial());
            
            // East-West line
            placeBlock(center.clone().add(i, -1, 0), getPatternMaterial());
            placeBlock(center.clone().add(-i, -1, 0), getPatternMaterial());
        }
        
        // Add diagonal accents
        for (int i = 1; i <= 2; i++) {
            placeBlock(center.clone().add(i, -1, i), getPatternAccentMaterial());
            placeBlock(center.clone().add(i, -1, -i), getPatternAccentMaterial());
            placeBlock(center.clone().add(-i, -1, i), getPatternAccentMaterial());
            placeBlock(center.clone().add(-i, -1, -i), getPatternAccentMaterial());
        }
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
            case WATER -> Material.PRISMARINE_BRICKS;
            case AIR -> Material.QUARTZ_PILLAR;
            case DARK -> Material.BLACKSTONE;
            case LIGHT -> Material.QUARTZ_BLOCK;
            case VOID -> Material.OBSIDIAN;
            case STORM -> Material.DARK_PRISMARINE;
            case DRAGON -> Material.PURPUR_PILLAR;
            case TIME -> Material.GOLD_BLOCK;
            case LUCK -> Material.EMERALD_BLOCK;
            case ADMIN -> Material.BEDROCK;
            default -> Material.STONE_BRICKS;
        };
    }
    
    /**
     * Get pillar accent material (decorative top/bottom)
     */
    private Material getPillarAccentMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.RED_NETHER_BRICKS;
            case WATER -> Material.DARK_PRISMARINE;
            case AIR -> Material.CHISELED_QUARTZ_BLOCK;
            case DARK -> Material.POLISHED_BLACKSTONE_BRICKS;
            case LIGHT -> Material.CHISELED_QUARTZ_BLOCK;
            case VOID -> Material.CRYING_OBSIDIAN;
            case STORM -> Material.WARPED_HYPHAE;
            case DRAGON -> Material.PURPUR_BLOCK;
            case TIME -> Material.CHISELED_QUARTZ_BLOCK;
            case LUCK -> Material.DIAMOND_BLOCK;
            case ADMIN -> Material.NETHERITE_BLOCK;
            default -> Material.CHISELED_STONE_BRICKS;
        };
    }
    
    /**
     * Get pillar base material (ground decoration)
     */
    private Material getPillarBaseMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.MAGMA_BLOCK;
            case WATER -> Material.PRISMARINE;
            case AIR -> Material.SMOOTH_QUARTZ;
            case DARK -> Material.POLISHED_BLACKSTONE;
            case LIGHT -> Material.SMOOTH_QUARTZ;
            case VOID -> Material.BLACKSTONE;
            case STORM -> Material.WARPED_PLANKS;
            case DRAGON -> Material.PURPUR_SLAB;
            case TIME -> Material.SMOOTH_QUARTZ;
            case LUCK -> Material.PRISMARINE;
            case ADMIN -> Material.OBSIDIAN;
            default -> Material.COBBLESTONE;
        };
    }
    
    /**
     * Get glow material (light sources)
     */
    private Material getGlowMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.GLOWSTONE;
            case WATER -> Material.SEA_LANTERN;
            case AIR -> Material.GLOWSTONE;
            case DARK -> Material.SHROOMLIGHT;
            case LIGHT -> Material.SEA_LANTERN;
            case VOID -> Material.SHROOMLIGHT;
            case STORM -> Material.SEA_LANTERN;
            case DRAGON -> Material.END_ROD;
            case TIME -> Material.GLOWSTONE;
            case LUCK -> Material.SEA_LANTERN;
            case ADMIN -> Material.BEACON;
            default -> Material.GLOWSTONE;
        };
    }
    
    /**
     * Get arch material (decorative arches)
     */
    private Material getArchMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.NETHER_BRICK_FENCE;
            case WATER -> Material.PRISMARINE_WALL;
            case AIR -> Material.QUARTZ_STAIRS;
            case DARK -> Material.BLACKSTONE_WALL;
            case LIGHT -> Material.QUARTZ_STAIRS;
            case VOID -> Material.BLACKSTONE_WALL;
            case STORM -> Material.DARK_PRISMARINE;
            case DRAGON -> Material.PURPUR_STAIRS;
            case TIME -> Material.QUARTZ_STAIRS;
            case LUCK -> Material.PRISMARINE_WALL;
            case ADMIN -> Material.IRON_BARS;
            default -> Material.STONE_BRICK_WALL;
        };
    }
    
    /**
     * Get rune material (ground circle)
     */
    private Material getRuneMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.NETHERRACK;
            case WATER -> Material.PRISMARINE;
            case AIR -> Material.WHITE_CONCRETE;
            case DARK -> Material.BLACKSTONE;
            case LIGHT -> Material.QUARTZ_BLOCK;
            case VOID -> Material.OBSIDIAN;
            case STORM -> Material.CYAN_CONCRETE;
            case DRAGON -> Material.PURPUR_BLOCK;
            case TIME -> Material.YELLOW_CONCRETE;
            case LUCK -> Material.LIME_CONCRETE;
            case ADMIN -> Material.RED_CONCRETE;
            default -> Material.STONE;
        };
    }
    
    /**
     * Get rune accent material (inner circle)
     */
    private Material getRuneAccentMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.MAGMA_BLOCK;
            case WATER -> Material.DARK_PRISMARINE;
            case AIR -> Material.LIGHT_GRAY_CONCRETE;
            case DARK -> Material.POLISHED_BLACKSTONE;
            case LIGHT -> Material.WHITE_CONCRETE;
            case VOID -> Material.CRYING_OBSIDIAN;
            case STORM -> Material.BLUE_CONCRETE;
            case DRAGON -> Material.PURPUR_PILLAR;
            case TIME -> Material.ORANGE_CONCRETE;
            case LUCK -> Material.GREEN_CONCRETE;
            case ADMIN -> Material.BLACK_CONCRETE;
            default -> Material.COBBLESTONE;
        };
    }
    
    /**
     * Get rune symbol material (cardinal points)
     */
    private Material getRuneSymbolMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.FIRE_CORAL_BLOCK;
            case WATER -> Material.TUBE_CORAL_BLOCK;
            case AIR -> Material.WHITE_GLAZED_TERRACOTTA;
            case DARK -> Material.BLACK_GLAZED_TERRACOTTA;
            case LIGHT -> Material.YELLOW_GLAZED_TERRACOTTA;
            case VOID -> Material.PURPLE_GLAZED_TERRACOTTA;
            case STORM -> Material.CYAN_GLAZED_TERRACOTTA;
            case DRAGON -> Material.MAGENTA_GLAZED_TERRACOTTA;
            case TIME -> Material.ORANGE_GLAZED_TERRACOTTA;
            case LUCK -> Material.LIME_GLAZED_TERRACOTTA;
            case ADMIN -> Material.RED_GLAZED_TERRACOTTA;
            default -> Material.STONE;
        };
    }
    
    /**
     * Get altar material (main platform)
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
            case ADMIN -> Material.NETHERITE_BLOCK;
            default -> Material.CHISELED_STONE_BRICKS;
        };
    }
    
    /**
     * Get altar border material (outer ring)
     */
    private Material getAltarBorderMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.NETHER_BRICKS;
            case WATER -> Material.PRISMARINE_BRICKS;
            case AIR -> Material.QUARTZ_BRICKS;
            case DARK -> Material.POLISHED_BLACKSTONE_BRICKS;
            case LIGHT -> Material.QUARTZ_BRICKS;
            case VOID -> Material.OBSIDIAN;
            case STORM -> Material.DARK_PRISMARINE;
            case DRAGON -> Material.PURPUR_PILLAR;
            case TIME -> Material.GOLD_BLOCK;
            case LUCK -> Material.EMERALD_BLOCK;
            case ADMIN -> Material.BEDROCK;
            default -> Material.STONE_BRICKS;
        };
    }
    
    /**
     * Get altar center material (focal point)
     */
    private Material getAltarCenterMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.FIRE_CORAL_BLOCK;
            case WATER -> Material.TUBE_CORAL_BLOCK;
            case AIR -> Material.WHITE_CONCRETE;
            case DARK -> Material.BLACK_CONCRETE;
            case LIGHT -> Material.YELLOW_CONCRETE;
            case VOID -> Material.PURPLE_CONCRETE;
            case STORM -> Material.CYAN_CONCRETE;
            case DRAGON -> Material.MAGENTA_CONCRETE;
            case TIME -> Material.ORANGE_CONCRETE;
            case LUCK -> Material.LIME_CONCRETE;
            case ADMIN -> Material.RED_CONCRETE;
            default -> Material.STONE;
        };
    }
    
    /**
     * Get brazier material (corner stands)
     */
    private Material getBrazierMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.NETHER_BRICK_FENCE;
            case WATER -> Material.PRISMARINE_WALL;
            case AIR -> Material.QUARTZ_PILLAR;
            case DARK -> Material.BLACKSTONE_WALL;
            case LIGHT -> Material.QUARTZ_PILLAR;
            case VOID -> Material.BLACKSTONE_WALL;
            case STORM -> Material.DARK_PRISMARINE;
            case DRAGON -> Material.PURPUR_PILLAR;
            case TIME -> Material.GOLD_BLOCK;
            case LUCK -> Material.EMERALD_BLOCK;
            case ADMIN -> Material.IRON_BARS;
            default -> Material.STONE_BRICK_WALL;
        };
    }
    
    /**
     * Get brazier light material (fire/light on top)
     */
    private Material getBrazierLightMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.FIRE;
            case WATER -> Material.SOUL_FIRE;
            case AIR -> Material.TORCH;
            case DARK -> Material.SOUL_CAMPFIRE;
            case LIGHT -> Material.TORCH;
            case VOID -> Material.SOUL_FIRE;
            case STORM -> Material.SOUL_CAMPFIRE;
            case DRAGON -> Material.FIRE;
            case TIME -> Material.TORCH;
            case LUCK -> Material.TORCH;
            case ADMIN -> Material.FIRE;
            default -> Material.TORCH;
        };
    }
    
    /**
     * Get crystal material (floating crystals)
     */
    private Material getCrystalMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.FIRE_CORAL_BLOCK;
            case WATER -> Material.TUBE_CORAL_BLOCK;
            case AIR -> Material.WHITE_STAINED_GLASS;
            case DARK -> Material.BLACK_STAINED_GLASS;
            case LIGHT -> Material.YELLOW_STAINED_GLASS;
            case VOID -> Material.PURPLE_STAINED_GLASS;
            case STORM -> Material.CYAN_STAINED_GLASS;
            case DRAGON -> Material.MAGENTA_STAINED_GLASS;
            case TIME -> Material.ORANGE_STAINED_GLASS;
            case LUCK -> Material.LIME_STAINED_GLASS;
            case ADMIN -> Material.RED_STAINED_GLASS;
            default -> Material.GLASS;
        };
    }
    
    /**
     * Get pattern material (ground cross)
     */
    private Material getPatternMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.RED_CONCRETE;
            case WATER -> Material.BLUE_CONCRETE;
            case AIR -> Material.WHITE_CONCRETE;
            case DARK -> Material.BLACK_CONCRETE;
            case LIGHT -> Material.YELLOW_CONCRETE;
            case VOID -> Material.PURPLE_CONCRETE;
            case STORM -> Material.CYAN_CONCRETE;
            case DRAGON -> Material.MAGENTA_CONCRETE;
            case TIME -> Material.ORANGE_CONCRETE;
            case LUCK -> Material.LIME_CONCRETE;
            case ADMIN -> Material.RED_CONCRETE;
            default -> Material.GRAY_CONCRETE;
        };
    }
    
    /**
     * Get pattern accent material (diagonal accents)
     */
    private Material getPatternAccentMaterial() {
        return switch (fragmentType) {
            case FIRE -> Material.ORANGE_CONCRETE;
            case WATER -> Material.LIGHT_BLUE_CONCRETE;
            case AIR -> Material.LIGHT_GRAY_CONCRETE;
            case DARK -> Material.GRAY_CONCRETE;
            case LIGHT -> Material.WHITE_CONCRETE;
            case VOID -> Material.MAGENTA_CONCRETE;
            case STORM -> Material.BLUE_CONCRETE;
            case DRAGON -> Material.PINK_CONCRETE;
            case TIME -> Material.YELLOW_CONCRETE;
            case LUCK -> Material.GREEN_CONCRETE;
            case ADMIN -> Material.BLACK_CONCRETE;
            default -> Material.STONE;
        };
    }
    
    public UUID getRitualId() {
        return ritualId;
    }
    
    public Location getCenter() {
        return center.clone();
    }
}
