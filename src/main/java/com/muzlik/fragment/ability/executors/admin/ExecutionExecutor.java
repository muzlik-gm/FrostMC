package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;

/**
 * Execution - Admin Fragment Slot 7
 * Point at any entity within 50 blocks for instant death
 * Works on players too
 * No projectile, instant effect
 */
public class ExecutionExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        double range = 50.0;
        
        // Raycast to find target
        RayTraceResult result = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            range,
            0.5,
            entity -> entity instanceof LivingEntity && entity != player
        );
        
        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) result.getHitEntity();
            Location targetLoc = target.getLocation().add(0, 1, 0);
            
            // Instant death
            target.setHealth(0);
            
            // Dramatic VFX - around target, not blocking player view
            targetLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, targetLoc, 100, 0.5, 1, 0.5, 0.2);
            targetLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, targetLoc, 80, 0.4, 0.8, 0.4, 0.1);
            targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 50, 0.3, 0.6, 0.3, 0.5);
            targetLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, targetLoc, 3, 0.2, 0.2, 0.2, 0);
            
            // Beam from player to target - thin line
            org.bukkit.util.Vector direction = targetLoc.toVector().subtract(player.getEyeLocation().toVector());
            double distance = direction.length();
            direction.normalize();
            
            for (double d = 0; d < distance; d += 1.0) {
                Location beamLoc = player.getEyeLocation().clone().add(direction.clone().multiply(d));
                beamLoc.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 1, 0, 0, 0, 0);
            }
            
            // Sound
            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.5f);
            
            String targetName = target instanceof Player ? ((Player) target).getName() : target.getType().name();
            player.sendMessage("§4§lEXECUTED: §c" + targetName);
        } else {
            player.sendMessage("§c✗ No target found!");
        }
    }
}
