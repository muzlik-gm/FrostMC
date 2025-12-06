package com.muzlik.ritual;

/**
 * Enum representing different types of rituals in the system.
 */
public enum RitualType {
    FRAGMENT_CREATION("Fragment Creation", 60),       // 1 minute
    FRAGMENT_CHANGER("Fragment Changer", 30),         // 30 seconds
    RANK_UP("Rank Up", 45),                           // 45 seconds
    ABILITY_EXPANSION("Ability Expansion", 120),      // 2 minutes - Unlocks slot 3
    MASTERY_EXPANSION("Mastery Expansion", 180),      // 3 minutes - Unlocks slot 4
    CUSTOM("Custom", 0);                              // Variable duration

    private final String displayName;
    private final int defaultDuration; // in seconds

    RitualType(String displayName, int defaultDuration) {
        this.displayName = displayName;
        this.defaultDuration = defaultDuration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }
}
