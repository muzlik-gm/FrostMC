package com.muzlik.vfx.animation;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Wave particle pattern implementation
 * 
 * Requirements: 6.3
 */
public class WavePattern implements ParticlePatternInterface {
    private final double amplitude;
    private final double frequency;
    private final double length;
    private final int points;
    
    public WavePattern(double amplitude, double frequency, double length, int points) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.length = length;
        this.points = points;
    }
    
    @Override
    public List<Location> generatePoints(Location origin, int tick, int maxTicks) {
        List<Location> pointList = new ArrayList<>();
        double timeOffset = tick * 0.1;
        
        Vector direction = origin.getDirection();
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double x = progress * length;
            double waveOffset = Math.sin((progress * frequency * Math.PI * 2) + timeOffset) * amplitude;
            
            Vector offset = direction.clone().multiply(x);
            Vector wave = perpendicular.clone().multiply(waveOffset);
            
            Location point = origin.clone().add(offset).add(wave);
            pointList.add(point);
        }
        
        return pointList;
    }
}
