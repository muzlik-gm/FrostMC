package com.muzlik.fragment.ability.executors.earth;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.environment.EnvironmentManager;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Earthen Fortress - Earth Fragment Defensive Ability
 * Creates protective stone wall
 * 
 * VFX: 5-Layer System with rising stone
 * - Core: BLOCK_CRACK rising shards
 * - Secondary: BLOCK_DUST trail
 * - Ambient: Ground rumble
 * - Impact: Debris burst
 * - Cinematic: Ground shake at rank 4+
 */
public class EarthenFortressExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 8.0;
        int duration = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        // Get environment manager
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        EnvironmentManager envManager = plugin.getEnvironmentManager();
        
        // Create wall in front of player
        Vector direction = player.getLocation().getDirection().normalize();
        Location center = player.getLocation().add(direction.multiply(2));
        
        // Build 3x3 wall
        int wallHeight = 3;
        int wallWidth = 3;
        
        // Determine wall orientation based on player direction
        Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        
        // Launch block projectiles that rise up to form the wall
        com.muzlik.block.BlockManipulationEngine blockEngine = plugin.getBlockManipulationEngine();
        int blocksLaunched = 0;
        int maxBlocks = 4 + rank; // 4-7 blocks based on rank
        
        for (int y = 0; y < wallHeight && blocksLaunched < maxBlocks; y++) {
            for (int x = -wallWidth/2; x <= wallWidth/2 && blocksLaunched < maxBlocks; x++) {
                Location blockSpawn = center.clone().add(right.clone().multiply(x)).add(0, y - 1, 0);
                
                // Calculate upward direction
                Vector upDirection = new Vector(0, 1.0, 0);
                
                // Create block controller config
                com.muzlik.block.BlockControllerConfig config = new com.muzlik.block.BlockControllerConfig.Builder()
                    .ownerUUID(player.getUniqueId())
                    .abilityId("earth_earthen_fortress")
                    .startLocation(blockSpawn)
                    .direction(upDirection)
                    .baseSpeed(0.15)
                    .accelerationFactor(1.01)
                    .maxSpeed(0.5)
                    .maxLifeTicks(25) // 1.25 seconds
                    .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
                    .blockType(Material.STONE)
                    .collisionRadius(0.5)
                    .baseDamage(0) // No damage, just visual
                    .fragmentRank(rank)
                    .build();
                
                // Spawn block projectile with slight delay
                int delay = blocksLaunched * 2; // Stagger spawns
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    blockEngine.spawnController(config);
                }, delay);
                
                blocksLaunched++;
            }
        }
        
        for (int y = 0; y < wallHeight; y++) {
            for (int x = -wallWidth/2; x <= wallWidth/2; x++) {
                Location blockLoc = center.clone().add(right.clone().multiply(x)).add(0, y, 0);
                Block block = blockLoc.getBlock();
                
                // Only place if air
                if (block.getType() == Material.AIR) {
                    envManager.placeTemporaryBlock(blockLoc, Material.STONE, duration, player);
                }
            }
        }
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: SMOKE rising shards
            .core(Particle.SMOKE_LARGE, 80, ParticlePattern.CONE, 1.5, 3, 1.5, 0.1, null)
            // Secondary: CLOUD trail
            .secondary(Particle.CLOUD, 50, ParticlePattern.RING, 1.5, 1, 1.5, 0.05, null)
            // Ambient: Ground rumble
            .ambient(Particle.SMOKE_NORMAL, 40, ParticlePattern.SPHERE, 2, 1, 2, 0.02, null)
            // Impact: Debris burst
            .impact(Particle.BLOCK_CRACK, 60, ParticlePattern.BURST, 1.5, 1, 1.5, 0.15, Material.STONE.createBlockData());
        
        // Cinematic: Ground shake at rank 4+
        if (rank >= 4) {
            vfxBuilder.cinematic(0.2, CinematicEffect.SCREEN_SHAKE);
        }
        
        vfxBuilder.spawn();
        
        // Sound: Rumble
        player.getWorld().playSound(center, Sound.BLOCK_STONE_PLACE, 2.0f, 0.8f);
        player.getWorld().playSound(center, Sound.ENTITY_RAVAGER_ROAR, 0.5f, 0.5f);
        
        // Buff: Resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 1));
        
    }
}
