package com.muzlik.block;

import com.muzlik.vfx.DamageAttributionManager;
import org.bukkit.Location;
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
        // Spawn impact VFX
        if (particleRenderer != null) {
            particleRenderer.renderImpact(impactLocation, controller.getBlockType());
        }
        
        plugin.getLogger().log(Level.FINE, 
            "Block controller block impact at " + impactLocation);
    }
}
