package com.muzlik.fragment.ability.executors.air;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Slice - Air Fragment Primary Ability
 * Launches a cutting wind projectile
 * 
 * VFX: 5-Layer System
 * - Core: CLOUD blade
 * - Secondary: END_ROD trail
 * - Ambient: Dust swirls
 * - Impact: SWEEP_ATTACK slash
 * - Cinematic: Wind distortion at rank 5+
 */
public class SliceExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        double baseDamage = 3.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Launch arrow from eye location
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(direction.multiply(3.0 + (rank * 0.2))); // Faster at higher ranks
        arrow.setDamage(damage);
        arrow.setCritical(true);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // CLEAN VFX for primary ability - swift, focused blade effect
        // Further reduced by VFXLayerBuilder's rank multiplier at low ranks
        int coreCount = 10 + (rank * 3);       // 10 → 19 at rank 3 (was 30 → 66)
        int secondaryCount = 5 + (rank * 2);   // 5 → 11 at rank 3 (was 15 → 39)
        int ambientCount = 6 + (rank * 2);     // 6 → 12 at rank 3 (was 20 → 50)
        int impactCount = 4 + (rank * 2);      // 4 → 10 at rank 3 (was 10 → 28)
        
        double spread = 0.2 + (rank * 0.04);   // Tight, focused spread (was 0.3 + rank * 0.1)
        
        // 5-Layer VFX System - Swift cutting blade
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, eyeLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: CLOUD blade - sharp, focused wind
            .core(Particle.CLOUD, coreCount, ParticlePattern.LINE, spread, spread, spread, 0.05 + (rank * 0.01), null)
            // Secondary: END_ROD trail - minimal accent
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.POINT, spread * 0.5, spread * 0.5, spread * 0.5, 0.03, null)
            // Ambient: DUST swirls - subtle air movement
            .ambient(Particle.WHITE_ASH, ambientCount, ParticlePattern.POINT, spread * 0.8, spread * 0.8, spread * 0.8, 0.01, null)
            // Impact: SWEEP_ATTACK slash - cutting effect
            .impact(Particle.SWEEP_ATTACK, impactCount, ParticlePattern.BURST, spread, spread, spread, 0.04 + (rank * 0.02), null);
        
        // Cinematic: Wind distortion at rank 5+
        if (rank >= 5) {
            vfxBuilder.cinematic(0.12 + (rank * 0.02), CinematicEffect.WIND_DISTORTION);
        }
        
        vfxBuilder.spawn();
        
        // Sound effect - higher pitch at higher ranks
        float pitch = 1.3f + (rank * 0.1f);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f + (rank * 0.1f), pitch);
    }
}