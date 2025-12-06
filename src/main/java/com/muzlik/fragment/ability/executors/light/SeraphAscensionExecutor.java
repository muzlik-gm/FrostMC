package com.muzlik.fragment.ability.executors.light;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SeraphAscensionExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 15.0;
        int duration = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 1));
        
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 300, 2, 2, 2, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 2.0f);
        
        // Schedule flight disable after duration ends
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Only disable flight if not in creative/spectator mode
                    if (player.getGameMode() != GameMode.CREATIVE && 
                        player.getGameMode() != GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                    player.sendMessage("§7Seraph Ascension ended");
                }
            }
        }.runTaskLater(plugin, duration);
    }
}
