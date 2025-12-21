package com.muzlik.listener;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
        FRAGMENT_WEIGHTS.put(FragmentType.FIRE, 15);    // 15%
        FRAGMENT_WEIGHTS.put(FragmentType.WATER, 15);   // 15%
        FRAGMENT_WEIGHTS.put(FragmentType.AIR, 14);     // 14%
        
        // Intermediate Fragments (Base Rank 4-5) - Medium chance (36% total)
        FRAGMENT_WEIGHTS.put(FragmentType.DARK, 18);    // 18%
        FRAGMENT_WEIGHTS.put(FragmentType.LIGHT, 18);   // 18%
        
        // Advanced Fragments (Base Rank 6-8) - Low chance (20% total)
        FRAGMENT_WEIGHTS.put(FragmentType.STORM, 10);   // 10%
        FRAGMENT_WEIGHTS.put(FragmentType.VOID, 7);     // 7%
        FRAGMENT_WEIGHTS.put(FragmentType.DRAGON, 3);   // 3% (rarest)
    }

    public FirstJoinListener(JavaPlugin plugin, FragmentManager fragmentManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.hasJoinedBefore = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.LOWEST) // Run AFTER data loading
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player has joined before
        if (hasJoinedBefore.contains(playerId)) {
            return;
        }
        
        // Check if player already has any Fragments (loaded from disk)
        if (!fragmentManager.getPlayerFragments(player).isEmpty()) {
            hasJoinedBefore.add(playerId);
            return;
        }
        
        // Check if player has played before (Bukkit API)
        if (player.hasPlayedBefore()) {
            hasJoinedBefore.add(playerId);
            return;
        }
        
        // First time joining - grant random Fragment
        FragmentType randomFragment = getWeightedRandomFragment();
        fragmentManager.grantFragment(player, randomFragment);
        fragmentManager.setActiveFragment(player, randomFragment);
        
        // Welcome message - MINIMAL
        player.sendMessage("");
        player.sendMessage(
            com.muzlik.util.Typography.COLOR_ACCENT + "★ " +
            com.muzlik.util.Typography.COLOR_HIGHLIGHT + com.muzlik.util.Typography.toSmallCaps("welcome to frostsmp")
        );
        player.sendMessage(
            com.muzlik.util.Typography.COLOR_TEXT_DARK + com.muzlik.util.Typography.toSmallCaps("fragment") + ": " +
            com.muzlik.util.Typography.COLOR_SECONDARY + randomFragment.getDisplayName()
        );
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
