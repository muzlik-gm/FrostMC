package com.muzlik.vfx.cinematic.miniblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Configuration for creating a mini block projectile.
 * Uses builder pattern for flexible construction.
 * 
 * Requirements: 3.1, 3.4
 */
public class MiniBlockConfig {
    private final UUID ownerUUID;
    private final Location startLocation;
    private final Vector direction;
    private final Material blockType;
    private final double speed;
    private final double damage;
    private final int fragmentRank;
    private final int maxLifeTicks;
    
    private MiniBlockConfig(Builder builder) {
        this.ownerUUID = builder.ownerUUID;
        this.startLocation = builder.startLocation;
        this.direction = builder.direction;
        this.blockType = builder.blockType;
        this.speed = builder.speed;
        this.damage = builder.damage;
        this.fragmentRank = builder.fragmentRank;
        this.maxLifeTicks = builder.maxLifeTicks;
    }
    
    // Getters
    public UUID getOwnerUUID() { return ownerUUID; }
    public Location getStartLocation() { return startLocation; }
    public Vector getDirection() { return direction; }
    public Material getBlockType() { return blockType; }
    public double getSpeed() { return speed; }
    public double getDamage() { return damage; }
    public int getFragmentRank() { return fragmentRank; }
    public int getMaxLifeTicks() { return maxLifeTicks; }
    
    /**
     * Builder for MiniBlockConfig
     */
    public static class Builder {
        private UUID ownerUUID;
        private Location startLocation;
        private Vector direction;
        private Material blockType = Material.STONE;
        private double speed = 1.0;
        private double damage = 5.0;
        private int fragmentRank = 1;
        private int maxLifeTicks = 200; // 10 seconds
        
        public Builder ownerUUID(UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
            return this;
        }
        
        public Builder startLocation(Location startLocation) {
            this.startLocation = startLocation;
            return this;
        }
        
        public Builder direction(Vector direction) {
            this.direction = direction;
            return this;
        }
        
        public Builder blockType(Material blockType) {
            this.blockType = blockType;
            return this;
        }
        
        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }
        
        public Builder damage(double damage) {
            this.damage = damage;
            return this;
        }
        
        public Builder fragmentRank(int fragmentRank) {
            this.fragmentRank = fragmentRank;
            return this;
        }
        
        public Builder maxLifeTicks(int maxLifeTicks) {
            this.maxLifeTicks = maxLifeTicks;
            return this;
        }
        
        public MiniBlockConfig build() {
            if (ownerUUID == null) {
                throw new IllegalArgumentException("Owner UUID cannot be null");
            }
            if (startLocation == null) {
                throw new IllegalArgumentException("Start location cannot be null");
            }
            if (direction == null) {
                throw new IllegalArgumentException("Direction cannot be null");
            }
            return new MiniBlockConfig(this);
        }
    }
}
