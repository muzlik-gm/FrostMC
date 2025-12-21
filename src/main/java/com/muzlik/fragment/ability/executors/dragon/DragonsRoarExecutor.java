package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class DragonsRoarExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        int rank = context.getRank();
        Vector direction = context.getDirection().clone().normalize();
        double baseDamage = 12.0; // Increased from 8.0 - more destructive
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Cone parameters scale with rank - WIDER and LONGER
        double baseRange = 20.0; // Increased from 15.0
        double range = baseRange + (rank * 2.0); // 20 → 36 at rank 8
        double coneWidth = 3.0 + (rank * 0.5); // 3 → 7 at rank 8
        
        java.util.Set<java.util.UUID> hitEntities = new java.util.HashSet<>();
        
        // Get plugin and block manipulation engine
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        if (plugin != null && plugin.getBlockManipulationEngine() != null) {
            com.muzlik.block.BlockManipulationEngine blockEngine = plugin.getBlockManipulationEngine();
            
            // Spawn 4-7 magma/netherrack blocks shooting forward in the breath cone
            int blockCount = 4 + (rank / 2); // 4-8 blocks based on rank
            
            for (int i = 0; i < blockCount; i++) {
                // Spread blocks across the cone width
                double spreadAngle = (Math.random() - 0.5) * Math.toRadians(30); // ±15 degrees
                Vector spreadDir = direction.clone();
                
                // Rotate direction for spread
                double cos = Math.cos(spreadAngle);
                double sin = Math.sin(spreadAngle);
                double x = spreadDir.getX() * cos - spreadDir.getZ() * sin;
                double z = spreadDir.getX() * sin + spreadDir.getZ() * cos;
                spreadDir.setX(x).setZ(z);
                
                Location blockLoc = eyeLoc.clone().add(direction.clone().multiply(2));
                
                // Alternate between magma and netherrack
                Material blockType = (i % 2 == 0) ? Material.MAGMA_BLOCK : Material.NETHERRACK;
                
                com.muzlik.block.BlockControllerConfig config = new com.muzlik.block.BlockControllerConfig.Builder()
                    .ownerUUID(player.getUniqueId())
                    .abilityId("dragons_roar")
                    .startLocation(blockLoc)
                    .direction(spreadDir)
                    .baseSpeed(1.2)
                    .accelerationFactor(0.08)
                    .maxSpeed(2.5)
                    .maxLifeTicks(50)
                    .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
                    .blockType(blockType)
                    .collisionRadius(1.5)
                    .baseDamage(3.0)
                    .fragmentRank(rank)
                    .build();
                
                blockEngine.spawnController(config);
            }
        }
        
        // Create dragon breath cone - MEANINGFUL, REACTIVE VFX
        // Particles scale with rank but focus on quality, not spam
        for (int i = 1; i <= range; i++) {
            Location checkLoc = eyeLoc.clone().add(direction.clone().multiply(i));
            double currentWidth = coneWidth * (i / range); // Expanding cone
            
            // Damage entities in cone
            for (Entity entity : checkLoc.getWorld().getNearbyEntities(checkLoc, currentWidth, currentWidth, currentWidth)) {
                if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity.getUniqueId())) {
                    LivingEntity target = (LivingEntity) entity;
                    target.damage(damage, player);
                    target.setFireTicks(100 + (rank * 20)); // Longer burn at higher ranks
                    hitEntities.add(entity.getUniqueId());
                    
                    // REACTIVE HIT VFX - 30% of original
                    int hitParticles = (int)((8 + (rank * 2)) * 0.3);  // 2-7 particles (30% of 8-24)
                    checkLoc.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), hitParticles, 0.2, 0.3, 0.2, 0.05);
                    // Secondary hit glow
                    checkLoc.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), Math.max(1, hitParticles / 2), 0.3, 0.4, 0.3, 0.02);
                }
            }
            
            // MINIMAL VFX - 30% of original
            // Core flame - only every 5th segment (was every 3rd)
            if (i % 5 == 0) {
                int flameCount = (int)((2 + (rank / 2)) * 0.3);  // 0-1 particles (30% of 2-6)
                if (flameCount > 0) {
                    checkLoc.getWorld().spawnParticle(Particle.FLAME, checkLoc, flameCount, currentWidth * 0.4, currentWidth * 0.4, currentWidth * 0.4, 0.03);
                }
            }
            
            // Lava accents - only every 10th segment (was every 6th)
            if (i % 10 == 0) {
                int lavaCount = (int)((1 + (rank / 3)) * 0.3);  // 0-1 particles (30% of 1-3)
                if (lavaCount > 0) {
                    checkLoc.getWorld().spawnParticle(Particle.LAVA, checkLoc, lavaCount, currentWidth * 0.3, currentWidth * 0.3, currentWidth * 0.3, 0.02);
                }
            }
            
            // Smoke trail - only every 15th segment (was every 8th)
            if (i % 15 == 0) {
                int smokeCount = (int)((1 + (rank / 4)) * 0.3);  // 0-1 particles (30% of 1-3)
                if (smokeCount > 0) {
                    checkLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, checkLoc, smokeCount, currentWidth * 0.3, currentWidth * 0.3, currentWidth * 0.3, 0.01);
                }
            }
        }
        
        // Sound - more intense at higher ranks
        float volume = 2.0f + (rank * 0.2f);
        float pitch = 0.8f - (rank * 0.03f); // Lower pitch at higher ranks (more menacing)
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, volume, pitch);
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_BLAZE_SHOOT, volume * 0.8f, 0.6f);
        
        // Magic circle at player's feet - dragon themed
        if (plugin != null) {
            VFXLayerBuilder circleVfx = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .core(Particle.FLAME, 5, ParticlePattern.POINT, 0.1, 0.1, 0.1, 0.01, null)
                .withMagicCircle(FragmentType.DRAGON, 2.5 + (rank * 0.25), 40 + (rank * 5));
            circleVfx.spawn();
        }
    }
}
