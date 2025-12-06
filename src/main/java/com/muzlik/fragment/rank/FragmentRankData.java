package com.muzlik.fragment.rank;

/**
 * Stores rank data for a single Fragment for a player.
 */
public class FragmentRankData {
    private int currentRank;

    public FragmentRankData() {
        this.currentRank = 1;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }
}
