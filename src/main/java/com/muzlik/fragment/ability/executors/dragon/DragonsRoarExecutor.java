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
        
        // Create dragon breath cone - MORE CHAOTIC AT HIGHER RANKS
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
                    
                    // Hit VFX
                    checkLoc.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), 10 + rank * 3, 0.3, 0.5, 0.3, 0.1);
                }
            }
            
            // VFX - MORE PARTICLES AT HIGHER RANKS
            int flameCount = 20 + (rank * 8); // 20 → 84 at rank 8
            int lavaCount = 10 + (rank * 5); // 10 → 50 at rank 8
            int smokeCount = 15 + (rank * 6); // 15 → 63 at rank 8
            
            // Core flame particles
            checkLoc.getWorld().spawnParticle(Particle.FLAME, checkLoc, flameCount, currentWidth, currentWidth, currentWidth, 0.1 + (rank * 0.02));
            
            // Lava particles for dragon intensity
            if (i % 2 == 0) {
                checkLoc.getWorld().spawnParticle(Particle.LAVA, checkLoc, lavaCount, currentWidth * 0.8, currentWidth * 0.8, currentWidth * 0.8, 0.05);
            }
            
            // Smoke trail
            if (i % 3 == 0) {
                checkLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, checkLoc, smokeCount, currentWidth * 0.6, currentWidth * 0.6, currentWidth * 0.6, 0.03);
            }
            
            // Dragon breath particles at higher ranks
            if (rank >= 5 && i % 2 == 0) {
                checkLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, checkLoc, rank * 2, currentWidth, currentWidth, currentWidth, 0.02);
            }
        }
        
        // Sound - more intense at higher ranks
        float volume = 2.0f + (rank * 0.2f);
        float pitch = 0.8f - (rank * 0.03f); // Lower pitch at higher ranks (more menacing)
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, volume, pitch);
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_BLAZE_SHOOT, volume * 0.8f, 0.6f);
        
        player.sendMessage("§c🐉 Dragon's Roar! §7(Rank " + rank + ", " + hitEntities.size() + " enemies hit)");
    }
}
