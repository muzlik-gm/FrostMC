package com.muzlik.fragment.ability.executors.voidfrag;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Void Slash - Void Fragment Primary (Slot 0)
 * Tears space dealing true damage in a cone
 * FIXED: Proper cone targeting and VFX
 */
public class VoidSlashExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 5.0; // EXTREME NERF: 16.0 → 5.0 (2.5 hearts, primary true damage cone)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        double range = 8.0 + (rank * 0.5);
        
        Vector direction = player.getLocation().getDirection();
        Location origin = player.getEyeLocation();
        
        int hitCount = 0;
        
        // Cone attack in front of player
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                
                // Check if entity is in front of player (cone)
                Vector toEntity = entity.getLocation().toVector().subtract(origin.toVector()).normalize();
                double dot = toEntity.dot(direction);
                
                if (dot > 0.7) { // ~45 degree cone
                    // Deal true damage (ignores armor)
                    living.damage(damage, player);
                    
                    // FOCUSED hit VFX - clean void impact
                    // Reduced counts for primary ability
                    int portalCount = 10 + (rank * 2);    // 10 → 18 at rank 4 (was 30 constant)
                    int spellCount = 5 + rank;            // 5 → 9 at rank 4 (was 15 constant)
                    int rodCount = 3 + rank;              // 3 → 7 at rank 4 (was 10 constant)
                    
                    living.getWorld().spawnParticle(Particle.PORTAL, living.getLocation().add(0, 1, 0), portalCount + spellCount, 0.2, 0.4, 0.2, 0.1);
                    living.getWorld().spawnParticle(Particle.END_ROD, living.getLocation().add(0, 1, 0), rodCount, 0.15, 0.3, 0.15, 0.02);
                    
                    hitCount++;
                }
            }
        }
        
        // FOCUSED slash VFX - clean arc with reduced particle density
        // Increased step sizes for cleaner visual with less particles
        // FIXED: Smoke particles spawn further away to not block eyesight
        for (double d = 0; d < range; d += 0.5) {  // Was 0.3, now 0.5 (60% fewer iterations)
            for (double angle = -30; angle <= 30; angle += 15) {  // Was 10, now 15 (50% fewer iterations)
                Vector slashDir = direction.clone();
                slashDir = rotateAroundY(slashDir, Math.toRadians(angle));
                Location particleLoc = origin.clone().add(slashDir.multiply(d));
                
                // Minimal particles per position - rift aesthetic
                player.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 3, 0.05, 0.05, 0.05, 0.3);
                // Smoke only at distance (not close to player) and every other position
                if (d > 2.0 && d % 1.0 < 0.5) { // Only spawn smoke 2+ blocks away
                    player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 1, 0.03, 0.03, 0.03, 0.005);
                }
            }
        }
        
        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.7f);
        
    }
    
    private Vector rotateAroundY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z);
    }
}
