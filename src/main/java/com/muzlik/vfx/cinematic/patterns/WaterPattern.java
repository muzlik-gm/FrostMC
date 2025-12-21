package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Water fragment pattern: Wave/Koi fish spiral.
 * 
 * Requirements: 7.2, 13.1-13.4
 */
public class WaterPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public WaterPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "water_wave";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple ripple rings
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.4, 16, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.7, 20, rotation));
        } else if (rank <= 4) {
            // Triple wave with spiral
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius, 2, 20, rotation));
        } else if (rank <= 6) {
            // Coral reef pattern (hexagons)
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius, rotation));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.5, rotation + Math.PI / 6));
        } else {
            // Ocean god trident
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.3, 3, rotation));
        }
        
        return points;
    }
}
