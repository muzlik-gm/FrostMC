package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.cinematic.ability.FireAbilityVFX;
import org.bukkit.entity.Player;

/**
 * Phoenix Rebirth - Fire Fragment Ultimate Ability
 * Grants resurrection upon death
 * 
 * VFX: Cinematic phoenix silhouette with vertical magic circle
 */
public class PhoenixRebirthExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Use cinematic VFX system
        FireAbilityVFX fireVFX = new FireAbilityVFX(plugin);
        
        // Phoenix Rebirth cinematic VFX: phoenix rising with vertical magic circle
        fireVFX.phoenixRebirth(player, player.getLocation(), rank);
    }
}
