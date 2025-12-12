package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Drain - Dark Fragment Secondary Ability
 * Drains health from target and heals caster
 */
public class DrainExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseRange = 12.0;
        double range = context.getScalingEngine().scaleRange(baseRange, rank);
        double baseDamage = 5.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Raycast to find target
        RayTraceResult result = player.getWorld().rayTraceEntities(
            player.getEyeLocation(), 
            player.getLocation().getDirection(), 
            range,
            entity -> entity instanceof LivingEntity && entity != player
        );
        
        if (result == null || !(result.getHitEntity() instanceof LivingEntity)) {
            player.sendMessage("§c✗ No target found");
            return;
        }
        
        LivingEntity target = (LivingEntity) result.getHitEntity();
        Location targetLoc = target.getLocation();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Damage target
        target.damage(damage, player);
        
        // Heal player (70% of damage dealt)
        double healing = damage * 0.7;
        double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + healing);
        player.setHealth(newHealth);
        
        // VFX - Dark energy transfer
        VFXLayerBuilder drainVFX = new VFXLayerBuilder(plugin, targetLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.SMOKE_LARGE, 30, ParticlePattern.SPIRAL, 1, 2, 1, 0.05, null)
            .secondary(Particle.SQUID_INK, 20, ParticlePattern.RING, 0.8, 1, 0.8, 0.03, null)
            .ambient(Particle.SMOKE_NORMAL, 15, ParticlePattern.SPHERE, 1.2, 1.5, 1.2, 0.02, null)
            .impact(Particle.REDSTONE, 25, ParticlePattern.BURST, 0.5, 0.5, 0.5, 0.1, new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.0f));
        
        if (rank >= 4) {
            drainVFX.cinematic(0.2, CinematicEffect.DARKNESS_PULSE);
        }
        
        drainVFX.spawn();
        
        // Healing VFX on player
        new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.HEART, 8, ParticlePattern.SPHERE, 1, 1, 1, 0.02, null)
            .secondary(Particle.SMOKE_LARGE, 12, ParticlePattern.RING, 0.8, 0.5, 0.8, 0.03, null)
            .spawn();
        
        // Sound
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_WITHER_HURT, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        
        player.sendMessage("§5⚡ §dDrained " + String.format("%.1f", damage) + " health, healed " + String.format("%.1f", healing));
    }
}