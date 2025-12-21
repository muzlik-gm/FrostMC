package com.muzlik.vfx.cinematic.animation;

import com.muzlik.vfx.cinematic.ParticleSpawnData;

import java.util.ArrayList;
import java.util.List;

/**
 * Blends between two particle patterns.
 * Enforces minimum 5 tick blend duration.
 * 
 * Requirements: 6.2
 */
public class TransitionBlender {
    
    private static final int MIN_BLEND_TICKS = 5;
    
    /**
     * Blend between two particle patterns
     * 
     * @param patternA First pattern
     * @param patternB Second pattern
     * @param blendFactor Blend factor 0.0 (all A) to 1.0 (all B)
     * @param minBlendTicks Minimum ticks for blend (at least 5)
     * @return Blended particle list
     */
    public List<ParticleSpawnData> blendPatterns(List<ParticleSpawnData> patternA,
                                                   List<ParticleSpawnData> patternB,
                                                   double blendFactor,
                                                   int minBlendTicks) {
        if (patternA == null || patternB == null) {
            throw new IllegalArgumentException("Patterns cannot be null");
        }
        
        if (minBlendTicks < MIN_BLEND_TICKS) {
            throw new IllegalArgumentException("Blend duration must be at least " + MIN_BLEND_TICKS + " ticks");
        }
        
        // Clamp blend factor
        blendFactor = Math.max(0.0, Math.min(1.0, blendFactor));
        
        List<ParticleSpawnData> blended = new ArrayList<>();
        
        // Calculate how many particles to take from each pattern
        int countA = (int) (patternA.size() * (1.0 - blendFactor));
        int countB = (int) (patternB.size() * blendFactor);
        
        // Add particles from pattern A with fading opacity
        for (int i = 0; i < Math.min(countA, patternA.size()); i++) {
            ParticleSpawnData original = patternA.get(i);
            double fadedOpacity = original.getOpacity() * (1.0 - blendFactor);
            
            blended.add(new ParticleSpawnData.Builder()
                .location(original.getLocation())
                .particleType(original.getParticleType())
                .count(original.getCount())
                .offset(original.getOffsetX(), original.getOffsetY(), original.getOffsetZ())
                .speed(original.getSpeed())
                .data(original.getData())
                .size(original.getSize())
                .opacity(fadedOpacity)
                .layer(original.getLayer())
                .build());
        }
        
        // Add particles from pattern B with fading in opacity
        for (int i = 0; i < Math.min(countB, patternB.size()); i++) {
            ParticleSpawnData original = patternB.get(i);
            double fadedOpacity = original.getOpacity() * blendFactor;
            
            blended.add(new ParticleSpawnData.Builder()
                .location(original.getLocation())
                .particleType(original.getParticleType())
                .count(original.getCount())
                .offset(original.getOffsetX(), original.getOffsetY(), original.getOffsetZ())
                .speed(original.getSpeed())
                .data(original.getData())
                .size(original.getSize())
                .opacity(fadedOpacity)
                .layer(original.getLayer())
                .build());
        }
        
        return blended;
    }
    
    /**
     * Get minimum blend ticks constant
     */
    public static int getMinBlendTicks() {
        return MIN_BLEND_TICKS;
    }
}
