package com.muzlik.fragment.ability.executors.fire;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Rebirth - Fire Fragment Advanced Passive Ability
 * Upon death, revive with 50% HP in flames, damaging nearby enemies
 * Cooldown: 1 hour
 * 
 * This is a passive ability that triggers on death
 */
public class RebirthExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        // This is a passive ability - the actual logic is handled by a listener
        // When this executor is called, it means the passive is being activated
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Dramatic rebirth VFX
        int coreCount = 25 + (rank * 8);
        int secondaryCount = 15 + (rank * 5);
        int impactCount = 20 + (rank * 6);
        
        VFXLayerBuilder rebirthVFX = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.FLAME, coreCount, ParticlePattern.SPHERE, 2.0, 3.0, 2.0, 0.1, null)
            .secondary(Particle.END_ROD, secondaryCount, ParticlePattern.SPHERE, 1.5, 4.0, 1.5, 0.08, null)
            .impact(Particle.LAVA, impactCount, ParticlePattern.BURST, 2.5, 1.0, 2.5, 0.12, null);
        
        if (rank >= 5) {
            rebirthVFX.cinematic(0.3, CinematicEffect.HEAT_SHIMMER);
        }
        
        rebirthVFX.spawn();
        
        // Heal player to 50% HP
        double maxHealth = player.getMaxHealth();
        player.setHealth(maxHealth * 0.5);
        
        // Damage nearby enemies
        double damageRadius = 4.0 + (rank * 0.5);
        double damage = 4.0 + (rank * 1.0);
        
        for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, damageRadius, damageRadius, damageRadius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                target.setFireTicks(100); // 5 seconds of fire
            }
        }
        
        // Sound effects
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 1.0f);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.8f);
        
        player.sendMessage("§6🔥 §eYou have been reborn from the ashes!");
    }
}