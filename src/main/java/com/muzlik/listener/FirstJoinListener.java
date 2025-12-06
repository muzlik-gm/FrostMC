package com.muzlik.listener;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Handles first join logic - grants a random Fragment with weighted probabilities.
 * Lower rank Fragments have higher chances.
 */
public class FirstJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final Set<UUID> hasJoinedBefore;
    
    // Weighted probabilities based on Fragment rank
    private static final Map<FragmentType, Integer> FRAGMENT_WEIGHTS = new HashMap<>();
    
    static {
        // Starter Fragments (Base Rank 2-3) - High chance (40% total)
        FRAGMENT_WEIGHTS.put(FragmentType.FIRE, 12);    // 12%
        FRAGMENT_WEIGHTS.put(FragmentType.WATER, 12);   // 12%
        FRAGMENT_WEIGHTS.put(FragmentType.AIR, 10);     // 10%
        FRAGMENT_WEIGHTS.put(FragmentType.EARTH, 10);   // 10%
        
        // Intermediate Fragments (Base Rank 4-5) - Medium chance (35% total)
        FRAGMENT_WEIGHTS.put(FragmentType.DARK, 12);    // 12%
        FRAGMENT_WEIGHTS.put(FragmentType.LIGHT, 12);   // 12%
        FRAGMENT_WEIGHTS.put(FragmentType.MOB, 11);     // 11%
        
        // Advanced Fragments (Base Rank 6-8) - Low chance (25% total)
        FRAGMENT_WEIGHTS.put(FragmentType.STORM, 10);   // 10%
        FRAGMENT_WEIGHTS.put(FragmentType.VOID, 8);     // 8%
        FRAGMENT_WEIGHTS.put(FragmentType.DRAGON, 3);   // 3% (rarest)
    }

    public FirstJoinListener(JavaPlugin plugin, FragmentManager fragmentManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.hasJoinedBefore = new HashSet<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player has joined before
        if (hasJoinedBefore.contains(playerId)) {
            return;
        }
        
        // Check if player already has any Fragments
        if (!fragmentManager.getPlayerFragments(player).isEmpty()) {
            hasJoinedBefore.add(playerId);
            return;
        }
        
        // First time joining - grant random Fragment
        FragmentType randomFragment = getWeightedRandomFragment();
        fragmentManager.grantFragment(player, randomFragment);
        fragmentManager.setActiveFragment(player, randomFragment);
        
        // Welcome message - CLEAN MINIMAL DESIGN
        player.sendMessage("");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("  §6§lWELCOME TO FROSTSMP");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("");
        player.sendMessage("  §7Fragment: §f" + randomFragment.getDisplayName());
        player.sendMessage("  §8" + randomFragment.getDescription());
        player.sendMessage("");
        player.sendMessage("  §7Type §f/fragment list §7to continue");
        player.sendMessage("");
        
        // Mark as joined
        hasJoinedBefore.add(playerId);
    }
    
    /**
     * Get a weighted random Fragment based on probabilities
     */
    private FragmentType getWeightedRandomFragment() {
        int totalWeight = FRAGMENT_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        int random = new Random().nextInt(totalWeight);
        
        int currentWeight = 0;
        for (Map.Entry<FragmentType, Integer> entry : FRAGMENT_WEIGHTS.entrySet()) {
            currentWeight += entry.getValue();
            if (random < currentWeight) {
                return entry.getKey();
            }
        }
        
        // Fallback (should never happen)
        return FragmentType.FIRE;
    }
}
