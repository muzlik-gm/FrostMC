package com.muzlik.texture;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilitySlot;

/**
 * Unicode symbols for Fragments and abilities.
 * These work without a resource pack and provide clear, meaningful icons.
 */
public class FragmentSymbols {
    
    // Fragment symbols - geometric shapes with clear meanings
    public static final String FIRE = "▲";      // Triangle - flames point up
    public static final String WATER = "▼";     // Inverted triangle - water flows down
    public static final String AIR = "○";       // Circle - wind is circular
    public static final String EARTH = "■";     // Square - solid and stable
    public static final String DARK = "●";      // Filled circle - darkness/void
    public static final String LIGHT = "★";     // Star - radiance
    public static final String VOID = "◆";      // Diamond - dimensional rift
    public static final String MOB = "⬡";       // Hexagon - organic forms
    public static final String DRAGON = "⬠";    // Pentagon - power/majesty
    public static final String STORM = "⚡";     // Lightning - electricity
    
    // Ability slot symbols
    public static final String PRIMARY = "⚔";   // Sword - basic attack
    public static final String SECONDARY = "🛡"; // Shield - defense/utility
    public static final String ULTIMATE = "✦";  // Burst - powerful ability
    public static final String ADVANCED = "ᚱ";  // Rune - advanced technique
    public static final String MASTERY = "♔";   // Crown - mastery level
    
    // Status indicators
    public static final String UNLOCKED = "✓";
    public static final String LOCKED = "✗";
    public static final String WARNING = "⚠";
    public static final String COOLDOWN = "⏱";
    public static final String ACTIVE = "✦";
    
    // Rank badges
    public static final String RANK_BRONZE = "◆";
    public static final String RANK_SILVER = "◆";
    public static final String RANK_GOLD = "★";
    public static final String RANK_DIAMOND = "◈";
    public static final String RANK_MASTER = "✦";
    
    // Mana indicators
    public static final String MANA_FULL = "█";
    public static final String MANA_EMPTY = "░";
    
    /**
     * Get symbol for Fragment type
     */
    public static String getFragmentSymbol(FragmentType type) {
        return switch (type) {
            case FIRE -> FIRE;
            case WATER -> WATER;
            case AIR -> AIR;
            case EARTH -> EARTH;
            case DARK -> DARK;
            case LIGHT -> LIGHT;
            case VOID -> VOID;
            case MOB -> MOB;
            case DRAGON -> DRAGON;
            case STORM -> STORM;
            case ADMIN -> "⚠"; // Admin fragment symbol
        };
    }
    
    /**
     * Get colored symbol for Fragment type
     */
    public static String getColoredFragmentSymbol(FragmentType type) {
        String symbol = getFragmentSymbol(type);
        String color = getFragmentColor(type);
        return color + symbol;
    }
    
    /**
     * Get color code for Fragment type
     */
    public static String getFragmentColor(FragmentType type) {
        return switch (type) {
            case FIRE -> "§c";      // Red
            case WATER -> "§9";     // Blue
            case AIR -> "§f";       // White
            case EARTH -> "§6";     // Gold/Brown
            case DARK -> "§0";      // Black (was Purple)
            case LIGHT -> "§e";     // Yellow
            case VOID -> "§5";      // Purple (was Magenta)
            case MOB -> "§a";       // Green
            case DRAGON -> "§c";    // Red
            case STORM -> "§b";     // Cyan
            case ADMIN -> "§4";     // Dark Red
        };
    }
    
    /**
     * Get symbol for ability slot
     */
    public static String getAbilitySlotSymbol(AbilitySlot slot) {
        return switch (slot) {
            case PRIMARY -> PRIMARY;
            case SECONDARY -> SECONDARY;
            case ULTIMATE -> ULTIMATE;
            case ADVANCED -> ADVANCED;
            case MASTERY -> MASTERY;
            case SLOT_6, SLOT_7, SLOT_8, SLOT_9 -> "⚠"; // Admin slots
        };
    }
    
    /**
     * Get rank badge symbol based on rank
     */
    public static String getRankSymbol(int rank, int maxRank) {
        if (rank >= maxRank) {
            return "§d" + RANK_MASTER; // Master - Magenta
        } else if (rank >= maxRank - 1) {
            return "§b" + RANK_DIAMOND; // Diamond - Cyan
        } else if (rank >= (maxRank / 2) + 1) {
            return "§e" + RANK_GOLD; // Gold - Yellow
        } else if (rank >= (maxRank / 2)) {
            return "§7" + RANK_SILVER; // Silver - Gray
        } else {
            return "§6" + RANK_BRONZE; // Bronze - Gold
        }
    }
    
    /**
     * Build mana bar with symbols
     */
    public static String buildManaBar(double current, double max, int length) {
        if (max <= 0) return "§7" + MANA_EMPTY.repeat(length);
        
        double percentage = current / max;
        int filled = (int) (percentage * length);
        int empty = length - filled;
        
        String color = percentage >= 0.5 ? "§b" : (percentage >= 0.25 ? "§e" : "§c");
        return color + MANA_FULL.repeat(Math.max(0, filled)) + "§7" + MANA_EMPTY.repeat(Math.max(0, empty));
    }
    
    /**
     * Build XP progress bar with symbols
     */
    public static String buildProgressBar(double current, double max, int length) {
        if (max <= 0) return "§7" + MANA_EMPTY.repeat(length);
        
        double percentage = current / max;
        int filled = (int) (percentage * length);
        int empty = length - filled;
        
        return "§a" + MANA_FULL.repeat(Math.max(0, filled)) + "§7" + MANA_EMPTY.repeat(Math.max(0, empty));
    }
}
