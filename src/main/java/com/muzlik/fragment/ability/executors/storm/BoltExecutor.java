package com.muzlik.fragment.ability.executors.storm;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Bolt - Storm Fragment Primary Ability
 * Summons lightning strike on target location, instant cast
 * Base: 7 hearts damage, scales with rank
 */
public class BoltExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Get target location
        Location targetLoc = player.getTargetBlock(null, 25).getLocation();
        targetLoc.setY(targetLoc.getWorld().getHighestBlockYAt(targetLoc) + 1);
        
        // Scale damage
        double baseDamage = 8.0; // 4 hearts
        double damage = baseDamage + (rank * 1.0); // +0.5 hearts per rank
        double radius = 3.0 + (rank * 0.5);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Strike lightning effect (visual only, no vanilla damage)
        targetLoc.getWorld().strikeLightningEffect(targetLoc);
        
        // Damage entities in radius
        for (Entity entity : targetLoc.getWorld().getNearbyEntities(targetLoc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                
                // Lightning impact VFX
                VFXLayerBuilder impactVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .impact(Particle.ELECTRIC_SPARK, 15 + (rank * 3), ParticlePattern.BURST, 0.8, 0.8, 0.8, 0.1, null);
                
                if (rank >= 7) {
                    impactVfx.cinematic(0.2, CinematicEffect.STORM_PULSE);
                }
                
                impactVfx.spawn();
            }
        }
        
        // Lightning strike VFX
        VFXLayerBuilder strikeVfx = new VFXLayerBuilder(plugin, targetLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.ELECTRIC_SPARK, 30 + (rank * 6), ParticlePattern.POINT, 1.0, 3.0, 1.0, 0.15, null)
            .secondary(Particle.FLASH, 20 + (rank * 4), ParticlePattern.BURST, 2.0, 2.0, 2.0, 0.2, null);
        
        strikeVfx.spawn();
        
        // Sound effect
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
    }
}