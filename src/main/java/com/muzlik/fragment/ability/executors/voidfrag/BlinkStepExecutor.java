package com.muzlik.fragment.ability.executors.voidfrag;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.SafeTeleport;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
            // VFX at departure - FIXED: Smoke spawns behind player, not in face
            Location startLoc = player.getLocation();
            // Get direction player is facing and spawn smoke BEHIND them
            Vector behindPlayer = player.getLocation().getDirection().multiply(-1.0);
            Location smokeLoc = startLoc.clone().add(behindPlayer).add(0, 1, 0);
            
            player.getWorld().spawnParticle(Particle.PORTAL, startLoc, 80, 1, 1, 1, 0.5);
            player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, smokeLoc, 20, 0.8, 0.8, 0.8, 0.05);
            player.getWorld().spawnParticle(Particle.END_ROD, startLoc, 15, 0.6, 0.6, 0.6, 0.1);
            
            // VFX at arrival - smoke spawns around, not in center
            player.getWorld().spawnParticle(Particle.PORTAL, target, 80, 1, 1, 1, 0.5);
            player.getWorld().spawnParticle(Particle.END_ROD, target, 15, 0.5, 0.5, 0.5, 0.3);
            
            // Sound
            player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            player.getWorld().playSound(target, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
            
        } else {
            player.sendMessage("§c✗ Cannot teleport there!");
        }
    }
}
