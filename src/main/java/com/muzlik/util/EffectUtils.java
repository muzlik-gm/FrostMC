package com.muzlik.util;

import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Utility class for visual effects
 */
public class EffectUtils {

    /**
     * Spawn particles at a location
     * @param location The location to spawn particles
     * @param particle The particle type
     * @param count Number of particles
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param offsetZ Z offset
     */
    public static void spawnParticles(Location location, Particle particle, 
                                     int count, double offsetX, double offsetY, double offsetZ) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
    }

    /**
     * Spawn particles in a circle around a location
     * @param location Center location
     * @param particle Particle type
     * @param radius Radius of the circle
     * @param particles Number of particles to spawn
     */
    public static void spawnCircleParticles(Location location, Particle particle, 
                                           double radius, int particles) {
        double angleIncrement = 2 * Math.PI / particles;
        for (int i = 0; i < particles; i++) {
            double angle = i * angleIncrement;
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(location.getWorld(), x, location.getY(), z);
            location.getWorld().spawnParticle(particle, particleLocation, 1);
        }
    }
}


