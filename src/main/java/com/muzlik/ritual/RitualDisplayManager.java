package com.muzlik.ritual;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Manages the visual display for rituals:
 * - Floating fragment item with hologram name tag
 * - Particle beams pointing at the fragment (dark/purple energy beams)
 * - Timer display above the fragment
 */
public class RitualDisplayManager {
    
    private final JavaPlugin plugin;
    private final Map<UUID, RitualDisplay> activeDisplays;
    
    // Configuration
    private static final double FRAGMENT_HEIGHT = 2.5; // Height above ritual center
    private static final double BEAM_SOURCE_DISTANCE = 35.0; // Distance of beam sources from center (reduced from 100 for visibility)
    private static final double BEAM_SOURCE_HEIGHT = 15.0; // Height of beam sources above ground (reduced from 50)
    private static final int BEAM_COUNT = 6; // Number of beams in circle
    private static final double BEAM_TARGET_OFFSET = -0.5; // Lower the beam target by 0.5 blocks
    
    public RitualDisplayManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeDisplays = new HashMap<>();
    }
    
    /**
     * Create ritual display with floating fragment and particle beams
     * Magic circle complexity increases with fragment rank
     */
    public void createDisplay(Player player, Location center, FragmentType fragmentType, ItemStack fragmentItem, long durationMs, int fragmentRank) {
        // Remove any existing display for this player
        removeDisplay(player.getUniqueId());
        
        World world = center.getWorld();
        if (world == null) return;
        
        RitualDisplay display = new RitualDisplay();
        display.center = center.clone();
        display.fragmentType = fragmentType;
        display.fragmentRank = fragmentRank;
        display.startTime = System.currentTimeMillis();
        display.duration = durationMs;
        
        // 1. Create floating item (the fragment)
        Location itemLoc = center.clone().add(0, FRAGMENT_HEIGHT, 0);
        Item floatingItem = world.dropItem(itemLoc, fragmentItem);
        floatingItem.setPickupDelay(Integer.MAX_VALUE); // Cannot be picked up
        floatingItem.setGravity(false); // Float in place
        floatingItem.setVelocity(new Vector(0, 0, 0));
        floatingItem.setInvulnerable(true);
        floatingItem.setGlowing(true);
        floatingItem.setCustomName("§5§l" + fragmentType.getDisplayName() + " Fragment");
        floatingItem.setCustomNameVisible(true);
        floatingItem.setPersistent(true); // Prevent entity cleanup
        floatingItem.setUnlimitedLifetime(true); // Never despawn
        display.floatingItem = floatingItem;
        
        // 2. Calculate beam source positions (high up in a circle)
        display.beamSources = new ArrayList<>();
        for (int i = 0; i < BEAM_COUNT; i++) {
            double angle = (2 * Math.PI * i) / BEAM_COUNT;
            double x = center.getX() + BEAM_SOURCE_DISTANCE * Math.cos(angle);
            double z = center.getZ() + BEAM_SOURCE_DISTANCE * Math.sin(angle);
            Location beamSource = new Location(world, x, center.getY() + BEAM_SOURCE_HEIGHT, z);
            display.beamSources.add(beamSource);
        }
        
        // 3. Create timer hologram (ArmorStand with custom name)
        Location timerLoc = center.clone().add(0, FRAGMENT_HEIGHT + 0.8, 0);
        ArmorStand timerStand = (ArmorStand) world.spawnEntity(timerLoc, EntityType.ARMOR_STAND);
        timerStand.setVisible(false);
        timerStand.setGravity(false);
        timerStand.setInvulnerable(true);
        timerStand.setMarker(true);
        timerStand.setSmall(true);
        timerStand.setCustomNameVisible(true);
        timerStand.setCustomName("§7Ritual in progress...");
        display.timerHologram = timerStand;
        
        // 4. Create ENDER CRYSTAL BEAM SYSTEM
        createEnderCrystalBeams(display, world, itemLoc);
        
        // 5. Create MAGIC CIRCLE TEXTURE DISPLAY (rank-based)
        display.magicCircleDisplay = new MagicCircleDisplay(plugin);
        display.magicCircleDisplay.create(center, fragmentRank, 8.0); // 8 block diameter
        
        activeDisplays.put(player.getUniqueId(), display);
        
        // Start update task for timer and beams
        startDisplayUpdate(player.getUniqueId(), display);
        
    }
    
    /**
     * Create ender crystal beam system
     * Uses invisible armor stands as beam targets instead of dragon
     */
    private void createEnderCrystalBeams(RitualDisplay display, World world, Location targetLoc) {
        // Beam target should be BELOW the fragment location to almost touch it
        // Subtract 1.6 blocks to make beams point just below the fragment
        Location beamTargetLoc = targetLoc.clone().add(0, -1.6, 0);
        
        // Spawn invisible armor stand at adjusted location as beam target
        ArmorStand beamTarget = (ArmorStand) world.spawnEntity(beamTargetLoc, EntityType.ARMOR_STAND);
        beamTarget.setVisible(false);
        beamTarget.setGravity(false);
        beamTarget.setInvulnerable(true);
        beamTarget.setMarker(true);
        beamTarget.setSmall(true);
        beamTarget.setCustomNameVisible(false);
        
        // Store beam target for cleanup
        display.beamTarget = beamTarget;
        display.invisibleDragon = null; // We're not using a dragon anymore
        
        // Spawn end crystals at beam source positions
        display.enderCrystals = new ArrayList<>();
        for (Location beamSource : display.beamSources) {
            
            EnderCrystal crystal = (EnderCrystal) world.spawnEntity(beamSource, EntityType.ENDER_CRYSTAL);
            crystal.setShowingBottom(false); // No bedrock base
            crystal.setInvulnerable(true);
            
            // Set beam target to the adjusted location (lower than fragment)
            crystal.setBeamTarget(beamTargetLoc.toBlockLocation());
            
            display.enderCrystals.add(crystal);
            
        }
        
    }

    
    /**
     * Start display update task (timer + particle beams + ambient sounds)
     * OPTIMIZED: Reduced update frequency to minimize packet spam
     */
    private void startDisplayUpdate(UUID playerId, RitualDisplay display) {
        display.updateTask = new BukkitRunnable() {
            int tick = 0;
            
            @Override
            public void run() {
                if (!activeDisplays.containsKey(playerId)) {
                    cancel();
                    return;
                }
                
                // Update timer display ONLY every 20 ticks (1 second) - OPTIMIZED
                if (tick % 20 == 0) {
                    long elapsed = System.currentTimeMillis() - display.startTime;
                    long remaining = Math.max(0, display.duration - elapsed);
                    long minutes = (remaining / 1000) / 60;
                    long seconds = (remaining / 1000) % 60;
                    
                    String timerText = String.format("§5⚡ §f%d:%02d §5⚡", minutes, seconds);
                    if (display.timerHologram != null && !display.timerHologram.isDead()) {
                        display.timerHologram.setCustomName(timerText);
                    }
                }
                
                // Keep floating item in place ONLY every 40 ticks (2 seconds) - OPTIMIZED
                // Fragment doesn't move, so no need to update frequently
                if (tick % 40 == 0) {
                    Location targetLoc = display.center.clone().add(0, FRAGMENT_HEIGHT, 0);
                    if (display.floatingItem != null && !display.floatingItem.isDead()) {
                        display.floatingItem.teleport(targetLoc);
                        display.floatingItem.setVelocity(new Vector(0, 0, 0));
                    }
                }
                
                // Crystal beam targets are STATIC - set once, no need to update
                // Removed the crystal.setBeamTarget() loop that was spamming packets
                
                // Add visual effects around fragment and magic circles
                addRitualParticleEffects(display, tick);
                
                // Play ambient ritual sounds (dim, atmospheric)
                playAmbientSounds(display, tick);
                
                tick++;
            }
        };
        // OPTIMIZED: Update every 4 ticks is fine for sounds, but individual updates are throttled
        display.updateTask.runTaskTimer(plugin, 0L, 4L);
    }
    
    /**
     * Add visual particle effects around fragment and magic circles
     * Makes the ritual look more impressive and magical
     */
    private void addRitualParticleEffects(RitualDisplay display, int tick) {
        World world = display.center.getWorld();
        if (world == null) return;
        
        Location fragmentLoc = display.center.clone().add(0, FRAGMENT_HEIGHT, 0);
        Color fragmentColor = getFragmentColor(display.fragmentType);
        Particle.DustOptions dustOptions = new Particle.DustOptions(fragmentColor, 1.0f);
        
        // 1. Spiral particles rising around the fragment (every tick)
        double spiralAngle = tick * 0.2;
        double spiralRadius = 0.5;
        double spiralHeight = (tick % 40) * 0.1; // Rise up 4 blocks then reset
        Location spiralLoc = fragmentLoc.clone().add(
            spiralRadius * Math.cos(spiralAngle),
            spiralHeight - 2.0,
            spiralRadius * Math.sin(spiralAngle)
        );
        world.spawnParticle(Particle.REDSTONE, spiralLoc, 1, 0, 0, 0, 0, dustOptions);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, spiralLoc, 1, 0, 0, 0, 0.3);
        
        // 2. Pulsing glow around fragment (every 10 ticks)
        if (tick % 10 == 0) {
            double pulseSize = 0.3 + 0.2 * Math.sin(tick * 0.1);
            world.spawnParticle(Particle.REDSTONE, fragmentLoc, 8, pulseSize, pulseSize, pulseSize, 0, dustOptions);
            world.spawnParticle(Particle.END_ROD, fragmentLoc, 3, pulseSize, pulseSize, pulseSize, 0.02);
        }
        
        // 3. Floating runes around magic circle (every 5 ticks)
        if (tick % 5 == 0) {
            double runeRadius = 4.0;
            int runeCount = 8;
            for (int i = 0; i < runeCount; i++) {
                double angle = (2 * Math.PI * i / runeCount) + (tick * 0.05);
                Location runeLoc = display.center.clone().add(
                    runeRadius * Math.cos(angle),
                    0.3 + 0.1 * Math.sin(tick * 0.1 + i),
                    runeRadius * Math.sin(angle)
                );
                world.spawnParticle(Particle.ENCHANTMENT_TABLE, runeLoc, 2, 0.1, 0.1, 0.1, 0.2);
                world.spawnParticle(Particle.REDSTONE, runeLoc, 1, 0, 0, 0, 0, dustOptions);
            }
        }
        
        // 4. Energy particles flowing from magic circle to fragment (every 8 ticks)
        if (tick % 8 == 0) {
            double circleRadius = 3.5;
            int flowCount = 6;
            for (int i = 0; i < flowCount; i++) {
                double angle = (2 * Math.PI * i / flowCount) + (tick * 0.03);
                Location startLoc = display.center.clone().add(
                    circleRadius * Math.cos(angle),
                    0.1,
                    circleRadius * Math.sin(angle)
                );
                
                // Particle flows upward toward fragment
                Vector direction = fragmentLoc.toVector().subtract(startLoc.toVector()).normalize();
                Location flowLoc = startLoc.clone().add(direction.multiply(0.5));
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, flowLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        
        // 5. Ambient particles around the ritual area (every 3 ticks)
        if (tick % 3 == 0) {
            // Random particles in the ritual area
            double randomX = (Math.random() - 0.5) * 6;
            double randomZ = (Math.random() - 0.5) * 6;
            Location ambientLoc = display.center.clone().add(randomX, 0.1, randomZ);
            world.spawnParticle(Particle.PORTAL, ambientLoc, 1, 0, 0.2, 0, 0.1);
        }
        
        // 6. Rank-based additional effects (higher ranks = more particles)
        if (display.fragmentRank >= 5 && tick % 15 == 0) {
            // Outer ring of energy for high rank fragments
            double outerRadius = 5.5;
            int outerCount = 12;
            for (int i = 0; i < outerCount; i++) {
                double angle = (2 * Math.PI * i / outerCount) - (tick * 0.02);
                Location outerLoc = display.center.clone().add(
                    outerRadius * Math.cos(angle),
                    0.2,
                    outerRadius * Math.sin(angle)
                );
                world.spawnParticle(Particle.REDSTONE, outerLoc, 1, 0, 0, 0, 0, dustOptions);
                world.spawnParticle(Particle.SOUL, outerLoc, 1, 0, 0.1, 0, 0.01);
            }
        }
        
        // 7. Dramatic burst effect every 5 seconds
        if (tick % 100 == 0) {
            world.spawnParticle(Particle.EXPLOSION_LARGE, fragmentLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.REDSTONE, fragmentLoc, 30, 0.5, 0.5, 0.5, 0, dustOptions);
            world.spawnParticle(Particle.END_ROD, fragmentLoc, 15, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, fragmentLoc, 20, 0.4, 0.4, 0.4, 0.05);
        }
    }
    
    /**
     * Play dim, atmospheric ambient sounds during ritual
     * Sounds are quiet and mysterious to create atmosphere without being intrusive
     */
    private void playAmbientSounds(RitualDisplay display, int tick) {
        World world = display.center.getWorld();
        if (world == null) return;
        
        Location soundLoc = display.center.clone().add(0, FRAGMENT_HEIGHT, 0);
        
        // Base ambient hum - plays every 3 seconds (30 ticks at 10 tps = 60 game ticks)
        // Very quiet beacon ambient for mystical atmosphere
        if (tick % 30 == 0) {
            world.playSound(soundLoc, Sound.BLOCK_BEACON_AMBIENT, 0.15f, 0.5f);
        }
        
        // Enchanting whispers - plays every 5 seconds
        // Quiet enchanting table sounds for magical feel
        if (tick % 50 == 5) {
            world.playSound(soundLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.1f, 0.7f);
        }
        
        // Portal whoosh - plays every 8 seconds
        // Very dim portal sound for otherworldly atmosphere
        if (tick % 80 == 15) {
            world.playSound(soundLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.08f, 1.2f);
        }
        
        // Soul sand valley ambience - plays every 10 seconds
        // Adds depth and mystery
        if (tick % 100 == 25) {
            world.playSound(soundLoc, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 0.12f, 0.8f);
        }
        
        // Occasional energy pulse - plays every 15 seconds
        // Subtle conduit pulse for energy feel
        if (tick % 150 == 40) {
            world.playSound(soundLoc, Sound.BLOCK_CONDUIT_AMBIENT, 0.1f, 0.6f);
        }
        
        // Fragment-specific accent sound - plays every 20 seconds
        if (tick % 200 == 60) {
            playFragmentAccentSound(world, soundLoc, display.fragmentType);
        }
    }
    
    /**
     * Play a fragment-specific accent sound
     */
    private void playFragmentAccentSound(World world, Location loc, FragmentType type) {
        float volume = 0.12f; // Keep it dim
        
        switch (type) {
            case FIRE -> world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, volume, 0.6f);
            case WATER -> world.playSound(loc, Sound.AMBIENT_UNDERWATER_LOOP, volume, 1.0f);
            case AIR -> world.playSound(loc, Sound.ENTITY_PHANTOM_FLAP, volume, 1.5f);
            case DARK -> world.playSound(loc, Sound.AMBIENT_BASALT_DELTAS_MOOD, volume, 0.7f);
            case LIGHT -> world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, volume, 1.2f);
            case VOID -> world.playSound(loc, Sound.AMBIENT_WARPED_FOREST_MOOD, volume, 0.5f);
            case STORM -> world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, volume * 0.5f, 2.0f);
            case DRAGON -> world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_AMBIENT, volume * 0.3f, 1.5f);
            default -> world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, volume, 0.8f);
        }
    }
    
    /**
     * Draw particle beams from beam sources to the fragment
     * OPTIMIZED: Reduced particle count for better performance
     */
    private void drawParticleBeams(RitualDisplay display, Location target, int tick) {
        World world = target.getWorld();
        if (world == null) return;
        
        // Get fragment-specific color
        Color beamColor = getFragmentColor(display.fragmentType);
        Particle.DustOptions dustOptions = new Particle.DustOptions(beamColor, 1.2f);
        Particle.DustOptions dustOptionsSmall = new Particle.DustOptions(beamColor, 0.6f);
        
        for (int i = 0; i < display.beamSources.size(); i++) {
            Location source = display.beamSources.get(i);
            
            // Animate beam - particles flow from source to target
            double animOffset = (tick * 0.1 + i * 0.5) % 1.0;
            
            // Draw beam line with particles
            Vector direction = target.toVector().subtract(source.toVector());
            double distance = direction.length();
            direction.normalize();
            
            // OPTIMIZED: Reduced particle count from 2 per block to 1 per block
            int particleCount = (int) distance;
            for (int p = 0; p < particleCount; p++) {
                double progress = (p + animOffset) / particleCount;
                Location particleLoc = source.clone().add(direction.clone().multiply(distance * progress));
                
                // Main beam particles
                world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0, dustOptions);
                
                // OPTIMIZED: Reduced soul fire particles from every 3rd to every 5th
                if (p % 5 == 0) {
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
            
            // Spawn particles at beam source (reduced frequency)
            if (tick % 10 == i % 10) {
                world.spawnParticle(Particle.REDSTONE, source, 3, 0.2, 0.2, 0.2, 0, dustOptionsSmall);
                world.spawnParticle(Particle.PORTAL, source, 2, 0.15, 0.15, 0.15, 0.1);
            }
        }
        
        // Spawn particles at target (fragment) - reduced count
        world.spawnParticle(Particle.REDSTONE, target, 2, 0.15, 0.15, 0.15, 0, dustOptions);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, target, 3, 0.4, 0.4, 0.4, 0.5);
        
        // Occasional burst at fragment (reduced frequency)
        if (tick % 30 == 0) {
            world.spawnParticle(Particle.PORTAL, target, 15, 0.25, 0.25, 0.25, 0.5);
        }
    }
    
    /**
     * Draw rank-based magic circles on the ground
     * Complexity increases with rank:
     * - Rank 1-2: Single circle
     * - Rank 3-4: Double circle with runes
     * - Rank 5-6: Triple circle with geometric patterns + 2 side circles
     * - Rank 7-8: Quad circle with complex sacred geometry + 4 side circles
     */
    private void drawMagicCircles(RitualDisplay display, int tick) {
        World world = display.center.getWorld();
        if (world == null) return;
        
        Location center = display.center.clone();
        Color circleColor = getFragmentColor(display.fragmentType);
        Particle.DustOptions dustOptions = new Particle.DustOptions(circleColor, 1.0f);
        
        int rank = display.fragmentRank;
        double rotationSpeed = 0.02; // Rotation speed for animated circles
        double rotation = tick * rotationSpeed;
        
        // === MAIN CENTRAL CIRCLE ===
        
        // Base circle (always present)
        drawCircle(world, center, 3.0, dustOptions, rotation, 50);
        
        // Rank 2+: Add inner circle
        if (rank >= 2) {
            drawCircle(world, center, 2.0, dustOptions, -rotation * 1.5, 35);
        }
        
        // Rank 3+: Add runes around outer circle
        if (rank >= 3) {
            drawRuneCircle(world, center, 3.5, dustOptions, rotation * 0.5, 8);
        }
        
        // Rank 4+: Add middle geometric pattern
        if (rank >= 4) {
            drawGeometricPattern(world, center, 2.5, dustOptions, rotation, 6);
        }
        
        // Rank 5+: Add outer circle + 2 SIDE CIRCLES
        if (rank >= 5) {
            drawCircle(world, center, 4.0, dustOptions, rotation * 0.8, 60);
            
            // Add 2 side circles (left and right)
            Location leftCircle = center.clone().add(-6, 0, 0);
            Location rightCircle = center.clone().add(6, 0, 0);
            drawCircle(world, leftCircle, 1.5, dustOptions, -rotation, 30);
            drawCircle(world, rightCircle, 1.5, dustOptions, -rotation, 30);
        }
        
        // Rank 6+: Add complex inner geometry
        if (rank >= 6) {
            drawStarPattern(world, center, 1.5, dustOptions, -rotation * 2, 5);
        }
        
        // Rank 7+: Add sacred geometry layer + 4 SIDE CIRCLES
        if (rank >= 7) {
            drawSacredGeometry(world, center, 3.5, dustOptions, rotation * 0.3, 12);
            
            // Add 4 side circles (cardinal directions)
            Location northCircle = center.clone().add(0, 0, -6);
            Location southCircle = center.clone().add(0, 0, 6);
            drawCircle(world, northCircle, 1.5, dustOptions, rotation, 30);
            drawCircle(world, southCircle, 1.5, dustOptions, rotation, 30);
        }
        
        // Rank 8: Add ultimate complexity
        if (rank >= 8) {
            drawCircle(world, center, 4.5, dustOptions, -rotation * 1.2, 70);
            drawComplexRunes(world, center, 4.2, dustOptions, rotation * 0.7, 16);
        }
        
        // Center glow (intensity increases with rank)
        int glowParticles = Math.min(rank * 2, 12); // Reduced from 16
        world.spawnParticle(Particle.REDSTONE, center.clone().add(0, 0.1, 0), 
            glowParticles, 0.3, 0.05, 0.3, 0, dustOptions);
    }
    
    /**
     * Draw a circle of particles
     */
    private void drawCircle(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i / points) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(world, x, center.getY() + 0.1, z);
            world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0, dust);
        }
    }
    
    /**
     * Draw runes around a circle
     */
    private void drawRuneCircle(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int runeCount) {
        for (int i = 0; i < runeCount; i++) {
            double angle = (2 * Math.PI * i / runeCount) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location runeLoc = new Location(world, x, center.getY() + 0.1, z);
            
            // Draw small rune symbol (cross pattern)
            world.spawnParticle(Particle.REDSTONE, runeLoc, 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0.1, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(-0.1, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0, 0, 0.1), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0, 0, -0.1), 1, 0, 0, 0, 0, dust);
        }
    }
    
    /**
     * Draw geometric pattern (hexagon/pentagon)
     */
    private void drawGeometricPattern(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int sides) {
        for (int i = 0; i < sides; i++) {
            double angle1 = (2 * Math.PI * i / sides) + rotation;
            double angle2 = (2 * Math.PI * (i + 1) / sides) + rotation;
            
            double x1 = center.getX() + radius * Math.cos(angle1);
            double z1 = center.getZ() + radius * Math.sin(angle1);
            double x2 = center.getX() + radius * Math.cos(angle2);
            double z2 = center.getZ() + radius * Math.sin(angle2);
            
            // Draw line between points
            drawLine(world, new Location(world, x1, center.getY() + 0.1, z1),
                    new Location(world, x2, center.getY() + 0.1, z2), dust, 10);
        }
    }
    
    /**
     * Draw star pattern
     */
    private void drawStarPattern(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int points) {
        for (int i = 0; i < points; i++) {
            double angle1 = (2 * Math.PI * i / points) + rotation;
            double angle2 = (2 * Math.PI * ((i + 2) % points) / points) + rotation;
            
            double x1 = center.getX() + radius * Math.cos(angle1);
            double z1 = center.getZ() + radius * Math.sin(angle1);
            double x2 = center.getX() + radius * Math.cos(angle2);
            double z2 = center.getZ() + radius * Math.sin(angle2);
            
            drawLine(world, new Location(world, x1, center.getY() + 0.1, z1),
                    new Location(world, x2, center.getY() + 0.1, z2), dust, 8);
        }
    }
    
    /**
     * Draw sacred geometry (flower of life pattern)
     */
    private void drawSacredGeometry(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int petals) {
        for (int i = 0; i < petals; i++) {
            double angle = (2 * Math.PI * i / petals) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location petalCenter = new Location(world, x, center.getY() + 0.1, z);
            
            // Draw small circle at each petal position
            drawCircle(world, petalCenter, 0.5, dust, -rotation, 12);
        }
    }
    
    /**
     * Draw complex runes for highest ranks
     */
    private void drawComplexRunes(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int runeCount) {
        for (int i = 0; i < runeCount; i++) {
            double angle = (2 * Math.PI * i / runeCount) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location runeLoc = new Location(world, x, center.getY() + 0.1, z);
            
            // Draw complex rune (diamond pattern)
            world.spawnParticle(Particle.REDSTONE, runeLoc, 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0.15, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(-0.15, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0, 0, 0.15), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0, 0, -0.15), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.ENCHANTMENT_TABLE, runeLoc, 2, 0.1, 0.1, 0.1, 0);
        }
    }
    
    /**
     * Draw a line between two points with particles
     */
    private void drawLine(World world, Location start, Location end, Particle.DustOptions dust, int points) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();
        
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            Location particleLoc = start.clone().add(direction.clone().multiply(length * progress));
            world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0, dust);
        }
    }
    
    /**
     * Get color for fragment type (dark/purple theme)
     */
    private Color getFragmentColor(FragmentType type) {
        return switch (type) {
            case FIRE -> Color.fromRGB(139, 0, 0); // Dark red
            case WATER -> Color.fromRGB(0, 0, 139); // Dark blue
            case AIR -> Color.fromRGB(70, 70, 70); // Dark gray
            case DARK -> Color.fromRGB(30, 0, 50); // Very dark purple
            case LIGHT -> Color.fromRGB(100, 100, 50); // Dark gold
            case VOID -> Color.fromRGB(20, 0, 30); // Almost black purple
            case STORM -> Color.fromRGB(30, 30, 80); // Dark blue-purple
            case DRAGON -> Color.fromRGB(80, 0, 80); // Dark magenta
            case TIME -> Color.fromRGB(180, 180, 0); // Dark yellow
            case LUCK -> Color.fromRGB(0, 100, 0); // Dark green
            default -> Color.fromRGB(30, 0, 50); // Default dark purple
        };
    }
    
    /**
     * Remove ritual display
     */
    public void removeDisplay(UUID playerId) {
        RitualDisplay display = activeDisplays.remove(playerId);
        if (display == null) return;
        
        // Cancel update task
        if (display.updateTask != null) {
            display.updateTask.cancel();
        }
        
        // Remove floating item
        if (display.floatingItem != null && !display.floatingItem.isDead()) {
            display.floatingItem.remove();
        }
        
        // Remove timer hologram
        if (display.timerHologram != null && !display.timerHologram.isDead()) {
            display.timerHologram.remove();
        }
        
        // Remove ender crystals
        if (display.enderCrystals != null) {
            for (EnderCrystal crystal : display.enderCrystals) {
                if (crystal != null && !crystal.isDead()) {
                    crystal.remove();
                }
            }
        }
        
        // Remove beam target armor stand
        if (display.beamTarget != null && !display.beamTarget.isDead()) {
            display.beamTarget.remove();
        }
        
        // Remove invisible dragon (legacy, shouldn't exist but check anyway)
        if (display.invisibleDragon != null && !display.invisibleDragon.isDead()) {
            display.invisibleDragon.remove();
        }
        
        // Remove magic circle display
        if (display.magicCircleDisplay != null) {
            display.magicCircleDisplay.remove();
        }
        
    }
    
    /**
     * Get the floating item for a ritual (to drop on completion)
     */
    public Item getFloatingItem(UUID playerId) {
        RitualDisplay display = activeDisplays.get(playerId);
        return display != null ? display.floatingItem : null;
    }
    
    /**
     * Complete ritual - drop the fragment on ground with dramatic completion sounds
     */
    public void completeAndDropFragment(UUID playerId) {
        RitualDisplay display = activeDisplays.get(playerId);
        if (display == null) {
            plugin.getLogger().warning("⚠️ completeAndDropFragment: No display found for player " + playerId);
            return;
        }
        
        // Get the floating item before removing display
        Item floatingItem = display.floatingItem;
        FragmentType fragmentType = display.fragmentType;
        Location center = display.center;
        
        // Play completion sound sequence
        playCompletionSounds(center, fragmentType);
        
        // Remove all display elements EXCEPT the floating item
        if (display.updateTask != null) {
            display.updateTask.cancel();
            display.updateTask = null;
        }
        
        // Remove timer hologram
        if (display.timerHologram != null && !display.timerHologram.isDead()) {
            display.timerHologram.remove();
        }
        
        // Remove ender crystals
        if (display.enderCrystals != null) {
            for (EnderCrystal crystal : display.enderCrystals) {
                if (crystal != null && !crystal.isDead()) {
                    crystal.remove();
                }
            }
        }
        
        // Remove beam target armor stand
        if (display.beamTarget != null && !display.beamTarget.isDead()) {
            display.beamTarget.remove();
        }
        
        // Remove invisible dragon (legacy, shouldn't exist but check anyway)
        if (display.invisibleDragon != null && !display.invisibleDragon.isDead()) {
            display.invisibleDragon.remove();
        }
        
        // Remove magic circle display
        if (display.magicCircleDisplay != null) {
            display.magicCircleDisplay.remove();
        }
        
        // Now drop the fragment to the ground
        if (floatingItem != null && !floatingItem.isDead()) {
            
            // Clear the reference so it won't be removed by cleanup
            display.floatingItem = null;
            
            // Configure the item for pickup
            floatingItem.setGravity(true); // Enable gravity so it falls
            floatingItem.setPickupDelay(0); // Allow pickup immediately
            floatingItem.setGlowing(true);
            floatingItem.setInvulnerable(false); // Allow normal item behavior
            floatingItem.setCustomName("§5§l⚡ " + fragmentType.getDisplayName() + " Fragment §5§l⚡");
            floatingItem.setCustomNameVisible(true);
            floatingItem.setPersistent(true); // Prevent entity cleanup
            floatingItem.setUnlimitedLifetime(true); // Never despawn
            
            // Give it a small upward velocity for dramatic effect
            floatingItem.setVelocity(new Vector(0, 0.3, 0));
            
        } else {
            // Floating item was removed or doesn't exist - create a new one
            plugin.getLogger().warning("⚠️ Floating item was null or dead, creating new fragment item...");
            
            if (center != null && center.getWorld() != null && fragmentType != null) {
                // Create a new fragment item and drop it
                ItemStack fragmentItem = com.muzlik.texture.TextureItemBuilder.createFragmentItem(fragmentType);
                if (fragmentItem != null) {
                    Location dropLoc = center.clone().add(0, FRAGMENT_HEIGHT, 0);
                    Item droppedItem = center.getWorld().dropItem(dropLoc, fragmentItem);
                    droppedItem.setPickupDelay(0);
                    droppedItem.setGlowing(true);
                    droppedItem.setCustomName("§5§l⚡ " + fragmentType.getDisplayName() + " Fragment §5§l⚡");
                    droppedItem.setCustomNameVisible(true);
                    droppedItem.setPersistent(true); // Prevent entity cleanup
                    droppedItem.setUnlimitedLifetime(true); // Never despawn
                    droppedItem.setVelocity(new Vector(0, 0.3, 0));
                    
                }
            }
        }
        
        activeDisplays.remove(playerId);
    }
    
    /**
     * Play dramatic completion sound sequence
     * Creates a satisfying, triumphant sound when ritual completes
     */
    private void playCompletionSounds(Location center, FragmentType fragmentType) {
        if (center == null || center.getWorld() == null) return;
        
        World world = center.getWorld();
        Location soundLoc = center.clone().add(0, FRAGMENT_HEIGHT, 0);
        
        // Main completion sound - triumphant level up
        world.playSound(soundLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        
        // Beacon activation for magical completion feel
        world.playSound(soundLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        
        // End portal eye placement for mystical resonance
        world.playSound(soundLoc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.7f, 1.0f);
        
        // Totem of undying for epic feel (quieter)
        world.playSound(soundLoc, Sound.ITEM_TOTEM_USE, 0.4f, 1.5f);
        
        // Amethyst chime for magical sparkle
        world.playSound(soundLoc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 0.6f, 1.3f);
        
        // Fragment-specific completion accent
        playFragmentCompletionSound(world, soundLoc, fragmentType);
        
        // Delayed secondary sounds for layered effect
        new BukkitRunnable() {
            @Override
            public void run() {
                world.playSound(soundLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 1.5f);
                world.playSound(soundLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 0.5f);
            }
        }.runTaskLater(plugin, 5L);
        
        // Final resonance
        new BukkitRunnable() {
            @Override
            public void run() {
                world.playSound(soundLoc, Sound.BLOCK_BEACON_DEACTIVATE, 0.4f, 1.5f);
            }
        }.runTaskLater(plugin, 15L);
    }
    
    /**
     * Play fragment-specific completion sound
     */
    private void playFragmentCompletionSound(World world, Location loc, FragmentType type) {
        float volume = 0.5f;
        
        switch (type) {
            case FIRE -> world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, volume, 1.2f);
            case WATER -> world.playSound(loc, Sound.ENTITY_DOLPHIN_SPLASH, volume, 1.0f);
            case AIR -> world.playSound(loc, Sound.ENTITY_PHANTOM_DEATH, volume * 0.6f, 1.5f);
            case DARK -> world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, volume * 0.3f, 1.5f);
            case LIGHT -> world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, volume, 1.0f);
            case VOID -> world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, volume, 0.8f);
            case STORM -> world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, volume * 0.6f, 1.3f);
            case DRAGON -> world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, volume * 0.4f, 1.5f);
            default -> world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, volume, 1.0f);
        }
    }
    
    /**
     * Cleanup all displays (on plugin disable)
     */
    public void cleanup() {
        for (UUID playerId : new ArrayList<>(activeDisplays.keySet())) {
            removeDisplay(playerId);
        }
        activeDisplays.clear();
    }
    
    /**
     * Check if player has active display
     */
    public boolean hasDisplay(UUID playerId) {
        return activeDisplays.containsKey(playerId);
    }
    
    /**
     * Inner class to hold display entities
     */
    private static class RitualDisplay {
        Location center;
        FragmentType fragmentType;
        int fragmentRank; // Rank determines magic circle complexity
        long startTime;
        long duration;
        Item floatingItem;
        List<Location> beamSources;
        ArmorStand timerHologram;
        BukkitRunnable updateTask;
        
        // Ender crystal beam system
        EnderDragon invisibleDragon;
        List<EnderCrystal> enderCrystals;
        ArmorStand beamTarget; // Armor stand used as beam target
        
        // Magic circle texture display
        MagicCircleDisplay magicCircleDisplay;
    }
}
