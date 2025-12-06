package com.muzlik.ui;

/**
 * Enum representing UI display modes for player preferences.
 */
public enum UIMode {
    STANDARD("Standard", "Full details with styling"),
    ESSENTIALS("Essentials", "Minimal information only");

    private final String displayName;
    private final String description;

    UIMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
