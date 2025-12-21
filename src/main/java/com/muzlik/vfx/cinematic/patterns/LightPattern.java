package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Light fragment pattern: Sun/Halo with rays.
 * 
 * Requirements: 7.4, 17.1-17.5
 */
public class LightPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public LightPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "light_halo";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple sun rays (8-pointed star)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.4, 8, rotation));
        } else if (rank <= 4) {
            // Halo with cross (12-pointed star + circle)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.5, 12, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.3, 16, rotation));
        } else if (rank <= 6) {
            // Angel wing silhouette (complex star)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.3, 16, rotation));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.7, patternRadius * 0.4, 8, rotation + Math.PI / 8));
        } else {
            // Seraph six-wing formation (triple star)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.4, 16, rotation));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.8, patternRadius * 0.5, 12, rotation + Math.PI / 12));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.6, patternRadius * 0.3, 8, rotation + Math.PI / 8));
        }
        
        return points;
    }
}
