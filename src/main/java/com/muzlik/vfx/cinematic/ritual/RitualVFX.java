package com.muzlik.vfx.cinematic.ritual;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import com.muzlik.ritual.RitualStage;
import com.muzlik.ritual.RitualType;
import com.muzlik.vfx.cinematic.CinematicVFXEngine;
import com.muzlik.vfx.cinematic.MagicCircle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Cinematic VFX for ritual system
 * Provides enhanced visual effects for all ritual types with phase-based animations
 */
public class RitualVFX {
    
    private final FrostSMPPlugin plugin;
    private final CinematicVFXEngine vfxEngine;
    
    public RitualVFX(FrostSMPPlugin plugin, CinematicVFXEngine vfxEngine) {
        this.plugin = plugin;
        this.vfxEngine = vfxEngine;
    }
    
    /**
     * Helper method to spawn particles with fallback
     */
    private void spawnParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, Player player) {
        if (vfxEngine != null) {
            vfxEngine.spawnParticle(location, particle, count, offsetX, offsetY, offsetZ, speed, player);
        } else {
            // Fallback to direct world particle spawning
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }
    
    /**
     * Fragment Creation Ritual VFX
     * Three-layer magic circle system with elemental symbols
     */
    public void fragmentCreation(Player player, Location center, FragmentType fragmentType, RitualStage stage, int progressPercent) {
        // Skip if vfxEngine is not available
        if (vfxEngine == null) {
            plugin.getLogger().warning("❌ RitualVFX: vfxEngine is NULL!");
            return;
        }
        
        
        // Outer ring with elemental symbols (clockwise) - raised 0.5 blocks
        MagicCircle outerCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            fragmentType, center.clone().add(0, 0.4, 0), 8, player
        );
        
        // Middle ring with fragment-type runes (counter-clockwise)
        MagicCircle middleCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            fragmentType, center.clone().add(0, 0.5, 0), 6, player
        );
        
        // Inner ring with creation sigil (clockwise)
        MagicCircle innerCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            fragmentType, center.clone().add(0, 0.6, 0), 4, player
        );
        
        
        // Spawn vertical beam to sky (like ender dragon healing beam) - ALWAYS spawn during ritual
        createVerticalBeam(center, fragmentType, player);
        
        // Spawn circles based on stage
        switch (stage) {
            case CHARGING:
                vfxEngine.spawnMagicCircle(outerCircle, 20);
                if (progressPercent > 25) {
                    vfxEngine.spawnMagicCircle(middleCircle, 20);
                }
                if (progressPercent > 50) {
                    vfxEngine.spawnMagicCircle(innerCircle, 20);
                }
                break;
                
            case ACTIVATION:
                vfxEngine.spawnMagicCircle(outerCircle, 20);
                vfxEngine.spawnMagicCircle(middleCircle, 20);
                vfxEngine.spawnMagicCircle(innerCircle, 20);
                // Intensified particles
                createIntensifiedEffect(center, fragmentType, player);
                break;
                
            case COMPLETION:
                // Upward energy burst
                createCompletionBurst(center, fragmentType, player);
                break;
        }
    }
    
    /**
     * Fragment Changer Ritual VFX
     * Two-layer magic circle with transformation arrows
     */
    public void fragmentChanger(Player player, Location center, FragmentType oldFragment, FragmentType newFragment, RitualStage stage) {
        // Skip if vfxEngine is not available
        if (vfxEngine == null) {
            return;
        }
        
        // Outer ring (old fragment)
        MagicCircle outerCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            oldFragment, center.clone().subtract(0, 0.1, 0), 6, player
        );
        
        // Inner ring (new fragment)
        MagicCircle innerCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            newFragment, center.clone().add(0, 0.1, 0), 4, player
        );
        
        vfxEngine.spawnMagicCircle(outerCircle, 20);
        vfxEngine.spawnMagicCircle(innerCircle, 20);
        
        // Yin-yang symbol in center
        createYinYangSymbol(center, oldFragment, newFragment, player);
        
        // Particle streams connecting old/new
        if (stage == RitualStage.ACTIVATION || stage == RitualStage.COMPLETION) {
            createTransformationStreams(center, oldFragment, newFragment, player);
        }
    }
    
    /**
     * Rank Up Ritual VFX
     * Ascending magic circles at multiple heights
     */
    public void rankUp(Player player, Location center, FragmentType fragmentType, int currentRank, int targetRank, RitualStage stage) {
        // Skip if vfxEngine is not available
        if (vfxEngine == null) {
            return;
        }
        
        // Create ascending circles
        int circleCount = Math.min(targetRank, 5);
        for (int i = 0; i < circleCount; i++) {
            double height = i * 0.5;
            Location circleLocation = center.clone().add(0, height, 0);
            
            MagicCircle circle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
                fragmentType, circleLocation, currentRank + i, player
            );
            vfxEngine.spawnMagicCircle(circle, 20);
        }
        
        // Vertical energy streams connecting circles
        if (stage == RitualStage.ACTIVATION || stage == RitualStage.COMPLETION) {
            createVerticalEnergyStreams(center, circleCount, fragmentType, player);
        }
    }
    
    /**
     * Ability Expansion Ritual VFX
     * Large magic circle with ability-slot quadrants
     */
    public void abilityExpansion(Player player, Location center, FragmentType fragmentType, RitualStage stage) {
        // Skip if vfxEngine is not available
        if (vfxEngine == null) {
            return;
        }
        
        // Large magic circle
        MagicCircle mainCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
            fragmentType, center.clone(), 8, player
        );
        vfxEngine.spawnMagicCircle(mainCircle, 20);
        
        // Four quadrant circles (ability slots)
        double radius = 2.0;
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location quadrantLoc = center.clone().add(x, 0, z);
            MagicCircle quadrantCircle = vfxEngine.getMagicCircleFactory().createFragmentCircle(
                fragmentType, quadrantLoc, 3, player
            );
            vfxEngine.spawnMagicCircle(quadrantCircle, 20);
        }
        
        // Energy flow from outer edge to center
        if (stage == RitualStage.ACTIVATION) {
            createEnergyFlowToCenter(center, fragmentType, player);
        }
    }
    
    /**
     * Mastery Expansion Ritual VFX
     * Five concentric rings with mastery runes
     */
    public void masteryExpansion(Player player, Location center, FragmentType fragmentType, RitualStage stage) {
        // Skip if vfxEngine is not available
        if (vfxEngine == null) {
            return;
        }
        
        // Five concentric rings
        for (int i = 0; i < 5; i++) {
            double heightOffset = i * 0.2;
            Location ringLocation = center.clone().add(0, heightOffset, 0);
            
            MagicCircle ring = vfxEngine.getMagicCircleFactory().createFragmentCircle(
                fragmentType, ringLocation, 8 - i, player
            );
            vfxEngine.spawnMagicCircle(ring, 20);
        }
        
        // Pulsing energy waves
        if (stage == RitualStage.ACTIVATION) {
            createPulsingEnergyWaves(center, fragmentType, player);
        }
        
        // Ascending particle columns
        if (stage == RitualStage.ACTIVATION || stage == RitualStage.COMPLETION) {
            createAscendingColumns(center, fragmentType, player);
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Create vertical beam to sky (like ender dragon healing beam)
     */
    private void createVerticalBeam(Location center, FragmentType fragmentType, Player player) {
        // Beam goes from center up to sky (50 blocks high)
        double beamHeight = 50.0;
        int particlesPerBlock = 3;
        
        for (double y = 0; y < beamHeight; y += (1.0 / particlesPerBlock)) {
            Location beamLoc = center.clone().add(0, y, 0);
            
            // Use END_ROD particles for the beam effect (like dragon healing)
            spawnParticle(beamLoc, Particle.END_ROD, 1, 0.05, 0, 0.05, 0, player);
            
            // Add some glow particles for extra effect
            if (y % 2 == 0) {
                spawnParticle(beamLoc, Particle.GLOW, 1, 0.1, 0, 0.1, 0, player);
            }
        }
    }
    
    /**
     * Create pulse effect expanding outward from center
     */
    private void createPulseEffect(Location center, FragmentType fragmentType, Player player) {
        new BukkitRunnable() {
            double radius = 0;
            @Override
            public void run() {
                if (radius > 5.0) {
                    cancel();
                    return;
                }
                
                int points = 32;
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location pulseLoc = center.clone().add(x, 0.1, z);
                    spawnParticle(pulseLoc, Particle.END_ROD, 1, 0, 0, 0, 0, player);
                }
                
                radius += 0.5;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }
    
    /**
     * Create intensified glow effects
     */
    private void createIntensifiedEffect(Location center, FragmentType fragmentType, Player player) {
        // Faster rotation with additional particle layers
        for (int layer = 0; layer < 3; layer++) {
            double height = layer * 0.3;
            spawnParticle(center.clone().add(0, height, 0), Particle.GLOW, 10, 0.5, 0.1, 0.5, 0.05, player);
        }
    }
    
    /**
     * Create completion burst effect
     */
    private void createCompletionBurst(Location center, FragmentType fragmentType, Player player) {
        // Upward energy burst
        vfxEngine.getExplosionEffect().createExplosion(center, 3.0, Particle.END_ROD, 100, player);
        
        // Fragment materialization
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                double height = ticks * 0.1;
                spawnParticle(center.clone().add(0, height, 0), Particle.ENCHANTMENT_TABLE, 5, 0.2, 0.1, 0.2, 0.05, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.0f);
    }
    

    
    /**
     * Create yin-yang symbol
     */
    private void createYinYangSymbol(Location center, FragmentType oldFragment, FragmentType newFragment, Player player) {
        // Simple yin-yang representation with particles
        int points = 16;
        for (int i = 0; i < points; i++) {
            double angle = (i * Math.PI * 2 / points);
            double radius = 0.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location symbolLoc = center.clone().add(x, 0.5, z);
            
            // Half circle for each fragment
            if (i < points / 2) {
                spawnParticle(symbolLoc, Particle.SMOKE_LARGE, 1, 0, 0, 0, 0, player);
            } else {
                spawnParticle(symbolLoc, Particle.END_ROD, 1, 0, 0, 0, 0, player);
            }
        }
    }
    
    /**
     * Create transformation particle streams
     */
    private void createTransformationStreams(Location center, FragmentType oldFragment, FragmentType newFragment, Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                // Spiral streams
                double angle = ticks * 0.3;
                double radius = 1.0 + Math.sin(ticks * 0.2) * 0.3;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                Location streamLoc = center.clone().add(x, 0.5, z);
                spawnParticle(streamLoc, Particle.ENCHANTMENT_TABLE, 2, 0.1, 0.1, 0.1, 0.02, player);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create vertical energy streams
     */
    private void createVerticalEnergyStreams(Location center, int circleCount, FragmentType fragmentType, Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                for (int i = 0; i < circleCount; i++) {
                    double height = i * 0.5 + (ticks * 0.05);
                    spawnParticle(center.clone().add(0, height, 0), Particle.END_ROD, 3, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Create energy flow to center
     */
    private void createEnergyFlowToCenter(Location center, FragmentType fragmentType, Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }
                
                // Particles flowing inward
                double radius = 3.0 - (ticks * 0.1);
                int points = 16;
                
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location flowLoc = center.clone().add(x, 0.5, z);
                    spawnParticle(flowLoc, Particle.ENCHANTMENT_TABLE, 1, 0, 0, 0, 0, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create pulsing energy waves
     */
    private void createPulsingEnergyWaves(Location center, FragmentType fragmentType, Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                double radius = Math.sin(ticks * 0.3) * 2.0 + 2.5;
                int points = 24;
                
                for (int i = 0; i < points; i++) {
                    double angle = (i * Math.PI * 2 / points);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location waveLoc = center.clone().add(x, 0.5, z);
                    spawnParticle(waveLoc, Particle.GLOW, 2, 0.1, 0.1, 0.1, 0.02, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Create ascending particle columns
     */
    private void createAscendingColumns(Location center, FragmentType fragmentType, Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                // Five columns around the center
                for (int i = 0; i < 5; i++) {
                    double angle = (i * Math.PI * 2 / 5);
                    double radius = 1.5;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double height = ticks * 0.1;
                    
                    Location columnLoc = center.clone().add(x, height, z);
                    spawnParticle(columnLoc, Particle.END_ROD, 2, 0.05, 0.05, 0.05, 0.01, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
