package com.muzlik.listener;

import com.muzlik.config.ConfigManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.ritual.RitualManager;
import com.muzlik.util.FragmentUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FragmentItemListener implements Listener {

    private final RitualManager ritualManager;
    private final ConfigManager configManager;

    public FragmentItemListener(RitualManager ritualManager, ConfigManager configManager) {
        this.ritualManager = ritualManager;
        this.configManager = configManager;
    }

    @EventHandler
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

        FragmentType fragmentType = FragmentUtil.getFragmentTypeFromItem(item);
        if (fragmentType == null) {
            return;
        }

        event.setCancelled(true);

        boolean changerRequired = configManager.isFragmentChangerRequired();

        if (changerRequired) {
            player.sendMessage("§eThis Fragment is dormant. Use a §bFragment Changer §eto awaken its power.");
            return;
        }

        ritualManager.startRitual(player, com.muzlik.ritual.RitualType.FRAGMENT_CREATION, item, fragmentType);
    }
}
