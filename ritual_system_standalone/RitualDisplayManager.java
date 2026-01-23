package your.plugin.ritual;

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
 * Handles all the fancy visual stuff for rituals
 * 
 * This was a pain to get working right - the ender crystal beams kept breaking
 * But now it looks pretty cool with the floating crystal and particle effects
 * 
 * Features:
 * - Floating crystal item that glows and spins
 * - Ender crystal beams from all directions (looks epic)
 * - Magic circles with particles on the ground
 * - Timer hologram so players know how long is left
 */
public class RitualDisplayManager {
    
    private final JavaPlugin plugin;
    private final Map<UUID, RitualDisplay> activeDisplays;
    
    // Config for the visual display - tweak these if you want
    private static final double CRYSTAL_HEIGHT = 3.5; // how high the crystal floats
    private static final double BEAM_SOURCE_DISTANCE = 25.0; // how far away the beam crystals are
    private static final double BEAM_SOURCE_HEIGHT = 15.0; // how high up the beam crystals are
    private static final int BEAM_COUNT = 6; // number of beams (6 looks good)
    
    public RitualDisplayManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeDisplays = new HashMap<>();
    }
    
    /**
     * Create the whole visual display - this is where the magic happens
     */
    public void createDisplay(Player player, Location center, CrystalType crystalType, ItemStack crystalItem, long durationMs) {
        // Clean up any old display first
        removeDisplay(player.getUniqueId());
        
        World world = center.getWorld();
        if (world == null) return;
        
        RitualDisplay display = new RitualDisplay();
        display.center = center.clone();
        display.crystalType = crystalType;
        display.startTime = System.currentTimeMillis();
        display.duration = durationMs;
        
        // 1. Spawn the floating crystal item
        Location itemLoc = center.clone().add(0, CRYSTAL_HEIGHT, 0);
        Item floatingItem = world.dropItem(itemLoc, crystalItem);
        floatingItem.setPickupDelay(Integer.MAX_VALUE); // can't be picked up
        floatingItem.setGravity(false); // stays floating
        floatingItem.setVelocity(new Vector(0, 0, 0));
        floatingItem.setInvulnerable(true);
        floatingItem.setGlowing(true); // makes it glow
        floatingItem.setCustomName("§5§l" + crystalType.getDisplayName());
        floatingItem.setCustomNameVisible(true);
        floatingItem.setPersistent(true); // don't let it despawn
        floatingItem.setUnlimitedLifetime(true);
        display.floatingItem = floatingItem;
        
        // 2. Figure out where to put the beam crystals (in a circle around the ritual)
        display.beamSources = new ArrayList<>();
        for (int i = 0; i < BEAM_COUNT; i++) {
            double angle = (2 * Math.PI * i) / BEAM_COUNT;
            double x = center.getX() + BEAM_SOURCE_DISTANCE * Math.cos(angle);
            double z = center.getZ() + BEAM_SOURCE_DISTANCE * Math.sin(angle);
            Location beamSource = new Location(world, x, center.getY() + BEAM_SOURCE_HEIGHT, z);
            display.beamSources.add(beamSource);
        }
        
        // 3. Create the timer display above the crystal
        Location timerLoc = center.clone().add(0, CRYSTAL_HEIGHT + 0.8, 0);
        ArmorStand timerStand = (ArmorStand) world.spawnEntity(timerLoc, EntityType.ARMOR_STAND);
        timerStand.setVisible(false); // invisible armor stand
        timerStand.setGravity(false);
        timerStand.setInvulnerable(true);
        timerStand.setMarker(true); // no collision
        timerStand.setSmall(true);
        timerStand.setCustomNameVisible(true);
        timerStand.setCustomName("§7Ritual in progress...");
        display.timerHologram = timerStand;
        
        // 4. Create the ender crystal beam system (this is the cool part)
        createEnderCrystalBeams(display, world, itemLoc);
        
        activeDisplays.put(player.getUniqueId(), display);
        
        // Start the update loop for animations and timer
        startDisplayUpdate(player.getUniqueId(), display);
    }
    
    /**
     * Set up the ender crystal beams - this took forever to get right
     */
    private void createEnderCrystalBeams(RitualDisplay display, World world, Location targetLoc) {
        // The beam target needs to be a bit below the crystal or it looks weird
        Location beamTargetLoc = targetLoc.clone().add(0, -1.6, 0);
        
        // Spawn invisible armor stand as the beam target
        ArmorStand beamTarget = (ArmorStand) world.spawnEntity(beamTargetLoc, EntityType.ARMOR_STAND);
        beamTarget.setVisible(false);
        beamTarget.setGravity(false);
        beamTarget.setInvulnerable(true);
        beamTarget.setMarker(true);
        beamTarget.setSmall(true);
        beamTarget.setCustomNameVisible(false);
        
        display.beamTarget = beamTarget;
        
        // Spawn ender crystals at each beam source position
        display.enderCrystals = new ArrayList<>();
        for (Location beamSource : display.beamSources) {
            EnderCrystal crystal = (EnderCrystal) world.spawnEntity(beamSource, EntityType.ENDER_CRYSTAL);
            crystal.setShowingBottom(false); // no ugly bedrock base
            crystal.setInvulnerable(true);
            
            // Point the beam at our target
            crystal.setBeamTarget(beamTargetLoc.toBlockLocation());
            
            display.enderCrystals.add(crystal);
        }
    }
    
    /**
     * Start display update task
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
                
                // Update timer display every second
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
                
                // Keep floating item in place
                if (tick % 40 == 0) {
                    Location targetLoc = display.center.clone().add(0, CRYSTAL_HEIGHT, 0);
                    if (display.floatingItem != null && !display.floatingItem.isDead()) {
                        display.floatingItem.teleport(targetLoc);
                        display.floatingItem.setVelocity(new Vector(0, 0, 0));
                    }
                }
                
                // Add visual effects
                addRitualParticleEffects(display, tick);
                
                // Play ambient sounds
                playAmbientSounds(display, tick);
                
                tick++;
            }
        };
        display.updateTask.runTaskTimer(plugin, 0L, 4L);
    }
    
    /**
     * Add visual particle effects around crystal and magic circles
     */
    private void addRitualParticleEffects(RitualDisplay display, int tick) {
        World world = display.center.getWorld();
        if (world == null) return;
        
        Location crystalLoc = display.center.clone().add(0, CRYSTAL_HEIGHT, 0);
        Color crystalColor = getCrystalColor(display.crystalType);
        Particle.DustOptions dustOptions = new Particle.DustOptions(crystalColor, 1.0f);
        
        // 1. Spiral particles rising around the crystal
        double spiralAngle = tick * 0.2;
        double spiralRadius = 0.5;
        double spiralHeight = (tick % 40) * 0.1; // Rise up 4 blocks then reset
        Location spiralLoc = crystalLoc.clone().add(
            spiralRadius * Math.cos(spiralAngle),
            spiralHeight - 2.0,
            spiralRadius * Math.sin(spiralAngle)
        );
        world.spawnParticle(Particle.REDSTONE, spiralLoc, 1, 0, 0, 0, 0, dustOptions);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, spiralLoc, 1, 0, 0, 0, 0.3);
        
        // 2. Pulsing glow around crystal
        if (tick % 10 == 0) {
            double pulseSize = 0.3 + 0.2 * Math.sin(tick * 0.1);
            world.spawnParticle(Particle.REDSTONE, crystalLoc, 8, pulseSize, pulseSize, pulseSize, 0, dustOptions);
            world.spawnParticle(Particle.END_ROD, crystalLoc, 3, pulseSize, pulseSize, pulseSize, 0.02);
        }
        
        // 3. Magic circle on the ground
        if (tick % 5 == 0) {
            drawMagicCircle(display, tick);
        }
        
        // 4. Energy particles flowing from circle to crystal
        if (tick % 8 == 0) {
            double circleRadius = 3.5;
            int flowCount = 6;
            for (int i = 0; i < flowCount; i++) {
                double angle = (2 * Math.PI * i / flowCount) + (tick * 0.03);
                Location startLoc = display.center.clone().add(
                    circleRadius * Math.cos(angle),
                    1.1,
                    circleRadius * Math.sin(angle)
                );
                
                // Particle flows upward toward crystal
                Vector direction = crystalLoc.toVector().subtract(startLoc.toVector()).normalize();
                Location flowLoc = startLoc.clone().add(direction.multiply(0.5));
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, flowLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        
        // 5. Dramatic burst effect every 5 seconds
        if (tick % 100 == 0) {
            world.spawnParticle(Particle.EXPLOSION_LARGE, crystalLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.REDSTONE, crystalLoc, 30, 0.5, 0.5, 0.5, 0, dustOptions);
            world.spawnParticle(Particle.END_ROD, crystalLoc, 15, 0.3, 0.3, 0.3, 0.1);
        }
    }
    
    /**
     * Draw magic circle on the ground
     */
    private void drawMagicCircle(RitualDisplay display, int tick) {
        World world = display.center.getWorld();
        if (world == null) return;
        
        Location center = display.center.clone();
        Color circleColor = getCrystalColor(display.crystalType);
        Particle.DustOptions dustOptions = new Particle.DustOptions(circleColor, 1.0f);
        
        double rotationSpeed = 0.02;
        double rotation = tick * rotationSpeed;
        
        // Main circle
        drawCircle(world, center, 3.0, dustOptions, rotation, 50);
        
        // Inner circle
        drawCircle(world, center, 2.0, dustOptions, -rotation * 1.5, 35);
        
        // Runes around outer circle
        drawRuneCircle(world, center, 3.5, dustOptions, rotation * 0.5, 8);
        
        // Center glow
        world.spawnParticle(Particle.REDSTONE, center.clone().add(0, 1.1, 0), 
            6, 0.3, 0.05, 0.3, 0, dustOptions);
    }
    
    /**
     * Draw a circle of particles
     */
    private void drawCircle(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i / points) + rotation;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(world, x, center.getY() + 1.1, z);
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
            Location runeLoc = new Location(world, x, center.getY() + 1.1, z);
            
            // Draw small rune symbol (cross pattern)
            world.spawnParticle(Particle.REDSTONE, runeLoc, 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0.1, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(-0.1, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0, 0, 0.1), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeLoc.clone().add(0, 0, -0.1), 1, 0, 0, 0, 0, dust);
        }
    }
    
    /**
     * Play ambient sounds during ritual
     */
    private void playAmbientSounds(RitualDisplay display, int tick) {
        World world = display.center.getWorld();
        if (world == null) return;
        
        Location soundLoc = display.center.clone().add(0, CRYSTAL_HEIGHT, 0);
        
        // Base ambient hum every 3 seconds
        if (tick % 60 == 0) {
            world.playSound(soundLoc, Sound.BLOCK_BEACON_AMBIENT, 0.15f, 0.5f);
        }
        
        // Enchanting whispers every 5 seconds
        if (tick % 100 == 5) {
            world.playSound(soundLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.1f, 0.7f);
        }
        
        // Portal whoosh every 8 seconds
        if (tick % 160 == 15) {
            world.playSound(soundLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.08f, 1.2f);
        }
        
        // Crystal-specific accent sound every 20 seconds
        if (tick % 400 == 60) {
            playCrystalAccentSound(world, soundLoc, display.crystalType);
        }
    }
    
    /**
     * Play a crystal-specific accent sound
     */
    private void playCrystalAccentSound(World world, Location loc, CrystalType type) {
        float volume = 0.12f;
        
        switch (type) {
            case FIRE -> world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, volume, 0.6f);
            case WATER -> world.playSound(loc, Sound.AMBIENT_UNDERWATER_LOOP, volume, 1.0f);
            case AIR -> world.playSound(loc, Sound.ENTITY_PHANTOM_FLAP, volume, 1.5f);
            case EARTH -> world.playSound(loc, Sound.BLOCK_GRAVEL_BREAK, volume, 0.5f);
            case DARK -> world.playSound(loc, Sound.AMBIENT_BASALT_DELTAS_MOOD, volume, 0.7f);
            case LIGHT -> world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, volume, 1.2f);
            case VOID -> world.playSound(loc, Sound.AMBIENT_WARPED_FOREST_MOOD, volume, 0.5f);
            case STORM -> world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, volume * 0.5f, 2.0f);
            case TIME -> world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, volume, 1.8f);
            case LUCK -> world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.5f);
        }
    }
    
    /**
     * Get color for crystal type
     */
    private Color getCrystalColor(CrystalType type) {
        return switch (type) {
            case FIRE -> Color.fromRGB(255, 69, 0);      // Orange Red
            case WATER -> Color.fromRGB(0, 191, 255);    // Deep Sky Blue
            case AIR -> Color.fromRGB(230, 230, 250);    // Lavender
            case EARTH -> Color.fromRGB(139, 69, 19);    // Saddle Brown
            case DARK -> Color.fromRGB(75, 0, 130);      // Indigo
            case LIGHT -> Color.fromRGB(255, 215, 0);    // Gold
            case VOID -> Color.fromRGB(138, 43, 226);    // Blue Violet
            case STORM -> Color.fromRGB(70, 130, 180);   // Steel Blue
            case TIME -> Color.fromRGB(255, 20, 147);    // Deep Pink
            case LUCK -> Color.fromRGB(50, 205, 50);     // Lime Green
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
        
        // Remove beam target
        if (display.beamTarget != null && !display.beamTarget.isDead()) {
            display.beamTarget.remove();
        }
        
        // Remove ender crystals
        if (display.enderCrystals != null) {
            for (EnderCrystal crystal : display.enderCrystals) {
                if (crystal != null && !crystal.isDead()) {
                    crystal.remove();
                }
            }
        }
    }
    
    /**
     * Cleanup all displays
     */
    public void cleanup() {
        for (UUID playerId : new ArrayList<>(activeDisplays.keySet())) {
            removeDisplay(playerId);
        }
    }
    
    /**
     * Inner class to store display data
     */
    private static class RitualDisplay {
        Location center;
        CrystalType crystalType;
        long startTime;
        long duration;
        
        Item floatingItem;
        ArmorStand timerHologram;
        ArmorStand beamTarget;
        List<Location> beamSources;
        List<EnderCrystal> enderCrystals;
        
        BukkitRunnable updateTask;
    }
}