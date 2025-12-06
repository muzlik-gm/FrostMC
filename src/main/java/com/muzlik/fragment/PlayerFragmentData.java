package com.muzlik.fragment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores all Fragment-related data for a single player.
 */
public class PlayerFragmentData {
    private final Set<FragmentType> ownedFragments;
    private FragmentType activeFragment;
    private long lastFragmentSwitch;

    public PlayerFragmentData() {
        this.ownedFragments = new HashSet<>();
        this.activeFragment = null;
        this.lastFragmentSwitch = 0;
    }

    /**
     * Add a Fragment to owned Fragments
     */
    public void addFragment(FragmentType type) {
        ownedFragments.add(type);
    }

    /**
     * Check if player owns a Fragment
     */
    public boolean hasFragment(FragmentType type) {
        return ownedFragments.contains(type);
    }

    /**
     * Get all owned Fragments
     */
    public Collection<FragmentType> getOwnedFragments() {
        return new HashSet<>(ownedFragments);
    }

    /**
     * Get active Fragment
     */
    public FragmentType getActiveFragment() {
        return activeFragment;
    }

    /**
     * Set active Fragment
     */
    public void setActiveFragment(FragmentType activeFragment) {
        this.activeFragment = activeFragment;
    }

    /**
     * Get last Fragment switch time
     */
    public long getLastFragmentSwitch() {
        return lastFragmentSwitch;
    }

    /**
     * Set last Fragment switch time
     */
    public void setLastFragmentSwitch(long lastFragmentSwitch) {
        this.lastFragmentSwitch = lastFragmentSwitch;
    }
}
