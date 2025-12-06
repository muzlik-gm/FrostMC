package com.muzlik.fragment.ability.executors.light;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class HolySanctuaryExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseRadius = 6.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 150, radius, 2, radius, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
    }
}
