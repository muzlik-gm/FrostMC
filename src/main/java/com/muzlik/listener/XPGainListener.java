package com.muzlik.listener;

import com.muzlik.character.CharacterLevelManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.FrostSMPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles XP gain from kills and ability usage
 * Awards XP to BOTH Fragment Level and Character Level
 */
public class XPGainListener implements Listener {
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final LevelManager levelManager;
    private CharacterLevelManager characterLevelManager;
    
    // XP values
    private static final double XP_PER_MOB_KILL = 10.0;
    private static final double XP_PER_PLAYER_KILL = 50.0;
    private static final double XP_PER_ABILITY_USE = 5.0;
    
    // BOSS XP values
    private static final double XP_WITHER = 500.0;
    private static final double XP_ENDER_DRAGON = 1000.0;
    private static final double XP_WARDEN = 750.0;
    private static final double XP_ELDER_GUARDIAN = 200.0;
    private static final double XP_EVOKER = 100.0;
    private static final double XP_RAVAGER = 80.0;
    
    public XPGainListener(JavaPlugin plugin, FragmentManager fragmentManager, LevelManager levelManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.levelManager = levelManager;
        
        // Get CharacterLevelManager reference from main plugin
        if (plugin instanceof FrostSMPPlugin) {
            this.characterLevelManager = ((FrostSMPPlugin) plugin).getCharacterLevelManager();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        FragmentType activeFragment = fragmentManager.getActiveFragment(killer);
        if (activeFragment == null) return;
        
        double xpGain;
        String mobName = "";
        
        if (event.getEntity() instanceof Player) {
            xpGain = XP_PER_PLAYER_KILL;
            mobName = "Player";
        } else {
            // Check for boss mobs
            switch (event.getEntityType()) {
                case WITHER:
                    xpGain = XP_WITHER;
                    mobName = "§4§lWITHER";
                    break;
                case ENDER_DRAGON:
                    xpGain = XP_ENDER_DRAGON;
                    mobName = "§5§lENDER DRAGON";
                    break;
                case WARDEN:
                    xpGain = XP_WARDEN;
                    mobName = "§8§lWARDEN";
                    break;
                case ELDER_GUARDIAN:
                    xpGain = XP_ELDER_GUARDIAN;
                    mobName = "§b§lELDER GUARDIAN";
                    break;
                case EVOKER:
                    xpGain = XP_EVOKER;
                    mobName = "§6Evoker";
                    break;
                case RAVAGER:
                    xpGain = XP_RAVAGER;
                    mobName = "§cRavager";
                    break;
                default:
                    xpGain = XP_PER_MOB_KILL;
                    mobName = event.getEntityType().name();
            }
        }
        
        // Add XP to Fragment Level
        levelManager.addXP(killer, activeFragment, xpGain);
        
        // Add XP to Character Level (same amount)
        if (characterLevelManager != null) {
            boolean leveledUp = characterLevelManager.awardCharacterXP(killer, xpGain);
            // Level up notification is handled by CharacterLevelManager
        }
        
        // Show XP gain message - special message for bosses
        if (xpGain >= 100) {
            killer.sendMessage("§6§l✦ BOSS KILL! §a+§e" + String.format("%.0f", xpGain) + " XP §7(" + mobName + ")");
            killer.playSound(killer.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        } else {
            killer.sendActionBar("§a+§e" + String.format("%.0f", xpGain) + " XP");
        }
    }
    
    /**
     * Award XP for ability usage (call this from ability executors)
     */
    public void awardAbilityXP(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        if (activeFragment == null) return;
        
        levelManager.addXP(player, activeFragment, XP_PER_ABILITY_USE);
    }
}
