package com.muzlik.fragment.ability.executors.water;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.environment.EnvironmentManager;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Tidal Shield - Water Fragment Secondary (Slot 1)
 * Creates water barrier with absorption and damage resistance
 * FIXED: Proper block manipulation with safety checks
 * 
 * VFX: 5-Layer System with bubble effects
 * - Core: WATER_BUBBLE shield
 * - Secondary: DRIP_WATER flowing
 * - Ambient: BUBBLE_COLUMN_UP rising
 * - Impact: WATER_SPLASH burst
 * - Cinematic: Water shimmer at rank 3+
 */
public class TidalShieldExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDuration = 8.0;
        int durationTicks = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        if (plugin == null) {
            player.sendMessage("§c✗ Plugin error");
            return;
        }
        
        EnvironmentManager envManager = plugin.getEnvironmentManager();
        if (envManager == null) {
            player.sendMessage("§c✗ Environment manager not available");
            return;
        }
        
        // Create water wall in front of player
        Vector direction = player.getLocation().getDirection().normalize();
        direction.setY(0); // Keep horizontal
        direction.normalize();
        
        Location center = player.getLocation().add(direction.multiply(2));
        
        // Build 3x3 water wall
        int wallHeight = 3;
        int wallWidth = 3;
        
        // Determine wall orientation (perpendicular to player direction)
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        
        int blocksPlaced = 0;
        for (int y = 0; y < wallHeight; y++) {
            for (int x = -wallWidth/2; x <= wallWidth/2; x++) {
                Location blockLoc = center.clone().add(right.clone().multiply(x)).add(0, y, 0);
                Block block = blockLoc.getBlock();
                
                // Only place if air and not too many blocks
                if (block.getType() == Material.AIR && blocksPlaced < 9) {
                    try {
                        // Use BLUE_ICE instead of WATER (water doesn't stay static)
                        envManager.placeTemporaryBlock(blockLoc, Material.BLUE_ICE, durationTicks, player);
                        blocksPlaced++;
                    } catch (Exception e) {
                        // Silently fail for individual blocks
                    }
                }
            }
        }
        
        // Buffs
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, durationTicks, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, durationTicks, 2, false, false));
        
        // 5-Layer VFX System
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, center, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: WATER_BUBBLE shield
            .core(Particle.WATER_BUBBLE, 80, ParticlePattern.SPHERE, 1.5, 1, 1.5, 0.1, null)
            // Secondary: DRIP_WATER flowing
            .secondary(Particle.DRIP_WATER, 50, ParticlePattern.RING, 1.5, 1, 1.5, 0.05, null)
            // Ambient: BUBBLE_COLUMN_UP rising
            .ambient(Particle.BUBBLE_COLUMN_UP, 30, ParticlePattern.LINE, 1.5, 2, 1.5, 0.1, null)
            // Impact: WATER_SPLASH burst
            .impact(Particle.WATER_SPLASH, 50, ParticlePattern.BURST, 1.5, 1, 1.5, 0.2, null);
        
        // Cinematic: Water shimmer at rank 3+
        if (rank >= 3) {
            vfxBuilder.cinematic(0.2, CinematicEffect.WATER_SHIMMER);
        }
        
        vfxBuilder.spawn();
        
        // Sound
        player.getWorld().playSound(center, Sound.BLOCK_WATER_AMBIENT, 2.0f, 0.8f);
        player.getWorld().playSound(center, Sound.ENTITY_PLAYER_SPLASH, 1.5f, 1.0f);
        
    }
}
