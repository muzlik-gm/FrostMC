package com.muzlik.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.UUID;

/**
 * Prevents summoned mobs from attacking their owner
 * CRITICAL FIX: Summoned mobs were attacking the player who summoned them
 */
public class SummonProtectionListener implements Listener {
    
    private static final String SUMMON_OWNER_KEY = "summonOwner";
    
    /**
     * Prevent summoned mobs from targeting their owner
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        // Check if the target is a player
        if (!(event.getTarget() instanceof Player)) {
            return;
        }
        
        Player target = (Player) event.getTarget();
        Entity entity = event.getEntity();
        
        // Check if this entity is a summon
        if (!entity.hasMetadata(SUMMON_OWNER_KEY)) {
            return;
        }
        
        // Get the owner UUID
        String ownerUUIDString = entity.getMetadata(SUMMON_OWNER_KEY).get(0).asString();
        UUID ownerUUID = UUID.fromString(ownerUUIDString);
        
        // If targeting the owner, cancel it
        if (target.getUniqueId().equals(ownerUUID)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Prevent summoned mobs from damaging their owner
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the victim is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Entity damager = event.getDamager();
        
        // Check if the damager is a summon
        if (!damager.hasMetadata(SUMMON_OWNER_KEY)) {
            return;
        }
        
        // Get the owner UUID
        String ownerUUIDString = damager.getMetadata(SUMMON_OWNER_KEY).get(0).asString();
        UUID ownerUUID = UUID.fromString(ownerUUIDString);
        
        // If damaging the owner, cancel it
        if (victim.getUniqueId().equals(ownerUUID)) {
            event.setCancelled(true);
        }
    }
}
