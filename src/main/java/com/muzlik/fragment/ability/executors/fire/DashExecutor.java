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
 * Dash - Fire Fragment Secondary Ability
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
public class DashExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = context.getDirection().clone().normalize();
        Location startLoc = player.getLocation();
        int rank = context.getRank();
        
        // Scale range
        double baseRange = 8.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Get safe dash target
        // Reduced counts for secondary ability
        int burstCore = 15 + (rank * 4);       // 15 → 27 at rank 3 (was 40 constant)
        int burstSecondary = 8 + (rank * 2);   // 8 → 14 at rank 3 (was 20 constant)
        int burstImpact = 6 + (rank * 2);      // 6 → 12 at rank 3 (was 15 constant)
        
        VFXLayerBuilder startVFX = new VFXLayerBuilder(plugin, startLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, burstCore, ParticlePattern.BURST, 0.3, 0.3, 0.3, 0.06, null)
            .secondary(Particle.END_ROD, burstSecondary, ParticlePattern.SPHERE, 0.2, 0.2, 0.2, 0.03, null)
            .impact(Particle.LAVA, burstImpact, ParticlePattern.BURST, 0.25, 0.25, 0.25, 0.05, null);
        
        // Cinematic only at higher ranks (5+)
        if (rank >= 5) {
            startVFX.cinematic(0.15, CinematicEffect.HEAT_SHIMMER);
        }
        
        startVFX.spawn();
        
        // Perform safe dash
        Location targetLoc = startLoc.clone().add(direction.multiply(range));
        Location safeLoc = SafeTeleport.findSafeLocation(targetLoc);
        if (safeLoc != null) {
            player.teleport(safeLoc);
        }
        
        // Create burning trail with FOCUSED VFX
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                
                Location trailLoc = startLoc.clone().add(direction.clone().multiply(ticks * range / 60.0));
                
                // Trail VFX every 5 ticks - minimal, focused embers
                if (ticks % 5 == 0) {
                    // Minimal trail counts - just enough to show the path
                    int trailCore = 3 + rank;      // 3 → 6 at rank 3 (was 8 constant)
                    int trailSecondary = 1 + (rank / 2); // 1 → 2 at rank 3 (was 3 constant)
                    int trailAmbient = 2 + (rank / 2);   // 2 → 3 at rank 3 (was 4 constant)
                    
                    new VFXLayerBuilder(plugin, trailLoc, rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, trailCore, ParticlePattern.POINT, 0.15, 0.05, 0.15, 0.01, null)
                        .secondary(Particle.END_ROD, trailSecondary, ParticlePattern.POINT, 0.08, 0.02, 0.08, 0.005, null)
                        .ambient(Particle.SMOKE_LARGE, trailAmbient, ParticlePattern.POINT, 0.1, 0.05, 0.1, 0.005, null)
                        .spawn();
                }
                
                // Damage nearby enemies - FIXED: Use larger radius for better effectiveness
                double trailRadius = 2.5 + (rank * 0.2); // Scales with rank
                for (org.bukkit.entity.Entity entity : trailLoc.getWorld().getNearbyEntities(trailLoc, trailRadius, trailRadius, trailRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        // Fire damage
                        target.damage(2.0, player);
                        target.setFireTicks(40);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effect
        player.getWorld().playSound(startLoc, Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.5f);
        

    }
}