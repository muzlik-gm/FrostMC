package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Storm fragment pattern: Lightning bolt/Thunder cloud.
 * 
 * Requirements: 7.5, 19.1-19.4
 */
public class StormPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public StormPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "storm_lightning";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple bolt pattern (zigzag star)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.3, 4, rotation));
        } else if (rank <= 4) {
            // Branching lightning web (8-pointed star)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.4, 8, rotation));
        } else if (rank <= 6) {
            // Storm cloud vortex (spiral + circle)
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius, 2, 20, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius, 24, rotation));
        } else {
            // Thor hammer symbol (complex cross pattern)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.2, 4, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.4, 16, rotation));
        }
        
        return points;
    }
}
