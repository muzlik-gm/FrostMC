package com.muzlik.vfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a complete VFX effect with multiple particle layers,
 * animation curves, and timing information.
 * 
 * Requirements: 1.1
 */
public class EffectDefinition {
    private final String id;
    private final List<ParticleLayer> particleLayers;
    private final AnimationCurve animationCurve;
    private final int lifetime; // in ticks
    private final boolean rankScaling;
    
    /**
     * Private constructor - use Builder
     */
    private EffectDefinition(String id, List<ParticleLayer> particleLayers, 
                            AnimationCurve animationCurve, int lifetime, boolean rankScaling) {
        this.id = id;
        this.particleLayers = new ArrayList<>(particleLayers);
        this.animationCurve = animationCurve;
        this.lifetime = lifetime;
        this.rankScaling = rankScaling;
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public List<ParticleLayer> getParticleLayers() {
        return Collections.unmodifiableList(particleLayers);
    }
    
    public AnimationCurve getAnimationCurve() {
        return animationCurve;
    }
    
    public int getLifetime() {
        return lifetime;
    }
    
    public boolean isRankScaling() {
        return rankScaling;
    }
    
    /**
     * Builder for EffectDefinition
     */
    public static class Builder {
        private String id;
        private List<ParticleLayer> particleLayers = new ArrayList<>();
        private AnimationCurve animationCurve = AnimationCurve.LINEAR;
        private int lifetime = 50; // 2.5 seconds default
        private boolean rankScaling = true;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder addParticleLayer(ParticleLayer layer) {
            this.particleLayers.add(layer);
            return this;
        }
        
        public Builder setAnimationCurve(AnimationCurve curve) {
            this.animationCurve = curve;
            return this;
        }
        
        public Builder setLifetime(int ticks) {
            this.lifetime = ticks;
            return this;
        }
        
        public Builder setRankScaling(boolean rankScaling) {
            this.rankScaling = rankScaling;
            return this;
        }
        
        public EffectDefinition build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Effect ID cannot be null or empty");
            }
            if (particleLayers.isEmpty()) {
                throw new IllegalStateException("Effect must have at least one particle layer");
            }
            return new EffectDefinition(id, particleLayers, animationCurve, lifetime, rankScaling);
        }
    }
}
