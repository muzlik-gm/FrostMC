package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Temporal Freeze - Admin Fragment Slot 5
 * Freezes all entities within 40 blocks for 6 seconds
 * Player moves normally, everyone else is frozen
 */
public class TemporalFreezeExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        double radius = 40.0;
        int duration = 120; // 6 seconds
        
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        Set<UUID> frozenEntities = new HashSet<>();
        
        // Freeze all entities
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                frozenEntities.add(entity.getUniqueId());
            }
        }
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                for (UUID uuid : frozenEntities) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        
                        // Complete freeze
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 255, false, false));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 25, 255, false, false));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 25, 255, false, false));
                        living.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                        
                        // Frozen particles - around entity, not blocking view
                        Location entityLoc = living.getLocation().add(0, 1, 0);
                        living.getWorld().spawnParticle(Particle.SNOWFLAKE, entityLoc, 3, 0.3, 0.5, 0.3, 0);
                        living.getWorld().spawnParticle(Particle.END_ROD, entityLoc, 1, 0.2, 0.3, 0.2, 0);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // VFX - time freeze wave
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particleLoc = center.clone().add(x, 1, z);
            center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
        }
        
        // Sound
        player.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
        player.getWorld().playSound(center, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 1.5f);
    }
}
