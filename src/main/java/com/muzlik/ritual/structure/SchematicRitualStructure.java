package com.muzlik.ritual.structure;

import com.muzlik.fragment.FragmentType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Schematic-based ritual structure - uses WorldEdit to load and paste schematics
 * Falls back to procedural generation if WorldEdit is not available
 */
public class SchematicRitualStructure implements RitualStructure {
    private final UUID ritualId;
    private final Location center;
    private final FragmentType fragmentType;
    private final JavaPlugin plugin;
    private final Map<Location, BlockData> originalBlocks;
    private final Set<Location> protectedArea;
    private final boolean worldEditAvailable;
    private SimpleRitualStructure fallbackStructure;
    
    public SchematicRitualStructure(JavaPlugin plugin, UUID ritualId, Location center, FragmentType fragmentType) {
        this.plugin = plugin;
        this.ritualId = ritualId;
        this.center = center.clone();
        this.fragmentType = fragmentType;
        this.originalBlocks = new HashMap<>();
        this.protectedArea = new HashSet<>();
        this.worldEditAvailable = checkWorldEdit();
        
        // Create fallback structure
        this.fallbackStructure = new SimpleRitualStructure(ritualId, center, fragmentType);
    }
    
    /**
     * Check if WorldEdit is available
     */
    private boolean checkWorldEdit() {
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            return Bukkit.getPluginManager().getPlugin("WorldEdit") != null ||
                   Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Spawn the ritual structure
     */
    public void spawn() {
        if (worldEditAvailable) {
            try {
                spawnSchematic();
                plugin.getLogger().info("✓ Spawned schematic ritual structure");
                return;
            } catch (Exception e) {
                plugin.getLogger().warning("⚠ Failed to spawn schematic, using fallback: " + e.getMessage());
            }
        }
        
        // Fallback to procedural generation
        fallbackStructure.spawn();
    }
    
    /**
     * Spawn schematic using WorldEdit
     */
    private void spawnSchematic() throws IOException {
        File schematicFile = getSchematicFile();
        
        if (!schematicFile.exists()) {
            throw new IOException("Schematic file not found: " + schematicFile.getPath());
        }
        
        // Load schematic
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            throw new IOException("Unknown schematic format");
        }
        
        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = format.getReader(fis)) {
            
            Clipboard clipboard = reader.read();
            
            // Convert Bukkit location to WorldEdit location
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(center.getWorld());
            
            // Get schematic info
            BlockVector3 clipboardOrigin = clipboard.getOrigin();
            BlockVector3 clipboardMin = clipboard.getMinimumPoint();
            BlockVector3 clipboardMax = clipboard.getMaximumPoint();
            
            // Calculate schematic dimensions
            int width = clipboardMax.getBlockX() - clipboardMin.getBlockX() + 1;
            int length = clipboardMax.getBlockZ() - clipboardMin.getBlockZ() + 1;
            int height = clipboardMax.getBlockY() - clipboardMin.getBlockY() + 1;
            
            // Calculate the center of the schematic relative to its origin
            BlockVector3 schematicCenter = BlockVector3.at(
                (clipboardMin.getBlockX() + clipboardMax.getBlockX()) / 2,
                clipboardMin.getBlockY(), // Keep Y at minimum (ground level)
                (clipboardMin.getBlockZ() + clipboardMax.getBlockZ()) / 2
            );
            
            // Calculate offset from origin to center
            BlockVector3 originToCenter = schematicCenter.subtract(clipboardOrigin);
            
            // Calculate paste location: ritual center minus the offset to schematic center
            // Raise by +1 block so everything is one block higher
            BlockVector3 pasteLocation = BlockVector3.at(
                center.getBlockX() - originToCenter.getBlockX(),
                center.getBlockY() - originToCenter.getBlockY() + 1,
                center.getBlockZ() - originToCenter.getBlockZ()
            );
            
            plugin.getLogger().info(String.format("Schematic info - Size: %dx%dx%d, Origin: %s, Center: %s", 
                width, length, height, clipboardOrigin, schematicCenter));
            plugin.getLogger().info(String.format("Ritual center: (%d, %d, %d), Paste at: %s", 
                center.getBlockX(), center.getBlockY(), center.getBlockZ(), pasteLocation));
            
            // Store original blocks before pasting (with delay to ensure blocks are placed first)
            storeOriginalBlocks(clipboard, pasteLocation);
            
            // Paste schematic centered at the ritual location
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pasteLocation)
                    .ignoreAirBlocks(false)
                    .build();
                
                Operations.complete(operation);
            } catch (com.sk89q.worldedit.WorldEditException e) {
                throw new IOException("Failed to paste schematic: " + e.getMessage(), e);
            }
            
            // Mark protected area with larger radius
            markProtectedArea(20);
        }
    }
    
    /**
     * Store original blocks that will be replaced
     */
    private void storeOriginalBlocks(Clipboard clipboard, BlockVector3 pasteLocation) {
        BlockVector3 clipboardOrigin = clipboard.getOrigin();
        BlockVector3 clipboardMin = clipboard.getMinimumPoint();
        BlockVector3 clipboardMax = clipboard.getMaximumPoint();
        
        // Calculate offset
        BlockVector3 offset = pasteLocation.subtract(clipboardOrigin);
        
        // Store all blocks in the clipboard region
        for (int x = clipboardMin.getBlockX(); x <= clipboardMax.getBlockX(); x++) {
            for (int y = clipboardMin.getBlockY(); y <= clipboardMax.getBlockY(); y++) {
                for (int z = clipboardMin.getBlockZ(); z <= clipboardMax.getBlockZ(); z++) {
                    BlockVector3 clipboardPos = BlockVector3.at(x, y, z);
                    BlockVector3 worldPos = clipboardPos.add(offset);
                    
                    Location loc = new Location(
                        center.getWorld(),
                        worldPos.getBlockX(),
                        worldPos.getBlockY(),
                        worldPos.getBlockZ()
                    );
                    
                    Block block = loc.getBlock();
                    originalBlocks.put(loc.clone(), block.getBlockData().clone());
                }
            }
        }
    }
    
    /**
     * Get schematic file, extracting from JAR if needed
     */
    private File getSchematicFile() throws IOException {
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
        
        File schematicFile = new File(schematicsFolder, "Ritual_Area.schem");
        
        // Extract from JAR if not exists
        if (!schematicFile.exists()) {
            try (InputStream in = plugin.getResource("schematics/Ritual_Area.schem")) {
                if (in == null) {
                    throw new IOException("Bundled schematic not found in JAR");
                }
                Files.copy(in, schematicFile.toPath());
                plugin.getLogger().info("✓ Extracted ritual schematic from JAR");
            }
        }
        
        return schematicFile;
    }
    
    /**
     * Mark area as protected
     */
    private void markProtectedArea(int radius) {
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = new Location(center.getWorld(), centerX + x, centerY + y, centerZ + z);
                    protectedArea.add(loc);
                }
            }
        }
        
        plugin.getLogger().info(String.format("Protected %d blocks in %d block radius", protectedArea.size(), radius));
    }
    
    /**
     * Despawn structure and restore original blocks
     */
    public void despawn() {
        if (worldEditAvailable && !originalBlocks.isEmpty()) {
            // Sort blocks by Y coordinate (bottom to top) to prevent physics issues
            List<Map.Entry<Location, BlockData>> sortedBlocks = new ArrayList<>(originalBlocks.entrySet());
            sortedBlocks.sort(Comparator.comparingInt(e -> e.getKey().getBlockY()));
            
            // Restore blocks from bottom to top
            for (Map.Entry<Location, BlockData> entry : sortedBlocks) {
                try {
                    Block block = entry.getKey().getBlock();
                    block.setBlockData(entry.getValue(), false); // false = no physics update
                } catch (Exception e) {
                    // Ignore errors during cleanup
                }
            }
            
            // Schedule a second pass after 2 ticks to catch any missed blocks
            org.bukkit.scheduler.BukkitRunnable cleanupTask = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Location, BlockData> entry : sortedBlocks) {
                        try {
                            Block block = entry.getKey().getBlock();
                            if (!block.getBlockData().matches(entry.getValue())) {
                                block.setBlockData(entry.getValue(), false);
                            }
                        } catch (Exception e) {
                            // Ignore errors
                        }
                    }
                }
            };
            cleanupTask.runTaskLater(plugin, 2L);
            
            originalBlocks.clear();
            protectedArea.clear();
        } else {
            // Use fallback
            fallbackStructure.despawn();
        }
    }
    
    /**
     * Check if location is in protected area
     */
    public boolean isProtected(Location location) {
        if (!protectedArea.isEmpty()) {
            // Check by coordinates instead of Location object equality
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            
            for (Location protectedLoc : protectedArea) {
                if (protectedLoc.getBlockX() == x && 
                    protectedLoc.getBlockY() == y && 
                    protectedLoc.getBlockZ() == z &&
                    protectedLoc.getWorld().equals(location.getWorld())) {
                    return true;
                }
            }
            return false;
        }
        return fallbackStructure.isProtected(location);
    }
    
    public UUID getRitualId() {
        return ritualId;
    }
    
    public Location getCenter() {
        return center.clone();
    }
}
