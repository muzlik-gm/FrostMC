package com.muzlik.listener;

import com.muzlik.cooldown.CooldownManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.IFragmentAbility;
import com.muzlik.mana.ManaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for Fragment ability interactions
 * Handles Sneak + Right-Click and Sneak + Left-Click for ability activation
 */
public class FragmentAbilityListener implements Listener {

    private final FragmentManager fragmentManager;
    private final ManaManager manaManager;
    private final CooldownManager cooldownManager;
    
    // Track which players are currently sneaking
    private final Set<String> sneakingPlayers = ConcurrentHashMap.newKeySet();
    
    // Track last ability use time per player to prevent spam (player UUID -> timestamp)
    private final Map<String, Long> lastAbilityUse = new ConcurrentHashMap<>();
    
    // Minimum delay between ability activations (milliseconds)
    private static final long ABILITY_SPAM_DELAY = 500; // 0.5 seconds

    public FragmentAbilityListener(FragmentManager fragmentManager, ManaManager manaManager, CooldownManager cooldownManager) {
        this.fragmentManager = fragmentManager;
        this.manaManager = manaManager;
        this.cooldownManager = cooldownManager;
    }

    /**
     * Handle player sneak toggle
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        if (event.isSneaking()) {
            sneakingPlayers.add(playerUUID);
        } else {
            sneakingPlayers.remove(playerUUID);
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

        // Check if player is sneaking
        if (!sneakingPlayers.contains(playerUUID)) {
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
     * Handle player animation (arm swing) for Shift + Left Click detection
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
        
        // Check if player is sneaking
        if (!sneakingPlayers.contains(playerUUID)) {
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

        // Check cooldown
        if (cooldownManager.isOnCooldown(player, abilityId)) {
            double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
            // Only show cooldown message if not spamming
            if (lastUse == null || (currentTime - lastUse) >= 1000) {
                player.sendMessage("§c✗ Ability on cooldown: §b" + 
                                 String.format("%.1f", remaining) + "s");
            }
            return;
        }

        // Check mana cost
        double manaCost = ability.getManaCost();
        double currentMana = manaManager.getMana(player);
        
        if (currentMana < manaCost) {
            player.sendMessage("§c✗ Not enough mana: §9" + 
                             String.format("%.0f", currentMana) + "/" + 
                             String.format("%.0f", manaCost));
            return;
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
            
            // Consume mana
            manaManager.consumeMana(player, manaCost);
            
            // Show success message
            player.sendMessage("§a✓ §b" + ability.getDisplayName() + 
                             "§a used (§b" + interactionType + "§a) §7[-" + 
                             String.format("%.0f", manaCost) + " mana]");
            
            // Start cooldown
            cooldownManager.startCooldown(player, abilityId, ability.getCooldown());
            
            // Show action bar with remaining mana
            double remainingMana = manaManager.getMana(player);
            double maxMana = manaManager.getMaxMana(player);
            player.sendActionBar("§9Mana: " + String.format("%.0f", remainingMana) + "/" + 
                               String.format("%.0f", maxMana));
        } else {
            player.sendMessage("§c✗ Ability failed to execute");
        }
    }
}
