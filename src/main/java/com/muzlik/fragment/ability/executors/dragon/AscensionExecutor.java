package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.util.PotionEffectHelper;

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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ascension - Dragon Fragment Mastery Ability
 * Become a true dragon for 15s - Strength IV, Resistance IV, AOE damage aura, flight
 */
public class AscensionExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        int duration = 300 + (rank * 60); // 15s + 3s per rank
        
        // Store original flight state
        boolean originalFlight = player.getAllowFlight();
        boolean originalFlying = player.isFlying();
        
        // Grant true dragon form
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.INCREASE_DAMAGE, duration, 3)); // Strength IV
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 3)); // Resistance IV
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SPEED, duration, 2)); // Speed III
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.GLOWING, duration, 0)); // Dragon glow
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Ultimate transformation VFX
        createAscensionVFX(plugin, player, rank);
        
        // Continuous dragon lord effects
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    endAscension(plugin, player, originalFlight, originalFlying, rank);
                    cancel();
                    return;
                }
                
                // Dragon aura VFX every 3 ticks
                if (ticks % 3 == 0) {
                    createDragonLordAura(plugin, player, rank);
                }
                
                // AOE damage every second
                if (ticks % 20 == 0) {
                    applyAOEDamage(plugin, player, rank);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Ultimate transformation sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
    }
    
    private void createAscensionVFX(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        VFXLayerBuilder ascensionVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.DRAGON_BREATH, 80 + (rank * 15), ParticlePattern.BURST, 4.0, 4.0, 4.0, 0.3, null)
            .secondary(Particle.FLAME, 60 + (rank * 12), ParticlePattern.SPHERE, 3.0, 3.0, 3.0, 0.25, null)
            .impact(Particle.EXPLOSION_LARGE, 40 + (rank * 8), ParticlePattern.BURST, 5.0, 5.0, 5.0, 0.4, null)
            .cinematic(0.8, CinematicEffect.DRAGON_ROAR);
        
        ascensionVfx.spawn();
    }
    
    private void createDragonLordAura(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        VFXLayerBuilder auraVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.DRAGON_BREATH, 12 + (rank * 2), ParticlePattern.SPHERE, 2.0, 2.0, 2.0, 0.05, null)
            .secondary(Particle.FLAME, 8 + rank, ParticlePattern.POINT, 1.5, 1.5, 1.5, 0.03, null);
        
        auraVfx.spawn();
    }
    
    private void applyAOEDamage(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        double damage = 6.0 + (rank * 2.0); // 3 hearts + 1 heart per rank
        double radius = 8.0 + (rank * 1.0);
        
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                
                // AOE damage VFX
                VFXLayerBuilder damageVfx = new VFXLayerBuilder(plugin, target.getLocation().add(0, 1, 0), rank, player)
                    .withPerformanceManager(plugin.getVFXPerformanceManager())
                    .impact(Particle.FLAME, 8 + rank, ParticlePattern.BURST, 0.5, 0.5, 0.5, 0.08, null);
                damageVfx.spawn();
            }
        }
    }
    
    private void endAscension(com.muzlik.FrostSMPPlugin plugin, Player player, boolean originalFlight, boolean originalFlying, int rank) {
        // Restore flight state
        player.setAllowFlight(originalFlight);
        if (!originalFlying) {
            player.setFlying(false);
        }
        
        // End VFX
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.EXPLOSION_LARGE, 50 + (rank * 10), ParticlePattern.BURST, 3.0, 3.0, 3.0, 0.2, null);
        
        endVfx.spawn();
        
        player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
    }
}