package com.muzlik.vfx;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a source of damage for attribution tracking.
 * Links damage back to the original player and ability.
 * 
 * Requirements: 4.2
 */
public class DamageSource {
    private final UUID ownerId;
    private final String abilityId;
    private final FragmentType fragmentType;
    private final long timestamp;
    private final long expirationTime;
    
    /**
     * Constructor
     * 
     * @param ownerId UUID of the player who owns this damage source
     * @param abilityId ID of the ability that created this damage source
     * @param fragmentType Type of Fragment
     * @param durationMillis How long this damage source remains valid (milliseconds)
     */
    public DamageSource(UUID ownerId, String abilityId, FragmentType fragmentType, long durationMillis) {
        this.ownerId = ownerId;
        this.abilityId = abilityId;
        this.fragmentType = fragmentType;
        this.timestamp = System.currentTimeMillis();
        this.expirationTime = timestamp + durationMillis;
    }
    
    /**
     * Get the owner of this damage source
     * 
     * @return The player who owns this damage source, or null if offline
     */
    public Player getOwner() {
        return Bukkit.getPlayer(ownerId);
    }
    
    /**
     * Check if this damage source has expired
     * 
     * @return true if current time is past expiration time
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
    
    /**
     * Get the age of this damage source in milliseconds
     * 
     * @return Age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Get remaining time before expiration
     * 
     * @return Remaining time in milliseconds
     */
    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
    
    // Getters
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public String getAbilityId() {
        return abilityId;
    }
    
    public FragmentType getFragmentType() {
        return fragmentType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public long getExpirationTime() {
        return expirationTime;
    }
    
    @Override
    public String toString() {
        return "DamageSource{" +
                "ownerId=" + ownerId +
                ", abilityId='" + abilityId + '\'' +
                ", fragmentType=" + fragmentType +
                ", age=" + getAge() + "ms" +
                ", remaining=" + getRemainingTime() + "ms" +
                '}';
    }
}
