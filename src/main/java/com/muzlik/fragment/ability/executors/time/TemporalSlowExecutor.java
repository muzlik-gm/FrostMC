package com.muzlik.fragment.ability.executors.time;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.fragment.ability.TargetingUtil;
import com.muzlik.util.PotionEffectHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Temporal Slow - Time Fragment Primary Ability
 * Slows down target entity with time distortion effects
 * 
 * Stats:
 * - Mana Cost: 30 - 5*rank
 * - Cooldown: 18 - 1.5*rank seconds
 * - Duration: 5 + rank seconds
 * - Range: 20 blocks
 * - Slowness Amplifier: rank/2 (rounded down)
 * 
 * VFX: Yellow clock particles spiraling around target
 * Sound: BLOCK_PORTAL_AMBIENT at low pitch (0.5f)
 */
public class TemporalSlowExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Raycast to find target entity within 20 blocks
        LivingEntity target = TargetingUtil.getTargetEntity(player, 20.0);
        
        if (target == null) {
            player.sendMessage("§eNo target found!");
            return;
        }
        
        // Calculate duration and amplifier based on rank
        int duration = (5 + rank) * 20; // Convert to ticks
        int amplifier = rank / 2; // Rounded down
        
        // Apply Slowness effect with hidden particles
        target.addPotionEffect(PotionEffectHelper.createHiddenEffect(
            PotionEffectType.SLOW,
            duration,
            amplifier
        ));
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn yellow clock particle effects around target
        Location targetLoc = target.getLocation().add(0, 1, 0);
        
        // Create particle effect task
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || target.isDead() || !target.isValid()) {
                    cancel();
                    return;
                }
                
                // Spawn yellow clock particles in a spiral pattern
                double angle = (ticks * 0.3) % (2 * Math.PI);
                double radius = 1.0 + Math.sin(ticks * 0.1) * 0.3;
                double x = targetLoc.getX() + Math.cos(angle) * radius;
                double y = targetLoc.getY() + (ticks % 20) * 0.1;
                double z = targetLoc.getZ() + Math.sin(angle) * radius;
                
                Location particleLoc = new Location(targetLoc.getWorld(), x, y, z);
                
                // Yellow dust particles (RGB: 255, 255, 0)
                targetLoc.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    particleLoc,
                    1,
                    0, 0, 0,
                    0,
                    new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(255, 255, 0),
                        1.0f
                    )
                );
                
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        // Play time-distortion sound effect
        player.getWorld().playSound(
            targetLoc,
            Sound.BLOCK_PORTAL_AMBIENT,
            1.0f,
            0.5f // Low pitch for slow effect
        );
    }
}
