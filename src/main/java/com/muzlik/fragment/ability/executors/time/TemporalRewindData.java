package com.muzlik.fragment.ability.executors.time;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages temporal state history for players using Temporal Rewind.
 * Maintains a circular buffer of the last 10 seconds of player state.
 */
public class TemporalRewindData {
    private static final int MAX_HISTORY = 10; // 10 seconds of history
    private static final Map<UUID, TemporalRewindData> playerData = new HashMap<>();
    
    private final LinkedList<PlayerState> history;
    private BukkitTask recordingTask;
    
    private TemporalRewindData() {
        this.history = new LinkedList<>();
    }
    
    /**
     * Get or create temporal data for a player
     * @param player The player
     * @return The temporal data manager
     */
    public static TemporalRewindData getOrCreate(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> new TemporalRewindData());
    }
    
    /**
     * Start recording player state every second
     * @param player The player to record
     * @param plugin The plugin instance
     */
    public void startRecording(Player player, Plugin plugin) {
        // Stop any existing recording task
        stopRecording();
        
        // Start new recording task
        recordingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }
                
                recordState(player);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second (20 ticks)
    }
    
    /**
     * Stop recording player state
     */
    public void stopRecording() {
        if (recordingTask != null) {
            recordingTask.cancel();
            recordingTask = null;
        }
    }
    
    /**
     * Record the current state of the player
     * @param player The player to record
     */
    public void recordState(Player player) {
        history.addLast(new PlayerState(player));
        
        // Maintain circular buffer - remove oldest if exceeds max
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }
    
    /**
     * Get the player state from N seconds ago
     * @param seconds Number of seconds in the past
     * @return The player state, or null if insufficient history
     */
    public PlayerState getStateFromSecondsAgo(int seconds) {
        if (history.isEmpty()) {
            return null;
        }
        
        // Calculate index (0 = oldest, size-1 = newest)
        int index = Math.max(0, history.size() - seconds - 1);
        
        if (index >= history.size()) {
            return null;
        }
        
        return history.get(index);
    }
    
    /**
     * Check if there is sufficient history for a rewind
     * @param seconds Number of seconds to check
     * @return True if sufficient history exists
     */
    public boolean hasSufficientHistory(int seconds) {
        return history.size() > seconds;
    }
    
    /**
     * Get the most recent state (fallback)
     * @return The most recent state, or null if no history
     */
    public PlayerState getMostRecentState() {
        return history.isEmpty() ? null : history.getLast();
    }
    
    /**
     * Clear all history for this player
     */
    public void clearHistory() {
        history.clear();
    }
    
    /**
     * Remove player data when they log out
     * @param playerId The player's UUID
     */
    public static void removePlayer(UUID playerId) {
        TemporalRewindData data = playerData.remove(playerId);
        if (data != null) {
            data.stopRecording();
            data.clearHistory();
        }
    }
    
    /**
     * Get the size of the history buffer
     * @return Number of recorded states
     */
    public int getHistorySize() {
        return history.size();
    }
}
