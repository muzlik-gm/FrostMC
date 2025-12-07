package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
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
        double baseDamage = 8.0; // Increased from 6.0
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Cone parameters scale with rank
        double baseRange = 15.0; // Increased from 12.0
        double range = baseRange + (rank * 1.5); // 12 → 24 at rank 8
        double coneWidth = 2.0 + (rank * 0.3); // 2 → 4.4 at rank 8
        
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
                    
                    // REACTIVE HIT VFX - meaningful feedback when hitting enemies
                    // This VFX responds to actual damage, making it feel impactful
                    int hitParticles = 8 + (rank * 2);  // 8 → 24 at rank 8 (was 10 + rank*3 = up to 34)
                    checkLoc.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), hitParticles, 0.2, 0.3, 0.2, 0.05);
                    // Secondary hit glow - indicates fire damage
                    checkLoc.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), hitParticles / 2, 0.3, 0.4, 0.3, 0.02);
                }
            }
            
            // ULTRA MINIMAL VFX - very clean and visible without spam
            // Core flame - only every 3rd segment
            if (i % 3 == 0) {
                int flameCount = 2 + (rank / 2);      // 2 → 6 at rank 8 (reduced from 3-11)
                checkLoc.getWorld().spawnParticle(Particle.FLAME, checkLoc, flameCount, currentWidth * 0.4, currentWidth * 0.4, currentWidth * 0.4, 0.03);
            }
            
            // Lava accents - only every 6th segment
            if (i % 6 == 0) {
                int lavaCount = 1 + (rank / 3);    // 1 → 3 at rank 6 (reduced from 2-10)
                checkLoc.getWorld().spawnParticle(Particle.LAVA, checkLoc, lavaCount, currentWidth * 0.3, currentWidth * 0.3, currentWidth * 0.3, 0.02);
            }
            
            // Smoke trail - only every 8th segment
            if (i % 8 == 0) {
                int smokeCount = 1 + (rank / 4);         // 1 → 3 at rank 8 (reduced from 2-6)
                checkLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, checkLoc, smokeCount, currentWidth * 0.3, currentWidth * 0.3, currentWidth * 0.3, 0.01);
            }
        }
        
        // Sound - more intense at higher ranks
        float volume = 2.0f + (rank * 0.2f);
        float pitch = 0.8f - (rank * 0.03f); // Lower pitch at higher ranks (more menacing)
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, volume, pitch);
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_BLAZE_SHOOT, volume * 0.8f, 0.6f);
        
    }
}
