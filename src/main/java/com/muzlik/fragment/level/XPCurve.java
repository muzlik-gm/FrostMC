package com.muzlik.fragment.level;

/**
 * Enum representing different XP scaling curves for Fragment leveling.
 */
public enum XPCurve {
    LINEAR("Linear", "Steady, predictable progression"),
    EXPONENTIAL("Exponential", "Increasingly difficult progression"),
    LOGARITHMIC("Logarithmic", "Fast early levels, slower later"),
    CUSTOM("Custom", "Unique progression curve");

    private final String displayName;
    private final String description;

    XPCurve(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calculate XP required for a given level based on the curve type
     * @param level The target level
     * @param baseXP The base XP value
     * @return XP required to reach the level
     */
    public double calculateXP(int level, double baseXP) {
        return switch (this) {
            case LINEAR -> baseXP * level;
            case EXPONENTIAL -> baseXP * Math.pow(1.5, level);
            case LOGARITHMIC -> baseXP * Math.log(level + 1) * 100;
            case CUSTOM -> baseXP * level; // Default to linear for custom
        };
    }
}
