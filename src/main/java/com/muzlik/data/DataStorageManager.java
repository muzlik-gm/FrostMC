package com.muzlik.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main data storage manager that handles different storage backends
 * Provides automatic migration, health monitoring, and backup scheduling
 */
public class DataStorageManager {
    
    private final JavaPlugin plugin;
    private final AsyncExecutor asyncExecutor;
    private DataStorage currentStorage;
    private ScheduledExecutorService scheduler;
    
    // Configuration
    private StorageType storageType;
    private boolean autoMigrationEnabled;
    private int migrationTimeoutMinutes;
    private boolean autoBackupEnabled;
    private int autoBackupInterval; // minutes
    private boolean autoRepairEnabled;
    private int healthCheckInterval; // minutes
    
    public DataStorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.asyncExecutor = new AsyncExecutor(plugin);
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "FrostSMP-DataStorage");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Initialize the data storage system
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Load configuration
                loadConfiguration();
                
                // Detect and handle storage migration if needed
                StorageType detectedType = detectExistingStorage();
                if (detectedType != null && detectedType != storageType) {
                    plugin.getLogger().info("Detected existing " + detectedType + " storage, but config specifies " + storageType);
                    
                    if (autoMigrationEnabled && shouldAutoMigrate(detectedType, storageType)) {
                        plugin.getLogger().info("Auto-migrating from " + detectedType + " to " + storageType + "...");
                        try {
                            performMigration(detectedType, storageType).join();
                        } catch (Exception e) {
                            plugin.getLogger().severe("Auto-migration failed: " + e.getMessage());
                            plugin.getLogger().warning("Falling back to existing " + detectedType + " storage");
                            plugin.getLogger().warning("You can try manual migration later with /fragment migrate command");
                            // Use existing storage as fallback
                            storageType = detectedType;
                        }
                    } else {
                        if (!autoMigrationEnabled) {
                            plugin.getLogger().info("Auto-migration is disabled in config");
                        }
                        plugin.getLogger().warning("Manual migration required! Use /fragment migrate command");
                        // Use existing storage for now
                        storageType = detectedType;
                    }
                }
                
                // Initialize the selected storage
                currentStorage = createStorage(storageType);
                currentStorage.initialize().join();
                
                // Start background tasks
                startBackgroundTasks();
                
                plugin.getLogger().info("Data storage initialized successfully using " + storageType);
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to initialize data storage: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    /**
     * Get the current storage instance
     */
    public DataStorage getStorage() {
        return currentStorage;
    }
    
    /**
     * Migrate from one storage type to another
     */
    public CompletableFuture<Void> migrateStorage(StorageType fromType, StorageType toType) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting manual migration from " + fromType + " to " + toType + "...");
                
                // Create backup before migration
                plugin.getLogger().info("Creating backup before migration...");
                currentStorage.createBackup().join();
                
                // Perform migration
                performMigration(fromType, toType).join();
                
                plugin.getLogger().info("Migration completed successfully!");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Migration failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    /**
     * Get storage statistics
     */
    public CompletableFuture<DataStorage.StorageStats> getStats() {
        if (currentStorage == null) {
            return CompletableFuture.completedFuture(
                new DataStorage.StorageStats(0, 0, 0, 0, false)
            );
        }
        return currentStorage.getStats();
    }
    
    /**
     * Perform manual backup
     */
    public CompletableFuture<Void> createBackup() {
        if (currentStorage == null) {
            return CompletableFuture.completedFuture(null);
        }
        return currentStorage.createBackup();
    }
    
    /**
     * Repair corrupted data
     */
    public CompletableFuture<Integer> repairCorruptedData() {
        if (currentStorage == null) {
            return CompletableFuture.completedFuture(0);
        }
        return currentStorage.repairCorruptedData();
    }
    
    /**
     * Check storage health
     */
    public CompletableFuture<Boolean> isHealthy() {
        if (currentStorage == null) {
            return CompletableFuture.completedFuture(false);
        }
        return currentStorage.isHealthy();
    }
    
    /**
     * Shutdown the storage system
     */
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Stop background tasks
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Shutdown current storage
                if (currentStorage != null) {
                    currentStorage.shutdown().join();
                }
                
                // Shutdown async executor
                asyncExecutor.shutdown();
                
                plugin.getLogger().info("Data storage shutdown completed");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Error during storage shutdown: " + e.getMessage());
            }
        }, asyncExecutor.getExecutor());
    }
    
    // Private helper methods
    
    private void loadConfiguration() {
        ConfigurationSection config = plugin.getConfig();
        
        // Storage type configuration
        String storageTypeStr = config.getString("data.storage_type", "sqlite");
        this.storageType = StorageType.fromConfig(storageTypeStr);
        
        // Migration configuration
        this.autoMigrationEnabled = config.getBoolean("data.auto_migration.enabled", true);
        this.migrationTimeoutMinutes = config.getInt("data.auto_migration.timeout_minutes", 5);
        
        // Backup configuration
        this.autoBackupEnabled = config.getBoolean("data.auto_backup.enabled", true);
        this.autoBackupInterval = config.getInt("data.auto_backup.interval_minutes", 60);
        
        // Repair configuration
        this.autoRepairEnabled = config.getBoolean("data.auto_repair.enabled", true);
        this.healthCheckInterval = config.getInt("data.health_check.interval_minutes", 30);
        
        // Validate configuration
        if (migrationTimeoutMinutes < 1) {
            plugin.getLogger().warning("Migration timeout too low, setting to 1 minute");
            migrationTimeoutMinutes = 1;
        }
        
        if (autoBackupInterval < 5) {
            plugin.getLogger().warning("Auto backup interval too low, setting to 5 minutes");
            autoBackupInterval = 5;
        }
        
        if (healthCheckInterval < 5) {
            plugin.getLogger().warning("Health check interval too low, setting to 5 minutes");
            healthCheckInterval = 5;
        }
        
        plugin.getLogger().info("Storage configuration loaded: type=" + storageType + 
            ", auto_migration=" + autoMigrationEnabled + " (" + migrationTimeoutMinutes + "m timeout)" +
            ", auto_backup=" + autoBackupEnabled + " (" + autoBackupInterval + "m)" +
            ", auto_repair=" + autoRepairEnabled + " (" + healthCheckInterval + "m)");
    }
    
    private StorageType detectExistingStorage() {
        // Check for existing JSON files
        java.io.File jsonDir = new java.io.File(plugin.getDataFolder(), "playerdata");
        if (jsonDir.exists() && jsonDir.listFiles((dir, name) -> name.endsWith(".json")) != null) {
            java.io.File[] jsonFiles = jsonDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (jsonFiles != null && jsonFiles.length > 0) {
                return StorageType.JSON;
            }
        }
        
        // Check for existing SQLite database
        java.io.File sqliteFile = new java.io.File(plugin.getDataFolder(), "playerdata.db");
        if (sqliteFile.exists() && sqliteFile.length() > 0) {
            return StorageType.SQLITE;
        }
        
        // No existing storage detected
        return null;
    }
    
    private boolean shouldAutoMigrate(StorageType from, StorageType to) {
        // Auto-migrate from JSON to SQLite (performance improvement)
        if (from == StorageType.JSON && to == StorageType.SQLITE) {
            return true;
        }
        
        // Auto-migrate from JSON to MySQL (performance improvement)
        if (from == StorageType.JSON && to == StorageType.MYSQL) {
            return true;
        }
        
        // Don't auto-migrate from SQLite/MySQL to JSON (potential data loss)
        // Don't auto-migrate between SQLite and MySQL (requires manual decision)
        return false;
    }
    
    private CompletableFuture<Void> performMigration(StorageType fromType, StorageType toType) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting migration from " + fromType + " to " + toType + "...");
                
                // Create source storage
                DataStorage fromStorage = createStorage(fromType);
                fromStorage.initialize().join();
                
                // Create destination storage
                DataStorage toStorage = createStorage(toType);
                toStorage.initialize().join();
                
                // Add timeout to migration process
                CompletableFuture<Void> migrationFuture = toStorage.migrateFrom(fromStorage);
                
                try {
                    // Wait for migration with configurable timeout
                    migrationFuture.get(migrationTimeoutMinutes, TimeUnit.MINUTES);
                } catch (java.util.concurrent.TimeoutException e) {
                    plugin.getLogger().severe("Migration timed out after " + migrationTimeoutMinutes + " minutes! This may indicate a problem with the source data.");
                    plugin.getLogger().severe("Please check your data files and try manual migration with /fragment migrate command");
                    throw new RuntimeException("Migration timeout", e);
                } catch (Exception e) {
                    plugin.getLogger().severe("Migration failed with error: " + e.getMessage());
                    throw new RuntimeException("Migration failed", e);
                }
                
                // Update current storage
                if (currentStorage != null) {
                    currentStorage.shutdown().join();
                }
                currentStorage = toStorage;
                
                // Shutdown old storage
                fromStorage.shutdown().join();
                
                plugin.getLogger().info("Migration from " + fromType + " to " + toType + " completed successfully");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Migration failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    private DataStorage createStorage(StorageType type) {
        switch (type) {
            case JSON:
                return new JSONStorage(plugin, asyncExecutor);
                
            case SQLITE:
                return new SQLiteStorage(plugin, asyncExecutor);
                
            case MYSQL:
                ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("data.mysql");
                if (mysqlConfig == null) {
                    throw new IllegalStateException("MySQL storage selected but no MySQL configuration found!");
                }
                
                String host = mysqlConfig.getString("host", "localhost");
                int port = mysqlConfig.getInt("port", 3306);
                String database = mysqlConfig.getString("database", "frostsmp");
                String username = mysqlConfig.getString("username", "root");
                String password = mysqlConfig.getString("password", "");
                String tablePrefix = mysqlConfig.getString("table_prefix", "frostsmp");
                
                return new MySQLStorage(plugin, asyncExecutor, host, port, database, username, password, tablePrefix);
                
            default:
                throw new IllegalArgumentException("Unsupported storage type: " + type);
        }
    }
    
    private void startBackgroundTasks() {
        // Auto backup task
        if (autoBackupEnabled) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    plugin.getLogger().info("Starting automatic backup...");
                    currentStorage.createBackup().join();
                    plugin.getLogger().info("Automatic backup completed");
                } catch (Exception e) {
                    plugin.getLogger().warning("Automatic backup failed: " + e.getMessage());
                }
            }, autoBackupInterval, autoBackupInterval, TimeUnit.MINUTES);
            
            plugin.getLogger().info("Automatic backup scheduled every " + autoBackupInterval + " minutes");
        }
        
        // Health check and auto repair task
        if (autoRepairEnabled) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    // Check storage health
                    boolean healthy = currentStorage.isHealthy().join();
                    if (!healthy) {
                        plugin.getLogger().warning("Storage health check failed! Attempting repair...");
                        
                        // Attempt to repair corrupted data
                        int repaired = currentStorage.repairCorruptedData().join();
                        if (repaired > 0) {
                            plugin.getLogger().info("Repaired " + repaired + " corrupted entries");
                        }
                        
                        // Check health again
                        healthy = currentStorage.isHealthy().join();
                        if (healthy) {
                            plugin.getLogger().info("Storage health restored");
                        } else {
                            plugin.getLogger().severe("Storage health check still failing after repair!");
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Health check failed: " + e.getMessage());
                }
            }, healthCheckInterval, healthCheckInterval, TimeUnit.MINUTES);
            
            plugin.getLogger().info("Health monitoring scheduled every " + healthCheckInterval + " minutes");
        }
    }
    
    /**
     * Get recommended storage type based on server size
     */
    public static StorageType getRecommendedStorageType(int expectedPlayers) {
        return StorageType.getRecommended(expectedPlayers);
    }
    
    /**
     * Validate storage configuration
     */
    public static boolean validateStorageConfig(JavaPlugin plugin, StorageType type) {
        switch (type) {
            case JSON:
            case SQLITE:
                // No additional configuration needed
                return true;
                
            case MYSQL:
                ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("data.mysql");
                if (mysqlConfig == null) {
                    plugin.getLogger().severe("MySQL storage selected but no MySQL configuration section found!");
                    return false;
                }
                
                String host = mysqlConfig.getString("host");
                String database = mysqlConfig.getString("database");
                String username = mysqlConfig.getString("username");
                
                if (host == null || host.isEmpty()) {
                    plugin.getLogger().severe("MySQL host not configured!");
                    return false;
                }
                
                if (database == null || database.isEmpty()) {
                    plugin.getLogger().severe("MySQL database not configured!");
                    return false;
                }
                
                if (username == null || username.isEmpty()) {
                    plugin.getLogger().severe("MySQL username not configured!");
                    return false;
                }
                
                return true;
                
            default:
                plugin.getLogger().severe("Unknown storage type: " + type);
                return false;
        }
    }
}