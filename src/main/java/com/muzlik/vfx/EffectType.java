package com.muzlik.vfx;

/**
 * Enum representing different types of effects that can be tracked
 * in the ActiveEffectRegistry.
 * 
 * Per SYSTEM.md specification requirements.
 */
public enum EffectType {
    /**
     * Particle effect
     */
    PARTICLE,
    
    /**
     * Sound effect
     */
    SOUND,
    
    /**
     * Entity (summon, projectile, etc.)
     */
    ENTITY,
    
    /**
     * Temporary block placement
     */
    BLOCK,
    
    /**
     * Armor stand hitbox for collision detection
     */
    ARMOR_STAND,
    
    /**
     * Projectile with owner tracking
     */
    PROJECTILE,
    
    /**
     * Area of effect ability
     */
    AREA_EFFECT,
    
    /**
     * Summoned creature
     */
    SUMMON,
    
    /**
     * Block manipulation (moving blocks, ramps, etc.)
     */
    BLOCK_MANIPULATION,
    
    /**
     * Buff effect on player
     */
    BUFF,
    
    /**
     * Debuff effect on player
     */
    DEBUFF,
    
    /**
     * Transformation effect (dragon form, etc.)
     */
    TRANSFORMATION
}
