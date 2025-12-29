package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;

/**
 * Vampiric Drain - Dark Fragment Secondary (Slot 1)
 * Drains life from target, heals caster
 * FIXED: Proper raycast targeting
 */
public class VampiricDrainExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 4.0; // EXTREME NERF: 10.0 → 4.0 (2 hearts, secondary ability with lifesteal)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Use raycast to find target
        RayTraceResult rayTrace = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getLocation().getDirection(),
            15.0,
            entity -> entity instanceof LivingEntity && entity != player
        );
        
        if (rayTrace != null && rayTrace.getHitEntity() instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) rayTrace.getHitEntity();
            
            // Deal magic damage
            livingTarget.damage(damage, player);
            
            // Heal player
            double healing = damage * 0.7;
            double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + healing);
            player.setHealth(newHealth);
            
            // VFX: Drain beam effect
            Location start = player.getEyeLocation();
            Location end = livingTarget.getEyeLocation();
            
            // Particle beam
            org.bukkit.util.Vector direction = end.toVector().subtract(start.toVector());
            double distance = direction.length();
            direction.normalize();
            
            for (double d = 0; d < distance; d += 0.5) {
                Location particleLoc = start.clone().add(direction.clone().multiply(d));
                player.getWorld().spawnParticle(Particle.SMOKE_LARGE, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                player.getWorld().spawnParticle(Particle.SQUID_INK, particleLoc, 1, 0.03, 0.03, 0.03, 0);
            }
            
            // Target VFX
            player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, livingTarget.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.SOUL, livingTarget.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
            
            // Player VFX (healing)
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0);
            
            // Sound
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 0.8f);
            player.getWorld().playSound(livingTarget.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 0.6f);
            
        } else {
            player.sendMessage("§c✗ No target found!");
        }
    }
}
