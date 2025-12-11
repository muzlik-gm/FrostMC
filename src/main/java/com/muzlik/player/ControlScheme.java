package com.muzlik.player;

/**
 * Different control schemes for Fragment abilities
 * Each player can choose their preferred control method
 */
public enum ControlScheme {
    /**
     * SNEAK + CLICK (Default)
     * - Hold Sneak + Right Click = Use ability
     * - Hold Sneak + Left Click = Alternate mode
     * - Hotbar slots 1-5 = Different abilities
     */
    SNEAK_CLICK("Sneak + Click", 
        "Hold Sneak and click to use abilities",
        new String[]{
            "§7• §fHold §eSNEAK §7+ §eRIGHT CLICK §7= Use ability",
            "§7• §fHold §eSNEAK §7+ §eLEFT CLICK §7= Alternate mode",
            "§7• §fHotbar slots §e1-5 §7= Different abilities"
        }),
    
    /**
     * DOUBLE SNEAK + CLICK
     * - Double tap Sneak, then click = Use ability
     * - Less accidental activation
     * - Good for builders
     */
    DOUBLE_SNEAK("Double Sneak + Click",
        "Double tap Sneak, then click to use abilities",
        new String[]{
            "§7• §fDouble tap §eSNEAK §7quickly",
            "§7• §fThen §eRIGHT CLICK §7= Use ability",
            "§7• §fThen §eLEFT CLICK §7= Alternate mode",
            "§7• §fMode lasts 3 seconds",
            "§7• §fHotbar slots §e1-5 §7= Different abilities"
        }),
    
    /**
     * SWAP HANDS + CLICK
     * - Press F (swap hands) + Click = Use ability
     * - No sneaking required
     * - Good for combat
     */
    SWAP_HANDS("Swap Hands + Click",
        "Press F (swap hands) and click to use abilities",
        new String[]{
            "§7• §fPress §eF §7(swap hands) + §eRIGHT CLICK §7= Use ability",
            "§7• §fPress §eF §7+ §eLEFT CLICK §7= Alternate mode",
            "§7• §fHotbar slots §e1-5 §7= Different abilities",
            "§7• §fNo sneaking needed"
        }),
    
    /**
     * CLICK ONLY (Dangerous)
     * - Just click = Use ability
     * - No modifier key needed
     * - High risk of accidents
     */
    CLICK_ONLY("Click Only",
        "Just click to use abilities (WARNING: Easy to trigger accidentally)",
        new String[]{
            "§7• §eRIGHT CLICK §7= Use ability",
            "§7• §eLEFT CLICK §7= Alternate mode",
            "§7• §fHotbar slots §e1-5 §7= Different abilities",
            "§c⚠ WARNING: Very easy to trigger accidentally!"
        });
    
    private final String displayName;
    private final String description;
    private final String[] instructions;
    
    ControlScheme(String displayName, String description, String[] instructions) {
        this.displayName = displayName;
        this.description = description;
        this.instructions = instructions;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String[] getInstructions() {
        return instructions;
    }
    
    /**
     * Get next control scheme in cycle
     */
    public ControlScheme next() {
        ControlScheme[] values = values();
        int nextIndex = (this.ordinal() + 1) % values.length;
        return values[nextIndex];
    }
    
    /**
     * Get previous control scheme in cycle
     */
    public ControlScheme previous() {
        ControlScheme[] values = values();
        int prevIndex = (this.ordinal() - 1 + values.length) % values.length;
        return values[prevIndex];
    }
}
