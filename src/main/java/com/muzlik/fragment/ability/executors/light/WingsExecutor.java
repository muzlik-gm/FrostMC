package com.muzlik.fragment.ability.executors.light;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Wings - Light Fragment Mastery Ability
 * Become angelic being for 15s: flight, all healing doubled, immune to debuffs, +60% damage
 * Duration and effects scale with rank
 * 
 * VFX: 5-Layer System
 * - Core: END_ROD wings
 * - Secondary: GLOW aura
 * - Ambient: HEART particles
 * - Impact: FLASH pulses
 * - Cinematic: Angelic transformation at rank 7+
 */
public class WingsExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        int duration = 300 + (rank * 40); // 15s + 2s per rank (in ticks)
        
        // Store original flight state
        boolean originalFlight = player.getAllowFlight();
        boolean originalFlying = player.isFlying();
        
        // Grant flight
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Grant beneficial effects
        int effectDuration = duration + 20; // Slightly longer than transformation
        
        // Immunity to debuffs (give beneficial effects that counteract)
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.REGENERATION, effectDuration, 1));
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.DAMAGE_RESISTANCE, effectDuration, 1));
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.INCREASE_DAMAGE, effectDuration, 1)); // +60% damage approximation
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.GLOWING, effectDuration, 0)); // Angelic glow
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial transformation VFX
        createTransformationVFX(plugin, player, rank);
        
        // Continuous angelic effects
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    // End transformation
                    endTransformation(plugin, player, originalFlight, originalFlying, rank);
                    cancel();
                    return;
                }
                
                // Continuous VFX every 5 ticks
                if (ticks % 5 == 0) {
                    createWingVFX(plugin, player, rank);
                }
                
                // Healing boost every second
                if (ticks % 20 == 0) {
                    applyHealingBoost(player, rank);
                }
                
                // Wing flap sound every 3 seconds
                if (ticks % 60 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.3f, 1.5f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Transformation sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.8f);
    }
    
    private void createTransformationVFX(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        int coreCount = 50 + (rank * 10);
        int secondaryCount = 40 + (rank * 8);
        int ambientCount = 30 + (rank * 6);
        int impactCount = 35 + (rank * 7);
        
        double spread = 2.0 + (rank * 0.3);
        
        VFXLayerBuilder transformVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core layer: END_ROD explosion
            .core(Particle.END_ROD, coreCount, ParticlePattern.BURST, spread, spread * 1.5, spread, 0.2, null)
            // Secondary layer: GLOW aura
            .secondary(Particle.GLOW, secondaryCount, ParticlePattern.SPHERE, spread * 0.8, spread, spread * 0.8, 0.15, null)
            // Ambient layer: HEART particles
            .ambient(Particle.HEART, ambientCount, ParticlePattern.SPIRAL, spread * 0.6, spread * 2, spread * 0.6, 0.1, null)
            // Impact layer: FLASH burst
            .impact(Particle.FLASH, impactCount, ParticlePattern.BURST, spread * 1.2, spread * 2, spread * 1.2, 0.25, null);
        
        // Cinematic layer: Light bloom at rank 7+
        if (rank >= 7) {
            transformVfx.cinematic(0.5 + (rank * 0.1), CinematicEffect.LIGHT_BLOOM);
        }
        
        transformVfx.spawn();
    }
    
    private void createWingVFX(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        // Get player's direction vector
        org.bukkit.util.Vector direction = playerLoc.getDirection().normalize();
        
        // Calculate wing positions relative to player's direction
        // Wings are positioned behind and slightly to the sides
        double wingSpread = 0.8; // Distance to the side
        double wingBackOffset = 1.0; // Behind the player
        double wingHeight = 0.3; // Height offset
        
        // Get perpendicular vector for left/right positioning
        org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        // Position wings behind and to the sides
        org.bukkit.util.Vector backVector = direction.clone().multiply(-wingBackOffset);
        
        // Left wing (behind + left)
        org.bukkit.util.Vector leftOffset = backVector.clone().add(perpendicular.clone().multiply(wingSpread));
        Location leftWing = playerLoc.clone().add(leftOffset).add(0, wingHeight, 0);
        
        // Right wing (behind + right)
        org.bukkit.util.Vector rightOffset = backVector.clone().add(perpendicular.clone().multiply(-wingSpread));
        Location rightWing = playerLoc.clone().add(rightOffset).add(0, wingHeight, 0);
        
        int wingCount = 8 + (rank * 2);
        int auraCount = 5 + rank;
        
        // Left wing
        VFXLayerBuilder leftWingVfx = new VFXLayerBuilder(plugin, leftWing, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.END_ROD, wingCount, ParticlePattern.POINT, 0.8, 1.2, 0.3, 0.05, null)
            .secondary(Particle.GLOW, auraCount, ParticlePattern.POINT, 0.5, 0.8, 0.2, 0.03, null);
        
        // Right wing
        VFXLayerBuilder rightWingVfx = new VFXLayerBuilder(plugin, rightWing, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.END_ROD, wingCount, ParticlePattern.POINT, 0.8, 1.2, 0.3, 0.05, null)
            .secondary(Particle.GLOW, auraCount, ParticlePattern.POINT, 0.5, 0.8, 0.2, 0.03, null);
        
        leftWingVfx.spawn();
        rightWingVfx.spawn();
        
        // Player aura
        VFXLayerBuilder auraVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .ambient(Particle.GLOW, auraCount, ParticlePattern.SPHERE, 0.8, 1.0, 0.8, 0.02, null);
        
        auraVfx.spawn();
    }
    
    private void applyHealingBoost(Player player, int rank) {
        // Doubled healing effect - heal small amount continuously
        double healAmount = 1.0 + (rank * 0.3); // 0.5 hearts + scaling
        
        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double newHealth = Math.min(maxHealth, currentHealth + healAmount);
        player.setHealth(newHealth);
    }
    
    private void endTransformation(com.muzlik.FrostSMPPlugin plugin, Player player, boolean originalFlight, boolean originalFlying, int rank) {
        // Restore original flight state
        player.setAllowFlight(originalFlight);
        if (!originalFlying) {
            player.setFlying(false);
        }
        
        // End transformation VFX
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        int endCount = 30 + (rank * 5);
        
        VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.FLASH, endCount, ParticlePattern.BURST, 1.5, 2.0, 1.5, 0.15, null)
            .core(Particle.END_ROD, endCount, ParticlePattern.SPHERE, 1.0, 1.5, 1.0, 0.1, null);
        
        endVfx.spawn();
        
        // End sound
        player.getWorld().playSound(playerLoc, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.5f);
    }
}