package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.cinematic.ability.FireAbilityVFX;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

/**
 * Flame Burst - Fire Fragment Primary Ability
 * Shoots fireball that explodes on impact, ignites enemies for 5s
 * Base: 6 hearts damage, scales with rank
 * 
 * VFX: Cinematic magic circle with phoenix pattern and spiral flame trail
 */
public class FlameBurstExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        
        // Scale damage
        double baseDamage = 3.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Spawn fireball at eye location + 1.5 blocks forward
        Location spawnLoc = eyeLoc.clone().add(direction.clone().multiply(1.5));
        SmallFireball fireball = eyeLoc.getWorld().spawn(spawnLoc, SmallFireball.class);
        fireball.setShooter(player);
        fireball.setVelocity(direction.multiply(2.0 + (rank * 0.1))); // Faster at higher ranks
        
        // Use cinematic VFX system
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        FireAbilityVFX fireVFX = new FireAbilityVFX(plugin);
        
        // Flame Burst cinematic VFX: cast circle + fireball trail
        fireVFX.flameBurst(player, player.getLocation(), direction, rank);
    }
}
