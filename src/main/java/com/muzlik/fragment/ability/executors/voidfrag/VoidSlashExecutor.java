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
                    
                    // VFX on hit - FIXED: Use particles that don't require data
                    living.getWorld().spawnParticle(Particle.PORTAL, living.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.2);
                    living.getWorld().spawnParticle(Particle.SPELL_MOB, living.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                    living.getWorld().spawnParticle(Particle.END_ROD, living.getLocation().add(0, 1, 0), 10, 0.2, 0.4, 0.2, 0.03);
                    
                    hitCount++;
                }
            }
        }
        
        // Slash VFX - arc in front of player - FIXED: Use safe particles
        for (double d = 0; d < range; d += 0.3) {
            for (double angle = -30; angle <= 30; angle += 10) {
                Vector slashDir = direction.clone();
                slashDir = rotateAroundY(slashDir, Math.toRadians(angle));
                Location particleLoc = origin.clone().add(slashDir.multiply(d));
                
                player.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 3, 0.1, 0.1, 0.1, 0.5);
                player.getWorld().spawnParticle(Particle.SPELL_MOB, particleLoc, 2, 0.1, 0.1, 0.1, 0.3);
                player.getWorld().spawnParticle(Particle.SMOKE_LARGE, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        
        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.7f);
        
        player.sendMessage("§5🌀 Void Slash! §7(" + hitCount + " hit)");
    }
    
    private Vector rotateAroundY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z);
    }
}
