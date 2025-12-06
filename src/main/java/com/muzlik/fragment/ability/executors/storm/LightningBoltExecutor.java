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
        
        // Strike lightning effect - single strike at low ranks, additional at rank 4+
        world.strikeLightningEffect(target);
        if (rank >= 4) {
            world.strikeLightningEffect(target.clone().add(1, 0, 0));
            world.strikeLightningEffect(target.clone().add(-1, 0, 0));
        }
        
        // FOCUSED Lightning VFX - clean electric strike, not particle spam
        // Reduced counts for primary ability + VFXLayerBuilder would further reduce
        // Layer 1: Electric core - focused sparks
        int electricCount = 15 + (rank * 5);   // 15 → 45 at rank 6 (was 60 → 180)
        world.spawnParticle(Particle.ELECTRIC_SPARK, target.clone().add(0, 1, 0), electricCount, 0.3 + (rank * 0.08), 1.2 + (rank * 0.15), 0.3 + (rank * 0.08), 0.1 + (rank * 0.02));
        
        // Layer 2: Soul fire flame (blue glow) - subtle accent
        int soulFireCount = 10 + (rank * 4);   // 10 → 34 at rank 6 (was 40 → 130)
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, target.clone().add(0, 1, 0), soulFireCount, 0.25 + (rank * 0.06), 0.9 + (rank * 0.12), 0.25 + (rank * 0.06), 0.05 + (rank * 0.01));
        
        // Layer 3: End rod (white flash) - minimal highlight
        int endRodCount = 8 + (rank * 3);      // 8 → 26 at rank 6 (was 30 → 102)
        world.spawnParticle(Particle.END_ROD, target.clone().add(0, 2, 0), endRodCount, 0.2 + (rank * 0.05), 0.6 + (rank * 0.1), 0.2 + (rank * 0.05), 0.08 + (rank * 0.02));
        
        // Layer 4: Crit particles - focused impact burst
        int critCount = 12 + (rank * 5);       // 12 → 42 at rank 6 (was 50 → 158)
        world.spawnParticle(Particle.CRIT, target, critCount, 0.6 + (rank * 0.1), 0.15 + (rank * 0.05), 0.6 + (rank * 0.1), 0.15 + (rank * 0.04));
        
        // Layer 5: Cloud particles - subtle atmosphere (only at higher ranks)
        if (rank >= 3) {
            int cloudCount = 6 + (rank * 2);   // Only 6 → 18 at rank 6 (was 25 → 85)
            world.spawnParticle(Particle.CLOUD, target.clone().add(0, 0.5, 0), cloudCount, 0.5 + (rank * 0.08), 0.2 + (rank * 0.05), 0.5 + (rank * 0.08), 0.02);
        }
        
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
