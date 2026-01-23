package your.plugin.ritual;

// Different ritual types - add more as needed
// Durations are in seconds
public enum RitualType {
    CRYSTAL_CREATION("Crystal Creation", 360),      // 6 minutes
    CRYSTAL_UPGRADE("Crystal Upgrade", 240),        // 4 minutes
    POWER_INFUSION("Power Infusion", 180),          // 3 minutes
    ENCHANTMENT_RITUAL("Enchantment Ritual", 300),  // 5 minutes
    CUSTOM("Custom", 0);                            // Variable duration

    private final String displayName;
    private final int defaultDuration; // in seconds

    RitualType(String displayName, int defaultDuration) {
        this.displayName = displayName;
        this.defaultDuration = defaultDuration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }
}