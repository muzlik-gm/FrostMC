package com.muzlik.fragment.ability;

/**
 * Enum representing ability slots with unlock requirements.
 * Follows anime RPG progression where advanced abilities unlock at higher ranks.
 */
public enum AbilitySlot {
    PRIMARY(0, "Primary", 0),           // Always available - Main attack
    SECONDARY(1, "Secondary", 0),       // Always available - Utility/Support
    ULTIMATE(2, "Ultimate", 0),         // Always available - Powerful finishing move
    ADVANCED(3, "Advanced", 5),         // Unlocks at rank threshold - Advanced ability
    MASTERY(4, "Mastery", 7);           // Unlocks at max rank threshold - Mastery ability

    private final int slotIndex;
    private final String displayName;
    private final int baseUnlockRank;   // Base threshold, adjusted per Fragment

    AbilitySlot(int slotIndex, String displayName, int baseUnlockRank) {
        this.slotIndex = slotIndex;
        this.displayName = displayName;
        this.baseUnlockRank = baseUnlockRank;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseUnlockRank() {
        return baseUnlockRank;
    }

    /**
     * Get AbilitySlot by slot index
     * @param index The slot index (0-4)
     * @return The corresponding AbilitySlot, or null if invalid
     */
    public static AbilitySlot fromIndex(int index) {
        for (AbilitySlot slot : values()) {
            if (slot.slotIndex == index) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Check if this slot requires unlocking via ritual
     * @return true if slot 3 or 4, false otherwise
     */
    public boolean requiresUnlock() {
        return this == ADVANCED || this == MASTERY;
    }
}
