package com.muzlik.vfx;

import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages damage attribution for Fragment abilities.
 * Ensures all damage (direct, indirect, AoE, debuff) is properly attributed
 * to the original caster for kill credit and statistics.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
public class DamageAttributionManager implements Listener {
    private final JavaPlugin plugin;
    
    // Metadata key for fragment owner
    private static final String FRAGMENT_OWNER_KEY = "fragmentOwner";
    
    // Projectile owner tracking: projectileId -> ownerId
    private final Map<UUID, UUID> projectileOwners;
    
    // Damage source tracking: entityId -> DamageSource
    private final Map<UUID, DamageSource> damageSources;
    
    // Cleanup task
    private BukkitRunnable cleanupTask;
    
    /**
     * Constructor
     * 
     * @param plugin The plugin instance
     */
    public DamageAttributionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.projectileOwners = new ConcurrentHashMap<>();
        this.damageSources = new ConcurrentHashMap<>();
    }
    
    /**
     * Start the cleanup task for expired damage sources
     */
    public void startCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        // Run cleanup every 100 ticks (5 seconds)
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredSources();
            }
        };
        cleanupTask.runTaskTimer(plugin, 100L, 100L);
    }
    
    /**
     * Stop the cleanup task
     */
    public void stopCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }
    
    /**
     * Tag a projectile with owner and ability information using PersistentDataContainer
     * 
     * @param projectile The projectile entity
     * @param owner The player who shot the projectile
     * @param abilityId The ability identifier
     */
    public void tagProjectile(Projectile projectile, Player owner, String abilityId) {
        if (projectile == null || owner == null) {
            return;
        }
        
        UUID projectileId = projectile.getUniqueId();
        UUID ownerId = owner.getUniqueId();
        
        // Store in map
        projectileOwners.put(projectileId, ownerId);
        
        // Set metadata on projectile
        projectile.setMetadata(FRAGMENT_OWNER_KEY, 
                new FixedMetadataValue(plugin, ownerId.toString()));
        
        // Set PersistentDataContainer keys
        org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey(plugin, "fragmentOwner");
        org.bukkit.NamespacedKey abilityKey = new org.bukkit.NamespacedKey(plugin, "abilityId");
        
        projectile.getPersistentDataContainer().set(ownerKey, 
            org.bukkit.persistence.PersistentDataType.STRING, ownerId.toString());
        projectile.getPersistentDataContainer().set(abilityKey, 
            org.bukkit.persistence.PersistentDataType.STRING, abilityId);
        
        // Set shooter
        projectile.setShooter(owner);
        
        plugin.getLogger().log(Level.FINE, 
                "Tagged projectile " + projectileId + " for owner " + owner.getName() + 
                " with ability " + abilityId);
    }
    
    /**
     * Register a projectile with its owner (legacy method)
     * 
     * @param projectile The projectile entity
     * @param owner The player who shot the projectile
     */
    public void registerProjectile(Projectile projectile, Player owner) {
        tagProjectile(projectile, owner, "unknown");
    }
    
    /**
     * Register a damage source (for indirect/AoE damage)
     * 
     * @param entity The entity that will deal damage
     * @param owner The player who owns this damage source
     * @param abilityId The ID of the ability
     * @param fragmentType The Fragment type
     * @param durationMillis How long this damage source remains valid
     */
    public void registerDamageSource(Entity entity, Player owner, String abilityId, 
                                     FragmentType fragmentType, long durationMillis) {
        if (entity == null || owner == null) {
            return;
        }
        
        UUID entityId = entity.getUniqueId();
        DamageSource source = new DamageSource(owner.getUniqueId(), abilityId, 
                fragmentType, durationMillis);
        
        // Store in map
        damageSources.put(entityId, source);
        
        // Set metadata on entity
        entity.setMetadata(FRAGMENT_OWNER_KEY, 
                new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        
        plugin.getLogger().log(Level.FINE, 
                "Registered damage source " + entityId + " for owner " + owner.getName());
    }
    
    /**
     * Register a damage source with default duration (10 seconds)
     */
    public void registerDamageSource(Entity entity, Player owner, String abilityId, 
                                     FragmentType fragmentType) {
        registerDamageSource(entity, owner, abilityId, fragmentType, 10000L);
    }
    
    /**
     * Create an invisible marker entity as a damage source for indirect damage
     * 
     * @param owner The player who owns this damage source
     * @param abilityId The ability identifier
     * @param fragmentType The fragment type
     * @param location Location to spawn the marker
     * @param durationMillis How long this damage source remains valid
     * @return UUID of the created damage source entity
     */
    public java.util.UUID createDamageSource(Player owner, String abilityId, 
                                            FragmentType fragmentType, 
                                            org.bukkit.Location location, 
                                            long durationMillis) {
        if (owner == null || location == null || location.getWorld() == null) {
            return null;
        }
        
        // Spawn invisible marker entity
        org.bukkit.entity.ArmorStand marker = (org.bukkit.entity.ArmorStand) location.getWorld().spawnEntity(
            location, 
            org.bukkit.entity.EntityType.ARMOR_STAND
        );
        
        // Configure as invisible marker
        marker.setVisible(false);
        marker.setGravity(false);
        marker.setMarker(true);
        marker.setInvulnerable(true);
        marker.setCollidable(false);
        marker.setCustomNameVisible(false);
        
        // Register as damage source
        registerDamageSource(marker, owner, abilityId, fragmentType, durationMillis);
        
        // Tag with PersistentDataContainer
        org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey(plugin, "fragmentOwner");
        org.bukkit.NamespacedKey abilityKey = new org.bukkit.NamespacedKey(plugin, "abilityId");
        
        marker.getPersistentDataContainer().set(ownerKey, 
            org.bukkit.persistence.PersistentDataType.STRING, owner.getUniqueId().toString());
        marker.getPersistentDataContainer().set(abilityKey, 
            org.bukkit.persistence.PersistentDataType.STRING, abilityId);
        
        plugin.getLogger().log(Level.FINE, 
                "Created damage source marker for " + owner.getName() + " with ability " + abilityId);
        
        return marker.getUniqueId();
    }
    
    /**
     * Deal indirect damage using a damage source marker
     * 
     * @param damageSourceId UUID of the damage source marker entity
     * @param target The entity to damage
     * @param damage Amount of damage
     */
    public void dealIndirectDamage(java.util.UUID damageSourceId, org.bukkit.entity.Entity target, double damage) {
        if (damageSourceId == null || target == null) {
            return;
        }
        
        // Find the damage source marker
        org.bukkit.entity.Entity marker = null;
        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(damageSourceId)) {
                    marker = entity;
                    break;
                }
            }
            if (marker != null) break;
        }
        
        if (marker == null) {
            plugin.getLogger().warning("Damage source marker not found: " + damageSourceId);
            return;
        }
        
        // Get the owner
        Player owner = getOwner(marker);
        
        if (owner != null && target instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) target;
            living.damage(damage, owner);
            
            plugin.getLogger().log(Level.FINE, 
                    "Indirect damage: " + owner.getName() + " -> " + target.getType() + 
                    " (" + damage + " damage)");
        }
    }
    
    /**
     * Tag a summon entity with owner information
     * 
     * @param summon The summoned entity
     * @param owner The player who summoned it
     * @param abilityId The ability identifier
     */
    public void tagSummon(org.bukkit.entity.LivingEntity summon, Player owner, String abilityId) {
        if (summon == null || owner == null) {
            return;
        }
        
        // Set metadata
        summon.setMetadata(FRAGMENT_OWNER_KEY, 
                new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        
        // Set PersistentDataContainer keys
        org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey(plugin, "fragmentOwner");
        org.bukkit.NamespacedKey abilityKey = new org.bukkit.NamespacedKey(plugin, "abilityId");
        org.bukkit.NamespacedKey summonKey = new org.bukkit.NamespacedKey(plugin, "isSummon");
        
        summon.getPersistentDataContainer().set(ownerKey, 
            org.bukkit.persistence.PersistentDataType.STRING, owner.getUniqueId().toString());
        summon.getPersistentDataContainer().set(abilityKey, 
            org.bukkit.persistence.PersistentDataType.STRING, abilityId);
        summon.getPersistentDataContainer().set(summonKey, 
            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        
        plugin.getLogger().log(Level.FINE, 
                "Tagged summon " + summon.getType() + " for owner " + owner.getName());
    }
    
    /**
     * Get the owner of a damager entity
     * Checks projectiles, damage sources, and metadata
     * 
     * @param damager The entity that dealt damage
     * @return The player who owns this damager, or null if not found
     */
    public Player getOwner(Entity damager) {
        if (damager == null) {
            return null;
        }
        
        // If damager is a player, return directly
        if (damager instanceof Player) {
            return (Player) damager;
        }
        
        UUID damagerId = damager.getUniqueId();
        
        // Check projectile owners
        if (damager instanceof Projectile) {
            UUID ownerId = projectileOwners.get(damagerId);
            if (ownerId != null) {
                return plugin.getServer().getPlayer(ownerId);
            }
            
            // Check shooter
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        
        // Check damage sources
        DamageSource source = damageSources.get(damagerId);
        if (source != null && !source.isExpired()) {
            return source.getOwner();
        }
        
        // Check metadata
        if (damager.hasMetadata(FRAGMENT_OWNER_KEY)) {
            // Task 3.2: Fix metadata null safety - check size and null before accessing
            java.util.List<org.bukkit.metadata.MetadataValue> metadata = damager.getMetadata(FRAGMENT_OWNER_KEY);
            if (!metadata.isEmpty() && metadata.get(0) != null) {
                try {
                    String ownerIdStr = metadata.get(0).asString();
                    if (ownerIdStr != null) {
                        UUID ownerId = UUID.fromString(ownerIdStr);
                        return plugin.getServer().getPlayer(ownerId);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid owner UUID in metadata: " + e.getMessage());
                } catch (Exception e) {
                    plugin.getLogger().warning("Error reading metadata: " + e.getMessage());
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the damage source for an entity
     * 
     * @param entity The entity
     * @return The damage source, or null if not found
     */
    public DamageSource getDamageSource(Entity entity) {
        if (entity == null) {
            return null;
        }
        
        return damageSources.get(entity.getUniqueId());
    }
    
    /**
     * Handle damage events and ensure proper attribution
     * This is called automatically by the event system
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleDamageEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        
        // Get the owner of the damager
        Player owner = getOwner(damager);
        
        if (owner != null) {
            // Log for debugging
            plugin.getLogger().log(Level.FINE, 
                    "Damage attributed: " + owner.getName() + " -> " + 
                    victim.getType() + " (" + event.getFinalDamage() + " damage)");
            
            // The damage is already properly attributed through the event system
            // Additional logic for kill credit, statistics, etc. can be added here
        }
    }
    
    /**
     * Clean up expired damage sources
     * 
     * @return Number of sources cleaned up
     */
    private int cleanupExpiredSources() {
        final int[] cleaned = {0};
        
        // Clean up expired damage sources
        damageSources.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                cleaned[0]++;
                return true;
            }
            return false;
        });
        
        if (cleaned[0] > 0) {
            plugin.getLogger().log(Level.FINE, 
                    "Cleaned up " + cleaned[0] + " expired damage sources");
        }
        
        return cleaned[0];
    }
    
    /**
     * Remove a projectile from tracking
     * 
     * @param projectileId The UUID of the projectile
     */
    public void removeProjectile(UUID projectileId) {
        projectileOwners.remove(projectileId);
    }
    
    /**
     * Remove a damage source from tracking
     * 
     * @param entityId The UUID of the entity
     */
    public void removeDamageSource(UUID entityId) {
        damageSources.remove(entityId);
    }
    
    /**
     * Get statistics about the damage attribution system
     * 
     * @return Map of statistic name to value
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("tracked_projectiles", projectileOwners.size());
        stats.put("tracked_damage_sources", damageSources.size());
        return stats;
    }
    
    /**
     * Clear all tracked data
     */
    public void clearAll() {
        projectileOwners.clear();
        damageSources.clear();
    }
    
    /**
     * Shutdown the manager
     */
    public void shutdown() {
        stopCleanupTask();
        clearAll();
    }
}
