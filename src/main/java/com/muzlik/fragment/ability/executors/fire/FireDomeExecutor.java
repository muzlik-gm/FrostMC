package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import com.muzlik.vfx.cinematic.ability.FireAbilityVFX;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Fire Dome - Fire Fragment Secondary Ability
 * Creates a protective dome of fire around the player
 * Burns enemies that get too close
 * 
 * VFX: Cinematic geodesic hexagon dome with flame edges and rising embers
 */
public class FireDomeExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Scale parameters
        double baseDuration = 6.0;
        double duration = context.getScalingEngine().scaleDuration(baseDuration, rank);
        int durationTicks = (int) (duration * 20);
        
        double baseRadius = 6.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        
        Location center = player.getLocation();
        
        // Apply fire resistance to player
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.FIRE_RESISTANCE, durationTicks, 0));
        
        // Use cinematic VFX system
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        FireAbilityVFX fireVFX = new FireAbilityVFX(plugin);
        
        // Fire Dome cinematic VFX: geodesic dome with flame edges
        fireVFX.fireDome(player, center, radius, durationTicks, rank);
        
        // Damage logic
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                ticks++;
                
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                Location playerLoc = player.getLocation();
                
                // Damage nearby enemies every 10 ticks (0.5 seconds)
                if (ticks % 10 == 0) {
                    for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;
                            double distance = target.getLocation().distance(playerLoc);
                            
                            if (distance <= radius) {
                                target.damage(1.0, player);
                                target.setFireTicks(20);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
