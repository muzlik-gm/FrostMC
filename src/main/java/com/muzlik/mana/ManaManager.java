package com.muzlik.mana;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages mana for all players with passive regeneration.
 * Handles mana consumption, regeneration, and temporary boosts.
 */
public class ManaManager {
    private final JavaPlugin plugin;
    private final Map<UUID, PlayerManaData> playerManaData;
    private final ManaDisplay manaDisplay;
    private BukkitRunnable regenTask;

    // Configuration constants (Anime RPG Style)
    private static final double BASE_MAX_MANA = 100.0;
    private static final double MANA_PER_RANK = 20.0;
    private static final double MANA_PER_LEVEL = 5.0;
    private static final double BASE_REGEN_RATE = 2.0;
    private static final double REGEN_PER_RANK = 0.5;

    public ManaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerManaData = new ConcurrentHashMap<>();
        this.manaDisplay = new ManaDisplay(plugin);
        startManaRegeneration();
    }

    /**
     * Get player's current mana
     */
    public double getMana(Player player) {
        PlayerManaData data = getOrCreateManaData(player);
        return data.getCurrentMana();
    }

    /**
     * Get player's maximum mana based on rank and level
     */
    public double getMaxMana(Player player) {
        PlayerManaData data = getOrCreateManaData(player);
        return calculateMaxMana(data.getRank(), data.getLevel());
    }

    /**
     * Calculate max mana using the formula: 100 + (Rank * 20) + (Level * 5)
     * @param rank The Fragment rank
     * @param level The player level
     * @return The calculated max mana
     */
    public double calculateMaxMana(int rank, int level) {
        return BASE_MAX_MANA + (rank * MANA_PER_RANK) + (level * MANA_PER_LEVEL);
    }

    /**
     * Set player's mana to a specific value
     */
    public void setMana(Player player, double amount) {
        PlayerManaData data = getOrCreateManaData(player);
        double maxMana = getMaxMana(player);
        double clampedMana = Math.max(0, Math.min(amount, maxMana));
        
        double previousMana = data.getCurrentMana();
        data.setCurrentMana(clampedMana);
        
        // Trigger events
        if (clampedMana == 0 && previousMana > 0) {
            triggerManaEmptyEvent(player);
        } else if (clampedMana == maxMana && previousMana < maxMana) {
            triggerManaFullEvent(player);
        }
        
        manaDisplay.updateDisplay(player, clampedMana, maxMana);
    }

    /**
     * Consume mana from player
     * @return true if mana was consumed, false if insufficient
     */
    public boolean consumeMana(Player player, double cost) {
        if (!hasMana(player, cost)) {
            player.sendMessage("§c✗ Not enough mana: " + String.format("%.1f", getMana(player)) + "/" + String.format("%.1f", cost));
            return false;
        }
        
        double currentMana = getMana(player);
        setMana(player, currentMana - cost);
        return true;
    }

    /**
     * Regenerate mana for player
     */
    public void regenerateMana(Player player, double amount) {
        double currentMana = getMana(player);
        double maxMana = getMaxMana(player);
        setMana(player, Math.min(currentMana + amount, maxMana));
    }

    /**
     * Check if player has sufficient mana
     */
    public boolean hasMana(Player player, double cost) {
        return getMana(player) >= cost;
    }

    /**
     * Get mana regeneration rate based on rank
     * Formula: 2.0 + (Rank * 0.5) mana per second
     */
    public double getManaRegenRate(Player player) {
        PlayerManaData data = getOrCreateManaData(player);
        return calculateManaRegen(data.getRank());
    }

    /**
     * Calculate mana regeneration rate using the formula: 2.0 + (Rank * 0.5)
     * @param rank The Fragment rank
     * @return The calculated mana regen rate per second
     */
    public double calculateManaRegen(int rank) {
        return BASE_REGEN_RATE + (rank * REGEN_PER_RANK);
    }

    /**
     * Apply temporary mana boost
     */
    public void applyManaBoost(Player player, double boost, long durationTicks) {
        PlayerManaData data = getOrCreateManaData(player);
        data.addManaBoost(boost);
        
        // Schedule boost removal
        new BukkitRunnable() {
            @Override
            public void run() {
                data.removeManaBoost(boost);
                double currentMana = data.getCurrentMana();
                double maxMana = getMaxMana(player);
                if (currentMana > maxMana) {
                    setMana(player, maxMana);
                }
                player.sendMessage("§e⚡ Mana boost expired");
            }
        }.runTaskLater(plugin, durationTicks);
        
        player.sendMessage("§a⚡ Mana boost applied: +" + String.format("%.0f", boost) + " max mana");
    }

    /**
     * Update player's rank (affects max mana)
     */
    public void updatePlayerRank(Player player, int rank) {
        PlayerManaData data = getOrCreateManaData(player);
        data.setRank(rank);
        
        // Update display
        manaDisplay.updateDisplay(player, getMana(player), getMaxMana(player));
    }

    /**
     * Update player's level (affects regen rate)
     */
    public void updatePlayerLevel(Player player, int level) {
        PlayerManaData data = getOrCreateManaData(player);
        data.setLevel(level);
    }

    /**
     * Start passive mana regeneration task
     */
    public void startManaRegeneration() {
        if (regenTask != null) {
            regenTask.cancel();
        }
        
        regenTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    double currentMana = getMana(player);
                    double maxMana = getMaxMana(player);
                    
                    if (currentMana < maxMana) {
                        double regenRate = getManaRegenRate(player);
                        regenerateMana(player, regenRate);
                    }
                }
            }
        };
        
        // Run every second (20 ticks)
        regenTask.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Stop mana regeneration task
     */
    public void stopManaRegeneration() {
        if (regenTask != null) {
            regenTask.cancel();
            regenTask = null;
        }
    }



    /**
     * Get or create mana data for player
     * FIXED: Now properly initializes with saved mana value
     */
    private PlayerManaData getOrCreateManaData(Player player) {
        return playerManaData.computeIfAbsent(player.getUniqueId(), 
            uuid -> {
                // Create with base max mana as default
                PlayerManaData data = new PlayerManaData(BASE_MAX_MANA);
                // Current mana will be loaded from persistence by loadPlayerMana()
                return data;
            });
    }
    
    /**
     * Load player mana from persistence
     * Should be called on player join
     */
    public void loadPlayerMana(Player player, double savedMana, int rank, int level) {
        PlayerManaData data = getOrCreateManaData(player);
        data.setRank(rank);
        data.setLevel(level);
        
        // Calculate max mana based on rank and level
        double maxMana = calculateMaxMana(rank, level);
        
        // Set current mana to saved value, clamped to max
        double clampedMana = Math.max(0, Math.min(savedMana, maxMana));
        data.setCurrentMana(clampedMana);
        
        // Update display
        manaDisplay.updateDisplay(player, clampedMana, maxMana);
        
        plugin.getLogger().info("Loaded mana for " + player.getName() + ": " + clampedMana + "/" + maxMana);
    }
    
    /**
     * Get current mana for saving to persistence
     */
    public double getCurrentManaForSave(Player player) {
        PlayerManaData data = playerManaData.get(player.getUniqueId());
        return data != null ? data.getCurrentMana() : BASE_MAX_MANA;
    }

    /**
     * Trigger "On Mana Empty" event
     */
    private void triggerManaEmptyEvent(Player player) {
        player.sendMessage("§c⚡ Mana depleted!");
        // Fragment-specific effects can be added here
    }

    /**
     * Trigger "On Mana Full" event
     */
    private void triggerManaFullEvent(Player player) {
        player.sendMessage("§a⚡ Mana fully restored!");
        // Fragment-specific effects can be added here
    }

    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        playerManaData.remove(player.getUniqueId());
        manaDisplay.removeDisplay(player);
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        stopManaRegeneration();
        playerManaData.clear();
    }
}
