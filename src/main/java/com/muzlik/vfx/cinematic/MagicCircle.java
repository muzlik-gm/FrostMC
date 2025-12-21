package com.muzlik.vfx.cinematic;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Clean, minimalistic magic circles with rank-based complexity.
 * 
 * Design Philosophy:
 * - CLEAN over complex: Every line should be crisp and visible
 * - MINIMALISTIC: Use fewer particles, but place them precisely
 * - RANK SCALING: Simple at low ranks, intricate at high ranks
 * 
 * Rank Tiers:
 * - Rank 1-5: Simple - Single ring, basic center symbol
 * - Rank 6-7: Complex - Multiple rings, geometric shapes, runes
 * - Rank 8-10: Master - Full transmutation circle with satellite circles
 */
public class MagicCircle {
    private final Location center;
    private final List<MagicCircleRing> rings;
    private final InnerPattern innerPattern;
    private final FragmentType fragmentType;
    private final int rank;
    private final org.bukkit.entity.Player owner;
    private double currentRotation;
    private final double rotationSpeed;
    private int currentTick;
    private final int lifetime;
    private MagicCirclePhase phase;
    private final Color primaryColor;
    private final Color secondaryColor;
    
    // Phase durations
    private static final int SPAWN_DURATION = 10;
    private static final int FADE_DURATION = 15;
    
    // Particle size for crisp lines
    private static final float PARTICLE_SIZE = 0.8f;
    private static final float PARTICLE_SIZE_SMALL = 0.5f;
    
    public enum MagicCirclePhase {
        SPAWNING, ACTIVE, FADING
    }
    
    public MagicCircle(Location center, List<MagicCircleRing> rings, InnerPattern innerPattern,
                       FragmentType fragmentType, int rank, double rotationSpeed, int lifetime,
                       Particle primaryParticle, Particle secondaryParticle, org.bukkit.entity.Player owner) {
        if (center == null) throw new IllegalArgumentException("Center cannot be null");
        if (rings == null || rings.isEmpty()) throw new IllegalArgumentException("Rings cannot be null or empty");
        if (innerPattern == null) throw new IllegalArgumentException("Inner pattern cannot be null");
        
        this.center = center;
        this.rings = rings;
        this.innerPattern = innerPattern;
        this.fragmentType = fragmentType;
        this.rank = Math.min(rank, 10); // Cap at 10
        this.owner = owner;
        this.currentRotation = 0;
        this.rotationSpeed = rotationSpeed;
        this.currentTick = 0;
        this.lifetime = lifetime;
        this.phase = MagicCirclePhase.SPAWNING;
        this.primaryColor = getFragmentPrimaryColor(fragmentType);
        this.secondaryColor = getFragmentSecondaryColor(fragmentType);
    }
    
    private Color getFragmentPrimaryColor(FragmentType type) {
        if (type == null) return Color.fromRGB(200, 150, 255);
        return switch (type) {
            case FIRE -> Color.fromRGB(255, 80, 0);
            case WATER -> Color.fromRGB(0, 150, 255);
            case AIR -> Color.fromRGB(220, 240, 255);
            case DARK -> Color.fromRGB(120, 0, 180);
            case LIGHT -> Color.fromRGB(255, 255, 200);
            case VOID -> Color.fromRGB(60, 0, 90);
            case STORM -> Color.fromRGB(100, 120, 255);
            case DRAGON -> Color.fromRGB(220, 50, 100);
            default -> Color.fromRGB(200, 150, 255);
        };
    }
    
    private Color getFragmentSecondaryColor(FragmentType type) {
        if (type == null) return Color.WHITE;
        return switch (type) {
            case FIRE -> Color.fromRGB(255, 200, 50);
            case WATER -> Color.fromRGB(150, 220, 255);
            case AIR -> Color.WHITE;
            case DARK -> Color.fromRGB(180, 50, 255);
            case LIGHT -> Color.fromRGB(255, 255, 150);
            case VOID -> Color.fromRGB(140, 0, 200);
            case STORM -> Color.fromRGB(200, 200, 255);
            case DRAGON -> Color.fromRGB(255, 150, 100);
            default -> Color.WHITE;
        };
    }
    
    public void update(int tick) {
        currentTick = tick;
        currentRotation += rotationSpeed;
        if (currentRotation > 2 * Math.PI) currentRotation -= 2 * Math.PI;
        
        if (phase == MagicCirclePhase.SPAWNING && tick >= SPAWN_DURATION) {
            phase = MagicCirclePhase.ACTIVE;
        } else if (phase == MagicCirclePhase.ACTIVE && tick >= lifetime - FADE_DURATION) {
            phase = MagicCirclePhase.FADING;
        }
    }
    
    /**
     * Render the magic circle - CLEAN and MINIMALISTIC
     */
    public void render(World world) {
        if (world == null) return;
        
        // Calculate opacity for fade effects
        float opacity = 1.0f;
        if (phase == MagicCirclePhase.SPAWNING) {
            opacity = currentTick / (float) SPAWN_DURATION;
        } else if (phase == MagicCirclePhase.FADING) {
            int fadeProgress = currentTick - (lifetime - FADE_DURATION);
            opacity = 1.0f - (fadeProgress / (float) FADE_DURATION);
        }
        
        float size = PARTICLE_SIZE * opacity;
        float sizeSmall = PARTICLE_SIZE_SMALL * opacity;
        
        Particle.DustOptions primary = new Particle.DustOptions(primaryColor, size);
        Particle.DustOptions secondary = new Particle.DustOptions(secondaryColor, sizeSmall);
        Particle.DustOptions accent = new Particle.DustOptions(Color.WHITE, sizeSmall * 0.8f);
        
        // Render based on rank tier
        if (rank <= 5) {
            renderSimpleCircle(world, primary, secondary, accent);
        } else if (rank <= 7) {
            renderComplexCircle(world, primary, secondary, accent);
        } else {
            renderMasterCircle(world, primary, secondary, accent);
        }
    }
    
    // ==================== SIMPLE CIRCLE (Rank 1-5) ====================
    // Clean, minimal design with single ring and basic center
    
    private void renderSimpleCircle(World world, Particle.DustOptions primary, 
                                    Particle.DustOptions secondary, Particle.DustOptions accent) {
        double baseRadius = rings.isEmpty() ? 3.0 : rings.get(0).getRadius();
        
        // Single outer ring - clean circle
        int ringPoints = 40 + (rank * 4); // 44-60 points based on rank
        renderCircle(world, center, baseRadius, ringPoints, currentRotation * 0.3, primary);
        
        // Inner ring at rank 3+
        if (rank >= 3) {
            renderCircle(world, center, baseRadius * 0.6, ringPoints - 10, currentRotation * -0.4, secondary);
        }
        
        // Simple center cross
        renderCenterCross(world, 0.4, currentRotation, primary);
        
        // Center dot
        world.spawnParticle(Particle.REDSTONE, center, 1, 0, 0, 0, 0, accent);
        
        // Simple triangle at rank 4+
        if (rank >= 4) {
            renderTriangle(world, center, baseRadius * 0.45, currentRotation * 0.5, secondary);
        }
        
        // Hexagon at rank 5
        if (rank >= 5) {
            renderHexagon(world, center, baseRadius * 0.75, currentRotation * -0.2, secondary);
        }
    }
    
    // ==================== COMPLEX CIRCLE (Rank 6-7) ====================
    // Multiple rings, geometric shapes, rune markers
    
    private void renderComplexCircle(World world, Particle.DustOptions primary, 
                                     Particle.DustOptions secondary, Particle.DustOptions accent) {
        double baseRadius = rings.isEmpty() ? 3.5 : rings.get(0).getRadius();
        
        // Outer ring with double line effect
        renderCircle(world, center, baseRadius, 60, currentRotation * 0.25, primary);
        renderCircle(world, center, baseRadius * 0.95, 55, currentRotation * 0.25, secondary);
        
        // Middle ring (counter-rotating)
        renderCircle(world, center, baseRadius * 0.7, 50, currentRotation * -0.35, primary);
        
        // Inner ring
        renderCircle(world, center, baseRadius * 0.45, 40, currentRotation * 0.4, secondary);
        
        // Hexagon with radial lines
        renderHexagonWithLines(world, center, baseRadius * 0.85, currentRotation * -0.15, primary, secondary);
        
        // 6-pointed star
        renderSixPointedStar(world, center, baseRadius * 0.55, currentRotation * 0.3, primary);
        
        // Triangle pair (normal + inverted)
        renderTriangle(world, center, baseRadius * 0.35, currentRotation * 0.5, secondary);
        renderTriangle(world, center, baseRadius * 0.28, currentRotation * 0.5 + Math.PI, accent);
        
        // Center symbol
        renderCenterSymbol(world, currentRotation, primary, accent);
        
        // Rune markers on outer edge (rank 7)
        if (rank >= 7) {
            renderRuneMarkers(world, center, baseRadius * 0.88, 8, currentRotation * 0.1, secondary);
        }
    }

    
    // ==================== MASTER CIRCLE (Rank 8-10) ====================
    // Full transmutation circle with satellite circles, intricate geometry
    
    private void renderMasterCircle(World world, Particle.DustOptions primary, 
                                    Particle.DustOptions secondary, Particle.DustOptions accent) {
        double baseRadius = rings.isEmpty() ? 4.0 : rings.get(0).getRadius();
        
        // === MAIN CIRCLE STRUCTURE ===
        
        // Triple outer ring for emphasis
        renderCircle(world, center, baseRadius, 70, currentRotation * 0.2, primary);
        renderCircle(world, center, baseRadius * 0.97, 65, currentRotation * 0.2, secondary);
        renderCircle(world, center, baseRadius * 0.94, 60, currentRotation * 0.2, accent);
        
        // Secondary ring (counter-rotating)
        renderCircle(world, center, baseRadius * 0.75, 55, currentRotation * -0.3, primary);
        
        // Tertiary ring
        renderCircle(world, center, baseRadius * 0.55, 45, currentRotation * 0.35, secondary);
        
        // Inner ring
        renderCircle(world, center, baseRadius * 0.35, 35, currentRotation * -0.4, primary);
        
        // === GEOMETRIC SHAPES ===
        
        // Outer hexagon with connecting lines
        renderHexagonWithLines(world, center, baseRadius * 0.88, currentRotation * -0.1, primary, secondary);
        
        // 8-pointed star
        renderEightPointedStar(world, center, baseRadius * 0.65, baseRadius * 0.35, currentRotation * 0.25, primary);
        
        // Inner hexagon
        renderHexagon(world, center, baseRadius * 0.5, currentRotation * 0.3, secondary);
        
        // Triangle pair
        renderTriangle(world, center, baseRadius * 0.4, currentRotation * 0.4, primary);
        renderTriangle(world, center, baseRadius * 0.32, currentRotation * 0.4 + Math.PI, secondary);
        
        // === SATELLITE CIRCLES (The key feature of master circles) ===
        int satelliteCount = rank >= 9 ? 6 : 4;
        double satelliteRadius = baseRadius * 0.22;
        double satelliteDistance = baseRadius * 1.15;
        
        for (int i = 0; i < satelliteCount; i++) {
            double angle = (2 * Math.PI * i / satelliteCount) + currentRotation * 0.15;
            Location satelliteCenter = new Location(world,
                center.getX() + satelliteDistance * Math.cos(angle),
                center.getY(),
                center.getZ() + satelliteDistance * Math.sin(angle));
            
            // Satellite outer ring
            renderCircle(world, satelliteCenter, satelliteRadius, 25, currentRotation * -0.5, primary);
            
            // Satellite inner ring
            renderCircle(world, satelliteCenter, satelliteRadius * 0.6, 18, currentRotation * 0.6, secondary);
            
            // Satellite center symbol
            renderMiniSymbol(world, satelliteCenter, satelliteRadius * 0.35, currentRotation * 0.8, accent);
            
            // Connecting line from satellite to main circle
            Location mainEdge = new Location(world,
                center.getX() + baseRadius * Math.cos(angle),
                center.getY(),
                center.getZ() + baseRadius * Math.sin(angle));
            Location satEdge = new Location(world,
                satelliteCenter.getX() - satelliteRadius * Math.cos(angle),
                center.getY(),
                satelliteCenter.getZ() - satelliteRadius * Math.sin(angle));
            renderLine(world, mainEdge, satEdge, 6, secondary);
        }
        
        // === RUNE MARKERS ===
        renderRuneMarkers(world, center, baseRadius * 0.82, 12, currentRotation * 0.08, secondary);
        
        // === CENTER SYMBOL ===
        renderCenterSymbol(world, currentRotation, primary, accent);
        
        // === CONNECTING ARCS (Rank 9+) ===
        if (rank >= 9) {
            renderConnectingArcs(world, center, baseRadius * 0.6, 6, currentRotation * -0.2, accent);
        }
        
        // === PENTAGRAM (Rank 10 only) ===
        if (rank >= 10) {
            renderPentagram(world, center, baseRadius * 0.45, currentRotation * 0.35, accent);
        }
    }
    
    // ==================== PRIMITIVE RENDERING METHODS ====================
    // Clean, precise particle placement for crisp lines
    
    private void renderCircle(World world, Location center, double radius, int points, 
                              double rotation, Particle.DustOptions dust) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i / points) + rotation;
            Location loc = new Location(world,
                center.getX() + radius * Math.cos(angle),
                center.getY(),
                center.getZ() + radius * Math.sin(angle));
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
        }
    }
    
    private void renderLine(World world, Location start, Location end, int points, Particle.DustOptions dust) {
        for (int i = 0; i < points; i++) {
            double t = i / (double) Math.max(1, points - 1);
            Location loc = new Location(world,
                start.getX() + (end.getX() - start.getX()) * t,
                start.getY(),
                start.getZ() + (end.getZ() - start.getZ()) * t);
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
        }
    }
    
    private void renderTriangle(World world, Location center, double radius, double rotation, Particle.DustOptions dust) {
        Location[] vertices = new Location[3];
        for (int i = 0; i < 3; i++) {
            double angle = (i * 2 * Math.PI / 3) + rotation - (Math.PI / 2);
            vertices[i] = new Location(world,
                center.getX() + radius * Math.cos(angle),
                center.getY(),
                center.getZ() + radius * Math.sin(angle));
        }
        for (int i = 0; i < 3; i++) {
            renderLine(world, vertices[i], vertices[(i + 1) % 3], 8, dust);
        }
    }
    
    private void renderHexagon(World world, Location center, double radius, double rotation, Particle.DustOptions dust) {
        Location[] vertices = new Location[6];
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI / 3) + rotation;
            vertices[i] = new Location(world,
                center.getX() + radius * Math.cos(angle),
                center.getY(),
                center.getZ() + radius * Math.sin(angle));
        }
        for (int i = 0; i < 6; i++) {
            renderLine(world, vertices[i], vertices[(i + 1) % 6], 6, dust);
        }
    }
    
    private void renderHexagonWithLines(World world, Location center, double radius, double rotation,
                                        Particle.DustOptions primary, Particle.DustOptions secondary) {
        Location[] vertices = new Location[6];
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI / 3) + rotation;
            vertices[i] = new Location(world,
                center.getX() + radius * Math.cos(angle),
                center.getY(),
                center.getZ() + radius * Math.sin(angle));
        }
        
        // Hexagon edges
        for (int i = 0; i < 6; i++) {
            renderLine(world, vertices[i], vertices[(i + 1) % 6], 7, primary);
        }
        
        // Radial lines from vertices toward center (stop at 50%)
        for (int i = 0; i < 6; i++) {
            Location midpoint = new Location(world,
                center.getX() + (vertices[i].getX() - center.getX()) * 0.5,
                center.getY(),
                center.getZ() + (vertices[i].getZ() - center.getZ()) * 0.5);
            renderLine(world, vertices[i], midpoint, 5, secondary);
        }
    }
    
    private void renderSixPointedStar(World world, Location center, double radius, double rotation, Particle.DustOptions dust) {
        // Two overlapping triangles
        renderTriangle(world, center, radius, rotation, dust);
        renderTriangle(world, center, radius, rotation + Math.PI, dust);
    }
    
    private void renderEightPointedStar(World world, Location center, double outerRadius, double innerRadius,
                                        double rotation, Particle.DustOptions dust) {
        int points = 8;
        Location[] outer = new Location[points];
        Location[] inner = new Location[points];
        
        for (int i = 0; i < points; i++) {
            double outerAngle = (i * Math.PI / 4) + rotation;
            double innerAngle = outerAngle + (Math.PI / 8);
            
            outer[i] = new Location(world,
                center.getX() + outerRadius * Math.cos(outerAngle),
                center.getY(),
                center.getZ() + outerRadius * Math.sin(outerAngle));
            
            inner[i] = new Location(world,
                center.getX() + innerRadius * Math.cos(innerAngle),
                center.getY(),
                center.getZ() + innerRadius * Math.sin(innerAngle));
        }
        
        for (int i = 0; i < points; i++) {
            renderLine(world, outer[i], inner[i], 5, dust);
            renderLine(world, inner[i], outer[(i + 1) % points], 5, dust);
        }
    }
    
    private void renderPentagram(World world, Location center, double radius, double rotation, Particle.DustOptions dust) {
        Location[] vertices = new Location[5];
        for (int i = 0; i < 5; i++) {
            double angle = (i * 72 * Math.PI / 180) + rotation - (Math.PI / 2);
            vertices[i] = new Location(world,
                center.getX() + radius * Math.cos(angle),
                center.getY(),
                center.getZ() + radius * Math.sin(angle));
        }
        // Connect every 2nd vertex
        for (int i = 0; i < 5; i++) {
            renderLine(world, vertices[i], vertices[(i + 2) % 5], 10, dust);
        }
    }

    
    private void renderCenterCross(World world, double size, double rotation, Particle.DustOptions dust) {
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2) + rotation;
            Location loc = new Location(world,
                center.getX() + size * Math.cos(angle),
                center.getY(),
                center.getZ() + size * Math.sin(angle));
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
        }
    }
    
    private void renderCenterSymbol(World world, double rotation, Particle.DustOptions primary, Particle.DustOptions accent) {
        // Central dot
        world.spawnParticle(Particle.REDSTONE, center, 1, 0, 0, 0, 0, accent);
        
        // Rotating cross
        double crossSize = 0.5;
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2) + rotation * 2;
            Location loc = new Location(world,
                center.getX() + crossSize * Math.cos(angle),
                center.getY(),
                center.getZ() + crossSize * Math.sin(angle));
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, primary);
        }
        
        // Inner diamond (counter-rotating)
        double diamondSize = 0.7;
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2) + rotation * -1.5 + (Math.PI / 4);
            Location loc = new Location(world,
                center.getX() + diamondSize * Math.cos(angle),
                center.getY(),
                center.getZ() + diamondSize * Math.sin(angle));
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, accent);
        }
    }
    
    private void renderMiniSymbol(World world, Location center, double size, double rotation, Particle.DustOptions dust) {
        // Simple 4-point symbol for satellite circles
        world.spawnParticle(Particle.REDSTONE, center, 1, 0, 0, 0, 0, dust);
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2) + rotation;
            Location loc = new Location(world,
                center.getX() + size * Math.cos(angle),
                center.getY(),
                center.getZ() + size * Math.sin(angle));
            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
        }
    }
    
    private void renderRuneMarkers(World world, Location center, double radius, int count, 
                                   double rotation, Particle.DustOptions dust) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i / count) + rotation;
            Location runeCenter = new Location(world,
                center.getX() + radius * Math.cos(angle),
                center.getY(),
                center.getZ() + radius * Math.sin(angle));
            
            // Simple rune: small cross pattern
            double runeSize = 0.15;
            world.spawnParticle(Particle.REDSTONE, runeCenter, 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeCenter.clone().add(runeSize, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeCenter.clone().add(-runeSize, 0, 0), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeCenter.clone().add(0, 0, runeSize), 1, 0, 0, 0, 0, dust);
            world.spawnParticle(Particle.REDSTONE, runeCenter.clone().add(0, 0, -runeSize), 1, 0, 0, 0, 0, dust);
        }
    }
    
    private void renderConnectingArcs(World world, Location center, double radius, int count,
                                      double rotation, Particle.DustOptions dust) {
        for (int i = 0; i < count; i++) {
            double startAngle = (i * 2 * Math.PI / count) + rotation;
            double arcLength = Math.PI / (count * 1.5);
            
            int arcPoints = 6;
            for (int p = 0; p < arcPoints; p++) {
                double t = p / (double) (arcPoints - 1);
                double angle = startAngle + t * arcLength;
                Location loc = new Location(world,
                    center.getX() + radius * Math.cos(angle),
                    center.getY(),
                    center.getZ() + radius * Math.sin(angle));
                world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dust);
            }
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    public List<ParticleSpawnData> getParticleSpawns() {
        return new ArrayList<>(); // Use render() method instead
    }
    
    public void startFade() {
        if (phase == MagicCirclePhase.ACTIVE) {
            phase = MagicCirclePhase.FADING;
            currentTick = lifetime - FADE_DURATION;
        }
    }
    
    public Location getCenter() { return center; }
    public MagicCirclePhase getPhase() { return phase; }
    public int getCurrentTick() { return currentTick; }
    public boolean isComplete() { return currentTick >= lifetime; }
    public FragmentType getFragmentType() { return fragmentType; }
    public int getRank() { return rank; }
    public org.bukkit.entity.Player getOwner() { return owner; }
}
