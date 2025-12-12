package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Cataclysm - Dragon Fragment Ultimate Ability
 * Transform into dragon form for 10s: all abilities cost 0 mana, +100% damage, AOE attacks
 */
public class CataclysmExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        int duration = 200 + (rank * 40); // 10s + 2s per rank
        
        // Grant dragon form effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 2)); // Strength III
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 1)); // Resistance II
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1)); // Speed II
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0)); // Dragon glow
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Transformation VFX
        createTransformationVFX(plugin, player, rank);
        
        // Continuous dragon aura
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    endTransformation(plugin, player, rank);
                    cancel();
                    return;
                }
                
                // Dragon aura VFX every 5 ticks
                if (ticks % 5 == 0) {
                    createDragonAura(plugin, player, rank);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Transformation sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
    }
    
    private void createTransformationVFX(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        VFXLayerBuilder transformVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.DRAGON_BREATH, 50 + (rank * 10), ParticlePattern.BURST, 3.0, 3.0, 3.0, 0.2, null)
            .cinematic(0.5, CinematicEffect.DRAGON_ROAR);
        
        transformVfx.spawn();
    }
    
    private void createDragonAura(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        VFXLayerBuilder auraVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .ambient(Particle.DRAGON_BREATH, 8 + rank, ParticlePattern.SPHERE, 1.5, 1.5, 1.5, 0.03, null);
        
        auraVfx.spawn();
    }
    
    private void endTransformation(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.EXPLOSION_LARGE, 30 + (rank * 5), ParticlePattern.BURST, 2.0, 2.0, 2.0, 0.15, null);
        
        endVfx.spawn();
        
        player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 1.2f);
    }
}