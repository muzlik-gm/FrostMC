package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Mathematical patterns for particle rendering.
 * 
 * Each pattern defines how particles are distributed in 3D space.
 * Patterns use mathematical functions for consistency and predictability.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4
 */
public enum ParticlePattern {
    /**
     * Single point - all particles at one location
     */
    POINT {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed, data);
        }
    },
    
    /**
     * Spherical distribution - particles distributed on sphere surface
     */
    SPHERE {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            double radius = Math.max(offsetX, Math.max(offsetY, offsetZ));
            
            for (int i = 0; i < count; i++) {
                double theta = Math.random() * 2 * Math.PI;
                double phi = Math.acos(2 * Math.random() - 1);
                
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.sin(phi) * Math.sin(theta);
                double z = radius * Math.cos(phi);
                
                Location particleLoc = loc.clone().add(x, y, z);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    },
    
    /**
     * Circular ring - particles in a horizontal circle
     */
    RING {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            double radius = Math.max(offsetX, offsetZ);
            
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                
                Location particleLoc = loc.clone().add(x, offsetY, z);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    },
    
    /**
     * Spiral motion - particles in a spiral pattern
     */
    SPIRAL {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            double radius = Math.max(offsetX, offsetZ);
            double height = offsetY;
            
            for (int i = 0; i < count; i++) {
                double progress = (double) i / count;
                double angle = progress * 4 * Math.PI; // 2 full rotations
                double y = height * progress;
                double r = radius * (1 - progress * 0.5); // Shrinking radius
                
                double x = r * Math.cos(angle);
                double z = r * Math.sin(angle);
                
                Location particleLoc = loc.clone().add(x, y, z);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    },
    
    /**
     * Linear trail - particles in a straight line
     */
    LINE {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            Vector direction = new Vector(offsetX, offsetY, offsetZ).normalize();
            double length = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
            
            for (int i = 0; i < count; i++) {
                double progress = (double) i / count;
                Vector offset = direction.clone().multiply(length * progress);
                
                Location particleLoc = loc.clone().add(offset);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    },
    
    /**
     * Cone shape - particles in a cone
     */
    CONE {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            double height = offsetY;
            double baseRadius = Math.max(offsetX, offsetZ);
            
            for (int i = 0; i < count; i++) {
                double progress = Math.random();
                double angle = Math.random() * 2 * Math.PI;
                double radius = baseRadius * progress;
                double y = height * progress;
                
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                
                Location particleLoc = loc.clone().add(x, y, z);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    },
    
    /**
     * Radial explosion - particles bursting outward
     */
    BURST {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            for (int i = 0; i < count; i++) {
                double theta = Math.random() * 2 * Math.PI;
                double phi = Math.acos(2 * Math.random() - 1);
                
                double x = offsetX * Math.sin(phi) * Math.cos(theta);
                double y = offsetY * Math.sin(phi) * Math.sin(theta);
                double z = offsetZ * Math.cos(phi);
                
                Location particleLoc = loc.clone().add(x, y, z);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    },
    
    /**
     * Circle pattern - alias for RING
     */
    CIRCLE {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            RING.render(loc, particle, count, offsetX, offsetY, offsetZ, speed, data);
        }
    },
    
    /**
     * Linear pattern - alias for LINE
     */
    LINEAR {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            LINE.render(loc, particle, count, offsetX, offsetY, offsetZ, speed, data);
        }
    },
    
    /**
     * Wave pattern - sinusoidal motion
     */
    WAVE {
        @Override
        public void render(Location loc, Particle particle, int count, 
                         double offsetX, double offsetY, double offsetZ, 
                         double speed, Object data) {
            double length = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
            
            for (int i = 0; i < count; i++) {
                double progress = (double) i / count;
                double angle = progress * 2 * Math.PI;
                
                double x = offsetX * progress;
                double y = offsetY * Math.sin(angle * 3); // 3 waves
                double z = offsetZ * progress;
                
                Location particleLoc = loc.clone().add(x, y, z);
                loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, speed, data);
            }
        }
    };
    
    /**
     * Render particles using this pattern
     * 
     * @param loc Center location
     * @param particle Particle type
     * @param count Number of particles
     * @param offsetX X offset/radius
     * @param offsetY Y offset/height
     * @param offsetZ Z offset/radius
     * @param speed Particle speed
     * @param data Particle data (for colored particles, block data, etc.)
     */
    public abstract void render(Location loc, Particle particle, int count, 
                                double offsetX, double offsetY, double offsetZ, 
                                double speed, Object data);
}
