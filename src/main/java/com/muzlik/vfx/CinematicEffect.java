package com.muzlik.vfx;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Cinematic effects for ultimate abilities.
 * 
 * Brief screen effects (0.15-0.3 seconds) that enhance the visual impact
 * of high-rank abilities. Only triggered at rank thresholds (5+, 6+, etc.)
 * 
 * Requirements: 3.5
 */
public enum CinematicEffect {
    /**
     * No cinematic effect
     */
    NONE {
        @Override
        public void apply(Player player, double duration) {
            // No effect
        }
    },
    
    /**
     * Heat shimmer - brief blindness/fade effect
     * Used for fire abilities at rank 5+
     */
    HEAT_SHIMMER {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20); // Convert seconds to ticks
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Screen shake - small velocity impulse
     * Used for earth abilities at rank 4+
     */
    SCREEN_SHAKE {
        @Override
        public void apply(Player player, double duration) {
            Vector velocity = player.getVelocity();
            velocity.setY(velocity.getY() + 0.1);
            player.setVelocity(velocity);
        }
    },
    
    /**
     * Slow motion - brief slowness effect
     * Used for time-based abilities
     */
    SLOW_MOTION {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1, true, false, false));
        }
    },
    
    /**
     * Light bloom - brightness pulse
     * Used for light abilities at rank 6+
     */
    LIGHT_BLOOM {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Darkness pulse - brief darkness effect
     * Used for dark abilities at rank 6+
     */
    DARKNESS_PULSE {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Wind distortion - levitation effect
     * Used for air abilities at rank 5+
     */
    WIND_DISTORTION {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Water shimmer - brief water breathing visual
     * Used for water abilities at rank 3+
     */
    WATER_SHIMMER {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Reality warp - nausea effect
     * Used for void abilities at rank 8+
     */
    REALITY_WARP {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Pack aura - brief speed boost
     * Used for mob abilities at rank 6+
     */
    PACK_AURA {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 1, true, false, false));
        }
    },
    
    /**
     * Dragon roar - brief strength boost
     * Used for dragon abilities at rank 9+
     */
    DRAGON_ROAR {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, ticks, 0, true, false, false));
        }
    },
    
    /**
     * Storm pulse - brief jump boost
     * Used for storm abilities at rank 7+
     */
    STORM_PULSE {
        @Override
        public void apply(Player player, double duration) {
            int ticks = (int) (duration * 20);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 1, true, false, false));
        }
    };
    
    /**
     * Apply this cinematic effect to a player
     * 
     * @param player The player to apply the effect to
     * @param duration Duration in seconds (typically 0.15-0.3)
     */
    public abstract void apply(Player player, double duration);
}
