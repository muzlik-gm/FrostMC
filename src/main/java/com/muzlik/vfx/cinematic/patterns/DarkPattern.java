package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import com.muzlik.vfx.cinematic.ShapeRenderer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Dark fragment pattern: Eclipse/Pentagram with shadow tendrils.
 * 
 * Requirements: 7.5, 16.1-16.5
 */
public class DarkPattern implements InnerPattern {
    
    private final ShapeRenderer shapeRenderer;
    
    public DarkPattern() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    @Override
    public String getPatternId() {
        return "dark_eclipse";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        List<Location> points = new ArrayList<>();
        double patternRadius = radius * 0.7;
        
        if (rank <= 2) {
            // Simple crescent moon with inner circle
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.6, 24, rotation));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.3, 16, -rotation));
        } else if (rank <= 4) {
            // Eclipse with corona and pentagram
            points.addAll(shapeRenderer.renderCircle(center, patternRadius, 32, rotation));
            points.addAll(shapeRenderer.renderPentagram(center, patternRadius * 0.7, -rotation * 0.8, false));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.4, 20, rotation * 1.5));
        } else if (rank <= 6) {
            // Complex pentagram with multiple layers
            points.addAll(shapeRenderer.renderPentagram(center, patternRadius, rotation, true));
            points.addAll(shapeRenderer.renderPentagram(center, patternRadius * 0.6, -rotation * 1.2, false));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.8, 40, -rotation * 0.7));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.3, 24, rotation * 2.0));
        } else {
            // Eldritch mandala (pentagrams + circles + spirals + hexagons)
            points.addAll(shapeRenderer.renderPentagram(center, patternRadius, rotation, true));
            points.addAll(shapeRenderer.renderPentagram(center, patternRadius * 0.7, -rotation * 1.3, true));
            points.addAll(shapeRenderer.renderHexagon(center, patternRadius * 0.85, rotation * 0.6));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.55, 32, -rotation * 1.8));
            points.addAll(shapeRenderer.renderStar(center, patternRadius * 0.4, patternRadius * 0.25, 8, rotation * 2.5));
            points.addAll(shapeRenderer.renderCircle(center, patternRadius * 0.15, 16, -rotation * 3.5));
            // Add spiral tendrils
            points.addAll(shapeRenderer.renderSpiral(center, patternRadius * 0.5, 1.5, 20, rotation * 2.0));
        }
        
        return points;
    }
}
