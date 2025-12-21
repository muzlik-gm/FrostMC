package com.muzlik.vfx.cinematic;

import org.bukkit.Bukkit;

/**
 * Monitors server TPS in real-time.
 * Provides TPS thresholds for performance scaling.
 * 
 * Requirements: 9.3
 */
public class TPSMonitor {
    
    private static final int TPS_SAMPLE_SIZE = 20;
    private final long[] tickTimes;
    private int currentIndex;
    private long lastTickTime;
    
    public TPSMonitor() {
        this.tickTimes = new long[TPS_SAMPLE_SIZE];
        this.currentIndex = 0;
        this.lastTickTime = System.currentTimeMillis();
    }
    
    /**
     * Update TPS measurement (should be called every tick)
     */
    public void tick() {
        long currentTime = System.currentTimeMillis();
        long tickTime = currentTime - lastTickTime;
        
        tickTimes[currentIndex] = tickTime;
        currentIndex = (currentIndex + 1) % TPS_SAMPLE_SIZE;
        lastTickTime = currentTime;
    }
    
    /**
     * Get current TPS estimate
     * 
     * @return Current TPS (0-20)
     */
    public double getCurrentTPS() {
        // Use Bukkit's TPS if available (Paper API)
        try {
            double[] tps = Bukkit.getTPS();
            if (tps != null && tps.length > 0) {
                return Math.min(20.0, tps[0]); // 1-minute average
            }
        } catch (NoSuchMethodError e) {
            // Fallback for non-Paper servers
        }
        
        // Calculate from our samples
        long totalTime = 0;
        int validSamples = 0;
        
        for (long tickTime : tickTimes) {
            if (tickTime > 0) {
                totalTime += tickTime;
                validSamples++;
            }
        }
        
        if (validSamples == 0) {
            return 20.0; // Assume good TPS if no data
        }
        
        double averageTickTime = totalTime / (double) validSamples;
        double tps = 1000.0 / averageTickTime; // Convert ms to TPS
        
        return Math.min(20.0, tps);
    }
    
    /**
     * Check if TPS is below threshold
     * 
     * @param threshold TPS threshold
     * @return true if current TPS is below threshold
     */
    public boolean isBelowThreshold(double threshold) {
        return getCurrentTPS() < threshold;
    }
}
