package com.muzlik.fx;

import org.bukkit.Sound;

/**
 * Enum for sound presets used in effects.
 */
public enum SoundPreset {
    // Elemental sounds
    FIRE_CAST(Sound.ITEM_FIRECHARGE_USE),
    FIRE_IMPACT(Sound.ENTITY_BLAZE_SHOOT),
    WATER_CAST(Sound.ITEM_BUCKET_EMPTY),
    WATER_IMPACT(Sound.ENTITY_PLAYER_SPLASH),
    AIR_CAST(Sound.ENTITY_ENDER_DRAGON_FLAP),
    AIR_IMPACT(Sound.ENTITY_PLAYER_ATTACK_SWEEP),
    EARTH_CAST(Sound.BLOCK_GRAVEL_BREAK),
    EARTH_IMPACT(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR),
    
    // Dark/Light sounds
    DARK_CAST(Sound.ENTITY_WITHER_SHOOT),
    DARK_IMPACT(Sound.ENTITY_WITHER_HURT),
    LIGHT_CAST(Sound.BLOCK_BEACON_ACTIVATE),
    LIGHT_IMPACT(Sound.BLOCK_BEACON_POWER_SELECT),
    
    // Special sounds
    VOID_CAST(Sound.ENTITY_ENDERMAN_TELEPORT),
    VOID_IMPACT(Sound.BLOCK_PORTAL_TRAVEL),
    MOB_CAST(Sound.ENTITY_EVOKER_CAST_SPELL),
    MOB_IMPACT(Sound.ENTITY_EVOKER_PREPARE_SUMMON),
    DRAGON_CAST(Sound.ENTITY_ENDER_DRAGON_GROWL),
    DRAGON_IMPACT(Sound.ENTITY_ENDER_DRAGON_HURT),
    STORM_CAST(Sound.ENTITY_LIGHTNING_BOLT_THUNDER),
    STORM_IMPACT(Sound.ENTITY_LIGHTNING_BOLT_IMPACT),
    
    // Generic sounds
    ABILITY_READY(Sound.BLOCK_NOTE_BLOCK_PLING),
    LEVEL_UP(Sound.ENTITY_PLAYER_LEVELUP),
    RANK_UP(Sound.UI_TOAST_CHALLENGE_COMPLETE),
    RITUAL_COMPLETE(Sound.UI_TOAST_CHALLENGE_COMPLETE),
    RITUAL_FAIL(Sound.ENTITY_VILLAGER_NO);

    private final Sound sound;

    SoundPreset(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }
}
