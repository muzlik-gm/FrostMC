package com.muzlik.fragment;

/**
 * Enum representing the 10 Fragment types (Shards) in the system.
 * Each Fragment represents an elemental or thematic power category.
 */
public enum FragmentType {
    FIRE("Fire", "Aggressive damage-over-time and ignition mechanics"),
    WATER("Water", "Defensive healing and flow-based movement"),
    AIR("Air", "Mobility, speed, knockback, and flight mechanics"),
    EARTH("Earth", "Tanky shields and terrain manipulation"),
    DARK("Dark", "Debuffs, life-steal, and shadow mechanics"),
    LIGHT("Light", "Buffs, purification, and radiant damage"),
    VOID("Void", "Teleportation, dimension manipulation, and chaos"),
    MOB("Mob", "Summoning, pet control, and swarm mechanics"),
    DRAGON("Dragon", "Raw power, breath attacks, and wing mechanics"),
    STORM("Storm", "Lightning, weather control, and chain effects");

    private final String displayName;
    private final String description;

    FragmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
