package com.muzlik.mana;

/**
 * Stores mana-related data for a single player.
 */
public class PlayerManaData {
    private double currentMana;
    private int rank;
    private int level;
    private double temporaryManaBoost;

    public PlayerManaData(double initialMana) {
        this.currentMana = initialMana;
        this.rank = 1;
        this.level = 1;
        this.temporaryManaBoost = 0.0;
    }

    public double getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(double currentMana) {
        this.currentMana = currentMana;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getTemporaryManaBoost() {
        return temporaryManaBoost;
    }

    public void addManaBoost(double boost) {
        this.temporaryManaBoost += boost;
    }

    public void removeManaBoost(double boost) {
        this.temporaryManaBoost = Math.max(0, this.temporaryManaBoost - boost);
    }
}
