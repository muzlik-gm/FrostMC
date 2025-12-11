package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.SafeTeleport;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Omnipresent Blink - Admin Fragment Slot 1
 * Teleport anywhere within 100 blocks
 * Can teleport through walls, no line-of-sight required
 * Leaves destruction trail
 */
public class OmnipresentBlinkExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = context.getDirection();
        double range = 100.0;
        
        Location start = player.getLocation();
        Location target = start.clone().add(direction.multiply(range));
        
        // Find safe location (admin can teleport anywhere)
        Location safeLoc = SafeTeleport.findSafeLocation(target);
        if (safeLoc == null) {
            safeLoc = target;
        }
        
        // Teleport
        player.teleport(safeLoc);
        
        // Destruction trail - particles spawn between start and end
        Vector trailDir = safeLoc.toVector().subtract(start.toVector());
        double distance = trailDir.length();
        trailDir.normalize();
        
        for (double d = 0; d < distance; d += 2.0) {
            Location trailLoc = start.clone().add(trailDir.clone().multiply(d));
            
            // Particles spawn at trail location, not blocking view
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, trailLoc, 5, 0.3, 0.3, 0.3, 0.05);
            player.getWorld().spawnParticle(Particle.SMOKE_LARGE, trailLoc, 3, 0.2, 0.2, 0.2, 0.02);
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, trailLoc, 2, 0.1, 0.1, 0.1, 0.3);
        }
        
        // VFX at departure and arrival - behind player
        Location departVFX = start.clone().subtract(direction.clone().multiply(1));
        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, departVFX, 3, 0.5, 0.5, 0.5, 0);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, departVFX, 50, 1, 1, 1, 0.1);
        
        Location arriveVFX = safeLoc.clone().subtract(direction.clone().multiply(1));
        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, arriveVFX, 3, 0.5, 0.5, 0.5, 0);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, arriveVFX, 50, 1, 1, 1, 0.1);
        
        // Sound
        player.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        player.getWorld().playSound(safeLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        player.getWorld().playSound(safeLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
    }
}
