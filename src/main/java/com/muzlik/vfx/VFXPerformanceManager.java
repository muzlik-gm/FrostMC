package com.muzlik.vfx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Performance manager for VFX throttling based on server TPS.
 * 
 * Monitors server TPS and adjusts particle density multiplier to maintain performance.
 * Automatically reduces particle counts when TPS drops below thresholds.
 * 
 * Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6
 */
public class VFXPerformanceManager {
    private final JavaPlugin plugin;
    
    // Current TPS and multiplier
    private double currentTPS;
    private double particleDensityMultiplier;
    
    // Monitoring task
    private BukkitTask monitorTask;
    
    // TPS thresholds
    private static final double TPS_THRESHOLD_MEDIUM = 18.0;
    private static final double TPS_THRESHOLD_LOW = 15.0;
    private static final double TPS_TARGET = 20.0;
    
    // Multipliers
    private static final double MULTIPLIER_FULL = 1.0;
    private static final double MULTIPLIER_MEDIUM = 0.5;
    private static final double MULTIPLIER_LOW = 0.25;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     */
    public VFXPerformanceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentTPS = TPS_TARGET;
        this.particleDensityMultiplier = MULTIPLIER_FULL;
    }
    
    /**
     * Start TPS monitoring
     * Checks TPS every 100 ticks (5 seconds)
     */
    public void startMonitoring() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        
        // Run every 100 ticks (5 seconds)
        monitorTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updatePerformance, 100L, 100L);
        
        plugin.getLogger().info("VFXPerformanceManager monitoring started");
    }
    
    /**
     * Stop TPS monitoring
     */
    public void stopMonitoring() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }
    
    /**
     * Update performance metrics and adjust multiplier
     */
    private void updatePerformance() {
        currentTPS = getServerTPS();
        
        // Adjust particle density multiplier based on TPS
        if (currentTPS >= TPS_THRESHOLD_MEDIUM) {
            particleDensityMultiplier = MULTIPLIER_FULL;
        } else if (currentTPS >= TPS_THRESHOLD_LOW) {
            particleDensityMultiplier = MULTIPLIER_MEDIUM;
        } else {
            particleDensityMultiplier = MULTIPLIER_LOW;
        }
    }
    
    /**
     * Get current server TPS
     * Uses Bukkit's TPS tracking
     * 
     * @return Current TPS (capped at 20.0)
     */
    private double getServerTPS() {
        try {
            // Try to get TPS from server
            Object server = Bukkit.getServer();
            java.lang.reflect.Method getTPSMethod = server.getClass().getMethod("getTPS");
            double[] tps = (double[]) getTPSMethod.invoke(server);
            
            // Return 1-minute average TPS
            return Math.min(tps[0], TPS_TARGET);
        } catch (Exception e) {
            // Fallback: assume good TPS if we can't measure
            return TPS_TARGET;
        }
    }
    
    /**
     * Get current TPS
     * 
     * @return Current TPS
     */
    public double getCurrentTPS() {
        return currentTPS;
    }
    
    /**
     * Get particle density multiplier
     * 
     * @return Multiplier (0.25, 0.5, or 1.0)
     */
    public double getParticleDensityMultiplier() {
        return particleDensityMultiplier;
    }
    
    /**
     * Scale particle count based on current performance
     * 
     * @param baseCount Base particle count
     * @return Scaled particle count
     */
    public int scaleParticleCount(int baseCount) {
        return (int) Math.max(1, Math.floor(baseCount * particleDensityMultiplier));
    }
    
    /**
     * Check if cinematic effects should be skipped
     * Skips when TPS is below medium threshold
     * 
     * @return true if should skip cinematic effects
     */
    public boolean shouldSkipCinematic() {
        return currentTPS < TPS_THRESHOLD_MEDIUM;
    }
    
    /**
     * Get performance tier as string
     * 
     * @return "FULL", "MEDIUM", or "LOW"
     */
    public String getPerformanceTier() {
        if (particleDensityMultiplier >= MULTIPLIER_FULL) {
            return "FULL";
        } else if (particleDensityMultiplier >= MULTIPLIER_MEDIUM) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Shutdown the performance manager
     */
    public void shutdown() {
        stopMonitoring();
        plugin.getLogger().info("VFXPerformanceManager shutdown complete");
    }
}
