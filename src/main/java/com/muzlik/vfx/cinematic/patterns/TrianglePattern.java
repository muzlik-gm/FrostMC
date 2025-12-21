package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple triangle inner pattern for basic rank magic circles.
 * 
 * Requirements: 4.2, 10.1
 */
public class TrianglePattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public TrianglePattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "triangle";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        
        List<Location> points = new ArrayList<>();
        
        // Use 60% of available radius for inner pattern
        double patternRadius = radius * 0.6;
        
        // Generate triangle (3-pointed star with equal inner/outer radius)
        points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.5, 3, rotation));
        
        return points;
    }
}
