package com.muzlik.ritual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays magic circle textures on the ground using ItemDisplay entities
 * Rank-based texture selection:
 * - Rank 1-2: magic_circle1
 * - Rank 3-4: magic_circle2
 * - Rank 5-6: magic_circle3
 * - Rank 7-8: magic_circle4
 */
public class MagicCircleDisplay {
    
    private final JavaPlugin plugin;
    private final List<ItemDisplay> displays;
    private BukkitRunnable rotationTask;
    
    // Custom model data for magic circles
    private static final int MAGIC_CIRCLE_1_CMD = 5001;
    private static final int MAGIC_CIRCLE_2_CMD = 5002;
    private static final int MAGIC_CIRCLE_3_CMD = 5003;
    private static final int MAGIC_CIRCLE_4_CMD = 5004;
    private static final int SIDE_MAGIC_CIRCLE_1_CMD = 5005;
    private static final int SIDE_MAGIC_CIRCLE_2_CMD = 5006;
    
    public MagicCircleDisplay(JavaPlugin plugin) {
        this.plugin = plugin;
        this.displays = new ArrayList<>();
    }
    
    /**
     * Create magic circle display at location
     * @param center Center location for the magic circle
     * @param rank Fragment rank (determines texture complexity)
     * @param size Size of the magic circle in blocks
     */
    public void create(Location center, int rank, double size) {
        if (center.getWorld() == null) return;
        
        // Determine which texture to use based on rank
        int customModelData = getMagicCircleTexture(rank);
        
        // Create ItemStack with custom model data for main circle
        ItemStack circleItem = new ItemStack(Material.PAPER);
        ItemMeta meta = circleItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            circleItem.setItemMeta(meta);
        }
        
        // Create main magic circle (on ground, rotating)
        // Use fixed yaw/pitch to prevent player view affecting orientation
        Location displayLoc = center.clone().add(0, 0.01, 0);
        displayLoc.setYaw(0);
        displayLoc.setPitch(0);
        ItemDisplay mainCircle = createCircleDisplay(displayLoc, circleItem, size);
        displays.add(mainCircle);
        
        // Create side circle item with appropriate texture
        ItemStack sideCircleItem = new ItemStack(Material.PAPER);
        ItemMeta sideMeta = sideCircleItem.getItemMeta();
        if (sideMeta != null) {
            // Use side_magic_circle1 for ranks 5-6, side_magic_circle2 for ranks 7+
            int sideCircleCmd = (rank >= 7) ? SIDE_MAGIC_CIRCLE_2_CMD : SIDE_MAGIC_CIRCLE_1_CMD;
            sideMeta.setCustomModelData(sideCircleCmd);
            sideCircleItem.setItemMeta(sideMeta);
        }
        
        // Add side circles for higher ranks (INCREASED DISTANCE from 4 to 5)
        if (rank >= 5) {
            // 2 side circles (left and right) - 5 blocks from main circle
            double sideDistance = 5.0;
            double sideSize = size * 0.4;
            
            Location leftLoc = center.clone().add(-sideDistance, 0.01, 0);
            leftLoc.setYaw(0);
            leftLoc.setPitch(0);
            Location rightLoc = center.clone().add(sideDistance, 0.01, 0);
            rightLoc.setYaw(0);
            rightLoc.setPitch(0);
            
            ItemDisplay leftCircle = createCircleDisplay(leftLoc, sideCircleItem, sideSize);
            ItemDisplay rightCircle = createCircleDisplay(rightLoc, sideCircleItem, sideSize);
            
            displays.add(leftCircle);
            displays.add(rightCircle);
        }
        
        if (rank >= 7) {
            // 4 side circles (cardinal directions) - 5 blocks from main circle
            double sideDistance = 5.0;
            double sideSize = size * 0.4;
            
            Location northLoc = center.clone().add(0, 0.01, -sideDistance);
            northLoc.setYaw(0);
            northLoc.setPitch(0);
            Location southLoc = center.clone().add(0, 0.01, sideDistance);
            southLoc.setYaw(0);
            southLoc.setPitch(0);
            
            ItemDisplay northCircle = createCircleDisplay(northLoc, sideCircleItem, sideSize);
            ItemDisplay southCircle = createCircleDisplay(southLoc, sideCircleItem, sideSize);
            
            displays.add(northCircle);
            displays.add(southCircle);
        }
        
        // Start rotation animation
        startRotation();
        
    }
    
    /**
     * Create a single circle display entity
     */
    private ItemDisplay createCircleDisplay(Location loc, ItemStack item, double size) {
        // Spawn the entity at a location with fixed orientation
        ItemDisplay display = (ItemDisplay) loc.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
        display.setItemStack(item);
        display.setBrightness(new Display.Brightness(15, 15)); // Full brightness
        
        // Use FIXED mode - shows items as flat 2D textures (like item frames)
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        
        // Set transformation to lay flat on ground and scale
        Transformation transform = display.getTransformation();
        
        // Create a quaternion for flat rotation facing up
        // We need to rotate so the texture faces upward (+Y direction)
        org.joml.Quaternionf flatRotation = new org.joml.Quaternionf();
        // Rotate 90 degrees around X to make it horizontal (face up)
        flatRotation.rotationX((float) Math.toRadians(-90));
        transform.getLeftRotation().set(flatRotation);
        
        // Reset right rotation to identity
        transform.getRightRotation().identity();
        
        // Scale to desired size
        float scale = (float) size;
        transform.getScale().set(scale, scale, scale);
        
        display.setTransformation(transform);
        display.setInterpolationDuration(5);
        display.setInterpolationDelay(0);
        
        return display;
    }
    
    /**
     * Start rotation animation for all circles
     * OPTIMIZED: Smooth rotation with smaller angle steps
     */
    private void startRotation() {
        rotationTask = new BukkitRunnable() {
            float angle = 0;
            
            @Override
            public void run() {
                if (displays.isEmpty() || displays.get(0).isDead()) {
                    cancel();
                    return;
                }
                
                angle += 0.03f; // Smooth rotation speed (reduced from 0.1f)
                if (angle >= 2 * Math.PI) {
                    angle = 0;
                }
                
                // Update rotation for all displays
                for (ItemDisplay display : displays) {
                    if (display != null && !display.isDead()) {
                        Transformation transform = display.getTransformation();
                        
                        // Create rotation: lay flat then spin
                        org.joml.Quaternionf rotation = new org.joml.Quaternionf();
                        // First rotate to lay flat (-90° around X)
                        rotation.rotationX((float) Math.toRadians(-90));
                        // Then rotate around the local Z axis (which is now pointing up) for spinning
                        rotation.rotateZ(angle);
                        
                        transform.getLeftRotation().set(rotation);
                        transform.getRightRotation().identity();
                        
                        display.setTransformation(transform);
                    }
                }
            }
        };
        // OPTIMIZED: Update every 2 ticks for smooth animation
        rotationTask.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Get magic circle texture based on rank
     * Rank 1-2: magic_circle1
     * Rank 3-4: magic_circle2
     * Rank 5-6: magic_circle3
     * Rank 7-8+: magic_circle4
     */
    private int getMagicCircleTexture(int rank) {
        if (rank <= 2) {
            return MAGIC_CIRCLE_1_CMD;
        } else if (rank <= 4) {
            return MAGIC_CIRCLE_2_CMD;
        } else if (rank <= 6) {
            return MAGIC_CIRCLE_3_CMD;
        } else {
            return MAGIC_CIRCLE_4_CMD;
        }
    }
    
    /**
     * Remove all magic circle displays
     */
    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }
        
        for (ItemDisplay display : displays) {
            if (display != null && !display.isDead()) {
                display.remove();
            }
        }
        displays.clear();
    }
    
    /**
     * Check if displays are still active
     */
    public boolean isActive() {
        return !displays.isEmpty() && !displays.get(0).isDead();
    }
}
