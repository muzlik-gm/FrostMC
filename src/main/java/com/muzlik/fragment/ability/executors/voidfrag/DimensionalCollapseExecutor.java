package com.muzlik.fragment.ability.executors.voidfrag;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DimensionalCollapseExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        // FIXED: Prioritize entity targeting over blocks
        Location center = com.muzlik.fragment.ability.TargetingUtil.getTargetLocation(player, 30).add(0, 1, 0);
        World world = center.getWorld();
        double baseRadius = 10.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 7.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Initial massive VFX - MORE CHAOTIC AT HIGHER RANKS - FIXED: Use safe particles
        // FIXED: Smoke spawns away from player's view (at target location, not near player)
        int portalCount = 300 + (rank * 60); // 300 → 720 at rank 7
        int witchCount = 200 + (rank * 50); // 200 → 550 at rank 7
        int smokeCount = 150 + (rank * 40); // 150 → 430 at rank 7
        int endRodCount = 100 + (rank * 30); // 100 → 310 at rank 7
        
        world.spawnParticle(Particle.PORTAL, center, portalCount + witchCount, radius, radius, radius, 2 + (rank * 0.3));
        // Smoke spawns at target location (center), which is away from player
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, center, smokeCount, radius * 0.6, radius * 0.6, radius * 0.6, 0.5 + (rank * 0.1));
        world.spawnParticle(Particle.END_ROD, center, endRodCount, radius * 0.5, radius * 0.5, radius * 0.5, 0.3);
        world.spawnParticle(Particle.SQUID_INK, center, 80 + (rank * 20), radius * 0.4, radius * 0.4, radius * 0.4, 0.2);
        
        // Create black hole effect over time
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60; // 3 seconds
            java.util.Set<java.util.UUID> hitEntities = new java.util.HashSet<>();
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Final explosion VFX - FIXED: Use safe particles
                    // Smoke spawns at center (target location), not near player
                    world.spawnParticle(Particle.PORTAL, center, 350 + (rank * 70), 2, 2, 2, 3);
                    world.spawnParticle(Particle.EXPLOSION_HUGE, center, 5 + rank, 1, 1, 1, 0);
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, center, 100, 2, 2, 2, 0.5);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                    cancel();
                    return;
                }
                
                // Pull entities toward center
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        Vector pullVector = center.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.8 + (rank * 0.1));
                        target.setVelocity(pullVector);
                        
                        // Damage once when they get close
                        if (target.getLocation().distance(center) < 2.5 && !hitEntities.contains(entity.getUniqueId())) {
                            target.damage(damage, player);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2));
                            hitEntities.add(entity.getUniqueId());
                            
                            // Hit VFX - FIXED: Use safe particles
                            world.spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 50, 0.3, 0.5, 0.3, 0.5);
                        }
                    }
                }
                
                // Continuous VFX - spiral into center
                if (ticks % 2 == 0) {
                    for (int i = 0; i < 8 + rank; i++) {
                        double angle = (ticks * 0.3) + (i * Math.PI * 2 / (8 + rank));
                        double currentRadius = radius * (1 - (double)ticks / maxTicks);
                        double x = Math.cos(angle) * currentRadius;
                        double z = Math.sin(angle) * currentRadius;
                        Location particleLoc = center.clone().add(x, Math.sin(ticks * 0.2) * 2, z);
                        
                        world.spawnParticle(Particle.PORTAL, particleLoc, 5, 0.1, 0.1, 0.1, 0.5);
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.05, 0.05, 0.05, 0.02);
                    }
                }
                
                // Sound every 10 ticks
                if (ticks % 10 == 0) {
                    world.playSound(center, Sound.BLOCK_PORTAL_AMBIENT, 1.5f, 0.5f + (ticks * 0.02f));
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Initial sound
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f + (rank * 0.15f), 0.5f);
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.6f);
        
    }
}
