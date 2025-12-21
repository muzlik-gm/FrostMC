package com.muzlik.vfx;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.cinematic.CinematicVFXEngine;
import com.muzlik.vfx.cinematic.MagicCircle;
import com.muzlik.vfx.cinematic.MagicCircleConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder for creating Five-Layer VFX effects.
 * 
 * Provides fluent API for constructing complex particle effects with:
 * - Core layer: Primary particle identity
 * - Secondary layer: Trail particles
 * - Ambient layer: Environmental particles
 * - Impact layer: Collision particles
 * - Cinematic layer: Screen effects
 * 
 * Layers are rendered in order: Core → Secondary → Ambient → Impact → Cinematic
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
public class VFXLayerBuilder {
    private final JavaPlugin plugin;
    private final Location origin;
    private final int rank;
    private final Player owner;
    
    // Performance manager for throttling
    private VFXPerformanceManager perfManager;
    
    // Layer configurations
    private VFXLayer coreLayer;
    private VFXLayer secondaryLayer;
    private VFXLayer ambientLayer;
    private VFXLayer impactLayer;
    private CinematicEffect cinematicEffect;
    private double cinematicDuration;
    
    // Magic circle configuration
    private boolean spawnMagicCircle = false;
    private FragmentType magicCircleFragmentType;
    private double magicCircleRadius = 2.5;
    private int magicCircleDuration = 40; // ticks
    
    // Tracking
    private final List<VFXLayer> layers;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     * @param origin Origin location for the effect
     * @param rank Fragment rank for scaling
     * @param owner Player who owns this effect
     */
    public VFXLayerBuilder(JavaPlugin plugin, Location origin, int rank, Player owner) {
        this.plugin = plugin;
        this.origin = origin;
        this.rank = rank;
        this.owner = owner;
        this.layers = new ArrayList<>();
        this.cinematicEffect = CinematicEffect.NONE;
        this.cinematicDuration = 0.2; // Default 0.2 seconds
    }
    
    /**
     * Set performance manager for throttling
     * 
     * @param perfManager Performance manager
     * @return This builder
     */
    public VFXLayerBuilder withPerformanceManager(VFXPerformanceManager perfManager) {
        this.perfManager = perfManager;
        return this;
    }
    
    /**
     * Add core layer - primary particle identity
     * 
     * @param type Particle type
     * @param baseCount Base particle count
     * @param pattern Particle pattern
     * @return This builder
     */
    public VFXLayerBuilder core(Particle type, int baseCount, ParticlePattern pattern) {
        return core(type, baseCount, pattern, 0.2, 0.2, 0.2, 0.0, null);
    }
    
    /**
     * Add core layer with full parameters
     */
    public VFXLayerBuilder core(Particle type, int baseCount, ParticlePattern pattern,
                               double offsetX, double offsetY, double offsetZ,
                               double speed, Object data) {
        this.coreLayer = new VFXLayer("core", type, baseCount, pattern, 
                                     offsetX, offsetY, offsetZ, speed, data, true, true);
        layers.add(coreLayer);
        return this;
    }
    
    /**
     * Add secondary layer - trail particles
     * 
     * @param type Particle type
     * @param baseCount Base particle count
     * @param pattern Particle pattern
     * @return This builder
     */
    public VFXLayerBuilder secondary(Particle type, int baseCount, ParticlePattern pattern) {
        return secondary(type, baseCount, pattern, 0.15, 0.15, 0.15, 0.05, null);
    }
    
    /**
     * Add secondary layer with full parameters
     */
    public VFXLayerBuilder secondary(Particle type, int baseCount, ParticlePattern pattern,
                                    double offsetX, double offsetY, double offsetZ,
                                    double speed, Object data) {
        this.secondaryLayer = new VFXLayer("secondary", type, baseCount, pattern,
                                          offsetX, offsetY, offsetZ, speed, data, true, true);
        layers.add(secondaryLayer);
        return this;
    }
    
    /**
     * Add ambient layer - environmental particles
     * 
     * @param type Particle type
     * @param baseCount Base particle count
     * @param pattern Particle pattern
     * @return This builder
     */
    public VFXLayerBuilder ambient(Particle type, int baseCount, ParticlePattern pattern) {
        return ambient(type, baseCount, pattern, 0.3, 0.3, 0.3, 0.01, null);
    }
    
    /**
     * Add ambient layer with full parameters
     */
    public VFXLayerBuilder ambient(Particle type, int baseCount, ParticlePattern pattern,
                                  double offsetX, double offsetY, double offsetZ,
                                  double speed, Object data) {
        this.ambientLayer = new VFXLayer("ambient", type, baseCount, pattern,
                                        offsetX, offsetY, offsetZ, speed, data, true, true);
        layers.add(ambientLayer);
        return this;
    }
    
    /**
     * Add impact layer - collision particles
     * 
     * @param type Particle type
     * @param baseCount Base particle count
     * @param pattern Particle pattern
     * @return This builder
     */
    public VFXLayerBuilder impact(Particle type, int baseCount, ParticlePattern pattern) {
        return impact(type, baseCount, pattern, 0.5, 0.5, 0.5, 0.1, null);
    }
    
    /**
     * Add impact layer with full parameters
     */
    public VFXLayerBuilder impact(Particle type, int baseCount, ParticlePattern pattern,
                                 double offsetX, double offsetY, double offsetZ,
                                 double speed, Object data) {
        this.impactLayer = new VFXLayer("impact", type, baseCount, pattern,
                                       offsetX, offsetY, offsetZ, speed, data, true, true);
        layers.add(impactLayer);
        return this;
    }
    
    /**
     * Add cinematic layer - screen effects
     * 
     * @param duration Duration in seconds (0.15-0.3)
     * @param effect Cinematic effect type
     * @return This builder
     */
    public VFXLayerBuilder cinematic(double duration, CinematicEffect effect) {
        this.cinematicDuration = Math.max(0.15, Math.min(0.3, duration));
        this.cinematicEffect = effect;
        return this;
    }
    
    /**
     * Add a magic circle beneath the player when ability is cast
     * Magic circle complexity scales with rank automatically
     * 
     * @param fragmentType Fragment type for color theming
     * @return This builder
     */
    public VFXLayerBuilder withMagicCircle(FragmentType fragmentType) {
        return withMagicCircle(fragmentType, 2.5, 40);
    }
    
    /**
     * Add a magic circle with custom parameters
     * 
     * @param fragmentType Fragment type for color theming
     * @param radius Circle radius in blocks
     * @param durationTicks How long the circle lasts
     * @return This builder
     */
    public VFXLayerBuilder withMagicCircle(FragmentType fragmentType, double radius, int durationTicks) {
        this.spawnMagicCircle = true;
        this.magicCircleFragmentType = fragmentType;
        this.magicCircleRadius = radius;
        this.magicCircleDuration = durationTicks;
        return this;
    }
    
    /**
     * Spawn the VFX effect
     * 
     * @return UUID of the spawned effect
     */
    public UUID spawn() {
        if (origin == null || origin.getWorld() == null) {
            return null;
        }
        
        // Get rank scaling multiplier from config
        double rankScalingMultiplier = plugin.getConfig().getDouble("vfx.rank_scaling_multiplier", 0.06);
        
        // ULTRA MINIMAL PARTICLES - Quality over quantity, clean aesthetic
        // Philosophy: Less is more. Clean, visible effects without particle spam.
        // 
        // Rank 1: 0.15x particles (85% reduction - ultra minimal)
        // Rank 2: 0.20x particles (80% reduction)
        // Rank 3: 0.25x particles (75% reduction)
        // Rank 4: 0.35x particles (65% reduction)
        // Rank 5: 0.45x particles (55% reduction)
        // Rank 6: 0.55x particles (45% reduction)
        // Rank 7: 0.70x particles (30% reduction)
        // Rank 8: 0.85x particles (15% reduction - still conservative)
        double rankMultiplier;
        if (rank <= 4) {
            // Low ranks: Ultra minimal, clean VFX
            // Formula: 0.15 + (rank - 1) * 0.067 = 0.15, 0.22, 0.28, 0.35
            rankMultiplier = 0.15 + ((rank - 1) * 0.067);
        } else {
            // High ranks (5-8): Conservative scaling, quality focus
            // Formula: 0.45 + (rank - 5) * 0.133 = 0.45, 0.58, 0.71, 0.85
            rankMultiplier = 0.45 + ((rank - 5) * 0.133);
        }
        
        double perfMultiplier = perfManager != null ? perfManager.getParticleDensityMultiplier() : 1.0;
        
        // Apply scaling to all layers
        for (VFXLayer layer : layers) {
            layer.applyScaling(rankMultiplier, perfMultiplier);
        }
        
        // Render layers in order
        if (coreLayer != null) {
            coreLayer.render(origin);
        }
        
        if (secondaryLayer != null) {
            secondaryLayer.render(origin);
        }
        
        if (ambientLayer != null) {
            ambientLayer.render(origin);
        }
        
        if (impactLayer != null) {
            impactLayer.render(origin);
        }
        
        // Apply cinematic effect if rank is high enough and TPS is good
        if (cinematicEffect != CinematicEffect.NONE && owner != null) {
            boolean shouldApplyCinematic = true;
            
            // Check TPS if performance manager is available
            if (perfManager != null && perfManager.shouldSkipCinematic()) {
                shouldApplyCinematic = false;
            }
            
            if (shouldApplyCinematic) {
                cinematicEffect.apply(owner, cinematicDuration);
            }
        }
        
        // Spawn magic circle if configured
        if (spawnMagicCircle && magicCircleFragmentType != null && owner != null) {
            spawnAbilityMagicCircle();
        }
        
        return UUID.randomUUID();
    }
    
    /**
     * Spawn a magic circle for ability casting
     */
    private void spawnAbilityMagicCircle() {
        if (!(plugin instanceof FrostSMPPlugin)) return;
        
        FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
        CinematicVFXEngine cinematicEngine = frostPlugin.getCinematicVFXEngine();
        if (cinematicEngine == null) return;
        
        // Create magic circle at player's feet
        Location circleLocation = owner.getLocation().clone();
        
        // Build magic circle config
        MagicCircleConfig config = new MagicCircleConfig.Builder()
            .fragmentType(magicCircleFragmentType)
            .center(circleLocation)
            .rank(rank)
            .owner(owner)
            .baseRadius(magicCircleRadius)
            .rotationSpeed(0.08) // Slightly faster for abilities
            .lifetime(magicCircleDuration)
            .build();
        
        // Create and spawn the magic circle
        MagicCircle circle = cinematicEngine.getMagicCircleFactory().createMagicCircle(config);
        cinematicEngine.spawnMagicCircle(circle, magicCircleDuration);
    }
    
    /**
     * Get the number of layers configured
     * 
     * @return Number of layers
     */
    public int getLayerCount() {
        return layers.size();
    }
    
    /**
     * Get all configured layers
     * 
     * @return List of layers
     */
    public List<VFXLayer> getLayers() {
        return new ArrayList<>(layers);
    }
}
