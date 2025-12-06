package com.muzlik.powers;

import com.muzlik.power.IPower;
import com.muzlik.power.ability.AbilityManager;
import org.bukkit.entity.Player;

/**
 * Placeholder power that does nothing
 * Used to prevent "non-existent power" warnings
 * Actual abilities are handled by the Fragment system
 */
public class PlaceholderPower implements IPower {
    
    private final String powerName;
    private final AbilityManager abilityManager;
    
    public PlaceholderPower(String powerName) {
        this.powerName = powerName;
        this.abilityManager = new AbilityManager(powerName);
    }
    
    @Override
    public String getId() {
        return powerName;
    }
    
    @Override
    public String getDisplayName() {
        return powerName.replace("_", " ").toUpperCase();
    }
    
    @Override
    public String getTheme() {
        return "Fragment abilities - use Fragment system";
    }
    
    @Override
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
    
    @Override
    public void onPowerGrant(Player player) {
        // Do nothing - Fragment system handles abilities
    }
    
    @Override
    public void onPowerRevoke(Player player) {
        // Do nothing
    }
    
    @Override
    public void cleanup() {
        // Do nothing
    }
}
