package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;

/**
 * Apocalypse - Admin Fragment Slot 8 (Ultimate)
 * 60-block radius devastation
 * 80 damage to players (40 hearts)
 * Instant kill for all mobs
 * Massive screen shake and effects
 */
public class ApocalypseExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        World world = center.getWorld();
        double radius = 60.0;
        
        // Damage all entities
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                
                if (entity instanceof Player) {
                    living.damage(80.0, player); // 40 hearts
                    // Screen shake effect (CONFUSION is the new name for NAUSEA in 1.20.4+)
                    ((Player) entity).addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.CONFUSION, 200, 1));
                } else {
                    living.setHealth(0); // Instant kill mobs
                }
                
                living.setFireTicks(200);
            }
        }
        
        // MASSIVE VFX - multi-layered
        // Layer 1: Core explosion
        world.spawnParticle(Particle.EXPLOSION_HUGE, center, 50, radius * 0.2, radius * 0.2, radius * 0.2, 0);
        
        // Layer 2: Fire ring
        for (int i = 0; i < 360; i += 5) {
            double angle = Math.toRadians(i);
            for (double r = 0; r < radius; r += 5) {
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                Location particleLoc = center.clone().add(x, 0, z);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 10, 0.5, 1, 0.5, 0.1);
            }
        }
        
        // Layer 3: Smoke pillars
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = Math.random() * radius;
            double x = Math.cos(angle) * dist;
            double z = Math.sin(angle) * dist;
            Location pillarLoc = center.clone().add(x, 0, z);
            
            for (int y = 0; y < 30; y += 2) {
                Location smokeLoc = pillarLoc.clone().add(0, y, 0);
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, smokeLoc, 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
        
        // Layer 4: Lava rain
        world.spawnParticle(Particle.LAVA, center, 500, radius * 0.5, radius * 0.3, radius * 0.5, 0);
        
        // Layer 5: Dark energy
        world.spawnParticle(Particle.REVERSE_PORTAL, center, 1000, radius * 0.6, radius * 0.4, radius * 0.6, 0.5);
        
        // Layer 6: Lightning strikes
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = Math.random() * radius;
            double x = Math.cos(angle) * dist;
            double z = Math.sin(angle) * dist;
            Location strikeLoc = center.clone().add(x, 0, z);
            
            int delay = (int) (Math.random() * 40);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                world.strikeLightningEffect(strikeLoc);
            }, delay);
        }
        
        // Sound - MASSIVE
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 3.0f, 0.5f);
        world.playSound(center, Sound.ENTITY_WITHER_DEATH, 3.0f, 0.5f);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.3f);
        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 0.5f);
        
        // Broadcast message
        Bukkit.broadcastMessage("§4§l§k|||§r §4§lAPOCALYPSE UNLEASHED§r §4§l§k|||");
        Bukkit.broadcastMessage("§c" + player.getName() + " has brought devastation!");
    }
}
