package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Clone - Dark Fragment Advanced Ability
 * Creates shadow clones that mimic attacks
 */
public class CloneExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        double baseDuration = 15.0;
        int durationTicks = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Create 2 shadow clones (armor stands)
        ArmorStand clone1 = loc.getWorld().spawn(loc.clone().add(2, 0, 0), ArmorStand.class);
        ArmorStand clone2 = loc.getWorld().spawn(loc.clone().add(-2, 0, 0), ArmorStand.class);
        
        // Configure clones
        for (ArmorStand clone : new ArmorStand[]{clone1, clone2}) {
            clone.setVisible(false);
            clone.setGravity(false);
            clone.setInvulnerable(true);
            clone.setCustomName("§5Shadow Clone");
            clone.setCustomNameVisible(true);
            clone.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.WITHER_SKELETON_SKULL));
        }
        
        // Initial clone creation VFX
        VFXLayerBuilder cloneVFX = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.SMOKE_LARGE, 60, ParticlePattern.SPHERE, 3, 2, 3, 0.1, null)
            .secondary(Particle.SQUID_INK, 40, ParticlePattern.BURST, 2, 1, 2, 0.08, null)
            .ambient(Particle.PORTAL, 30, ParticlePattern.RING, 2.5, 1, 2.5, 0.05, null)
            .cinematic(0.25, CinematicEffect.DARKNESS_PULSE);
        
        cloneVFX.spawn();
        
        // Clone behavior and cleanup
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    // Remove clones
                    clone1.remove();
                    clone2.remove();
                    player.sendMessage("§5👥 §dShadow clones fade away");
                    cancel();
                    return;
                }
                
                // Clone VFX every 40 ticks
                if (ticks % 40 == 0) {
                    for (ArmorStand clone : new ArmorStand[]{clone1, clone2}) {
                        if (clone.isValid()) {
                            new VFXLayerBuilder(plugin, clone.getLocation(), rank, player)
                                .withPerformanceManager(plugin.getVFXPerformanceManager())
                                .core(Particle.SMOKE_LARGE, 8, ParticlePattern.SPHERE, 0.5, 1, 0.5, 0.02, null)
                                .secondary(Particle.SQUID_INK, 5, ParticlePattern.POINT, 0.3, 0.5, 0.3, 0.01, null)
                                .spawn();
                        }
                    }
                }
                
                // Move clones to follow player (simplified)
                if (ticks % 20 == 0) {
                    Location playerLoc = player.getLocation();
                    if (clone1.isValid()) {
                        clone1.teleport(playerLoc.clone().add(2, 0, 1));
                    }
                    if (clone2.isValid()) {
                        clone2.teleport(playerLoc.clone().add(-2, 0, 1));
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
        
        player.sendMessage("§5👥 §dShadow clones summoned!");
    }
}