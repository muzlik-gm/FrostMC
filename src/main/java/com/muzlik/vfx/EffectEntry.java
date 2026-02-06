package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a single effect entry in the ActiveEffectRegistry.
 * Tracks all metadata needed for effect management and cleanup.
 * 
 * Enhanced per SYSTEM.md specification to track:
 * - Linked entities (armor stands, falling blocks)
 * - Ability-specific metadata (configFlags)
 * - Ability ID for damage attribution
 */
public class EffectEntry {
    private final UUID effectId;
    private final UUID ownerId;
    private final String abilityId;
    private final EffectType type;
    private final Location location;
    private final long spawnTime;
    private final long removalTime;
    private final Object effectData;
    
    // Enhanced tracking per SYSTEM.md
    private final List<Entity> linkedEntities;
    private final Map<String, Object> configFlags;
    
    /**
     * Constructor for EffectEntry
     * 
     * @param effectId Unique identifier for this effect
     * @param ownerId UUID of the player who owns this effect
     * @param type Type of effect
     * @param location Location where effect was spawned
     * @param spawnTime System time when effect was spawned (milliseconds)
     * @param removalTime System time when effect should be removed (milliseconds)
     * @param effectData Type-specific data (Entity, Block, etc.)
     */
    public EffectEntry(UUID effectId, UUID ownerId, EffectType type, Location location,
                      long spawnTime, long removalTime, Object effectData) {
        this(effectId, ownerId, null, type, location, spawnTime, removalTime, effectData);
    }
    
    /**
     * Enhanced constructor for EffectEntry with ability ID
     * 
     * @param effectId Unique identifier for this effect
     * @param ownerId UUID of the player who owns this effect
     * @param abilityId Ability identifier (e.g., "fire_flame_burst")
     * @param type Type of effect
     * @param location Location where effect was spawned
     * @param spawnTime System time when effect was spawned (milliseconds)
     * @param removalTime System time when effect should be removed (milliseconds)
     * @param effectData Type-specific data (Entity, Block, etc.)
     */
    public EffectEntry(UUID effectId, UUID ownerId, String abilityId, EffectType type, Location location,
                      long spawnTime, long removalTime, Object effectData) {
        this.effectId = effectId;
        this.ownerId = ownerId;
        this.abilityId = abilityId;
        this.type = type;
        this.location = location;
        this.spawnTime = spawnTime;
        this.removalTime = removalTime;
        this.effectData = effectData;
        this.linkedEntities = new CopyOnWriteArrayList<>();
        this.configFlags = new HashMap<>();
    }
    
    /**
     * Check if this effect has expired
     * 
     * @return true if current time is past removal time
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= removalTime;
    }
    
    /**
     * Clean up this effect's resources
     * Removes entities, restores blocks, etc.
     * Enhanced to cleanup all linked entities per SYSTEM.md
     */
    public void cleanup() {
        // Cleanup all linked entities first
        for (Entity entity : linkedEntities) {
            if (entity != null && entity.isValid() && !entity.isDead()) {
                entity.remove();
            }
        }
        linkedEntities.clear();
        
        // Cleanup primary effect data
        if (effectData == null) {
            return;
        }
        
        switch (type) {
            case ENTITY:
            case ARMOR_STAND:
            case PROJECTILE:
            case SUMMON:
                if (effectData instanceof Entity) {
                    Entity entity = (Entity) effectData;
                    if (entity.isValid() && !entity.isDead()) {
                        entity.remove();
                    }
                }
                break;
                
            case BLOCK:
            case BLOCK_MANIPULATION:
                // Block restoration handled by EnvironmentManager
                break;
                
            case PARTICLE:
            case SOUND:
            case BUFF:
            case DEBUFF:
            case TRANSFORMATION:
            case AREA_EFFECT:
                // These clean up automatically
                break;
        }
    }
    
    /**
     * Add a linked entity to this effect
     * Linked entities will be cleaned up when the effect is removed
     * 
     * @param entity Entity to link (armor stand, falling block, etc.)
     */
    public void addLinkedEntity(Entity entity) {
        if (entity != null && !linkedEntities.contains(entity)) {
            linkedEntities.add(entity);
        }
    }
    
    /**
     * Remove a linked entity from this effect
     * 
     * @param entity Entity to unlink
     * @return true if entity was removed
     */
    public boolean removeLinkedEntity(Entity entity) {
        return linkedEntities.remove(entity);
    }
    
    /**
     * Get all linked entities
     * 
     * @return Unmodifiable list of linked entities
     */
    public List<Entity> getLinkedEntities() {
        return Collections.unmodifiableList(linkedEntities);
    }
    
    /**
     * Set a config flag for ability-specific metadata
     * 
     * @param key Flag key
     * @param value Flag value
     */
    public void setConfigFlag(String key, Object value) {
        configFlags.put(key, value);
    }
    
    /**
     * Get a config flag value
     * 
     * @param key Flag key
     * @return Flag value, or null if not set
     */
    public Object getConfigFlag(String key) {
        return configFlags.get(key);
    }
    
    /**
     * Get a config flag value with type casting
     * 
     * @param key Flag key
     * @param type Expected type
     * @param <T> Type parameter
     * @return Flag value cast to type, or null if not set or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigFlag(String key, Class<T> type) {
        Object value = configFlags.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Get all config flags
     * 
     * @return Unmodifiable map of config flags
     */
    public Map<String, Object> getConfigFlags() {
        return Collections.unmodifiableMap(configFlags);
    }
    
    // Getters
    
    public UUID getEffectId() {
        return effectId;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public String getAbilityId() {
        return abilityId;
    }
    
    public EffectType getType() {
        return type;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public long getSpawnTime() {
        return spawnTime;
    }
    
    public long getRemovalTime() {
        return removalTime;
    }
    
    public Object getEffectData() {
        return effectData;
    }
    
    /**
     * Get the lifetime of this effect in milliseconds
     */
    public long getLifetime() {
        return removalTime - spawnTime;
    }
    
    /**
     * Get the remaining time before this effect expires
     */
    public long getRemainingTime() {
        return Math.max(0, removalTime - System.currentTimeMillis());
    }
    
    /**
     * Get the age of this effect in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - spawnTime;
    }
    
    @Override
    public String toString() {
        return "EffectEntry{" +
                "effectId=" + effectId +
                ", ownerId=" + ownerId +
                ", type=" + type +
                ", location=" + location +
                ", age=" + getAge() + "ms" +
                ", remaining=" + getRemainingTime() + "ms" +
                '}';
    }
}
