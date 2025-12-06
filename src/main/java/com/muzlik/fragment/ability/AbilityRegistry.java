package com.muzlik.fragment.ability;

import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Centralized registry for all abilities across all Fragments.
 * Provides ability lookup, validation, and requirement checking.
 */
public class AbilityRegistry {
    private final JavaPlugin plugin;
    private final Map<String, AbilityDefinition> abilities;
    private final Map<FragmentType, List<AbilityDefinition>> abilitiesByFragment;

    public AbilityRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.abilities = new ConcurrentHashMap<>();
        this.abilitiesByFragment = new ConcurrentHashMap<>();
    }

    /**
     * Register an ability
     */
    public void registerAbility(AbilityDefinition ability) {
        if (ability == null) {
            throw new IllegalArgumentException("Ability cannot be null");
        }
        
        String id = ability.getId();
        if (abilities.containsKey(id)) {
            plugin.getLogger().warning("Ability with ID '" + id + "' is already registered!");
            return;
        }
        
        abilities.put(id, ability);
        
        // Add to Fragment-specific list
        abilitiesByFragment.computeIfAbsent(ability.getFragmentType(), 
            type -> new ArrayList<>()).add(ability);
        
        plugin.getLogger().info("Ability registered: " + ability.getDisplayName() + 
            " (" + ability.getFragmentType() + " - " + ability.getSlot() + ")");
    }

    /**
     * Get an ability by ID
     */
    public AbilityDefinition getAbility(String abilityId) {
        return abilities.get(abilityId);
    }

    /**
     * Get all abilities for a Fragment
     */
    public Collection<AbilityDefinition> getAbilitiesForFragment(FragmentType type) {
        List<AbilityDefinition> fragmentAbilities = abilitiesByFragment.get(type);
        return fragmentAbilities != null ? new ArrayList<>(fragmentAbilities) : Collections.emptyList();
    }

    /**
     * Get available abilities for a player (based on level and rank)
     */
    public Collection<AbilityDefinition> getAvailableAbilities(Player player, FragmentType type, 
                                                               int playerLevel, int playerRank) {
        Collection<AbilityDefinition> fragmentAbilities = getAbilitiesForFragment(type);
        
        return fragmentAbilities.stream()
            .filter(ability -> meetsRequirements(playerLevel, playerRank, ability))
            .collect(Collectors.toList());
    }

    /**
     * Check if player meets requirements for an ability
     */
    public boolean meetsRequirements(Player player, AbilityDefinition ability, 
                                    int playerLevel, int playerRank, double playerMana) {
        // Check level requirement
        if (playerLevel < ability.getLevelRequirement()) {
            return false;
        }
        
        // Check rank requirement
        if (playerRank < ability.getRankRequirement()) {
            return false;
        }
        
        // Check mana requirement
        if (playerMana < ability.getManaCost()) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if player meets level and rank requirements (without mana check)
     */
    private boolean meetsRequirements(int playerLevel, int playerRank, AbilityDefinition ability) {
        return playerLevel >= ability.getLevelRequirement() && 
               playerRank >= ability.getRankRequirement();
    }

    /**
     * Get all registered abilities
     */
    public Collection<AbilityDefinition> getAllAbilities() {
        return new ArrayList<>(abilities.values());
    }

    /**
     * Get abilities by slot for a Fragment
     */
    public Collection<AbilityDefinition> getAbilitiesBySlot(FragmentType type, AbilitySlot slot) {
        return getAbilitiesForFragment(type).stream()
            .filter(ability -> ability.getSlot() == slot)
            .collect(Collectors.toList());
    }

    /**
     * Check if Fragment has required ability slots
     */
    public boolean hasRequiredAbilities(FragmentType type) {
        Collection<AbilityDefinition> fragmentAbilities = getAbilitiesForFragment(type);
        
        boolean hasPrimary = fragmentAbilities.stream()
            .anyMatch(ability -> ability.getSlot() == AbilitySlot.PRIMARY);
        
        boolean hasSecondary = fragmentAbilities.stream()
            .anyMatch(ability -> ability.getSlot() == AbilitySlot.SECONDARY);
        
        return hasPrimary && hasSecondary;
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        abilities.clear();
        abilitiesByFragment.clear();
    }
}
