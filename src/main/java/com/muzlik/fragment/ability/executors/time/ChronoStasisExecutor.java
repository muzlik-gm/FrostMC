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
import org.bukkit.util.Vector;

/**
 * Chrono Stasis - Time Fragment Mastery Ability
 * Freezes target entities in time, preventing all movement
 * 
 * Stats:
 * - Mana Cost: 50 - 5*rank
 * - Cooldown: 30 - 2*rank seconds
 * - Duration: 3 + 0.5*rank seconds
 * - Range: 15 blocks
 * - Max Targets: 1 + (rank/3, rounded down)
 * 
 * VFX: Yellow particle cage around frozen entity
 * Sound: BLOCK_GLASS_BREAK on unfreeze
 */
public class ChronoStasisExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Raycast to find target entity within 15 blocks
        LivingEntity target = TargetingUtil.getTargetEntity(player, 15.0);
        
        if (target == null) {
            player.sendMessage("§eNo target found!");
            return;
        }
        
        // Calculate duration based on rank
        int durationTicks = (int) ((3.0 + (0.5 * rank)) * 20); // Convert to ticks
        
        // Apply Slowness 255 effect with hidden particles (complete freeze)
        target.addPotionEffect(PotionEffectHelper.createHiddenEffect(
            PotionEffectType.SLOW,
            durationTicks,
            255 // Maximum slowness
        ));
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create BukkitRunnable to set entity velocity to zero every tick
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || target.isDead() || !target.isValid()) {
                    // Play unfreeze sound
                    target.getWorld().playSound(
                        target.getLocation(),
                        Sound.BLOCK_GLASS_BREAK,
                        1.0f,
                        1.0f
                    );
                    cancel();
                    return;
                }
                
                // Set velocity to zero to prevent all movement
                target.setVelocity(new Vector(0, 0, 0));
                
                // Spawn yellow particle cage around frozen entity
                if (ticks % 5 == 0) { // Every 5 ticks for performance
                    Location targetLoc = target.getLocation().add(0, 1, 0);
                    
                    // Create cage effect with particles
                    double radius = 0.8;
                    int points = 12;
                    
                    for (int i = 0; i < points; i++) {
                        double angle = (2 * Math.PI * i) / points;
                        double x = targetLoc.getX() + Math.cos(angle) * radius;
                        double z = targetLoc.getZ() + Math.sin(angle) * radius;
                        
                        // Vertical lines
                        for (double y = -0.5; y <= 1.5; y += 0.3) {
                            Location particleLoc = new Location(
                                targetLoc.getWorld(),
                                x,
                                targetLoc.getY() + y,
                                z
                            );
                            
                            // Yellow dust particles (RGB: 255, 255, 0)
                            targetLoc.getWorld().spawnParticle(
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
                        }
                    }
                    
                    // Horizontal rings
                    for (double y = 0; y <= 1.0; y += 0.5) {
                        for (int i = 0; i < points * 2; i++) {
                            double angle = (2 * Math.PI * i) / (points * 2);
                            double x = targetLoc.getX() + Math.cos(angle) * radius;
                            double z = targetLoc.getZ() + Math.sin(angle) * radius;
                            
                            Location particleLoc = new Location(
                                targetLoc.getWorld(),
                                x,
                                targetLoc.getY() + y,
                                z
                            );
                            
                            targetLoc.getWorld().spawnParticle(
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
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick
        
        // Play freeze sound
        player.getWorld().playSound(
            target.getLocation(),
            Sound.BLOCK_PORTAL_AMBIENT,
            1.0f,
            0.3f // Very low pitch for frozen effect
        );
        
        player.sendMessage("§eTarget frozen in time!");
    }
}
