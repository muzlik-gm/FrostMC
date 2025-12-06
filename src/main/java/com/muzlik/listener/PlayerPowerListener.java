package com.muzlik.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import com.muzlik.power.PowerManager;
import com.muzlik.power.IPower;
import com.muzlik.power.ability.AbilityManager;
import com.muzlik.cooldown.CooldownManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Listener for player power interactions
 * Handles Sneak + Right-Click and Sneak + Left-Click for ability activation
 */
public class PlayerPowerListener implements Listener {

    private final PowerManager powerManager;
    
    // Track which players are currently sneaking
    private final Set<String> sneakingPlayers = ConcurrentHashMap.newKeySet();
    
    // Track entities currently being damaged by abilities to prevent infinite recursion
    private final Set<UUID> entitiesBeingDamagedByAbilities = ConcurrentHashMap.newKeySet();

    public PlayerPowerListener(PowerManager powerManager) {
        this.powerManager = powerManager;
    }

    /**
     * Handle player sneak toggle
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        if (event.isSneaking()) {
            // Started sneaking
            sneakingPlayers.add(playerUUID);
        } else {
            // Stopped sneaking
            sneakingPlayers.remove(playerUUID);
        }
    }

    /**
     * Handle player interact event (clicks)
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        Action action = event.getAction();

        // Check if player is sneaking
        if (!sneakingPlayers.contains(playerUUID)) {
            return;
        }

        // Get player's active power
        IPower activePower = powerManager.getPlayerActivePower(player);
        if (activePower == null) {
            return;
        }

        // Determine which ability slot based on hotbar position
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        if (hotbarSlot > 2) {
            // Only slots 0-2 are valid
            return;
        }

        // Get the ability at this slot
        AbilityManager abilityManager = activePower.getAbilityManager();
        com.muzlik.power.IAbility ability = abilityManager.getAbility(hotbarSlot);
        
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
        executeAbility(player, activePower, hotbarSlot, ability, isRightClick, isLeftClick);
    }
    
    /**
     * Handle entity damage for Shift + Left Click on entities
     * CRITICAL: Must filter out ability damage to prevent infinite recursion
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        // CRITICAL FIX: Check if this entity is currently being damaged by an ability
        // This prevents infinite recursion when abilities deal damage
        UUID victimUUID = event.getEntity().getUniqueId();
        if (entitiesBeingDamagedByAbilities.contains(victimUUID)) {
            // This is ability damage, don't trigger another ability
            return;
        }
        
        Player player = (Player) event.getDamager();
        String playerUUID = player.getUniqueId().toString();
        
        // Check if player is sneaking
        if (!sneakingPlayers.contains(playerUUID)) {
            return;
        }
        
        // Get player's active power
        IPower activePower = powerManager.getPlayerActivePower(player);
        if (activePower == null) {
            return;
        }
        
        // Determine which ability slot based on hotbar position
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        if (hotbarSlot > 2) {
            return;
        }
        
        // Get the ability at this slot
        AbilityManager abilityManager = activePower.getAbilityManager();
        com.muzlik.power.IAbility ability = abilityManager.getAbility(hotbarSlot);
        
        if (ability == null) {
            return;
        }
        
        // Cancel the normal damage
        event.setCancelled(true);
        
        // Execute ability (left click on entity)
        executeAbility(player, activePower, hotbarSlot, ability, false, true);
    }
    
    /**
     * Mark an entity as being damaged by an ability (prevents recursion)
     */
    public void markEntityBeingDamagedByAbility(UUID entityUUID) {
        entitiesBeingDamagedByAbilities.add(entityUUID);
    }
    
    /**
     * Unmark an entity after ability damage is complete
     */
    public void unmarkEntityBeingDamagedByAbility(UUID entityUUID) {
        entitiesBeingDamagedByAbilities.remove(entityUUID);
    }

    /**
     * Execute an ability
     */
    private void executeAbility(Player player, IPower power, int slotIndex, 
                               com.muzlik.power.IAbility ability, 
                               boolean isRightClick, boolean isLeftClick) {
        CooldownManager cooldownManager = powerManager.getCooldownManager();
        String abilityId = ability.getId();

        // Check cooldown
        if (cooldownManager.isOnCooldown(player, abilityId)) {
            double remaining = cooldownManager.getRemainingCooldownSeconds(player, abilityId);
            player.sendMessage("§c✗ Ability on cooldown: §b" + 
                             String.format("%.1f", remaining) + "s");
            return;
        }

        // Determine interaction type for logging
        String interactionType = isRightClick ? "Right-Click" : "Left-Click";

        // Execute the ability
        boolean success = ability.execute(player);

        if (success) {
            player.sendMessage("§a✓ §b" + ability.getDisplayName() + 
                             "§a used (§b" + interactionType + "§a)");
            
            // Start cooldown
            cooldownManager.startCooldown(player, abilityId, ability.getCooldown());
        } else {
            player.sendMessage("§c✗ Ability failed to execute");
        }
    }

    /**
     * Get the sneaking players set (for testing/debugging)
     */
    protected Set<String> getSneakingPlayers() {
        return new HashSet<>(sneakingPlayers);
    }
}


