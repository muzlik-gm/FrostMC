package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Void Chains - Admin Fragment Slot 3
 * Immobilizes all entities within 30 blocks
 * Pulls them toward caster slowly
 * 5 damage per second for 8 seconds
 */
public class VoidChainsExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location center = player.getLocation();
        double radius = 30.0;
        int duration = 160; // 8 seconds
        
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                Location playerLoc = player.getLocation();
                
                for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity living = (LivingEntity) entity;
                        
                        // Immobilize
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 10, false, false));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 25, 250, false, false));
                        
                        // Pull toward player
                        Vector pullDir = playerLoc.toVector().subtract(living.getLocation().toVector()).normalize();
                        living.setVelocity(pullDir.multiply(0.3));
                        
                        // Damage every second
                        if (ticks % 20 == 0) {
                            living.damage(5.0, player);
                        }
                        
                        // Chain particles - from entity to player, not blocking view
                        Location chainStart = living.getLocation().add(0, 1, 0);
                        Vector chainDir = playerLoc.toVector().subtract(chainStart.toVector());
                        double chainLength = chainDir.length();
                        chainDir.normalize();
                        
                        for (double d = 0; d < chainLength; d += 1.0) {
                            Location chainLoc = chainStart.clone().add(chainDir.clone().multiply(d));
                            playerLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, chainLoc, 1, 0.05, 0.05, 0.05, 0);
                        }
                    }
                }
                
                // Aura particles around player - at shoulder level, not blocking view
                Location auraLoc = playerLoc.clone().add(0, 1.5, 0);
                for (int i = 0; i < 8; i++) {
                    double angle = (ticks * 0.1) + (i * Math.PI * 2 / 8);
                    double x = Math.cos(angle) * 1.5;
                    double z = Math.sin(angle) * 1.5;
                    Location particleLoc = auraLoc.clone().add(x, 0, z);
                    playerLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        player.getWorld().playSound(center, Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.5f);
    }
}
