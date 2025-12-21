package com.muzlik.fx;

import com.muzlik.fragment.FragmentType;
import com.muzlik.ritual.RitualStage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized library for all visual and audio effects.
 * Provides anime-inspired particle effects and sounds.
 */
public class FXLibrary {
    private final JavaPlugin plugin;
    private final Map<FragmentType, ColorScheme> colorSchemes;
    private boolean debugMode;
    private double particleDensityMultiplier;

    public FXLibrary(JavaPlugin plugin) {
        this.plugin = plugin;
        this.colorSchemes = new HashMap<>();
        this.debugMode = false;
        this.particleDensityMultiplier = 1.0;
        
        initializeColorSchemes();
    }

    /**
     * Initialize color schemes for all Fragment types
     */
    private void initializeColorSchemes() {
        // FIRE: Orange/Red/Gold
        colorSchemes.put(FragmentType.FIRE, new ColorScheme(
            Color.fromRGB(255, 69, 0),    // Primary: OrangeRed
            Color.fromRGB(255, 140, 0),   // Secondary: DarkOrange
            Color.fromRGB(255, 215, 0),   // Accent: Gold
            Particle.FLAME
        ));

        // WATER: Blue/Cyan/LightBlue
        colorSchemes.put(FragmentType.WATER, new ColorScheme(
            Color.fromRGB(30, 144, 255),  // Primary: DodgerBlue
            Color.fromRGB(0, 206, 209),   // Secondary: DarkTurquoise
            Color.fromRGB(135, 206, 235), // Accent: SkyBlue
            Particle.WATER_DROP
        ));

        // AIR: White/Cyan/LightGray
        colorSchemes.put(FragmentType.AIR, new ColorScheme(
            Color.fromRGB(240, 248, 255), // Primary: AliceBlue
            Color.fromRGB(224, 255, 255), // Secondary: LightCyan
            Color.fromRGB(255, 255, 255), // Accent: White
            Particle.CLOUD
        ));

        // DARK: Purple/DarkPurple/Violet
        colorSchemes.put(FragmentType.DARK, new ColorScheme(
            Color.fromRGB(75, 0, 130),    // Primary: Indigo
            Color.fromRGB(139, 0, 139),   // Secondary: DarkMagenta
            Color.fromRGB(148, 0, 211),   // Accent: DarkViolet
            Particle.SMOKE_LARGE
        ));

        // LIGHT: Yellow/LightYellow/White
        colorSchemes.put(FragmentType.LIGHT, new ColorScheme(
            Color.fromRGB(255, 255, 224), // Primary: LightYellow
            Color.fromRGB(255, 250, 205), // Secondary: LemonChiffon
            Color.fromRGB(255, 255, 255), // Accent: White
            Particle.END_ROD
        ));

        // VOID: DarkSlateGray/SlateBlue/MediumPurple
        colorSchemes.put(FragmentType.VOID, new ColorScheme(
            Color.fromRGB(47, 79, 79),    // Primary: DarkSlateGray
            Color.fromRGB(72, 61, 139),   // Secondary: DarkSlateBlue
            Color.fromRGB(106, 90, 205),  // Accent: SlateBlue
            Particle.PORTAL
        ));

        // DRAGON: Crimson/Red/Tomato
        colorSchemes.put(FragmentType.DRAGON, new ColorScheme(
            Color.fromRGB(220, 20, 60),   // Primary: Crimson
            Color.fromRGB(178, 34, 34),   // Secondary: Firebrick
            Color.fromRGB(255, 99, 71),   // Accent: Tomato
            Particle.DRAGON_BREATH
        ));

        // STORM: SteelBlue/CadetBlue/LightSteelBlue
        colorSchemes.put(FragmentType.STORM, new ColorScheme(
            Color.fromRGB(70, 130, 180),  // Primary: SteelBlue
            Color.fromRGB(95, 158, 160),  // Secondary: CadetBlue
            Color.fromRGB(176, 196, 222), // Accent: LightSteelBlue
            Particle.ELECTRIC_SPARK
        ));

        // TIME: Yellow/Gold/LightYellow (Temporal theme)
        colorSchemes.put(FragmentType.TIME, new ColorScheme(
            Color.fromRGB(255, 255, 0),   // Primary: Yellow
            Color.fromRGB(255, 215, 0),   // Secondary: Gold
            Color.fromRGB(255, 255, 224), // Accent: LightYellow
            Particle.END_ROD
        ));

        // LUCK: Green/LimeGreen/SpringGreen (Fortune theme)
        colorSchemes.put(FragmentType.LUCK, new ColorScheme(
            Color.fromRGB(0, 255, 0),     // Primary: Green
            Color.fromRGB(50, 205, 50),   // Secondary: LimeGreen
            Color.fromRGB(0, 255, 127),   // Accent: SpringGreen
            Particle.VILLAGER_HAPPY
        ));

        // ADMIN: DarkRed/Black/Crimson (Destructive power)
        colorSchemes.put(FragmentType.ADMIN, new ColorScheme(
            Color.fromRGB(139, 0, 0),     // Primary: DarkRed
            Color.fromRGB(0, 0, 0),       // Secondary: Black
            Color.fromRGB(220, 20, 60),   // Accent: Crimson
            Particle.SMOKE_LARGE
        ));
    }

    /**
     * Get color scheme for a Fragment type
     */
    public ColorScheme getColorScheme(FragmentType type) {
        return colorSchemes.get(type);
    }

    /**
     * Play particle line effect
     */
    public void playParticleLine(Location start, Location end, ColorScheme colors, int density) {
        if (start.getWorld() == null || end.getWorld() == null) return;
        
        int adjustedDensity = (int) (density * particleDensityMultiplier);
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();
        
        for (int i = 0; i < adjustedDensity; i++) {
            double progress = (double) i / adjustedDensity;
            Location particleLoc = start.clone().add(direction.clone().multiply(length * progress));
            
            start.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0,
                colors.getPrimaryDust(1.0f)
            );
        }
    }

    /**
     * Play circular shockwave effect
     */
    public void playCircularShockwave(Location center, double radius, ColorScheme colors) {
        if (center.getWorld() == null) return;
        
        int particles = (int) (radius * 20 * particleDensityMultiplier);
        
        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            
            Location particleLoc = center.clone().add(x, 0.1, z);
            
            center.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0,
                colors.getSecondaryDust(1.5f)
            );
        }
    }

    /**
     * Play beam effect
     */
    public void playBeam(Location start, Vector direction, double length, ColorScheme colors) {
        if (start.getWorld() == null) return;
        
        int particles = (int) (length * 10 * particleDensityMultiplier);
        direction.normalize();
        
        for (int i = 0; i < particles; i++) {
            double progress = (double) i / particles;
            Location particleLoc = start.clone().add(direction.clone().multiply(length * progress));
            
            // Gradient effect: primary to accent
            Particle.DustOptions dust = progress < 0.5 
                ? colors.getPrimaryDust(1.2f)
                : colors.getAccentDust(1.2f);
            
            start.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                2,
                0.05, 0.05, 0.05,
                0,
                dust
            );
        }
    }

    /**
     * Play aura effect around player
     */
    public void playAura(Player player, ColorScheme colors, int duration) {
        Location loc = player.getLocation();
        if (loc.getWorld() == null) return;
        
        int particles = (int) (30 * particleDensityMultiplier);
        
        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            double radius = 1.0 + 0.2 * Math.sin(System.currentTimeMillis() / 200.0); // Pulsing
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.sin(angle * 2) * 0.5;
            
            Location particleLoc = loc.clone().add(x, y + 1, z);
            
            loc.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0,
                colors.getPrimaryDust(0.8f)
            );
        }
    }

    /**
     * Play sound effect
     */
    public void playSound(Location location, SoundPreset preset, float volume, float pitch) {
        if (location.getWorld() == null) return;
        
        Sound sound = preset.getSound();
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    /**
     * Play ritual effect based on stage
     */
    public void playRitualEffect(Location location, RitualStage stage, ColorScheme colors) {
        if (location.getWorld() == null) return;
        
        switch (stage) {
            case CHARGING:
                playChargingEffect(location, colors);
                break;
            case ACTIVATION:
                playActivationEffect(location, colors);
                break;
            case COMPLETION:
                playCompletionEffect(location, colors);
                break;
        }
    }

    /**
     * Play charging stage effect
     */
    private void playChargingEffect(Location location, ColorScheme colors) {
        int particles = (int) (60 * particleDensityMultiplier); // Increased from 20 to 60
        
        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles + System.currentTimeMillis() / 1000.0;
            double radius = 3.0; // Increased from 2.0 to 3.0 for larger circle
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.sin(angle * 3) * 0.3;
            
            Location particleLoc = location.clone().add(x, y + 0.5, z);
            
            location.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0,
                colors.getPrimaryDust(1.0f)
            );
        }
        
        // Add inner circle for better visibility
        int innerParticles = (int) (40 * particleDensityMultiplier);
        for (int i = 0; i < innerParticles; i++) {
            double angle = 2 * Math.PI * i / innerParticles - System.currentTimeMillis() / 1500.0;
            double radius = 1.5;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            
            Location particleLoc = location.clone().add(x, 0.1, z);
            
            location.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0,
                colors.getSecondaryDust(0.7f)
            );
        }
    }

    /**
     * Play activation stage effect
     */
    private void playActivationEffect(Location location, ColorScheme colors) {
        // Beam from sky
        Location skyLoc = location.clone().add(0, 10, 0);
        playBeam(skyLoc, new Vector(0, -1, 0), 10, colors);
        
        // Rotating runes
        int runes = 8;
        for (int i = 0; i < runes; i++) {
            double angle = 2 * Math.PI * i / runes + System.currentTimeMillis() / 500.0;
            double radius = 1.5;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            
            Location runeLoc = location.clone().add(x, 1.5, z);
            
            location.getWorld().spawnParticle(
                Particle.REDSTONE,
                runeLoc,
                3,
                0.1, 0.1, 0.1,
                0,
                colors.getAccentDust(1.5f)
            );
        }
    }

    /**
     * Play completion stage effect
     */
    private void playCompletionEffect(Location location, ColorScheme colors) {
        int particles = (int) (100 * particleDensityMultiplier);
        
        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * Math.random();
            double radius = Math.random() * 3.0;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = Math.random() * 2.0;
            
            Location particleLoc = location.clone().add(x, y, z);
            
            location.getWorld().spawnParticle(
                Particle.REDSTONE,
                particleLoc,
                1,
                0, 0, 0,
                0.1,
                colors.getAccentDust(2.0f)
            );
        }
    }

    /**
     * Set debug mode
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Get debug mode status
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Set particle density multiplier
     */
    public void setParticleDensityMultiplier(double multiplier) {
        this.particleDensityMultiplier = Math.max(0.1, Math.min(5.0, multiplier));
        plugin.getLogger().info("Particle Density Multiplier: " + this.particleDensityMultiplier);
    }

    /**
     * Get particle density multiplier
     */
    public double getParticleDensityMultiplier() {
        return particleDensityMultiplier;
    }
}
