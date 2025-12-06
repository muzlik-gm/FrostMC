package com.muzlik.texture;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.AbilitySlot;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for custom model data (textures) used throughout the plugin.
 * Uses a single base item (PAPER) with different CustomModelData values to display different textures.
 * 
 * This allows for a resource pack to define custom textures for:
 * - Fragment icons
 * - Ability icons
 * - Mana indicators
 * - UI elements
 * 
 * CustomModelData Range Allocation:
 * - 1000-1099: Fragment icons
 * - 2000-2999: Ability icons (organized by Fragment)
 * - 3000-3099: Mana indicators
 * - 4000-4099: UI elements
 */
public class TextureRegistry {
    
    // Base material for all custom textures
    public static final Material BASE_MATERIAL = Material.PAPER;
    
    // Fragment icon custom model data (1000-1099)
    private static final Map<FragmentType, Integer> FRAGMENT_TEXTURES = new HashMap<>();
    
    // Ability icon custom model data (2000-2999)
    private static final Map<String, Integer> ABILITY_TEXTURES = new HashMap<>();
    
    // Mana indicator custom model data (3000-3099)
    private static final Map<String, Integer> MANA_TEXTURES = new HashMap<>();
    
    // UI element custom model data (4000-4099)
    private static final Map<String, Integer> UI_TEXTURES = new HashMap<>();
    
    static {
        initializeFragmentTextures();
        initializeAbilityTextures();
        initializeManaTextures();
        initializeUITextures();
    }
    
    /**
     * Initialize Fragment icon textures (1000-1099)
     */
    private static void initializeFragmentTextures() {
        FRAGMENT_TEXTURES.put(FragmentType.FIRE, 1000);
        FRAGMENT_TEXTURES.put(FragmentType.WATER, 1001);
        FRAGMENT_TEXTURES.put(FragmentType.AIR, 1002);
        FRAGMENT_TEXTURES.put(FragmentType.EARTH, 1003);
        FRAGMENT_TEXTURES.put(FragmentType.DARK, 1004);
        FRAGMENT_TEXTURES.put(FragmentType.LIGHT, 1005);
        FRAGMENT_TEXTURES.put(FragmentType.VOID, 1006);
        FRAGMENT_TEXTURES.put(FragmentType.MOB, 1007);
        FRAGMENT_TEXTURES.put(FragmentType.DRAGON, 1008);
        FRAGMENT_TEXTURES.put(FragmentType.STORM, 1009);
    }
    
    /**
     * Initialize ability icon textures (2000-2999)
     * Each Fragment gets 100 slots for abilities
     */
    private static void initializeAbilityTextures() {
        // Fire Fragment abilities (2000-2099)
        ABILITY_TEXTURES.put("fire_flame_burst", 2000);
        ABILITY_TEXTURES.put("fire_blazing_step", 2001);
        ABILITY_TEXTURES.put("fire_inferno_maelstrom", 2002);
        ABILITY_TEXTURES.put("fire_phoenix_rebirth", 2003);
        ABILITY_TEXTURES.put("fire_eternal_flame", 2004);
        ABILITY_TEXTURES.put("fire_fire_dome", 2005);
        
        // Water Fragment abilities (2100-2199)
        ABILITY_TEXTURES.put("water_aqua_pulse", 2100);
        ABILITY_TEXTURES.put("water_tidal_shield", 2101);
        ABILITY_TEXTURES.put("water_tsunami_wave", 2102);
        
        // Air Fragment abilities (2200-2299)
        ABILITY_TEXTURES.put("air_wind_blade", 2200);
        ABILITY_TEXTURES.put("air_gale_step", 2201);
        ABILITY_TEXTURES.put("air_tempest_barrage", 2202);
        ABILITY_TEXTURES.put("air_cyclone_armor", 2203);
        ABILITY_TEXTURES.put("air_storm_sovereign", 2204);
        
        // Earth Fragment abilities (2300-2399)
        ABILITY_TEXTURES.put("earth_stone_fist", 2300);
        ABILITY_TEXTURES.put("earth_seismic_slam", 2301);
        ABILITY_TEXTURES.put("earth_earthen_fortress", 2302);
        ABILITY_TEXTURES.put("earth_terra_shaper", 2303);
        
        // Dark Fragment abilities (2400-2499)
        ABILITY_TEXTURES.put("dark_shadow_strike", 2400);
        ABILITY_TEXTURES.put("dark_vampiric_drain", 2401);
        ABILITY_TEXTURES.put("dark_abyssal_void", 2402);
        ABILITY_TEXTURES.put("dark_shadow_clone", 2403);
        ABILITY_TEXTURES.put("dark_eternal_darkness", 2404);
        
        // Light Fragment abilities (2500-2599)
        ABILITY_TEXTURES.put("light_radiant_lance", 2500);
        ABILITY_TEXTURES.put("light_divine_blessing", 2501);
        ABILITY_TEXTURES.put("light_celestial_judgment", 2502);
        ABILITY_TEXTURES.put("light_holy_sanctuary", 2503);
        ABILITY_TEXTURES.put("light_seraph_ascension", 2504);
        
        // Void Fragment abilities (2600-2699)
        ABILITY_TEXTURES.put("void_void_slash", 2600);
        ABILITY_TEXTURES.put("void_blink_step", 2601);
        ABILITY_TEXTURES.put("void_dimensional_collapse", 2602);
        ABILITY_TEXTURES.put("void_spatial_manipulation", 2603);
        
        // Mob Fragment abilities (2700-2799)
        ABILITY_TEXTURES.put("mob_beast_summon", 2700);
        ABILITY_TEXTURES.put("mob_pack_call", 2701);
        ABILITY_TEXTURES.put("mob_iron_golem_guardian", 2702);
        ABILITY_TEXTURES.put("mob_legion_of_shadows", 2703);
        
        // Dragon Fragment abilities (2800-2899)
        ABILITY_TEXTURES.put("dragon_dragons_roar", 2800);
        ABILITY_TEXTURES.put("dragon_draconic_wings", 2801);
        ABILITY_TEXTURES.put("dragon_dragon_meteor", 2802);
        ABILITY_TEXTURES.put("dragon_cataclysm", 2803);
        
        // Storm Fragment abilities (2900-2999)
        ABILITY_TEXTURES.put("storm_lightning_bolt", 2900);
        ABILITY_TEXTURES.put("storm_chain_lightning", 2901);
        ABILITY_TEXTURES.put("storm_storm_surge", 2902);
        ABILITY_TEXTURES.put("storm_thunderstorm_descent", 2903);
        
        // Generic ability slot icons (2950-2959)
        ABILITY_TEXTURES.put("ability_primary", 2950);
        ABILITY_TEXTURES.put("ability_secondary", 2951);
        ABILITY_TEXTURES.put("ability_ultimate", 2952);
        ABILITY_TEXTURES.put("ability_advanced", 2953);
        ABILITY_TEXTURES.put("ability_mastery", 2954);
        ABILITY_TEXTURES.put("ability_locked", 2955);
        ABILITY_TEXTURES.put("ability_cooldown", 2956);
    }
    
    /**
     * Initialize mana indicator textures (3000-3099)
     */
    private static void initializeManaTextures() {
        MANA_TEXTURES.put("mana_full", 3000);
        MANA_TEXTURES.put("mana_high", 3001);
        MANA_TEXTURES.put("mana_medium", 3002);
        MANA_TEXTURES.put("mana_low", 3003);
        MANA_TEXTURES.put("mana_empty", 3004);
        MANA_TEXTURES.put("mana_flask", 3005);
        MANA_TEXTURES.put("mana_icon", 3006);
    }
    
    /**
     * Initialize UI element textures (4000-4099)
     */
    private static void initializeUITextures() {
        UI_TEXTURES.put("ui_back_button", 4000);
        UI_TEXTURES.put("ui_info_button", 4001);
        UI_TEXTURES.put("ui_close_button", 4002);
        UI_TEXTURES.put("ui_locked_icon", 4003);
        UI_TEXTURES.put("ui_unlocked_icon", 4004);
        UI_TEXTURES.put("ui_warning_icon", 4005);
        UI_TEXTURES.put("ui_rank_badge_bronze", 4010);
        UI_TEXTURES.put("ui_rank_badge_silver", 4011);
        UI_TEXTURES.put("ui_rank_badge_gold", 4012);
        UI_TEXTURES.put("ui_rank_badge_diamond", 4013);
        UI_TEXTURES.put("ui_rank_badge_master", 4014);
        UI_TEXTURES.put("ui_border_top", 4020);
        UI_TEXTURES.put("ui_border_bottom", 4021);
        UI_TEXTURES.put("ui_border_left", 4022);
        UI_TEXTURES.put("ui_border_right", 4023);
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Get custom model data for a Fragment icon
     */
    public static int getFragmentTexture(FragmentType type) {
        return FRAGMENT_TEXTURES.getOrDefault(type, 1000);
    }
    
    /**
     * Get custom model data for an ability icon
     */
    public static int getAbilityTexture(String abilityId) {
        return ABILITY_TEXTURES.getOrDefault(abilityId, 2950); // Default to generic ability icon
    }
    
    /**
     * Get custom model data for an ability by Fragment and slot
     */
    public static int getAbilityTextureBySlot(FragmentType fragmentType, AbilitySlot slot) {
        String key = "ability_" + slot.name().toLowerCase();
        return ABILITY_TEXTURES.getOrDefault(key, 2950);
    }
    
    /**
     * Get custom model data for a mana indicator
     */
    public static int getManaTexture(String type) {
        return MANA_TEXTURES.getOrDefault(type, 3000);
    }
    
    /**
     * Get custom model data for a mana indicator based on percentage
     */
    public static int getManaTextureByPercentage(double percentage) {
        if (percentage >= 0.8) {
            return MANA_TEXTURES.get("mana_full");
        } else if (percentage >= 0.5) {
            return MANA_TEXTURES.get("mana_high");
        } else if (percentage >= 0.3) {
            return MANA_TEXTURES.get("mana_medium");
        } else if (percentage > 0) {
            return MANA_TEXTURES.get("mana_low");
        } else {
            return MANA_TEXTURES.get("mana_empty");
        }
    }
    
    /**
     * Get custom model data for a UI element
     */
    public static int getUITexture(String element) {
        return UI_TEXTURES.getOrDefault(element, 4000);
    }
    
    /**
     * Get base material for all custom textures
     */
    public static Material getBaseMaterial() {
        return BASE_MATERIAL;
    }
    
    /**
     * Check if a custom model data value is registered
     */
    public static boolean isRegistered(int customModelData) {
        return FRAGMENT_TEXTURES.containsValue(customModelData) ||
               ABILITY_TEXTURES.containsValue(customModelData) ||
               MANA_TEXTURES.containsValue(customModelData) ||
               UI_TEXTURES.containsValue(customModelData);
    }
    
    /**
     * Get all registered Fragment textures
     */
    public static Map<FragmentType, Integer> getAllFragmentTextures() {
        return new HashMap<>(FRAGMENT_TEXTURES);
    }
    
    /**
     * Get all registered ability textures
     */
    public static Map<String, Integer> getAllAbilityTextures() {
        return new HashMap<>(ABILITY_TEXTURES);
    }
    
    /**
     * Get all registered mana textures
     */
    public static Map<String, Integer> getAllManaTextures() {
        return new HashMap<>(MANA_TEXTURES);
    }
    
    /**
     * Get all registered UI textures
     */
    public static Map<String, Integer> getAllUITextures() {
        return new HashMap<>(UI_TEXTURES);
    }
}
