package com.muzlik.vfx.animation;

import org.bukkit.Location;

import java.util.List;

/**
 * Interface for particle pattern generators
 * 
 * Requirements: 6.1, 6.3
 */
public interface ParticlePatternInterface {
    /**
     * Generate particle points for this pattern
     * 
     * @param origin Origin location
     * @param tick Current tick in the animation
     * @param maxTicks Maximum ticks for the animation
     * @return List of locations where particles should be spawned
     */
    List<Location> generatePoints(Location origin, int tick, int maxTicks);
}
