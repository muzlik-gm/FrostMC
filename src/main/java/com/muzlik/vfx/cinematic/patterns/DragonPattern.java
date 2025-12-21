package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Dragon fragment pattern: Dragon eye/Scale pattern.
 * 
 * Requirements: 7.5, 21.1-21.4
 */
public class DragonPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public DragonPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "dragon_eye";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple scale pattern (hexagons)
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius, rotation));
        } else if (rank <= 4) {
            // Dragon eye with slit pupil (ellipse approximation)
            points.addAll(shapeRenderer.renderCircle(center, patternRadius, 32, rotation));
            // Vertical slit
            for (int i = 0; i < 10; i++) {
                double y = center.getY() + (i - 5) * 0.1;
                points.add(new Location(center.getWorld(), center.getX(), y, center.getZ()));
            }
        } else if (rank <= 6) {
            // Full dragon head (complex star + hexagon)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.4, 8, rotation));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.6, rotation));
        } else {
            // Elder dragon with wings spread (triple star)
            points.addAll(shapeRenderer.renderStar(center, patternRadius, patternRadius * 0.3, 12, rotation));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.8, patternRadius * 0.5, 8, rotation + Math.PI / 8));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.4, rotation));
        }
        
        return points;
    }
}
