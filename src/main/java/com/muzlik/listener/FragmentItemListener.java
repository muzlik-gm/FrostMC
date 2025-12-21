package com.muzlik.listener;

import com.muzlik.config.ConfigManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.ritual.RitualManager;
import com.muzlik.util.FragmentUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class FragmentItemListener implements Listener {

    private final RitualManager ritualManager;
    private final ConfigManager configManager;
    private final FragmentManager fragmentManager;

    public FragmentItemListener(RitualManager ritualManager, ConfigManager configManager, FragmentManager fragmentManager) {
        this.ritualManager = ritualManager;
        this.configManager = configManager;
        this.fragmentManager = fragmentManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (item == null) {
            return;
        }

        // Check if it's a Fragment Activator (dropped from ritual)
        FragmentType activatorType = getFragmentActivatorType(item);
        if (activatorType != null) {
            event.setCancelled(true);
            activateFragment(player, item, activatorType);
            return;
        }

        // Note: Ritual starting is handled by RitualListener
        // This listener only handles fragment activation (picking up completed ritual fragments)
    }
    
    /**
     * Get FragmentType from a Fragment Activator item
     */
    private FragmentType getFragmentActivatorType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return null;
        }
        
        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }
        
        // Check if it's a Fragment Activator
        boolean isActivator = false;
        String fragmentTypeName = null;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.equals("FRAGMENT_ACTIVATOR")) {
                isActivator = true;
            }
            if (stripped.startsWith("Fragment Type: ")) {
                fragmentTypeName = stripped.replace("Fragment Type: ", "").trim();
            }
        }
        
        if (!isActivator || fragmentTypeName == null) {
            return null;
        }
        
        // Parse the fragment type
        try {
            return FragmentType.valueOf(fragmentTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Activate a fragment for the player
     */
    private void activateFragment(Player player, ItemStack item, FragmentType fragmentType) {
        // Check if player already has ANY fragment active
        FragmentType currentActive = fragmentManager.getActiveFragment(player);
        if (currentActive != null) {
            if (currentActive == fragmentType) {
                player.sendMessage("§e⚠ You already have the " + fragmentType.getDisplayName() + " Fragment active!");
            } else {
                player.sendMessage("§c✗ You already have the " + currentActive.getDisplayName() + " Fragment active!");
                player.sendMessage("§7Use §e/fragment withdraw §7to deactivate it first.");
            }
            return;
        }
        
        // Activate the fragment
        fragmentManager.grantAndActivateFragment(player, fragmentType);
        
        // Remove the item from player's hand
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Send success message
        player.sendMessage("");
        player.sendMessage("§a§l✓ FRAGMENT ACTIVATED!");
        player.sendMessage("§7You have activated the §5" + fragmentType.getDisplayName() + " §7Fragment!");
        player.sendMessage("§7Use your abilities with right-click while holding a weapon.");
        player.sendMessage("");
        
        // Play activation effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        
        // Spawn particles
        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.3);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 100, 0.5, 1, 0.5, 0.5);
    }
}
