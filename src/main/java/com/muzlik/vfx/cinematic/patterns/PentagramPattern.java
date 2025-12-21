package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.List;

/**
 * Pentagram inner pattern for dark/mystical magic circles.
 * 
 * Requirements: 4.2, 16.1
 */
public class PentagramPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public PentagramPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "pentagram";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        
        // Use 70% of available radius for inner pattern
        double patternRadius = radius * 0.7;
        
        // Generate pentagram with filled lines for higher ranks
        boolean filled = rank >= 3;
        return shapeRenderer.renderPentagram(center, patternRadius, rotation, filled);
    }
}
