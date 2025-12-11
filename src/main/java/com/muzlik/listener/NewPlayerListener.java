package com.muzlik.listener;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles new player join events and gives starter fragments
 */
public class NewPlayerListener implements Listener {
    
    private final FrostSMPPlugin plugin;
    private final FragmentManager fragmentManager;
    
    public NewPlayerListener(FrostSMPPlugin plugin, FragmentManager fragmentManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if this is a new player (first time joining)
        if (!player.hasPlayedBefore()) {
            handleNewPlayer(player);
        }
    }
    
    /**
     * Handle new player - give starter fragment if enabled
     */
    private void handleNewPlayer(Player player) {
        // Check if starter fragment is enabled
        boolean giveStarterFragment = plugin.getConfig().getBoolean("new_player.give_starter_fragment", true);
        
        if (!giveStarterFragment) {
            return; // Feature disabled
        }
        
        // Get starter fragment type from config
        String starterFragmentName = plugin.getConfig().getString("new_player.starter_fragment_type", "water").toUpperCase();
        FragmentType starterFragment;
        
        try {
            starterFragment = FragmentType.valueOf(starterFragmentName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid starter fragment type in config: " + starterFragmentName + ". Using WATER instead.");
            starterFragment = FragmentType.WATER;
        }
        
        // Don't give admin fragment to new players
        if (starterFragment == FragmentType.ADMIN) {
            plugin.getLogger().warning("Cannot give ADMIN fragment to new players. Using WATER instead.");
            starterFragment = FragmentType.WATER;
        }
        
        boolean autoActivate = plugin.getConfig().getBoolean("new_player.auto_activate", true);
        boolean sendWelcomeMessage = plugin.getConfig().getBoolean("new_player.welcome_message", true);
        
        // Make variables effectively final for lambda
        final FragmentType finalStarterFragment = starterFragment;
        final boolean finalAutoActivate = autoActivate;
        final boolean finalSendWelcomeMessage = sendWelcomeMessage;
        
        // Delay the fragment giving by 1 second to ensure player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                giveStarterFragment(player, finalStarterFragment, finalAutoActivate, finalSendWelcomeMessage);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }
    
    /**
     * Give starter fragment to new player
     */
    private void giveStarterFragment(Player player, FragmentType fragmentType, boolean autoActivate, boolean sendWelcomeMessage) {
        try {
            if (autoActivate) {
                // Directly give and activate the fragment
                fragmentManager.grantFragment(player, fragmentType);
                fragmentManager.setActiveFragment(player, fragmentType);
                
                plugin.getLogger().info("[New Player] Gave and activated " + fragmentType.getDisplayName() + " fragment to " + player.getName());
                
                if (sendWelcomeMessage) {
                    sendWelcomeMessages(player, fragmentType, true);
                }
            } else {
                // Just charge the fragment (requires Fragment Changer to activate)
                fragmentManager.chargeFragment(player, fragmentType);
                
                plugin.getLogger().info("[New Player] Charged " + fragmentType.getDisplayName() + " fragment for " + player.getName());
                
                if (sendWelcomeMessage) {
                    sendWelcomeMessages(player, fragmentType, false);
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("[New Player] Failed to give starter fragment to " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send welcome messages to new player
     */
    private void sendWelcomeMessages(Player player, FragmentType fragmentType, boolean activated) {
        // Make variables effectively final for lambda
        final FragmentType finalFragmentType = fragmentType;
        final boolean finalActivated = activated;
        
        // Delay messages slightly so they appear after join messages
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("");
                player.sendMessage("§6§l✦ §e§lWELCOME TO FROSTMC! §6§l✦");
                player.sendMessage("");
                
                if (finalActivated) {
                    player.sendMessage("§a✓ You have been granted the §b" + finalFragmentType.getDisplayName() + " Fragment§a!");
                    player.sendMessage("§7Your fragment is now §aACTIVE §7and ready to use!");
                    player.sendMessage("");
                    player.sendMessage("§e⚡ Use your abilities:");
                    player.sendMessage("§7• Scroll wheel to select abilities");
                    player.sendMessage("§7• Right-click to use selected ability");
                    player.sendMessage("§7• Type §e/fragment §7for more info");
                } else {
                    player.sendMessage("§e⚡ You have been granted a §b" + finalFragmentType.getDisplayName() + " Fragment§e!");
                    player.sendMessage("§7Your fragment is §eCHARGED §7but not active yet.");
                    player.sendMessage("§7Use §e/fragment activate " + finalFragmentType.name().toLowerCase() + " §7to activate it!");
                }
                
                player.sendMessage("");
                player.sendMessage("§7Learn more: §e/fragment §7or §e/controls");
                player.sendMessage("§8Type §7/fragment list §8to see all available fragments");
                player.sendMessage("");
            }
        }.runTaskLater(plugin, 40L); // 2 second delay
    }
}