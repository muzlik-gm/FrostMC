package com.muzlik.vfx.cinematic.ability;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.vfx.cinematic.*;
import com.muzlik.vfx.cinematic.animation.AnimationController;
import com.muzlik.vfx.cinematic.miniblock.MiniBlockManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Cinematic VFX implementations for Fire Fragment abilities
 * Inspired by: Avatar (fire bending), Fullmetal Alchemist (transmutation circles)
 * 
 * Abilities:
 * - Flame Burst: Cast circle with phoenix feather inner pattern, fireball with spiral flame trail
 * - Fire Dome: Geodesic hexagon dome (hollow, no view blocking), flame edges with rising embers
 * - Inferno Maelstrom: Large rotating magic circle with flame-tornado center, spiral fire columns
 * - Phoenix Rebirth: Phoenix silhouette using flame particles, vertical magic circle with sun-symbol
 * - Eternal Flame: Sustained flame aura with pulsing magic circle
 */
public class FireAbilityVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    private final MiniBlockManager miniBlockManager;
    private final AnimationController animationController;
    
    public FireAbilityVFX(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.vfxEngine = plugin.getCinematicVFXEngine();
        this.miniBlockManager = plugin.getMiniBlockManager();
        this.animationController = new AnimationController();
    }
    
    /**
     * Flame Burst VFX
     * Cast circle with phoenix feather inner pattern, fireball with spiral flame trail
     * Rank scaling: 1-ring to 4-ring with phoenix pattern
     */
    public void flameBurst(Player player, Location castLocation, Vector direction, int rank) {
        // Create casting magic circle at player's feet
        MagicCircle castCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.FIRE,
            castLocation.clone().subtract(0, 0.5, 0),
            rank,
            player
        );
        
        // Spawn cast circle for 20 ticks (1 second)
        vfxEngine.spawnMagicCircle(castCircle, 20);
        
        // Play casting sound
        castLocation.getWorld().playSound(castLocation, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f + (rank * 0.1f));
        
        // Create fireball projectile with spiral flame trail
        Location fireballLoc = castLocation.clone().add(direction.clone().multiply(1.5));
        
        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = fireballLoc.clone();
            
            @Override
            public void run() {
                if (ticks >= 60 || currentLoc.getBlock().getType().isSolid()) {
                    // Impact explosion
                    createFlameExplosion(currentLoc, rank, player);
                    cancel();
                    return;
                }
                
                // Move fireball
                currentLoc.add(direction.clone().multiply(0.5));
                
                // Spiral flame trail
                double angle = ticks * 0.3;
                for (int i = 0; i < 3; i++) {
                    double spiralAngle = angle + (i * Math.PI * 2 / 3);
                    double offsetX = Math.cos(spiralAngle) * 0.3;
                    double offsetZ = Math.sin(spiralAngle) * 0.3;
                    
                    Location particleLoc = currentLoc.clone().add(offsetX, 0, offsetZ);
                    vfxEngine.spawnParticle(particleLoc, Particle.FLAME, 2, 0.05, 0.05, 0.05, 0.02, player);
                    
                    if (rank >= 3) {
                        vfxEngine.spawnParticle(particleLoc, Particle.SOUL_FIRE_FLAME, 1, 0.03, 0.03, 0.03, 0.01, player);
                    }
                }
                
                // Core fireball
                vfxEngine.spawnParticle(currentLoc, Particle.FLAME, 5, 0.1, 0.1, 0.1, 0.03, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Fire Dome VFX
     * Geodesic hexagon dome (hollow, no view blocking), flame edges with rising embers
     * Rank scaling: simple outline to phoenix-wing dome
     */
    public void fireDome(Player player, Location center, double radius, int durationTicks, int rank) {
        // Create ground magic circle
        MagicCircle groundCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.FIRE,
            center.clone().subtract(0, 0.5, 0),
            rank,
            player
        );
        
        vfxEngine.spawnMagicCircle(groundCircle, durationTicks);
        
        // Play activation sound
        center.getWorld().playSound(center, Sound.ITEM_FIRECHARGE_USE, 1.5f, 1.0f);
        
        // Create geodesic dome structure
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Geodesic dome edges (hexagon pattern)
                int segments = 6 + (rank * 2); // More segments at higher ranks
                double heightSegments = 4 + rank;
                
                for (int i = 0; i < segments; i++) {
                    double angle = (i * Math.PI * 2) / segments;
                    
                    // Vertical edges
                    for (double h = 0; h <= heightSegments; h++) {
                        double currentHeight = (h / heightSegments) * radius;
                        double currentRadius = radius * Math.sin(Math.acos(currentHeight / radius));
                        
                        double x = Math.cos(angle) * currentRadius;
                        double z = Math.sin(angle) * currentRadius;
                        
                        Location edgeLoc = center.clone().add(x, currentHeight, z);
                        vfxEngine.spawnParticle(edgeLoc, Particle.FLAME, 1, 0.05, 0.05, 0.05, 0.01, player);
                        
                        // Rising embers
                        if (ticks % 5 == 0 && Math.random() < 0.3) {
                            vfxEngine.spawnParticle(edgeLoc, Particle.LAVA, 1, 0.1, 0.2, 0.1, 0.05, player);
                        }
                    }
                    
                    // Horizontal rings
                    if (ticks % 3 == 0) {
                        for (double h = 0; h <= heightSegments; h += 2) {
                            double currentHeight = (h / heightSegments) * radius;
                            double currentRadius = radius * Math.sin(Math.acos(currentHeight / radius));
                            
                            double x = Math.cos(angle) * currentRadius;
                            double z = Math.sin(angle) * currentRadius;
                            
                            Location ringLoc = center.clone().add(x, currentHeight, z);
                            vfxEngine.spawnParticle(ringLoc, Particle.FLAME, 1, 0.03, 0.03, 0.03, 0.005, player);
                        }
                    }
                }
                
                // Phoenix wing pattern at rank 7+
                if (rank >= 7 && ticks % 10 == 0) {
                    createPhoenixWingPattern(center, radius, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Inferno Maelstrom VFX
     * Large rotating magic circle with flame-tornado center, spiral fire columns
     * Counter-rotating rings with phoenix rising (R7+)
     */
    public void infernoMaelstrom(Player player, Location center, double radius, int durationTicks, int rank) {
        // Create large ground magic circle
        MagicCircle maelstromCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.FIRE,
            center.clone().subtract(0, 0.5, 0),
            rank,
            player
        );
        
        vfxEngine.spawnMagicCircle(maelstromCircle, durationTicks);
        
        // Play dramatic sound
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        center.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 2.0f, 0.8f);
        
        // Create flame tornado and spiral columns
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks) {
                    cancel();
                    return;
                }
                
                double rotation = ticks * 0.1;
                
                // Central flame tornado
                int tornadoHeight = 5 + rank;
                for (int h = 0; h < tornadoHeight * 4; h++) {
                    double height = h * 0.25;
                    double tornadoRadius = 0.5 + (height * 0.1);
                    double angle = rotation + (height * 0.5);
                    
                    double x = Math.cos(angle) * tornadoRadius;
                    double z = Math.sin(angle) * tornadoRadius;
                    
                    Location tornadoLoc = center.clone().add(x, height, z);
                    vfxEngine.spawnParticle(tornadoLoc, Particle.FLAME, 2, 0.05, 0.05, 0.05, 0.02, player);
                    
                    if (rank >= 5 && h % 4 == 0) {
                        vfxEngine.spawnParticle(tornadoLoc, Particle.SOUL_FIRE_FLAME, 1, 0.03, 0.03, 0.03, 0.01, player);
                    }
                }
                
                // Spiral fire columns at cardinal points
                int columnCount = 4 + (rank / 2);
                for (int i = 0; i < columnCount; i++) {
                    double columnAngle = (i * Math.PI * 2 / columnCount) + rotation;
                    double columnRadius = radius * 0.7;
                    
                    double x = Math.cos(columnAngle) * columnRadius;
                    double z = Math.sin(columnAngle) * columnRadius;
                    
                    // Spiral upward
                    for (int h = 0; h < 3 + rank; h++) {
                        double spiralAngle = columnAngle + (h * 0.3);
                        double spiralOffset = 0.3;
                        
                        Location columnLoc = center.clone().add(
                            x + Math.cos(spiralAngle) * spiralOffset,
                            h * 0.5,
                            z + Math.sin(spiralAngle) * spiralOffset
                        );
                        
                        vfxEngine.spawnParticle(columnLoc, Particle.FLAME, 3, 0.1, 0.1, 0.1, 0.03, player);
                    }
                }
                
                // Phoenix rising at rank 7+
                if (rank >= 7 && ticks % 20 == 0) {
                    createPhoenixRising(center, tornadoHeight, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Phoenix Rebirth VFX
     * Phoenix silhouette using flame particles, vertical magic circle with sun-symbol
     */
    public void phoenixRebirth(Player player, Location location, int rank) {
        // Create vertical magic circle
        MagicCircle rebirthCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.FIRE,
            location.clone(),
            rank,
            player
        );
        
        vfxEngine.spawnMagicCircle(rebirthCircle, 60);
        
        // Play rebirth sounds
        location.getWorld().playSound(location, Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.5f);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.8f);
        
        // Create phoenix rising animation
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }
                
                double progress = ticks / 60.0;
                double height = progress * 5;
                
                // Phoenix body
                Location phoenixLoc = location.clone().add(0, height, 0);
                
                // Wings (direction-aware)
                org.bukkit.util.Vector direction = phoenixLoc.getDirection().normalize();
                org.bukkit.util.Vector perpendicular = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
                
                double wingSpan = 1.5 + (rank * 0.2);
                double wingAngle = Math.sin(ticks * 0.2) * 0.3;
                double wingBackOffset = 0.8;
                
                org.bukkit.util.Vector backVector = direction.clone().multiply(-wingBackOffset);
                
                for (int i = -1; i <= 1; i += 2) {
                    for (double w = 0; w < wingSpan; w += 0.2) {
                        org.bukkit.util.Vector wingOffset = backVector.clone()
                            .add(perpendicular.clone().multiply(i * w * Math.cos(wingAngle)));
                        double wingY = -w * 0.3;
                        
                        Location wingLoc = phoenixLoc.clone().add(wingOffset).add(0, wingY, 0);
                        vfxEngine.spawnParticle(wingLoc, Particle.FLAME, 2, 0.05, 0.05, 0.05, 0.02, player);
                        
                        if (rank >= 5) {
                            vfxEngine.spawnParticle(wingLoc, Particle.SOUL_FIRE_FLAME, 1, 0.03, 0.03, 0.03, 0.01, player);
                        }
                    }
                }
                
                // Tail feathers
                for (int t = 0; t < 3; t++) {
                    Location tailLoc = phoenixLoc.clone().add(0, -0.5 - (t * 0.3), 0);
                    vfxEngine.spawnParticle(tailLoc, Particle.FLAME, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                // Ember trail
                if (ticks % 3 == 0) {
                    vfxEngine.spawnParticle(phoenixLoc, Particle.LAVA, 3, 0.2, 0.2, 0.2, 0.05, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Eternal Flame VFX
     * Sustained flame aura with pulsing magic circle
     */
    public void eternalFlame(Player player, int durationTicks, int rank) {
        Location playerLoc = player.getLocation();
        
        // Create pulsing magic circle
        MagicCircle flameCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            FragmentType.FIRE,
            playerLoc.clone().subtract(0, 0.5, 0),
            rank,
            player
        );
        
        vfxEngine.spawnMagicCircle(flameCircle, durationTicks);
        
        // Sustained flame aura
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                
                Location currentLoc = player.getLocation();
                double radius = 1.5 + (rank * 0.2);
                
                // Pulsing flame ring
                double pulse = Math.sin(ticks * 0.1) * 0.3 + 1.0;
                int particleCount = 8 + (rank * 2);
                
                for (int i = 0; i < particleCount; i++) {
                    double angle = (i * Math.PI * 2 / particleCount) + (ticks * 0.05);
                    double x = Math.cos(angle) * radius * pulse;
                    double z = Math.sin(angle) * radius * pulse;
                    
                    Location flameLoc = currentLoc.clone().add(x, 1.0, z);
                    vfxEngine.spawnParticle(flameLoc, Particle.FLAME, 2, 0.05, 0.05, 0.05, 0.02, player);
                    
                    if (rank >= 5) {
                        vfxEngine.spawnParticle(flameLoc, Particle.SOUL_FIRE_FLAME, 1, 0.03, 0.03, 0.03, 0.01, player);
                    }
                }
                
                // Rising embers
                if (ticks % 5 == 0) {
                    vfxEngine.spawnParticle(currentLoc.clone().add(0, 0.5, 0), Particle.LAVA, 3, 0.3, 0.3, 0.3, 0.05, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    // Helper methods
    
    private void createFlameExplosion(Location location, int rank, Player player) {
        double radius = 1.5 + (rank * 0.3);
        
        // Explosion burst
        vfxEngine.getExplosionEffect().createExplosion(
            location,
            radius,
            Particle.FLAME,
            50 + (rank * 10),
            player
        );
        
        // Secondary lava burst
        vfxEngine.getExplosionEffect().createExplosion(
            location,
            radius * 0.7,
            Particle.LAVA,
            20 + (rank * 5),
            player
        );
        
        // Sound
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
    
    private void createPhoenixWingPattern(Location center, double radius, Player player) {
        double wingSpan = radius * 0.8;
        
        for (int side = -1; side <= 1; side += 2) {
            for (double w = 0; w < wingSpan; w += 0.3) {
                double x = side * w;
                double y = radius * 0.5 + Math.sin(w) * 0.5;
                
                Location wingLoc = center.clone().add(x, y, 0);
                vfxEngine.spawnParticle(wingLoc, Particle.SOUL_FIRE_FLAME, 2, 0.05, 0.05, 0.05, 0.02, player);
            }
        }
    }
    
    private void createPhoenixRising(Location center, double height, Player player) {
        for (double h = 0; h < height; h += 0.3) {
            double wingSpan = 1.0 + (h * 0.1);
            
            for (int side = -1; side <= 1; side += 2) {
                double x = side * wingSpan;
                Location phoenixLoc = center.clone().add(x, h, 0);
                vfxEngine.spawnParticle(phoenixLoc, Particle.SOUL_FIRE_FLAME, 1, 0.05, 0.05, 0.05, 0.02, player);
            }
        }
    }
}
