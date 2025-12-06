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
        double baseDamage = 6.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Cone parameters scale with rank
        double baseRange = 12.0;
        double range = baseRange + (rank * 1.5); // 12 → 24 at rank 8
        double coneWidth = 2.0 + (rank * 0.3); // 2 → 4.4 at rank 8
        
        java.util.Set<java.util.UUID> hitEntities = new java.util.HashSet<>();
        
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
            
            // QUALITY VFX - reduced counts, focused pattern
            // Core flame - main visual indicator of breath
            int flameCount = 10 + (rank * 3);      // 10 → 34 at rank 8 (was 20 → 84)
            checkLoc.getWorld().spawnParticle(Particle.FLAME, checkLoc, flameCount, currentWidth * 0.6, currentWidth * 0.6, currentWidth * 0.6, 0.05 + (rank * 0.01));
            
            // Lava accents - only on alternating segments for visual rhythm
            if (i % 2 == 0) {
                int lavaCount = 4 + (rank * 2);    // 4 → 20 at rank 8 (was 10 → 50)
                checkLoc.getWorld().spawnParticle(Particle.LAVA, checkLoc, lavaCount, currentWidth * 0.5, currentWidth * 0.5, currentWidth * 0.5, 0.03);
            }
            
            // Smoke trail - subtle depth, only every 3rd segment
            if (i % 3 == 0) {
                int smokeCount = 5 + rank;         // 5 → 13 at rank 8 (was 15 → 63)
                checkLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, checkLoc, smokeCount, currentWidth * 0.4, currentWidth * 0.4, currentWidth * 0.4, 0.02);
            }
            
            // MEANINGFUL HIGH-RANK VFX - Dragon breath particles only at rank 5+
            // These are special particles that indicate mastery of the ability
            if (rank >= 5 && i % 2 == 0) {
                int dragonCount = (rank - 4) * 2;  // 2 → 8 at rank 8 (was rank*2 = up to 16)
                checkLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, checkLoc, dragonCount, currentWidth * 0.7, currentWidth * 0.7, currentWidth * 0.7, 0.01);
            }
        }
        
        // Sound - more intense at higher ranks
        float volume = 2.0f + (rank * 0.2f);
        float pitch = 0.8f - (rank * 0.03f); // Lower pitch at higher ranks (more menacing)
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, volume, pitch);
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_BLAZE_SHOOT, volume * 0.8f, 0.6f);
        
    }
}
