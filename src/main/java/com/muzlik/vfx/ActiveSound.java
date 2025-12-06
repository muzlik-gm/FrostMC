package com.muzlik.vfx;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Represents an active sound that is currently playing or scheduled to play
 */
public class ActiveSound {
    private final UUID id;
    private final UUID ownerId;
    private Location location;
    private final SoundDefinition definition;
    private final long startTime;
    private final long playTime;
    private boolean played;
    
    public ActiveSound(UUID id, UUID ownerId, Location location, SoundDefinition definition) {
        this.id = id;
        this.ownerId = ownerId;
        this.location = location.clone();
        this.definition = definition;
        this.startTime = System.currentTimeMillis();
        this.playTime = startTime + (definition.getDelay() * 50L); // Convert ticks to milliseconds
        this.played = false;
    }
    
    /**
     * Check if this sound is ready to play
     */
    public boolean isReadyToPlay() {
        return !played && System.currentTimeMillis() >= playTime;
    }
    
    /**
     * Mark this sound as played
     */
    public void markPlayed() {
        this.played = true;
    }
    
    /**
     * Update the position of this sound
     */
    public void updatePosition(Location newLocation) {
        if (newLocation != null) {
            this.location = newLocation.clone();
        }
    }
    
    /**
     * Check if this sound has expired (played and past cleanup time)
     */
    public boolean isExpired() {
        return played && (System.currentTimeMillis() - playTime) > 5000; // 5 seconds after play
    }
    
    // Getters
    
    public UUID getId() {
        return id;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public SoundDefinition getDefinition() {
        return definition;
    }
    
    public boolean isPlayed() {
        return played;
    }
    
    @Override
    public String toString() {
        return "ActiveSound{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", location=" + location +
                ", played=" + played +
                '}';
    }
}
