package com.muzlik.fragment.ability;

/**
 * Enum representing how an ability is activated
 */
public enum ActivationType {
    RIGHT_CLICK,
    LEFT_CLICK,
    BOTH,
    PASSIVE,  // For abilities that trigger automatically (e.g., Phoenix Rebirth on death)
    
    // Aliases for backward compatibility
    PRIMARY,   // Same as RIGHT_CLICK
    ALTERNATE  // Same as LEFT_CLICK
}
