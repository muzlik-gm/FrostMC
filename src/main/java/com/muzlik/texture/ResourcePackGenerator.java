package com.muzlik.texture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.muzlik.fragment.FragmentType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Generates resource pack template files for custom textures.
 * Creates the necessary JSON files and folder structure for a Minecraft resource pack.
 * 
 * Usage: Run this once to generate the template, then add your custom textures.
 */
public class ResourcePackGenerator {
    
    private final JavaPlugin plugin;
    private final Gson gson;
    
    public ResourcePackGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * Generate complete resource pack template
     */
    public void generateResourcePack() {
        File resourcePackDir = new File(plugin.getDataFolder(), "resourcepack");
        
        try {
            // Create directory structure
            createDirectoryStructure(resourcePackDir);
            
            // Generate pack.mcmeta
            generatePackMcmeta(resourcePackDir);
            
            // Generate item model overrides for paper.json
            generatePaperModel(resourcePackDir);
            
            // Generate example model files
            generateExampleModels(resourcePackDir);
            
            // Copy texture files from plugin textures folder
            copyTextureFiles(resourcePackDir);
            
            // Generate texture mapping documentation
            generateTextureMapping(resourcePackDir);
            
            plugin.getLogger().info("Resource pack generated at: " + resourcePackDir.getAbsolutePath());
            plugin.getLogger().info("Fragment textures have been copied automatically!");
            plugin.getLogger().info("Zip the 'resourcepack' folder and distribute to players!");
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate resource pack: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create directory structure for resource pack
     */
    private void createDirectoryStructure(File baseDir) {
        new File(baseDir, "assets/minecraft/models/item").mkdirs();
        new File(baseDir, "assets/minecraft/textures/item/fragments").mkdirs();
        new File(baseDir, "assets/minecraft/textures/item/abilities").mkdirs();
        new File(baseDir, "assets/minecraft/textures/item/mana").mkdirs();
        new File(baseDir, "assets/minecraft/textures/item/ui").mkdirs();
    }
    
    /**
     * Generate pack.mcmeta file
     */
    private void generatePackMcmeta(File baseDir) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", 34); // Minecraft 1.21.x format
        pack.addProperty("description", "FrostSMP Fragment System Custom Textures");
        root.add("pack", pack);
        
        File file = new File(baseDir, "pack.mcmeta");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        }
    }
    
    /**
     * Generate paper.json model with all custom model data overrides
     */
    private void generatePaperModel(File baseDir) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "item/generated");
        
        // Default texture
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "item/paper");
        root.add("textures", textures);
        
        // Overrides array
        JsonArray overrides = new JsonArray();
        
        // Add Fragment overrides
        for (Map.Entry<FragmentType, Integer> entry : TextureRegistry.getAllFragmentTextures().entrySet()) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", entry.getValue());
            override.add("predicate", predicate);
            override.addProperty("model", "item/fragments/" + entry.getKey().name().toLowerCase());
            overrides.add(override);
        }
        
        // Add ability overrides
        for (Map.Entry<String, Integer> entry : TextureRegistry.getAllAbilityTextures().entrySet()) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", entry.getValue());
            override.add("predicate", predicate);
            override.addProperty("model", "item/abilities/" + entry.getKey());
            overrides.add(override);
        }
        
        // Add mana overrides
        for (Map.Entry<String, Integer> entry : TextureRegistry.getAllManaTextures().entrySet()) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", entry.getValue());
            override.add("predicate", predicate);
            override.addProperty("model", "item/mana/" + entry.getKey());
            overrides.add(override);
        }
        
        // Add UI overrides
        for (Map.Entry<String, Integer> entry : TextureRegistry.getAllUITextures().entrySet()) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", entry.getValue());
            override.add("predicate", predicate);
            override.addProperty("model", "item/ui/" + entry.getKey());
            overrides.add(override);
        }
        
        root.add("overrides", overrides);
        
        File file = new File(baseDir, "assets/minecraft/models/item/paper.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        }
    }
    
    /**
     * Generate texture mapping documentation
     */
    private void generateTextureMapping(File baseDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# FrostSMP Fragment System - Texture Mapping\n\n");
        sb.append("This document lists all custom model data IDs and their corresponding textures.\n\n");
        
        sb.append("## Fragment Icons (1000-1099)\n");
        sb.append("Place textures in: assets/minecraft/textures/item/fragments/\n\n");
        for (Map.Entry<FragmentType, Integer> entry : TextureRegistry.getAllFragmentTextures().entrySet()) {
            sb.append(String.format("- %d: %s.png (Fragment: %s)\n", 
                    entry.getValue(), entry.getKey().name().toLowerCase(), entry.getKey().getDisplayName()));
        }
        
        sb.append("\n## Ability Icons (2000-2999)\n");
        sb.append("Place textures in: assets/minecraft/textures/item/abilities/\n\n");
        for (Map.Entry<String, Integer> entry : TextureRegistry.getAllAbilityTextures().entrySet()) {
            sb.append(String.format("- %d: %s.png\n", entry.getValue(), entry.getKey()));
        }
        
        sb.append("\n## Mana Indicators (3000-3099)\n");
        sb.append("Place textures in: assets/minecraft/textures/item/mana/\n\n");
        for (Map.Entry<String, Integer> entry : TextureRegistry.getAllManaTextures().entrySet()) {
            sb.append(String.format("- %d: %s.png\n", entry.getValue(), entry.getKey()));
        }
        
        sb.append("\n## UI Elements (4000-4099)\n");
        sb.append("Place textures in: assets/minecraft/textures/item/ui/\n\n");
        for (Map.Entry<String, Integer> entry : TextureRegistry.getAllUITextures().entrySet()) {
            sb.append(String.format("- %d: %s.png\n", entry.getValue(), entry.getKey()));
        }
        
        sb.append("\n## How to Create Textures\n\n");
        sb.append("1. Create 16x16 PNG images for each texture\n");
        sb.append("2. Place them in the appropriate folders listed above\n");
        sb.append("3. Create corresponding .json model files (see generated paper.json for examples)\n");
        sb.append("4. Zip the entire 'resourcepack' folder\n");
        sb.append("5. Distribute to players or host on a server\n\n");
        sb.append("## Model File Template\n\n");
        sb.append("Each texture needs a corresponding model file. Example for fire.json:\n\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"parent\": \"item/generated\",\n");
        sb.append("  \"textures\": {\n");
        sb.append("    \"layer0\": \"item/fragments/fire\"\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("```\n");
        
        File file = new File(baseDir, "TEXTURE_MAPPING.md");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
        }
    }
    
    /**
     * Generate example model files for all textures
     */
    public void generateExampleModels(File baseDir) throws IOException {
        // Generate Fragment models
        for (FragmentType type : FragmentType.values()) {
            generateModelFile(baseDir, "fragments", type.name().toLowerCase(), 
                    "item/fragments/" + type.name().toLowerCase());
        }
        
        // Generate ability models
        for (String abilityId : TextureRegistry.getAllAbilityTextures().keySet()) {
            generateModelFile(baseDir, "abilities", abilityId, "item/abilities/" + abilityId);
        }
        
        // Generate mana models
        for (String manaType : TextureRegistry.getAllManaTextures().keySet()) {
            generateModelFile(baseDir, "mana", manaType, "item/mana/" + manaType);
        }
        
        // Generate UI models
        for (String uiElement : TextureRegistry.getAllUITextures().keySet()) {
            generateModelFile(baseDir, "ui", uiElement, "item/ui/" + uiElement);
        }
    }
    
    /**
     * Generate a single model file
     */
    private void generateModelFile(File baseDir, String category, String name, String texturePath) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "item/generated");
        
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texturePath);
        root.add("textures", textures);
        
        File file = new File(baseDir, "assets/minecraft/models/item/" + category + "/" + name + ".json");
        file.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        }
        
        plugin.getLogger().info("  Generated model: " + category + "/" + name + ".json → " + texturePath);
    }
    
    /**
     * Copy texture files from plugin's textures folder to resource pack
     */
    private void copyTextureFiles(File resourcePackDir) {
        // Get the textures folder from the plugin's root directory (not data folder)
        File pluginFolder = plugin.getDataFolder().getParentFile().getParentFile();
        File texturesSource = new File(pluginFolder, "textures");
        
        if (!texturesSource.exists()) {
            plugin.getLogger().warning("Textures folder not found at: " + texturesSource.getAbsolutePath());
            plugin.getLogger().warning("Skipping texture file copy. Add PNG files manually to the resource pack.");
            return;
        }
        
        int copiedCount = 0;
        
        // Copy Fragment textures - they're directly in textures/ folder
        File fragmentsDest = new File(resourcePackDir, "assets/minecraft/textures/item/fragments");
        fragmentsDest.mkdirs();
        
        // List of Fragment texture files to copy
        String[] fragmentFiles = {"fire.png", "water.png", "air.png", "dark.png", 
                                  "light.png", "void.png", "dragon.png", "storm.png"};
        
        for (String fileName : fragmentFiles) {
            File sourceFile = new File(texturesSource, fileName);
            if (sourceFile.exists()) {
                File destFile = new File(fragmentsDest, fileName);
                try {
                    copyFile(sourceFile, destFile);
                    copiedCount++;
                    plugin.getLogger().info("  Copied: " + fileName);
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to copy " + fileName + ": " + e.getMessage());
                }
            } else {
                plugin.getLogger().warning("  Missing: " + fileName);
            }
        }
        
        // Copy Ability textures (if they exist in subdirectory)
        File abilitiesSource = new File(texturesSource, "abilities");
        if (abilitiesSource.exists() && abilitiesSource.isDirectory()) {
            File abilitiesDest = new File(resourcePackDir, "assets/minecraft/textures/item/abilities");
            copiedCount += copyPngFiles(abilitiesSource, abilitiesDest);
        }
        
        // Copy Mana textures (if they exist in subdirectory)
        File manaSource = new File(texturesSource, "mana");
        if (manaSource.exists() && manaSource.isDirectory()) {
            File manaDest = new File(resourcePackDir, "assets/minecraft/textures/item/mana");
            copiedCount += copyPngFiles(manaSource, manaDest);
        }
        
        // Copy UI textures (if they exist in subdirectory)
        File uiSource = new File(texturesSource, "ui");
        if (uiSource.exists() && uiSource.isDirectory()) {
            File uiDest = new File(resourcePackDir, "assets/minecraft/textures/item/ui");
            copiedCount += copyPngFiles(uiSource, uiDest);
        }
        
        if (copiedCount > 0) {
            plugin.getLogger().info("Copied " + copiedCount + " texture files to resource pack!");
        } else {
            plugin.getLogger().warning("No texture files found to copy. Add PNG files manually.");
        }
    }
    
    /**
     * Copy all PNG files from source to destination directory
     */
    private int copyPngFiles(File sourceDir, File destDir) {
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            return 0;
        }
        
        destDir.mkdirs();
        int count = 0;
        
        File[] files = sourceDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null) {
            for (File sourceFile : files) {
                File destFile = new File(destDir, sourceFile.getName());
                try {
                    copyFile(sourceFile, destFile);
                    count++;
                    plugin.getLogger().info("  Copied: " + sourceFile.getName());
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to copy " + sourceFile.getName() + ": " + e.getMessage());
                }
            }
        }
        
        return count;
    }
    
    /**
     * Copy a single file
     */
    private void copyFile(File source, File dest) throws IOException {
        java.nio.file.Files.copy(
            source.toPath(), 
            dest.toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );
    }
}
