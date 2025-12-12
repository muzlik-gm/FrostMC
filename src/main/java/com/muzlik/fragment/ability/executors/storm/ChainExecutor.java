package com.muzlik.fragment.ability.executors.storm;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain - Storm Fragment Secondary Ability
 * Lightning chains between up to 5 enemies within 8 blocks
 * Base: 5 hearts per target, scales with rank
 */
public class ChainExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Find initial target
        LivingEntity initialTarget = findNearestEnemy(player.getEyeLocation(), 15.0, player);
        if (initialTarget == null) {
            return;
        }
        
        // Scale chain parameters
        double damage = 10.0 + (rank * 2.0); // 5 hearts + 1 heart per rank
        int maxChains = 5 + (rank / 2); // 5-8 chains
        double chainRange = 8.0 + (rank * 1.0);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Start chain lightning
        List<LivingEntity> hitTargets = new ArrayList<>();
        executeChainLightning(plugin, player, initialTarget, hitTargets, damage, maxChains, chainRange, rank, 0);
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.2f);
    }
    
    private void executeChainLightning(com.muzlik.FrostSMPPlugin plugin, Player caster, LivingEntity target, 
                                     List<LivingEntity> hitTargets, double damage, int maxChains, 
                                     double chainRange, int rank, int chainIndex) {
        
        if (chainIndex >= maxChains || target == null || hitTargets.contains(target)) {
            return;
        }
        
        // Add to hit list
        hitTargets.add(target);
        
        // Deal damage
        target.damage(damage, caster);
        
        // Lightning impact VFX
        VFXLayerBuilder impactVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, caster)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.ELECTRIC_SPARK, 20 + (rank * 4), ParticlePattern.BURST, 1.0, 1.0, 1.0, 0.12, null)
            .core(Particle.FLASH, 10 + (rank * 2), ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.08, null);
        
        impactVfx.spawn();
        
        // Find next target
        LivingEntity nextTarget = findNearestEnemy(target.getLocation(), chainRange, caster, hitTargets);
        
        if (nextTarget != null) {
            // Create chain VFX
            createChainVFX(plugin, target.getLocation().add(0, 1, 0), nextTarget.getLocation().add(0, 1, 0), rank, caster);
            
            // Continue chain after short delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    executeChainLightning(plugin, caster, nextTarget, hitTargets, damage * 0.9, 
                                        maxChains, chainRange, rank, chainIndex + 1);
                }
            }.runTaskLater(plugin, 3L); // 0.15 second delay
        }
        
        // Chain sound
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.5f);
    }
    
    private void createChainVFX(com.muzlik.FrostSMPPlugin plugin, Location from, Location to, int rank, Player caster) {
        // Create particles along the chain path
        double distance = from.distance(to);
        int particleCount = (int) (distance * 3); // 3 particles per block
        
        for (int i = 0; i <= particleCount; i++) {
            double ratio = (double) i / particleCount;
            Location chainLoc = from.clone().add(to.toVector().subtract(from.toVector()).multiply(ratio));
            
            VFXLayerBuilder chainVfx = new VFXLayerBuilder(plugin, chainLoc, rank, caster)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .core(Particle.ELECTRIC_SPARK, 2 + (rank / 3), ParticlePattern.POINT, 0.1, 0.1, 0.1, 0.02, null);
            
            chainVfx.spawn();
        }
    }
    
    private LivingEntity findNearestEnemy(Location center, double range, Player player) {
        return findNearestEnemy(center, range, player, new ArrayList<>());
    }
    
    private LivingEntity findNearestEnemy(Location center, double range, Player player, List<LivingEntity> exclude) {
        LivingEntity nearest = null;
        double nearestDistance = range;
        
        for (Entity entity : center.getWorld().getNearbyEntities(center, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player && !exclude.contains(entity)) {
                double distance = entity.getLocation().distance(center);
                if (distance < nearestDistance) {
                    nearest = (LivingEntity) entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearest;
    }
}