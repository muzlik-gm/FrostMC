package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

/**
 * Armor stand collider system for packet-only blocks.
 * 
 * Creates invisible armor stand hitboxes at packet block positions to enable
 * server-side collision detection. Generates sounds for collisions and ensures
 * cleanup when packet blocks expire.
 * 
 * Per SYSTEM.md specification:
 * - Create invisible armor stand hitboxes at packet block positions
 * - Implement server-side collision detection
 * - Add sound generation for collisions
 * - Ensure cleanup when packet blocks expire
 * 
 * Requirements: 12.6, 17.6
 */
public class PacketBlockCollider {
    private final JavaPlugin plugin;
    
    // Track colliders by group ID
    private final Map<UUID, List<ArmorStand>> collidersByGroup;
    
    // Collision sounds
    private static final Sound COLLISION_SOUND = Sound.BLOCK_STONE_STEP;
    private static final float COLLISION_VOLUME = 0.5f;
    private static final float COLLISION_PITCH = 1.0f;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     */
    public PacketBlockCollider(JavaPlugin plugin) {
        this.plugin = plugin;
        this.collidersByGroup = new HashMap<>();
    }
    
    /**
     * Create colliders for a packet block group
     * 
     * @param groupId UUID of the packet block group
     * @param locations Locations where colliders should be placed
     * @return Number of colliders created
     */
    public int createColliders(UUID groupId, Collection<Location> locations) {
        if (groupId == null || locations == null || locations.isEmpty()) {
            return 0;
        }
        
        List<ArmorStand> colliders = new ArrayList<>();
        
        for (Location location : locations) {
            if (location == null || location.getWorld() == null) {
                continue;
            }
            
            try {
                // Spawn invisible armor stand at block center
                Location centerLoc = location.clone().add(0.5, 0.0, 0.5);
                ArmorStand collider = (ArmorStand) location.getWorld().spawnEntity(
                    centerLoc,
                    EntityType.ARMOR_STAND
                );
                
                // Configure as invisible hitbox
                collider.setVisible(false);
                collider.setGravity(false);
                collider.setMarker(false); // false = has collision
                collider.setInvulnerable(true);
                collider.setCollidable(true); // true = can collide
                collider.setCustomNameVisible(false);
                collider.setSmall(true);
                collider.setBasePlate(false);
                collider.setArms(false);
                
                // Tag with metadata
                collider.setMetadata("packetBlockCollider", 
                    new org.bukkit.metadata.FixedMetadataValue(plugin, groupId.toString()));
                
                colliders.add(collider);
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Failed to create collider at " + location, e);
            }
        }
        
        // Store colliders
        collidersByGroup.put(groupId, colliders);
        
        plugin.getLogger().log(Level.FINE, 
            "Created " + colliders.size() + " colliders for group " + groupId);
        
        return colliders.size();
    }
    
    /**
     * Check for collisions with a collider
     * 
     * @param collider The armor stand collider
     * @param entity The entity to check collision with
     * @return true if collision detected
     */
    public boolean checkCollision(ArmorStand collider, Entity entity) {
        if (collider == null || entity == null) {
            return false;
        }
        
        // Check if entity is within collision range
        double distance = collider.getLocation().distance(entity.getLocation());
        
        // Collision range (armor stand is small, so use tight range)
        double collisionRange = 0.6;
        
        return distance <= collisionRange;
    }
    
    /**
     * Handle collision event
     * Plays collision sound at the collision location
     * 
     * @param collider The collider that was hit
     * @param entity The entity that collided
     */
    public void handleCollision(ArmorStand collider, Entity entity) {
        if (collider == null || entity == null) {
            return;
        }
        
        Location collisionLoc = collider.getLocation();
        
        // Play collision sound
        if (collisionLoc.getWorld() != null) {
            collisionLoc.getWorld().playSound(
                collisionLoc,
                COLLISION_SOUND,
                COLLISION_VOLUME,
                COLLISION_PITCH
            );
        }
        
        // If entity is a player, send feedback
        if (entity instanceof Player) {
            Player player = (Player) entity;
            // Could send actionbar message or other feedback here
        }
        
        plugin.getLogger().log(Level.FINE, 
            "Collision detected: " + entity.getType() + " with collider at " + collisionLoc);
    }
    
    /**
     * Remove colliders for a packet block group
     * 
     * @param groupId UUID of the packet block group
     * @return Number of colliders removed
     */
    public int removeColliders(UUID groupId) {
        List<ArmorStand> colliders = collidersByGroup.remove(groupId);
        
        if (colliders == null || colliders.isEmpty()) {
            return 0;
        }
        
        int removed = 0;
        for (ArmorStand collider : colliders) {
            try {
                if (collider.isValid()) {
                    collider.remove();
                    removed++;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Failed to remove collider", e);
            }
        }
        
        plugin.getLogger().log(Level.FINE, 
            "Removed " + removed + " colliders for group " + groupId);
        
        return removed;
    }
    
    /**
     * Get colliders for a packet block group
     * 
     * @param groupId UUID of the packet block group
     * @return List of colliders, or empty list if none
     */
    public List<ArmorStand> getColliders(UUID groupId) {
        List<ArmorStand> colliders = collidersByGroup.get(groupId);
        return colliders != null ? new ArrayList<>(colliders) : new ArrayList<>();
    }
    
    /**
     * Get the number of active collider groups
     * 
     * @return Number of groups
     */
    public int getActiveGroupCount() {
        return collidersByGroup.size();
    }
    
    /**
     * Get the total number of active colliders
     * 
     * @return Total number of colliders
     */
    public int getTotalColliderCount() {
        return collidersByGroup.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Check if an armor stand is a packet block collider
     * 
     * @param entity The entity to check
     * @return true if it's a packet block collider
     */
    public boolean isPacketBlockCollider(Entity entity) {
        if (!(entity instanceof ArmorStand)) {
            return false;
        }
        
        return entity.hasMetadata("packetBlockCollider");
    }
    
    /**
     * Get the group ID for a collider
     * 
     * @param collider The collider armor stand
     * @return Group ID, or null if not found
     */
    public UUID getGroupId(ArmorStand collider) {
        if (collider == null || !collider.hasMetadata("packetBlockCollider")) {
            return null;
        }
        
        try {
            String groupIdStr = collider.getMetadata("packetBlockCollider").get(0).asString();
            return UUID.fromString(groupIdStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Clear all colliders
     */
    public void clearAll() {
        plugin.getLogger().info("Clearing all packet block colliders...");
        
        for (UUID groupId : new ArrayList<>(collidersByGroup.keySet())) {
            removeColliders(groupId);
        }
        
        collidersByGroup.clear();
        
        plugin.getLogger().info("All packet block colliders cleared");
    }
    
    /**
     * Shutdown the collider system
     */
    public void shutdown() {
        clearAll();
        plugin.getLogger().info("PacketBlockCollider shutdown complete");
    }
}
