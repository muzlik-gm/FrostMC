package com.muzlik.fragment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores all Fragment-related data for a single player.
 * 
 * Fragment States:
 * - LOCKED: No ritual completed for this fragment
 * - CHARGED: Ritual completed, ready to activate with Fragment Changer
 * - ACTIVATED: Currently active (only one can be active at a time)
 * 
 * Flow:
 * 1. Complete Fragment Creation Ritual → Fragment becomes CHARGED
 * 2. Use Fragment Changer → Fragment becomes ACTIVATED (added to owned)
 * 3. If you switch to another fragment, the previous one is no longer owned
 *    and you must re-craft + re-ritual to use it again
 */
public class PlayerFragmentData {
    private final Set<FragmentType> ownedFragments;  // Currently usable fragments
    private final Set<FragmentType> chargedFragments; // Rituals completed, ready to activate
    private final Set<FragmentType> completedRituals; // Fragments that have been successfully created (one-time only)
    private FragmentType activeFragment;
    private long lastFragmentSwitch;

    public PlayerFragmentData() {
        this.ownedFragments = new HashSet<>();
        this.chargedFragments = new HashSet<>();
        this.completedRituals = new HashSet<>();
        this.activeFragment = null;
        this.lastFragmentSwitch = 0;
    }

    // ═══════════════════════════════════════════════════════════════
    // OWNED FRAGMENTS (currently usable)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add a Fragment to owned Fragments
     */
    public void addFragment(FragmentType type) {
        ownedFragments.add(type);
    }

    /**
     * Remove a Fragment from owned Fragments
     */
    public void removeFragment(FragmentType type) {
        ownedFragments.remove(type);
    }

    /**
     * Check if player owns a Fragment (can use it)
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

    // ═══════════════════════════════════════════════════════════════
    // CHARGED FRAGMENTS (ritual complete, ready to activate)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Charge a Fragment (ritual completed)
     */
    public void chargeFragment(FragmentType type) {
        chargedFragments.add(type);
    }

    /**
     * Uncharge a Fragment (activated or expired)
     */
    public void unchargeFragment(FragmentType type) {
        chargedFragments.remove(type);
    }

    /**
     * Check if a Fragment is charged
     */
    public boolean isCharged(FragmentType type) {
        return chargedFragments.contains(type);
    }

    /**
     * Get all charged Fragments
     */
    public Collection<FragmentType> getChargedFragments() {
        return new HashSet<>(chargedFragments);
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTIVE FRAGMENT
    // ═══════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════
    // COMPLETED RITUALS (one-time fragment creation tracking)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Mark a fragment ritual as completed (one-time only)
     */
    public void markRitualCompleted(FragmentType type) {
        completedRituals.add(type);
    }

    /**
     * Check if a fragment ritual has been completed before
     */
    public boolean hasCompletedRitual(FragmentType type) {
        return completedRituals.contains(type);
    }

    /**
     * Get all completed rituals
     */
    public Collection<FragmentType> getCompletedRituals() {
        return new HashSet<>(completedRituals);
    }
}
