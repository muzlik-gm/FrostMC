package com.muzlik.vfx.cinematic;

import com.muzlik.vfx.ActiveEffectRegistry;
import com.muzlik.vfx.cinematic.animation.AnimationController;
import com.muzlik.vfx.cinematic.miniblock.MiniBlockManager;
import com.muzlik.vfx.cinematic.ritual.RitualVFX;
import com.muzlik.vfx.cinematic.ritual.RitualAnimationSequence;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Main cinematic VFX engine.
 * Wires together ShapeRenderer, MagicCircleFactory, ParticleDensityManager,
 * AnimationController, and MiniBlockManager.
 * 
 * Requirements: 8.1-8.5, 9.4
 */
public class CinematicVFXEngine {
    
    private final Plugin plugin;
    private final ShapeRenderer shapeRenderer;
    private final MagicCircleFactory magicCircleFactory;
    private final ParticleDensityManager densityManager;
    private final AnimationController animationController;
    private final MiniBlockManager miniBlockManager;
    private final TPSMonitor tpsMonitor;
    private final ParticleBatcher particleBatcher;
    private final ExplosionEffect explosionEffect;
    private final ActiveEffectRegistry effectRegistry;
    private final RitualVFX ritualVFX;
    private final RitualAnimationSequence ritualAnimations;
    
    public CinematicVFXEngine(Plugin plugin, ActiveEffectRegistry effectRegistry) {
        this.plugin = plugin;
        this.shapeRenderer = new ShapeRenderer();
        this.magicCircleFactory = new MagicCircleFactory();
        this.densityManager = new ParticleDensityManager();
        this.animationController = new AnimationController();
        this.miniBlockManager = new MiniBlockManager(plugin);
        this.tpsMonitor = new TPSMonitor();
        this.particleBatcher = new ParticleBatcher();
        this.explosionEffect = new ExplosionEffect();
        this.effectRegistry = effectRegistry;
        this.ritualVFX = new RitualVFX((com.muzlik.FrostSMPPlugin) plugin, this);
        this.ritualAnimations = new RitualAnimationSequence((com.muzlik.FrostSMPPlugin) plugin, this);
    }
    
    /**
     * Update engine (call every tick)
     */
    public void tick() {
        // Update TPS monitor
        tpsMonitor.tick();
        
        // Update mini blocks
        miniBlockManager.tick();
        
        // Reset particle batcher counts
        particleBatcher.resetAllCounts();
    }
    
    /**
     * Clean up resources (call on plugin disable)
     */
    public void cleanup() {
        miniBlockManager.cleanup();
    }
    
    // Component getters
    
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    public MagicCircleFactory getMagicCircleFactory() {
        return magicCircleFactory;
    }
    
    public ParticleDensityManager getDensityManager() {
        return densityManager;
    }
    
    public AnimationController getAnimationController() {
        return animationController;
    }
    
    public MiniBlockManager getMiniBlockManager() {
        return miniBlockManager;
    }
    
    public TPSMonitor getTpsMonitor() {
        return tpsMonitor;
    }
    
    public ParticleBatcher getParticleBatcher() {
        return particleBatcher;
    }
    
    public Plugin getPlugin() {
        return plugin;
    }
    
    public ExplosionEffect getExplosionEffect() {
        return explosionEffect;
    }
    
    public ActiveEffectRegistry getEffectRegistry() {
        return effectRegistry;
    }
    
    public RitualVFX getRitualVFX() {
        return ritualVFX;
    }
    
    public RitualAnimationSequence getRitualAnimations() {
        return ritualAnimations;
    }
    
    // High-level VFX methods
    
    /**
     * Spawn a magic circle for a duration
     */
    public void spawnMagicCircle(MagicCircle circle, int durationTicks) {
        effectRegistry.registerMagicCircle(circle);
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks) {
                    circle.startFade();
                    effectRegistry.unregisterMagicCircle(circle);
                    cancel();
                    return;
                }
                
                circle.update(ticks);
                
                // Use the new direct render method for crisp, complex magic circles
                circle.render(circle.getCenter().getWorld());
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Get dust particle options for fragment type
     */
    private org.bukkit.Particle.DustOptions getDustOptionsForFragment(com.muzlik.fragment.FragmentType fragmentType, int layer) {
        org.bukkit.Color color;
        
        // Fragment-specific colors
        switch (fragmentType) {
            case FIRE:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(255, 100, 0) : org.bukkit.Color.fromRGB(255, 200, 0);
                break;
            case WATER:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(0, 150, 255) : org.bukkit.Color.fromRGB(100, 200, 255);
                break;
            case AIR:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(200, 255, 255) : org.bukkit.Color.fromRGB(255, 255, 255);
                break;
            case DARK:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(100, 0, 150) : org.bukkit.Color.fromRGB(50, 0, 100);
                break;
            case LIGHT:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(255, 255, 200) : org.bukkit.Color.fromRGB(255, 255, 255);
                break;
            case VOID:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(50, 0, 50) : org.bukkit.Color.fromRGB(100, 0, 100);
                break;
            case STORM:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(100, 100, 255) : org.bukkit.Color.fromRGB(200, 200, 255);
                break;
            case DRAGON:
                color = layer % 2 == 0 ? org.bukkit.Color.fromRGB(255, 50, 50) : org.bukkit.Color.fromRGB(255, 150, 0);
                break;
            default:
                color = org.bukkit.Color.fromRGB(255, 255, 255);
                break;
        }
        
        // Small particle size for crisp lines (0.5 = half block)
        return new org.bukkit.Particle.DustOptions(color, 0.5f);
    }
    
    /**
     * Spawn particles with density management
     */
    public void spawnParticle(Location location, Particle particle, int count, 
                             double offsetX, double offsetY, double offsetZ, 
                             double speed, Player viewer) {
        // Check particle batcher limits
        if (viewer != null && !particleBatcher.canSpawnParticles(viewer, count)) {
            return;
        }
        
        // Spawn particles
        if (viewer != null) {
            viewer.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
            particleBatcher.addParticles(viewer, count);
        } else {
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }
}
