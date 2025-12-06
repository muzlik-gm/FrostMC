package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for quickly creating ability VFX and sounds.
 * Provides pre-configured effects for common ability patterns.
 * 
 * This accelerates ability implementation by providing reusable VFX patterns.
 */
public class AbilityVFXHelper {
    private final JavaPlugin plugin;
    private final VFXEngine vfxEngine;
    private final SoundEngine soundEngine;
    
    public AbilityVFXHelper(JavaPlugin plugin, VFXEngine vfxEngine, SoundEngine soundEngine) {
        this.plugin = plugin;
        this.vfxEngine = vfxEngine;
        this.soundEngine = soundEngine;
    }
    
    /**
     * Create a fire burst effect (Flame Burst ability)
     */
    public void createFireBurst(Location location, Player owner, int rank) {
        // Core flame spiral
        ParticleLayer flameSpiral = new ParticleLayer.Builder()
            .particleType(Particle.FLAME)
            .count(VFXEngine.calculateScaledParticleCount(30, rank))
            .pattern(ParticlePattern.SPIRAL)
            .speed(0.1)
            .build();
        
        // Heat shimmer
        ParticleLayer heatShimmer = new ParticleLayer.Builder()
            .particleType(Particle.END_ROD)
            .count(VFXEngine.calculateScaledParticleCount(15, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.05)
            .build();
        
        // Smoke layers
        ParticleLayer smoke = new ParticleLayer.Builder()
            .particleType(Particle.SMOKE_LARGE)
            .count(VFXEngine.calculateScaledParticleCount(20, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.02)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("fire_burst")
            .addParticleLayer(flameSpiral)
            .addParticleLayer(heatShimmer)
            .addParticleLayer(smoke)
            .setLifetime(50) // 2.5 seconds
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        
        // Sounds
        soundEngine.playSound(SoundDefinition.create(Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f), location, owner);
        soundEngine.playSound(SoundDefinition.create(Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.8f, 5), location, owner);
    }
    
    /**
     * Create a water healing effect (Aqua Pulse ability)
     */
    public void createWaterHeal(Location location, Player owner, int rank) {
        ParticleLayer splash = new ParticleLayer.Builder()
            .particleType(Particle.WATER_SPLASH)
            .count(VFXEngine.calculateScaledParticleCount(40, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.2)
            .build();
        
        ParticleLayer drip = new ParticleLayer.Builder()
            .particleType(Particle.DRIP_WATER)
            .count(VFXEngine.calculateScaledParticleCount(20, rank))
            .pattern(ParticlePattern.CIRCLE)
            .speed(0.1)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("water_heal")
            .addParticleLayer(splash)
            .addParticleLayer(drip)
            .setLifetime(100) // 5 seconds
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.2f), location, owner);
    }
    
    /**
     * Create an air blade effect (Wind Blade ability)
     */
    public void createAirBlade(Location location, Player owner, int rank) {
        ParticleLayer cloud = new ParticleLayer.Builder()
            .particleType(Particle.CLOUD)
            .count(VFXEngine.calculateScaledParticleCount(15, rank))
            .pattern(ParticlePattern.LINEAR)
            .speed(0.3)
            .build();
        
        ParticleLayer endRod = new ParticleLayer.Builder()
            .particleType(Particle.END_ROD)
            .count(VFXEngine.calculateScaledParticleCount(10, rank))
            .pattern(ParticlePattern.LINEAR)
            .speed(0.2)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("air_blade")
            .addParticleLayer(cloud)
            .addParticleLayer(endRod)
            .setLifetime(30)
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f), location, owner);
    }
    
    /**
     * Create an earth impact effect (Stone Fist ability)
     */
    public void createEarthImpact(Location location, Player owner, int rank) {
        ParticleLayer blockDust = new ParticleLayer.Builder()
            .particleType(Particle.BLOCK_DUST)
            .count(VFXEngine.calculateScaledParticleCount(50, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.3)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("earth_impact")
            .addParticleLayer(blockDust)
            .setLifetime(20)
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f), location, owner);
    }
    
    /**
     * Create a dark shadow effect (Shadow Strike ability)
     */
    public void createShadowStrike(Location location, Player owner, int rank) {
        ParticleLayer smoke = new ParticleLayer.Builder()
            .particleType(Particle.SMOKE_LARGE)
            .count(VFXEngine.calculateScaledParticleCount(30, rank))
            .pattern(ParticlePattern.SPIRAL)
            .speed(0.1)
            .build();
        
        ParticleLayer portal = new ParticleLayer.Builder()
            .particleType(Particle.PORTAL)
            .count(VFXEngine.calculateScaledParticleCount(20, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.2)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("shadow_strike")
            .addParticleLayer(smoke)
            .addParticleLayer(portal)
            .setLifetime(40)
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.7f), location, owner);
    }
    
    /**
     * Create a light beam effect (Radiant Lance ability)
     */
    public void createLightBeam(Location location, Player owner, int rank) {
        ParticleLayer endRod = new ParticleLayer.Builder()
            .particleType(Particle.END_ROD)
            .count(VFXEngine.calculateScaledParticleCount(40, rank))
            .pattern(ParticlePattern.LINEAR)
            .speed(0.4)
            .build();
        
        ParticleLayer firework = new ParticleLayer.Builder()
            .particleType(Particle.FIREWORKS_SPARK)
            .count(VFXEngine.calculateScaledParticleCount(25, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.2)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("light_beam")
            .addParticleLayer(endRod)
            .addParticleLayer(firework)
            .setLifetime(30)
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f), location, owner);
    }
    
    /**
     * Create a lightning strike effect (Lightning Bolt ability)
     */
    public void createLightningStrike(Location location, Player owner, int rank) {
        ParticleLayer electric = new ParticleLayer.Builder()
            .particleType(Particle.END_ROD)
            .count(VFXEngine.calculateScaledParticleCount(60, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.5)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("lightning_strike")
            .addParticleLayer(electric)
            .setLifetime(20)
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f), location, owner);
    }
    
    /**
     * Create a dragon breath effect (Dragon's Roar ability)
     */
    public void createDragonBreath(Location location, Player owner, int rank) {
        ParticleLayer dragonBreath = new ParticleLayer.Builder()
            .particleType(Particle.DRAGON_BREATH)
            .count(VFXEngine.calculateScaledParticleCount(50, rank))
            .pattern(ParticlePattern.WAVE)
            .speed(0.3)
            .build();
        
        ParticleLayer flame = new ParticleLayer.Builder()
            .particleType(Particle.FLAME)
            .count(VFXEngine.calculateScaledParticleCount(30, rank))
            .pattern(ParticlePattern.BURST)
            .speed(0.2)
            .build();
        
        EffectDefinition effect = new EffectDefinition.Builder()
            .id("dragon_breath")
            .addParticleLayer(dragonBreath)
            .addParticleLayer(flame)
            .setLifetime(60)
            .build();
        
        vfxEngine.spawnEffect(effect, location, owner, rank);
        soundEngine.playSound(SoundDefinition.create(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f), location, owner);
    }
}
