package com.muzlik.fragment.ability;

/**
 * Activation mode for abilities.
 * 
 * Defines how an ability is activated:
 * - PRIMARY: Sneak + Right-Click
 * - ALTERNATE: Sneak + Left-Click (arm swing)
 * 
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5
 */
public enum ActivationMode {
    /**
     * Primary activation - Sneak + Right-Click
     */
    PRIMARY,
    
    /**
     * Alternate activation - Sneak + Left-Click (arm swing)
     */
    ALTERNATE
}
