package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Configuration for creating a BlockController.
 * Uses builder pattern for flexible construction.
 */
public class BlockControllerConfig {
    private final UUID ownerUUID;
    private final String abilityId;
    private final Location startLocation;
    private final Vector direction;
    private final double baseSpeed;
    private final double accelerationFactor;
    private final double maxSpeed;
    private final int maxLifeTicks;
    private final ShellType shellType;
    private final Material blockType;
    private final double collisionRadius;
    private final double baseDamage;
    private final int fragmentRank;
    private final ParticleShellRenderer particleRenderer;
    private final ImpactHandler impactHandler;
    
    private BlockControllerConfig(Builder builder) {
        this.ownerUUID = builder.ownerUUID;
        this.abilityId = builder.abilityId;
        this.startLocation = builder.startLocation;
        this.direction = builder.direction;
        this.baseSpeed = builder.baseSpeed;
        this.accelerationFactor = builder.accelerationFactor;
        this.maxSpeed = builder.maxSpeed;
        this.maxLifeTicks = builder.maxLifeTicks;
        this.shellType = builder.shellType;
        this.blockType = builder.blockType;
        this.collisionRadius = builder.collisionRadius;
        this.baseDamage = builder.baseDamage;
        this.fragmentRank = builder.fragmentRank;
        this.particleRenderer = builder.particleRenderer;
        this.impactHandler = builder.impactHandler;
    }
    
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getAbilityId() { return abilityId; }
    public Location getStartLocation() { return startLocation; }
    public Vector getDirection() { return direction; }
    public double getBaseSpeed() { return baseSpeed; }
    public double getAccelerationFactor() { return accelerationFactor; }
    public double getMaxSpeed() { return maxSpeed; }
    public int getMaxLifeTicks() { return maxLifeTicks; }
    public ShellType getShellType() { return shellType; }
    public Material getBlockType() { return blockType; }
    public double getCollisionRadius() { return collisionRadius; }
    public double getBaseDamage() { return baseDamage; }
    public int getFragmentRank() { return fragmentRank; }
    public ParticleShellRenderer getParticleRenderer() { return particleRenderer; }
    public ImpactHandler getImpactHandler() { return impactHandler; }
    
    public static class Builder {
        private UUID ownerUUID;
        private String abilityId;
        private Location startLocation;
        private Vector direction;
        private double baseSpeed = 1.0;
        private double accelerationFactor = 0.05;
        private double maxSpeed = 3.0;
        private int maxLifeTicks = 60;
        private ShellType shellType = ShellType.PARTICLE_SHELL;
        private Material blockType = Material.STONE;
        private double collisionRadius = 0.5;
        private double baseDamage = 5.0;
        private int fragmentRank = 1;
        private ParticleShellRenderer particleRenderer;
        private ImpactHandler impactHandler;
        
        public Builder ownerUUID(UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
            return this;
        }
        
        public Builder abilityId(String abilityId) {
            this.abilityId = abilityId;
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
        
        public Builder baseSpeed(double baseSpeed) {
            this.baseSpeed = baseSpeed;
            return this;
        }
        
        public Builder accelerationFactor(double accelerationFactor) {
            this.accelerationFactor = accelerationFactor;
            return this;
        }
        
        public Builder maxSpeed(double maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }
        
        public Builder maxLifeTicks(int maxLifeTicks) {
            this.maxLifeTicks = maxLifeTicks;
            return this;
        }
        
        public Builder shellType(ShellType shellType) {
            this.shellType = shellType;
            return this;
        }
        
        public Builder blockType(Material blockType) {
            this.blockType = blockType;
            return this;
        }
        
        public Builder collisionRadius(double collisionRadius) {
            this.collisionRadius = collisionRadius;
            return this;
        }
        
        public Builder baseDamage(double baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }
        
        public Builder fragmentRank(int fragmentRank) {
            this.fragmentRank = fragmentRank;
            return this;
        }
        
        public Builder particleRenderer(ParticleShellRenderer particleRenderer) {
            this.particleRenderer = particleRenderer;
            return this;
        }
        
        public Builder impactHandler(ImpactHandler impactHandler) {
            this.impactHandler = impactHandler;
            return this;
        }
        
        public BlockControllerConfig build() {
            if (ownerUUID == null) throw new IllegalStateException("ownerUUID required");
            if (startLocation == null) throw new IllegalStateException("startLocation required");
            if (direction == null) throw new IllegalStateException("direction required");
            return new BlockControllerConfig(this);
        }
    }
}
