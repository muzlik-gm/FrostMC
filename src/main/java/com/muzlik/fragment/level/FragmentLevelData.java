package com.muzlik.fragment.level;

/**
 * Stores level and XP data for a single Fragment for a player.
 */
public class FragmentLevelData {
    private int level;
    private double xp;
    private int prestigeLevel;
    private XPCurve xpCurve;

    public FragmentLevelData(XPCurve xpCurve) {
        this.level = 1;
        this.xp = 0.0;
        this.prestigeLevel = 0;
        this.xpCurve = xpCurve;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    public void incrementPrestige() {
        this.prestigeLevel++;
    }

    public XPCurve getXpCurve() {
        return xpCurve;
    }

    public void setXpCurve(XPCurve xpCurve) {
        this.xpCurve = xpCurve;
    }
}
