package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Void fragment pattern: Portal/Dimensional rift mandala.
 * 
 * Requirements: 7.3, 18.1-18.4
 */
public class VoidPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public VoidPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "void_portal";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple portal ring
            points.addAll(shapeRenderer.renderCircle(center, patternRadius, 24, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.6, 20, -rotation));
        } else if (rank <= 4) {
            // Doctor Strange mandala (hexagon + star)
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius, rotation));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.7, patternRadius * 0.4, 6, rotation));
        } else if (rank <= 6) {
            // Reality-tear cracks (complex star pattern)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.3, 12, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.5, 20, -rotation));
        } else {
            // Mirror dimension glimpse (triple mandala)
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius, rotation));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.7, rotation + Math.PI / 6));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.5, patternRadius * 0.25, 8, rotation));
        }
        
        return points;
    }
}
