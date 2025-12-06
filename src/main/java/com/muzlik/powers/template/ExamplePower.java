package com.muzlik.powers.template;

import com.muzlik.power.AbstractPower;
import com.muzlik.power.AbstractAbility;
import org.bukkit.entity.Player;

/**
 * TEMPLATE: Example power implementation
 * Copy this file to create new powers
 * 
 * This template shows how to:
 * 1. Create a power class
 * 2. Create three ability classes (Primary, Secondary, Tertiary)
 * 3. Register abilities with the power
 */
public class ExamplePower extends AbstractPower {

    public ExamplePower() {
        super(
            "example_power",           // Unique ID
            "Example Power",           // Display name
            "An example power template" // Theme description
        );

        // Register the three abilities
        getAbilityManager().registerPrimaryAbility(new ExamplePrimaryAbility());
        getAbilityManager().registerSecondaryAbility(new ExampleSecondaryAbility());
        getAbilityManager().registerTertiaryAbility(new ExampleTertiaryAbility());
    }

    /**
     * PRIMARY ABILITY: Example implementation
     * Triggered by: Sneak + Right-Click with slot 0 (first hotbar item)
     */
    private static class ExamplePrimaryAbility extends AbstractAbility {

        public ExamplePrimaryAbility() {
            super(
                "example_primary",
                "Primary Blast",
                "Shoots a blast forward",
                5000 // 5 second cooldown
            );
        }

        @Override
        public boolean execute(Player player) {
            // TODO: Implement your primary ability logic here
            // Example: Launch a projectile, deal damage, create effects, etc.
            
            setActive(true);
            
            // Placeholder implementation
            player.sendMessage("§b→ Primary ability executed!");
            
            setActive(false);
            return true;
        }
    }

    /**
     * SECONDARY ABILITY: Example implementation
     * Triggered by: Sneak + Left-Click with slot 1 (second hotbar item)
     */
    private static class ExampleSecondaryAbility extends AbstractAbility {

        public ExampleSecondaryAbility() {
            super(
                "example_secondary",
                "Secondary Shield",
                "Creates a protective shield",
                8000 // 8 second cooldown
            );
        }

        @Override
        public boolean execute(Player player) {
            // TODO: Implement your secondary ability logic here
            // Example: Grant resistance, create barriers, heal, etc.
            
            setActive(true);
            
            // Placeholder implementation
            player.sendMessage("§a◆ Secondary ability executed!");
            
            setActive(false);
            return true;
        }

        @Override
        public void cancel(Player player) {
            // Called when the ability is cancelled or power is lost
            // Cleanup any ongoing effects
            setActive(false);
        }
    }

    /**
     * TERTIARY ABILITY: Example implementation
     * Triggered by: Sneak + Right-Click with slot 2 (third hotbar item)
     */
    private static class ExampleTertiaryAbility extends AbstractAbility {

        public ExampleTertiaryAbility() {
            super(
                "example_tertiary",
                "Tertiary Dash",
                "Quickly dashes forward",
                6000 // 6 second cooldown
            );
        }

        @Override
        public boolean execute(Player player) {
            // TODO: Implement your tertiary ability logic here
            // Example: Apply speed boost, velocity boost, teleport, etc.
            
            setActive(true);
            
            // Placeholder implementation
            player.sendMessage("§c⚡ Tertiary ability executed!");
            
            setActive(false);
            return true;
        }
    }
}


