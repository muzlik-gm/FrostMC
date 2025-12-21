package com.muzlik.util;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Utility class for creating potion effects with consistent settings.
 * 
 * This helper ensures all fragment abilities apply potion effects with hidden particles
 * to prevent vanilla Minecraft particle effects from obscuring custom VFX.
 */
public class PotionEffectHelper {
    
    /**
     * Creates a potion effect with hidden particles.
     * 
     * This method ensures that:
     * - Particles are hidden (particles = false) to avoid obscuring custom VFX
     * - Ambient effects are disabled (ambient = false) for cleaner visuals
     * - Icon is shown (icon = true) so players can see active effects in their inventory
     * 
     * @param type The type of potion effect (e.g., SPEED, SLOWNESS, STRENGTH)
     * @param duration Duration in ticks (20 ticks = 1 second)
     * @param amplifier Effect amplifier (0 = level I, 1 = level II, etc.)
     * @return A PotionEffect with hidden particles
     */
    public static PotionEffect createHiddenEffect(PotionEffectType type, int duration, int amplifier) {
        return new PotionEffect(
            type,
            duration,
            amplifier,
            false,  // ambient: false (not from beacon/conduit)
            false,  // particles: false (hide vanilla particles)
            true    // icon: true (show effect icon in inventory)
        );
    }
    
    /**
     * Creates a potion effect with hidden particles and custom icon visibility.
     * 
     * @param type The type of potion effect
     * @param duration Duration in ticks (20 ticks = 1 second)
     * @param amplifier Effect amplifier (0 = level I, 1 = level II, etc.)
     * @param showIcon Whether to show the effect icon in the player's inventory
     * @return A PotionEffect with hidden particles
     */
    public static PotionEffect createHiddenEffect(PotionEffectType type, int duration, int amplifier, boolean showIcon) {
        return new PotionEffect(
            type,
            duration,
            amplifier,
            false,      // ambient: false
            false,      // particles: false (hide vanilla particles)
            showIcon    // icon: configurable
        );
    }
}
