package com.muzlik.fragment.ability;

import java.util.HashMap;
import java.util.Map;

/**
 * Rank threshold behavior unlocks.
 * 
 * Defines behavior modifications and enhancements that unlock at specific rank thresholds.
 * Common thresholds: Rank 5, 7, 9
 * 
 * Per SYSTEM.md specification:
 * - Implement behavior modifications at rank 5, 7, 9
 * - Add enhanced VFX at rank thresholds
 * - Add ability behavior changes (e.g., piercing count, summon count)
 * 
 * Requirements: 4.6
 */
public class RankThreshold {
    
    // Standard rank thresholds
    public static final int THRESHOLD_TIER_1 = 3;  // Basic enhancement
    public static final int THRESHOLD_TIER_2 = 5;  // Significant enhancement
    public static final int THRESHOLD_TIER_3 = 7;  // Major enhancement
    public static final int THRESHOLD_TIER_4 = 9;  // Ultimate enhancement
    
    /**
     * Behavior modification types
     */
    public enum BehaviorType {
        PIERCING_COUNT,      // Number of enemies projectile can pierce
        SUMMON_COUNT,        // Number of summons that can be active
        PROJECTILE_COUNT,    // Number of projectiles spawned
        CHAIN_COUNT,         // Number of chain lightning jumps
        AREA_SIZE,           // Size of area effects
        DURATION_BONUS,      // Additional duration beyond scaling
        COOLDOWN_REDUCTION,  // Cooldown reduction percentage
        MANA_EFFICIENCY,     // Mana cost reduction percentage
        CINEMATIC_EFFECTS,   // Enable cinematic screen effects
        ENHANCED_VFX,        // Enable enhanced particle effects
        SPECIAL_MECHANIC     // Unlock special ability mechanic
    }
    
    /**
     * Rank threshold configuration for an ability
     */
    public static class ThresholdConfig {
        private final Map<Integer, Map<BehaviorType, Object>> thresholds;
        
        public ThresholdConfig() {
            this.thresholds = new HashMap<>();
        }
        
        /**
         * Add a behavior unlock at a specific rank
         * 
         * @param rank Rank threshold
         * @param behavior Behavior type
         * @param value Value for the behavior
         * @return This config for chaining
         */
        public ThresholdConfig addUnlock(int rank, BehaviorType behavior, Object value) {
            thresholds.computeIfAbsent(rank, k -> new HashMap<>()).put(behavior, value);
            return this;
        }
        
        /**
         * Get behavior value at current rank
         * Returns the highest threshold value that the rank meets
         * 
         * @param currentRank Current rank
         * @param behavior Behavior type
         * @param defaultValue Default value if no threshold met
         * @return Behavior value
         */
        public Object getValue(int currentRank, BehaviorType behavior, Object defaultValue) {
            Object result = defaultValue;
            
            // Find highest threshold that current rank meets
            for (Map.Entry<Integer, Map<BehaviorType, Object>> entry : thresholds.entrySet()) {
                int thresholdRank = entry.getKey();
                
                if (currentRank >= thresholdRank) {
                    Map<BehaviorType, Object> behaviors = entry.getValue();
                    if (behaviors.containsKey(behavior)) {
                        result = behaviors.get(behavior);
                    }
                }
            }
            
            return result;
        }
        
        /**
         * Get integer behavior value
         */
        public int getInt(int currentRank, BehaviorType behavior, int defaultValue) {
            Object value = getValue(currentRank, behavior, defaultValue);
            return value instanceof Number ? ((Number) value).intValue() : defaultValue;
        }
        
        /**
         * Get double behavior value
         */
        public double getDouble(int currentRank, BehaviorType behavior, double defaultValue) {
            Object value = getValue(currentRank, behavior, defaultValue);
            return value instanceof Number ? ((Number) value).doubleValue() : defaultValue;
        }
        
        /**
         * Get boolean behavior value
         */
        public boolean getBoolean(int currentRank, BehaviorType behavior, boolean defaultValue) {
            Object value = getValue(currentRank, behavior, defaultValue);
            return value instanceof Boolean ? (Boolean) value : defaultValue;
        }
        
        /**
         * Check if rank meets any threshold
         */
        public boolean meetsAnyThreshold(int currentRank) {
            return thresholds.keySet().stream().anyMatch(threshold -> currentRank >= threshold);
        }
        
        /**
         * Get the highest threshold met by current rank
         */
        public int getHighestThresholdMet(int currentRank) {
            return thresholds.keySet().stream()
                .filter(threshold -> currentRank >= threshold)
                .max(Integer::compareTo)
                .orElse(0);
        }
    }
    
    /**
     * Create a standard threshold config for projectile abilities
     * 
     * @return Threshold config with common projectile unlocks
     */
    public static ThresholdConfig createProjectileConfig() {
        return new ThresholdConfig()
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.PIERCING_COUNT, 1)
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.ENHANCED_VFX, true)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.PIERCING_COUNT, 2)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.CINEMATIC_EFFECTS, true)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.PIERCING_COUNT, 3)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.PROJECTILE_COUNT, 2);
    }
    
    /**
     * Create a standard threshold config for summon abilities
     * 
     * @return Threshold config with common summon unlocks
     */
    public static ThresholdConfig createSummonConfig() {
        return new ThresholdConfig()
            .addUnlock(THRESHOLD_TIER_1, BehaviorType.SUMMON_COUNT, 2)
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.SUMMON_COUNT, 3)
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.ENHANCED_VFX, true)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.SUMMON_COUNT, 4)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.CINEMATIC_EFFECTS, true)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.SUMMON_COUNT, 5)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.SPECIAL_MECHANIC, true);
    }
    
    /**
     * Create a standard threshold config for area abilities
     * 
     * @return Threshold config with common area effect unlocks
     */
    public static ThresholdConfig createAreaConfig() {
        return new ThresholdConfig()
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.AREA_SIZE, 1.2)
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.ENHANCED_VFX, true)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.AREA_SIZE, 1.5)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.CINEMATIC_EFFECTS, true)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.AREA_SIZE, 2.0)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.DURATION_BONUS, 1.3);
    }
    
    /**
     * Create a standard threshold config for chain abilities
     * 
     * @return Threshold config with common chain effect unlocks
     */
    public static ThresholdConfig createChainConfig() {
        return new ThresholdConfig()
            .addUnlock(THRESHOLD_TIER_1, BehaviorType.CHAIN_COUNT, 2)
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.CHAIN_COUNT, 3)
            .addUnlock(THRESHOLD_TIER_2, BehaviorType.ENHANCED_VFX, true)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.CHAIN_COUNT, 4)
            .addUnlock(THRESHOLD_TIER_3, BehaviorType.CINEMATIC_EFFECTS, true)
            .addUnlock(THRESHOLD_TIER_4, BehaviorType.CHAIN_COUNT, 5);
    }
    
    /**
     * Check if rank meets a specific threshold
     * 
     * @param rank Current rank
     * @param threshold Threshold to check
     * @return true if rank meets or exceeds threshold
     */
    public static boolean meetsThreshold(int rank, int threshold) {
        return rank >= threshold;
    }
    
    /**
     * Get cinematic effect unlock rank for a fragment type
     * 
     * @param fragmentType Fragment type name
     * @return Rank at which cinematic effects unlock
     */
    public static int getCinematicUnlockRank(String fragmentType) {
        return switch (fragmentType.toLowerCase()) {
            case "water" -> THRESHOLD_TIER_1;  // Rank 3
            case "earth" -> THRESHOLD_TIER_2;  // Rank 5
            case "fire", "air" -> THRESHOLD_TIER_2;  // Rank 5
            case "dark", "light", "mob" -> THRESHOLD_TIER_3;  // Rank 7
            case "storm" -> THRESHOLD_TIER_3;  // Rank 7
            case "void" -> THRESHOLD_TIER_4;  // Rank 9
            case "dragon" -> THRESHOLD_TIER_4;  // Rank 9
            default -> THRESHOLD_TIER_2;  // Default rank 5
        };
    }
}
