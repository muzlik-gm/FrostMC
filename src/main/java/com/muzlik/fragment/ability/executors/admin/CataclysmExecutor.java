package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;

/**
 * Cataclysm - Admin Fragment Slot 2
 * Creates massive 25-block radius explosion
 * 40 damage to players, instant kill for mobs
 * Optional terrain destruction
 */
public class CataclysmExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = com.muzlik.fragment.ability.TargetingUtil.getTargetLocation(player, 50);
        World world = center.getWorld();
        double radius = 25.0;
        
        // Damage entities
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                double distance = living.getLocation().distance(center);
                
                if (distance <= radius) {
                    if (entity instanceof Player) {
                        living.damage(40.0, player); // 20 hearts
                    } else {
                        living.setHealth(0); // Instant kill mobs
                    }
                    
                    living.setFireTicks(100);
                }
            }
        }
        
        // Optional: Create explosion (set breakBlocks based on config)
        boolean breakBlocks = player.getServer().getPluginManager().getPlugin("FrostSMP")
            .getConfig().getBoolean("admin_fragment.break_blocks", false);
        
        if (breakBlocks) {
            world.createExplosion(center, 8.0f, true, true, player);
        }
        
        // Massive VFX - particles spawn at center, not blocking player view
        world.spawnParticle(Particle.EXPLOSION_HUGE, center, 20, radius * 0.3, radius * 0.3, radius * 0.3, 0);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 500, radius * 0.5, radius * 0.5, radius * 0.5, 0.2);
        world.spawnParticle(Particle.SMOKE_LARGE, center, 300, radius * 0.6, radius * 0.6, radius * 0.6, 0.1);
        world.spawnParticle(Particle.LAVA, center, 200, radius * 0.4, radius * 0.4, radius * 0.4, 0);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, center, 150, radius * 0.7, radius * 0.7, radius * 0.7, 0.05);
        
        // Sound
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.7f);
        world.playSound(center, Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
    }
}
