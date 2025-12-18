package com.muzlik.fragment.ability.executors.voidfrag;

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
import org.bukkit.util.Vector;

/**
 * Slash - Void Fragment Primary Ability
 * Tears space itself, dealing true damage that bypasses defenses and totems
 * Fixed: 2 hearts (ignores armor and totem), no scaling
 * 
 * VFX: 5-Layer System
 * - Core: PORTAL particles
 * - Secondary: DRAGON_BREATH trail
 * - Ambient: SMOKE_LARGE wisps
 * - Impact: EXPLOSION_LARGE burst
 * - Cinematic: Reality warp at rank 8+
 */
public class SlashExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        
        // True damage - bypasses armor and totems
        double damage = 4.0; // 2 hearts, no scaling
        
        // Slash range increases with rank
        double range = 8.0 + (rank * 1.0);
        
        // Create void slash along path
        Location currentLoc = eyeLoc.clone();
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Track hit entities to prevent multiple hits
        java.util.Set<java.util.UUID> hitEntities = new java.util.HashSet<>();
        
        for (double d = 0; d < range; d += 0.3) {
            currentLoc.add(direction.clone().multiply(0.3));
            
            // Check for block collision
            if (!currentLoc.getBlock().isPassable()) {
                break;
            }
            
            // Void slash VFX
            int coreCount = 5 + rank;
            int secondaryCount = 3 + rank;
            
            VFXLayerBuilder slashVfx = new VFXLayerBuilder(plugin, currentLoc.clone(), rank, player)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .core(Particle.PORTAL, coreCount, ParticlePattern.POINT, 0.2, 0.2, 0.2, 0.05, null)
                .secondary(Particle.DRAGON_BREATH, secondaryCount, ParticlePattern.POINT, 0.3, 0.3, 0.3, 0.03, null);
            
            slashVfx.spawn();
            
            // Check for entity hits
            for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 1.5, 1.5, 1.5)) {
                if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity.getUniqueId())) {
                    LivingEntity target = (LivingEntity) entity;
                    
                    // Mark as hit to prevent multiple hits
                    hitEntities.add(entity.getUniqueId());
                    
                    // Deal true damage using damage API (bypasses armor but respects game mechanics)
                    // Use the plugin's damage API if available, otherwise use setHealth
                    if (plugin.getDamageAPI() != null) {
                        plugin.getDamageAPI().dealTrueDamage(player, target, damage);
                    } else {
                        target.setHealth(Math.max(0, target.getHealth() - damage));
                    }
                    
                    // Impact VFX
                    int impactCount = 20 + (rank * 5);
                    VFXLayerBuilder impactVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, player)
                        .withPerformanceManager(plugin.getVFXPerformanceManager())
                        .impact(Particle.EXPLOSION_LARGE, impactCount, ParticlePattern.BURST, 1.0, 1.0, 1.0, 0.15, null)
                        .ambient(Particle.SMOKE_LARGE, impactCount / 2, ParticlePattern.SPHERE, 0.8, 0.8, 0.8, 0.08, null);
                    
                    if (rank >= 8) {
                        impactVfx.cinematic(0.3 + (rank * 0.05), CinematicEffect.REALITY_WARP);
                    }
                    
                    impactVfx.spawn();
                }
            }
        }
        
        // Sound effect
        float pitch = 0.8f + (rank * 0.1f);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f + (rank * 0.2f), pitch);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, pitch);
    }
}