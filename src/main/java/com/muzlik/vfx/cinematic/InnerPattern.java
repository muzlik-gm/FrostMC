package com.muzlik.vfx.cinematic;

import org.bukkit.Location;

import java.util.List;

/**
 * Interface for magic circle inner patterns.
 * Each fragment type has a unique inner pattern.
 * 
 * Requirements: 4.2, 4.4
 */
public interface InnerPattern {
    
    /**
     * Get pattern type identifier
     * 
     * @return Pattern ID string
     */
    String getPatternId();
    
    /**
     * Generate particle locations for this pattern
     * 
     * @param center Center location
     * @param radius Available radius for the pattern
     * @param rotation Current rotation angle
     * @param rank Player rank for complexity scaling (1-9+)
     * @return List of particle spawn locations
     */
    List<Location> generatePoints(Location center, double radius, double rotation, int rank);
}
