package com.muzlik.vfx.cinematic;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Transformation matrix for rotating, scaling, and translating particle positions.
 * Used for geometric transformations in the cinematic VFX system.
 * 
 * Requirements: 8.5
 */
public class TransformMatrix {
    private final double rotationX;
    private final double rotationY;
    private final double rotationZ;
    private final double scaleX;
    private final double scaleY;
    private final double scaleZ;
    private final double translateX;
    private final double translateY;
    private final double translateZ;
    
    private TransformMatrix(Builder builder) {
        this.rotationX = builder.rotationX;
        this.rotationY = builder.rotationY;
        this.rotationZ = builder.rotationZ;
        this.scaleX = builder.scaleX;
        this.scaleY = builder.scaleY;
        this.scaleZ = builder.scaleZ;
        this.translateX = builder.translateX;
        this.translateY = builder.translateY;
        this.translateZ = builder.translateZ;
    }
    
    /**
     * Apply transformation to a location relative to an origin point
     * 
     * @param location Original location
     * @param origin Transform origin point
     * @return Transformed location
     */
    public Location apply(Location location, Location origin) {
        if (location == null || origin == null) {
            throw new IllegalArgumentException("Location and origin cannot be null");
        }
        
        // Get relative position
        double x = location.getX() - origin.getX();
        double y = location.getY() - origin.getY();
        double z = location.getZ() - origin.getZ();
        
        // Apply scale
        x *= scaleX;
        y *= scaleY;
        z *= scaleZ;
        
        // Apply rotation around X axis
        if (rotationX != 0) {
            double cos = Math.cos(rotationX);
            double sin = Math.sin(rotationX);
            double newY = y * cos - z * sin;
            double newZ = y * sin + z * cos;
            y = newY;
            z = newZ;
        }
        
        // Apply rotation around Y axis
        if (rotationY != 0) {
            double cos = Math.cos(rotationY);
            double sin = Math.sin(rotationY);
            double newX = x * cos + z * sin;
            double newZ = -x * sin + z * cos;
            x = newX;
            z = newZ;
        }
        
        // Apply rotation around Z axis
        if (rotationZ != 0) {
            double cos = Math.cos(rotationZ);
            double sin = Math.sin(rotationZ);
            double newX = x * cos - y * sin;
            double newY = x * sin + y * cos;
            x = newX;
            y = newY;
        }
        
        // Apply translation and return to world coordinates
        return new Location(
            location.getWorld(),
            origin.getX() + x + translateX,
            origin.getY() + y + translateY,
            origin.getZ() + z + translateZ
        );
    }
    
    /**
     * Builder for TransformMatrix
     */
    public static class Builder {
        private double rotationX = 0;
        private double rotationY = 0;
        private double rotationZ = 0;
        private double scaleX = 1.0;
        private double scaleY = 1.0;
        private double scaleZ = 1.0;
        private double translateX = 0;
        private double translateY = 0;
        private double translateZ = 0;
        
        public Builder rotateX(double radians) {
            this.rotationX = radians;
            return this;
        }
        
        public Builder rotateY(double radians) {
            this.rotationY = radians;
            return this;
        }
        
        public Builder rotateZ(double radians) {
            this.rotationZ = radians;
            return this;
        }
        
        public Builder scale(double scale) {
            this.scaleX = scale;
            this.scaleY = scale;
            this.scaleZ = scale;
            return this;
        }
        
        public Builder scale(double x, double y, double z) {
            this.scaleX = x;
            this.scaleY = y;
            this.scaleZ = z;
            return this;
        }
        
        public Builder translate(double x, double y, double z) {
            this.translateX = x;
            this.translateY = y;
            this.translateZ = z;
            return this;
        }
        
        public TransformMatrix build() {
            return new TransformMatrix(this);
        }
    }
}
