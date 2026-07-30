package com.muzlik.fragment;

import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all Fragment-related operations.
 * Manages Fragment registration, ownership, and activation.
 */
public class FragmentManager {
    private final JavaPlugin plugin;
    private final Map<FragmentType, FragmentDefinition> registeredFragments;
    private final Map<UUID, PlayerFragmentData> playerFragmentData;
    private final ManaManager manaManager;
    private final LevelManager levelManager;
    private final RankManager rankManager;

    public FragmentManager(JavaPlugin plugin, ManaManager manaManager, 
                          LevelManager levelManager, RankManager rankManager) {
        this.plugin = plugin;
        this.registeredFragments = new ConcurrentHashMap<>();
        this.playerFragmentData = new ConcurrentHashMap<>();
        this.manaManager = manaManager;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
    }

    /**
     * Register a Fragment definition
     */
    public void registerFragment(FragmentDefinition fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment cannot be null");
        }
        
        FragmentType type = fragment.getType();
        if (registeredFragments.containsKey(type)) {
            plugin.getLogger().warning("Fragment " + type + " is already registered!");
            return;
        }
        
        registeredFragments.put(type, fragment);
        rankManager.setBaseRank(type, fragment.getBaseRank());
        
        // Register rank passives
        fragment.getRankPassives().forEach((rank, passive) -> 
            rankManager.registerRankPassive(type, rank, passive)
        );
        
        plugin.getLogger().info("Fragment registered: " + fragment.getDisplayName() + 
            " (Base Rank: " + fragment.getBaseRank() + ")");
    }

    /**
     * Get a registered Fragment
     */
    public FragmentDefinition getFragment(FragmentType type) {
        return registeredFragments.get(type);
    }

    /**
     * Get all registered Fragments
     */
    public Collection<FragmentDefinition> getAllFragments() {
        return new ArrayList<>(registeredFragments.values());
    }

    /**
     * Get the RankManager
     */
    public RankManager getRankManager() {
        return rankManager;
    }

    /**
     * Grant a Fragment to a player
     */
    public void grantFragment(Player player, FragmentType type) {
        if (!registeredFragments.containsKey(type)) {
            player.sendMessage("§c✗ Fragment not found");
            return;
        }

        // SECURITY: Prevent non-admins from being granted the Admin fragment
        if (type == FragmentType.ADMIN && !player.hasPermission("fragment.admin") && !player.isOp()) {
            player.sendMessage("§c✗ You do not have permission to use the Admin fragment");
            return;
        }
        
        PlayerFragmentData data = getOrCreatePlayerData(player);
        boolean isFirstFragment = data.getOwnedFragments().isEmpty();
        
        if (data.hasFragment(type)) {
            player.sendMessage("§e⚠ You already have this Fragment");
            return;
        }
        
        data.addFragment(type);
        rankManager.initializeRank(player, type);
        
        FragmentDefinition fragment = getFragment(type);
        
        // FIXED: Only send acquisition message if NOT auto-activating (to avoid duplicate messages)
        if (!isFirstFragment) {
            player.sendMessage(
                com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                com.muzlik.util.Typography.COLOR_SECONDARY + fragment.getDisplayName()
            );
        }
        
        // Auto-activate if this is the first Fragment
        if (isFirstFragment) {
            setActiveFragment(player, type);
        }
    }

    /**
     * Grant and activate a Fragment in one step
     */
    public void grantAndActivateFragment(Player player, FragmentType type) {
        grantFragment(player, type);
        setActiveFragment(player, type);
    }

    /**
     * Check if player has a Fragment (owns it and can use it)
     */
    public boolean hasFragment(Player player, FragmentType type) {
        PlayerFragmentData data = playerFragmentData.get(player.getUniqueId());
        return data != null && data.hasFragment(type);
    }

    /**
     * Charge a Fragment (ritual completed, ready to activate)
     */
    public void chargeFragment(Player player, FragmentType type) {
        if (!registeredFragments.containsKey(type)) {
            player.sendMessage("§c✗ Fragment not found");
            return;
        }
        
        PlayerFragmentData data = getOrCreatePlayerData(player);
        data.chargeFragment(type);
        
        FragmentDefinition fragment = getFragment(type);
        player.sendMessage("§a§l✓ " + fragment.getDisplayName() + " Fragment CHARGED!");
        player.sendMessage("§7Use a §eFragment Changer §7to activate it");
    }

    /**
     * Check if a Fragment is charged for a player
     */
    public boolean isCharged(Player player, FragmentType type) {
        PlayerFragmentData data = playerFragmentData.get(player.getUniqueId());
        return data != null && data.isCharged(type);
    }

    /**
     * Activate a charged Fragment using Fragment Changer
     * This grants the fragment and makes it active
     * 
     * DESIGN NOTE: When switching to a new fragment via ritual activation,
     * the previous fragment is removed from ownership. This is intentional
     * game balance - players must re-craft and re-ritual to use the previous
     * fragment again. This is different from /fragment withdraw which preserves
     * the fragment for later re-use.
     */
    public boolean activateChargedFragment(Player player, FragmentType type) {
        PlayerFragmentData data = getOrCreatePlayerData(player);
        
        // Check if fragment is charged
        if (!data.isCharged(type)) {
            player.sendMessage("§c✗ This Fragment is not charged");
            player.sendMessage("§7Complete a Fragment Creation Ritual first");
            return false;
        }
        
        // If player already has an active fragment that's not this one,
        // they lose the previous fragment when switching (game balance design)
        FragmentType previousActive = data.getActiveFragment();
        if (previousActive != null && previousActive != type) {
            // Remove the previous fragment - they need to re-craft and re-ritual
            data.removeFragment(previousActive);
            player.sendMessage("§6⚠ §e" + previousActive.getDisplayName() + " §6Fragment deactivated");
            player.sendMessage("§7Re-complete the ritual to use it again");
        }
        
        // Grant the new fragment
        data.addFragment(type);
        rankManager.initializeRank(player, type);
        
        // Remove from charged (used up)
        data.unchargeFragment(type);
        
        // Set as active
        setActiveFragment(player, type);
        
        return true;
    }


    /**
     * Get player's active Fragment
     */
    public FragmentType getActiveFragment(Player player) {
        PlayerFragmentData data = playerFragmentData.get(player.getUniqueId());
        return data != null ? data.getActiveFragment() : null;
    }

    /**
     * Set player's active Fragment
     */
    public void setActiveFragment(Player player, FragmentType type) {
        if (type != null && !hasFragment(player, type)) {
            player.sendMessage("§c✗ You don't have this Fragment");
            return;
        }
        
        PlayerFragmentData data = getOrCreatePlayerData(player);
        FragmentType previousFragment = data.getActiveFragment();
        
        // Deactivate previous Fragment passives
        if (previousFragment != null) {
            rankManager.deactivatePassives(player, previousFragment);
            
            // Clean up active abilities from previous fragment
            cleanupFragmentAbilities(player, previousFragment);
        }
        
        // Set new active Fragment
        data.setActiveFragment(type);
        
        // Activate new Fragment passives
        if (type != null) {
            rankManager.activatePassives(player, type);
            
            // Update mana manager with new rank
            int rank = rankManager.getRank(player, type);
            int level = levelManager.getLevel(player, type);
            manaManager.updatePlayerRank(player, rank);
            manaManager.updatePlayerLevel(player, level);
            
            // Silent activation - message shown by caller if needed
        } else {
            // Silent deactivation
        }
    }
    
    /**
     * Clean up active abilities for a specific fragment
     */
    private void cleanupFragmentAbilities(Player player, FragmentType type) {
        // CRITICAL FIX: Clean up VFX effects from previous fragment
        try {
            com.muzlik.FrostSMPPlugin frostPlugin = (com.muzlik.FrostSMPPlugin) plugin;
            if (frostPlugin.getEffectRegistry() != null) {
                frostPlugin.getEffectRegistry().cleanupPlayerEffects(player.getUniqueId());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to cleanup VFX effects for " + player.getName() + ": " + e.getMessage());
        }
        
        // End flight if active for Dragon or Air fragments
        if (type == FragmentType.DRAGON || type == FragmentType.AIR) {
            try {
                com.muzlik.FrostSMPPlugin frostPlugin = (com.muzlik.FrostSMPPlugin) plugin;
                frostPlugin.getFlightManager().endFlight(player, false);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to end flight for " + player.getName() + ": " + e.getMessage());
            }
        }
        
        // Ensure flight is disabled when switching away from flying fragments
        if (!player.getGameMode().equals(org.bukkit.GameMode.CREATIVE) && 
            !player.getGameMode().equals(org.bukkit.GameMode.SPECTATOR)) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    /**
     * Get all Fragments owned by player
     */
    public Collection<FragmentType> getPlayerFragments(Player player) {
        PlayerFragmentData data = playerFragmentData.get(player.getUniqueId());
        return data != null ? data.getOwnedFragments() : Collections.emptySet();
    }

    /**
     * Check if player can switch Fragment (cooldown check)
     */
    public boolean canSwitchFragment(Player player) {
        PlayerFragmentData data = playerFragmentData.get(player.getUniqueId());
        if (data == null) {
            return true;
        }
        
        long lastSwitch = data.getLastFragmentSwitch();
        long cooldown = 3600000; // 1 hour in milliseconds (configurable)
        long timeSinceSwitch = System.currentTimeMillis() - lastSwitch;
        
        return timeSinceSwitch >= cooldown;
    }

    /**
     * Get Fragment switch cooldown remaining
     */
    public long getFragmentSwitchCooldown(Player player) {
        PlayerFragmentData data = playerFragmentData.get(player.getUniqueId());
        if (data == null) {
            return 0;
        }
        
        long lastSwitch = data.getLastFragmentSwitch();
        long cooldown = 3600000; // 1 hour in milliseconds
        long timeSinceSwitch = System.currentTimeMillis() - lastSwitch;
        
        return Math.max(0, cooldown - timeSinceSwitch);
    }

    /**
     * Record Fragment switch time
     */
    public void recordFragmentSwitch(Player player) {
        PlayerFragmentData data = getOrCreatePlayerData(player);
        data.setLastFragmentSwitch(System.currentTimeMillis());
    }

    /**
     * Get or create player Fragment data
     */
    private PlayerFragmentData getOrCreatePlayerData(Player player) {
        return playerFragmentData.computeIfAbsent(player.getUniqueId(), 
            uuid -> new PlayerFragmentData());
    }

    /**
     * Get player Fragment data (may return null)
     */
    public PlayerFragmentData getPlayerData(Player player) {
        return playerFragmentData.get(player.getUniqueId());
    }

    /**
     * Get ability at specific slot for player's Fragment
     */
    public com.muzlik.fragment.ability.IFragmentAbility getAbility(Player player, FragmentType fragmentType, int slot) {
        // Get Fragment definition
        FragmentDefinition fragment = getFragment(fragmentType);
        if (fragment == null) {
            return null;
        }

        // Get player's level and rank
        int playerLevel = levelManager.getLevel(player, fragmentType);
        int playerRank = rankManager.getRank(player, fragmentType);

        // Find ability at this slot
        for (com.muzlik.fragment.ability.AbilityDefinition abilityDef : fragment.getAbilities()) {
            if (abilityDef.getSlot().getSlotIndex() == slot) {
                // Check if player meets requirements
                if (playerLevel >= abilityDef.getLevelRequirement() && 
                    playerRank >= abilityDef.getRankRequirement()) {
                    // Create and return ability instance
                    return new FragmentAbilityWrapper(abilityDef);
                }
            }
        }

        return null;
    }

    /**
     * Wrapper class to adapt AbilityDefinition to IFragmentAbility
     */
    private class FragmentAbilityWrapper implements com.muzlik.fragment.ability.IFragmentAbility {
        private final com.muzlik.fragment.ability.AbilityDefinition definition;

        public FragmentAbilityWrapper(com.muzlik.fragment.ability.AbilityDefinition definition) {
            this.definition = definition;
        }

        @Override
        public String getName() {
            return definition.getId();
        }

        @Override
        public String getDisplayName() {
            return definition.getDisplayName();
        }

        @Override
        public String getDescription() {
            return definition.getDescription();
        }

        @Override
        public double getManaCost() {
            return definition.getManaCost();
        }

        @Override
        public long getCooldown() {
            return definition.getCooldown();
        }

        @Override
        public boolean execute(Player player) {
            com.muzlik.fragment.ability.AbilityExecutor executor = definition.getExecutor();
            if (executor != null) {
                // Get player's rank for this fragment
                FragmentType fragmentType = definition.getFragmentType();
                int playerRank = rankManager.getRank(player, fragmentType);
                int playerLevel = levelManager.getLevel(player, fragmentType);
                
                // Create ability context
                com.muzlik.fragment.ability.AbilityContext context = 
                    new com.muzlik.fragment.ability.AbilityContext.Builder()
                        .player(player)
                        .fragmentType(fragmentType)
                        .playerRank(playerRank)
                        .playerLevel(playerLevel)
                        .location(player.getLocation())
                        .direction(player.getLocation().getDirection())
                        .build();
                
                // Set scaling engine and rank
                context.setRank(playerRank);
                context.setScalingEngine(new com.muzlik.fragment.ability.AbilityScalingEngine());
                
                executor.execute(context);
                return true;
            }
            return false;
        }
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        playerFragmentData.remove(player.getUniqueId());
    }

    /**
     * Create a Fragment item (without giving to player)
     * @return The fragment ItemStack, or null if fragment type not registered
     */
    public org.bukkit.inventory.ItemStack createFragmentItem(FragmentType type) {
        if (!registeredFragments.containsKey(type)) {
            return null;
        }
        return com.muzlik.texture.TextureItemBuilder.createFragmentItem(type);
    }
    
    /**
     * Create and give a physical Fragment item to a player
     */
    public void giveFragmentItem(Player player, FragmentType type) {
        if (!registeredFragments.containsKey(type)) {
            player.sendMessage("§c✗ Fragment not found");
            return;
        }
        
        // Create the fragment item with texture
        org.bukkit.inventory.ItemStack fragmentItem = createFragmentItem(type);
        
        // Try to add to inventory
        java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftover = 
            player.getInventory().addItem(fragmentItem);
        
        if (!leftover.isEmpty()) {
            // Inventory full, drop at player location
            player.getWorld().dropItemNaturally(player.getLocation(), fragmentItem);
            player.sendMessage("§e⚠ Inventory full! Fragment dropped at your feet");
        }
        
        FragmentDefinition fragment = getFragment(type);
        player.sendMessage("§a✓ Received " + fragment.getDisplayName() + " Fragment item!");
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        registeredFragments.clear();
        playerFragmentData.clear();
    }
}
