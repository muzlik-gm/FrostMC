package com.muzlik.fx;

import org.bukkit.Color;
import org.bukkit.Particle;

/**
 * Represents a color scheme for Fragment visual effects.
 * Contains primary, secondary, and accent colors plus particle type.
 */
public class ColorScheme {
    private final Color primary;
    private final Color secondary;
    private final Color accent;
    private final Particle particleType;

    public ColorScheme(Color primary, Color secondary, Color accent, Particle particleType) {
        if (primary == null || secondary == null || accent == null || particleType == null) {
            throw new IllegalArgumentException("ColorScheme components cannot be null");
        }
        this.primary = primary;
        this.secondary = secondary;
        this.accent = accent;
        this.particleType = particleType;
    }

    public Color getPrimary() {
        return primary;
    }

    public Color getSecondary() {
        return secondary;
    }

    public Color getAccent() {
        return accent;
    }

    public Particle getParticleType() {
        return particleType;
    }

    /**
     * Create a Particle.DustOptions from a color
     */
    public Particle.DustOptions createDustOptions(Color color, float size) {
        return new Particle.DustOptions(color, size);
    }

    /**
     * Get primary color as DustOptions
     */
    public Particle.DustOptions getPrimaryDust(float size) {
        return createDustOptions(primary, size);
    }

    /**
     * Get secondary color as DustOptions
     */
    public Particle.DustOptions getSecondaryDust(float size) {
        return createDustOptions(secondary, size);
    }

    /**
     * Get accent color as DustOptions
     */
    public Particle.DustOptions getAccentDust(float size) {
        return createDustOptions(accent, size);
    }
}
