package your.plugin.ritual;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

// Holds data for an active ritual
// This gets created when someone starts a ritual and stores all the info we need
public class RitualInstance {
    private final UUID playerId;
    private final RitualType type;
    private final Location location;
    private final long startTime;
    private final long duration; // in milliseconds
    private final ItemStack catalyst;
    private final CrystalType crystalType;
    private RitualStage stage;
    
    // Progress milestone tracking
    private int previousProgressPercent = 0;
    
    // Grace period stuff - gives players time to come back if they leave
    private boolean inGracePeriod = false;
    private long gracePeriodStartTime = 0;
    private static final long GRACE_PERIOD_DURATION = 10000; // 10 seconds seems fair

    public RitualInstance(UUID playerId, RitualType type, Location location, 
                         long duration, ItemStack catalyst, CrystalType crystalType) {
        this.playerId = playerId;
        this.type = type;
        this.location = location;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
        this.catalyst = catalyst;
        this.crystalType = crystalType;
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

    public CrystalType getCrystalType() {
        return crystalType;
    }

    public RitualStage getStage() {
        return stage;
    }

    public void setStage(RitualStage stage) {
        this.stage = stage;
    }

    // Get progress percentage (0-100)
    // Just basic math here
    public int getProgressPercent() {
        long elapsed = System.currentTimeMillis() - startTime;
        return (int) Math.min(100, (elapsed * 100) / duration);
    }
    
    /**
     * Get previous progress percentage
     */
    public int getPreviousProgressPercent() {
        return previousProgressPercent;
    }
    
    /**
     * Set previous progress percentage
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
    
    // Grace period methods - this was a pain to get right
    
    // Start the grace period when no players are around
    public void startGracePeriod() {
        if (!inGracePeriod) {
            inGracePeriod = true;
            gracePeriodStartTime = System.currentTimeMillis();
        }
    }
    
    // End grace period when someone comes back
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