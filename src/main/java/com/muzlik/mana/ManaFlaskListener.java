package com.muzlik.mana;

import com.muzlik.recipe.RecipeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles Mana Flask usage and mana restoration.
 */
public class ManaFlaskListener implements Listener {
    private final JavaPlugin plugin;
    private final ManaManager manaManager;
    private final RecipeManager recipeManager;

    public ManaFlaskListener(JavaPlugin plugin, ManaManager manaManager, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.recipeManager = recipeManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) {
            return;
        }
        
        // Check if item is a Mana Flask
        if (recipeManager.isManaFlask(item)) {
            event.setCancelled(true);
            useManaFlask(player, item);
        }
    }

    /**
     * Use a Mana Flask
     */
    private void useManaFlask(Player player, ItemStack item) {
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        
        if (currentMana >= maxMana) {
            player.sendMessage("§e⚡ Your mana is already full");
            return;
        }
        
        // Restore 50 mana instantly
        double restoreAmount = 50.0;
        manaManager.regenerateMana(player, restoreAmount);
        
        // Remove one flask from stack
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§a⚡ Restored §b" + String.format("%.0f", restoreAmount) + " §amana");
    }
}
