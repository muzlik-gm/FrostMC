package com.muzlik.fragment.ability.executors.light;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Celestial Judgment - Light Fragment Ultimate (Slot 2)
 * Summons pillar of light with AoE damage
 * FIXED: No looping, single-hit per entity, optimized
 */
public class CelestialJudgmentExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseRadius = 7.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 6.0; // EXTREME NERF: 10.0 → 6.0 (3 hearts, ultimate AoE with undead bonus)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // FIXED: Prioritize entity targeting over blocks
        Location target = com.muzlik.fragment.ability.TargetingUtil.getGroundTargetLocation(player, 30);
        World world = target.getWorld();
        
        // Track hit entities to prevent multiple hits
        Set<UUID> hitEntities = new HashSet<>();
        
        // Lightning effect
        world.strikeLightningEffect(target);
        
        // Damage entities ONCE
        for (Entity entity : world.getNearbyEntities(target, radius, 10, radius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity.getUniqueId())) {
                LivingEntity livingTarget = (LivingEntity) entity;
                
                // Extra damage to undead
                double finalDamage = damage;
                if (livingTarget.getType() == EntityType.ZOMBIE || 
                    livingTarget.getType() == EntityType.SKELETON ||
                    livingTarget.getType() == EntityType.ZOMBIE_VILLAGER ||
                    livingTarget.getType() == EntityType.HUSK ||
                    livingTarget.getType() == EntityType.DROWNED ||
                    livingTarget.getType() == EntityType.PHANTOM ||
                    livingTarget.getType() == EntityType.WITHER_SKELETON) {
                    finalDamage *= 1.6;
                }
                
                livingTarget.damage(finalDamage, player);
                hitEntities.add(entity.getUniqueId());
                
                // Hit VFX
                world.spawnParticle(Particle.END_ROD, livingTarget.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
            }
        }
        
        // VFX: Pillar of light (animated over time) - MORE CHAOTIC AT HIGHER RANKS
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 20 + (rank * 5); // Longer animation at higher ranks
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    return;
                }
                
                // More particles at higher ranks
                int coreParticles = 3 + rank; // 3 → 7 at rank 4
                double pillarHeight = 10 + (rank * 2); // 10 → 18 at rank 4
                double stepSize = 0.5 - (rank * 0.05); // Denser at higher ranks
                
                // Pillar particles - MORE INTENSE AT HIGHER RANKS
                for (double y = 0; y < pillarHeight; y += Math.max(0.2, stepSize)) {
                    Location particleLoc = target.clone().add(0, y, 0);
                    
                    // Core beam - more particles at higher ranks
                    world.spawnParticle(Particle.END_ROD, particleLoc, coreParticles, 0.2 + (rank * 0.05), 0.1, 0.2 + (rank * 0.05), 0.02 + (rank * 0.01));
                    
                    // Additional light particles at higher ranks
                    if (rank >= 3) {
                        world.spawnParticle(Particle.FIREWORKS_SPARK, particleLoc, rank, 0.15, 0.1, 0.15, 0.05);
                    }
                    
                    // Outer glow - more rays at higher ranks
                    if (ticks % Math.max(1, 2 - rank / 2) == 0) {
                        int rays = 8 + (rank * 2); // 8 → 16 at rank 4
                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI * 2 / rays) {
                            double x = Math.cos(angle) * radius * 0.5;
                            double z = Math.sin(angle) * radius * 0.5;
                            Location glowLoc = target.clone().add(x, y * 0.5, z);
                            world.spawnParticle(Particle.FIREWORKS_SPARK, glowLoc, 1 + (rank / 2), 0.1, 0.1, 0.1, 0);
                        }
                    }
                }
                
                // Ground circle - more segments at higher ranks
                int circleInterval = Math.max(2, 3 - rank / 2);
                if (ticks % circleInterval == 0) {
                    int segments = 16 + (rank * 4); // 16 → 32 at rank 4
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI * 2 / segments) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location circleLoc = target.clone().add(x, 0.1, z);
                        world.spawnParticle(Particle.END_ROD, circleLoc, 1 + (rank / 3), 0, 0, 0, 0);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.2f);
        world.playSound(target, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.5f);
        world.playSound(target, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        
        player.sendMessage("§e✨ Celestial Judgment! §7(" + hitEntities.size() + " enemies struck)");
    }
}
