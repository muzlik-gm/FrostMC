package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

/**
 * Fireball - Fire Fragment Primary Ability
 * Shoots fireball that explodes on impact, ignites enemies for 5s
 * Base: 6 hearts damage, scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: FLAME spiral
 * - Secondary: END_ROD trail
 * - Ambient: SMOKE_LARGE
 * - Impact: LAVA burst
 * - Cinematic: Heat shimmer at rank 5+
 */
public class FireballExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        
        // Scale damage
        double baseDamage = 3.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Spawn LARGE fireball (not small) for better visibility and explosion
        Location spawnLoc = eyeLoc.clone().add(direction.clone().multiply(1.5));
        org.bukkit.entity.Fireball fireball = eyeLoc.getWorld().spawn(spawnLoc, org.bukkit.entity.Fireball.class);
        fireball.setShooter(player);
        fireball.setDirection(direction.multiply(1.5 + (rank * 0.1))); // Faster at higher ranks
        fireball.setYield(2.0f); // Explosion power (2.0 = moderate destruction)
        fireball.setIsIncendiary(true); // Sets fire to blocks
        
        // 5-Layer VFX System - CLEAN AND FOCUSED for primary ability
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Reduced base counts for low-rank primary ability - clean, proportional VFX
        // At rank 1, these will be further reduced by VFXLayerBuilder (0.25x multiplier)
        int coreCount = 12 + (rank * 4);       // 12 → 24 at rank 3 (was 30 → 75)
        int secondaryCount = 6 + (rank * 2);   // 6 → 12 at rank 3 (was 15 → 45)
        int ambientCount = 4 + (rank * 2);     // 4 → 10 at rank 3 (was 10 → 34)
        int impactCount = 8 + (rank * 3);      // 8 → 17 at rank 3 (was 20 → 56)
        
        // Subtle spread increase - ability stays focused
        double spread = 0.2 + (rank * 0.05);   // 0.2 → 0.35 at rank 3 (was 0.3 → 0.75)
        
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, spawnLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core layer: FLAME spiral - focused flame trail
            .core(Particle.FLAME, coreCount, ParticlePattern.SPIRAL, spread, spread, spread, 0.04 + (rank * 0.01), null)
            // Secondary layer: END_ROD trail - minimal accent
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.POINT, spread * 0.4, spread * 0.4, spread * 0.4, 0.03, null)
            // Ambient layer: SMOKE_LARGE - subtle smoke wisps
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.POINT, spread * 0.6, spread * 0.6, spread * 0.6, 0.01, null)
            // Impact layer: LAVA burst - controlled burst
            .impact(Particle.LAVA, impactCount, ParticlePattern.BURST, spread, spread, spread, 0.05 + (rank * 0.02), null)
            // Magic circle at player's feet - scales with rank
            .withMagicCircle(FragmentType.FIRE, 2.0 + (rank * 0.2), 30 + (rank * 5));
        
        // Cinematic layer: Heat shimmer at rank 2+, more intense at higher ranks
        if (rank >= 2) {
            vfxBuilder.cinematic(0.15 + (rank * 0.03), CinematicEffect.HEAT_SHIMMER);
        }
        
        vfxBuilder.spawn();
        
        // Sound effect - higher pitch at higher ranks
        float pitch = 1.0f + (rank * 0.1f);
        eyeLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f + (rank * 0.2f), pitch);
        

    }
}