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

public class SlamExecutor implements AbilityExecutor {
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
                    .abilityId("earth_slam")
                    .startLocation(spawnLoc)
                    .direction(direction)
                    .baseSpeed(0.3)
                    .accelerationFactor(1.05)
                    .maxSpeed(1.2)
                    .maxLifeTicks(80) // 4 seconds
                    .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
                    .blockType(Material.STONE)
                    .collisionRadius(1.2)
                    .baseDamage(damage)
                    .fragmentRank(rank)
                    .build();
                
                // Spawn block projectile with delay
                int delay = blocksLaunched * 5; // Stagger launches
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    blockEngine.spawnController(config);
                }, delay);
                
                blocksLaunched++;
            }
        }
        
        // Damage and stun all nearby entities
        for (Entity entity : player.getWorld().getNearbyEntities(center, radius, 3, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 4)); // 2s stun
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 128)); // Prevent jumping
            }
        }
        
        // Ground effects - create temporary cracks
        if (envManager != null) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (Math.random() < 0.4) {
                        Location crackLoc = center.clone().add(x, -1, z);
                        Block block = crackLoc.getBlock();
                        if (block.getType().isSolid()) {
                            try {
                                envManager.placeTemporaryBlock(crackLoc.clone().add(0, 1, 0), Material.COBBLESTONE, 100, player);
                            } catch (Exception e) {
                                // Silently fail
                            }
                        }
                    }
                }
            }
        }
        
        // VFX and Sound
        center.getWorld().spawnParticle(Particle.BLOCK_CRACK, center, 100, radius, 1, radius, 0.1, Material.STONE.createBlockData());
        center.getWorld().spawnParticle(Particle.ITEM_CRACK, center, 60, radius * 0.8, 0.5, radius * 0.8, 0.05, new ItemStack(Material.COBBLESTONE));
        
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        center.getWorld().playSound(center, Sound.BLOCK_STONE_BREAK, 2.0f, 0.8f);
    }
}