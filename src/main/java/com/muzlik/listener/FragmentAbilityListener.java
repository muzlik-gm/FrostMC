package com.muzlik.listener;

import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.IFragmentAbility;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.mana.ManaManager;
import com.muzlik.player.ControlScheme;
import com.muzlik.player.PlayerPreferencesManager;
import com.muzlik.FrostSMPPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for Fragment ability interactions
 * Supports multiple control schemes per player
 * 
 * Fragment Level affects:
 * - Mana cost reduction (up to 30% at max level)
 * - Cooldown reduction (up to 30% at max level)
 * - Damage bonus (up to 30% at max level)
 */
public class FragmentAbilityListener implements Listener {

    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final ManaManager manaManager;
    private final CooldownManager cooldownManager;
    private PlayerPreferencesManager preferencesManager;
    private LevelManager levelManager;
    
    // Track which players are currently sneaking
    private final Set<String> sneakingPlayers = ConcurrentHashMap.newKeySet();
    
    // Track double sneak for DOUBLE_SNEAK control scheme
    private final Map<String, Long> lastSneakTime = new ConcurrentHashMap<>();
    private final Set<String> doubleSneakActive = ConcurrentHashMap.newKeySet();
    private static final long DOUBLE_SNEAK_WINDOW = 500; // 0.5 seconds to double tap
    private static final long DOUBLE_SNEAK_DURATION = 3000; // 3 seconds active
    
    // Track swap hands for SWAP_HANDS control scheme
    private final Set<String> swapHandsActive = ConcurrentHashMap.newKeySet();
    private static final long SWAP_HANDS_DURATION = 2000; // 2 seconds active
    
    // Track last ability use time per player to prevent spam (player UUID -> timestamp)
    private final Map<String, Long> lastAbilityUse = new ConcurrentHashMap<>();
    
    // Minimum delay between ability activations (milliseconds)
    private static final long ABILITY_SPAM_DELAY = 100; // 0.1 seconds (reduced for responsiveness)

    public FragmentAbilityListener(JavaPlugin plugin, FragmentManager fragmentManager, ManaManager manaManager, 
                                   CooldownManager cooldownManager, PlayerPreferencesManager preferencesManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.cooldownManager = cooldownManager;
        this.preferencesManager = preferencesManager;
    }
    
    /**
     * Set LevelManager reference (called from FrostSMPPlugin)
     */
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }
    
    /**
     * Set PreferencesManager reference (called from FrostSMPPlugin)
     */
    public void setPreferencesManager(PlayerPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    /**
     * Handle player sneak toggle
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        
        if (preferencesManager == null) {
            return;
        }
        
        ControlScheme scheme = preferencesManager.getControlScheme(player);

        if (event.isSneaking()) {
            sneakingPlayers.add(playerUUID);
            
            // Handle DOUBLE_SNEAK control scheme
            if (scheme == ControlScheme.DOUBLE_SNEAK) {
                long currentTime = System.currentTimeMillis();
                Long lastSneak = lastSneakTime.get(playerUUID);
                
                if (lastSneak != null && (currentTime - lastSneak) < DOUBLE_SNEAK_WINDOW) {
                    // Double sneak detected!
                    doubleSneakActive.add(playerUUID);
                    player.sendActionBar("§a✓ §eAbility Mode Active §7(3s)");
                    
                    // Deactivate after duration
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            doubleSneakActive.remove(playerUUID);
                        }
                    }.runTaskLater(plugin, DOUBLE_SNEAK_DURATION / 50);
                }
                
                lastSneakTime.put(playerUUID, currentTime);
            }
        } else {
            sneakingPlayers.remove(playerUUID);
        }
    }
    
    /**
     * Handle swap hands event (F key)
     */
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        
        if (preferencesManager == null) {
            return;
        }
        
        ControlScheme scheme = preferencesManager.getControlScheme(player);
        
        // Handle SWAP_HANDS control scheme
        if (scheme == ControlScheme.SWAP_HANDS) {
            // Check if player has active Fragment
            if (fragmentManager.getActiveFragment(player) != null) {
                event.setCancelled(true); // Prevent actual item swap
                swapHandsActive.add(playerUUID);
                player.sendActionBar("§a✓ §eAbility Mode Active §7(2s)");
                
                // Deactivate after duration
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        swapHandsActive.remove(playerUUID);
                    }
                }.runTaskLater(plugin, SWAP_HANDS_DURATION / 50);
            }
        }
    }

    /**
     * Handle player interact event (clicks)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        Action action = event.getAction();
        
        if (preferencesManager == null) {
            return;
        }
        
        // Check if abilities are enabled for this player
        if (!preferencesManager.areAbilitiesEnabled(player)) {
            return; // Abilities disabled, do nothing
        }
        
        // Get player's control scheme
        ControlScheme scheme = preferencesManager.getControlScheme(player);
        
        // Check if player meets control scheme requirements
        boolean canUseAbility = false;
        
        switch (scheme) {
            case SNEAK_CLICK:
                // Must be sneaking
                canUseAbility = sneakingPlayers.contains(playerUUID);
                break;
                
            case DOUBLE_SNEAK:
                // Must have double-sneaked recently
                canUseAbility = doubleSneakActive.contains(playerUUID);
                break;
                
            case SWAP_HANDS:
                // Must have pressed F recently
                canUseAbility = swapHandsActive.contains(playerUUID);
                break;
                
            case CLICK_ONLY:
                // Always can use (no requirements)
                canUseAbility = true;
                break;
        }
        
        if (!canUseAbility) {
            // Player doesn't meet control scheme requirements - silently ignore
            return;
        }

        // Get player's active Fragment
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        if (activeFragment == null) {
            return;
        }

        // Determine which ability slot based on hotbar position
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        if (hotbarSlot > 4) {
            // Only slots 0-4 are valid
            return;
        }

        // Get the ability at this slot
        IFragmentAbility ability = fragmentManager.getAbility(player, activeFragment, hotbarSlot);
        
        if (ability == null) {
            return;
        }

        // Determine action type
        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
        boolean isLeftClick = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK);

        if (!isRightClick && !isLeftClick) {
            return;
        }

        // Cancel default interaction to prevent block/entity interaction
        event.setCancelled(true);

        // Execute ability based on action
        executeAbility(player, activeFragment, hotbarSlot, ability, isRightClick, isLeftClick);
    }
    
    /**
     * Handle player animation (arm swing) for Left Click detection
     * Uses PlayerAnimationEvent instead of EntityDamageEvent for proper left-click detection
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAnimation(org.bukkit.event.player.PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        
        // Only handle arm swing animation
        if (event.getAnimationType() != org.bukkit.event.player.PlayerAnimationType.ARM_SWING) {
            return;
        }
        
        if (preferencesManager == null) {
            return;
        }
        
        // Check if abilities are enabled for this player
        if (!preferencesManager.areAbilitiesEnabled(player)) {
            return;
        }
        
        // Get player's control scheme
        ControlScheme scheme = preferencesManager.getControlScheme(player);
        
        // Check if player meets control scheme requirements (same as onPlayerInteract)
        boolean canUseAbility = false;
        
        switch (scheme) {
            case SNEAK_CLICK:
                canUseAbility = sneakingPlayers.contains(playerUUID);
                break;
            case DOUBLE_SNEAK:
                canUseAbility = doubleSneakActive.contains(playerUUID);
                break;
            case SWAP_HANDS:
                canUseAbility = swapHandsActive.contains(playerUUID);
                break;
            case CLICK_ONLY:
                canUseAbility = true;
                break;
        }
        
        if (!canUseAbility) {
            // Player doesn't meet control scheme requirements - silently ignore
            return;
        }
        
        // Get player's active Fragment
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        if (activeFragment == null) {
            return;
        }
        
        // Determine which ability slot based on hotbar position
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        if (hotbarSlot > 4) {
            return;
        }
        
        // Get the ability at this slot
        IFragmentAbility ability = fragmentManager.getAbility(player, activeFragment, hotbarSlot);
        
        if (ability == null) {
            return;
        }
        
        // Execute ability (left click / alternate mode)
        executeAbility(player, activeFragment, hotbarSlot, ability, false, true);
    }

    /**
     * Execute a Fragment ability
     * Applies Fragment Level bonuses to mana cost and cooldown
     */
    private void executeAbility(Player player, FragmentType fragmentType, int slotIndex, 
                               IFragmentAbility ability, 
                               boolean isRightClick, boolean isLeftClick) {
        String playerUUID = player.getUniqueId().toString();
        String abilityId = fragmentType.name() + "_" + ability.getName();
        
        // ANTI-SPAM: Check if player is trying to spam abilities
        long currentTime = System.currentTimeMillis();
        Long lastUse = lastAbilityUse.get(playerUUID);
        if (lastUse != null && (currentTime - lastUse) < ABILITY_SPAM_DELAY) {
            // Silently ignore spam attempts (no message to avoid chat spam)
            return;
        }

        // Check cooldown - show in action bar only
        if (cooldownManager.isOnCooldown(player, abilityId)) {
            double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
            // Show in action bar to avoid chat spam
            if (lastUse == null || (currentTime - lastUse) >= 1000) {
                player.sendActionBar(com.muzlik.util.Typography.COLOR_ERROR + 
                    com.muzlik.util.Typography.SYMBOL_COOLDOWN + " " + 
                    String.format("%.1fs", remaining));
            }
            return;
        }

        // ═══ APPLY LEVEL-BASED MANA COST REDUCTION ═══
        // Skip mana checks if mana system is disabled
        if (manaManager.isManaSystemEnabled()) {
            double baseManaCost = ability.getManaCost();
            double manaCostReduction = 0.0;
            
            if (levelManager != null) {
                manaCostReduction = levelManager.getManaCostReduction(player, fragmentType);
            }
            
            double finalManaCost = baseManaCost * (1 - manaCostReduction);
            double currentMana = manaManager.getMana(player);
            
            if (currentMana < finalManaCost) {
                // Show in action bar to avoid chat spam
                player.sendActionBar(com.muzlik.util.Typography.COLOR_ERROR + 
                    com.muzlik.util.Typography.SYMBOL_CROSS + " " + 
                    com.muzlik.util.Typography.toSmallCaps("not enough mana") + " " +
                    com.muzlik.util.Typography.COLOR_PRIMARY + 
                    String.format("%.0f", currentMana) + "/" + String.format("%.0f", finalManaCost));
                return;
            }
        }

        // Determine activation mode
        com.muzlik.fragment.ability.ActivationMode mode = isRightClick ? 
            com.muzlik.fragment.ability.ActivationMode.PRIMARY : 
            com.muzlik.fragment.ability.ActivationMode.ALTERNATE;
        
        String interactionType = isRightClick ? "Right-Click" : "Left-Click";

        // Execute the ability
        boolean success = ability.execute(player);

        if (success) {
            // Record ability use time (anti-spam)
            lastAbilityUse.put(playerUUID, currentTime);
            
            // Consume mana (with level reduction applied) - only if mana system is enabled
            if (manaManager.isManaSystemEnabled()) {
                double baseManaCost = ability.getManaCost();
                double manaCostReduction = 0.0;
                
                if (levelManager != null) {
                    manaCostReduction = levelManager.getManaCostReduction(player, fragmentType);
                }
                
                double finalManaCost = baseManaCost * (1 - manaCostReduction);
                manaManager.consumeMana(player, finalManaCost);
                
                // Show clean action bar with mana info
                double remainingMana = manaManager.getMana(player);
                double maxMana = manaManager.getMaxMana(player);
                player.sendActionBar(
                    com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                    com.muzlik.util.Typography.COLOR_PRIMARY + 
                    String.format("%.0f", remainingMana) + "/" + String.format("%.0f", maxMana) + " " +
                    com.muzlik.util.Typography.COLOR_TEXT_DARK + 
                    com.muzlik.util.Typography.toSmallCaps("mana")
                );
            } else {
                // Show success message without mana info
                player.sendActionBar(
                    com.muzlik.util.Typography.COLOR_SUCCESS + com.muzlik.util.Typography.SYMBOL_CHECK + " " +
                    com.muzlik.util.Typography.toSmallCaps("ability activated")
                );
            }
            
            // ═══ APPLY LEVEL-BASED COOLDOWN REDUCTION ═══
            long baseCooldown = ability.getCooldown();
            double cooldownReduction = 0.0;
            
            if (levelManager != null) {
                cooldownReduction = levelManager.getCooldownReduction(player, fragmentType);
            }
            
            long finalCooldown = (long) (baseCooldown * (1 - cooldownReduction));
            
            // Start cooldown (with level reduction applied)
            cooldownManager.startCooldown(player, abilityId, finalCooldown);
        }
    }
}

