package com.muzlik.util;

/**
 * Utility class for consistent typography and text formatting across the plugin.
 * Enforces the "Small Caps" style for titles and specific color schemes.
 */
public class Typography {

    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
    private static final String NORMAL = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Convert text to small caps unicode
     */
    public static String toSmallCaps(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toLowerCase().toCharArray()) {
            int index = NORMAL.indexOf(c);
            if (index >= 0) {
                result.append(SMALL_CAPS.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Convert small caps unicode back to normal characters
     */
    public static String fromSmallCaps(String smallCaps) {
        StringBuilder result = new StringBuilder();
        for (char c : smallCaps.toCharArray()) {
            int index = SMALL_CAPS.indexOf(c);
            if (index >= 0) {
                result.append(NORMAL.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Standard Colors
    public static final String COLOR_PRIMARY = "§b"; // Aqua
    public static final String COLOR_SECONDARY = "§e"; // Yellow
    public static final String COLOR_ACCENT = "§6"; // Gold
    public static final String COLOR_SUCCESS = "§a"; // Green
    public static final String COLOR_ERROR = "§c"; // Red
    public static final String COLOR_TEXT = "§7"; // Gray
    public static final String COLOR_TEXT_DARK = "§8"; // Dark Gray
    public static final String COLOR_HIGHLIGHT = "§f"; // White

    // Standard Symbols
    public static final String SYMBOL_CHECK = "✓";
    public static final String SYMBOL_CROSS = "✗";
    public static final String SYMBOL_WARNING = "⚠";
    public static final String SYMBOL_LIGHTNING = "⚡";
    public static final String SYMBOL_STAR = "★";
    public static final String SYMBOL_DIAMOND = "◆";
    public static final String SYMBOL_ARROW = "→";
    public static final String SYMBOL_DOT = "○";
    public static final String SYMBOL_COOLDOWN = "⏱";
    public static final String SYMBOL_SWORD = "⚔";

    /**
     * Format a title string (Small Caps + Dark Gray)
     */
    public static String formatTitle(String title) {
        return COLOR_TEXT_DARK + toSmallCaps(title);
    }
    
    /**
     * Format a menu title (Uppercase + Dark Gray) - for Ability Views
     */
    public static String formatMenuTitle(String title) {
        return COLOR_TEXT_DARK + title.toUpperCase();
    }

    /**
     * Format an item name (White/Color + Small Caps)
     */
    public static String formatItemName(String name, String color) {
        return color + toSmallCaps(name);
    }
    
    /**
     * Format an item name (White/Color + Uppercase) - Alternative style
     */
    public static String formatItemNameUppercase(String name, String color) {
        return color + name.toUpperCase();
    }

    /**
     * Format a success message
     */
    public static String formatSuccess(String message) {
        return COLOR_SUCCESS + SYMBOL_CHECK + " " + toSmallCaps(message);
    }

    /**
     * Format an error message
     */
    public static String formatError(String message) {
        return COLOR_ERROR + SYMBOL_CROSS + " " + toSmallCaps(message);
    }
    
    /**
     * Format a warning message
     */
    public static String formatWarning(String message) {
        return COLOR_SECONDARY + SYMBOL_WARNING + " " + toSmallCaps(message);
    }
    
    /**
     * Format a label (e.g. "Mana Cost:")
     */
    public static String formatLabel(String label) {
        return COLOR_TEXT + toSmallCaps(label);
    }
    
    /**
     * Format a value (e.g. "50")
     */
    public static String formatValue(String value) {
        return COLOR_HIGHLIGHT + value;
    }
}
