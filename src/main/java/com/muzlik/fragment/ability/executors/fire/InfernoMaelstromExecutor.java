package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.environment.EnvironmentManager;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Inferno Maelstrom - Fire Fragment Ultimate Ability
 * Creates a massive vortex of fire that pulls enemies in
 * 
 * VFX: 5-Layer System with cinematic screen tremor
 * - Core: FLAME spiral vortex
 * - Secondary: END_ROD swirling
 * - Ambient: SMOKE_LARGE rising
 * - Impact: LAVA bursts
 * - Cinematic: Screen tremor at rank 5+
 */
public class InfernoMaelstromExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        int rank = context.getRank();
        double baseRadius = 6.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 5.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        EnvironmentManager envManager = plugin.getEnvironmentManager();
        
        // Use cinematic VFX system
        com.muzlik.vfx.cinematic.ability.FireAbilityVFX fireVFX = new com.muzlik.vfx.cinematic.ability.FireAbilityVFX(plugin);
        
        // Inferno Maelstrom cinematic VFX: rotating magic circle with flame tornado
        fireVFX.infernoMaelstrom(player, center, radius, 100, rank);
        
        // Create ring of fire blocks
        for (double angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * (radius * 0.8);
            double z = Math.sin(radians) * (radius * 0.8);
            
            Location fireLoc = center.clone().add(x, 0, z);
            Block block = fireLoc.getBlock();
            
            if (block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType().isSolid()) {
                envManager.placeTemporaryBlock(fireLoc, Material.FIRE, 100, player);
            }
        }
        
        // Damage and pull logic
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                
                for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        Vector pullVector = center.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.3);
                        target.setVelocity(pullVector);
                        if (ticks % 20 == 0) {
                            target.damage(damage / 5, player);
                            target.setFireTicks(100);
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
