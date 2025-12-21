package com.muzlik.vfx.cinematic.miniblock;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spawns and manages mini block projectiles.
 * Handles projectile physics and movement.
 * 
 * Requirements: 3.1, 3.4
 */
public class MiniBlockSpawner {
    
    private final Map<UUID, MiniBlockProjectile> activeProjectiles;
    
    public MiniBlockSpawner() {
        this.activeProjectiles = new ConcurrentHashMap<>();
    }
    
    /**
     * Spawn a mini block projectile
     * 
     * @param config Configuration for the mini block
     * @return UUID of spawned mini block
     */
    public UUID spawnMiniBlock(MiniBlockConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        
        // Spawn armor stand with scaled block
        ArmorStand armorStand = ScaledArmorStand.spawnMiniBlock(
            config.getStartLocation(),
            config.getBlockType()
        );
        
        // Create projectile wrapper
        MiniBlockProjectile projectile = new MiniBlockProjectile(
            armorStand,
            config.getOwnerUUID(),
            config.getDirection().clone().normalize().multiply(config.getSpeed()),
            config.getDamage(),
            config.getFragmentRank(),
            config.getMaxLifeTicks()
        );
        
        UUID projectileId = armorStand.getUniqueId();
        activeProjectiles.put(projectileId, projectile);
        
        return projectileId;
    }
    
    /**
     * Update all active projectiles (call every tick)
     */
    public void tick() {
        activeProjectiles.values().removeIf(projectile -> {
            if (!projectile.isValid()) {
                projectile.remove();
                return true;
            }
            
            projectile.tick();
            return false;
        });
    }
    
    /**
     * Get a projectile by UUID
     * 
     * @param projectileId Projectile UUID
     * @return Projectile or null if not found
     */
    public MiniBlockProjectile getProjectile(UUID projectileId) {
        return activeProjectiles.get(projectileId);
    }
    
    /**
     * Remove a projectile
     * 
     * @param projectileId Projectile UUID
     */
    public void removeProjectile(UUID projectileId) {
        MiniBlockProjectile projectile = activeProjectiles.remove(projectileId);
        if (projectile != null) {
            projectile.remove();
        }
    }
    
    /**
     * Remove all projectiles
     */
    public void removeAll() {
        activeProjectiles.values().forEach(MiniBlockProjectile::remove);
        activeProjectiles.clear();
    }
    
    /**
     * Get count of active projectiles
     */
    public int getActiveCount() {
        return activeProjectiles.size();
    }
    
    /**
     * Wrapper class for mini block projectile
     */
    public static class MiniBlockProjectile {
        private final ArmorStand armorStand;
        private final UUID ownerUUID;
        private final Vector velocity;
        private final double damage;
        private final int fragmentRank;
        private final int maxLifeTicks;
        private int currentTick;
        
        public MiniBlockProjectile(ArmorStand armorStand, UUID ownerUUID, Vector velocity,
                                   double damage, int fragmentRank, int maxLifeTicks) {
            this.armorStand = armorStand;
            this.ownerUUID = ownerUUID;
            this.velocity = velocity;
            this.damage = damage;
            this.fragmentRank = fragmentRank;
            this.maxLifeTicks = maxLifeTicks;
            this.currentTick = 0;
        }
        
        public void tick() {
            currentTick++;
            
            // Move projectile
            Location newLocation = armorStand.getLocation().add(velocity);
            armorStand.teleport(newLocation);
            
            // Apply gravity (slight downward force)
            velocity.setY(velocity.getY() - 0.03);
        }
        
        public boolean isValid() {
            return armorStand.isValid() && currentTick < maxLifeTicks;
        }
        
        public void remove() {
            if (armorStand.isValid()) {
                armorStand.remove();
            }
        }
        
        // Getters
        public ArmorStand getArmorStand() { return armorStand; }
        public UUID getOwnerUUID() { return ownerUUID; }
        public Vector getVelocity() { return velocity; }
        public double getDamage() { return damage; }
        public int getFragmentRank() { return fragmentRank; }
        public Location getLocation() { return armorStand.getLocation(); }
    }
}
