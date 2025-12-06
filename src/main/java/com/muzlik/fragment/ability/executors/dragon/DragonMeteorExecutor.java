package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.block.BlockManipulationEngine;
import com.muzlik.block.BlockControllerConfig;
import com.muzlik.block.PacketBlockManager;
import com.muzlik.block.ShellType;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Dragon Meteor - Dragon Fragment Slot 3 Ability
 * 
 * Calls down a devastating meteor using BlockManipulationEngine.
 * Massive impact damage (40 HP base) with 8 block radius.
 * Creates packet-only crater visuals lasting 3 seconds.
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6
 */
public class DragonMeteorExecutor implements AbilityExecutor {
    
    private final FrostSMPPlugin plugin;
    private final BlockManipulationEngine blockEngine;
    private final PacketBlockManager packetBlockManager;
    
    // Track active meteors per player (max 1)
    private static final Map<UUID, UUID> activeMeteors = new HashMap<>();
    
    private static final double BASE_DAMAGE = 12.0;  // Reduced from 40 to 12
    private static final double IMPACT_RADIUS = 6.0;  // Reduced from 8 to 6
    private static final int SPAWN_HEIGHT = 50;
    
    public DragonMeteorExecutor(FrostSMPPlugin plugin, BlockManipulationEngine blockEngine,
                               PacketBlockManager packetBlockManager) {
        this.plugin = plugin;
        this.blockEngine = blockEngine;
        this.packetBlockManager = packetBlockManager;
    }
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Check if player already has an active meteor
        if (activeMeteors.containsKey(player.getUniqueId())) {
            player.sendMessage("§4✗ You already have an active meteor!");
            return;
        }
        
        // Get target location - FIXED: Prioritize entity targeting
        Location targetLoc = com.muzlik.fragment.ability.TargetingUtil.getGroundTargetLocation(player, 50);
        
        // Spawn meteor high above target
        Location spawnLoc = targetLoc.clone().add(0, SPAWN_HEIGHT, 0);
        
        // Calculate damage with rank scaling
        double damage = com.muzlik.fragment.ability.AbilityScaling.scaleDamage(BASE_DAMAGE, rank, 9);
        
        // Create meteor using BlockManipulationEngine
        BlockControllerConfig config = new BlockControllerConfig.Builder()
            .ownerUUID(player.getUniqueId())
            .abilityId("dragon_meteor")
            .startLocation(spawnLoc)
            .direction(new Vector(0, -1, 0))
            .baseSpeed(2.0)
            .accelerationFactor(0.1)
            .maxSpeed(4.0)
            .maxLifeTicks(100)
            .shellType(ShellType.FALLING_BLOCK)
            .blockType(Material.MAGMA_BLOCK)
            .collisionRadius(1.5)
            .baseDamage(damage)
            .fragmentRank(rank)
            .build();
        
        UUID meteorId = blockEngine.spawnController(config);
        
        if (meteorId == null) {
            player.sendMessage("§4✗ Failed to spawn meteor!");
            return;
        }
        
        // Track meteor
        activeMeteors.put(player.getUniqueId(), meteorId);
        
        // Monitor meteor for impact
        new org.bukkit.scheduler.BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                ticks++;
                
                // Check if meteor still exists
                if (blockEngine.getController(meteorId) == null || ticks > 100) {
                    // Meteor impacted or expired
                    handleImpact(player, targetLoc, damage, rank);
                    activeMeteors.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                
                // Spawn trail particles
                Location meteorLoc = blockEngine.getController(meteorId).getControllerEntity().getLocation();
                meteorLoc.getWorld().spawnParticle(Particle.FLAME, meteorLoc, 10, 0.5, 0.5, 0.5, 0.05);
                meteorLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, meteorLoc, 5, 0.3, 0.3, 0.3, 0.02);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Spawn warning VFX at target
        VFXLayerBuilder vfx = new VFXLayerBuilder(plugin, targetLoc, rank, player);
        vfx.core(Particle.FLAME, 30, ParticlePattern.RING, 
                IMPACT_RADIUS, 0.1, IMPACT_RADIUS, 0.0, null);
        vfx.spawn();
        
        // Play sound
        player.getWorld().playSound(spawnLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
        player.getWorld().playSound(targetLoc, Sound.BLOCK_BELL_USE, 1.5f, 0.5f);
        
    }
    
    /**
     * Handle meteor impact
     */
    private void handleImpact(Player player, Location impactLoc, double damage, int rank) {
        // Deal damage in radius
        for (Entity entity : impactLoc.getWorld().getNearbyEntities(impactLoc, IMPACT_RADIUS, IMPACT_RADIUS, IMPACT_RADIUS)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                
                // Calculate distance-based damage falloff
                double distance = entity.getLocation().distance(impactLoc);
                double damageMultiplier = 1.0 - (distance / IMPACT_RADIUS);
                double finalDamage = damage * Math.max(0.2, damageMultiplier);
                
                living.damage(finalDamage, player);
                
                // Knockback
                Vector knockback = entity.getLocation().toVector()
                    .subtract(impactLoc.toVector())
                    .normalize()
                    .multiply(2.0);
                knockback.setY(0.8);
                entity.setVelocity(knockback);
            }
        }
        
        // Create packet-only crater
        UUID craterGroupId = packetBlockManager.createBlockGroup(player.getUniqueId(), 60); // 3 seconds
        BlockData airData = Material.AIR.createBlockData();
        
        // Create crater pattern
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= 3.0) {
                    Location crateLoc = impactLoc.clone().add(x, -1, z);
                    packetBlockManager.addBlock(craterGroupId, crateLoc, airData);
                }
            }
        }
        
        packetBlockManager.sendToNearbyPlayers(craterGroupId, impactLoc, 32.0);
        
        // Schedule crater cleanup
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            packetBlockManager.revertGroup(craterGroupId);
        }, 60L);
        
        // Spawn massive impact VFX
        VFXLayerBuilder vfx = new VFXLayerBuilder(plugin, impactLoc, rank, player);
        
        vfx.core(Particle.EXPLOSION_HUGE, 5, ParticlePattern.POINT, 0, 0, 0, 0, null);
        vfx.secondary(Particle.FLAME, 100, ParticlePattern.BURST, 
                     IMPACT_RADIUS, IMPACT_RADIUS, IMPACT_RADIUS, 0.2, null);
        vfx.ambient(Particle.SMOKE_LARGE, 80, ParticlePattern.SPHERE,
                   IMPACT_RADIUS * 1.5, IMPACT_RADIUS * 1.5, IMPACT_RADIUS * 1.5, 0.1, null);
        vfx.impact(Particle.LAVA, 60, ParticlePattern.BURST,
                  IMPACT_RADIUS, 2.0, IMPACT_RADIUS, 0.15, null);
        
        if (rank >= 9) {
            vfx.cinematic(0.3, com.muzlik.vfx.CinematicEffect.DRAGON_ROAR);
        }
        
        vfx.spawn();
        
        // Play impact sounds
        impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
        impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_ENDER_DRAGON_HURT, 2.0f, 0.6f);
        impactLoc.getWorld().playSound(impactLoc, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.4f);
        
    }
}
