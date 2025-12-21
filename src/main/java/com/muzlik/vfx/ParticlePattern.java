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
            safeSpawnParticle(loc, particle, count, offsetX, offsetY, offsetZ, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
                safeSpawnParticle(particleLoc, particle, 1, 0, 0, 0, speed, data);
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
    
    /**
     * Helper method to safely spawn particles with proper data handling
     * Handles all particles that require specific data types
     */
    protected static void safeSpawnParticle(Location loc, Particle particle, int count,
                                         double offsetX, double offsetY, double offsetZ,
                                         double speed, Object data) {
        if (loc == null || loc.getWorld() == null) return;
        
        try {
            // Handle particles that require specific data
            if (data == null) {
                // REDSTONE/DUST requires DustOptions
                if (particle == Particle.REDSTONE || particle.name().equals("DUST")) {
                    data = new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f);
                }
                // DUST_COLOR_TRANSITION requires DustTransition
                else if (particle.name().equals("DUST_COLOR_TRANSITION")) {
                    data = new org.bukkit.Particle.DustTransition(
                        org.bukkit.Color.RED, org.bukkit.Color.ORANGE, 1.0f);
                }
                // BLOCK_CRACK, BLOCK_DUST, FALLING_DUST require BlockData
                else if (particle == Particle.BLOCK_CRACK || 
                         particle == Particle.BLOCK_DUST || 
                         particle == Particle.FALLING_DUST) {
                    data = org.bukkit.Material.STONE.createBlockData();
                }
                // ITEM_CRACK requires ItemStack
                else if (particle == Particle.ITEM_CRACK) {
                    data = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE);
                }
            }
            
            // Spawn with or without data
            if (data != null) {
                loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed, data);
            } else {
                // For particles that don't need data, use the simpler method
                loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed);
            }
        } catch (Exception e) {
            // Fallback: try spawning without data, or skip if that fails too
            try {
                loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed);
            } catch (Exception ignored) {
                // Particle spawn failed, skip silently
            }
        }
    }
}
