package com.muzlik.fragment.ability.executors.storm;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.DamageAttributionManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

public class ChainLightningExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 4.0; // EXTREME NERF: 10.0 → 4.0 (2 hearts, secondary chain with falloff)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Find first target using raycast - FIXED: Use shared targeting utility
        LivingEntity firstTarget = com.muzlik.fragment.ability.TargetingUtil.getTargetEntity(player, 15);
        
        if (firstTarget == null) {
            player.sendMessage("§c✗ No target found!");
            return;
        }
        
        // Chain lightning effect
        int maxChains = 3 + (rank / 2); // Scale chains with rank
        java.util.Set<LivingEntity> hitTargets = new java.util.HashSet<>();
        
        // Execute chain with delay to prevent recursion
        chainLightning(player, firstTarget, hitTargets, damage, maxChains, 0, rank);
        
    }
    
    private void chainLightning(Player player, LivingEntity target, 
                                java.util.Set<LivingEntity> hitTargets, 
                                double damage, int maxChains, int currentChain, int rank) {
        // Safety checks
        if (target == null || currentChain >= maxChains) {
            return;
        }
        
        // CRITICAL: Check if already hit this target
        if (hitTargets.contains(target)) {
            return; // Don't hit same target twice
        }
        
        // Check if target is still valid and alive
        if (!target.isValid() || target.isDead()) {
            return;
        }
        
        // Mark as hit BEFORE dealing damage
        hitTargets.add(target);
        
        // Deal damage using proper attribution (prevents infinite loop)
        target.damage(damage, player);
        
        // VFX: Lightning strike effect
        target.getWorld().strikeLightningEffect(target.getLocation());
        
        // FOCUSED chain hit VFX - clean electric impact per chain
        // Reduced counts since this fires multiple times per ability use
        Location loc = target.getLocation().add(0, 1, 0);
        World world = target.getWorld();
        
        // Layer 1: Electric sparks (core) - reduced for chain efficiency
        int sparkCount = 12 + (rank * 3);      // 12 → 30 at rank 6 (was 40 constant)
        world.spawnParticle(Particle.ELECTRIC_SPARK, loc, sparkCount, 0.3, 0.4, 0.3, 0.08);
        
        // Layer 2: Soul fire flame (blue glow) - subtle accent
        int flameCount = 6 + (rank * 2);       // 6 → 18 at rank 6 (was 20 constant)
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, flameCount, 0.2, 0.35, 0.2, 0.03);
        
        // Layer 3: End rod (white flash) - minimal highlight
        int rodCount = 4 + rank;               // 4 → 10 at rank 6 (was 15 constant)
        world.spawnParticle(Particle.END_ROD, loc, rodCount, 0.25, 0.4, 0.25, 0.05);
        
        // Layer 4: Crit particles - brief impact acknowledgment
        int critCount = 8 + (rank * 2);        // 8 → 20 at rank 6 (was 25 constant)
        world.spawnParticle(Particle.CRIT, loc, critCount, 0.4, 0.4, 0.4, 0.1);
        
        // Sound layers
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.5f);
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.4f, 1.2f);
        
        // Find next target
        LivingEntity next = null;
        double minDist = 8.0;
        for (Entity nearby : target.getNearbyEntities(8, 8, 8)) {
            if (nearby instanceof LivingEntity && nearby != player && !hitTargets.contains(nearby)) {
                double dist = nearby.getLocation().distance(target.getLocation());
                if (dist < minDist) {
                    next = (LivingEntity) nearby;
                    minDist = dist;
                }
            }
        }
        
        // Chain to next target with delay (prevents stack overflow)
        if (next != null) {
            LivingEntity finalNext = next;
            new BukkitRunnable() {
                @Override
                public void run() {
                    chainLightning(player, finalNext, hitTargets, damage * 0.9, maxChains, currentChain + 1, rank);
                }
            }.runTaskLater(player.getServer().getPluginManager().getPlugin("FrostSMP"), 3L); // 0.15s delay
        }
    }
}
