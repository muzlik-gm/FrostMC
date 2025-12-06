package com.muzlik.fragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks progression for a single Fragment for a player.
 * Includes level, rank, XP, and ability cooldowns.
 */
public class FragmentProgress {
    private final FragmentType type;
    private boolean unlocked;
    private int rank;
    private int level;
    private double xp;
    private int prestigeLevel;
    private double currentMana;
    private final Map<String, Long> abilityCooldowns;

    public FragmentProgress(FragmentType type) {
        this.type = type;
        this.unlocked = false;
        this.rank = 1;
        this.level = 1;
        this.xp = 0.0;
        this.prestigeLevel = 0;
        this.currentMana = 100.0;
        this.abilityCooldowns = new HashMap<>();
    }

    // Getters and setters
    public FragmentType getType() { return type; }
    
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    
    public int getPrestigeLevel() { return prestigeLevel; }
    public void setPrestigeLevel(int prestigeLevel) { this.prestigeLevel = prestigeLevel; }
    
    public double getCurrentMana() { return currentMana; }
    public void setCurrentMana(double currentMana) { this.currentMana = currentMana; }
    
    public Map<String, Long> getAbilityCooldowns() { return abilityCooldowns; }
    
    public void setAbilityCooldown(String abilityId, long cooldownEnd) {
        abilityCooldowns.put(abilityId, cooldownEnd);
    }
    
    public Long getAbilityCooldown(String abilityId) {
        return abilityCooldowns.get(abilityId);
    }
    
    public void removeAbilityCooldown(String abilityId) {
        abilityCooldowns.remove(abilityId);
    }
}
