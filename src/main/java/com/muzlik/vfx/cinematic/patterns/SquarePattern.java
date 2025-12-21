package com.muzlik.vfx.cinematic.patterns;

import com.muzlik.vfx.cinematic.InnerPattern;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple square inner pattern for basic rank magic circles.
 * 
 * Requirements: 4.2, 10.1
 */
public class SquarePattern implements InnerPattern {
    
    @Override
    public String getPatternId() {
        return "square";
    }
    
    @Override
    public List<Location> generatePoints(Location center, double radius, double rotation, int rank) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        
        List<Location> points = new ArrayList<>();
        
        // Use 60% of available radius for inner pattern
        double patternRadius = radius * 0.6;
        
        // Generate 4 vertices at 90° intervals
        Location[] vertices = new Location[4];
        for (int i = 0; i < 4; i++) {
            double angle = (i * 90 * Math.PI / 180) + rotation + (Math.PI / 4); // Rotate 45° to make it a diamond
            double x = center.getX() + patternRadius * Math.cos(angle);
            double z = center.getZ() + patternRadius * Math.sin(angle);
            vertices[i] = new Location(center.getWorld(), x, center.getY(), z);
        }
        
        // Connect vertices with edges
        for (int i = 0; i < 4; i++) {
            Location start = vertices[i];
            Location end = vertices[(i + 1) % 4];
            
            // Add points along the edge
            int edgePoints = 8;
            for (int j = 0; j < edgePoints; j++) {
                double t = j / (double) (edgePoints - 1);
                double x = start.getX() + (end.getX() - start.getX()) * t;
                double y = start.getY();
                double z = start.getZ() + (end.getZ() - start.getZ()) * t;
                points.add(new Location(center.getWorld(), x, y, z));
            }
        }
        
        return points;
    }
}
