package com.muzlik.power.ability;

import org.bukkit.entity.Player;
import com.muzlik.power.AbstractAbility;
import com.muzlik.power.IAbility;

/**
 * Manages the three abilities for a specific power
 */
public class AbilityManager {

    private final String powerId;
    private final IAbility[] abilities;

    /**
     * Create an ability manager for a power
     * @param powerId The ID of the power this manager belongs to
     */
    public AbilityManager(String powerId) {
        this.powerId = powerId;
        this.abilities = new IAbility[3]; // 0=Primary, 1=Secondary, 2=Tertiary
    }

    /**
     * Register a primary ability (slot 0)
     */
    public void registerPrimaryAbility(IAbility ability) {
        registerAbility(0, ability);
    }

    /**
     * Register a secondary ability (slot 1)
     */
    public void registerSecondaryAbility(IAbility ability) {
        registerAbility(1, ability);
    }

    /**
     * Register a tertiary ability (slot 2)
     */
    public void registerTertiaryAbility(IAbility ability) {
        registerAbility(2, ability);
    }

    /**
     * Register an ability to a specific slot
     * @param slot Slot index (0, 1, or 2)
     * @param ability The ability to register
     */
    public void registerAbility(int slot, IAbility ability) {
        if (slot < 0 || slot > 2) {
            throw new IllegalArgumentException("Slot must be between 0 and 2");
        }
        
        // Cleanup existing ability if present
        if (abilities[slot] != null) {
            abilities[slot].cleanup();
        }
        
        abilities[slot] = ability;
    }

    /**
     * Get the ability at a specific slot
     * @param slot Slot index (0, 1, or 2)
     * @return The ability, or null if no ability is registered
     */
    public IAbility getAbility(int slot) {
        if (slot < 0 || slot > 2) {
            return null;
        }
        return abilities[slot];
    }

    /**
     * Get all registered abilities
     */
    public IAbility[] getAbilities() {
        return abilities;
    }

    /**
     * Check if all three ability slots are filled
     */
    public boolean isFull() {
        return abilities[0] != null && abilities[1] != null && abilities[2] != null;
    }

    /**
     * Execute an ability in a specific slot
     * @param player The player executing the ability
     * @param slot The ability slot (0, 1, or 2)
     * @return true if ability was executed successfully
     */
    public boolean executeAbility(Player player, int slot) {
        IAbility ability = getAbility(slot);
        if (ability == null) {
            return false;
        }
        
        return ability.execute(player);
    }

    /**
     * Cancel an ability in a specific slot
     * @param player The player executing the ability
     * @param slot The ability slot (0, 1, or 2)
     */
    public void cancelAbility(Player player, int slot) {
        IAbility ability = getAbility(slot);
        if (ability != null) {
            ability.cancel(player);
        }
    }

    /**
     * Get the power ID this manager belongs to
     */
    public String getPowerId() {
        return powerId;
    }

    /**
     * Cleanup all abilities
     */
    public void cleanup() {
        for (IAbility ability : abilities) {
            if (ability != null) {
                ability.cleanup();
            }
        }
    }
}


