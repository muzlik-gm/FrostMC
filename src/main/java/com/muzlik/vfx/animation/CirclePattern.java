package com.muzlik.vfx.animation;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Circular particle pattern implementation
 * 
 * Requirements: 6.2
 */
public class CirclePattern implements ParticlePatternInterface {
    private final double radius;
    private final int points;
    private final boolean rotate;
    
    public CirclePattern(double radius, int points, boolean rotate) {
        this.radius = radius;
        this.points = points;
        this.rotate = rotate;
    }
    
    @Override
    public List<Location> generatePoints(Location origin, int tick, int maxTicks) {
        List<Location> pointList = new ArrayList<>();
        double rotationOffset = rotate ? (tick * 0.1) : 0;
        
        for (int i = 0; i < points; i++) {
            double angle = (i * Math.PI * 2 / points) + rotationOffset;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location point = origin.clone().add(x, 0, z);
            pointList.add(point);
        }
        
        return pointList;
    }
}
