package com.muzlik.fragment.ability.executors.earth;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.environment.EnvironmentManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SeismicSlamExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseRadius = 7.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        double baseDamage = 6.0;  // Reduced from 12.0 to 6.0 (3 hearts) for ultimate ability
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        Location center = player.getLocation();
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        EnvironmentManager envManager = plugin.getEnvironmentManager();
        
        // Launch block projectiles at nearby enemies
        com.muzlik.block.BlockManipulationEngine blockEngine = plugin.getBlockManipulationEngine();
        int blocksLaunched = 0;
        int maxBlocks = 3 + rank; // 3-6 blocks based on rank
        
        for (Entity entity : player.getWorld().getNearbyEntities(center, radius, 3, radius)) {
            if (entity instanceof LivingEntity && entity != player && blocksLaunched < maxBlocks) {
                LivingEntity target = (LivingEntity) entity;
                
                // Calculate position near target
                Location targetLoc = target.getLocation();
                Location spawnLoc = targetLoc.clone().subtract(0, 1, 0); // Below target
                
                // Calculate direction from spawn to target
                org.bukkit.util.Vector direction = targetLoc.toVector().subtract(spawnLoc.toVector()).normalize();
                direction.setY(0.8); // Arc upward
                
                // Create block controller config
                com.muzlik.block.BlockControllerConfig config = new com.muzlik.block.BlockControllerConfig.Builder()
                    .ownerUUID(player.getUniqueId())
                    .abilityId("earth_seismic_slam")
                    .startLocation(spawnLoc)
                    .direction(direction)
                    .baseSpeed(0.3)
                    .accelerationFactor(1.05)
                    .maxSpeed(1.2)
                    .maxLifeTicks(40) // 2 seconds
                    .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
                    .blockType(Material.STONE)
                    .collisionRadius(0.8)
                    .baseDamage(damage)
                    .fragmentRank(rank)
                    .build();
                
                // Spawn block projectile
                blockEngine.spawnController(config);
                blocksLaunched++;
                
                // Also apply slow effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
            }
        }
        
        // Create temporary raised ledge effect (ring of blocks)
        int ledgeDuration = 16; // 0.8 seconds
        for (double angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * (radius * 0.7);
            double z = Math.sin(radians) * (radius * 0.7);
            
            Location blockLoc = center.clone().add(x, 0, z);
            Block block = blockLoc.getBlock();
            
            if (block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType().isSolid()) {
                envManager.placeTemporaryBlock(blockLoc, Material.STONE, ledgeDuration, player);
            }
        }
        
        // 5-Layer VFX System
        com.muzlik.vfx.VFXLayerBuilder vfxBuilder = new com.muzlik.vfx.VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: SMOKE shockwave
            .core(Particle.SMOKE_LARGE, 150, com.muzlik.vfx.ParticlePattern.RING, radius, 0.5, radius, 0.1, null)
            // Secondary: CLOUD wave
            .secondary(Particle.CLOUD, 100, com.muzlik.vfx.ParticlePattern.BURST, radius, 0.2, radius, 0.05, null)
            // Ambient: Ground rumble
            .ambient(Particle.SMOKE_NORMAL, 80, com.muzlik.vfx.ParticlePattern.SPHERE, radius * 0.8, 1, radius * 0.8, 0.02, null)
            // Impact: Debris burst
            .impact(Particle.ITEM_CRACK, 60, com.muzlik.vfx.ParticlePattern.BURST, radius, 1, radius, 0.15, new ItemStack(Material.COBBLESTONE))
            // Cinematic: Ground shake
            .cinematic(0.25, com.muzlik.vfx.CinematicEffect.SCREEN_SHAKE);
        
        vfxBuilder.spawn();
        
        // Sound: Explosion + rumble
        player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        player.getWorld().playSound(center, Sound.ENTITY_RAVAGER_ROAR, 1.5f, 0.3f);
        
    }
}
