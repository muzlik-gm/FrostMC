package com.muzlik.vfx.cinematic;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Configuration for creating a magic circle.
 * Uses builder pattern for flexible construction.
 * 
 * Requirements: 8.2, 8.3
 */
public class MagicCircleConfig {
    private final Location center;
    private final FragmentType fragmentType;
    private final int rank;
    private final double baseRadius;
    private final double rotationSpeed;
    private final int lifetime;
    private final Particle primaryParticle;
    private final Particle secondaryParticle;
    private final boolean horizontal;
    private final org.bukkit.entity.Player owner;
    
    private MagicCircleConfig(Builder builder) {
        this.center = builder.center;
        this.fragmentType = builder.fragmentType;
        this.rank = builder.rank;
        this.baseRadius = builder.baseRadius;
        this.rotationSpeed = builder.rotationSpeed;
        this.lifetime = builder.lifetime;
        this.primaryParticle = builder.primaryParticle;
        this.secondaryParticle = builder.secondaryParticle;
        this.horizontal = builder.horizontal;
        this.owner = builder.owner;
    }
    
    // Getters
    public Location getCenter() { return center; }
    public FragmentType getFragmentType() { return fragmentType; }
    public int getRank() { return rank; }
    public double getBaseRadius() { return baseRadius; }
    public double getRotationSpeed() { return rotationSpeed; }
    public int getLifetime() { return lifetime; }
    public Particle getPrimaryParticle() { return primaryParticle; }
    public Particle getSecondaryParticle() { return secondaryParticle; }
    public boolean isHorizontal() { return horizontal; }
    public org.bukkit.entity.Player getOwner() { return owner; }
    
    /**
     * Builder for MagicCircleConfig
     */
    public static class Builder {
        private Location center;
        private FragmentType fragmentType = FragmentType.FIRE;
        private int rank = 1;
        private double baseRadius = 2.0;
        private double rotationSpeed = 0.05;
        private int lifetime = 100; // 5 seconds
        private Particle primaryParticle = Particle.FLAME;
        private Particle secondaryParticle = Particle.END_ROD;
        private boolean horizontal = true;
        private org.bukkit.entity.Player owner;
        
        public Builder center(Location center) {
            this.center = center;
            return this;
        }
        
        public Builder fragmentType(FragmentType fragmentType) {
            this.fragmentType = fragmentType;
            return this;
        }
        
        public Builder rank(int rank) {
            this.rank = rank;
            return this;
        }
        
        public Builder baseRadius(double baseRadius) {
            this.baseRadius = baseRadius;
            return this;
        }
        
        public Builder rotationSpeed(double rotationSpeed) {
            this.rotationSpeed = rotationSpeed;
            return this;
        }
        
        public Builder lifetime(int lifetime) {
            this.lifetime = lifetime;
            return this;
        }
        
        public Builder primaryParticle(Particle primaryParticle) {
            this.primaryParticle = primaryParticle;
            return this;
        }
        
        public Builder secondaryParticle(Particle secondaryParticle) {
            this.secondaryParticle = secondaryParticle;
            return this;
        }
        
        public Builder horizontal(boolean horizontal) {
            this.horizontal = horizontal;
            return this;
        }
        
        public Builder owner(org.bukkit.entity.Player owner) {
            this.owner = owner;
            return this;
        }
        
        public MagicCircleConfig build() {
            if (center == null) {
                throw new IllegalArgumentException("Center cannot be null");
            }
            if (baseRadius <= 0) {
                throw new IllegalArgumentException("Base radius must be positive");
            }
            return new MagicCircleConfig(this);
        }
    }
}
