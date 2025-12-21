package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Air fragment pattern: Cyclone/Feather spiral.
 * 
 * Requirements: 7.3, 14.1-14.5
 */
public class AirPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public AirPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "air_cyclone";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple wind swirl
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius, 1.5, 15, rotation));
        } else if (rank <= 4) {
            // Feather spiral pattern
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius, 2.5, 20, rotation));
        } else if (rank <= 6) {
            // Cyclone with debris (double spiral)
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius, 3, 25, rotation));
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius * 0.6, 2, 15, -rotation));
        } else {
            // Air elemental face (complex pattern)
            points.addAll(shapeRenderer.renderCircle(center, patternRadius, 32, rotation));
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius * 0.8, 3, 30, rotation));
        }
        
        return points;
    }
}
