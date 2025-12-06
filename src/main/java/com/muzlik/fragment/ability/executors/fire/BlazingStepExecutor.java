package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.SafeTeleport;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Blazing Step - Fire Fragment Secondary Ability
 * Dash forward 8 blocks, leaves burning trail that damages enemies
 * FIXED: Safe teleportation
 * 
 * VFX: 5-Layer System with ember trail
 * - Core: FLAME line trail
 * - Secondary: END_ROD trail
 * - Ambient: SMOKE_LARGE
 * - Impact: LAVA burst at start/end
 * - Cinematic: Heat shimmer at rank 5+
 */
public class BlazingStepExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = context.getDirection().clone().normalize();
        Location startLoc = player.getLocation();
        int rank = context.getRank();
        
        // Scale range
        double baseRange = 8.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        
        // Get safe dash target
        Location targetLoc = SafeTeleport.getSafeDashTarget(player, direction, range);
        
        // Teleport player safely
        boolean success = SafeTeleport.teleportSafely(player, targetLoc);
        
        if (!success) {
            player.sendMessage("§c✗ Cannot dash there!");
            return;
        }
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial VFX at start location
        VFXLayerBuilder startVFX = new VFXLayerBuilder(plugin, startLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, 40, ParticlePattern.BURST, 0.5, 0.5, 0.5, 0.1, null)
            .secondary(Particle.END_ROD, 20, ParticlePattern.SPHERE, 0.3, 0.3, 0.3, 0.05, null)
            .impact(Particle.LAVA, 15, ParticlePattern.BURST, 0.4, 0.4, 0.4, 0.1, null);
        
        if (rank >= 5) {
            startVFX.cinematic(0.15, CinematicEffect.HEAT_SHIMMER);
        }
        
        startVFX.spawn();
        
        // Create burning trail with enhanced VFX
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                
                Location trailLoc = startLoc.clone().add(direction.clone().multiply(ticks * range / 60.0));
                
                // Trail VFX every 5 ticks
                if (ticks % 5 == 0) {
                    new VFXLayerBuilder(plugin, trailLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, 8, ParticlePattern.POINT, 0.3, 0.1, 0.3, 0.02, null)
                        .secondary(Particle.END_ROD, 3, ParticlePattern.POINT, 0.15, 0.05, 0.15, 0.01, null)
                        .ambient(Particle.SMOKE_LARGE, 4, ParticlePattern.POINT, 0.2, 0.1, 0.2, 0.01, null)
                        .spawn();
                }
                
                // Damage nearby enemies - FIXED: Use larger radius for better effectiveness
                double trailRadius = 2.5 + (rank * 0.2); // Scales with rank
                for (org.bukkit.entity.Entity entity : trailLoc.getWorld().getNearbyEntities(trailLoc, trailRadius, trailRadius, trailRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        target.setLastDamageCause(new org.bukkit.event.entity.EntityDamageEvent(
                            target, 
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE, 
                            2.0
                        ));
                        target.damage(2.0, player);
                        target.setFireTicks(40);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effect
        player.getWorld().playSound(startLoc, Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.5f);
        
        player.sendMessage("§c🔥 Blazing Step!");
    }
}
