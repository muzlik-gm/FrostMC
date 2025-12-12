package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Fury - Dragon Fragment Advanced Ability
 * Summon 5 explosive homing fireballs that track enemies within 30 blocks
 */
public class FuryExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        int fireballCount = 5 + (rank / 2); // 5-8 fireballs
        double trackingRange = 30.0 + (rank * 5.0);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Launch fireballs in sequence
        new BukkitRunnable() {
            int launched = 0;
            
            @Override
            public void run() {
                if (launched >= fireballCount) {
                    cancel();
                    return;
                }
                
                launchHomingFireball(plugin, player, rank, trackingRange);
                launched++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Launch one every 0.25 seconds
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.5f);
    }
    
    private void launchHomingFireball(com.muzlik.FrostSMPPlugin plugin, Player player, int rank, double trackingRange) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection().normalize();
        
        // Spawn fireball
        Location spawnLoc = eyeLoc.clone().add(direction.multiply(2));
        SmallFireball fireball = eyeLoc.getWorld().spawn(spawnLoc, SmallFireball.class);
        fireball.setShooter(player);
        fireball.setVelocity(direction.multiply(1.5));
        
        // Homing behavior
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 200; // 10 second max lifetime
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !fireball.isValid()) {
                    cancel();
                    return;
                }
                
                // Find nearest enemy
                LivingEntity target = findNearestEnemy(fireball.getLocation(), trackingRange, player);
                
                if (target != null) {
                    // Calculate homing vector
                    Vector toTarget = target.getLocation().add(0, 1, 0).toVector()
                        .subtract(fireball.getLocation().toVector()).normalize();
                    
                    // Adjust velocity toward target
                    Vector currentVel = fireball.getVelocity();
                    Vector newVel = currentVel.multiply(0.7).add(toTarget.multiply(0.5));
                    fireball.setVelocity(newVel);
                }
                
                // Homing trail VFX
                if (ticks % 3 == 0) {
                    VFXLayerBuilder trailVfx = new VFXLayerBuilder(plugin, fireball.getLocation(), rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .core(Particle.FLAME, 3 + rank, ParticlePattern.POINT, 0.2, 0.2, 0.2, 0.02, null);
                    trailVfx.spawn();
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private LivingEntity findNearestEnemy(Location center, double range, Player player) {
        LivingEntity nearest = null;
        double nearestDistance = range;
        
        for (Entity entity : center.getWorld().getNearbyEntities(center, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                double distance = entity.getLocation().distance(center);
                if (distance < nearestDistance) {
                    nearest = (LivingEntity) entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearest;
    }
}