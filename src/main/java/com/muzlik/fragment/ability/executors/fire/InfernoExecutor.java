package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Meteor Strike - Fire Fragment Ultimate Ability
 * Rains down explosive fireballs from the sky in the target area
 * Creates a devastating bombardment that damages and ignites enemies
 * 
 * VFX: 5-Layer System with cinematic screen shake
 * - Core: FLAME explosions
 * - Secondary: END_ROD trails
 * - Ambient: SMOKE_LARGE rising
 * - Impact: LAVA bursts
 * - Cinematic: Screen shake at rank 3+
 */
public class InfernoExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location targetLoc = player.getTargetBlock(null, 50).getLocation().add(0, 1, 0);
        int rank = context.getRank();
        double baseRadius = 8.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 6.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial warning VFX at target location
        int coreCount = 40 + (rank * 15);
        int secondaryCount = 25 + (rank * 10);
        int ambientCount = 15 + (rank * 8);
        
        VFXLayerBuilder warningVFX = new VFXLayerBuilder(plugin, targetLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, coreCount, ParticlePattern.SPHERE, radius * 0.5, 0.5, radius * 0.5, 0.05, null)
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.BURST, radius * 0.3, 0.3, radius * 0.3, 0.03, null)
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.SPHERE, radius * 0.4, 1.0, radius * 0.4, 0.02, null)
            .withMagicCircle(FragmentType.FIRE, radius * 0.8, 80);
        
        if (rank >= 3) {
            warningVFX.cinematic(0.15 + (rank * 0.05), CinematicEffect.SCREEN_SHAKE);
        }
        
        warningVFX.spawn();
        
        // Warning sound
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
        targetLoc.getWorld().playSound(targetLoc, Sound.BLOCK_FIRE_AMBIENT, 1.5f, 0.8f);
        
        // Rain down meteors over 3 seconds
        int meteorCount = 3; // Fixed 3 meteors
        
        new BukkitRunnable() {
            int meteorsSpawned = 0;
            
            @Override
            public void run() {
                if (meteorsSpawned >= meteorCount) {
                    cancel();
                    return;
                }
                
                // Random location within radius
                double angle = Math.random() * Math.PI * 2;
                double distance = Math.random() * radius;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                
                Location meteorTarget = targetLoc.clone().add(offsetX, 0, offsetZ);
                Location meteorSpawn = meteorTarget.clone().add(0, 30, 0); // Spawn 30 blocks up
                
                // Spawn fireball falling down
                Fireball meteor = meteorSpawn.getWorld().spawn(meteorSpawn, Fireball.class);
                meteor.setDirection(new Vector(0, -1, 0));
                meteor.setYield(2.5f); // Explosion power
                meteor.setIsIncendiary(true);
                meteor.setShooter(player);
                
                // Trail particles
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (!meteor.isValid() || ticks++ > 60) {
                            cancel();
                            return;
                        }
                        
                        Location loc = meteor.getLocation();
                        loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, 0.2, 0.2, 0.2, 0.02);
                        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 2, 0.1, 0.1, 0.1, 0.01);
                        loc.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0.1, 0.1, 0.1, 0);
                        
                        if (ticks % 10 == 0) {
                            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.6f);
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);
                
                meteorsSpawned++;
            }
        }.runTaskTimer(plugin, 10L, 6L); // Start after 0.5s, spawn every 0.3s
        
        player.sendMessage("§c☄ Meteor Strike incoming!");
    }
}