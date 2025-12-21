package com.muzlik.ritual.structure;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Interface for ritual structures
 */
public interface RitualStructure {
    /**
     * Spawn the structure
     */
    void spawn();
    
    /**
     * Despawn the structure and restore original blocks
     */
    void despawn();
    
    /**
     * Check if a location is protected by this structure
     */
    boolean isProtected(Location location);
    
    /**
     * Get the ritual ID
     */
    UUID getRitualId();
    
    /**
     * Get the center location
     */
    Location getCenter();
}
