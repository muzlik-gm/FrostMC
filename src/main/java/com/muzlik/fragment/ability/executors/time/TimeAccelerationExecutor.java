package com.muzlik.fragment.ability.executors.time;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Time Acceleration - Time Fragment Secondary Ability
 * Grants speed and haste to the player with time acceleration effects
 * 
 * Stats:
 * - Mana Cost: 40 - 5*rank
 * - Cooldown: 20 - 2*rank seconds
 * - Duration: 8 + 2*rank seconds
 * - Speed Amplifier: 1 + (rank/4, rounded down)
 * - Haste Amplifier: 1 + (rank/4, rounded down)
 * 
 * VFX: Yellow speed-line particles trailing behind player
 * Sound: ENTITY_PHANTOM_FLAP at high pitch (1.5f)
 */
public class TimeAccelerationExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate duration and amplifiers based on rank
        int duration = (8 + (2 * rank)) * 20; // Convert to ticks
        int speedAmplifier = 1 + (rank / 4); // Rounded down
        int hasteAmplifier = 1 + (rank / 4); // Rounded down
        
        // Apply Speed and Haste effects with hidden particles
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(
            PotionEffectType.SPEED,
            duration,
            speedAmplifier
        ));
        
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(
            PotionEffectType.FAST_DIGGING,
            duration,
            hasteAmplifier
        ));
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn yellow speed-line particles trailing behind player
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || player.isDead() || !player.isValid()) {
                    cancel();
                    return;
                }
                
                // Get player's current location
                Location playerLoc = player.getLocation().add(0, 1, 0);
                
                // Spawn speed-line particles behind the player
                // Create multiple lines radiating from behind
                for (int i = 0; i < 3; i++) {
                    double offsetX = (Math.random() - 0.5) * 0.5;
                    double offsetY = (Math.random() - 0.5) * 0.5;
                    double offsetZ = (Math.random() - 0.5) * 0.5;
                    
                    // Get direction opposite to player's movement
                    Location particleLoc = playerLoc.clone().add(offsetX, offsetY, offsetZ);
                    
                    // Yellow dust particles (RGB: 255, 255, 0)
                    player.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        particleLoc,
                        1,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(
                            org.bukkit.Color.fromRGB(255, 255, 0),
                            0.8f
                        )
                    );
                    
                    // Add some END_ROD particles for shimmer effect
                    player.getWorld().spawnParticle(
                        Particle.END_ROD,
                        particleLoc,
                        1,
                        0.1, 0.1, 0.1,
                        0.02
                    );
                }
                
                ticks += 3;
            }
        }.runTaskTimer(plugin, 0L, 3L);
        
        // Play acceleration sound effect
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_PHANTOM_FLAP,
            1.0f,
            1.5f // High pitch for speed effect
        );
    }
}
