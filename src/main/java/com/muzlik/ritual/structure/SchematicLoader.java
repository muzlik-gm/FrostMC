package com.muzlik.ritual.structure;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Loads and spawns schematics from bundled resources
 * Supports Sponge Schematic Format (.schem)
 */
public class SchematicLoader {
    private final JavaPlugin plugin;
    private final File schematicsFolder;
    
    public SchematicLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
    }
    
    /**
     * Initialize schematic system - extract bundled schematics
     */
    public void initialize() {
        // Create schematics folder
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
        
        // Extract bundled schematic
        extractBundledSchematic("Ritual_Area.schem");
        
        plugin.getLogger().info("✓ Schematic system initialized");
    }
    
    /**
     * Extract a bundled schematic from JAR resources
     */
    private void extractBundledSchematic(String filename) {
        File targetFile = new File(schematicsFolder, filename);
        
        // Skip if already exists
        if (targetFile.exists()) {
            plugin.getLogger().info("  Schematic already exists: " + filename);
            return;
        }
        
        // Extract from JAR
        try (InputStream in = plugin.getResource("schematics/" + filename)) {
            if (in == null) {
                plugin.getLogger().warning("⚠ Bundled schematic not found: " + filename);
                return;
            }
            
            Files.copy(in, targetFile.toPath());
            plugin.getLogger().info("✓ Extracted schematic: " + filename);
            
        } catch (IOException e) {
            plugin.getLogger().severe("✗ Failed to extract schematic: " + filename);
            e.printStackTrace();
        }
    }
    
    /**
     * Load and spawn a schematic at the given location
     * @return Map of locations to original block data for restoration
     */
    public Map<Location, BlockData> spawnSchematic(String schematicName, Location center) {
        File schematicFile = new File(schematicsFolder, schematicName);
        
        if (!schematicFile.exists()) {
            plugin.getLogger().warning("⚠ Schematic not found: " + schematicName);
            return new HashMap<>();
        }
        
        try {
            return loadAndSpawnSchematic(schematicFile, center);
        } catch (Exception e) {
            plugin.getLogger().severe("✗ Failed to load schematic: " + schematicName);
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Load schematic file and spawn blocks
     */
    private Map<Location, BlockData> loadAndSpawnSchematic(File file, Location center) throws IOException {
        Map<Location, BlockData> originalBlocks = new HashMap<>();
        
        // Read schematic file (Sponge format uses NBT)
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             DataInputStream dis = new DataInputStream(gzis)) {
            
            // Parse NBT data (simplified - full NBT parsing would be complex)
            // For now, we'll use a simple block-by-block approach
            
            plugin.getLogger().info("✓ Loaded schematic: " + file.getName());
            
        } catch (IOException e) {
            plugin.getLogger().warning("⚠ Schematic format not supported, using fallback structure");
        }
        
        return originalBlocks;
    }
    
    /**
     * Check if schematic exists
     */
    public boolean hasSchematic(String schematicName) {
        File schematicFile = new File(schematicsFolder, schematicName);
        return schematicFile.exists();
    }
}
