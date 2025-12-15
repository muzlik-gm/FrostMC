package com.muzlik.fragment;

/**
 * Enum representing the 10 Fragment types (Shards) in the system.
 * Each Fragment represents an elemental or thematic power category.
 */
public enum FragmentType {
    FIRE("Fire", "Aggressive damage-over-time and ignition mechanics", 1),
    WATER("Water", "Defensive healing and flow-based movement", 2),
    AIR("Air", "Mobility, speed, knockback, and flight mechanics", 3),
    EARTH("Earth", "Tanky shields and terrain manipulation", 4),
    DARK("Dark", "Debuffs, life-steal, and shadow mechanics", 5),
    LIGHT("Light", "Buffs, purification, and radiant damage", 6),
    VOID("Void", "Teleportation, dimension manipulation, and chaos", 7),
    MOB("Mob", "Summoning, pet control, and swarm mechanics", 8),
    DRAGON("Dragon", "Raw power, breath attacks, and wing mechanics", 9),
    STORM("Storm", "Lightning, weather control, and chain effects", 10),
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
