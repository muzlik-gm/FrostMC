package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Fire fragment pattern: Phoenix/Sun with radiating flames.
 * Complexity scales with rank from simple flame ring to animated phoenix.
 * 
 * Requirements: 7.1, 12.1-12.5
 */
public class FirePattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public FirePattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "fire_phoenix";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // R1-2: Simple flame ring with inner circle
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.6, 24, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.3, 16, -rotation * 0.5));
        } else if (rank <= 4) {
            // R3-4: Phoenix feather pattern (8-pointed star + circles)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.5, 8, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.7, 32, -rotation * 0.7));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.4, 20, rotation * 1.2));
        } else if (rank <= 6) {
            // R5-6: Sun-core with corona (12-pointed star + multiple circles + hexagon)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.6, 12, rotation));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.8, -rotation * 0.6));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.5, 24, rotation * 1.5));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.3, 16, -rotation * 2.0));
            // Add small triangular flames
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.2, patternRadius * 0.1, 6, rotation * 3.0));
        } else {
            // R7+: Complex phoenix mandala (multiple overlapping shapes)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.4, 16, rotation));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.85, patternRadius * 0.55, 12, -rotation * 0.8));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.7, rotation * 0.5));
            points.addAll(shapeRenderer.renderPentagram(center, patternRadius * 0.6, rotation * 1.3, true));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.45, 32, -rotation * 1.8));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.25, 20, rotation * 2.5));
            // Inner core
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.15, patternRadius * 0.08, 8, -rotation * 4.0));
        }
        
        return points;
    }
}
