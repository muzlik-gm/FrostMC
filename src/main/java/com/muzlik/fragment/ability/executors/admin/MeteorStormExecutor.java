package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Meteor Storm - Admin Fragment Slot 6
 * Summons 15 meteors that rain down in 30-block radius
 * Each meteor: 15 damage + fire + optional crater
 */
public class MeteorStormExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = com.muzlik.fragment.ability.TargetingUtil.getTargetLocation(player, 50);
        double radius = 30.0;
        int meteorCount = 15;
        
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        boolean breakBlocks = plugin.getConfig().getBoolean("admin_fragment.break_blocks", false);
        
        new BukkitRunnable() {
            int meteorsSpawned = 0;
            
            @Override
            public void run() {
                if (meteorsSpawned >= meteorCount) {
                    cancel();
                    return;
                }
                
                // Random location in radius
                double angle = Math.random() * 2 * Math.PI;
                double dist = Math.random() * radius;
                double x = Math.cos(angle) * dist;
                double z = Math.sin(angle) * dist;
                Location meteorLoc = center.clone().add(x, 0, z);
                
                // Warning particles from sky
                Location skyLoc = meteorLoc.clone().add(0, 50, 0);
                for (int i = 0; i < 50; i += 5) {
                    Location trailLoc = skyLoc.clone().subtract(0, i, 0);
                    meteorLoc.getWorld().spawnParticle(Particle.FLAME, trailLoc, 5, 0.3, 0.3, 0.3, 0.05);
                    meteorLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, trailLoc, 3, 0.2, 0.2, 0.2, 0.02);
                }
                
                // Impact after 1 second
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Damage
                    for (Entity entity : meteorLoc.getWorld().getNearbyEntities(meteorLoc, 5, 5, 5)) {
                        if (entity instanceof LivingEntity) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(15.0, player);
                            living.setFireTicks(100);
                        }
                    }
                    
                    // Optional crater
                    if (breakBlocks) {
                        meteorLoc.getWorld().createExplosion(meteorLoc, 3.0f, true, true, player);
                    }
                    
                    // Impact VFX
                    meteorLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, meteorLoc, 5, 1, 1, 1, 0);
                    meteorLoc.getWorld().spawnParticle(Particle.FLAME, meteorLoc, 100, 2, 2, 2, 0.2);
                    meteorLoc.getWorld().spawnParticle(Particle.LAVA, meteorLoc, 50, 1.5, 1.5, 1.5, 0);
                    meteorLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, meteorLoc, 80, 2, 2, 2, 0.1);
                    
                    // Sound
                    meteorLoc.getWorld().playSound(meteorLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                    meteorLoc.getWorld().playSound(meteorLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5f, 0.5f);
                }, 20L);
                
                meteorsSpawned++;
            }
        }.runTaskTimer(plugin, 0L, 15L); // Meteor every 0.75 seconds
        
        // Sound
        player.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
    }
}
