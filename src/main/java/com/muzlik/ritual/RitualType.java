package com.muzlik.ritual;

/**
 * Enum representing different types of rituals in the system.
 */
public enum RitualType {
    FRAGMENT_CREATION("Fragment Creation", 360),      // 6 minutes (was 1 minute, +5 minutes)
    FRAGMENT_CHANGER("Fragment Changer", 330),        // 5.5 minutes (was 30 seconds, +5 minutes)
    RANK_UP("Rank Up", 345),                          // 5.75 minutes (was 45 seconds, +5 minutes)
    ABILITY_EXPANSION("Ability Expansion", 420),      // 7 minutes (was 2 minutes, +5 minutes) - Unlocks slot 3
    MASTERY_EXPANSION("Mastery Expansion", 480),      // 8 minutes (was 3 minutes, +5 minutes) - Unlocks slot 4
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
