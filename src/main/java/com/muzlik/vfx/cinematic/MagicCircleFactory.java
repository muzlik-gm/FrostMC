package com.muzlik.vfx.cinematic;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for building magic circles with configuration.
 * Wires together rings, inner patterns, and rank scaling.
 * 
 * Requirements: 8.2, 8.3
 */
public class MagicCircleFactory {
    
    private final FragmentPatternRegistry patternRegistry;
    private final RankVisualScaler rankScaler;
    
    public MagicCircleFactory() {
        this.patternRegistry = new FragmentPatternRegistry();
        this.rankScaler = new RankVisualScaler();
    }
    
    /**
     * Create a magic circle from configuration
     * 
     * @param config Magic circle configuration
     * @return Configured magic circle
     */
    public MagicCircle createMagicCircle(MagicCircleConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        
        // Get ring count based on rank
        int ringCount = rankScaler.getRingCount(config.getRank());
        
        // Create rings with increasing radii
        List<MagicCircleRing> rings = new ArrayList<>();
        double radiusStep = config.getBaseRadius() / ringCount;
        
        for (int i = 0; i < ringCount; i++) {
            double radius = config.getBaseRadius() - (i * radiusStep);
            int particleCount = 16 + (i * 2); // Reduced particle density for crisp lines
            
            // Determine rotation speed (middle rings counter-rotate at rank 5+)
            double rotationSpeed = config.getRotationSpeed();
            if (i == 1 && rankScaler.hasFeature(config.getRank(), RankVisualScaler.RankFeature.COUNTER_ROTATION)) {
                rotationSpeed = -config.getRotationSpeed(); // Counter-rotate middle ring
            }
            
            // Add rune segments at rank 3+
            boolean hasRunes = (i == ringCount - 1) && rankScaler.hasFeature(config.getRank(), RankVisualScaler.RankFeature.RUNE_SEGMENTS);
            
            rings.add(new MagicCircleRing(radius, particleCount, rotationSpeed, hasRunes));
        }
        
        // Get inner pattern for fragment type
        InnerPattern innerPattern = patternRegistry.getPattern(config.getFragmentType());
        
        // Create and return magic circle
        return new MagicCircle(
            config.getCenter(),
            rings,
            innerPattern,
            config.getFragmentType(),
            config.getRank(),
            config.getRotationSpeed(),
            config.getLifetime(),
            config.getPrimaryParticle(),
            config.getSecondaryParticle(),
            config.getOwner()
        );
    }
    
    /**
     * Create a fragment-specific magic circle
     * 
     * @param fragmentType Fragment type
     * @param center Center location
     * @param rank Player rank
     * @param owner Player owner
     * @return Configured magic circle
     */
    public MagicCircle createFragmentCircle(com.muzlik.fragment.FragmentType fragmentType,
                                            org.bukkit.Location center,
                                            int rank,
                                            org.bukkit.entity.Player owner) {
        MagicCircleConfig config = new MagicCircleConfig.Builder()
            .fragmentType(fragmentType)
            .center(center)
            .rank(rank)
            .owner(owner)
            .baseRadius(3.0)
            .rotationSpeed(0.05)
            .lifetime(100)
            .build();
        
        return createMagicCircle(config);
    }
}
