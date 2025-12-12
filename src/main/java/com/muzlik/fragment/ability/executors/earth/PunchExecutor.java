package com.muzlik.fragment.ability.executors.earth;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Punch - Earth Fragment Primary Ability
 * Powerful melee strike
 * 
 * VFX: 5-Layer System
 * - Core: BLOCK_DUST impact
 * - Secondary: SWEEP_ATTACK strike
 * - Ambient: Ground rumble
 * - Impact: Debris burst
 * - Cinematic: Ground shake at rank 4+
 */
public class PunchExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 4.0;  // Reduced from 14.0 to 4.0 (2 hearts)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Launch stone projectile in facing direction
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        com.muzlik.block.BlockManipulationEngine blockEngine = plugin.getBlockManipulationEngine();
        
        // Get player's facing direction
        Vector direction = player.getLocation().getDirection().normalize();
        Location spawnLoc = player.getEyeLocation().add(direction.clone().multiply(1.5));
        
        // Create block controller config
        com.muzlik.block.BlockControllerConfig config = new com.muzlik.block.BlockControllerConfig.Builder()
            .ownerUUID(player.getUniqueId())
            .abilityId("earth_punch")
            .startLocation(spawnLoc)
            .direction(direction)
            .baseSpeed(0.8)
            .accelerationFactor(1.1)
            .maxSpeed(2.0)
            .maxLifeTicks(60) // 3 seconds
            .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
            .blockType(Material.COBBLESTONE)
            .collisionRadius(1.0)
            .baseDamage(damage)
            .fragmentRank(rank)
            .build();
        
        // Spawn the block projectile
        blockEngine.spawnController(config);
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, spawnLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: BLOCK_DUST impact
            .core(Particle.BLOCK_DUST, 40, ParticlePattern.BURST, 0.8, 0.8, 0.8, 0.1, Material.STONE.createBlockData())
            // Secondary: SWEEP_ATTACK strike
            .secondary(Particle.SWEEP_ATTACK, 20, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.05, null)
            // Ambient: Ground rumble
            .ambient(Particle.BLOCK_CRACK, 30, ParticlePattern.RING, 1, 0.2, 1, 0.05, Material.DIRT.createBlockData())
            // Impact: Debris burst
            .impact(Particle.ITEM_CRACK, 25, ParticlePattern.BURST, 0.6, 0.6, 0.6, 0.08, new org.bukkit.inventory.ItemStack(Material.COBBLESTONE));
        
        // Cinematic: Ground shake at rank 4+
        if (rank >= 4) {
            vfxBuilder.cinematic(0.15, CinematicEffect.SCREEN_SHAKE);
        }
        
        vfxBuilder.spawn();
        
        // Sound
        player.getWorld().playSound(spawnLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.5f, 0.8f);
        player.getWorld().playSound(spawnLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
    }
}