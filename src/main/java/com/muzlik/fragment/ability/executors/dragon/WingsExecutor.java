package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.util.PotionEffectHelper;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Wings - Dragon Fragment Secondary Ability
 * Fly freely for 30 minutes, +80% speed, immune to fall damage
 * Duration scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: DRAGON_BREATH wings
 * - Secondary: FLAME trail
 * - Ambient: SMOKE_NORMAL wisps
 * - Impact: Wing flap particles
 */
public class WingsExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Duration scales with rank (in ticks)
        int baseDuration = 600; // 30 seconds
        int duration = baseDuration + (rank * 60); // +3 seconds per rank
        
        // Store original flight state
        boolean originalFlight = player.getAllowFlight();
        boolean originalFlying = player.isFlying();
        
        // Grant flight
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Grant speed boost
        int speedLevel = 1 + (rank / 3); // Speed II at rank 3, III at rank 6, etc.
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(PotionEffectType.SPEED, duration + 20, speedLevel));
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Initial transformation VFX
        createTransformationVFX(plugin, player, rank);
        
        // Continuous wing effects
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    // End flight
                    endFlight(plugin, player, originalFlight, originalFlying, rank);
                    cancel();
                    return;
                }
                
                // Wing VFX every 10 ticks when flying
                if (ticks % 10 == 0 && player.isFlying()) {
                    createWingVFX(plugin, player, rank);
                }
                
                // Wing flap sound every 5 seconds when flying
                if (ticks % 100 == 0 && player.isFlying()) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.4f, 0.8f);
                }
                
                // Immunity to fall damage (reset fall distance)
                if (player.getFallDistance() > 0) {
                    player.setFallDistance(0);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Transformation sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
    }
    
    private void createTransformationVFX(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        int coreCount = 30 + (rank * 6);
        int secondaryCount = 25 + (rank * 5);
        int ambientCount = 20 + (rank * 4);
        
        double spread = 2.0 + (rank * 0.3);
        
        VFXLayerBuilder transformVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.DRAGON_BREATH, coreCount, ParticlePattern.BURST, spread, spread * 1.5, spread, 0.15, null)
            .secondary(Particle.FLAME, secondaryCount, ParticlePattern.SPHERE, spread * 0.8, spread, spread * 0.8, 0.12, null)
            .ambient(Particle.SMOKE_NORMAL, ambientCount, ParticlePattern.SPIRAL, spread * 0.6, spread * 2, spread * 0.6, 0.08, null);
        
        transformVfx.spawn();
    }
    
    private void createWingVFX(com.muzlik.FrostSMPPlugin plugin, Player player, int rank) {
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        // Get player's direction (yaw in radians)
        // In Minecraft: yaw 0 = south, 90 = west, 180 = north, 270 = east
        float yaw = playerLoc.getYaw();
        double yawRadians = Math.toRadians(yaw + 90); // Add 90 to align with direction vector
        
        // Calculate wing positions relative to player's direction
        // Wings are positioned behind and to the sides
        double wingDistance = 2.0; // Distance to the side
        double wingBackOffset = 1.0; // Behind the player
        double wingHeight = 0.5; // Height offset
        
        // Direction the player is facing
        double dirX = -Math.sin(yawRadians);
        double dirZ = Math.cos(yawRadians);
        
        // Perpendicular direction (for left/right)
        double perpX = -dirZ;
        double perpZ = dirX;
        
        // Left wing (to the left and behind)
        double leftX = perpX * wingDistance - dirX * wingBackOffset;
        double leftZ = perpZ * wingDistance - dirZ * wingBackOffset;
        Location leftWing = playerLoc.clone().add(leftX, wingHeight, leftZ);
        
        // Right wing (to the right and behind)
        double rightX = -perpX * wingDistance - dirX * wingBackOffset;
        double rightZ = -perpZ * wingDistance - dirZ * wingBackOffset;
        Location rightWing = playerLoc.clone().add(rightX, wingHeight, rightZ);
        
        int wingCount = 6 + rank;
        int trailCount = 4 + rank;
        
        // Left wing
        VFXLayerBuilder leftWingVfx = new VFXLayerBuilder(plugin, leftWing, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.DRAGON_BREATH, wingCount, ParticlePattern.POINT, 1.0, 1.5, 0.5, 0.04, null)
            .secondary(Particle.FLAME, trailCount, ParticlePattern.POINT, 0.6, 1.0, 0.3, 0.03, null);
        
        // Right wing
        VFXLayerBuilder rightWingVfx = new VFXLayerBuilder(plugin, rightWing, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.DRAGON_BREATH, wingCount, ParticlePattern.POINT, 1.0, 1.5, 0.5, 0.04, null)
            .secondary(Particle.FLAME, trailCount, ParticlePattern.POINT, 0.6, 1.0, 0.3, 0.03, null);
        
        leftWingVfx.spawn();
        rightWingVfx.spawn();
        
        // Trail behind player when moving fast (also direction-aware)
        if (player.getVelocity().length() > 0.3) {
            double trailX = -dirX * wingBackOffset;
            double trailZ = -dirZ * wingBackOffset;
            Location trailLoc = playerLoc.clone().add(trailX, 0, trailZ);
            
            VFXLayerBuilder trailVfx = new VFXLayerBuilder(plugin, trailLoc, rank, player)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .ambient(Particle.SMOKE_NORMAL, trailCount, ParticlePattern.POINT, 0.5, 0.5, 0.5, 0.02, null);
            trailVfx.spawn();
        }
    }
    
    private void endFlight(com.muzlik.FrostSMPPlugin plugin, Player player, boolean originalFlight, boolean originalFlying, int rank) {
        // Restore original flight state
        player.setAllowFlight(originalFlight);
        if (!originalFlying) {
            player.setFlying(false);
        }
        
        // End transformation VFX
        Location playerLoc = player.getLocation().add(0, 1, 0);
        
        int endCount = 20 + (rank * 4);
        
        VFXLayerBuilder endVfx = new VFXLayerBuilder(plugin, playerLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.SMOKE_LARGE, endCount, ParticlePattern.BURST, 1.5, 2.0, 1.5, 0.1, null)
            .core(Particle.DRAGON_BREATH, endCount, ParticlePattern.SPHERE, 1.0, 1.5, 1.0, 0.08, null);
        
        endVfx.spawn();
        
        // End sound
        player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_HURT, 0.6f, 1.5f);
    }
}