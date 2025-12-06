package com.muzlik.character;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Character Level - Global player level that affects max mana capacity.
 * 
 * Character Level is separate from Fragment Level:
 * - Fragment Level: Affects abilities, rank upgrades, has lower cap
 * - Character Level: Affects max mana capacity, has higher cap
 * 
 * Both gain XP simultaneously from the same sources.
 */
public class CharacterLevelManager {
    private final JavaPlugin plugin;
    private final Map<UUID, CharacterData> characterData;
    
    // Configurable values (loaded from config)
    private int maxCharacterLevel = 100;
    private double baseManaPerLevel = 10.0;  // Mana gained per level
    private double baseCharacterMana = 100.0; // Base mana at level 1
    private double baseXpRequired = 150.0;    // Base XP for level 2
    private double xpScalingFactor = 1.15;    // XP requirement scaling
    
    public CharacterLevelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.characterData = new ConcurrentHashMap<>();
        loadConfig();
    }
    
    /**
     * Load configuration values
     */
    private void loadConfig() {
        this.maxCharacterLevel = plugin.getConfig().getInt("character.max_level", 100);
        this.baseManaPerLevel = plugin.getConfig().getDouble("character.mana_per_level", 10.0);
        this.baseCharacterMana = plugin.getConfig().getDouble("character.base_mana", 100.0);
        this.baseXpRequired = plugin.getConfig().getDouble("character.base_xp_required", 150.0);
        this.xpScalingFactor = plugin.getConfig().getDouble("character.xp_scaling", 1.15);
    }
    
    /**
     * Get max character level (configurable)
     */
    public int getMaxCharacterLevel() {
        return maxCharacterLevel;
    }
    
    /**
     * Get player's character level
     */
    public int getCharacterLevel(Player player) {
        CharacterData data = getOrCreateData(player);
        return data.getLevel();
    }
    
    /**
     * Get player's character XP
     */
    public double getCharacterXP(Player player) {
        CharacterData data = getOrCreateData(player);
        return data.getXp();
    }
    
    /**
     * Check if player is at max character level
     */
    public boolean isMaxLevel(Player player) {
        return getCharacterLevel(player) >= maxCharacterLevel;
    }
    
    /**
     * Calculate max mana for a character level
     * Formula: baseMana + (level * manaPerLevel)
     */
    public double calculateMaxManaForLevel(int level) {
        return baseCharacterMana + (level * baseManaPerLevel);
    }
    
    /**
     * Get max mana for player based on their character level
     */
    public double getMaxMana(Player player) {
        int level = getCharacterLevel(player);
        return calculateMaxManaForLevel(level);
    }
    
    /**
     * Calculate XP required for a specific level
     * Formula: baseXP * (scalingFactor ^ (level - 1))
     */
    public double getXPForLevel(int level) {
        if (level <= 1) return 0;
        return baseXpRequired * Math.pow(xpScalingFactor, level - 2);
    }
    
    /**
     * Get XP required for next level
     */
    public double getXPForNextLevel(Player player) {
        int currentLevel = getCharacterLevel(player);
        if (currentLevel >= maxCharacterLevel) {
            return 0; // Max level
        }
        return getXPForLevel(currentLevel + 1);
    }
    
    /**
     * Award character XP (called alongside fragment XP)
     * @return true if player leveled up
     */
    public boolean awardCharacterXP(Player player, double amount) {
        if (isMaxLevel(player)) {
            return false; // No XP at max level
        }
        
        CharacterData data = getOrCreateData(player);
        double newXP = data.getXp() + amount;
        data.setXp(newXP);
        
        // Check for level up
        return checkLevelUp(player, data);
    }
    
    /**
     * Check and process level up
     */
    private boolean checkLevelUp(Player player, CharacterData data) {
        if (data.getLevel() >= maxCharacterLevel) {
            return false; // Already max
        }
        
        double xpRequired = getXPForLevel(data.getLevel() + 1);
        boolean leveledUp = false;
        
        while (data.getXp() >= xpRequired && data.getLevel() < maxCharacterLevel) {
            // Level up!
            data.setXp(data.getXp() - xpRequired);
            data.setLevel(data.getLevel() + 1);
            leveledUp = true;
            
            // Notification
            triggerLevelUpNotification(player, data.getLevel());
            
            // Calculate next level requirement
            if (data.getLevel() < maxCharacterLevel) {
                xpRequired = getXPForLevel(data.getLevel() + 1);
            } else {
                break;
            }
        }
        
        return leveledUp;
    }
    
    /**
     * Trigger level-up notification with nice formatting
     */
    private void triggerLevelUpNotification(Player player, int newLevel) {
        player.sendMessage("");
        player.sendMessage("§6§l  ⬆ §e§lCHARACTER LEVEL UP! §6§l⬆");
        player.sendMessage("");
        player.sendMessage("§f  ✦ You are now §bLevel " + newLevel + " §f✦");
        
        // Calculate and show new max mana
        double newMaxMana = calculateMaxManaForLevel(newLevel);
        player.sendMessage("§f  ⚡ Max Mana: §b" + String.format("%.0f", newMaxMana));
        
        if (newLevel >= maxCharacterLevel) {
            player.sendMessage("");
            player.sendMessage("§d  ★ §5§lMAX LEVEL REACHED! §d★");
        }
        player.sendMessage("");
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f);
    }
    
    /**
     * Set character level directly (for loading)
     */
    public void setCharacterLevel(Player player, int level) {
        CharacterData data = getOrCreateData(player);
        data.setLevel(Math.min(level, maxCharacterLevel));
    }
    
    /**
     * Set character XP directly (for loading)
     */
    public void setCharacterXP(Player player, double xp) {
        CharacterData data = getOrCreateData(player);
        data.setXp(Math.max(0, xp));
    }
    
    /**
     * Load character data from persistence
     */
    public void loadCharacterData(Player player, int level, double xp) {
        CharacterData data = getOrCreateData(player);
        data.setLevel(Math.max(1, Math.min(level, maxCharacterLevel)));
        data.setXp(Math.max(0, xp));
        
        plugin.getLogger().info("Loaded character data for " + player.getName() + 
            ": Level " + data.getLevel() + ", XP " + String.format("%.1f", data.getXp()));
    }
    
    /**
     * Get character level for saving
     */
    public int getCharacterLevelForSave(Player player) {
        CharacterData data = characterData.get(player.getUniqueId());
        return data != null ? data.getLevel() : 1;
    }
    
    /**
     * Get character XP for saving
     */
    public double getCharacterXPForSave(Player player) {
        CharacterData data = characterData.get(player.getUniqueId());
        return data != null ? data.getXp() : 0;
    }
    
    /**
     * Get or create character data
     */
    private CharacterData getOrCreateData(Player player) {
        return characterData.computeIfAbsent(player.getUniqueId(), 
            uuid -> new CharacterData());
    }
    
    /**
     * Remove player data
     */
    public void removePlayer(Player player) {
        characterData.remove(player.getUniqueId());
    }
    
    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        characterData.clear();
    }
    
    /**
     * Internal data class for character progression
     */
    private static class CharacterData {
        private int level = 1;
        private double xp = 0;
        
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public double getXp() { return xp; }
        public void setXp(double xp) { this.xp = xp; }
    }
}
