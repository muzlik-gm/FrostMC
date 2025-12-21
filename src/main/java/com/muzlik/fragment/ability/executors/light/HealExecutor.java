package com.muzlik.fragment.ability.executors.light;

import com.muzlik.util.PotionEffectHelper;

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

/**
 * Heal - Light Fragment Secondary Ability
 * Heals 5 hearts, removes all negative effects, grants Regeneration II for 8s
 * Healing scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: HEART particles
 * - Secondary: GLOW spiral
 * - Ambient: END_ROD wisps
 * - Impact: FLASH burst
 * - Cinematic: Golden glow at rank 4+
 */
public class HealExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location playerLoc = player.getLocation().add(0, 1, 0);
        int rank = context.getRank();
        
        // Scale healing
        double baseHealing = 10.0; // 5 hearts
        double healing = baseHealing + (rank * 2.0); // +1 heart per rank
        
        // Heal player
        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double newHealth = Math.min(maxHealth, currentHealth + healing);
        player.setHealth(newHealth);
        
        // Remove all negative effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (type.equals(PotionEffectType.POISON) || 
                type.equals(PotionEffectType.WITHER) ||
                type.equals(PotionEffectType.BLINDNESS) ||
                type.equals(PotionEffectType.CONFUSION) ||
                type.equals(PotionEffectType.WEAKNESS) ||
                type.equals(PotionEffectType.SLOW) ||
                type.equals(PotionEffectType.SLOW_DIGGING) ||
                type.equals(PotionEffectType.HUNGER) ||
                type.equals(PotionEffectType.UNLUCK)) {
                player.removePotionEffect(type);
            }
        }
        
        // Grant Regeneration II for 8 seconds
        int regenDuration = 160 + (rank * 20); // 8s + 1s per rank
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.REGENERATION, regenDuration, 1));
        
        // 5-Layer VFX System
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        int coreCount = 20 + (rank * 6);
        int secondaryCount = 15 + (rank * 4);
        int ambientCount = 10 + (rank * 3);
        int impactCount = 12 + (rank * 4);
        
        double spread = 0.8 + (rank * 0.1);
        
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core layer: HEART particles rising up
            .core(Particle.HEART, coreCount, ParticlePattern.SPIRAL, spread, spread * 1.5, spread, 0.08, null)
            // Secondary layer: GLOW spiral around player
            .secondary(Particle.GLOW, secondaryCount, ParticlePattern.SPIRAL, spread * 0.6, spread, spread * 0.6, 0.05, null)
            // Ambient layer: END_ROD wisps
            .ambient(Particle.END_ROD, ambientCount, ParticlePattern.SPHERE, spread * 0.4, spread * 0.8, spread * 0.4, 0.03, null)
            // Impact layer: FLASH burst
            .impact(Particle.FLASH, impactCount, ParticlePattern.BURST, spread, spread, spread, 0.1, null);
        
        // Cinematic layer: Light bloom at rank 4+
        if (rank >= 4) {
            vfxBuilder.cinematic(0.25 + (rank * 0.05), CinematicEffect.LIGHT_BLOOM);
        }
        
        vfxBuilder.spawn();
        
        // Sound effect
        float pitch = 1.3f + (rank * 0.1f);
        playerLoc.getWorld().playSound(playerLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f + (rank * 0.1f), pitch);
        playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, pitch);
    }
}