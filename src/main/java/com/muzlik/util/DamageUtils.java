package com.muzlik.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Bukkit;
import com.muzlik.listener.PlayerPowerListener;
import com.muzlik.FrostSMPPlugin;

/**
 * Utility for properly attributing ability damage to players
 * Ensures kill credit, combat logging, and death messages work correctly
 * 
 * FIXED: Prevents infinite recursion by marking entities during ability damage
 */
public class DamageUtils {
    
    /**
     * Deal damage from a player's ability to a target
     * Properly attributes damage for kill credit and combat logging
     * Prevents infinite recursion by marking the entity during damage
     * 
     * @param damager The player using the ability
     * @param target The entity being damaged
     * @param damage Amount of damage to deal
     */
    public static void dealAbilityDamage(Player damager, LivingEntity target, double damage) {
        // Get the plugin instance to access the listener
        FrostSMPPlugin plugin = (FrostSMPPlugin) Bukkit.getPluginManager().getPlugin("FrostSMP");
        if (plugin == null) {
            // Fallback: just deal damage without recursion protection
            target.damage(damage, damager);
            return;
        }
        
        PlayerPowerListener listener = plugin.getPlayerPowerListener();
        if (listener == null) {
            // Fallback: just deal damage without recursion protection
            target.damage(damage, damager);
            return;
        }
        
        // CRITICAL: Mark entity as being damaged by ability to prevent recursion
        listener.markEntityBeingDamagedByAbility(target.getUniqueId());
        
        try {
            // Deal the damage with proper attribution
            target.damage(damage, damager);
        } finally {
            // ALWAYS unmark, even if damage fails
            // Use a small delay to ensure the event has fully processed
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                listener.unmarkEntityBeingDamagedByAbility(target.getUniqueId());
            }, 1L);
        }
    }
    
    /**
     * Deal damage with a custom cause
     * 
     * @param damager The player using the ability
     * @param target The entity being damaged
     * @param damage Amount of damage to deal
     * @param cause The damage cause (ignored, uses default)
     */
    public static void dealAbilityDamage(Player damager, LivingEntity target, double damage, EntityDamageEvent.DamageCause cause) {
        // Use the main method which has recursion protection
        dealAbilityDamage(damager, target, damage);
    }
}
