package com.muzlik.vfx.cinematic;

/**
 * Scales visual complexity based on fragment rank.
 * Maps rank (1-9+) to ring counts, pattern complexity, and feature availability.
 * 
 * Requirements: 10.1-10.5, 11.1-11.5
 */
public class RankVisualScaler {
    
    /**
     * Pattern complexity levels
     */
    public enum PatternComplexity {
        BASIC,      // Rank 1-2: Simple shapes
        DETAILED,   // Rank 3-4: Fragment symbols, rotating segments
        COMPLEX,    // Rank 5-6: Mandalas, counter-rotation, glyphs
        ELABORATE,  // Rank 7-8: Animated sigils, energy lines, orbital runes
        MASTER      // Rank 9+: 3D depth, holographic, reality distortion
    }
    
    /**
     * Rank-gated features
     */
    public enum RankFeature {
        COUNTER_ROTATION,      // Rank 3+
        RUNE_SEGMENTS,         // Rank 3+
        GLOW_EFFECTS,          // Rank 3+
        ELEMENTAL_GLYPHS,      // Rank 5+
        AMBIENT_PARTICLES,     // Rank 5+
        LIGHT_EMISSION,        // Rank 5+
        ENERGY_LINES,          // Rank 7+
        ORBITAL_RUNES,         // Rank 7+
        MOTION_BLUR,           // Rank 7+
        HOLOGRAPHIC,           // Rank 9+
        REALITY_DISTORTION     // Rank 9+
    }
    
    /**
     * Get number of rings for magic circle based on rank
     * 
     * @param rank Player's fragment rank (1-9+)
     * @return Number of rings (1-5)
     */
    public int getRingCount(int rank) {
        if (rank <= 2) {
            return 1;
        } else if (rank <= 4) {
            return 2;
        } else if (rank <= 6) {
            return 3;
        } else if (rank <= 8) {
            return 4;
        } else {
            return 5; // Rank 9+
        }
    }
    
    /**
     * Get inner pattern complexity for rank
     * 
     * @param rank Player's fragment rank
     * @return Complexity level
     */
    public PatternComplexity getPatternComplexity(int rank) {
        if (rank <= 2) {
            return PatternComplexity.BASIC;
        } else if (rank <= 4) {
            return PatternComplexity.DETAILED;
        } else if (rank <= 6) {
            return PatternComplexity.COMPLEX;
        } else if (rank <= 8) {
            return PatternComplexity.ELABORATE;
        } else {
            return PatternComplexity.MASTER;
        }
    }
    
    /**
     * Get particle layer count for rank
     * 
     * @param rank Player's fragment rank
     * @return Number of particle layers (1-5)
     */
    public int getParticleLayerCount(int rank) {
        if (rank <= 2) {
            return 1;
        } else if (rank <= 4) {
            return 2;
        } else if (rank <= 6) {
            return 3;
        } else if (rank <= 8) {
            return 4;
        } else {
            return 5; // Rank 9+
        }
    }
    
    /**
     * Check if rank supports feature
     * 
     * @param rank Player's fragment rank
     * @param feature Feature to check
     * @return true if feature is available at this rank
     */
    public boolean hasFeature(int rank, RankFeature feature) {
        switch (feature) {
            case COUNTER_ROTATION:
            case RUNE_SEGMENTS:
            case GLOW_EFFECTS:
                return rank >= 3;
                
            case ELEMENTAL_GLYPHS:
            case AMBIENT_PARTICLES:
            case LIGHT_EMISSION:
                return rank >= 5;
                
            case ENERGY_LINES:
            case ORBITAL_RUNES:
            case MOTION_BLUR:
                return rank >= 7;
                
            case HOLOGRAPHIC:
            case REALITY_DISTORTION:
                return rank >= 9;
                
            default:
                return false;
        }
    }
}
