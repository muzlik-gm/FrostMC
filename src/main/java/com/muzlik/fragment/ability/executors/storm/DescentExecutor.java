package com.muzlik.fragment.ability.executors.storm;

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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Descent - Storm Fragment Ultimate Ability
 * Calls down 10 lightning strikes in 10-block radius over 5 seconds
 * Base: 6 hearts each strike, scales with rank
 */
public class DescentExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Get center location
        Location centerLoc = player.getTargetBlock(null, 30).getLocation();
        centerLoc.setY(centerLoc.getWorld().getHighestBlockYAt(centerLoc) + 1);
        
        // Scale parameters
        double damage = 12.0 + (rank * 2.0); // 6 hearts + 1 heart per rank
        int strikeCount = 10 + (rank / 2); // 10-14 strikes
        double radius = 10.0 + (rank * 1.0);
        int duration = 100; // 5 seconds in ticks
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        Random random = new Random();
        
        // Initial storm clouds VFX
        createStormClouds(plugin, centerLoc, radius, rank, player);
        
        // Lightning strikes over time
        new BukkitRunnable() {
            int strikes = 0;
            int ticks = 0;
            
            @Override
            public void run() {
                if (strikes >= strikeCount || ticks >= duration) {
                    // Final storm end VFX
                    endThunderstorm(plugin, centerLoc, rank, player);
                    cancel();
                    return;
                }
                
                // Random strike timing
                if (random.nextInt(10) < 3) { // 30% chance each tick
                    // Random location within radius
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * radius;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;
                    
                    Location strikeLoc = centerLoc.clone().add(x, 0, z);
                    strikeLoc.setY(strikeLoc.getWorld().getHighestBlockYAt(strikeLoc) + 1);
                    
                    executeLightningStrike(plugin, strikeLoc, damage, rank, player);
                    strikes++;
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 20L, 1L); // Start after 1 second
        
        // Storm sound
        centerLoc.getWorld().playSound(centerLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
        player.sendMessage("§6§lThunderstorm Descent §7activated! §e" + strikeCount + " §7lightning strikes incoming!");
    }
    
    private void createStormClouds(com.muzlik.FrostSMPPlugin plugin, Location center, double radius, int rank, Player player) {
        // Create storm cloud effect above the area
        for (int i = 0; i < 20; i++) {
            double angle = (i * Math.PI * 2) / 20;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location cloudLoc = center.clone().add(x, 15, z);
            
            VFXLayerBuilder cloudVfx = new VFXLayerBuilder(plugin, cloudLoc, rank, player)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .ambient(Particle.SMOKE_LARGE, 8 + rank, ParticlePattern.POINT, 2.0, 1.0, 2.0, 0.05, null)
                .core(Particle.ELECTRIC_SPARK, 5 + rank, ParticlePattern.POINT, 1.0, 0.5, 1.0, 0.03, null);
            
            cloudVfx.spawn();
        }
    }
    
    private void executeLightningStrike(com.muzlik.FrostSMPPlugin plugin, Location strikeLoc, double damage, int rank, Player caster) {
        // Strike lightning
        strikeLoc.getWorld().strikeLightning(strikeLoc);
        
        // Damage entities in small radius around strike
        double strikeRadius = 2.5 + (rank * 0.3);
        for (Entity entity : strikeLoc.getWorld().getNearbyEntities(strikeLoc, strikeRadius, strikeRadius, strikeRadius)) {
            if (entity instanceof LivingEntity && entity != caster) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, caster);
                
                // Strike impact VFX
                VFXLayerBuilder impactVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, caster)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .impact(Particle.ELECTRIC_SPARK, 12 + (rank * 2), ParticlePattern.BURST, 0.6, 0.6, 0.6, 0.08, null);
                
                impactVfx.spawn();
            }
        }
        
        // Lightning strike VFX
        VFXLayerBuilder strikeVfx = new VFXLayerBuilder(plugin, strikeLoc, rank, caster)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.ELECTRIC_SPARK, 25 + (rank * 5), ParticlePattern.POINT, 0.8, 2.5, 0.8, 0.12, null)
            .secondary(Particle.FLASH, 15 + (rank * 3), ParticlePattern.BURST, 1.5, 1.5, 1.5, 0.15, null);
        
        strikeVfx.spawn();
        
        // Strike sound
        strikeLoc.getWorld().playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.0f);
    }
    
    private void endThunderstorm(com.muzlik.FrostSMPPlugin plugin, Location center, int rank, Player player) {
        // Final thunderstorm end VFX
        VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, center.clone().add(0, 5, 0), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.EXPLOSION_LARGE, 30 + (rank * 6), ParticlePattern.BURST, 3.0, 3.0, 3.0, 0.2, null)
            .core(Particle.ELECTRIC_SPARK, 40 + (rank * 8), ParticlePattern.SPHERE, 2.5, 2.5, 2.5, 0.15, null);
        
        // Cinematic effect at rank 7+
        if (rank >= 7) {
            endVfx.cinematic(0.4, CinematicEffect.STORM_PULSE);
        }
        
        endVfx.spawn();
        
        // End sound
        center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 0.6f);
    }
}