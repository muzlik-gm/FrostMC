package com.muzlik.fragment.ability.executors.storm;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.DamageAttributionManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Lightning Bolt - Storm Fragment Primary Ability (Slot 0)
 * Targeted lightning strike with multi-layer VFX
 * Damage scales with rank
 */
public class LightningBoltExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        // FIXED: Prioritize entity targeting over blocks
        Location target = com.muzlik.fragment.ability.TargetingUtil.getGroundTargetLocation(player, 50);
        World world = target.getWorld();
        
        // Scale damage with rank
        double baseDamage = 5.0; // EXTREME NERF: 12.0 → 5.0 (2.5 hearts, primary instant strike)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        double radius = 3.0 + (rank * 0.3);
        
        // Strike lightning effect - multiple strikes at higher ranks
        world.strikeLightningEffect(target);
        if (rank >= 4) {
            world.strikeLightningEffect(target.clone().add(1, 0, 0));
            world.strikeLightningEffect(target.clone().add(-1, 0, 0));
        }
        
        // Multi-layer VFX - MORE CHAOTIC AT HIGHER RANKS
        // Layer 1: Electric core - more sparks at higher ranks
        int electricCount = 60 + (rank * 20); // 60 → 180 at rank 6
        world.spawnParticle(Particle.ELECTRIC_SPARK, target.clone().add(0, 1, 0), electricCount, 0.5 + (rank * 0.15), 2.0 + (rank * 0.3), 0.5 + (rank * 0.15), 0.2 + (rank * 0.05));
        
        // Layer 2: Soul fire flame (blue lightning glow) - more intense at higher ranks
        int soulFireCount = 40 + (rank * 15); // 40 → 130 at rank 6
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, target.clone().add(0, 1, 0), soulFireCount, 0.4 + (rank * 0.12), 1.5 + (rank * 0.25), 0.4 + (rank * 0.12), 0.1 + (rank * 0.03));
        
        // Layer 3: End rod (white flash) - more explosive at higher ranks
        int endRodCount = 30 + (rank * 12); // 30 → 102 at rank 6
        world.spawnParticle(Particle.END_ROD, target.clone().add(0, 2, 0), endRodCount, 0.3 + (rank * 0.1), 1.0 + (rank * 0.2), 0.3 + (rank * 0.1), 0.15 + (rank * 0.04));
        
        // Layer 4: Crit particles (impact burst) - more chaotic at higher ranks
        int critCount = 50 + (rank * 18); // 50 → 158 at rank 6
        world.spawnParticle(Particle.CRIT, target, critCount, 1.0 + (rank * 0.2), 0.2 + (rank * 0.1), 1.0 + (rank * 0.2), 0.3 + (rank * 0.08));
        
        // Layer 5: Cloud particles (atmospheric effect) - more clouds at higher ranks
        int cloudCount = 25 + (rank * 10); // 25 → 85 at rank 6
        world.spawnParticle(Particle.CLOUD, target.clone().add(0, 0.5, 0), cloudCount, 0.8 + (rank * 0.15), 0.3 + (rank * 0.1), 0.8 + (rank * 0.15), 0.05 + (rank * 0.02));
        
        // Sound layers - louder and more intense at higher ranks
        float volume = 1.2f + (rank * 0.15f);
        world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, volume, 1.0f - (rank * 0.05f));
        world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, volume * 0.7f, 1.2f + (rank * 0.05f));
        world.playSound(target, Sound.ENTITY_GENERIC_EXPLODE, volume * 0.4f, 1.5f + (rank * 0.08f));
        
        // Damage nearby entities
        for (Entity entity : world.getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                double distance = living.getLocation().distance(target);
                double damageMultiplier = 1.0 - (distance / radius) * 0.5; // Falloff
                
                living.damage(damage * damageMultiplier, player);
                
                // Additional VFX on hit entities
                living.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, living.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
            }
        }
        
        player.sendMessage("§b⚡ Lightning Bolt! §7(" + String.format("%.1f", damage) + " damage)");
    }
}
