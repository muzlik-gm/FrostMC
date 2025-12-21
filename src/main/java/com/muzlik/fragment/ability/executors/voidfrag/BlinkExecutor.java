package com.muzlik.fragment.ability.executors.voidfrag;

import com.muzlik.fragment.FragmentType;
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
import org.bukkit.util.Vector;

/**
 * Blink - Void Fragment Secondary Ability
 * Instantly teleport up to 15 blocks in facing direction
 * Leaves void rift that damages enemies
 * 
 * VFX: 5-Layer System
 * - Core: PORTAL teleport effect
 * - Secondary: DRAGON_BREATH trail
 * - Ambient: SMOKE_LARGE wisps
 * - Impact: Rift damage particles
 * - Cinematic: Reality warp at rank 6+
 */
public class BlinkExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location startLoc = player.getLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        
        // Teleport distance increases with rank
        double distance = 15.0 + (rank * 2.0);
        
        // Find teleport destination
        Location targetLoc = startLoc.clone();
        for (double d = 0; d < distance; d += 0.5) {
            Location testLoc = startLoc.clone().add(direction.clone().multiply(d));
            if (!testLoc.getBlock().isPassable() || !testLoc.clone().add(0, 1, 0).getBlock().isPassable()) {
                break;
            }
            targetLoc = testLoc;
        }
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Departure VFX
        createTeleportVFX(plugin, startLoc.clone().add(0, 1, 0), rank, player, true);
        
        // Teleport player
        player.teleport(targetLoc);
        
        // Arrival VFX
        createTeleportVFX(plugin, targetLoc.clone().add(0, 1, 0), rank, player, false);
        
        // Create void rift at departure location
        createVoidRift(plugin, startLoc.clone().add(0, 1, 0), rank, player);
        
        // Sound effects
        float pitch = 1.0f + (rank * 0.1f);
        startLoc.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, pitch);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, pitch);
    }
    
    private void createTeleportVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player, boolean isDeparture) {
        int coreCount = 25 + (rank * 5);
        int secondaryCount = 15 + (rank * 3);
        int ambientCount = 10 + (rank * 2);
        
        double spread = 1.0 + (rank * 0.1);
        
        VFXLayerBuilder teleportVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.PORTAL, coreCount, ParticlePattern.BURST, spread, spread * 1.5, spread, 0.1, null)
            .secondary(Particle.DRAGON_BREATH, secondaryCount, ParticlePattern.SPHERE, spread * 0.8, spread, spread * 0.8, 0.08, null)
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.POINT, spread * 0.6, spread * 0.8, spread * 0.6, 0.05, null);
        
        // Magic circle at departure location - void themed
        if (isDeparture) {
            teleportVfx.withMagicCircle(FragmentType.VOID, 1.8 + (rank * 0.15), 25 + (rank * 3));
        }
        
        if (rank >= 6 && isDeparture) {
            teleportVfx.cinematic(0.2 + (rank * 0.03), CinematicEffect.REALITY_WARP);
        }
        
        teleportVfx.spawn();
    }
    
    private void createVoidRift(com.muzlik.FrostSMPPlugin plugin, Location riftLoc, int rank, Player caster) {
        // Void fragment base rank is 7
        // Formula: (current_rank - base_rank + 2) * multiplier
        double riftDamage = 2.0 + ((rank - 7 + 2) * 0.5); // 1 heart base + scales with upgrades
        int riftDuration = 100 + (rank * 20); // 5s + 1s per rank
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= riftDuration) {
                    // End rift with final burst
                    VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, riftLoc, rank, caster)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .impact(Particle.EXPLOSION_LARGE, 15 + rank * 3, ParticlePattern.BURST, 1.2, 1.2, 1.2, 0.1, null);
                    endVfx.spawn();
                    
                    cancel();
                    return;
                }
                
                // Rift VFX every 10 ticks
                if (ticks % 10 == 0) {
                    int riftCount = 8 + rank;
                    VFXLayerBuilder riftVfx = new VFXLayerBuilder(plugin, riftLoc, rank, caster)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.PORTAL, riftCount, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.03, null)
                        .secondary(Particle.DRAGON_BREATH, riftCount / 2, ParticlePattern.POINT, 0.3, 0.3, 0.3, 0.02, null);
                    riftVfx.spawn();
                }
                
                // Damage enemies every second
                if (ticks % 20 == 0) {
                    for (Entity entity : riftLoc.getWorld().getNearbyEntities(riftLoc, 2.0, 2.0, 2.0)) {
                        if (entity instanceof LivingEntity && entity != caster) {
                            LivingEntity target = (LivingEntity) entity;
                            target.damage(riftDamage, caster);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}