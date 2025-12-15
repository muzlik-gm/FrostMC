package com.muzlik.damage;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Ensures all ability damage is properly attributed to players.
 * Handles direct, projectile, indirect, and environmental damage.
 */
public class DamageAPI {
    private final JavaPlugin plugin;
    private final NamespacedKey damageSourceKey;

    public DamageAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.damageSourceKey = new NamespacedKey(plugin, "damage_source");
    }

    /**
     * Deal direct damage to an entity with player attribution
     */
    public void dealDirectDamage(Player source, Entity target, double damage) {
        if (target == null || target.isDead()) {
            return;
        }
        
        // Set damage source metadata
        setDamageSource(target, source);
        
        // Deal damage using Damageable interface
        if (target instanceof org.bukkit.entity.Damageable) {
            ((org.bukkit.entity.Damageable) target).damage(damage, source);
        }
    }

    /**
     * Launch a projectile with player attribution
     */
    public <T extends Projectile> T launchAttributedProjectile(Player source, Class<T> projectileClass, Vector velocity) {
        T projectile = source.launchProjectile(projectileClass, velocity);
        
        // Set shooter (built-in Bukkit attribution)
        projectile.setShooter(source);
        
        // Add custom metadata for extra safety
        setDamageSource(projectile, source);
        
        return projectile;
    }

    /**
     * Set damage source for an entity using metadata
     */
    public void setDamageSource(Entity entity, Player source) {
        if (entity == null || source == null) {
            return;
        }
        
        PersistentDataContainer container = entity.getPersistentDataContainer();
        container.set(damageSourceKey, PersistentDataType.STRING, source.getUniqueId().toString());
    }

    /**
     * Get damage source from entity metadata
     */
    public Player getDamageSource(Entity entity) {
        if (entity == null) {
            return null;
        }
        
        PersistentDataContainer container = entity.getPersistentDataContainer();
        String uuidString = container.get(damageSourceKey, PersistentDataType.STRING);
        
        if (uuidString == null) {
            return null;
        }
        
        try {
            UUID uuid = UUID.fromString(uuidString);
            return plugin.getServer().getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Register damage event with player attribution
     * This should be called from event listeners to ensure proper attribution
     */
    public void registerDamageEvent(EntityDamageEvent event, Player source) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            
            // Set damage source metadata on the damaged entity
            setDamageSource(damageEvent.getEntity(), source);
        }
    }

    /**
     * Get player from damage event (handles various damage sources)
     */
    public Player getPlayerFromDamageEvent(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageEvent.getDamager();
            
            // Direct player damage
            if (damager instanceof Player) {
                return (Player) damager;
            }
            
            // Projectile damage
            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    return (Player) projectile.getShooter();
                }
                
                // Check custom metadata
                return getDamageSource(projectile);
            }
            
            // Check custom metadata on damager
            return getDamageSource(damager);
        }
        
        // Check custom metadata on damaged entity
        return getDamageSource(event.getEntity());
    }

    /**
     * Clear damage source from entity
     */
    public void clearDamageSource(Entity entity) {
        if (entity == null) {
            return;
        }
        
        PersistentDataContainer container = entity.getPersistentDataContainer();
        container.remove(damageSourceKey);
    }

    /**
     * Check if entity has damage source
     */
    public boolean hasDamageSource(Entity entity) {
        if (entity == null) {
            return false;
        }
        
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(damageSourceKey, PersistentDataType.STRING);
    }

    /**
     * Deals "true" damage that bypasses armor, enchantments, and Totems of Undying.
     * This is achieved by directly manipulating the entity's health.
     * The damage is capped to leave the player with at least 1 health (half a heart).
     */
    public void dealTrueDamage(Player source, Entity target, double damage) {
        if (!(target instanceof org.bukkit.entity.Damageable)) {
            return;
        }

        org.bukkit.entity.Damageable damageable = (org.bukkit.entity.Damageable) target;
        double currentHealth = damageable.getHealth();
        double newHealth = Math.max(1.0, currentHealth - damage);

        // Manually set health, bypassing normal damage calculations
        damageable.setHealth(newHealth);

        // Trigger visual damage effect
        damageable.playEffect(org.bukkit.EntityEffect.HURT);

        // Ensure the last damage cause is attributed to the player
        EntityDamageEvent lastDamageEvent = new EntityDamageByEntityEvent(source, target, EntityDamageEvent.DamageCause.CUSTOM, damage);
        target.setLastDamageCause(lastDamageEvent);
    }
}
