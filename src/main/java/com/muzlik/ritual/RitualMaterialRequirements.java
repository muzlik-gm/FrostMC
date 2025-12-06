package com.muzlik.ritual;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines material requirements for different ritual types.
 * Based on the anime RPG style progression system.
 */
public class RitualMaterialRequirements {

    /**
     * Get material requirements for a ritual type
     * @param ritualType The type of ritual
     * @param fragmentType The Fragment type (for Fragment-specific rituals)
     * @param currentRank The current rank (for rank-dependent requirements)
     * @return List of material requirements
     */
    public static List<MaterialRequirement> getRequirements(RitualType ritualType, 
                                                            FragmentType fragmentType, 
                                                            int currentRank) {
        List<MaterialRequirement> requirements = new ArrayList<>();

        switch (ritualType) {
            case FRAGMENT_CREATION:
                requirements.addAll(getFragmentCreationRequirements(fragmentType));
                break;

            case FRAGMENT_CHANGER:
                requirements.addAll(getFragmentChangerRequirements());
                break;

            case RANK_UP:
                requirements.addAll(getRankUpRequirements(currentRank));
                break;

            case ABILITY_EXPANSION:
                requirements.addAll(getAbilityExpansionRequirements(fragmentType, currentRank));
                break;

            case MASTERY_EXPANSION:
                requirements.addAll(getMasteryExpansionRequirements(fragmentType, currentRank));
                break;

            case CUSTOM:
                // Custom rituals define their own requirements
                break;
        }

        return requirements;
    }

    /**
     * Get requirements for Fragment Creation ritual
     */
    private static List<MaterialRequirement> getFragmentCreationRequirements(FragmentType fragmentType) {
        List<MaterialRequirement> requirements = new ArrayList<>();
        
        // Base requirements for all Fragments
        requirements.add(new MaterialRequirement(Material.DIAMOND, 4));
        requirements.add(new MaterialRequirement(Material.GOLD_BLOCK, 2));
        
        // Fragment-specific materials
        switch (fragmentType) {
            case FIRE:
                requirements.add(new MaterialRequirement(Material.BLAZE_ROD, 8));
                requirements.add(new MaterialRequirement(Material.FIRE_CHARGE, 16));
                break;
            case WATER:
                requirements.add(new MaterialRequirement(Material.PRISMARINE_CRYSTALS, 8));
                requirements.add(new MaterialRequirement(Material.HEART_OF_THE_SEA, 1));
                break;
            case AIR:
                requirements.add(new MaterialRequirement(Material.FEATHER, 32));
                requirements.add(new MaterialRequirement(Material.PHANTOM_MEMBRANE, 4));
                break;
            case EARTH:
                requirements.add(new MaterialRequirement(Material.EMERALD, 8));
                requirements.add(new MaterialRequirement(Material.CLAY_BALL, 32));
                break;
            case DARK:
                requirements.add(new MaterialRequirement(Material.WITHER_SKELETON_SKULL, 2));
                requirements.add(new MaterialRequirement(Material.OBSIDIAN, 16));
                break;
            case LIGHT:
                requirements.add(new MaterialRequirement(Material.BEACON, 1));
                requirements.add(new MaterialRequirement(Material.GLOWSTONE, 32));
                break;
            case STORM:
                requirements.add(new MaterialRequirement(Material.TRIDENT, 1));
                requirements.add(new MaterialRequirement(Material.LIGHTNING_ROD, 4));
                break;
            case VOID:
                requirements.add(new MaterialRequirement(Material.ENDER_PEARL, 16));
                requirements.add(new MaterialRequirement(Material.END_CRYSTAL, 2));
                break;
            case MOB:
                requirements.add(new MaterialRequirement(Material.TOTEM_OF_UNDYING, 1));
                requirements.add(new MaterialRequirement(Material.BONE, 64));
                break;
            case DRAGON:
                requirements.add(new MaterialRequirement(Material.DRAGON_HEAD, 1));
                requirements.add(new MaterialRequirement(Material.DRAGON_EGG, 1));
                requirements.add(new MaterialRequirement(Material.NETHER_STAR, 4));
                break;
        }

        return requirements;
    }

    /**
     * Get requirements for Fragment Changer ritual
     */
    private static List<MaterialRequirement> getFragmentChangerRequirements() {
        List<MaterialRequirement> requirements = new ArrayList<>();
        requirements.add(new MaterialRequirement(Material.ENDER_PEARL, 4));
        requirements.add(new MaterialRequirement(Material.GOLD_INGOT, 8));
        return requirements;
    }

    /**
     * Get requirements for standard Rank Up ritual
     */
    private static List<MaterialRequirement> getRankUpRequirements(int currentRank) {
        List<MaterialRequirement> requirements = new ArrayList<>();
        
        // Base requirements
        requirements.add(new MaterialRequirement(Material.DIAMOND, currentRank + 2));
        requirements.add(new MaterialRequirement(Material.EMERALD, currentRank + 1));
        
        // Higher ranks require more rare materials
        if (currentRank >= 5) {
            requirements.add(new MaterialRequirement(Material.NETHERITE_INGOT, 1));
        }
        if (currentRank >= 8) {
            requirements.add(new MaterialRequirement(Material.NETHER_STAR, 1));
        }

        return requirements;
    }

    /**
     * Get requirements for Ability Expansion ritual (unlocks slot 3)
     */
    private static List<MaterialRequirement> getAbilityExpansionRequirements(FragmentType fragmentType, 
                                                                             int currentRank) {
        List<MaterialRequirement> requirements = new ArrayList<>();
        
        // Expensive base requirements
        requirements.add(new MaterialRequirement(Material.NETHER_STAR, 1));
        requirements.add(new MaterialRequirement(Material.TOTEM_OF_UNDYING, 1));
        requirements.add(new MaterialRequirement(Material.DIAMOND_BLOCK, 2));
        
        // Fragment-specific rare materials
        switch (fragmentType) {
            case FIRE:
                requirements.add(new MaterialRequirement(Material.BLAZE_ROD, 16));
                break;
            case WATER:
                requirements.add(new MaterialRequirement(Material.HEART_OF_THE_SEA, 2));
                break;
            case AIR:
                requirements.add(new MaterialRequirement(Material.ELYTRA, 1));
                break;
            case EARTH:
                requirements.add(new MaterialRequirement(Material.EMERALD_BLOCK, 2));
                break;
            case DARK:
                requirements.add(new MaterialRequirement(Material.WITHER_SKELETON_SKULL, 3));
                break;
            case LIGHT:
                requirements.add(new MaterialRequirement(Material.BEACON, 1));
                break;
            case STORM:
                requirements.add(new MaterialRequirement(Material.TRIDENT, 1));
                break;
            case VOID:
                requirements.add(new MaterialRequirement(Material.END_CRYSTAL, 4));
                break;
            case MOB:
                requirements.add(new MaterialRequirement(Material.TOTEM_OF_UNDYING, 2));
                break;
            case DRAGON:
                requirements.add(new MaterialRequirement(Material.DRAGON_HEAD, 1));
                break;
        }

        return requirements;
    }

    /**
     * Get requirements for Mastery Expansion ritual (unlocks slot 4)
     */
    private static List<MaterialRequirement> getMasteryExpansionRequirements(FragmentType fragmentType, 
                                                                             int currentRank) {
        List<MaterialRequirement> requirements = new ArrayList<>();
        
        // Extremely expensive base requirements
        requirements.add(new MaterialRequirement(Material.NETHER_STAR, 2));
        requirements.add(new MaterialRequirement(Material.BEACON, 1));
        requirements.add(new MaterialRequirement(Material.ELYTRA, 1));
        requirements.add(new MaterialRequirement(Material.NETHERITE_BLOCK, 2));
        
        // Fragment-specific legendary materials
        switch (fragmentType) {
            case FIRE:
                requirements.add(new MaterialRequirement(Material.BLAZE_ROD, 32));
                requirements.add(new MaterialRequirement(Material.FIRE_CHARGE, 64));
                break;
            case WATER:
                requirements.add(new MaterialRequirement(Material.HEART_OF_THE_SEA, 3));
                requirements.add(new MaterialRequirement(Material.TRIDENT, 1));
                break;
            case AIR:
                requirements.add(new MaterialRequirement(Material.PHANTOM_MEMBRANE, 16));
                requirements.add(new MaterialRequirement(Material.ELYTRA, 1));
                break;
            case EARTH:
                requirements.add(new MaterialRequirement(Material.EMERALD_BLOCK, 4));
                requirements.add(new MaterialRequirement(Material.DIAMOND_BLOCK, 4));
                break;
            case DARK:
                requirements.add(new MaterialRequirement(Material.WITHER_SKELETON_SKULL, 6));
                requirements.add(new MaterialRequirement(Material.NETHER_STAR, 1));
                break;
            case LIGHT:
                requirements.add(new MaterialRequirement(Material.BEACON, 2));
                requirements.add(new MaterialRequirement(Material.GLOWSTONE, 64));
                break;
            case STORM:
                requirements.add(new MaterialRequirement(Material.TRIDENT, 2));
                requirements.add(new MaterialRequirement(Material.LIGHTNING_ROD, 8));
                break;
            case VOID:
                requirements.add(new MaterialRequirement(Material.END_CRYSTAL, 8));
                requirements.add(new MaterialRequirement(Material.ENDER_PEARL, 64));
                break;
            case MOB:
                requirements.add(new MaterialRequirement(Material.TOTEM_OF_UNDYING, 3));
                requirements.add(new MaterialRequirement(Material.BONE_BLOCK, 16));
                break;
            case DRAGON:
                requirements.add(new MaterialRequirement(Material.DRAGON_HEAD, 2));
                requirements.add(new MaterialRequirement(Material.DRAGON_EGG, 1));
                requirements.add(new MaterialRequirement(Material.NETHER_STAR, 3));
                break;
        }

        return requirements;
    }
}
