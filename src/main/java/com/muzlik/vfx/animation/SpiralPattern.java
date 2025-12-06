package com.muzlik.vfx.animation;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Spiral particle pattern implementation
 * 
 * Requirements: 6.1
 */
public class SpiralPattern implements ParticlePatternInterface {
    private final double radius;
    private final double height;
    private final int rotations;
    private final int pointsPerRotation;
    
    public SpiralPattern(double radius, double height, int rotations, int pointsPerRotation) {
        this.radius = radius;
        this.height = height;
        this.rotations = rotations;
        this.pointsPerRotation = pointsPerRotation;
    }
    
    @Override
    public List<Location> generatePoints(Location origin, int tick, int maxTicks) {
        List<Location> points = new ArrayList<>();
        double progress = (double) tick / maxTicks;
        
        int totalPoints = rotations * pointsPerRotation;
        int currentPoints = (int) (totalPoints * progress);
        
        for (int i = 0; i <= currentPoints; i++) {
            double pointProgress = (double) i / totalPoints;
            double angle = pointProgress * rotations * Math.PI * 2;
            double currentRadius = radius * (1 - pointProgress * 0.3);
            double y = pointProgress * height;
            
            double x = Math.cos(angle) * currentRadius;
            double z = Math.sin(angle) * currentRadius;
            
            Location point = origin.clone().add(x, y, z);
            points.add(point);
        }
        
        return points;
    }
}
