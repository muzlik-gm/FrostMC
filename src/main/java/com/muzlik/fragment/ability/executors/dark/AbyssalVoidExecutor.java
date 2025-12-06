package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbyssalVoidExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseRadius = 8.0;
        double radius = context.getScalingEngine().scaleRange(baseRadius, rank);
        
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, 5, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
            }
        }
        
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 200, radius, 2, radius, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.5f);
        player.sendMessage("§5⚫ Abyssal Void!");
    }
}
