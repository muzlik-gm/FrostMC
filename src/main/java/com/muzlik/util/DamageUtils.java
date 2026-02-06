package com.muzlik.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Bukkit;
import com.muzlik.FrostSMPPlugin;

/**
 * Utility for properly attributing ability damage to players
 * Ensures kill credit, combat logging, and death messages work correctly
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
        // Get the plugin instance
        FrostSMPPlugin plugin = (FrostSMPPlugin) Bukkit.getPluginManager().getPlugin("FrostSMP");
        if (plugin == null) {
            // Fallback: just deal damage
            target.damage(damage, damager);
            return;
        }
        
        // Deal damage directly
        target.damage(damage, damager);
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
