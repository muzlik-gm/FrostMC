package com.muzlik.fragment;

/**
 * Enum representing the 10 Fragment types (Shards) in the system.
 * Each Fragment represents an elemental or thematic power category.
 */
public enum FragmentType {
    FIRE("Fire", "Aggressive damage-over-time and ignition mechanics", 1),
    WATER("Water", "Defensive healing and flow-based movement", 2),
    AIR("Air", "Mobility, speed, knockback, and flight mechanics", 3),
    DARK("Dark", "Debuffs, life-steal, and shadow mechanics", 4),
    LIGHT("Light", "Buffs, purification, and radiant damage", 5),
    VOID("Void", "Teleportation, dimension manipulation, and chaos", 6),
    DRAGON("Dragon", "Raw power, breath attacks, and wing mechanics", 7),
    STORM("Storm", "Lightning, weather control, and chain effects", 1007),
    TIME("Time", "Temporal manipulation and time-based effects", 1008),
    LUCK("Luck", "Probability manipulation and fortune-based effects", 1009),
    ADMIN("Admin", "Ultimate destructive power - Admin only", 99);

    private final String displayName;
    private final String description;
    private final int customModelData;

    FragmentType(String displayName, String description, int customModelData) {
        this.displayName = displayName;
        this.description = description;
        this.customModelData = customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCustomModelData() {
        return customModelData;
    }
}
