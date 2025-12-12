package com.muzlik.fragment.ability.executors.light;

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

/**
 * Sanctuary - Light Fragment Advanced Ability
 * Creates 6-block holy zone for 12s
 * Allies heal 1 heart/sec, enemies take 2 hearts/sec
 * 
 * VFX: 5-Layer System
 * - Core: GLOW dome
 * - Secondary: END_ROD pillars
 * - Ambient: HEART particles for allies
 * - Impact: Damage particles for enemies
 * - Cinematic: Holy aura at rank 6+
 */
public class SanctuaryExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location centerLoc = player.getLocation();
        int rank = context.getRank();
        
        double radius = 6.0 + (rank * 0.5);
        int duration = 240 + (rank * 20); // 12s + 1s per rank (in ticks)
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create sanctuary effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration) {
                    // End sanctuary with final burst
                    endSanctuary(plugin, centerLoc, rank, player);
                    cancel();
                    return;
                }
                
                // Every second (20 ticks), apply effects
                if (ticks % 20 == 0) {
                    applySanctuaryEffects(plugin, centerLoc, radius, rank, player);
                }
                
                // Continuous VFX every 10 ticks
                if (ticks % 10 == 0) {
                    createSanctuaryVFX(plugin, centerLoc, radius, rank, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Initial creation sound
        centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }
    
    private void applySanctuaryEffects(com.muzlik.FrostSMPPlugin plugin, Location center, double radius, int rank, Player caster) {
        double healAmount = 2.0 + (rank * 0.5); // 1 heart + 0.25 hearts per rank
        double damageAmount = 4.0 + (rank * 1.0); // 2 hearts + 0.5 hearts per rank
        
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;
                
                if (target instanceof Player) {
                    // Heal players (allies)
                    double currentHealth = target.getHealth();
                    double maxHealth = target.getMaxHealth();
                    double newHealth = Math.min(maxHealth, currentHealth + healAmount);
                    target.setHealth(newHealth);
                    
                    // Healing VFX
                    VFXLayerBuilder healVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, caster)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .ambient(Particle.HEART, 3 + rank, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.05, null);
                    healVfx.spawn();
                    
                } else {
                    // Damage hostile mobs (enemies)
                    target.damage(damageAmount, caster);
                    
                    // Damage VFX
                    VFXLayerBuilder damageVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, caster)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .impact(Particle.ELECTRIC_SPARK, 5 + rank, ParticlePattern.BURST, 0.3, 0.3, 0.3, 0.08, null);
                    damageVfx.spawn();
                }
            }
        }
    }
    
    private void createSanctuaryVFX(com.muzlik.FrostSMPPlugin plugin, Location center, double radius, int rank, Player caster) {
        int coreCount = 15 + (rank * 3);
        int secondaryCount = 10 + (rank * 2);
        int ambientCount = 8 + (rank * 2);
        
        // Create dome effect
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2) / 8;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location edgeLoc = center.clone().add(x, 0, z);
            
            VFXLayerBuilder domeVfx = new VFXLayerBuilder(plugin, edgeLoc, rank, caster)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .core(Particle.GLOW, coreCount / 8, ParticlePattern.POINT, 0.2, 1.0, 0.2, 0.03, null)
                .secondary(Particle.END_ROD, secondaryCount / 8, ParticlePattern.POINT, 0.1, 0.8, 0.1, 0.02, null);
            
            domeVfx.spawn();
        }
        
        // Center pillar
        VFXLayerBuilder centerVfx = new VFXLayerBuilder(plugin, center.clone().add(0, 1, 0), rank, caster)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .ambient(Particle.GLOW, ambientCount, ParticlePattern.SPIRAL, 0.3, 2.0, 0.3, 0.05, null);
        
        if (rank >= 6) {
            centerVfx.cinematic(0.15 + (rank * 0.02), CinematicEffect.LIGHT_BLOOM);
        }
        
        centerVfx.spawn();
    }
    
    private void endSanctuary(com.muzlik.FrostSMPPlugin plugin, Location center, int rank, Player caster) {
        // Final burst effect
        int impactCount = 25 + (rank * 5);
        
        VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, center.clone().add(0, 1, 0), rank, caster)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.FLASH, impactCount, ParticlePattern.BURST, 2.0, 2.0, 2.0, 0.1, null)
            .core(Particle.GLOW, impactCount, ParticlePattern.SPHERE, 1.5, 1.5, 1.5, 0.08, null);
        
        endVfx.spawn();
        
        // End sound
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.2f);
    }
}