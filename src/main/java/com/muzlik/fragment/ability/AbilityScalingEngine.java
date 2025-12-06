package com.muzlik.fragment.ability;

/**
 * Engine for scaling ability effects based on Fragment rank.
 * Implements anime-style power progression where higher ranks feel significantly stronger.
 */
public class AbilityScalingEngine {

    /**
     * Scale damage based on rank
     * Formula: BASE_DAMAGE * (1 + (Rank * 0.10))
     * REBALANCED: Reduced from 15% to 10% per rank for PvP balance
     * @param baseDamage The base damage value
     * @param rank The Fragment rank
     * @return The scaled damage
     */
    public double scaleDamage(double baseDamage, int rank) {
        return baseDamage * (1 + (rank * 0.10));
    }

    /**
     * Scale defense/resistance based on rank
     * Formula: BASE_DEFENSE * (1 + (Rank * 0.10))
     * @param baseDefense The base defense value
     * @param rank The Fragment rank
     * @return The scaled defense
     */
    public double scaleDefense(double baseDefense, int rank) {
        return baseDefense * (1 + (rank * 0.10));
    }

    /**
     * Scale healing based on rank
     * Formula: BASE_HEALING * (1 + (Rank * 0.12))
     * @param baseHealing The base healing value
     * @param rank The Fragment rank
     * @return The scaled healing
     */
    public double scaleHealing(double baseHealing, int rank) {
        return baseHealing * (1 + (rank * 0.12));
    }

    /**
     * Scale duration based on rank
     * Formula: BASE_DURATION * (1 + (Rank * 0.08))
     * @param baseDuration The base duration in seconds
     * @param rank The Fragment rank
     * @return The scaled duration
     */
    public double scaleDuration(double baseDuration, int rank) {
        return baseDuration * (1 + (rank * 0.08));
    }

    /**
     * Scale range based on rank
     * Formula: BASE_RANGE * (1 + (Rank * 0.05))
     * @param baseRange The base range in blocks
     * @param rank The Fragment rank
     * @return The scaled range
     */
    public double scaleRange(double baseRange, int rank) {
        return baseRange * (1 + (rank * 0.05));
    }

    /**
     * Scale mana cost based on rank (increases by 8% per rank)
     * REBALANCED: Reduced from 10% to 8% per rank
     * @param baseManaCost The base mana cost
     * @param rank The Fragment rank
     * @return The scaled mana cost
     */
    public double scaleManaCost(double baseManaCost, int rank) {
        return baseManaCost * (1 + (rank * 0.08));
    }
}
