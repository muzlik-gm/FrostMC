package com.muzlik.power;

import org.bukkit.entity.Player;

/**
 * Base abstract class for all abilities
 * Provides common functionality like cooldown tracking
 */
public abstract class AbstractAbility implements IAbility {

    private final String id;
    private final String displayName;
    private final String description;
    private final long cooldown; // in milliseconds
    private boolean active;

    /**
     * Create a new ability
     * @param id Unique identifier for this ability
     * @param displayName Display name for UI purposes
     * @param description Description of what the ability does
     * @param cooldown Cooldown in milliseconds
     */
    public AbstractAbility(String id, String displayName, String description, long cooldown) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cooldown = cooldown;
        this.active = false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Set the active state of this ability
     */
    protected void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void cancel(Player player) {
        // Override in subclasses if needed
    }

    @Override
    public void cleanup() {
        // Override in subclasses if needed
    }
}


