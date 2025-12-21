package com.muzlik.vfx.cinematic;

import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.cinematic.patterns.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for fragment-specific inner patterns.
 * Maps each fragment type to its unique magic circle inner pattern.
 * 
 * Requirements: 4.4, 7.1-7.5
 */
public class FragmentPatternRegistry {
    
    private final Map<FragmentType, InnerPattern> patterns;
    
    public FragmentPatternRegistry() {
        this.patterns = new HashMap<>();
        registerDefaultPatterns();
    }
    
    /**
     * Register default patterns for all fragment types
     */
    private void registerDefaultPatterns() {
        // Fire: Phoenix/Sun pattern
        patterns.put(FragmentType.FIRE, new FirePattern());
        
        // Water: Wave/Koi fish pattern
        patterns.put(FragmentType.WATER, new WaterPattern());
        
        // Air: Cyclone/Feather pattern
        patterns.put(FragmentType.AIR, new AirPattern());
        
        // Dark: Eclipse/Pentagram pattern
        patterns.put(FragmentType.DARK, new DarkPattern());
        
        // Light: Sun/Halo pattern
        patterns.put(FragmentType.LIGHT, new LightPattern());
        
        // Void: Portal/Mandala pattern
        patterns.put(FragmentType.VOID, new VoidPattern());
        
        // Storm: Lightning/Thunder pattern
        patterns.put(FragmentType.STORM, new StormPattern());
        
        // Dragon: Dragon eye/Scale pattern
        patterns.put(FragmentType.DRAGON, new DragonPattern());
    }
    
    /**
     * Get the inner pattern for a fragment type
     * 
     * @param fragmentType The fragment type
     * @return The associated inner pattern, or a default pattern if not found
     */
    public InnerPattern getPattern(FragmentType fragmentType) {
        return patterns.getOrDefault(fragmentType, new TrianglePattern());
    }
    
    /**
     * Register a custom pattern for a fragment type
     * 
     * @param fragmentType The fragment type
     * @param pattern The pattern to register
     */
    public void registerPattern(FragmentType fragmentType, InnerPattern pattern) {
        if (fragmentType == null || pattern == null) {
            throw new IllegalArgumentException("Fragment type and pattern cannot be null");
        }
        patterns.put(fragmentType, pattern);
    }
}
