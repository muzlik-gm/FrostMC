package com.muzlik.fragment.ability;

import org.bukkit.Particle;

/**
 * Enum defining visual effects for abilities
 */
public enum VisualEffect {
    FIRE_PROJECTILE(Particle.FLAME),
    FIRE_DASH(Particle.FLAME),
    FIRE_VORTEX(Particle.FLAME),
    FIRE_RESURRECTION(Particle.FLAME),
    FIRE_TRANSFORMATION(Particle.FLAME),
    
    WATER_HEALING(Particle.WATER_SPLASH),
    WATER_SHIELD(Particle.WATER_BUBBLE),
    WATER_WAVE(Particle.WATER_SPLASH),
    
    AIR_BLADE(Particle.CLOUD),
    AIR_DASH(Particle.CLOUD),
    AIR_BARRAGE(Particle.CLOUD),
    AIR_ARMOR(Particle.CLOUD),
    AIR_TRANSFORMATION(Particle.CLOUD),
    
    EARTH_IMPACT(Particle.BLOCK_CRACK),
    EARTH_ARMOR(Particle.BLOCK_CRACK),
    EARTH_SHOCKWAVE(Particle.BLOCK_CRACK),
    
    DARK_BOLT(Particle.SMOKE_LARGE),
    DARK_DRAIN(Particle.DAMAGE_INDICATOR),
    DARK_ZONE(Particle.SMOKE_LARGE),
    DARK_CLONE(Particle.SMOKE_LARGE),
    DARK_TRANSFORMATION(Particle.SMOKE_LARGE),
    
    LIGHT_BEAM(Particle.END_ROD),
    LIGHT_HEALING(Particle.END_ROD),
    LIGHT_PILLAR(Particle.END_ROD),
    LIGHT_SANCTUARY(Particle.END_ROD),
    LIGHT_TRANSFORMATION(Particle.END_ROD),
    
    STORM_LIGHTNING(Particle.ELECTRIC_SPARK),
    VOID_PORTAL(Particle.PORTAL),
    MOB_SUMMON(Particle.SMOKE_LARGE),
    DRAGON_BREATH(Particle.DRAGON_BREATH);
    
    private final Particle particle;
    
    VisualEffect(Particle particle) {
        this.particle = particle;
    }
    
    public Particle getParticle() {
        return particle;
    }
}
