package com.muzlik.ritual;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fx.FXLibrary;
import com.muzlik.fx.SoundPreset;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all ritual processes including creation, changing, and custom rituals.
 */
public class RitualManager {
    private final JavaPlugin plugin;
    private final Map<UUID, RitualInstance> activeRituals;
    private final FragmentManager fragmentManager;
    private final FXLibrary fxLibrary;
    private com.muzlik.fragment.ability.AbilitySlotManager abilitySlotManager;
    private BukkitRunnable updateTask;
    
    // Configuration
    private double proximityDistance = 5.0;
    private long failureCooldown = 60000; // 1 minute in milliseconds
    private final Map<UUID, Long> failureCooldowns;

    public RitualManager(JavaPlugin plugin, FragmentManager fragmentManager, FXLibrary fxLibrary) {
        this.plugin = plugin;
        this.activeRituals = new ConcurrentHashMap<>();
        this.fragmentManager = fragmentManager;
        this.fxLibrary = fxLibrary;
        this.failureCooldowns = new ConcurrentHashMap<>();
        this.abilitySlotManager = null; // Will be set later
        
        startUpdateTask();
    }

    /**
     * Set the AbilitySlotManager (dependency injection)
     */
    public void setAbilitySlotManager(com.muzlik.fragment.ability.AbilitySlotManager abilitySlotManager) {
        this.abilitySlotManager = abilitySlotManager;
    }

    /**
     * Start a ritual
     */
    public boolean startRitual(Player player, RitualType type, ItemStack catalyst, FragmentType fragmentType) {
        // Check if player already has active ritual
        if (hasActiveRitual(player)) {
            player.sendMessage("§c✗ You already have an active ritual");
            return false;
        }
        
        // Check failure cooldown
        if (isOnFailureCooldown(player)) {
            long remaining = getFailureCooldownRemaining(player);
            player.sendMessage("§c✗ Ritual cooldown: " + (remaining / 1000) + " seconds");
            return false;
        }
        
        // Create ritual instance
        RitualInstance ritual = new RitualInstance(
            player.getUniqueId(),
            type,
            player.getLocation().clone(),
            type.getDefaultDuration() * 1000L, // Convert to milliseconds
            catalyst,
            fragmentType
        );
        
        activeRituals.put(player.getUniqueId(), ritual);
        
        player.sendMessage("§a✓ Ritual started: §b" + type.getDisplayName());
        player.sendMessage("§7Stay within " + proximityDistance + " blocks for " + 
            (type.getDefaultDuration() / 60) + " minutes");
        
        return true;
    }

    /**
     * Cancel a ritual
     */
    public void cancelRitual(Player player) {
        RitualInstance ritual = activeRituals.remove(player.getUniqueId());
        if (ritual != null) {
            player.sendMessage("§c✗ Ritual cancelled");
        }
    }

    /**
     * Check if player has active ritual
     */
    public boolean hasActiveRitual(Player player) {
        return activeRituals.containsKey(player.getUniqueId());
    }

    /**
     * Get active ritual for player
     */
    public RitualInstance getActiveRitual(Player player) {
        return activeRituals.get(player.getUniqueId());
    }

    /**
     * Update ritual (called by update task)
     */
    private void updateRitual(Player player, RitualInstance ritual) {
        // Check proximity
        if (!isInRitualProximity(player, ritual)) {
            failRitual(player, ritual, "Moved too far from ritual location");
            return;
        }
        
        // Update progress
        long elapsed = System.currentTimeMillis() - ritual.getStartTime();
        int progressPercent = (int) ((elapsed * 100) / ritual.getDuration());
        
        // Update stage
        RitualStage newStage = RitualStage.fromProgress(progressPercent);
        if (newStage != ritual.getStage()) {
            ritual.setStage(newStage);
            player.sendMessage("§e⚡ Ritual stage: §b" + newStage.name());
        }
        
        // Display effects
        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType != null) {
            fxLibrary.playRitualEffect(
                ritual.getLocation(),
                ritual.getStage(),
                fxLibrary.getColorScheme(fragmentType)
            );
        }
        
        // Check completion
        if (elapsed >= ritual.getDuration()) {
            completeRitual(player, ritual);
        }
    }

    /**
     * Check if player is in ritual proximity
     */
    public boolean isInRitualProximity(Player player, RitualInstance ritual) {
        Location ritualLoc = ritual.getLocation();
        Location playerLoc = player.getLocation();
        
        if (!ritualLoc.getWorld().equals(playerLoc.getWorld())) {
            return false;
        }
        
        return ritualLoc.distance(playerLoc) <= proximityDistance;
    }

    /**
     * Check if player is in ritual proximity (convenience method)
     */
    public boolean isInRitualProximity(Player player) {
        RitualInstance ritual = getActiveRitual(player);
        return ritual != null && isInRitualProximity(player, ritual);
    }

    /**
     * Complete a ritual
     */
    private void completeRitual(Player player, RitualInstance ritual) {
        activeRituals.remove(player.getUniqueId());
        
        RitualType type = ritual.getType();
        
        switch (type) {
            case FRAGMENT_CREATION:
                completeFragmentCreation(player, ritual);
                break;
            case FRAGMENT_CHANGER:
                completeFragmentChanger(player, ritual);
                break;
            case RANK_UP:
                completeRankUp(player, ritual);
                break;
            case ABILITY_EXPANSION:
                completeAbilityExpansion(player, ritual);
                break;
            case MASTERY_EXPANSION:
                completeMasteryExpansion(player, ritual);
                break;
            default:
                player.sendMessage("§a✓ Ritual complete!");
                break;
        }
        
        // Play completion effects
        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType != null) {
            fxLibrary.playRitualEffect(
                ritual.getLocation(),
                RitualStage.COMPLETION,
                fxLibrary.getColorScheme(fragmentType)
            );
            fxLibrary.playSound(ritual.getLocation(), SoundPreset.RITUAL_COMPLETE, 1.0f, 1.0f);
        }
    }

    /**
     * Complete Fragment Creation ritual
     */
    private void completeFragmentCreation(Player player, RitualInstance ritual) {
        // Determine Fragment type from catalyst item
        FragmentType fragmentType = determineFragmentType(ritual);
        
        if (fragmentType != null) {
            fragmentManager.grantFragment(player, fragmentType);
            player.sendMessage("§a✓ Fragment Creation complete!");
        } else {
            player.sendMessage("§c✗ Failed to determine Fragment type");
        }
    }

    /**
     * Complete Fragment Changer ritual
     */
    private void completeFragmentChanger(Player player, RitualInstance ritual) {
        player.sendMessage("§a✓ Fragment Changer ritual complete!");
        player.sendMessage("§7You can now switch Fragments without cooldown!");
        player.sendMessage("§7Use §e/fragment list §7or §e/fragment activate <type> §7to switch");
        
        // Reset the Fragment switch cooldown by recording a switch from long ago
        fragmentManager.recordFragmentSwitch(player);
        // Actually, we want to allow immediate switching, so let's set it to 0
        // by recording a switch time far in the past
        com.muzlik.fragment.PlayerFragmentData data = fragmentManager.getPlayerData(player);
        if (data != null) {
            data.setLastFragmentSwitch(0); // Reset to allow immediate switch
        }
    }

    /**
     * Complete Rank Up ritual
     */
    private void completeRankUp(Player player, RitualInstance ritual) {
        player.sendMessage("§a✓ Rank Up ritual complete!");
        // Rank up logic handled elsewhere
    }

    /**
     * Complete Ability Expansion ritual (unlocks slot 3)
     */
    private void completeAbilityExpansion(Player player, RitualInstance ritual) {
        if (abilitySlotManager == null) {
            player.sendMessage("§c✗ Ability system not initialized!");
            return;
        }

        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType == null) {
            player.sendMessage("§c✗ Failed to determine Fragment type");
            return;
        }

        // Get current Fragment data
        com.muzlik.fragment.PlayerFragmentData fragmentData = fragmentManager.getPlayerData(player);
        if (fragmentData == null || fragmentData.getActiveFragment() != fragmentType) {
            player.sendMessage("§c✗ You must have the Fragment active to unlock its abilities!");
            return;
        }

        int currentRank = 5; // Default rank for testing, should be retrieved from fragment progress system

        // Validate unlock requirements
        if (!abilitySlotManager.canUnlockSlot(fragmentType, currentRank, 3)) {
            String errorMsg = abilitySlotManager.getUnlockErrorMessage(fragmentType, currentRank, 3);
            player.sendMessage(errorMsg);
            return;
        }

        // Unlock slot 3
        boolean unlocked = abilitySlotManager.unlockSlot(player, fragmentType, 3);
        if (unlocked) {
            player.sendMessage("§a✓ Ability Expansion complete!");
            player.sendMessage("§e⚡ Unlocked Advanced Ability Slot (Slot 3)!");
            player.sendMessage("§7You can now use your 4th ability!");
        } else {
            player.sendMessage("§e⚠ Slot 3 was already unlocked");
        }
    }

    /**
     * Complete Mastery Expansion ritual (unlocks slot 4)
     */
    private void completeMasteryExpansion(Player player, RitualInstance ritual) {
        if (abilitySlotManager == null) {
            player.sendMessage("§c✗ Ability system not initialized!");
            return;
        }

        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType == null) {
            player.sendMessage("§c✗ Failed to determine Fragment type");
            return;
        }

        // Get current Fragment data
        com.muzlik.fragment.PlayerFragmentData fragmentData = fragmentManager.getPlayerData(player);
        if (fragmentData == null || fragmentData.getActiveFragment() != fragmentType) {
            player.sendMessage("§c✗ You must have the Fragment active to unlock its abilities!");
            return;
        }

        int currentRank = 7; // Default rank for testing, should be retrieved from fragment progress system

        // Validate unlock requirements
        if (!abilitySlotManager.canUnlockSlot(fragmentType, currentRank, 4)) {
            String errorMsg = abilitySlotManager.getUnlockErrorMessage(fragmentType, currentRank, 4);
            player.sendMessage(errorMsg);
            return;
        }

        // Unlock slot 4
        boolean unlocked = abilitySlotManager.unlockSlot(player, fragmentType, 4);
        if (unlocked) {
            player.sendMessage("§a✓ Mastery Expansion complete!");
            player.sendMessage("§e⚡ Unlocked Mastery Ability Slot (Slot 4)!");
            player.sendMessage("§7You can now use your 5th ability!");
            player.sendMessage("§6§l★ You have achieved mastery of this Fragment! ★");
        } else {
            player.sendMessage("§e⚠ Slot 4 was already unlocked");
        }
    }

    /**
     * Fail a ritual
     */
    private void failRitual(Player player, RitualInstance ritual, String reason) {
        activeRituals.remove(player.getUniqueId());
        
        player.sendMessage("§c✗ Ritual failed: " + reason);
        
        // Apply failure cooldown
        failureCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + failureCooldown);
        
        // Play failure effects
        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType != null) {
            fxLibrary.playSound(ritual.getLocation(), SoundPreset.RITUAL_FAIL, 1.0f, 0.8f);
        }
    }

    /**
     * Determine Fragment type from ritual
     */
    private FragmentType determineFragmentType(RitualInstance ritual) {
        return ritual.getFragmentType();
    }

    /**
     * Check if player is on failure cooldown
     */
    private boolean isOnFailureCooldown(Player player) {
        Long cooldownEnd = failureCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= cooldownEnd) {
            failureCooldowns.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }

    /**
     * Get failure cooldown remaining
     */
    private long getFailureCooldownRemaining(Player player) {
        Long cooldownEnd = failureCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }
        
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }

    /**
     * Start ritual update task
     */
    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, RitualInstance> entry : activeRituals.entrySet()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) {
                        // Player disconnected, cancel ritual
                        activeRituals.remove(entry.getKey());
                        continue;
                    }
                    
                    updateRitual(player, entry.getValue());
                }
            }
        };
        
        // Run every second (20 ticks)
        updateTask.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Stop ritual update task
     */
    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    /**
     * Set proximity distance
     */
    public void setProximityDistance(double distance) {
        this.proximityDistance = distance;
    }

    /**
     * Get proximity distance
     */
    public double getProximityDistance() {
        return proximityDistance;
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        activeRituals.remove(player.getUniqueId());
        failureCooldowns.remove(player.getUniqueId());
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        stopUpdateTask();
        activeRituals.clear();
        failureCooldowns.clear();
    }
}
