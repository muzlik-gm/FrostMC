package com.muzlik.fragment.ability.executors.storm;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Storm Surge - Storm Fragment Slot 3 Ability
 * 
 * Creates an electric field that slows and damages enemies.
 * 6-8 block radius (rank-based), 8-12 second duration (rank-based).
 * Applies Slowness II and deals 1.5 HP/second damage.
 * 
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
 */
public class StormSurgeExecutor implements AbilityExecutor {
    
    private final FrostSMPPlugin plugin;
    
    public StormSurgeExecutor(FrostSMPPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate rank-based parameters
        // Radius: 6-8 blocks based on rank
        double radius = 6.0 + (rank / 5.0);
        
        // Duration: 8-12 seconds based on rank
        double durationSeconds = 8.0 + (rank / 2.5);
        int durationTicks = (int) (durationSeconds * 20);
        
        Location center = player.getLocation();
        
        // Create electric field
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                ticks++;
                
                // Check for enemies in field
                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity living = (LivingEntity) entity;
                        
                        // Apply Slowness II
                        living.addPotionEffect(PotionEffectHelper.createHiddenEffect(
                            PotionEffectType.SLOW,
                            40, // 2 seconds
                            1 // Level II
                        ));
                        
                        // Deal damage every second (20 ticks)
                        if (ticks % 20 == 0) {
                            living.damage(0.5, player);  // Reduced from 1.5 to 0.5
                        }
                    }
                }
                
                // Spawn electric spark particles
                for (int i = 0; i < 10; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double r = Math.random() * radius;
                    double x = r * Math.cos(angle);
                    double z = r * Math.sin(angle);
                    double y = Math.random() * 3;
                    
                    Location particleLoc = center.clone().add(x, y, z);
                    center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
                    center.getWorld().spawnParticle(Particle.CRIT_MAGIC, particleLoc, 1, 0, 0, 0, 0);
                }
                
                // Play electric sound occasionally
                if (ticks % 40 == 0) {
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 2.0f);
                }
                
                // Expire after duration
                if (ticks >= durationTicks) {
                    cancel();
                    
                    // Final VFX
                    center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 50, 
                        radius, 2.0, radius, 0.1);
                    center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Spawn initial VFX
        VFXLayerBuilder vfx = new VFXLayerBuilder(plugin, center, rank, player);
        
        vfx.core(Particle.ELECTRIC_SPARK, 60, ParticlePattern.RING,
                radius, 1.0, radius, 0.1, null);
        vfx.secondary(Particle.CRIT_MAGIC, 40, ParticlePattern.SPHERE,
                     radius * 0.8, radius * 0.8, radius * 0.8, 0.05, null);
        vfx.ambient(Particle.CLOUD, 30, ParticlePattern.RING,
                   radius * 1.2, 3.0, radius * 1.2, 0.02, null);
        
        // Magic circle for storm surge - larger for area effect
        vfx.withMagicCircle(FragmentType.STORM, radius * 0.8, (int)(durationSeconds * 20));
        
        if (rank >= 7) {
            vfx.cinematic(0.2, com.muzlik.vfx.CinematicEffect.STORM_PULSE);
        }
        
        vfx.spawn();
        
        // Play sound
        center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.2f);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }
}
