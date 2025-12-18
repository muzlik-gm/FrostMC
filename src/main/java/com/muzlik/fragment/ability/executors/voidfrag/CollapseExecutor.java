package com.muzlik.fragment.ability.executors.voidfrag;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Collapse - Void Fragment Ultimate Ability
 * Creates black hole at target location, pulls all enemies within 10 blocks
 * 3 hearts damage + stun 3s, scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: PORTAL black hole
 * - Secondary: DRAGON_BREATH swirl
 * - Ambient: SMOKE_LARGE vortex
 * - Impact: EXPLOSION_LARGE implosion
 * - Cinematic: Reality warp at rank 8+
 */
public class CollapseExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location targetLoc = player.getTargetBlock(null, 25).getLocation().add(0.5, 1, 0.5);
        int rank = context.getRank();
        
        // Scale damage and effects
        // Void fragment base rank is 7
        // Formula: (current_rank - base_rank + 2) * multiplier
        double baseDamage = 6.0; // 3 hearts
        double damage = baseDamage + ((rank - 7 + 2) * 0.5); // Scales with upgrades beyond base rank
        double pullRadius = 10.0 + (rank * 1.0);
        int stunDuration = 60 + (rank * 10); // 3s + 0.5s per rank
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create black hole effect
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60; // 3 second buildup
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Final collapse
                    executeCollapse(plugin, player, targetLoc, damage, pullRadius, stunDuration, rank);
                    cancel();
                    return;
                }
                
                // Growing black hole VFX
                double intensity = (double) ticks / maxTicks;
                int coreCount = (int) (15 * intensity) + (rank * 2);
                int secondaryCount = (int) (10 * intensity) + rank;
                
                VFXLayerBuilder blackHoleVfx = new VFXLayerBuilder(plugin, targetLoc, rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .core(Particle.PORTAL, coreCount, ParticlePattern.POINT, 0.5 * intensity, 0.5 * intensity, 0.5 * intensity, 0.02, null)
                    .secondary(Particle.DRAGON_BREATH, secondaryCount, ParticlePattern.SPIRAL, 0.8 * intensity, 0.8 * intensity, 0.8 * intensity, 0.03, null);
                
                blackHoleVfx.spawn();
                
                // Pull entities toward black hole
                if (ticks % 5 == 0) {
                    pullEntities(targetLoc, pullRadius * intensity, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Warning sound
        targetLoc.getWorld().playSound(targetLoc, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 0.5f);
    }
    
    private void pullEntities(Location center, double radius, Player caster) {
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != caster) {
                Vector pullVector = center.toVector().subtract(entity.getLocation().toVector()).normalize();
                pullVector.multiply(0.3); // Pull strength
                pullVector.setY(Math.max(pullVector.getY(), 0.1)); // Slight upward pull
                
                entity.setVelocity(entity.getVelocity().add(pullVector));
            }
        }
    }
    
    private void executeCollapse(com.muzlik.FrostSMPPlugin plugin, Player caster, Location center, double damage, double radius, int stunDuration, int rank) {
        // Damage and stun all entities in radius
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != caster) {
                LivingEntity target = (LivingEntity) entity;
                
                // Deal damage
                plugin.getDamageAPI().dealTrueDamage(caster, target, damage);
                
                // Apply stun (slowness + mining fatigue)
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, stunDuration, 4));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, stunDuration, 4));
            }
        }
        
        // Massive collapse VFX
        int coreCount = 50 + (rank * 10);
        int secondaryCount = 40 + (rank * 8);
        int ambientCount = 30 + (rank * 6);
        int impactCount = 45 + (rank * 9);
        
        double spread = 4.0 + (rank * 0.5);
        
        VFXLayerBuilder collapseVfx = new VFXLayerBuilder(plugin, center, rank, caster)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core layer: PORTAL implosion
            .core(Particle.PORTAL, coreCount, ParticlePattern.BURST, spread, spread * 2, spread, 0.2, null)
            // Secondary layer: DRAGON_BREATH vortex
            .secondary(Particle.DRAGON_BREATH, secondaryCount, ParticlePattern.SPIRAL, spread * 0.8, spread * 1.5, spread * 0.8, 0.15, null)
            // Ambient layer: SMOKE_LARGE cloud
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.SPHERE, spread * 1.2, spread, spread * 1.2, 0.1, null)
            // Impact layer: EXPLOSION_LARGE burst
            .impact(Particle.EXPLOSION_LARGE, impactCount, ParticlePattern.BURST, spread * 1.5, spread * 2.5, spread * 1.5, 0.25, null);
        
        // Cinematic layer: Reality warp at rank 8+
        if (rank >= 8) {
            collapseVfx.cinematic(0.5 + (rank * 0.1), CinematicEffect.REALITY_WARP);
        }
        
        collapseVfx.spawn();
        
        // Collapse sound
        float pitch = 0.6f + (rank * 0.05f);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, pitch);
        center.getWorld().playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 1.5f, pitch);
    }
}