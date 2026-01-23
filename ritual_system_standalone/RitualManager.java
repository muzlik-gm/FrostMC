package your.plugin.ritual;

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

// This is the main ritual controller - handles all the ritual logic
// Took me forever to get the proximity detection working right lol
// 
// Based on our FrostSMP system but way simpler for people to understand
// You'll definitely want to change the completion stuff for your own plugin
public class RitualManager {
    private final JavaPlugin plugin;
    private final Map<UUID, RitualInstance> activeRituals;
    private final Map<UUID, BossBar> ritualBossBars;
    private BukkitRunnable updateTask;
    
    // Config stuff - tweak these if you want
    private double proximityDistance = 5.0; // how close players need to stay
    private long failureCooldown = 60000; // 1 min cooldown after failing (maybe too harsh?)
    private final Map<UUID, Long> failureCooldowns;
    private RitualDisplayManager displayManager;

    public RitualManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeRituals = new ConcurrentHashMap<>();
        this.ritualBossBars = new ConcurrentHashMap<>();
        this.failureCooldowns = new ConcurrentHashMap<>();
        this.displayManager = new RitualDisplayManager(plugin);
        
        startUpdateTask();
    }

    // Start a ritual - this is where the magic happens
    // Returns true if it worked, false if something went wrong
    public boolean startRitual(Player player, RitualType type, ItemStack catalyst, CrystalType crystalType) {
        // Check if player already has active ritual
        if (hasActiveRitual(player)) {
            player.sendMessage("§c✗ You already have an active ritual");
            return false;
        }
        
        // Check failure cooldown - don't want spam
        if (isOnFailureCooldown(player)) {
            long remaining = getFailureCooldownRemaining(player);
            player.sendMessage("§c✗ Ritual cooldown active: " + (remaining / 1000) + " seconds remaining");
            player.sendMessage("§7You must wait before starting another ritual");
            return false;
        }
        
        // Make sure they're in a good spot for rituals
        Location ritualLocation = player.getLocation().clone();
        if (!isValidRitualLocation(player, ritualLocation)) {
            return false; // Error messages sent by validation method
        }
        
        // Check if they actually have the catalyst item
        if (catalyst != null) {
            if (!player.getInventory().containsAtLeast(catalyst, 1)) {
                String itemName = catalyst.getItemMeta() != null && catalyst.getItemMeta().hasDisplayName() 
                    ? catalyst.getItemMeta().getDisplayName() 
                    : catalyst.getType().name();
                player.sendMessage("§c✗ Missing ritual catalyst: " + itemName);
                player.sendMessage("§7You need this item in your inventory to start the ritual");
                return false;
            }
            
            // Take the catalyst from their inventory
            ItemStack toRemove = catalyst.clone();
            toRemove.setAmount(1);
            player.getInventory().removeItem(toRemove);
            player.sendMessage("§7Ritual catalyst consumed...");
        }
        
        // Snap to block center - looks better
        ritualLocation.setX(ritualLocation.getBlockX() + 0.5);
        ritualLocation.setY(ritualLocation.getBlockY());
        ritualLocation.setZ(ritualLocation.getBlockZ() + 0.5);
        
        // Create the ritual instance
        RitualInstance ritual = new RitualInstance(
            player.getUniqueId(),
            type,
            ritualLocation,
            type.getDefaultDuration() * 1000L, // Convert to milliseconds
            catalyst,
            crystalType
        );
        
        activeRituals.put(player.getUniqueId(), ritual);
        
        // Create boss bar for progress tracking
        createRitualBossBar(player, ritual);
        
        // Spawn the visual display (crystals, beams, particles)
        if (crystalType != null) {
            ItemStack crystalItem = createCrystalItem(crystalType);
            if (crystalItem != null) {
                displayManager.createDisplay(player, ritual.getLocation(), crystalType, crystalItem, ritual.getDuration());
            }
        }
        
        // Tell the player what's happening
        player.sendMessage("§a✓ Ritual started: §b" + type.getDisplayName());
        player.sendMessage("§7Stay within §e" + proximityDistance + " blocks §7of the ritual location");
        player.sendMessage("§7Duration: §e" + (type.getDefaultDuration() / 60) + " minutes");
        player.sendMessage("§8Leaving the area will start a grace period before failure");
        
        return true;
    }
    
    /**
     * Make sure they're doing the ritual in a good spot
     */
    private boolean isValidRitualLocation(Player player, Location location) {
        // Only allow rituals in the overworld - nether/end would be weird
        if (location.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL) {
            player.sendMessage("§c✗ Rituals can only be performed in the Overworld!");
            player.sendMessage("§7You cannot perform rituals in the Nether or End.");
            return false;
        }
        
        // Make sure they're not underground - rituals need sky access
        org.bukkit.block.Block highestBlock = location.getWorld().getHighestBlockAt(location);
        int surfaceY = highestBlock.getY();
        int playerY = location.getBlockY();
        
        // Give them some leeway - 5 blocks below surface is fine
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
    private void updateRitual(UUID ritualOwnerId, RitualInstance ritual) {
        Player owner = plugin.getServer().getPlayer(ritualOwnerId);
        
        // Check if ANY player is in the ritual area
        boolean anyPlayerInArea = isAnyPlayerInRitualArea(ritual);
        
        // Handle grace period logic
        if (!anyPlayerInArea) {
            if (!ritual.isInGracePeriod()) {
                // Start grace period - no players in area
                ritual.startGracePeriod();
                
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
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage("§a✓ Ritual resumed! §7You returned to the ritual area.");
                }
            }
        }
        
        // Update progress
        long elapsed = System.currentTimeMillis() - ritual.getStartTime();
        int progressPercent = (int) ((elapsed * 100) / ritual.getDuration());
        
        // Check for milestone notifications (25%, 50%, 75%, 90%)
        int previousPercent = ritual.getPreviousProgressPercent();
        checkProgressMilestones(owner, ritual, previousPercent, progressPercent);
        ritual.setPreviousProgressPercent(progressPercent);
        
        // Update boss bar
        updateRitualBossBar(ritualOwnerId, ritual);
        
        // Update stage
        RitualStage newStage = RitualStage.fromProgress(progressPercent);
        if (newStage != ritual.getStage()) {
            ritual.setStage(newStage);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage("§e⚡ Ritual stage: §b" + newStage.name());
            }
        }
        
        // Check completion
        if (elapsed >= ritual.getDuration()) {
            completeRitual(ritualOwnerId, ritual);
        }
    }
    
    /**
     * Check and send progress milestone notifications
     */
    private void checkProgressMilestones(Player owner, RitualInstance ritual, int previousPercent, int currentPercent) {
        int[] milestones = {25, 50, 75, 90};
        
        for (int milestone : milestones) {
            if (previousPercent < milestone && currentPercent >= milestone) {
                // Send notification to owner
                if (owner != null && owner.isOnline()) {
                    String message = switch (milestone) {
                        case 25 -> "§e⚡ Ritual Progress: §b25% §7- Quarter complete";
                        case 50 -> "§e⚡ Ritual Progress: §b50% §7- Halfway there!";
                        case 75 -> "§e⚡ Ritual Progress: §b75% §7- Almost done!";
                        case 90 -> "§e⚡ Ritual Progress: §b90% §7- Final stage!";
                        default -> "";
                    };
                    owner.sendMessage(message);
                    
                    // Play notification sound
                    owner.playSound(owner.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                }
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
     * Complete a ritual
     */
    private void completeRitual(UUID ownerId, RitualInstance ritual) {
        activeRituals.remove(ownerId);
        removeRitualBossBar(ownerId);
        
        Player owner = plugin.getServer().getPlayer(ownerId);
        RitualType type = ritual.getType();
        CrystalType crystalType = ritual.getCrystalType();
        
        // CUSTOMIZE THIS SECTION FOR YOUR PLUGIN
        // This is where you add your completion logic
        switch (type) {
            case CRYSTAL_CREATION:
                completeCrystalCreation(owner, crystalType);
                break;
            case CRYSTAL_UPGRADE:
                completeCrystalUpgrade(owner, crystalType);
                break;
            case POWER_INFUSION:
                completePowerInfusion(owner, crystalType);
                break;
            case ENCHANTMENT_RITUAL:
                completeEnchantmentRitual(owner, crystalType);
                break;
            default:
                if (owner != null) owner.sendMessage("§a✓ Ritual complete!");
                break;
        }
        
        // Remove display
        displayManager.removeDisplay(ownerId);
        
        // Broadcast completion
        if (owner != null && crystalType != null) {
            Location loc = ritual.getLocation();
            String ownerName = owner.getName();
            Bukkit.broadcastMessage(String.format(
                "§a§l✓ RITUAL COMPLETE! §r§7%s has completed a §b%s §7at §f%d, %d, %d",
                ownerName,
                type.getDisplayName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
            ));
        }
    }

    /**
     * CUSTOMIZE THESE METHODS FOR YOUR PLUGIN
     * These are example completion handlers - replace with your own logic
     */
    private void completeCrystalCreation(Player player, CrystalType crystalType) {
        if (player != null) {
            player.sendMessage("§a✓ Crystal Creation complete!");
            player.sendMessage("§e⚡ You have created a " + crystalType.getDisplayName() + "!");
            
            // Give the player the crystal item (customize this)
            ItemStack crystal = createCrystalItem(crystalType);
            player.getInventory().addItem(crystal);
        }
    }
    
    private void completeCrystalUpgrade(Player player, CrystalType crystalType) {
        if (player != null) {
            player.sendMessage("§a✓ Crystal Upgrade complete!");
            player.sendMessage("§e⚡ Your " + crystalType.getDisplayName() + " has been upgraded!");
        }
    }
    
    private void completePowerInfusion(Player player, CrystalType crystalType) {
        if (player != null) {
            player.sendMessage("§a✓ Power Infusion complete!");
            player.sendMessage("§e⚡ You have been infused with " + crystalType.getDisplayName() + " power!");
        }
    }
    
    private void completeEnchantmentRitual(Player player, CrystalType crystalType) {
        if (player != null) {
            player.sendMessage("§a✓ Enchantment Ritual complete!");
            player.sendMessage("§e⚡ Your items have been enchanted with " + crystalType.getDisplayName() + " magic!");
        }
    }

    /**
     * Create a crystal item (customize this for your plugin)
     */
    private ItemStack createCrystalItem(CrystalType crystalType) {
        ItemStack item = new ItemStack(org.bukkit.Material.NETHER_STAR);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§l" + crystalType.getDisplayName());
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7" + crystalType.getDescription());
            lore.add("§8Created through ritual magic");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Fail a ritual
     */
    private void failRitual(UUID ownerId, RitualInstance ritual, String reason) {
        activeRituals.remove(ownerId);
        removeRitualBossBar(ownerId);
        displayManager.removeDisplay(ownerId);
        
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null) {
            owner.sendMessage("§c✗ Ritual failed: " + reason);
        }
        
        // Apply failure cooldown
        failureCooldowns.put(ownerId, System.currentTimeMillis() + failureCooldown);
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
                // Update all active rituals
                for (Map.Entry<UUID, RitualInstance> entry : activeRituals.entrySet()) {
                    UUID ownerId = entry.getKey();
                    RitualInstance ritual = entry.getValue();
                    updateRitual(ownerId, ritual);
                }
            }
        };
        
        // Run once per second
        updateTask.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Create boss bar for ritual
     */
    private void createRitualBossBar(Player player, RitualInstance ritual) {
        CrystalType crystalType = ritual.getCrystalType();
        BarColor color = getCrystalBarColor(crystalType);
        Location loc = ritual.getLocation();
        
        String title = String.format("§e⚡ %s §8- §b%s §8| §7Location: §f%d, %d, %d", 
            ritual.getType().getDisplayName(),
            crystalType != null ? crystalType.getDisplayName() : "Unknown",
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        
        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SEGMENTED_10);
        bossBar.setProgress(0.0);
        
        // Add ALL online players to the boss bar
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(onlinePlayer);
        }
        
        bossBar.setVisible(true);
        
        ritualBossBars.put(player.getUniqueId(), bossBar);
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
        
        // Update title with time remaining
        long remainingSeconds = ritual.getRemainingSeconds();
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        Location loc = ritual.getLocation();
        
        CrystalType crystalType = ritual.getCrystalType();
        String title = String.format("§e⚡ %s §8- §b%s §8| §7Time: §f%d:%02d §8| §7Loc: §f%d, %d, %d", 
            ritual.getType().getDisplayName(),
            crystalType != null ? crystalType.getDisplayName() : "Unknown",
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
        
        CrystalType crystalType = ritual.getCrystalType();
        String title = String.format("§c⚠ GRACE PERIOD §8- §b%s §8| §cTime: §f%ds §8| §7Loc: §f%d, %d, %d", 
            crystalType != null ? crystalType.getDisplayName() : "Unknown",
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
     * Get bar color for crystal type
     */
    private BarColor getCrystalBarColor(CrystalType type) {
        if (type == null) return BarColor.WHITE;
        
        return switch (type) {
            case FIRE -> BarColor.RED;
            case WATER -> BarColor.BLUE;
            case AIR -> BarColor.WHITE;
            case EARTH -> BarColor.GREEN;
            case DARK -> BarColor.PURPLE;
            case LIGHT -> BarColor.YELLOW;
            case VOID -> BarColor.PURPLE;
            case STORM -> BarColor.BLUE;
            case TIME -> BarColor.YELLOW;
            case LUCK -> BarColor.GREEN;
        };
    }
    
    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Remove all boss bars
        for (BossBar bossBar : ritualBossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        
        // Cleanup all ritual displays
        if (displayManager != null) {
            displayManager.cleanup();
        }
        
        activeRituals.clear();
        ritualBossBars.clear();
        failureCooldowns.clear();
    }
}