package com.muzlik.block;

import com.muzlik.vfx.ActiveEffectRegistry;
import com.muzlik.vfx.EffectEntry;
import com.muzlik.vfx.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Block Manipulation Engine - manages block projectiles using armor stand controllers.
 * 
 * Per SYSTEM.md specification:
 * - Uses invisible armor stands as controllers
 * - Supports particle shells (preferred) and falling block shells
 * - Implements physics simulation with velocity curves
 * - Handles collision detection and impact logic
 * - Integrates with ActiveEffectRegistry for tracking and cleanup
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7
 */
public class BlockManipulationEngine {
    private final JavaPlugin plugin;
    private final ActiveEffectRegistry registry;
    private final Map<UUID, BlockController> activeControllers;
    private final Map<UUID, FallingBlockTransaction> activeTransactions;
    private final ParticleShellRenderer particleRenderer;
    private final ImpactHandler defaultImpactHandler;
    private BukkitTask updateTask;
    private BukkitTask transactionCleanupTask;
    
    // Configuration
    private boolean allowRealFallingBlocks;
    
    // Debug
    private boolean updateTaskLoggedOnce = false;
    
    /**
     * Constructor
     * 
     * @param plugin The plugin instance
     * @param registry The ActiveEffectRegistry for tracking
     */
    public BlockManipulationEngine(JavaPlugin plugin, ActiveEffectRegistry registry) {
        this(plugin, registry, null);
    }
    
    /**
     * Constructor with damage attribution
     * 
     * @param plugin The plugin instance
     * @param registry The ActiveEffectRegistry for tracking
     * @param damageAttribution The damage attribution manager (can be null)
     */
    public BlockManipulationEngine(JavaPlugin plugin, ActiveEffectRegistry registry, 
                                  com.muzlik.vfx.DamageAttributionManager damageAttribution) {
        this.plugin = plugin;
        this.registry = registry;
        this.activeControllers = new ConcurrentHashMap<>();
        this.activeTransactions = new ConcurrentHashMap<>();
        this.particleRenderer = new ParticleShellRenderer(plugin);
        this.defaultImpactHandler = new DefaultImpactHandler(plugin, damageAttribution, particleRenderer);
        
        // Load configuration
        loadConfiguration();
    }
    
    /**
     * Load configuration from config.yml
     */
    private void loadConfiguration() {
        // Default: allow real falling blocks but prefer particles
        this.allowRealFallingBlocks = plugin.getConfig().getBoolean("block_manipulation.allow_real_blocks", true);
        
        plugin.getLogger().info("BlockManipulationEngine config: allowRealFallingBlocks=" + allowRealFallingBlocks);
    }
    
    /**
     * Reload configuration
     */
    public void reloadConfiguration() {
        loadConfiguration();
    }
    
    /**
     * Register the cleanup listener for player logout events
     */
    public void registerCleanupListener() {
        BlockControllerCleanupListener listener = new BlockControllerCleanupListener(plugin, this);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getLogger().info("BlockManipulationEngine cleanup listener registered");
    }
    
    /**
     * Start the update task that runs every tick
     */
    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Run every tick (1 tick = 50ms)
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 1L, 1L);
        
        // Start transaction cleanup task (every 5 seconds = 100 ticks)
        if (transactionCleanupTask != null) {
            transactionCleanupTask.cancel();
        }
        transactionCleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredTransactions, 100L, 100L);
    }
    
    /**
     * Stop the update task
     */
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        if (transactionCleanupTask != null) {
            transactionCleanupTask.cancel();
            transactionCleanupTask = null;
        }
    }
    
    /**
     * Spawn a new block controller
     * 
     * @param config Configuration for the controller
     * @return UUID of the spawned controller, or null if spawn failed
     */
    public UUID spawnController(BlockControllerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("BlockControllerConfig cannot be null");
        }
        
        Location startLoc = config.getStartLocation();
        if (startLoc == null || startLoc.getWorld() == null) {
            plugin.getLogger().warning("Cannot spawn controller: invalid location");
            return null;
        }
        
        try {
            // Spawn invisible armor stand as controller
            ArmorStand armorStand = (ArmorStand) startLoc.getWorld().spawnEntity(
                startLoc,
                EntityType.ARMOR_STAND
            );
            
            // Configure armor stand - make it completely invisible
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCollidable(false);
            armorStand.setCustomNameVisible(false);
            armorStand.setSmall(true);
            armorStand.setBasePlate(false);
            armorStand.setArms(false);
            
            // Ensure config has particle renderer and impact handler
            BlockControllerConfig finalConfig = config;
            boolean needsUpdate = (config.getShellType() == ShellType.PARTICLE_SHELL && config.getParticleRenderer() == null) 
                               || (config.getImpactHandler() == null);
            
            if (needsUpdate) {
                // Create new config with particle renderer and impact handler
                finalConfig = new BlockControllerConfig.Builder()
                    .ownerUUID(config.getOwnerUUID())
                    .abilityId(config.getAbilityId())
                    .startLocation(config.getStartLocation())
                    .direction(config.getDirection())
                    .baseSpeed(config.getBaseSpeed())
                    .accelerationFactor(config.getAccelerationFactor())
                    .maxSpeed(config.getMaxSpeed())
                    .maxLifeTicks(config.getMaxLifeTicks())
                    .shellType(config.getShellType())
                    .blockType(config.getBlockType())
                    .collisionRadius(config.getCollisionRadius())
                    .baseDamage(config.getBaseDamage())
                    .fragmentRank(config.getFragmentRank())
                    .particleRenderer(config.getParticleRenderer() != null ? config.getParticleRenderer() : particleRenderer)
                    .impactHandler(config.getImpactHandler() != null ? config.getImpactHandler() : defaultImpactHandler)
                    .build();
            }
            
            // Create controller
            UUID controllerId = UUID.randomUUID();
            BlockController controller = new BlockController(controllerId, armorStand, finalConfig);
            
            // Spawn falling block shell if configured
            if (finalConfig.getShellType() == ShellType.FALLING_BLOCK) {
                // Check if real falling blocks are allowed
                if (!allowRealFallingBlocks) {
                    plugin.getLogger().warning("Real falling blocks disabled in config - using particle shell instead");
                    // Fall back to particle shell
                    if (particleRenderer != null) {
                        // Controller will use particle renderer instead
                    }
                } else {
                    // Create transaction for safe falling block management
                    FallingBlockTransaction transaction = new FallingBlockTransaction(
                        plugin,
                        config.getOwnerUUID(),
                        config.getAbilityId(),
                        config.getMaxLifeTicks()
                    );
                    
                    // Spawn falling block
                    FallingBlock fallingBlock = startLoc.getWorld().spawnFallingBlock(
                        startLoc,
                        config.getBlockType().createBlockData()
                    );
                    fallingBlock.setGravity(false);
                    fallingBlock.setDropItem(false);
                    fallingBlock.setHurtEntities(false);
                    fallingBlock.setInvulnerable(true);
                    fallingBlock.setTicksLived(1);
                    fallingBlock.setPersistent(false);
                    
                    // Track in transaction (adds tempFalling flag)
                    transaction.trackFallingBlock(fallingBlock);
                    transaction.commit();
                    
                    // Store transaction
                    activeTransactions.put(controllerId, transaction);
                    
                    controller.setFallingBlock(fallingBlock);
                }
            }
            
            // Store controller
            activeControllers.put(controllerId, controller);
            
            // Register with ActiveEffectRegistry
            long currentTime = System.currentTimeMillis();
            long lifetime = config.getMaxLifeTicks() * 50L; // Convert ticks to milliseconds
            
            EffectEntry entry = new EffectEntry(
                controllerId,
                config.getOwnerUUID(),
                config.getAbilityId(),
                EffectType.BLOCK_MANIPULATION,
                startLoc.clone(),
                currentTime,
                currentTime + lifetime,
                controller
            );
            
            // Add armor stand as linked entity
            entry.addLinkedEntity(armorStand);
            
            // Add falling block as linked entity if present
            if (controller.getFallingBlock() != null) {
                entry.addLinkedEntity(controller.getFallingBlock());
            }
            
            registry.registerEffect(entry);
            
            plugin.getLogger().log(Level.FINE, 
                "Spawned block controller: " + controllerId + " for ability: " + config.getAbilityId());
            
            return controllerId;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to spawn block controller", e);
            return null;
        }
    }
    
    /**
     * Remove a block controller
     * 
     * @param controllerId UUID of the controller to remove
     * @return true if controller was found and removed
     */
    public boolean removeController(UUID controllerId) {
        BlockController controller = activeControllers.remove(controllerId);
        
        if (controller == null) {
            return false;
        }
        
        // Revert transaction if exists
        FallingBlockTransaction transaction = activeTransactions.remove(controllerId);
        if (transaction != null) {
            transaction.revert();
        }
        
        // Cleanup controller entities
        controller.remove();
        
        // Unregister from ActiveEffectRegistry
        registry.unregisterEffect(controllerId);
        
        plugin.getLogger().log(Level.FINE, "Removed block controller: " + controllerId);
        
        return true;
    }
    
    /**
     * Get a controller by its ID
     * 
     * @param controllerId UUID of the controller
     * @return The controller, or null if not found
     */
    public BlockController getController(UUID controllerId) {
        return activeControllers.get(controllerId);
    }
    
    /**
     * Update all active controllers
     * Called every tick by the update task
     */
    public void updateAll() {
        if (activeControllers.isEmpty()) {
            return;
        }
        
        // Update task running silently
        
        // Update each controller
        for (Map.Entry<UUID, BlockController> entry : activeControllers.entrySet()) {
            UUID controllerId = entry.getKey();
            BlockController controller = entry.getValue();
            
            try {
                // Update controller physics and collision
                controller.update();
                
                // Remove if expired
                if (controller.isExpired()) {
                    removeController(controllerId);
                }
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Error updating block controller " + controllerId, e);
                // Remove problematic controller
                removeController(controllerId);
            }
        }
    }
    
    /**
     * Get the number of active controllers
     * 
     * @return Number of active controllers
     */
    public int getActiveControllerCount() {
        return activeControllers.size();
    }
    
    /**
     * Get the number of controllers owned by a specific player
     * 
     * @param ownerUUID UUID of the player
     * @return Number of controllers owned by this player
     */
    public int getPlayerControllerCount(UUID ownerUUID) {
        return (int) activeControllers.values().stream()
            .filter(c -> c.getOwnerUUID().equals(ownerUUID))
            .count();
    }
    
    /**
     * Remove all controllers owned by a specific player
     * Called when player logs out
     * 
     * @param ownerUUID UUID of the player
     * @return Number of controllers removed
     */
    public int removePlayerControllers(UUID ownerUUID) {
        int removed = 0;
        
        for (Map.Entry<UUID, BlockController> entry : activeControllers.entrySet()) {
            if (entry.getValue().getOwnerUUID().equals(ownerUUID)) {
                removeController(entry.getKey());
                removed++;
            }
        }
        
        return removed;
    }
    
    /**
     * Cleanup expired transactions
     * Called periodically to revert expired falling block transactions
     */
    private void cleanupExpiredTransactions() {
        if (activeTransactions.isEmpty()) {
            return;
        }
        
        int cleaned = 0;
        for (Map.Entry<UUID, FallingBlockTransaction> entry : activeTransactions.entrySet()) {
            FallingBlockTransaction transaction = entry.getValue();
            
            if (transaction.isExpired() && !transaction.isReverted()) {
                transaction.revert();
                cleaned++;
            }
        }
        
        // Remove reverted transactions
        activeTransactions.entrySet().removeIf(entry -> entry.getValue().isReverted());
        
        if (cleaned > 0) {
            plugin.getLogger().log(Level.FINE, "Cleaned up " + cleaned + " expired transactions");
        }
    }
    
    /**
     * Clear all controllers (for shutdown or testing)
     */
    public void clearAll() {
        plugin.getLogger().info("Clearing all block controllers...");
        
        // Revert all transactions first
        for (FallingBlockTransaction transaction : activeTransactions.values()) {
            if (!transaction.isReverted()) {
                transaction.revert();
            }
        }
        activeTransactions.clear();
        
        for (UUID controllerId : activeControllers.keySet()) {
            removeController(controllerId);
        }
        
        activeControllers.clear();
        
        plugin.getLogger().info("All block controllers cleared");
    }
    
    /**
     * Get a transaction by controller ID
     * 
     * @param controllerId UUID of the controller
     * @return The transaction, or null if not found
     */
    public FallingBlockTransaction getTransaction(UUID controllerId) {
        return activeTransactions.get(controllerId);
    }
    
    /**
     * Get the number of active transactions
     * 
     * @return Number of active transactions
     */
    public int getActiveTransactionCount() {
        return activeTransactions.size();
    }
    
    /**
     * Check if real falling blocks are allowed
     * 
     * @return true if allowed
     */
    public boolean isAllowRealFallingBlocks() {
        return allowRealFallingBlocks;
    }
    
    /**
     * Set whether real falling blocks are allowed
     * 
     * @param allow true to allow
     */
    public void setAllowRealFallingBlocks(boolean allow) {
        this.allowRealFallingBlocks = allow;
        plugin.getLogger().info("Real falling blocks " + (allow ? "enabled" : "disabled"));
    }
    
    /**
     * Shutdown the engine
     * Stops update task and clears all controllers
     */
    public void shutdown() {
        stopUpdateTask();
        clearAll();
        
        plugin.getLogger().info("BlockManipulationEngine shutdown complete");
    }
}
