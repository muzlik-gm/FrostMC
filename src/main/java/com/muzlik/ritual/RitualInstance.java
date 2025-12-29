package com.muzlik.ritual;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents an active ritual instance.
 */
public class RitualInstance {
    private final UUID playerId;
    private final RitualType type;
    private final Location location;
    private final long startTime;
    private final long duration; // in milliseconds
    private final ItemStack catalyst;
    private final FragmentType fragmentType;
    private RitualStage stage;
    
    // Task 12.1: Progress milestone tracking
    private int previousProgressPercent = 0;
    
    // Grace period tracking
    private boolean inGracePeriod = false;
    private long gracePeriodStartTime = 0;
    private static final long GRACE_PERIOD_DURATION = 10000; // 10 seconds in milliseconds

    public RitualInstance(UUID playerId, RitualType type, Location location, 
                         long duration, ItemStack catalyst, FragmentType fragmentType) {
        this.playerId = playerId;
        this.type = type;
        this.location = location;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
        this.catalyst = catalyst;
        this.fragmentType = fragmentType;
        this.stage = RitualStage.CHARGING;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public RitualType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public FragmentType getFragmentType() {
        return fragmentType;
    }

    public RitualStage getStage() {
        return stage;
    }

    public void setStage(RitualStage stage) {
        this.stage = stage;
    }

    /**
     * Get progress percentage (0-100)
     */
    public int getProgressPercent() {
        long elapsed = System.currentTimeMillis() - startTime;
        return (int) Math.min(100, (elapsed * 100) / duration);
    }
    
    /**
     * Get previous progress percentage (Task 12.1)
     */
    public int getPreviousProgressPercent() {
        return previousProgressPercent;
    }
    
    /**
     * Set previous progress percentage (Task 12.1)
     */
    public void setPreviousProgressPercent(int percent) {
        this.previousProgressPercent = percent;
    }

    /**
     * Get remaining time in milliseconds
     */
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, duration - elapsed);
    }

    /**
     * Get remaining time in seconds
     */
    public long getRemainingSeconds() {
        return getRemainingTime() / 1000;
    }
    
    // Grace period methods
    
    /**
     * Start the grace period (no players in ritual area)
     */
    public void startGracePeriod() {
        if (!inGracePeriod) {
            inGracePeriod = true;
            gracePeriodStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * End the grace period (player returned to ritual area)
     */
    public void endGracePeriod() {
        inGracePeriod = false;
        gracePeriodStartTime = 0;
    }
    
    /**
     * Check if ritual is in grace period
     */
    public boolean isInGracePeriod() {
        return inGracePeriod;
    }
    
    /**
     * Check if grace period has expired
     */
    public boolean isGracePeriodExpired() {
        if (!inGracePeriod) return false;
        return System.currentTimeMillis() - gracePeriodStartTime >= GRACE_PERIOD_DURATION;
    }
    
    /**
     * Get remaining grace period time in seconds
     */
    public long getGracePeriodRemainingSeconds() {
        if (!inGracePeriod) return 0;
        long elapsed = System.currentTimeMillis() - gracePeriodStartTime;
        return Math.max(0, (GRACE_PERIOD_DURATION - elapsed) / 1000);
    }
    
    /**
     * Get grace period duration in seconds
     */
    public static long getGracePeriodDurationSeconds() {
        return GRACE_PERIOD_DURATION / 1000;
    }
}
