package com.muzlik.vfx.cinematic;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Core component for generating geometric particle patterns.
 * Provides methods for rendering circles, pentagrams, hexagons, spirals, and stars.
 * 
 * Requirements: 8.1, 8.5
 */
public class ShapeRenderer {
    
    /**
     * Generate points for a circle pattern
     * 
     * @param center Center location
     * @param radius Circle radius
     * @param points Number of points on circumference
     * @param rotation Current rotation angle in radians
     * @return List of particle spawn locations
     */
    public List<Location> renderCircle(Location center, double radius, int points, double rotation) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        if (points < 3) {
            throw new IllegalArgumentException("Points must be at least 3");
        }
        
        List<Location> locations = new ArrayList<>();
        double angleStep = (2 * Math.PI) / points;
        
        for (int i = 0; i < points; i++) {
            double angle = (i * angleStep) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            locations.add(new Location(center.getWorld(), x, center.getY(), z));
        }
        
        return locations;
    }
    
    /**
     * Generate points for a pentagram pattern (5-pointed star)
     * 
     * @param center Center location
     * @param radius Outer radius
     * @param rotation Current rotation angle
     * @param filled Whether to include connecting lines
     * @return List of particle spawn locations
     */
    public List<Location> renderPentagram(Location center, double radius, double rotation, boolean filled) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        
        List<Location> locations = new ArrayList<>();
        
        // 5 vertices at 72° intervals
        Location[] vertices = new Location[5];
        for (int i = 0; i < 5; i++) {
            double angle = (i * 72 * Math.PI / 180) + rotation - (Math.PI / 2); // Start at top
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            vertices[i] = new Location(center.getWorld(), x, center.getY(), z);
        }
        
        // Connect every 2nd vertex to form pentagram
        for (int i = 0; i < 5; i++) {
            Location start = vertices[i];
            Location end = vertices[(i + 2) % 5]; // Connect to vertex 2 positions ahead
            
            // Add points along the line (reduced for crisp lines)
            int linePoints = filled ? 10 : 8;
            for (int j = 0; j < linePoints; j++) {
                double t = j / (double) (linePoints - 1);
                double x = start.getX() + (end.getX() - start.getX()) * t;
                double y = start.getY();
                double z = start.getZ() + (end.getZ() - start.getZ()) * t;
                locations.add(new Location(center.getWorld(), x, y, z));
            }
        }
        
        return locations;
    }
    
    /**
     * Generate points for a hexagon pattern
     * 
     * @param center Center location
     * @param radius Outer radius
     * @param rotation Current rotation angle
     * @return List of particle spawn locations
     */
    public List<Location> renderHexagon(Location center, double radius, double rotation) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        
        List<Location> locations = new ArrayList<>();
        
        // 6 vertices at 60° intervals
        Location[] vertices = new Location[6];
        for (int i = 0; i < 6; i++) {
            double angle = (i * 60 * Math.PI / 180) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            vertices[i] = new Location(center.getWorld(), x, center.getY(), z);
        }
        
        // Connect vertices with edges
        for (int i = 0; i < 6; i++) {
            Location start = vertices[i];
            Location end = vertices[(i + 1) % 6];
            
            // Add points along the edge (reduced for crisp lines)
            int edgePoints = 6;
            for (int j = 0; j < edgePoints; j++) {
                double t = j / (double) (edgePoints - 1);
                double x = start.getX() + (end.getX() - start.getX()) * t;
                double y = start.getY();
                double z = start.getZ() + (end.getZ() - start.getZ()) * t;
                locations.add(new Location(center.getWorld(), x, y, z));
            }
        }
        
        return locations;
    }
    
    /**
     * Generate points for a spiral pattern (Archimedean spiral)
     * 
     * @param center Center location
     * @param maxRadius Maximum spiral radius
     * @param turns Number of spiral turns
     * @param pointsPerTurn Points per revolution
     * @param rotation Current rotation offset
     * @return List of particle spawn locations
     */
    public List<Location> renderSpiral(Location center, double maxRadius, double turns, int pointsPerTurn, double rotation) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (maxRadius <= 0) {
            throw new IllegalArgumentException("Max radius must be positive");
        }
        if (turns <= 0) {
            throw new IllegalArgumentException("Turns must be positive");
        }
        if (pointsPerTurn < 1) {
            throw new IllegalArgumentException("Points per turn must be at least 1");
        }
        
        List<Location> locations = new ArrayList<>();
        int totalPoints = (int) (turns * pointsPerTurn);
        double b = maxRadius / (turns * 2 * Math.PI); // Spiral growth rate
        
        for (int i = 0; i < totalPoints; i++) {
            double theta = (i / (double) pointsPerTurn) * 2 * Math.PI;
            double r = b * theta; // Archimedean spiral: r = a + b*θ (a=0)
            double angle = theta + rotation;
            
            double x = center.getX() + r * Math.cos(angle);
            double z = center.getZ() + r * Math.sin(angle);
            locations.add(new Location(center.getWorld(), x, center.getY(), z));
        }
        
        return locations;
    }
    
    /**
     * Generate points for a star pattern
     * 
     * @param center Center location
     * @param outerRadius Outer point radius
     * @param innerRadius Inner point radius
     * @param points Number of star points
     * @param rotation Current rotation angle
     * @return List of particle spawn locations
     */
    public List<Location> renderStar(Location center, double outerRadius, double innerRadius, int points, double rotation) {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (outerRadius <= 0 || innerRadius <= 0) {
            throw new IllegalArgumentException("Radii must be positive");
        }
        if (innerRadius >= outerRadius) {
            throw new IllegalArgumentException("Inner radius must be less than outer radius");
        }
        if (points < 3) {
            throw new IllegalArgumentException("Points must be at least 3");
        }
        
        List<Location> locations = new ArrayList<>();
        int totalVertices = points * 2; // Alternating outer and inner points
        Location[] vertices = new Location[totalVertices];
        
        // Generate vertices alternating between outer and inner radius
        for (int i = 0; i < totalVertices; i++) {
            double angle = (i * Math.PI / points) + rotation - (Math.PI / 2); // Start at top
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            vertices[i] = new Location(center.getWorld(), x, center.getY(), z);
        }
        
        // Connect vertices with lines
        for (int i = 0; i < totalVertices; i++) {
            Location start = vertices[i];
            Location end = vertices[(i + 1) % totalVertices];
            
            // Add points along the line (reduced for crisp lines)
            int linePoints = 6;
            for (int j = 0; j < linePoints; j++) {
                double t = j / (double) (linePoints - 1);
                double x = start.getX() + (end.getX() - start.getX()) * t;
                double y = start.getY();
                double z = start.getZ() + (end.getZ() - start.getZ()) * t;
                locations.add(new Location(center.getWorld(), x, y, z));
            }
        }
        
        return locations;
    }
    
    /**
     * Apply transformation matrix to points
     * 
     * @param points Original points
     * @param transform Transformation matrix (rotation, scale, translation)
     * @return Transformed points
     */
    public List<Location> applyTransform(List<Location> points, TransformMatrix transform) {
        if (points == null || transform == null) {
            throw new IllegalArgumentException("Points and transform cannot be null");
        }
        
        if (points.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Use first point's location as origin for transformation
        Location origin = points.get(0);
        
        List<Location> transformed = new ArrayList<>();
        for (Location point : points) {
            transformed.add(transform.apply(point, origin));
        }
        
        return transformed;
    }
}
