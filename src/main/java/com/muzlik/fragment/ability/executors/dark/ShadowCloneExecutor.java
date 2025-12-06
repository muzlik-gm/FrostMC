package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class ShadowCloneExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 100, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
    }
}
