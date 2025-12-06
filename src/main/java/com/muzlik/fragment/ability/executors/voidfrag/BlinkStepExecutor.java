package com.muzlik.fragment.ability.executors.voidfrag;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.SafeTeleport;
import org.bukkit.*;
import org.bukkit.entity.Player;

/**
 * Blink Step - Void Fragment Secondary (Slot 1)
 * Instant teleport with safe location checking
 * FIXED: Safe teleportation, no suffocation or fall damage
 */
public class BlinkStepExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseRange = 15.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        
        // Calculate target location
        Location target = player.getLocation().add(context.getDirection().multiply(range));
        
        // Teleport safely
        boolean success = SafeTeleport.teleportSafely(player, target);
        
        if (success) {
            // VFX at departure - FIXED: Use safe particles
            Location startLoc = player.getLocation();
            player.getWorld().spawnParticle(Particle.PORTAL, startLoc, 50, 1, 1, 1, 0.5);
            player.getWorld().spawnParticle(Particle.SPELL_MOB, startLoc, 30, 0.5, 0.5, 0.5, 0.3);
            player.getWorld().spawnParticle(Particle.SMOKE_LARGE, startLoc, 20, 0.8, 0.8, 0.8, 0.05);
            player.getWorld().spawnParticle(Particle.END_ROD, startLoc, 15, 0.6, 0.6, 0.6, 0.1);
            
            // VFX at arrival
            player.getWorld().spawnParticle(Particle.PORTAL, target, 50, 1, 1, 1, 0.5);
            player.getWorld().spawnParticle(Particle.SPELL_MOB, target, 30, 0.5, 0.5, 0.5, 0.3);
            
            // Sound
            player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            player.getWorld().playSound(target, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
            
        } else {
            player.sendMessage("§c✗ Cannot teleport there!");
        }
    }
}
