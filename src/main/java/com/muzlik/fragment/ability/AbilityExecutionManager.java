package com.muzlik.fragment.ability;

import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.PlayerFragmentData;
import com.muzlik.mana.ManaManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manages the execution of Fragment abilities.
 * Handles validation, mana consumption, cooldowns, and effect application.
 */
public class AbilityExecutionManager {

    private final ManaManager manaManager;
    private final CooldownManager cooldownManager;
    private final AbilityScalingEngine scalingEngine;
    private final FragmentManager fragmentManager;

    public AbilityExecutionManager(ManaManager manaManager, CooldownManager cooldownManager, 
                                   FragmentManager fragmentManager) {
        this.manaManager = manaManager;
        this.cooldownManager = cooldownManager;
        this.scalingEngine = new AbilityScalingEngine();
        this.fragmentManager = fragmentManager;
    }

    /**
     * Execute an ability for a player
     * @param player The player using the ability
     * @param ability The ability definition
     * @param rank The current Fragment rank
     * @return true if execution was successful, false otherwise
     */
    public boolean executeAbility(Player player, AbilityDefinition ability, int rank) {
        // 1. Validate mana
        if (!hasEnoughMana(player, ability, rank)) {
            double required = scalingEngine.scaleManaCost(ability.getManaCost(), rank);
            double current = manaManager.getMana(player);
            player.sendMessage(ChatColor.RED + "Not enough mana: " + 
                String.format("%.1f/%.1f", current, required));
            return false;
        }

        // 2. Check cooldown
        if (isOnCooldown(player, ability)) {
            double remaining = cooldownManager.getRemainingCooldownSeconds(player, ability.getId());
            player.sendMessage(ChatColor.RED + "Ability on cooldown: " + 
                String.format("%.1fs remaining", remaining));
            return false;
        }

        // 3. Check requirements
        if (!meetsRequirements(player, ability, rank)) {
            if (player.getLevel() < ability.getLevelRequirement()) {
                player.sendMessage(ChatColor.RED + "Requires Level " + ability.getLevelRequirement());
            } else if (rank < ability.getRankRequirement()) {
                player.sendMessage(ChatColor.RED + "Requires Rank " + ability.getRankRequirement());
            }
            return false;
        }

        // 4. Consume mana
        double manaCost = scalingEngine.scaleManaCost(ability.getManaCost(), rank);
        manaManager.consumeMana(player, manaCost);

        // 5. Start cooldown
        cooldownManager.startCooldown(player, ability.getId(), ability.getCooldown());

        // 6. Execute with scaling
        AbilityContext context = createContext(player, ability, rank);
        ability.getExecutor().execute(context);

        return true;
    }

    /**
     * Check if player has enough mana for the ability
     */
    private boolean hasEnoughMana(Player player, AbilityDefinition ability, int rank) {
        double required = scalingEngine.scaleManaCost(ability.getManaCost(), rank);
        double current = manaManager.getMana(player);
        return current >= required;
    }

    /**
     * Check if ability is on cooldown
     */
    private boolean isOnCooldown(Player player, AbilityDefinition ability) {
        return cooldownManager.isOnCooldown(player, ability.getId());
    }

    /**
     * Check if player meets ability requirements
     */
    private boolean meetsRequirements(Player player, AbilityDefinition ability, int rank) {
        return player.getLevel() >= ability.getLevelRequirement() && 
               rank >= ability.getRankRequirement();
    }

    /**
     * Create execution context with scaled values
     */
    private AbilityContext createContext(Player player, AbilityDefinition ability, int rank) {
        PlayerFragmentData fragmentData = fragmentManager.getPlayerData(player);
        
        AbilityContext context = new AbilityContext(player, ability, fragmentData);
        context.setRank(rank);
        context.setScalingEngine(scalingEngine);
        
        return context;
    }

    public AbilityScalingEngine getScalingEngine() {
        return scalingEngine;
    }
}
