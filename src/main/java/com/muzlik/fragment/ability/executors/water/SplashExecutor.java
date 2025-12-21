package com.muzlik.fragment.ability.executors.water;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Splash - Water Fragment Primary Ability
 * Healing splash that affects all nearby entities
 * 
 * VFX: 5-Layer System
 * - Core: WATER_SPLASH ripple
 * - Secondary: DRIP_WATER droplets
 * - Ambient: Blue ripple effect
 * - Impact: SPLASH burst
 * - Cinematic: Water shimmer at rank 3+
 */
public class SplashExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        double baseRadius = 6.0; // INCREASED from 4.0 to 6.0 for better effectiveness
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseHealing = 8.0;
        double healing = context.getScalingEngine().scaleHealing(baseHealing, rank);
        
        for (org.bukkit.entity.Entity entity : player.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.REGENERATION, 100, 1));
                if (target.getHealth() < target.getMaxHealth()) {
                    target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + healing));
                }
            }
        }
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // CLEAN, FOCUSED healing VFX - less is more for support abilities
        // These counts are then further reduced by VFXLayerBuilder's rank multiplier
        int coreCount = 25 + (rank * 8);       // 25 → 41 at rank 2 (was 100 → 160)
        int secondaryCount = 12 + (rank * 5);  // 12 → 22 at rank 2 (was 50 → 90)
        int ambientCount = 8 + (rank * 4);     // 8 → 16 at rank 2 (was 40 → 70)
        int impactCount = 15 + (rank * 6);     // 15 → 27 at rank 2 (was 60 → 110)
        
        // 5-Layer VFX System - Gentle healing aesthetic
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: WATER_SPLASH ripple - gentle outward pulse
            .core(Particle.WATER_SPLASH, coreCount, ParticlePattern.RING, radius * 0.5, 0.5 + (rank * 0.15), radius * 0.5, 0.05 + (rank * 0.01), null)
            // Secondary: DRIP_WATER droplets - subtle falling droplets
            .secondary(Particle.DRIP_WATER, secondaryCount, ParticlePattern.SPHERE, radius * 0.4, 0.8 + (rank * 0.15), radius * 0.4, 0.02, null)
            // Ambient: WATER_BUBBLE - sparse bubbles for atmosphere
            .ambient(Particle.WATER_BUBBLE, ambientCount, ParticlePattern.RING, radius * 0.3, 0.3 + (rank * 0.1), radius * 0.3, 0.01, null)
            // Impact: Gentle splash - not explosive, healing-focused
            .impact(Particle.WATER_SPLASH, impactCount, ParticlePattern.BURST, radius * 0.5, 0.5 + (rank * 0.2), radius * 0.5, 0.06 + (rank * 0.02), null);
        
        // Cinematic: Water shimmer at rank 2+, more intense at higher ranks
        if (rank >= 2) {
            vfxBuilder.cinematic(0.18 + (rank * 0.03), CinematicEffect.WATER_SHIMMER);
        }
        
        vfxBuilder.spawn();
        
        float pitch = 1.2f + (rank * 0.1f);
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_SPLASH, 1.0f + (rank * 0.2f), pitch);
    }
}