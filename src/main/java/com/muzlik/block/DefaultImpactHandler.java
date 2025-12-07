package com.muzlik.block;

import com.muzlik.vfx.DamageAttributionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.logging.Level;

/**
 * Default implementation of ImpactHandler for block controllers.
 * 
 * Handles standard impact logic:
 * - Damage calculation with rank scaling
 * - Knockback application
 * - Damage attribution
 * - Impact VFX spawning
 * 
 * Requirements: 2.4, 2.5
 */
public class DefaultImpactHandler implements ImpactHandler {
    private final JavaPlugin plugin;
    private final DamageAttributionManager damageAttribution;
    private final ParticleShellRenderer particleRenderer;
    
    // Default knockback strength
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.5;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     * @param damageAttribution Damage attribution manager
     * @param particleRenderer Particle renderer for VFX
     */
    public DefaultImpactHandler(JavaPlugin plugin, 
                               DamageAttributionManager damageAttribution,
                               ParticleShellRenderer particleRenderer) {
        this.plugin = plugin;
        this.damageAttribution = damageAttribution;
        this.particleRenderer = particleRenderer;
    }
    
    @Override
    public void onEntityImpact(BlockController controller, Entity target, Location impactLocation) {
        if (!(target instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity living = (LivingEntity) target;
        Player owner = getOwner(controller);
        
        if (owner == null) {
            plugin.getLogger().warning("Block controller owner is offline - cannot attribute damage");
            return;
        }
        
        // Calculate damage with rank scaling
        double baseDamage = controller.getBaseDamage();
        int rank = controller.getFragmentRank();
        double finalDamage = calculateDamage(baseDamage, rank);
        
        // Deal damage with proper attribution
        // The owner is set as the damager for proper kill credit
        living.damage(finalDamage, owner);
        
        // Calculate and apply knockback
        Vector knockback = calculateKnockback(
            impactLocation, 
            target.getLocation(), 
            DEFAULT_KNOCKBACK_STRENGTH
        );
        target.setVelocity(knockback);
        
        // Special effects based on block type
        Material blockType = controller.getBlockType();
        if (blockType == Material.MAGMA_BLOCK || blockType == Material.NETHERRACK) {
            // Set target on fire
            living.setFireTicks(60); // 3 seconds
            // Explosion effect
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, impactLocation, 1, 0, 0, 0, 0);
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.FLAME, impactLocation, 15, 0.5, 0.5, 0.5, 0.1);
            impactLocation.getWorld().playSound(impactLocation, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
        } else if (blockType == Material.ICE || blockType == Material.PACKED_ICE) {
            // Slow effect
            living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOW, 40, 1, false, false));
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, impactLocation, 10, 0.5, 0.5, 0.5, 0);
        }
        
        // Spawn impact VFX
        if (particleRenderer != null) {
            particleRenderer.renderImpact(impactLocation, controller.getBlockType());
        }
        
        plugin.getLogger().log(Level.FINE, 
            "Block controller impact: " + owner.getName() + " -> " + 
            target.getType() + " (" + finalDamage + " damage)");
    }
    
    @Override
    public void onBlockImpact(BlockController controller, Location impactLocation) {
        // Special effects based on block type
        Material blockType = controller.getBlockType();
        if (blockType == Material.MAGMA_BLOCK || blockType == Material.NETHERRACK) {
            // Explosion effect on ground
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, impactLocation, 1, 0, 0, 0, 0);
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.FLAME, impactLocation, 10, 0.5, 0.5, 0.5, 0.05);
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.LAVA, impactLocation, 5, 0.3, 0.3, 0.3, 0);
            impactLocation.getWorld().playSound(impactLocation, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);
        } else if (blockType == Material.ICE || blockType == Material.PACKED_ICE) {
            // Ice shatter effect
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, impactLocation, 15, 0.5, 0.5, 0.5, 0);
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, impactLocation, 10, 0.3, 0.3, 0.3, 0);
            impactLocation.getWorld().playSound(impactLocation, org.bukkit.Sound.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
        } else {
            // Stone/earth impact
            impactLocation.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, impactLocation, 8, 0.3, 0.3, 0.3, 0);
            impactLocation.getWorld().playSound(impactLocation, org.bukkit.Sound.BLOCK_STONE_HIT, 0.8f, 0.8f);
        }
        
        // Spawn impact VFX
        if (particleRenderer != null) {
            particleRenderer.renderImpact(impactLocation, controller.getBlockType());
        }
        
        plugin.getLogger().log(Level.FINE, 
            "Block controller block impact at " + impactLocation);
    }
}
