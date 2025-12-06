package com.muzlik.fragment.ability.executors.storm;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.DamageAttributionManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Thunder God's Wrath / Thunderstorm Descent - Storm Fragment Ultimate (Slot 2)
 * Massive AoE lightning storm with sequential strikes
 * Multi-layer VFX, rank-scaled damage and coverage
 */
public class ThunderstormDescentExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        // FIXED: Prioritize entity targeting over blocks
        Location center = com.muzlik.fragment.ability.TargetingUtil.getGroundTargetLocation(player, 30);
        World world = center.getWorld();
        
        // Scale with rank
        double baseDamage = 7.0; // EXTREME NERF: 15.0 → 7.0 (3.5 hearts, ultimate multi-strike)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        int strikeCount = 8 + (rank * 2); // More strikes at higher ranks
        double radius = 15.0 + (rank * 1.5);
        
        // Initial atmospheric VFX
        world.spawnParticle(Particle.CLOUD, center.clone().add(0, 10, 0), 100, radius * 0.5, 2, radius * 0.5, 0.1);
        world.spawnParticle(Particle.SMOKE_LARGE, center.clone().add(0, 8, 0), 50, radius * 0.4, 1, radius * 0.4, 0.05);
        
        // Sound: Thunder rumble
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.7f);
        
        // Sequential lightning strikes with delay
        new BukkitRunnable() {
            int strikes = 0;
            
            @Override
            public void run() {
                if (strikes >= strikeCount) {
                    cancel();
                    return;
                }
                
                // Random position within radius
                double angle = Math.random() * Math.PI * 2;
                double dist = Math.random() * radius;
                double x = Math.cos(angle) * dist;
                double z = Math.sin(angle) * dist;
                
                Location strikeLoc = center.clone().add(x, 0, z);
                
                // Lightning effect
                world.strikeLightningEffect(strikeLoc);
                
                // Multi-layer VFX per strike
                world.spawnParticle(Particle.ELECTRIC_SPARK, strikeLoc.clone().add(0, 1, 0), 40, 0.5, 1.5, 0.5, 0.15);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, strikeLoc.clone().add(0, 1, 0), 25, 0.4, 1.0, 0.4, 0.08);
                world.spawnParticle(Particle.END_ROD, strikeLoc.clone().add(0, 2, 0), 20, 0.3, 0.8, 0.3, 0.12);
                world.spawnParticle(Particle.CRIT, strikeLoc, 30, 0.8, 0.2, 0.8, 0.2);
                
                // Sound per strike
                world.playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.0f + (float)(Math.random() * 0.4));
                world.playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.6f, 1.2f);
                
                // Damage entities near strike
                for (Entity entity : world.getNearbyEntities(strikeLoc, 3.5, 3.5, 3.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(damage, player);
                        
                        // Hit VFX
                        living.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, living.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
                    }
                }
                
                strikes++;
            }
        }.runTaskTimer(player.getServer().getPluginManager().getPlugin("FrostSMP"), 0L, 4L); // Strike every 0.2s
        
        player.sendMessage("§b⚡ Thunder God's Wrath! §7(" + strikeCount + " strikes)");
    }
}
