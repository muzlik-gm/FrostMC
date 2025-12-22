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
 * Shoots 3 explosive fireballs in your aim direction with slight angle variations
 * Each fireball explodes on impact, dealing damage and knockback to nearby entities
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
        Location loc = player.getLocation();
        int rank = context.getRank();
        double baseDamage = 18.0; // Increased from 10.0
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        double explosionRadius = 6.0; // Increased from 4.0
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Get player's aim direction
        Vector playerDirection = player.getLocation().getDirection().normalize();
        
        // Launch 3 fireballs with slight angle variations
        double[] angleOffsets = {-10.0, 0.0, 10.0}; // Left, center, right
        
        for (int i = 0; i < 3; i++) {
            // Calculate direction with angle offset
            double spreadAngle = angleOffsets[i];
            double radians = Math.toRadians(spreadAngle);
            
            // Rotate player's direction by spread angle (horizontal rotation)
            Vector direction = playerDirection.clone();
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double x = direction.getX() * cos - direction.getZ() * sin;
            double z = direction.getX() * sin + direction.getZ() * cos;
            direction.setX(x).setZ(z).normalize();
            
            // Spawn fireball slightly in front of player
            Location spawnLoc = loc.clone().add(0, 1.5, 0).add(playerDirection.clone().multiply(2.0));
            Fireball fireball = player.getWorld().spawn(spawnLoc, Fireball.class);
            fireball.setDirection(direction.multiply(1.5)); // Set velocity
            fireball.setYield(0.0f); // No block damage - we handle explosion manually
            fireball.setIsIncendiary(false); // No fire spread
            fireball.setShooter(player);
            
            // Store fireball data for explosion handling
            ACTIVE_FIREBALLS.put(fireball.getUniqueId(), new FireballData(player, damage, explosionRadius));
            
            // Trail particles
            final Fireball fb = fireball;
            new org.bukkit.scheduler.BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (!fb.isValid() || ticks++ > 200) { // 10 seconds max
                        ACTIVE_FIREBALLS.remove(fb.getUniqueId());
                        cancel();
                        return;
                    }
                    // Minimal trail particles
                    fb.getWorld().spawnParticle(Particle.FLAME, fb.getLocation(), 2, 0.05, 0.05, 0.05, 0);
                    fb.getWorld().spawnParticle(Particle.SMOKE_NORMAL, fb.getLocation(), 1, 0.05, 0.05, 0.05, 0);
                }
            }.runTaskTimer(plugin, 0L, 2L);
        }
        
        // Cast VFX at player location
        loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.LAVA, loc.clone().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
        
        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
        loc.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1.5f, 0.6f);
        
        player.sendMessage("§5⚡ Dragonic Fury unleashed!");
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
