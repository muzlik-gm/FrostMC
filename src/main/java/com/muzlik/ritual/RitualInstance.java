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
}
