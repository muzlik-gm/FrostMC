package your.plugin.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a simple ritual circle when you don't have WorldEdit schematics
 * 
 * Just makes some basic circles with obsidian and glowstone
 * Nothing fancy but it gets the job done
 */
public class SimpleRitualStructure {
    private final UUID ritualId;
    private final Location center;
    private final CrystalType crystalType;
    private final Map<Location, BlockData> originalBlocks;
    
    public SimpleRitualStructure(UUID ritualId, Location center, CrystalType crystalType) {
        this.ritualId = ritualId;
        this.center = center.clone();
        this.crystalType = crystalType;
        this.originalBlocks = new HashMap<>();
    }
    
    /**
     * Build the ritual structure
     */
    public void spawn() {
        // Just create a simple circle pattern
        createRitualCircle();
    }
    
    /**
     * Make concentric circles with different materials
     */
    private void createRitualCircle() {
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        // Make 3 circles of different sizes
        for (int radius = 1; radius <= 3; radius++) {
            for (int angle = 0; angle < 360; angle += 15) { // every 15 degrees
                double radians = Math.toRadians(angle);
                int x = centerX + (int) Math.round(radius * Math.cos(radians));
                int z = centerZ + (int) Math.round(radius * Math.sin(radians));
                
                Location blockLoc = new Location(center.getWorld(), x, centerY, z);
                Block block = blockLoc.getBlock();
                
                // Save what was there before so we can put it back later
                originalBlocks.put(blockLoc.clone(), block.getBlockData().clone());
                
                // Different material for each ring
                Material material = switch (radius) {
                    case 1 -> Material.GLOWSTONE;           // inner ring - bright
                    case 2 -> Material.OBSIDIAN;            // middle ring - dark
                    case 3 -> getCrystalMaterial();         // outer ring - depends on crystal type
                    default -> Material.STONE;
                };
                
                block.setType(material);
            }
        }
        
        // Add some pillars at the cardinal directions for extra flair
        createPillars();
    }
    
    /**
     * Add some pillars at north/south/east/west - looks cooler
     */
    private void createPillars() {
        int[] directions = {0, 90, 180, 270}; // N, E, S, W
        
        for (int angle : directions) {
            double radians = Math.toRadians(angle);
            int x = center.getBlockX() + (int) Math.round(5 * Math.cos(radians));
            int z = center.getBlockZ() + (int) Math.round(5 * Math.sin(radians));
            
            // Make 3-block tall pillars
            for (int y = 0; y < 3; y++) {
                Location pillarLoc = new Location(center.getWorld(), x, center.getBlockY() + y, z);
                Block block = pillarLoc.getBlock();
                
                // Remember what was here
                originalBlocks.put(pillarLoc.clone(), block.getBlockData().clone());
                
                // Glowstone on top, obsidian for the base
                Material material = y == 2 ? Material.GLOWSTONE : Material.OBSIDIAN;
                block.setType(material);
            }
        }
    }
    
    /**
     * Pick a material based on what crystal type this is
     */
    private Material getCrystalMaterial() {
        return switch (crystalType) {
            case FIRE -> Material.NETHERRACK;
            case WATER -> Material.PRISMARINE;
            case AIR -> Material.QUARTZ_BLOCK;
            case EARTH -> Material.MOSS_BLOCK;
            case DARK -> Material.BLACKSTONE;
            case LIGHT -> Material.SEA_LANTERN;
            case VOID -> Material.PURPUR_BLOCK;
            case STORM -> Material.LAPIS_BLOCK;
            case TIME -> Material.AMETHYST_BLOCK;
            case LUCK -> Material.EMERALD_BLOCK;
        };
    }
    
    /**
     * Clean up the structure and put everything back how it was
     */
    public void despawn() {
        // Put all the original blocks back
        for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
            try {
                Block block = entry.getKey().getBlock();
                block.setBlockData(entry.getValue(), false);
            } catch (Exception e) {
                // Sometimes this fails but whatever, not a big deal
            }
        }
        
        originalBlocks.clear();
    }
    
    /**
     * Check if someone is trying to break part of our ritual structure
     */
    public boolean isProtected(Location location) {
        // Check if this location is part of our structure
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        for (Location structureLoc : originalBlocks.keySet()) {
            if (structureLoc.getBlockX() == x && 
                structureLoc.getBlockY() == y && 
                structureLoc.getBlockZ() == z &&
                structureLoc.getWorld().equals(location.getWorld())) {
                return true;
            }
        }
        return false;
    }
    
    public UUID getRitualId() {
        return ritualId;
    }
    
    public Location getCenter() {
        return center.clone();
    }
}