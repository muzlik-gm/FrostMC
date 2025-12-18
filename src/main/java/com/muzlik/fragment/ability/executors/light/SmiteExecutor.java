package com.muzlik.fragment.ability.executors.light;

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
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Smite - Light Fragment Ultimate Ability
 * Summons pillar of light from sky, 7-block radius
 * 8 hearts damage to undead/dark, 5 hearts to others
 * 
 * VFX: 5-Layer System
 * - Core: END_ROD pillar
 * - Secondary: ELECTRIC_SPARK cascade
 * - Ambient: GLOW aura
 * - Impact: FLASH explosion
 * - Cinematic: Divine light at rank 5+
 */
public class SmiteExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location targetLoc = player.getTargetBlock(null, 30).getLocation();
        
        int rank = context.getRank();
        
        // Find highest solid block at target location
        Location smiteLoc = targetLoc.clone();
        smiteLoc.setY(smiteLoc.getWorld().getHighestBlockYAt(smiteLoc) + 1);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create pillar effect from sky down
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 20; // 1 second buildup
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Final smite impact
                    executeSmite(plugin, player, smiteLoc, rank);
                    cancel();
                    return;
                }
                
                // Build pillar from sky
                Location skyLoc = smiteLoc.clone().add(0, 50 - (ticks * 2.5), 0);
                
                int coreCount = 8 + (rank * 2);
                int secondaryCount = 5 + rank;
                
                VFXLayerBuilder pillarVfx = new VFXLayerBuilder(plugin, skyLoc, rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .core(Particle.END_ROD, coreCount, ParticlePattern.POINT, 0.3, 0.3, 0.3, 0.05, null)
                    .secondary(Particle.ELECTRIC_SPARK, secondaryCount, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.03, null);
                
                pillarVfx.spawn();
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Warning sound
        smiteLoc.getWorld().playSound(smiteLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1.8f);
    }
    
    private void executeSmite(com.muzlik.FrostSMPPlugin plugin, Player caster, Location smiteLoc, int rank) {
        // Damage calculation
        double undeadDamage = 12.0 + (rank * 1.5); // 6 hearts + 0.75 hearts per rank
        double normalDamage = 8.0 + (rank * 1.0); // 4 hearts + 0.5 hearts per rank
        
        double radius = 7.0 + (rank * 0.5);
        
        // Damage all entities in radius
        for (Entity entity : smiteLoc.getWorld().getNearbyEntities(smiteLoc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != caster) {
                LivingEntity target = (LivingEntity) entity;
                
                // Check if undead or dark-type
                boolean isUndead = target.getType() == EntityType.ZOMBIE ||
                                 target.getType() == EntityType.SKELETON ||
                                 target.getType() == EntityType.WITHER ||
                                 target.getType() == EntityType.WITHER_SKELETON ||
                                 target.getType() == EntityType.ZOMBIE_VILLAGER ||
                                 target.getType() == EntityType.HUSK ||
                                 target.getType() == EntityType.STRAY ||
                                 target.getType() == EntityType.PHANTOM;
                
                double damage = isUndead ? undeadDamage : normalDamage;
                target.damage(damage, caster);
            }
        }
        
        // Massive VFX explosion
        int coreCount = 40 + (rank * 10);
        int secondaryCount = 30 + (rank * 8);
        int ambientCount = 25 + (rank * 6);
        int impactCount = 35 + (rank * 9);
        
        double spread = 3.0 + (rank * 0.5);
        
        VFXLayerBuilder smiteVfx = new VFXLayerBuilder(plugin, smiteLoc, rank, caster)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core layer: END_ROD explosion
            .core(Particle.END_ROD, coreCount, ParticlePattern.BURST, spread, spread * 2, spread, 0.15, null)
            // Secondary layer: ELECTRIC_SPARK cascade
            .secondary(Particle.ELECTRIC_SPARK, secondaryCount, ParticlePattern.SPHERE, spread * 0.8, spread * 1.5, spread * 0.8, 0.12, null)
            // Ambient layer: GLOW aura
            .ambient(Particle.GLOW, ambientCount, ParticlePattern.SPHERE, spread * 1.2, spread, spread * 1.2, 0.08, null)
            // Impact layer: FLASH explosion
            .impact(Particle.FLASH, impactCount, ParticlePattern.BURST, spread * 1.5, spread * 2.5, spread * 1.5, 0.2, null);
        
        // Cinematic layer: Light bloom at rank 5+
        if (rank >= 5) {
            smiteVfx.cinematic(0.4 + (rank * 0.1), CinematicEffect.LIGHT_BLOOM);
        }
        
        smiteVfx.spawn();
        
        // Thunder sound
        float pitch = 0.8f + (rank * 0.1f);
        smiteLoc.getWorld().playSound(smiteLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, pitch);
        smiteLoc.getWorld().playSound(smiteLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, pitch);
    }
}