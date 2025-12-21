package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class EternalDarknessExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 15.0;
        int duration = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.INVISIBILITY, duration, 0));
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.INCREASE_DAMAGE, duration, 2));
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.NIGHT_VISION, duration, 0));
        
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 200, 2, 2, 2, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
    }
}
