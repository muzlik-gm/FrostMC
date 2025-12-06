package com.muzlik.vfx.animation;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for generating procedural animation paths.
 * Provides methods for creating spiral, circular, wave, and other patterns.
 * 
 * Requirements: 6.1, 6.2, 6.3
 */
public class AnimationController {
    
    /**
     * Generate a spiral path
     * 
     * @param center Center location
     * @param radius Radius of the spiral
     * @param height Total height of the spiral
     * @param rotations Number of rotations
     * @param points Number of points to generate
     * @return List of locations forming a spiral
     */
    public List<Location> generateSpiralPath(Location center, double radius, double height, 
                                             int rotations, int points) {
        List<Location> path = new ArrayList<>();
        
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double angle = progress * rotations * Math.PI * 2;
            double currentRadius = radius * (1 - progress * 0.5); // Spiral inward
            double y = progress * height;
            
            double x = Math.cos(angle) * currentRadius;
            double z = Math.sin(angle) * currentRadius;
            
            Location point = center.clone().add(x, y, z);
            path.add(point);
        }
        
        return path;
    }
    
    /**
     * Generate a circular path
     * 
     * @param center Center location
     * @param radius Radius of the circle
     * @param points Number of points
     * @return List of locations forming a circle
     */
    public List<Location> generateCirclePath(Location center, double radius, int points) {
        List<Location> path = new ArrayList<>();
        
        for (int i = 0; i < points; i++) {
            double angle = (double) i / points * Math.PI * 2;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location point = center.clone().add(x, 0, z);
            path.add(point);
        }
        
        return path;
    }
    
    /**
     * Generate a wave path
     * 
     * @param start Start location
     * @param end End location
     * @param amplitude Wave amplitude
     * @param frequency Wave frequency
     * @param points Number of points
     * @return List of locations forming a wave
     */
    public List<Location> generateWavePath(Location start, Location end, double amplitude, 
                                           double frequency, int points) {
        List<Location> path = new ArrayList<>();
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();
        
        // Get perpendicular vector for wave motion
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double waveOffset = Math.sin(progress * frequency * Math.PI * 2) * amplitude;
            
            Vector offset = direction.clone().multiply(progress * distance);
            Vector waveVector = perpendicular.clone().multiply(waveOffset);
            
            Location point = start.clone().add(offset).add(waveVector);
            path.add(point);
        }
        
        return path;
    }
    
    /**
     * Generate a helix path
     * 
     * @param center Center location
     * @param radius Radius of the helix
     * @param height Total height
     * @param rotations Number of rotations
     * @param points Number of points
     * @return List of locations forming a helix
     */
    public List<Location> generateHelixPath(Location center, double radius, double height, 
                                            int rotations, int points) {
        List<Location> path = new ArrayList<>();
        
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double angle = progress * rotations * Math.PI * 2;
            double y = progress * height;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location point = center.clone().add(x, y, z);
            path.add(point);
        }
        
        return path;
    }
    
    /**
     * Ease-in-out function
     * Smooth acceleration and deceleration
     * 
     * @param t Progress (0.0 to 1.0)
     * @return Eased value (0.0 to 1.0)
     */
    public double easeInOut(double t) {
        if (t < 0.5) {
            return 2 * t * t;
        } else {
            return 1 - Math.pow(-2 * t + 2, 2) / 2;
        }
    }
    
    /**
     * Ease-in function
     * Slow start, fast end
     * 
     * @param t Progress (0.0 to 1.0)
     * @return Eased value (0.0 to 1.0)
     */
    public double easeIn(double t) {
        return t * t;
    }
    
    /**
     * Ease-out function
     * Fast start, slow end
     * 
     * @param t Progress (0.0 to 1.0)
     * @return Eased value (0.0 to 1.0)
     */
    public double easeOut(double t) {
        return 1 - Math.pow(1 - t, 2);
    }
    
    /**
     * Bounce function
     * 
     * @param t Progress (0.0 to 1.0)
     * @return Bounced value
     */
    public double bounce(double t) {
        if (t < 0.5) {
            return Math.abs(Math.sin(t * Math.PI * 4)) * (1 - t);
        } else {
            return Math.abs(Math.sin(t * Math.PI * 4)) * t;
        }
    }
    
    /**
     * Calculate fade multiplier for particle lifetime
     * Fades over the final 20% of lifetime
     * 
     * @param currentTick Current tick
     * @param maxTicks Maximum ticks
     * @return Fade multiplier (0.0 to 1.0)
     */
    public double calculateFade(int currentTick, int maxTicks) {
        double progress = (double) currentTick / maxTicks;
        
        if (progress < 0.8) {
            return 1.0; // Full intensity
        } else {
            // Fade over final 20%
            double fadeProgress = (progress - 0.8) / 0.2;
            return 1.0 - fadeProgress;
        }
    }
}
