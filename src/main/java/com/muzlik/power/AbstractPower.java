package com.muzlik.power;

import org.bukkit.entity.Player;
import com.muzlik.power.ability.AbilityManager;

/**
 * Base abstract class for all powers
 */
public abstract class AbstractPower implements IPower {

    private final String id;
    private final String displayName;
    private final String theme;
    private final AbilityManager abilityManager;

    /**
     * Create a new power
     * @param id Unique identifier for this power
     * @param displayName Display name for UI purposes
     * @param theme The theme/description of this power
     */
    public AbstractPower(String id, String displayName, String theme) {
        this.id = id;
        this.displayName = displayName;
        this.theme = theme;
        this.abilityManager = new AbilityManager(id);
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
    public String getTheme() {
        return theme;
    }

    @Override
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    @Override
    public void onPowerGrant(Player player) {
        // Override in subclasses if needed
    }

    @Override
    public void onPowerRevoke(Player player) {
        // Override in subclasses if needed
    }

    @Override
    public void cleanup() {
        abilityManager.cleanup();
    }
}


