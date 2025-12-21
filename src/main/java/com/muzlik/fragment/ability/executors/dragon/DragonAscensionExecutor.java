package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Dragon Ascension - Dragon Fragment Rank 10 Ability (Slot 4)
 * 
 * Ultimate transformation: Become a true dragon for 15 seconds
 * - Massive stat boosts
 * - AOE damage aura
 * - Flight
 * - Immunity to all damage
 */
public class DragonAscensionExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        int duration = 300; // 15 seconds
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Powerful but balanced buffs (under level 4)
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.INCREASE_DAMAGE, duration, 3, true)); // Strength IV
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 3, true)); // Resistance IV
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SPEED, duration, 2, true)); // Speed III
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.REGENERATION, duration, 2, true)); // Regen III
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, true));
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.ABSORPTION, duration, 2, true)); // Absorption III
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.GLOWING, duration, 0, true));
        
        // Enable flight
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // AOE damage aura task
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= duration || !player.isOnline()) {
                    // End transformation
                    if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                    player.sendMessage("§5⚡ Dragon Ascension ended");
                    cancel();
                    return;
                }
                
                // Every 20 ticks (1 second), damage nearby enemies
                if (ticks % 20 == 0) {
                    Location playerLoc = player.getLocation();
                    for (Entity entity : player.getWorld().getNearbyEntities(playerLoc, 8, 8, 8)) {
                        if (entity instanceof LivingEntity && entity != player && !(entity instanceof ArmorStand)) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(6.0, player);
                            living.setFireTicks(60);
                            
                            // Knockback
                            Vector knockback = entity.getLocation().toVector()
                                .subtract(playerLoc.toVector())
                                .normalize()
                                .multiply(0.5);
                            knockback.setY(0.3);
                            entity.setVelocity(knockback);
                        }
                    }
                    
                    // Aura VFX
                    playerLoc.getWorld().spawnParticle(Particle.FLAME, playerLoc, 30, 3, 3, 3, 0.1);
                    playerLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, playerLoc, 20, 3, 3, 3, 0.05);
                    playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5f, 1.5f);
                }
                
                // Constant trail particles
                if (ticks % 2 == 0) {
                    Location playerLoc = player.getLocation();
                    playerLoc.getWorld().spawnParticle(Particle.FLAME, playerLoc, 5, 0.5, 0.5, 0.5, 0.02);
                    playerLoc.getWorld().spawnParticle(Particle.END_ROD, playerLoc, 3, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Activation VFX
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 5, 0, 0, 0, 0);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 100, 3, 3, 3, 0.3);
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 80, 3, 3, 3, 0.2);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 2, 2, 2, 0.15);
        
        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.2f);
        
        player.sendMessage("§5⚡ §lDRAGON ASCENSION ACTIVATED!");
        player.sendMessage("§7You have become a true dragon for 15 seconds!");
    }
}
