package com.muzlik.fragment.ability.executors.water;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Tsunami Wave - Water Fragment Ultimate (Slot 2)
 * Summons massive wave that pushes enemies and slows them
 * FIXED: Proper wave animation and knockback
 * 
 * VFX: 5-Layer System with wave distortion cinematic
 * - Core: WATER_SPLASH wave front
 * - Secondary: DRIP_WATER spray
 * - Ambient: WATER_BUBBLE foam
 * - Impact: SPLASH burst on hit
 * - Cinematic: Wave distortion effect
 */
public class TsunamiWaveExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = context.getDirection().clone().normalize();
        direction.setY(0); // Keep horizontal
        int rank = context.getRank();
        double baseRange = 10.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        
        Location start = player.getLocation();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial cinematic VFX
        VFXLayerBuilder initialVFX = new VFXLayerBuilder(plugin, start, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.WATER_SPLASH, 100, ParticlePattern.CONE, 2, 3, 2, 0.2, null)
            .secondary(Particle.DRIP_WATER, 60, ParticlePattern.BURST, 2, 2, 2, 0.1, null)
            .impact(Particle.WATER_SPLASH, 80, ParticlePattern.BURST, 2, 2, 2, 0.3, null)
            .cinematic(0.25, CinematicEffect.WATER_SHIMMER);
        
        initialVFX.spawn();
        
        // Launch ice blocks that travel with the wave
        com.muzlik.block.BlockManipulationEngine blockEngine = plugin.getBlockManipulationEngine();
        int blockCount = 3 + rank; // 3-5 blocks based on rank
        
        for (int i = 0; i < blockCount; i++) {
            // Spread blocks across the wave width
            Vector perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
            double offset = (i - blockCount / 2.0) * 1.5;
            Location spawnLoc = start.clone().add(perpendicular.multiply(offset)).add(0, 1, 0);
            
            // Create ice block controller config
            com.muzlik.block.BlockControllerConfig config = new com.muzlik.block.BlockControllerConfig.Builder()
                .ownerUUID(player.getUniqueId())
                .abilityId("water_tsunami_wave")
                .startLocation(spawnLoc)
                .direction(direction.clone().setY(0.2))
                .baseSpeed(0.5)
                .accelerationFactor(1.02)
                .maxSpeed(1.5)
                .maxLifeTicks((int)(range * 2)) // Travel with wave
                .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
                .blockType(org.bukkit.Material.PACKED_ICE)
                .collisionRadius(0.8)
                .baseDamage(2.0) // Minor damage
                .fragmentRank(rank)
                .build();
            
            // Spawn block projectile with slight delay
            int delay = i * 2;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                blockEngine.spawnController(config);
            }, delay);
        }
        
        // Animated wave
        new BukkitRunnable() {
            double distance = 0;
            
            @Override
            public void run() {
                if (distance >= range) {
                    cancel();
                    return;
                }
                
                Location waveLoc = start.clone().add(direction.clone().multiply(distance));
                
                // Wave VFX using 5-layer system
                new VFXLayerBuilder(plugin, waveLoc, rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    // Core: WATER_SPLASH wave front
                    .core(Particle.WATER_SPLASH, 30, ParticlePattern.RING, 2, 1.5, 2, 0.1, null)
                    // Secondary: DRIP_WATER spray
                    .secondary(Particle.DRIP_WATER, 15, ParticlePattern.BURST, 2, 1.5, 2, 0.05, null)
                    // Ambient: WATER_BUBBLE foam
                    .ambient(Particle.WATER_BUBBLE, 20, ParticlePattern.SPHERE, 2, 1, 2, 0.05, null)
                    .spawn();
                
                // Push entities - FIXED: Use larger radius for better effectiveness
                double waveRadius = 4.0 + (rank * 0.3); // Scales with rank
                for (org.bukkit.entity.Entity entity : waveLoc.getWorld().getNearbyEntities(waveLoc, waveRadius, waveRadius, waveRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        
                        // Knockback
                        Vector knockback = direction.clone().multiply(1.5).setY(0.5);
                        target.setVelocity(knockback);
                        
                        // Slow effect
                        target.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SLOW, 80, 2));
                        
                        // Minor damage
                        target.setLastDamageCause(new org.bukkit.event.entity.EntityDamageEvent(
                            target, 
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.DROWNING, 
                            2.0
                        ));
                        target.damage(2.0, player);
                        
                        // Impact VFX on hit
                        new VFXLayerBuilder(plugin, target.getLocation(), rank, player)
                            .withPerformanceManager(plugin.getVFXPerformanceManager())
                            .impact(Particle.WATER_SPLASH, 20, ParticlePattern.BURST, 0.5, 0.5, 0.5, 0.2, null)
                            .spawn();
                    }
                }
                
                // Sound
                if (distance % 2 == 0) {
                    waveLoc.getWorld().playSound(waveLoc, Sound.ENTITY_PLAYER_SPLASH, 0.8f, 1.0f);
                }
                
                distance += 1.0;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        // Initial sound
        player.getWorld().playSound(start, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.8f);
        player.getWorld().playSound(start, Sound.BLOCK_WATER_AMBIENT, 2.0f, 0.6f);
        
    }
}
