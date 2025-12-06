package com.muzlik.power;

import org.bukkit.entity.Player;
import java.util.*;

/**
 * Represents an ability slot (Primary, Secondary, or Tertiary)
 */
public enum AbilitySlot {
    PRIMARY(0, "Primary Ability"),
    SECONDARY(1, "Secondary Ability"),
    TERTIARY(2, "Tertiary Ability");

    private final int slotIndex;
    private final String displayName;

    AbilitySlot(int slotIndex, String displayName) {
        this.slotIndex = slotIndex;
        this.displayName = displayName;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get ability slot from hotbar slot index (0-2)
     */
    public static AbilitySlot fromHotbarSlot(int hotbarSlot) {
        if (hotbarSlot >= 0 && hotbarSlot <= 2) {
            for (AbilitySlot slot : values()) {
                if (slot.slotIndex == hotbarSlot) {
                    return slot;
                }
            }
        }
        return null;
    }
}


