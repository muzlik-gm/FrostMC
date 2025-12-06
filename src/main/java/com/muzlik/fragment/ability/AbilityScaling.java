package com.muzlik.fragment.ability;

/**
 * Utility class for rank-based ability scaling.
 * 
 * Provides standardized scaling formulas for all ability parameters.
 * Per SYSTEM.md specification:
 * - Damage scaling: baseDamage * (1 + 0.12 * (rank - baseRank))
 * - Mana scaling: baseMana * (1 + 0.10 * (rank - baseRank))
 * - Duration scaling: baseDuration * (1 + 0.08 * (rank - baseRank))
 * - Range scaling: baseRange * (1 + 0.05 * (rank - baseRank))
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4
 */
public class AbilityScaling {
    
    // Scaling multipliers per rank (BALANCED)
    private static final double DAMAGE_MULTIPLIER = 0.08;  // Reduced from 0.12 to 0.08
    private static final double MANA_MULTIPLIER = 0.10;
    private static final double DURATION_MULTIPLIER = 0.08;
    private static final double RANGE_MULTIPLIER = 0.05;
    private static final double COOLDOWN_MULTIPLIER = 0.15; // Increase cooldown by 15% per rank
    
    /**
     * Scale damage based on rank
     * Formula: baseDamage * (1 + 0.12 * (rank - baseRank))
     * 
     * @param baseDamage Base damage value
     * @param rank Current rank
     * @param baseRank Base rank for the ability
     * @return Scaled damage
     */
    public static double scaleDamage(double baseDamage, int rank, int baseRank) {
        if (rank <= baseRank) {
            return baseDamage;
        }
        
        return baseDamage * (1.0 + DAMAGE_MULTIPLIER * (rank - baseRank));
    }
    
    /**
     * Scale mana cost based on rank
     * Formula: baseMana * (1 + 0.10 * (rank - baseRank))
     * 
     * @param baseMana Base mana cost
     * @param rank Current rank
     * @param baseRank Base rank for the ability
     * @return Scaled mana cost
     */
    public static double scaleMana(double baseMana, int rank, int baseRank) {
        if (rank <= baseRank) {
            return baseMana;
        }
        
        return baseMana * (1.0 + MANA_MULTIPLIER * (rank - baseRank));
    }
    
    /**
     * Scale duration based on rank
     * Formula: baseDuration * (1 + 0.08 * (rank - baseRank))
     * 
     * @param baseDuration Base duration in seconds
     * @param rank Current rank
     * @param baseRank Base rank for the ability
     * @return Scaled duration in seconds
     */
    public static double scaleDuration(double baseDuration, int rank, int baseRank) {
        if (rank <= baseRank) {
            return baseDuration;
        }
        
        return baseDuration * (1.0 + DURATION_MULTIPLIER * (rank - baseRank));
    }
    
    /**
     * Scale range based on rank
     * Formula: baseRange * (1 + 0.05 * (rank - baseRank))
     * 
     * @param baseRange Base range in blocks
     * @param rank Current rank
     * @param baseRank Base rank for the ability
     * @return Scaled range in blocks
     */
    public static double scaleRange(double baseRange, int rank, int baseRank) {
        if (rank <= baseRank) {
            return baseRange;
        }
        
        return baseRange * (1.0 + RANGE_MULTIPLIER * (rank - baseRank));
    }
    
    /**
     * Scale cooldown based on rank (increases with rank for balance)
     * Formula: baseCooldown * (1 + 0.15 * (rank - baseRank))
     * 
     * @param baseCooldown Base cooldown in seconds
     * @param rank Current rank
     * @param baseRank Base rank for the ability
     * @return Scaled cooldown in seconds
     */
    public static double scaleCooldown(double baseCooldown, int rank, int baseRank) {
        if (rank <= baseRank) {
            return baseCooldown;
        }
        
        return baseCooldown * (1.0 + COOLDOWN_MULTIPLIER * (rank - baseRank));
    }
    
    /**
     * Scale duration and return as ticks
     * 
     * @param baseDuration Base duration in seconds
     * @param rank Current rank
     * @param baseRank Base rank for the ability
     * @return Scaled duration in ticks (20 ticks = 1 second)
     */
    public static int scaleDurationTicks(double baseDuration, int rank, int baseRank) {
        double scaledSeconds = scaleDuration(baseDuration, rank, baseRank);
        return (int) Math.round(scaledSeconds * 20);
    }
    
    /**
     * Get damage multiplier for a given rank difference
     * 
     * @param rank Current rank
     * @param baseRank Base rank
     * @return Damage multiplier
     */
    public static double getDamageMultiplier(int rank, int baseRank) {
        if (rank <= baseRank) {
            return 1.0;
        }
        
        return 1.0 + DAMAGE_MULTIPLIER * (rank - baseRank);
    }
    
    /**
     * Get mana multiplier for a given rank difference
     * 
     * @param rank Current rank
     * @param baseRank Base rank
     * @return Mana multiplier
     */
    public static double getManaMultiplier(int rank, int baseRank) {
        if (rank <= baseRank) {
            return 1.0;
        }
        
        return 1.0 + MANA_MULTIPLIER * (rank - baseRank);
    }
    
    /**
     * Get duration multiplier for a given rank difference
     * 
     * @param rank Current rank
     * @param baseRank Base rank
     * @return Duration multiplier
     */
    public static double getDurationMultiplier(int rank, int baseRank) {
        if (rank <= baseRank) {
            return 1.0;
        }
        
        return 1.0 + DURATION_MULTIPLIER * (rank - baseRank);
    }
    
    /**
     * Get range multiplier for a given rank difference
     * 
     * @param rank Current rank
     * @param baseRank Base rank
     * @return Range multiplier
     */
    public static double getRangeMultiplier(int rank, int baseRank) {
        if (rank <= baseRank) {
            return 1.0;
        }
        
        return 1.0 + RANGE_MULTIPLIER * (rank - baseRank);
    }
    
    /**
     * Check if rank meets threshold for enhanced behavior
     * 
     * @param rank Current rank
     * @param threshold Threshold rank (5, 7, or 9)
     * @return true if rank meets or exceeds threshold
     */
    public static boolean meetsRankThreshold(int rank, int threshold) {
        return rank >= threshold;
    }
}
