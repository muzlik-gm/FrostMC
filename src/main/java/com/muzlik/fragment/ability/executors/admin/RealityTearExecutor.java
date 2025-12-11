package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Reality Tear - Admin Fragment Slot 0
 * Massive cone attack that tears through reality
 * 50 true damage to players, instant kill for mobs
 * 30 block range, 90-degree cone
 */
public class RealityTearExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        Vector direction = player.getLocation().getDirection();
        Location origin = player.getEyeLocation();
        double range = 30.0;
        
        // Cone attack
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                
                // Check if entity is in cone (90 degrees)
                Vector toEntity = entity.getLocation().toVector().subtract(origin.toVector()).normalize();
                double dot = toEntity.dot(direction);
                
                if (dot > 0.5) { // 60 degree cone
                    if (entity instanceof Player) {
                        // 50 true damage to players (25 hearts)
                        living.damage(50.0, player);
                    } else {
                        // Instant kill for mobs
                        living.setHealth(0);
                    }
                    
                    // Hit particles - behind target, not blocking view
                    Location hitLoc = living.getLocation().add(0, 1, 0);
                    living.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, hitLoc, 30, 0.3, 0.5, 0.3, 0.1);
                    living.getWorld().spawnParticle(Particle.SMOKE_LARGE, hitLoc.clone().subtract(direction.clone().multiply(0.5)), 20, 0.2, 0.3, 0.2, 0.05);
                }
            }
        }
        
        // Tear effect - particles spawn away from player's view
        for (double d = 2; d < range; d += 1.0) {
            for (double angle = -30; angle <= 30; angle += 10) {
                Vector tearDir = direction.clone();
                tearDir = rotateAroundY(tearDir, Math.toRadians(angle));
                Location particleLoc = origin.clone().add(tearDir.multiply(d));
                
                // Particles spawn at location, not in player's face
                player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 2, 0.1, 0.1, 0.1, 0.5);
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
        
        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
    }
    
    private Vector rotateAroundY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z);
    }
}
