package com.muzlik.ritual;

import com.muzlik.config.ConfigManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fx.FXLibrary;
import com.muzlik.fx.SoundPreset;
import com.muzlik.ritual.structure.RitualStructureProtectionListener;
import com.muzlik.ritual.structure.SchematicRitualStructure;
import com.muzlik.ritual.structure.SimpleRitualStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
    private final Map<UUID, BossBar> ritualBossBars;
    private final FragmentManager fragmentManager;
    private final FXLibrary fxLibrary;
    private final ConfigManager configManager;
    private com.muzlik.fragment.ability.AbilitySlotManager abilitySlotManager;
    private BukkitRunnable updateTask;
    private boolean discordSRVEnabled = false;
    private com.muzlik.vfx.cinematic.CinematicVFXEngine cinematicVFXEngine;
    
    // Configuration
    private double proximityDistance = 5.0;
    private long failureCooldown = 60000; // 1 minute in milliseconds
    private final Map<UUID, Long> failureCooldowns;
    private RitualDisplayManager displayManager;
    private RitualStructureProtectionListener structureProtection;

    public RitualManager(JavaPlugin plugin, FragmentManager fragmentManager, FXLibrary fxLibrary, ConfigManager configManager) {
        this.plugin = plugin;
        this.activeRituals = new ConcurrentHashMap<>();
        this.ritualBossBars = new ConcurrentHashMap<>();
        this.fragmentManager = fragmentManager;
        this.fxLibrary = fxLibrary;
        this.configManager = configManager;
        this.failureCooldowns = new ConcurrentHashMap<>();
        this.abilitySlotManager = null; // Will be set later
        this.displayManager = new RitualDisplayManager(plugin);
        this.structureProtection = new RitualStructureProtectionListener(plugin);
        
        // Initialize cinematic VFX engine
        if (plugin instanceof com.muzlik.FrostSMPPlugin) {
            this.cinematicVFXEngine = ((com.muzlik.FrostSMPPlugin) plugin).getCinematicVFXEngine();
            if (this.cinematicVFXEngine != null) {
            } else {
                plugin.getLogger().warning("⚠️ RitualManager: Cinematic VFX Engine is NULL - Using FXLibrary fallback");
            }
        } else {
            plugin.getLogger().warning("⚠️ RitualManager: Plugin is not FrostSMPPlugin instance");
        }
        
        // Check if DiscordSRV is available
        checkDiscordSRV();
        
        startUpdateTask();
    }
    
    /**
     * Check if DiscordSRV is available
     */
    private void checkDiscordSRV() {
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            discordSRVEnabled = true;
        }
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
        
        // Validate ritual location
        Location ritualLocation = player.getLocation().clone();
        if (!isValidRitualLocation(player, ritualLocation)) {
            return false; // Error messages sent by validation method
        }
        
        // Check if this is a Fragment Creation ritual and if it was already completed
        if (type == RitualType.FRAGMENT_CREATION && fragmentType != null) {
            com.muzlik.fragment.PlayerFragmentData fragmentData = fragmentManager.getPlayerData(player);
            if (fragmentData != null && fragmentData.hasCompletedRitual(fragmentType)) {
                player.sendMessage("§c✗ You have already created the " + fragmentType.getDisplayName() + " Fragment!");
                player.sendMessage("§7You cannot perform this ritual again.");
                return false;
            }
        }
        
        // Check if player has the catalyst in their inventory
        if (catalyst != null) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem == null || !heldItem.isSimilar(catalyst)) {
                // Check if catalyst is in inventory at all
                if (!player.getInventory().containsAtLeast(catalyst, 1)) {
                    player.sendMessage("§c✗ You need the ritual catalyst in your inventory!");
                    return false;
                }
            }
            
            // Consume the catalyst from inventory
            ItemStack toRemove = catalyst.clone();
            toRemove.setAmount(1);
            player.getInventory().removeItem(toRemove);
            player.sendMessage("§7Ritual catalyst consumed...");
        }
        
        // Center to block coordinates (e.g., 225.7 -> 225.5, 64.3 -> 64.0, 504.2 -> 504.5)
        ritualLocation.setX(ritualLocation.getBlockX() + 0.5);
        ritualLocation.setY(ritualLocation.getBlockY());
        ritualLocation.setZ(ritualLocation.getBlockZ() + 0.5);
        
        RitualInstance ritual = new RitualInstance(
            player.getUniqueId(),
            type,
            ritualLocation,
            type.getDefaultDuration() * 1000L, // Convert to milliseconds
            catalyst,
            fragmentType
        );
        
        activeRituals.put(player.getUniqueId(), ritual);
        
        // Create boss bar
        createRitualBossBar(player, ritual);
        
        // Create floating fragment display with particle beams
        if (fragmentType != null) {
            ItemStack fragmentItem = fragmentManager.createFragmentItem(fragmentType);
            if (fragmentItem != null) {
                // Get the fragment's base rank from the fragment definition
                int fragmentRank = fragmentManager.getRankManager().getBaseRank(fragmentType);
                displayManager.createDisplay(player, ritual.getLocation(), fragmentType, fragmentItem, ritual.getDuration(), fragmentRank);
            }
            
            // Spawn ritual structure (schematic-based with fallback)
            SchematicRitualStructure structure = new SchematicRitualStructure(plugin, player.getUniqueId(), ritual.getLocation(), fragmentType);
            structure.spawn();
            structureProtection.registerStructure(player.getUniqueId(), structure);
        }
        
        player.sendMessage("§a✓ Ritual started: §b" + type.getDisplayName());
        player.sendMessage("§7Stay within " + proximityDistance + " blocks for " + 
            (type.getDefaultDuration() / 60) + " minutes");
        
        // Send Discord notification
        sendDiscordRitualStart(player, ritual);
        
        return true;
    }
    
    /**
     * Validate ritual location
     * - Must be in Overworld
     * - Must be above ground (has sky access)
     */
    private boolean isValidRitualLocation(Player player, Location location) {
        // Check if in Overworld
        if (location.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL) {
            player.sendMessage("§c✗ Rituals can only be performed in the Overworld!");
            player.sendMessage("§7You cannot perform rituals in the Nether or End.");
            return false;
        }
        
        // Check if location has sky access (not underground/in cave)
        org.bukkit.block.Block highestBlock = location.getWorld().getHighestBlockAt(location);
        int surfaceY = highestBlock.getY();
        int playerY = location.getBlockY();
        
        // Player must be at or near the surface (within 5 blocks below surface)
        if (playerY < surfaceY - 5) {
            player.sendMessage("§c✗ Rituals must be performed above ground!");
            player.sendMessage("§7You are underground. Find an open area under the sky.");
            player.sendMessage("§8(Surface is at Y=" + surfaceY + ", you are at Y=" + playerY + ")");
            return false;
        }
        
        return true;
    }

    /**
     * Cancel a ritual
     */
    public void cancelRitual(Player player) {
        RitualInstance ritual = activeRituals.remove(player.getUniqueId());
        if (ritual != null) {
            player.sendMessage("§c✗ Ritual cancelled");
            removeRitualBossBar(player.getUniqueId());
            displayManager.removeDisplay(player.getUniqueId());
            structureProtection.unregisterStructure(player.getUniqueId());
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
     * Now supports grace period - any player can maintain the ritual
     * @param shouldPlayVFX Whether to play VFX this tick (to avoid playing 4x per second)
     */
    private void updateRitual(UUID ritualOwnerId, RitualInstance ritual, boolean shouldPlayVFX) {
        Player owner = plugin.getServer().getPlayer(ritualOwnerId);
        Location ritualLoc = ritual.getLocation();
        
        // Check if ANY player is in the ritual area (not just the owner)
        boolean anyPlayerInArea = isAnyPlayerInRitualArea(ritual);
        
        // Handle grace period logic
        if (!anyPlayerInArea) {
            if (!ritual.isInGracePeriod()) {
                // Start grace period - no players in area
                ritual.startGracePeriod();
                
                // Warn players within 150 blocks
                warnNearbyPlayers(ritual, "§c⚠ RITUAL ABANDONED! §7Grace period started - " + 
                    RitualInstance.getGracePeriodDurationSeconds() + "s until ritual fails!");
                
                // Warn the owner specifically if online
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage("§c⚠ You left the ritual area! Return within " + 
                        RitualInstance.getGracePeriodDurationSeconds() + " seconds or the ritual will fail!");
                }
            } else {
                // Check if grace period expired
                if (ritual.isGracePeriodExpired()) {
                    failRitual(ritualOwnerId, ritual, "No players in ritual area for too long");
                    return;
                }
                
                // Update grace period countdown in boss bar
                updateRitualBossBarGracePeriod(ritualOwnerId, ritual);
            }
            return; // Don't progress ritual during grace period
        } else {
            // Player returned - end grace period if active
            if (ritual.isInGracePeriod()) {
                ritual.endGracePeriod();
                warnNearbyPlayers(ritual, "§a✓ Ritual resumed! §7A player has returned to the ritual area.");
            }
        }
        
        // Update progress
        long elapsed = System.currentTimeMillis() - ritual.getStartTime();
        int progressPercent = (int) ((elapsed * 100) / ritual.getDuration());
        
        // Update boss bar (every tick for smooth progress)
        updateRitualBossBar(ritualOwnerId, ritual);
        
        // Update stage - notify owner if online (only check once per second)
        if (shouldPlayVFX) {
            RitualStage newStage = RitualStage.fromProgress(progressPercent);
            if (newStage != ritual.getStage()) {
                ritual.setStage(newStage);
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage("§e⚡ Ritual stage: §b" + newStage.name());
                }
            }
        }
        
        // Display effects - Only play once per second to avoid spam
        if (shouldPlayVFX) {
            FragmentType fragmentType = determineFragmentType(ritual);
            if (fragmentType != null) {
                // OLD VFX SYSTEM DISABLED - Using new rank-based magic circles in RitualDisplayManager instead
                // The old system was causing:
                // 1. Duplicate magic circles (light blue + dark blue)
                // 2. Performance issues (high ping due to excessive particles)
                // 3. Visual clutter
                
                // New system is in RitualDisplayManager.drawMagicCircles()
                // which provides rank-based complexity and better performance
                
                // Play dramatic ambient sounds during ritual
                playRitualAmbientSounds(ritual, progressPercent);
            }
        }
        
        // Check completion
        if (elapsed >= ritual.getDuration()) {
            completeRitual(ritualOwnerId, ritual);
        }
    }
    
    /**
     * Play dramatic ambient sounds during ritual based on progress
     */
    private void playRitualAmbientSounds(RitualInstance ritual, int progressPercent) {
        Location loc = ritual.getLocation();
        
        // Base ambient sound - plays throughout
        if (Math.random() < 0.3) { // 30% chance per second
            loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 0.7f);
        }
        
        // Dramatic building sounds based on progress
        if (progressPercent < 25) {
            // Early stage - mysterious whispers
            if (Math.random() < 0.2) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_ENDERMAN_AMBIENT, 0.4f, 0.5f);
            }
        } else if (progressPercent < 50) {
            // Mid stage - building tension
            if (Math.random() < 0.25) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_BEACON_AMBIENT, 0.6f, 0.8f);
            }
            if (Math.random() < 0.15) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.5f, 1.2f);
            }
        } else if (progressPercent < 75) {
            // Late stage - intense energy
            if (Math.random() < 0.3) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.7f, 1.5f);
            }
            if (Math.random() < 0.2) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_WARDEN_HEARTBEAT, 0.4f, 1.0f);
            }
        } else {
            // Final stage - climactic power
            if (Math.random() < 0.35) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 0.3f, 1.8f);
            }
            if (Math.random() < 0.25) {
                loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 1.5f);
            }
        }
    }
    
    /**
     * Check if ANY player is in the ritual area
     */
    private boolean isAnyPlayerInRitualArea(RitualInstance ritual) {
        Location ritualLoc = ritual.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(ritualLoc.getWorld()) && 
                player.getLocation().distance(ritualLoc) <= proximityDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get any player in the ritual area (for VFX purposes)
     */
    private Player getAnyPlayerInRitualArea(RitualInstance ritual) {
        Location ritualLoc = ritual.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(ritualLoc.getWorld()) && 
                player.getLocation().distance(ritualLoc) <= proximityDistance) {
                return player;
            }
        }
        return null;
    }
    
    /**
     * Warn all players within 150 blocks of the ritual
     */
    private void warnNearbyPlayers(RitualInstance ritual, String message) {
        Location ritualLoc = ritual.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(ritualLoc.getWorld()) && 
                player.getLocation().distance(ritualLoc) <= 150) {
                player.sendMessage(message);
            }
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
    private void completeRitual(UUID ownerId, RitualInstance ritual) {
        activeRituals.remove(ownerId);
        removeRitualBossBar(ownerId);
        structureProtection.unregisterStructure(ownerId);
        
        Player owner = plugin.getServer().getPlayer(ownerId);
        RitualType type = ritual.getType();
        
        switch (type) {
            case FRAGMENT_CREATION:
                completeFragmentCreation(ownerId, ritual);
                break;
            case FRAGMENT_CHANGER:
                if (owner != null) completeFragmentChanger(owner, ritual);
                break;
            case RANK_UP:
                if (owner != null) completeRankUp(owner, ritual);
                break;
            case ABILITY_EXPANSION:
                if (owner != null) completeAbilityExpansion(owner, ritual);
                break;
            case MASTERY_EXPANSION:
                if (owner != null) completeMasteryExpansion(owner, ritual);
                break;
            default:
                if (owner != null) owner.sendMessage("§a✓ Ritual complete!");
                break;
        }
        
        // Play completion effects - Use cinematic VFX if available
        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType != null) {
            Player vfxPlayer = owner != null ? owner : getAnyPlayerInRitualArea(ritual);
            if (cinematicVFXEngine != null && vfxPlayer != null) {
                // Use cinematic completion animation
                com.muzlik.vfx.cinematic.ritual.RitualAnimationSequence ritualAnimations = cinematicVFXEngine.getRitualAnimations();
                ritualAnimations.playCompletionAnimation(ritual.getLocation(), fragmentType, vfxPlayer);
            } else {
                // Fallback to FXLibrary
                fxLibrary.playRitualEffect(
                    ritual.getLocation(),
                    RitualStage.COMPLETION,
                    fxLibrary.getColorScheme(fragmentType)
                );
                fxLibrary.playSound(ritual.getLocation(), SoundPreset.RITUAL_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Complete Fragment Creation ritual
     * Behavior depends on fragment_changer_required config:
     * - If TRUE: Drops item on ground for player to pick up and activate
     * - If FALSE: Auto-grants and activates the fragment immediately
     */
    private void completeFragmentCreation(UUID ownerId, RitualInstance ritual) {
        // Determine Fragment type from catalyst item
        FragmentType fragmentType = determineFragmentType(ritual);
        Player owner = plugin.getServer().getPlayer(ownerId);
        
        if (fragmentType != null) {
            // Mark ritual as completed (one-time only)
            if (owner != null) {
                com.muzlik.fragment.PlayerFragmentData fragmentData = fragmentManager.getPlayerData(owner);
                if (fragmentData != null) {
                    fragmentData.markRitualCompleted(fragmentType);
                }
            }
            
            // Check if Fragment Changer is required
            boolean fragmentChangerRequired = configManager.isFragmentChangerRequired();
            
            if (fragmentChangerRequired) {
                // Drop the fragment from the floating display to the ground
                // The display manager handles this - it drops the floating item
                displayManager.completeAndDropFragment(ownerId);

                if (owner != null) {
                    owner.sendMessage("§a✓ Fragment Creation complete!");
                    owner.sendMessage("");
                    owner.sendMessage("§e§l⚡ " + fragmentType.getDisplayName() + " Fragment has materialized!");
                    owner.sendMessage("§7Pick it up and right-click to activate");
                    owner.sendMessage("§c§l⚠ This fragment can only be created once!");
                }
            } else {
                // Auto-grant and activate the fragment
                displayManager.removeDisplay(ownerId); // Remove the floating display
                
                if (owner != null) {
                    fragmentManager.grantFragment(owner, fragmentType);
                    fragmentManager.setActiveFragment(owner, fragmentType);
                    
                    owner.sendMessage("§a✓ Fragment Creation complete!");
                    owner.sendMessage("");
                    owner.sendMessage("§e§l⚡ " + fragmentType.getDisplayName() + " Fragment activated!");
                    owner.sendMessage("§7Your fragment has been automatically activated");
                    owner.sendMessage("§c§l⚠ This fragment can only be created once!");
                }
            }
            
            // Broadcast completion
            if (owner != null) {
                Location loc = ritual.getLocation();
                String ownerName = owner.getName();
                Bukkit.broadcastMessage(String.format(
                    "§a§l✓ RITUAL COMPLETE! §r§7%s has created a §b%s Fragment §7at §f%d, %d, %d",
                    ownerName,
                    fragmentType.getDisplayName(),
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
                ));
            }
        } else {
            if (owner != null) owner.sendMessage("§c✗ Failed to determine Fragment type");
            displayManager.removeDisplay(ownerId);
        }
    }

    /**
     * Complete Fragment Changer ritual
     * Resets cooldown to allow immediate fragment switching
     */
    private void completeFragmentChanger(Player player, RitualInstance ritual) {
        player.sendMessage("§a✓ Fragment Changer ritual complete!");
        player.sendMessage("§7You can now switch to owned Fragments!");
        player.sendMessage("§7Right-click owned fragments in §e/fragment list §7to switch");
        
        // Reset the Fragment switch cooldown
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
    private void failRitual(UUID ownerId, RitualInstance ritual, String reason) {
        activeRituals.remove(ownerId);
        removeRitualBossBar(ownerId);
        displayManager.removeDisplay(ownerId);
        structureProtection.unregisterStructure(ownerId);
        
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null) {
            owner.sendMessage("§c✗ Ritual failed: " + reason);
        }
        
        // Broadcast failure to nearby players
        warnNearbyPlayers(ritual, "§c✗ RITUAL FAILED! §7" + reason);
        
        // Apply failure cooldown
        failureCooldowns.put(ownerId, System.currentTimeMillis() + failureCooldown);
        
        // Play failure effects - Use cinematic VFX if available
        FragmentType fragmentType = determineFragmentType(ritual);
        if (fragmentType != null) {
            Player vfxPlayer = owner != null ? owner : getAnyPlayerInRitualArea(ritual);
            if (cinematicVFXEngine != null && vfxPlayer != null) {
                // Use cinematic failure animation
                com.muzlik.vfx.cinematic.ritual.RitualAnimationSequence ritualAnimations = cinematicVFXEngine.getRitualAnimations();
                ritualAnimations.playFailureAnimation(ritual.getLocation(), fragmentType, vfxPlayer);
            } else {
                // Fallback to FXLibrary
                fxLibrary.playSound(ritual.getLocation(), SoundPreset.RITUAL_FAIL, 1.0f, 0.8f);
            }
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
     * Now handles player disconnects with grace period instead of immediate cancel
     * Runs 4 times per second (every 5 ticks) for responsive grace period detection
     */
    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        updateTask = new BukkitRunnable() {
            private int tickCounter = 0;
            
            @Override
            public void run() {
                tickCounter++;
                
                // Use iterator to safely remove entries during iteration
                java.util.Iterator<Map.Entry<UUID, RitualInstance>> iterator = activeRituals.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, RitualInstance> entry = iterator.next();
                    UUID ownerId = entry.getKey();
                    RitualInstance ritual = entry.getValue();
                    
                    // Update ritual - grace period handles player absence
                    // Pass tickCounter to control VFX frequency (only every 4th call = once per second)
                    updateRitual(ownerId, ritual, tickCounter % 4 == 0);
                }
            }
        };
        
        // Run 4 times per second (every 5 ticks) for responsive grace period detection
        updateTask.runTaskTimer(plugin, 5L, 5L);
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
     * Remove player data and cleanup ritual display
     * Called when player disconnects
     */
    public void removePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        RitualInstance ritual = activeRituals.get(playerId);
        
        if (ritual != null) {
            // Don't remove the ritual - let grace period handle it
            // But DO cleanup the display if player was the owner
            // The ritual will continue if other players are in the area
        }
        
        failureCooldowns.remove(playerId);
    }
    
    /**
     * Force cleanup a ritual (for admin commands or shutdown)
     */
    public void forceCleanupRitual(UUID playerId) {
        RitualInstance ritual = activeRituals.remove(playerId);
        if (ritual != null) {
            removeRitualBossBar(playerId);
            displayManager.removeDisplay(playerId);
        }
        failureCooldowns.remove(playerId);
    }

    /**
     * Create boss bar for ritual - visible to ALL players
     */
    private void createRitualBossBar(Player player, RitualInstance ritual) {
        FragmentType fragmentType = determineFragmentType(ritual);
        BarColor color = getFragmentBarColor(fragmentType);
        Location loc = ritual.getLocation();
        
        String title = String.format("§e⚡ %s §8- §b%s §8| §7Location: §f%d, %d, %d", 
            ritual.getType().getDisplayName(),
            fragmentType != null ? fragmentType.getDisplayName() : "Unknown",
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        
        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SEGMENTED_10);
        bossBar.setProgress(0.0);
        
        // Add ALL online players to the boss bar
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(onlinePlayer);
        }
        
        bossBar.setVisible(true);
        
        ritualBossBars.put(player.getUniqueId(), bossBar);
        
        // Announce ritual start in chat to all players
        String announcement = String.format(
            "§e§l⚡ RITUAL STARTED! §r§7%s is performing §b%s §7at §f%d, %d, %d",
            player.getName(),
            ritual.getType().getDisplayName(),
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        );
        Bukkit.broadcastMessage(announcement);
    }
    
    /**
     * Update boss bar for ritual
     */
    private void updateRitualBossBar(UUID ownerId, RitualInstance ritual) {
        BossBar bossBar = ritualBossBars.get(ownerId);
        if (bossBar == null) return;
        
        // Calculate progress
        double progress = Math.min(1.0, (double) ritual.getProgressPercent() / 100.0);
        bossBar.setProgress(progress);
        
        // Update title with time remaining and coordinates
        long remainingSeconds = ritual.getRemainingSeconds();
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        Location loc = ritual.getLocation();
        
        FragmentType fragmentType = determineFragmentType(ritual);
        String title = String.format("§e⚡ %s §8- §b%s §8| §7Time: §f%d:%02d §8| §7Loc: §f%d, %d, %d", 
            ritual.getType().getDisplayName(),
            fragmentType != null ? fragmentType.getDisplayName() : "Unknown",
            minutes, seconds,
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        
        bossBar.setTitle(title);
    }
    
    /**
     * Update boss bar during grace period
     */
    private void updateRitualBossBarGracePeriod(UUID ownerId, RitualInstance ritual) {
        BossBar bossBar = ritualBossBars.get(ownerId);
        if (bossBar == null) return;
        
        // Show grace period countdown
        long graceRemaining = ritual.getGracePeriodRemainingSeconds();
        Location loc = ritual.getLocation();
        
        FragmentType fragmentType = determineFragmentType(ritual);
        String title = String.format("§c⚠ GRACE PERIOD §8- §b%s §8| §cTime: §f%ds §8| §7Loc: §f%d, %d, %d", 
            fragmentType != null ? fragmentType.getDisplayName() : "Unknown",
            graceRemaining,
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        
        bossBar.setTitle(title);
        bossBar.setColor(BarColor.RED);
    }
    
    /**
     * Remove boss bar for ritual
     */
    private void removeRitualBossBar(UUID ownerId) {
        BossBar bossBar = ritualBossBars.remove(ownerId);
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
    }
    
    /**
     * Get bar color for fragment type
     */
    private BarColor getFragmentBarColor(FragmentType type) {
        if (type == null) return BarColor.WHITE;
        
        return switch (type) {
            case FIRE -> BarColor.RED;
            case WATER -> BarColor.BLUE;
            case AIR -> BarColor.WHITE;
            case DARK -> BarColor.PURPLE;
            case LIGHT -> BarColor.YELLOW;
            case VOID -> BarColor.PURPLE;
            case DRAGON -> BarColor.RED;
            case STORM -> BarColor.BLUE;
            case TIME -> BarColor.YELLOW;
            case LUCK -> BarColor.GREEN;
            case ADMIN -> BarColor.RED;
        };
    }
    
    /**
     * Send Discord notification when ritual starts
     */
    private void sendDiscordRitualStart(Player player, RitualInstance ritual) {
        if (!discordSRVEnabled) return;
        
        try {
            github.scarsz.discordsrv.DiscordSRV discordSRV = github.scarsz.discordsrv.DiscordSRV.getPlugin();
            if (discordSRV == null) return;
            
            FragmentType fragmentType = determineFragmentType(ritual);
            Location loc = ritual.getLocation();
            long durationMinutes = ritual.getDuration() / 60000;
            
            String message = String.format(
                "⚡ **Ritual Started!**\n" +
                "**Player:** %s\n" +
                "**Type:** %s\n" +
                "**Fragment:** %s\n" +
                "**Duration:** %d minutes\n" +
                "**Location:** %s (%d, %d, %d)",
                player.getName(),
                ritual.getType().getDisplayName(),
                fragmentType != null ? fragmentType.getDisplayName() : "Unknown",
                durationMinutes,
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
            );
            
            // Send to main channel
            discordSRV.getMainTextChannel().sendMessage(message).queue();
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord notification: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        stopUpdateTask();
        
        // Remove all boss bars
        for (BossBar bossBar : ritualBossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        
        // Cleanup all ritual displays
        if (displayManager != null) {
            displayManager.cleanup();
        }
        
        // Cleanup all structures
        if (structureProtection != null) {
            structureProtection.cleanup();
        }
        
        activeRituals.clear();
        ritualBossBars.clear();
        failureCooldowns.clear();
    }
    
    /**
     * Get the structure protection listener for registration
     */
    public RitualStructureProtectionListener getStructureProtectionListener() {
        return structureProtection;
    }
}
