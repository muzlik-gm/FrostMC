package com.muzlik.vfx;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents an active VFX effect that is currently playing
 */
public class ActiveEffect {
    private final UUID effectId;
    private final UUID ownerId;
    private final EffectDefinition definition;
    private final Location location;
    private final long startTime;
    private final int rank;
    private int currentTick;
    
    public ActiveEffect(UUID effectId, UUID ownerId, EffectDefinition definition, 
                       Location location, int rank) {
        this.effectId = effectId;
        this.ownerId = ownerId;
        this.definition = definition;
        this.location = location;
        this.startTime = System.currentTimeMillis();
        this.rank = rank;
        this.currentTick = 0;
    }
    
    /**
     * Update this effect (called every tick)
     */
    public void update() {
        currentTick++;
        
        // Render all particle layers
        for (ParticleLayer layer : definition.getParticleLayers()) {
            layer.render(location, currentTick);
        }
    }
    
    /**
     * Check if this effect has completed
     */
    public boolean isComplete() {
        return currentTick >= definition.getLifetime();
    }
    
    /**
     * Get progress (0.0 to 1.0)
     */
    public double getProgress() {
        return Math.min(1.0, (double) currentTick / definition.getLifetime());
    }
    
    // Getters
    
    public UUID getEffectId() {
        return effectId;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public EffectDefinition getDefinition() {
        return definition;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public int getRank() {
        return rank;
    }
    
    public int getCurrentTick() {
        return currentTick;
    }
}
