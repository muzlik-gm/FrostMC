package your.plugin.ritual;

// Different crystal types for rituals
// TODO: Replace this with whatever item system you're using
public enum CrystalType {
    FIRE("Fire Crystal", "A blazing crystal of pure fire energy"),
    WATER("Water Crystal", "A flowing crystal of pure water energy"),
    AIR("Air Crystal", "A floating crystal of pure air energy"),
    EARTH("Earth Crystal", "A solid crystal of pure earth energy"),
    DARK("Dark Crystal", "A shadowy crystal of pure dark energy"),
    LIGHT("Light Crystal", "A radiant crystal of pure light energy"),
    VOID("Void Crystal", "A mysterious crystal of pure void energy"),
    STORM("Storm Crystal", "An electric crystal of pure storm energy"),
    TIME("Time Crystal", "A temporal crystal of pure time energy"),
    LUCK("Luck Crystal", "A fortunate crystal of pure luck energy");

    private final String displayName;
    private final String description;

    CrystalType(String displayName, String description) {
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