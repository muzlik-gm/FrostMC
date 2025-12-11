package com.muzlik.player;

/**
 * Stores player preferences for Fragment system
 * Each player has their own settings
 */
public class PlayerPreferences {
    private ControlScheme controlScheme;
    private boolean abilitiesEnabled;
    private boolean showTutorials;
    
    public PlayerPreferences() {
        this.controlScheme = ControlScheme.SNEAK_CLICK; // Default
        this.abilitiesEnabled = true; // Abilities enabled by default
        this.showTutorials = true; // Show tutorials by default
    }
    
    public ControlScheme getControlScheme() {
        return controlScheme;
    }
    
    public void setControlScheme(ControlScheme controlScheme) {
        this.controlScheme = controlScheme;
    }
    
    public boolean areAbilitiesEnabled() {
        return abilitiesEnabled;
    }
    
    public void setAbilitiesEnabled(boolean enabled) {
        this.abilitiesEnabled = enabled;
    }
    
    public boolean shouldShowTutorials() {
        return showTutorials;
    }
    
    public void setShowTutorials(boolean show) {
        this.showTutorials = show;
    }
}
