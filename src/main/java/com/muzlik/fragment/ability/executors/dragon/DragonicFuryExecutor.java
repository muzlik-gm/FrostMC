package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Dragonic Fury - Dragon Fragment Rank 9 Ability (Slot 3)
 * 
 * Summons 5 explosive fireballs that home in on nearby enemies
 * Each fireball deals massive damage and creates explosions
 */
public class DragonicFuryExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        double baseDamage = 10.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Find nearby enemies
        java.util.List<LivingEntity> targets = new java.util.ArrayList<>();
        for (Entity entity : player.getWorld().getNearbyEntities(loc, 30, 30, 30)) {
            if (entity instanceof LivingEntity && entity != player && !(entity instanceof ArmorStand)) {
                targets.add((LivingEntity) entity);
            }
        }
        
        // Launch 5 homing fireballs aimed at player's look direction or nearest targets
        int fireballCount = 5;
        Vector playerDirection = player.getLocation().getDirection().normalize();
        
        for (int i = 0; i < fireballCount; i++) {
            // Start with player's aim direction, then add slight spread
            double spreadAngle = (i - 2) * 15.0; // -30, -15, 0, 15, 30 degrees spread
            double radians = Math.toRadians(spreadAngle);
            
            // Rotate player's direction by spread angle
            Vector direction = playerDirection.clone();
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double x = direction.getX() * cos - direction.getZ() * sin;
            double z = direction.getX() * sin + direction.getZ() * cos;
            direction.setX(x).setZ(z).normalize();
            
            Location spawnLoc = loc.clone().add(0, 1.5, 0).add(playerDirection.clone().multiply(1.5));
            Fireball fireball = player.getWorld().spawn(spawnLoc, Fireball.class);
            fireball.setDirection(direction);
            fireball.setYield(2.0f); // Explosion power
            fireball.setIsIncendiary(true);
            fireball.setShooter(player);
            
            // If we have targets, make fireballs home in on them
            if (!targets.isEmpty()) {
                LivingEntity target = targets.get(i % targets.size());
                final Fireball fb = fireball;
                
                // Homing task - more aggressive tracking
                new org.bukkit.scheduler.BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (!fb.isValid() || ticks++ > 100 || !target.isValid()) {
                            cancel();
                            return;
                        }
                        
                        // Calculate direction to target with stronger homing
                        Vector toTarget = target.getLocation().add(0, 1, 0).toVector()
                            .subtract(fb.getLocation().toVector())
                            .normalize()
                            .multiply(0.5); // Increased from 0.3 for better tracking
                        
                        // Blend current velocity with target direction for smooth homing
                        Vector newVelocity = fb.getVelocity().multiply(0.7).add(toTarget.multiply(0.3));
                        fb.setVelocity(newVelocity);
                        
                        // Trail particles
                        fb.getWorld().spawnParticle(Particle.FLAME, fb.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            } else {
                // No targets - fireballs fly straight in aimed direction
                final Fireball fb = fireball;
                new org.bukkit.scheduler.BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (!fb.isValid() || ticks++ > 100) {
                            cancel();
                            return;
                        }
                        // Trail particles
                        fb.getWorld().spawnParticle(Particle.FLAME, fb.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }
        
        // VFX
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 3, 1, 1, 1, 0);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 2, 2, 2, 0.2);
        loc.getWorld().spawnParticle(Particle.LAVA, loc, 30, 1.5, 1.5, 1.5, 0.1);
        
        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
        loc.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 2.0f, 0.5f);
        
        player.sendMessage("§5⚡ Dragonic Fury unleashed!");
    }
}
