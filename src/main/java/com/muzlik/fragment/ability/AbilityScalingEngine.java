package com.muzlik.fragment.ability;

/**
 * Engine for scaling ability effects based on Fragment rank AND level.
 * Implements anime-style power progression where higher ranks feel significantly stronger.
 * 
 * RANK affects: Base damage, range, duration, defense, healing
 * LEVEL affects: Cooldown reduction, mana cost reduction, small damage bonus
 */
public class AbilityScalingEngine {

    /**
     * Scale damage based on rank AND level bonus
     * Formula: BASE_DAMAGE * (1 + (Rank * 0.10)) * (1 + levelDamageBonus)
     * @param baseDamage The base damage value
     * @param rank The Fragment rank
     * @param levelDamageBonus The damage bonus from level (0.0 to 0.15)
     * @return The scaled damage
     */
    public double scaleDamageWithLevel(double baseDamage, int rank, double levelDamageBonus) {
        return baseDamage * (1 + (rank * 0.10)) * (1 + levelDamageBonus);
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
     * Scale mana cost based on rank (increases) but reduced by level
     * Formula: BASE_MANA * (1 + (Rank * 0.08)) * (1 - levelManaReduction)
     * @param baseManaCost The base mana cost
     * @param rank The Fragment rank
     * @param levelManaReduction The mana reduction from level (0.0 to 0.20)
     * @return The scaled mana cost
     */
    public double scaleManaCostWithLevel(double baseManaCost, int rank, double levelManaReduction) {
        return baseManaCost * (1 + (rank * 0.08)) * (1 - levelManaReduction);
    }

    /**
     * Scale cooldown based on level reduction
     * Formula: BASE_COOLDOWN * (1 - levelCooldownReduction)
     * @param baseCooldown The base cooldown in milliseconds
     * @param levelCooldownReduction The cooldown reduction from level (0.0 to 0.25)
     * @return The scaled cooldown
     */
    public long scaleCooldown(long baseCooldown, double levelCooldownReduction) {
        return (long) (baseCooldown * (1 - levelCooldownReduction));
    }

    /**
     * Simple damage scaling based on rank only (for backward compatibility)
     * Formula: BASE_DAMAGE * (1 + (Rank * 0.10))
     * @param baseDamage The base damage value
     * @param rank The Fragment rank
     * @return The scaled damage
     */
    public double scaleDamage(double baseDamage, int rank) {
        return baseDamage * (1 + (rank * 0.10));
    }

    /**
     * Simple mana cost scaling based on rank only (for backward compatibility)
     * Formula: BASE_MANA * (1 + (Rank * 0.08))
     * @param baseManaCost The base mana cost
     * @param rank The Fragment rank
     * @return The scaled mana cost
     */
    public double scaleManaCost(double baseManaCost, int rank) {
        return baseManaCost * (1 + (rank * 0.08));
    }
}
