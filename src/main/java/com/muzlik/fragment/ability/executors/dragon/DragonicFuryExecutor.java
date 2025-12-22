package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dragonic Fury - Dragon Fragment Rank 9 Ability (Slot 3)
 * 
 * Rains down 8+ explosive meteors from the sky in the target area
 * Each meteor explodes on impact, dealing massive damage and knockback
 */
public class DragonicFuryExecutor implements AbilityExecutor {
    
    // Track fireballs and their damage values
    private static final Map<UUID, FireballData> ACTIVE_FIREBALLS = new HashMap<>();
    
    private static class FireballData {
        final Player shooter;
        final double damage;
        final double explosionRadius;
        
        FireballData(Player shooter, double damage, double explosionRadius) {
            this.shooter = shooter;
            this.damage = damage;
            this.explosionRadius = explosionRadius;
        }
    }
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location targetLoc = player.getTargetBlock(null, 50).getLocation().add(0, 1, 0);
        int rank = context.getRank();
        double baseDamage = 18.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        double explosionRadius = 6.0;
        double baseRadius = 10.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Warning VFX at target location
        targetLoc.getWorld().spawnParticle(Particle.FLAME, targetLoc, 50, radius * 0.5, 0.5, radius * 0.5, 0.05);
        targetLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, targetLoc, 30, radius * 0.3, 0.3, radius * 0.3, 0.03);
        targetLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, targetLoc, 20, radius * 0.4, 1.0, radius * 0.4, 0.02);
        
        // Warning sound
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.5f);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.8f);
        
        // Rain down meteors
        int meteorCount = 8 + (rank * 2); // 8-14 meteors
        
        new org.bukkit.scheduler.BukkitRunnable() {
            int meteorsSpawned = 0;
            
            @Override
            public void run() {
                if (meteorsSpawned >= meteorCount) {
                    cancel();
                    return;
                }
                
                // Random location within radius
                double angle = Math.random() * Math.PI * 2;
                double distance = Math.random() * radius;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                
                Location meteorTarget = targetLoc.clone().add(offsetX, 0, offsetZ);
                Location meteorSpawn = meteorTarget.clone().add(0, 35, 0); // Spawn 35 blocks up
                
                // Spawn fireball falling down
                Fireball meteor = meteorSpawn.getWorld().spawn(meteorSpawn, Fireball.class);
                meteor.setDirection(new Vector(0, -1, 0));
                meteor.setYield(3.5f); // Explosion power
                meteor.setIsIncendiary(false);
                meteor.setShooter(player);
                
                // Store meteor data
                ACTIVE_FIREBALLS.put(meteor.getUniqueId(), new FireballData(player, damage, explosionRadius));
                
                // Trail particles
                new org.bukkit.scheduler.BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (!meteor.isValid() || ticks++ > 70) {
                            ACTIVE_FIREBALLS.remove(meteor.getUniqueId());
                            cancel();
                            return;
                        }
                        
                        Location loc = meteor.getLocation();
                        loc.getWorld().spawnParticle(Particle.FLAME, loc, 8, 0.3, 0.3, 0.3, 0.02);
                        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 4, 0.2, 0.2, 0.2, 0.01);
                        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 3, 0.2, 0.2, 0.2, 0.01);
                        loc.getWorld().spawnParticle(Particle.LAVA, loc, 2, 0.1, 0.1, 0.1, 0);
                        
                        if (ticks % 8 == 0) {
                            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.5f);
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);
                
                meteorsSpawned++;
            }
        }.runTaskTimer(plugin, 10L, 5L); // Start after 0.5s, spawn every 0.25s
        
        player.sendMessage("§5☄ Dragonic Fury - Meteor Storm incoming!");
    }
    
    /**
     * Listener for fireball impacts - must be registered in main plugin
     */
    public static class FireballImpactListener implements Listener {
        
        @EventHandler
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!(event.getEntity() instanceof Fireball)) {
                return;
            }
            
            Fireball fireball = (Fireball) event.getEntity();
            UUID fireballId = fireball.getUniqueId();
            
            // Check if this is one of our tracked fireballs
            FireballData data = ACTIVE_FIREBALLS.remove(fireballId);
            if (data == null) {
                return;
            }
            
            Location impactLoc = fireball.getLocation();
            World world = impactLoc.getWorld();
            
            // MASSIVE Explosion VFX
            world.spawnParticle(Particle.EXPLOSION_HUGE, impactLoc, 2, 0, 0, 0, 0); // Huge explosion
            world.spawnParticle(Particle.EXPLOSION_LARGE, impactLoc, 8, 1.0, 1.0, 1.0, 0);
            world.spawnParticle(Particle.FLAME, impactLoc, 60, 2.5, 2.5, 2.5, 0.15);
            world.spawnParticle(Particle.LAVA, impactLoc, 30, 2.0, 2.0, 2.0, 0);
            world.spawnParticle(Particle.SMOKE_LARGE, impactLoc, 40, 2.0, 2.0, 2.0, 0.1);
            
            // LOUD Explosion sounds
            world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.6f);
            world.playSound(impactLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3.0f, 0.8f);
            world.playSound(impactLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            
            // ACTUAL BLOCK DESTRUCTION - Create real explosion
            // Check config to see if block breaking is enabled
            com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) Bukkit.getPluginManager().getPlugin("FrostSMP");
            boolean breakBlocks = plugin != null && plugin.getConfig().getBoolean("abilities.break_blocks", true);
            boolean createFire = plugin != null && plugin.getConfig().getBoolean("abilities.create_fire", true);
            
            // Power 3.5 = destructive but not excessive (TNT is 4.0)
            world.createExplosion(impactLoc, 3.5f, createFire, breakBlocks, data.shooter);
            
            // Damage and knockback to nearby entities
            for (Entity entity : world.getNearbyEntities(impactLoc, data.explosionRadius, data.explosionRadius, data.explosionRadius)) {
                if (!(entity instanceof LivingEntity)) {
                    continue;
                }
                
                LivingEntity target = (LivingEntity) entity;
                
                // Don't damage the shooter
                if (target.equals(data.shooter)) {
                    continue;
                }
                
                // Skip armor stands
                if (target instanceof ArmorStand) {
                    continue;
                }
                
                // Calculate distance-based damage falloff
                double distance = target.getLocation().distance(impactLoc);
                double damageMultiplier = 1.0 - (distance / data.explosionRadius);
                damageMultiplier = Math.max(0.3, damageMultiplier); // Minimum 30% damage
                
                double finalDamage = data.damage * damageMultiplier;
                
                // Apply damage
                target.damage(finalDamage, data.shooter);
                
                // Apply MASSIVE knockback
                Vector knockback = target.getLocation().toVector()
                    .subtract(impactLoc.toVector())
                    .normalize()
                    .multiply(2.5 * damageMultiplier) // Much stronger knockback
                    .setY(0.8); // Higher upward component
                
                target.setVelocity(target.getVelocity().add(knockback));
                
                // Longer fire effect
                target.setFireTicks(100); // 5 seconds of fire
            }
        }
    }
}
