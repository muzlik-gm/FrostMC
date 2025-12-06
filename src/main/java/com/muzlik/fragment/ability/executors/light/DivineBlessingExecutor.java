package com.muzlik.fragment.ability.executors.light;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DivineBlessingExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseHealing = 10.0;
        double healing = context.getScalingEngine().scaleHealing(baseHealing, rank);
        
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healing));
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            // Remove harmful effects (wither, poison, slowness, weakness, etc.)
            if (type.equals(PotionEffectType.WITHER) || type.equals(PotionEffectType.POISON) ||
                type.equals(PotionEffectType.SLOW) || type.equals(PotionEffectType.WEAKNESS) ||
                type.equals(PotionEffectType.BLINDNESS) || type.equals(PotionEffectType.HUNGER)) {
                player.removePotionEffect(type);
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 1));
        
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 80, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        player.sendMessage("§e✨ Divine Blessing!");
    }
}
