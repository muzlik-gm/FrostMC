package com.muzlik.fragment.ability;

/**
 * Enum representing ability tiers with associated mana costs and cooldowns.
 * Follows anime RPG style progression from basic attacks to ultimate transformations.
 */
public enum AbilityTier {
    BASIC(15, 25, 3000, 6000),          // 15-25 mana, 3-6s cooldown
    SKILL(30, 40, 8000, 12000),         // 30-40 mana, 8-12s cooldown
    ULTIMATE(50, 70, 15000, 25000),     // 50-70 mana, 15-25s cooldown
    ADVANCED(45, 65, 15000, 20000),     // 45-65 mana, 15-20s cooldown
    MASTERY(80, 100, 60000, 90000);     // 80-100 mana, 60-90s cooldown

    private final int minMana;
    private final int maxMana;
    private final long minCooldown;
    private final long maxCooldown;

    AbilityTier(int minMana, int maxMana, long minCooldown, long maxCooldown) {
        this.minMana = minMana;
        this.maxMana = maxMana;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
    }

    public int getMinMana() {
        return minMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public long getMinCooldown() {
        return minCooldown;
    }

    public long getMaxCooldown() {
        return maxCooldown;
    }

    /**
     * Get a mana cost within this tier's range
     * @param percentage 0.0 to 1.0, where 0 is min and 1 is max
     * @return The calculated mana cost
     */
    public double getManaCost(double percentage) {
        percentage = Math.max(0.0, Math.min(1.0, percentage));
        return minMana + (maxMana - minMana) * percentage;
    }

    /**
     * Get a cooldown within this tier's range
     * @param percentage 0.0 to 1.0, where 0 is min and 1 is max
     * @return The calculated cooldown in milliseconds
     */
    public long getCooldown(double percentage) {
        percentage = Math.max(0.0, Math.min(1.0, percentage));
        return (long) (minCooldown + (maxCooldown - minCooldown) * percentage);
    }
}
