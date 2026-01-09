package com.muzlik.monitoring;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * System health monitoring (Task 17)
 * Tracks system metrics and warns when thresholds are exceeded
 */
public class HealthMonitor {
    private final JavaPlugin plugin;
    private BukkitRunnable monitoringTask;
    
    // Thresholds
    private static final int MAX_RITUALS = 50;
    private static final int MAX_VFX_EFFECTS = 500;
    private static final int MAX_QUEUED_TASKS = 100;
    
    // Current metrics
    private int activeRituals = 0;
    private int activeVFXEffects = 0;
    private int queuedTasks = 0;
    
    // Warning flags
    private boolean ritualsWarning = false;
    private boolean vfxWarning = false;
    private boolean tasksWarning = false;
    
    public HealthMonitor(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start monitoring task (runs every 30 seconds)
     */
    public void startMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
        }
        
        monitoringTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkHealth();
            }
        };
        
        // Run every 30 seconds (600 ticks)
        monitoringTask.runTaskTimer(plugin, 600L, 600L);
        
        plugin.getLogger().info("Health monitoring started");
    }
    
    /**
     * Check system health and log warnings
     */
    private void checkHealth() {
        // Update metrics
        updateMetrics();
        
        // Check rituals
        if (activeRituals > MAX_RITUALS) {
            if (!ritualsWarning) {
                plugin.getLogger().warning("⚠ HIGH RITUAL COUNT: " + activeRituals + " active rituals (threshold: " + MAX_RITUALS + ")");
                ritualsWarning = true;
            }
        } else {
            ritualsWarning = false;
        }
        
        // Check VFX effects
        if (activeVFXEffects > MAX_VFX_EFFECTS) {
            if (!vfxWarning) {
                plugin.getLogger().warning("⚠ HIGH VFX COUNT: " + activeVFXEffects + " active effects (threshold: " + MAX_VFX_EFFECTS + ")");
                vfxWarning = true;
            }
        } else {
            vfxWarning = false;
        }
        
        // Check queued tasks
        if (queuedTasks > MAX_QUEUED_TASKS) {
            if (!tasksWarning) {
                plugin.getLogger().warning("⚠ HIGH TASK QUEUE: " + queuedTasks + " queued tasks (threshold: " + MAX_QUEUED_TASKS + ")");
                tasksWarning = true;
            }
        } else {
            tasksWarning = false;
        }
    }
    
    /**
     * Update metrics from various systems
     */
    private void updateMetrics() {
        if (plugin instanceof com.muzlik.FrostSMPPlugin) {
            com.muzlik.FrostSMPPlugin frostPlugin = (com.muzlik.FrostSMPPlugin) plugin;
            
            // Count active rituals
            activeRituals = 0;
            com.muzlik.ritual.RitualManager ritualManager = frostPlugin.getRitualManager();
            if (ritualManager != null) {
                for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                    if (ritualManager.hasActiveRitual(player)) {
                        activeRituals++;
                    }
                }
            }
            
            // Count active VFX effects
            com.muzlik.vfx.ActiveEffectRegistry effectRegistry = frostPlugin.getEffectRegistry();
            if (effectRegistry != null) {
                activeVFXEffects = effectRegistry.getActiveEffectCount();
            }
            
            // Count queued async tasks
            com.muzlik.data.DataPersistence dataPersistence = frostPlugin.getDataPersistence();
            if (dataPersistence != null) {
                // AsyncExecutor queue size would be tracked here if exposed
                queuedTasks = 0; // Placeholder
            }
        }
    }
    
    /**
     * Get current metrics
     */
    public HealthMetrics getMetrics() {
        updateMetrics();
        return new HealthMetrics(activeRituals, activeVFXEffects, queuedTasks);
    }
    
    /**
     * Check if any warnings are active
     */
    public boolean hasWarnings() {
        return ritualsWarning || vfxWarning || tasksWarning;
    }
    
    /**
     * Get warning messages
     */
    public String getWarningMessages() {
        StringBuilder warnings = new StringBuilder();
        
        if (ritualsWarning) {
            warnings.append("§c⚠ High ritual count: ").append(activeRituals).append("/").append(MAX_RITUALS).append("\n");
        }
        
        if (vfxWarning) {
            warnings.append("§c⚠ High VFX count: ").append(activeVFXEffects).append("/").append(MAX_VFX_EFFECTS).append("\n");
        }
        
        if (tasksWarning) {
            warnings.append("§c⚠ High task queue: ").append(queuedTasks).append("/").append(MAX_QUEUED_TASKS).append("\n");
        }
        
        return warnings.toString();
    }
    
    /**
     * Stop monitoring
     */
    public void shutdown() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
            monitoringTask = null;
        }
        
        plugin.getLogger().info("Health monitoring stopped");
    }
    
    /**
     * Health metrics container
     */
    public static class HealthMetrics {
        public final int activeRituals;
        public final int activeVFXEffects;
        public final int queuedTasks;
        
        public HealthMetrics(int activeRituals, int activeVFXEffects, int queuedTasks) {
            this.activeRituals = activeRituals;
            this.activeVFXEffects = activeVFXEffects;
            this.queuedTasks = queuedTasks;
        }
    }
}
