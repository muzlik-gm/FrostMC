package com.muzlik.vfx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Sound Engine for managing spatial audio and layered sound effects.
 * Handles 3D positioning, sound synchronization, and cleanup.
 * 
 * Requirements: 2.1, 2.2, 2.4, 2.5
 */
public class SoundEngine {
    private final JavaPlugin plugin;
    
    // Active sounds map: soundId -> ActiveSound
    private final Map<UUID, ActiveSound> activeSounds;
    
    // Update task for delayed sounds
    private BukkitTask updateTask;
    
    /**
     * Constructor
     * 
     * @param plugin The plugin instance
     */
    public SoundEngine(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeSounds = new ConcurrentHashMap<>();
    }
    
    /**
     * Start the update task
     * Checks for delayed sounds that are ready to play
     */
    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Run every 5 ticks
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateSpatialAudio();
        }, 5L, 5L);
    }
    
    /**
     * Stop the update task
     */
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    /**
     * Play a sound at a location
     * 
     * @param sound The sound definition
     * @param location The location to play the sound
     * @return The UUID of the active sound
     */
    public UUID playSound(SoundDefinition sound, Location location) {
        return playSound(sound, location, null);
    }
    
    /**
     * Play a sound at a location with owner tracking
     * 
     * @param sound The sound definition
     * @param location The location to play the sound
     * @param owner The player who owns this sound (can be null)
     * @return The UUID of the active sound
     */
    public UUID playSound(SoundDefinition sound, Location location, Player owner) {
        if (sound == null || location == null || location.getWorld() == null) {
            return null;
        }
        
        UUID soundId = UUID.randomUUID();
        UUID ownerId = owner != null ? owner.getUniqueId() : null;
        
        // Apply spatial offset
        Location soundLocation = location.clone();
        Vector offset = sound.getOffset();
        if (offset.lengthSquared() > 0) {
            soundLocation.add(offset);
        }
        
        // Create active sound
        ActiveSound activeSound = new ActiveSound(soundId, ownerId, soundLocation, sound);
        activeSounds.put(soundId, activeSound);
        
        // If no delay, play immediately
        if (sound.getDelay() == 0) {
            playSoundNow(activeSound);
        }
        
        plugin.getLogger().log(Level.FINE, 
                "Scheduled sound: " + sound.getSound() + " at " + soundLocation + 
                " (delay: " + sound.getDelay() + " ticks)");
        
        return soundId;
    }
    
    /**
     * Play multiple sound layers at a location
     * 
     * @param layers List of sound definitions
     * @param location The location to play the sounds
     * @param owner The player who owns these sounds (can be null)
     * @return List of sound UUIDs
     */
    public List<UUID> playSoundLayers(List<SoundDefinition> layers, Location location, Player owner) {
        if (layers == null || layers.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<UUID> soundIds = new ArrayList<>();
        for (SoundDefinition sound : layers) {
            UUID soundId = playSound(sound, location, owner);
            if (soundId != null) {
                soundIds.add(soundId);
            }
        }
        
        plugin.getLogger().log(Level.FINE, 
                "Played " + soundIds.size() + " sound layers at " + location);
        
        return soundIds;
    }
    
    /**
     * Update spatial audio
     * Plays delayed sounds and cleans up expired sounds
     */
    public void updateSpatialAudio() {
        if (activeSounds.isEmpty()) {
            return;
        }
        
        List<UUID> toRemove = new ArrayList<>();
        
        for (Map.Entry<UUID, ActiveSound> entry : activeSounds.entrySet()) {
            ActiveSound sound = entry.getValue();
            
            try {
                // Play sounds that are ready
                if (sound.isReadyToPlay()) {
                    playSoundNow(sound);
                }
                
                // Remove expired sounds
                if (sound.isExpired()) {
                    toRemove.add(entry.getKey());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                        "Error updating sound " + entry.getKey(), e);
                toRemove.add(entry.getKey());
            }
        }
        
        // Clean up
        for (UUID soundId : toRemove) {
            stopSound(soundId);
        }
    }
    
    /**
     * Play a sound immediately
     */
    private void playSoundNow(ActiveSound activeSound) {
        if (activeSound.isPlayed()) {
            return;
        }
        
        Location location = activeSound.getLocation();
        SoundDefinition def = activeSound.getDefinition();
        
        if (location.getWorld() == null) {
            return;
        }
        
        // Play the sound
        location.getWorld().playSound(
            location,
            def.getSound(),
            def.getVolume(),
            def.getPitch()
        );
        
        activeSound.markPlayed();
        
        plugin.getLogger().log(Level.FINE, 
                "Played sound: " + def.getSound() + " at " + location);
    }
    
    /**
     * Stop a sound
     * 
     * @param soundId The UUID of the sound to stop
     */
    public void stopSound(UUID soundId) {
        ActiveSound sound = activeSounds.remove(soundId);
        
        if (sound != null) {
            plugin.getLogger().log(Level.FINE, 
                    "Stopped sound: " + soundId);
        }
    }
    
    /**
     * Update the position of a sound (for moving effects)
     * 
     * @param soundId The UUID of the sound
     * @param newLocation The new location
     */
    public void updateSoundPosition(UUID soundId, Location newLocation) {
        ActiveSound sound = activeSounds.get(soundId);
        if (sound != null && !sound.isPlayed()) {
            sound.updatePosition(newLocation);
        }
    }
    
    /**
     * Get the number of active sounds
     */
    public int getActiveSoundCount() {
        return activeSounds.size();
    }
    
    /**
     * Clear all sounds
     */
    public void clearAll() {
        activeSounds.clear();
    }
    
    /**
     * Shutdown the sound engine
     */
    public void shutdown() {
        stopUpdateTask();
        clearAll();
    }
}
