package com.muzlik.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manager for packet-only block changes.
 * 
 * Sends block changes to clients without modifying server world state.
 * Provides safe, reversible visual block changes for abilities.
 * 
 * Per SYSTEM.md specification:
 * - Packet-only blocks sent via sendBlockChange
 * - Only send to players within 32 block radius
 * - Automatic revert on expiration
 * - Handle player join/leave for packet sync
 * 
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7
 */
public class PacketBlockManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, PacketBlockGroup> activeGroups;
    private BukkitTask cleanupTask;
    
    // Maximum radius for sending packet blocks
    private static final double MAX_RADIUS = 32.0;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     */
    public PacketBlockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeGroups = new ConcurrentHashMap<>();
    }
    
    /**
     * Start the cleanup task
     */
    public void startCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        // Run every 20 ticks (1 second)
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredGroups, 20L, 20L);
        
        plugin.getLogger().info("PacketBlockManager cleanup task started");
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
     * Create a new packet block group
     * 
     * @param ownerUUID UUID of the player who owns this group
     * @param durationTicks Duration before auto-revert (in ticks)
     * @return UUID of the created group
     */
    public UUID createBlockGroup(UUID ownerUUID, long durationTicks) {
        UUID groupId = UUID.randomUUID();
        long expirationTime = System.currentTimeMillis() + (durationTicks * 50L);
        
        PacketBlockGroup group = new PacketBlockGroup(groupId, ownerUUID, expirationTime);
        activeGroups.put(groupId, group);
        
        plugin.getLogger().log(Level.FINE, 
            "Created packet block group: " + groupId + " for owner " + ownerUUID);
        
        return groupId;
    }
    
    /**
     * Add a block to a group
     * 
     * @param groupId UUID of the group
     * @param location Location of the block
     * @param blockData Block data to display
     * @return true if added successfully
     */
    public boolean addBlock(UUID groupId, Location location, BlockData blockData) {
        PacketBlockGroup group = activeGroups.get(groupId);
        
        if (group == null) {
            plugin.getLogger().warning("Packet block group not found: " + groupId);
            return false;
        }
        
        group.addBlock(location, blockData);
        return true;
    }
    
    /**
     * Send packet blocks to nearby players
     * Only sends to players within specified radius
     * 
     * @param groupId UUID of the group
     * @param center Center location for radius check
     * @param radius Maximum radius (capped at 32 blocks)
     */
    public void sendToNearbyPlayers(UUID groupId, Location center, double radius) {
        PacketBlockGroup group = activeGroups.get(groupId);
        
        if (group == null || center == null || center.getWorld() == null) {
            return;
        }
        
        // Cap radius at maximum
        double effectiveRadius = Math.min(radius, MAX_RADIUS);
        
        // Find nearby players
        Collection<Player> nearbyPlayers = center.getWorld().getNearbyEntities(
            center, effectiveRadius, effectiveRadius, effectiveRadius
        ).stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .toList();
        
        // Send to each nearby player
        for (Player player : nearbyPlayers) {
            group.sendToPlayer(player);
        }
        
        plugin.getLogger().log(Level.FINE, 
            "Sent packet block group " + groupId + " to " + nearbyPlayers.size() + " players");
    }
    
    /**
     * Revert a packet block group
     * Sends original block data back to all affected players
     * 
     * @param groupId UUID of the group
     * @return true if reverted successfully
     */
    public boolean revertGroup(UUID groupId) {
        PacketBlockGroup group = activeGroups.remove(groupId);
        
        if (group == null) {
            return false;
        }
        
        group.revertAll();
        
        plugin.getLogger().log(Level.FINE, 
            "Reverted packet block group: " + groupId);
        
        return true;
    }
    
    /**
     * Handle player join - send active packet blocks
     * 
     * @param event Player join event
     */
    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        
        // Send all active packet block groups that are nearby
        for (PacketBlockGroup group : activeGroups.values()) {
            if (group.isNearby(playerLoc, MAX_RADIUS)) {
                group.sendToPlayer(player);
            }
        }
    }
    
    /**
     * Cleanup expired packet block groups
     */
    private void cleanupExpiredGroups() {
        if (activeGroups.isEmpty()) {
            return;
        }
        
        int cleaned = 0;
        List<UUID> expiredIds = new ArrayList<>();
        
        for (Map.Entry<UUID, PacketBlockGroup> entry : activeGroups.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredIds.add(entry.getKey());
            }
        }
        
        for (UUID groupId : expiredIds) {
            revertGroup(groupId);
            cleaned++;
        }
        
        if (cleaned > 0) {
            plugin.getLogger().log(Level.FINE, 
                "Cleaned up " + cleaned + " expired packet block groups");
        }
    }
    
    /**
     * Get the number of active groups
     * 
     * @return Number of active groups
     */
    public int getActiveGroupCount() {
        return activeGroups.size();
    }
    
    /**
     * Get a packet block group
     * 
     * @param groupId UUID of the group
     * @return The group, or null if not found
     */
    public PacketBlockGroup getGroup(UUID groupId) {
        return activeGroups.get(groupId);
    }
    
    /**
     * Clear all packet block groups
     */
    public void clearAll() {
        plugin.getLogger().info("Clearing all packet block groups...");
        
        for (UUID groupId : new ArrayList<>(activeGroups.keySet())) {
            revertGroup(groupId);
        }
        
        activeGroups.clear();
        
        plugin.getLogger().info("All packet block groups cleared");
    }
    
    /**
     * Shutdown the manager
     */
    public void shutdown() {
        stopCleanupTask();
        clearAll();
        
        plugin.getLogger().info("PacketBlockManager shutdown complete");
    }
    
    /**
     * Packet block group - represents a collection of packet-only blocks
     */
    public static class PacketBlockGroup {
        private final UUID groupId;
        private final UUID ownerUUID;
        private final Map<BlockPosition, BlockData> packetBlocks;
        private final Map<BlockPosition, BlockData> originalBlocks;
        private final Set<UUID> affectedPlayers;
        private final long expirationTime;
        
        public PacketBlockGroup(UUID groupId, UUID ownerUUID, long expirationTime) {
            this.groupId = groupId;
            this.ownerUUID = ownerUUID;
            this.packetBlocks = new HashMap<>();
            this.originalBlocks = new HashMap<>();
            this.affectedPlayers = new HashSet<>();
            this.expirationTime = expirationTime;
        }
        
        public void addBlock(Location location, BlockData blockData) {
            BlockPosition pos = new BlockPosition(location);
            
            // Store original block data
            if (!originalBlocks.containsKey(pos)) {
                originalBlocks.put(pos, location.getBlock().getBlockData());
            }
            
            // Store packet block data
            packetBlocks.put(pos, blockData);
        }
        
        public void sendToPlayer(Player player) {
            if (player == null || !player.isOnline()) {
                return;
            }
            
            for (Map.Entry<BlockPosition, BlockData> entry : packetBlocks.entrySet()) {
                Location loc = entry.getKey().toLocation(player.getWorld());
                player.sendBlockChange(loc, entry.getValue());
            }
            
            affectedPlayers.add(player.getUniqueId());
        }
        
        public void revertForPlayer(Player player) {
            if (player == null || !player.isOnline()) {
                return;
            }
            
            for (Map.Entry<BlockPosition, BlockData> entry : originalBlocks.entrySet()) {
                Location loc = entry.getKey().toLocation(player.getWorld());
                player.sendBlockChange(loc, entry.getValue());
            }
            
            affectedPlayers.remove(player.getUniqueId());
        }
        
        public void revertAll() {
            for (UUID playerId : new ArrayList<>(affectedPlayers)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    revertForPlayer(player);
                }
            }
            
            affectedPlayers.clear();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() >= expirationTime;
        }
        
        public boolean isNearby(Location location, double radius) {
            if (packetBlocks.isEmpty()) {
                return false;
            }
            
            // Check if any block in the group is within radius
            for (BlockPosition pos : packetBlocks.keySet()) {
                Location blockLoc = pos.toLocation(location.getWorld());
                if (blockLoc.distance(location) <= radius) {
                    return true;
                }
            }
            
            return false;
        }
        
        public UUID getGroupId() {
            return groupId;
        }
        
        public UUID getOwnerUUID() {
            return ownerUUID;
        }
        
        public int getBlockCount() {
            return packetBlocks.size();
        }
    }
    
    /**
     * Block position - immutable position for use as map key
     */
    private static class BlockPosition {
        private final int x;
        private final int y;
        private final int z;
        private final String worldName;
        
        public BlockPosition(Location location) {
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();
            this.worldName = location.getWorld().getName();
        }
        
        public Location toLocation(org.bukkit.World world) {
            return new Location(world, x, y, z);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockPosition)) return false;
            BlockPosition that = (BlockPosition) o;
            return x == that.x && y == that.y && z == that.z && 
                   Objects.equals(worldName, that.worldName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, worldName);
        }
    }
}
