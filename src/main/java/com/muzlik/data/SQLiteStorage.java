package com.muzlik.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * SQLite implementation of DataStorage
 * Recommended for most servers (up to ~500 players)
 * No external dependencies, good performance
 */
public class SQLiteStorage implements DataStorage {
    
    private final JavaPlugin plugin;
    private final AsyncExecutor asyncExecutor;
    private final Gson gson;
    private HikariDataSource dataSource;
    private final File databaseFile;
    
    // SQL Queries
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS player_data (
            player_id TEXT PRIMARY KEY,
            data_json TEXT NOT NULL,
            last_updated INTEGER NOT NULL,
            data_version INTEGER DEFAULT 1,
            is_corrupted INTEGER DEFAULT 0
        )
        """;
    
    private static final String CREATE_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_last_updated ON player_data(last_updated)
        """;
    
    private static final String INSERT_OR_UPDATE = """
        INSERT OR REPLACE INTO player_data (player_id, data_json, last_updated, data_version)
        VALUES (?, ?, ?, 1)
        """;
    
    private static final String SELECT_DATA = """
        SELECT data_json, is_corrupted FROM player_data WHERE player_id = ?
        """;
    
    private static final String SELECT_EXISTS = """
        SELECT 1 FROM player_data WHERE player_id = ? LIMIT 1
        """;
    
    private static final String DELETE_DATA = """
        DELETE FROM player_data WHERE player_id = ?
        """;
    
    private static final String SELECT_ALL_IDS = """
        SELECT player_id FROM player_data
        """;
    
    private static final String SELECT_STATS = """
        SELECT 
            COUNT(*) as total_players,
            SUM(LENGTH(data_json)) as total_size,
            SUM(is_corrupted) as corrupted_entries
        FROM player_data
        """;
    
    private static final String MARK_CORRUPTED = """
        UPDATE player_data SET is_corrupted = 1 WHERE player_id = ?
        """;
    
    private static final String SELECT_CORRUPTED = """
        SELECT player_id, data_json FROM player_data WHERE is_corrupted = 1
        """;
    
    private static final String REPAIR_CORRUPTED = """
        UPDATE player_data SET data_json = ?, is_corrupted = 0 WHERE player_id = ?
        """;
    
    public SQLiteStorage(JavaPlugin plugin, AsyncExecutor asyncExecutor) {
        this.plugin = plugin;
        this.asyncExecutor = asyncExecutor;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.databaseFile = new File(plugin.getDataFolder(), "playerdata.db");
    }
    
    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Ensure data folder exists
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                
                // Setup HikariCP connection pool
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.setLeakDetectionThreshold(60000);
                
                // SQLite specific settings
                config.addDataSourceProperty("journal_mode", "WAL");
                config.addDataSourceProperty("synchronous", "NORMAL");
                config.addDataSourceProperty("cache_size", "10000");
                config.addDataSourceProperty("temp_store", "MEMORY");
                
                dataSource = new HikariDataSource(config);
                
                // Create tables
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    
                    stmt.execute(CREATE_TABLE);
                    stmt.execute(CREATE_INDEX);
                    
                    plugin.getLogger().info("SQLite database initialized successfully");
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(UUID playerId, DataPersistence.PlayerDataContainer data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(INSERT_OR_UPDATE)) {
                
                String json = gson.toJson(data);
                stmt.setString(1, playerId.toString());
                stmt.setString(2, json);
                stmt.setLong(3, System.currentTimeMillis());
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save data for player " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<DataPersistence.PlayerDataContainer> loadPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SELECT_DATA)) {
                
                stmt.setString(1, playerId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String json = rs.getString("data_json");
                        boolean isCorrupted = rs.getBoolean("is_corrupted");
                        
                        if (isCorrupted) {
                            plugin.getLogger().warning("Loading corrupted data for player " + playerId + ", using defaults");
                            return createDefaultData(playerId);
                        }
                        
                        try {
                            DataPersistence.PlayerDataContainer data = gson.fromJson(json, DataPersistence.PlayerDataContainer.class);
                            if (data == null) {
                                plugin.getLogger().warning("Null data for player " + playerId + ", marking as corrupted");
                                markCorrupted(playerId);
                                return createDefaultData(playerId);
                            }
                            
                            // Validate data
                            if (!validateData(data)) {
                                plugin.getLogger().warning("Invalid data for player " + playerId + ", attempting repair");
                                data = repairData(data, playerId);
                                // Save repaired data
                                savePlayerData(playerId, data).join();
                            }
                            
                            return data;
                            
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to parse data for player " + playerId + ": " + e.getMessage());
                            markCorrupted(playerId);
                            return createDefaultData(playerId);
                        }
                    } else {
                        return createDefaultData(playerId);
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load data for player " + playerId + ": " + e.getMessage());
                return createDefaultData(playerId);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Boolean> hasPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SELECT_EXISTS)) {
                
                stmt.setString(1, playerId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to check data existence for player " + playerId + ": " + e.getMessage());
                return false;
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> deletePlayerData(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(DELETE_DATA)) {
                
                stmt.setString(1, playerId.toString());
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete data for player " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> createBackup() {
        return CompletableFuture.runAsync(() -> {
            try {
                File backupDir = new File(plugin.getDataFolder(), "backups");
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                
                String timestamp = String.valueOf(System.currentTimeMillis());
                File backupFile = new File(backupDir, "playerdata_" + timestamp + ".db");
                
                // Copy database file
                java.nio.file.Files.copy(databaseFile.toPath(), backupFile.toPath());
                
                plugin.getLogger().info("Database backup created: " + backupFile.getName());
                
                // Clean old backups (keep last 10)
                File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("playerdata_") && name.endsWith(".db"));
                if (backups != null && backups.length > 10) {
                    java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
                    for (int i = 0; i < backups.length - 10; i++) {
                        backups[i].delete();
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> migrateFrom(DataStorage fromStorage) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting migration from " + fromStorage.getType() + " to SQLite...");
                
                Set<UUID> playerIds = fromStorage.getAllPlayerIds().join();
                int migrated = 0;
                int failed = 0;
                
                for (UUID playerId : playerIds) {
                    try {
                        DataPersistence.PlayerDataContainer data = fromStorage.loadPlayerData(playerId).join();
                        savePlayerData(playerId, data).join();
                        migrated++;
                        
                        if (migrated % 100 == 0) {
                            plugin.getLogger().info("Migrated " + migrated + "/" + playerIds.size() + " players...");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to migrate player " + playerId + ": " + e.getMessage());
                        failed++;
                    }
                }
                
                plugin.getLogger().info("Migration completed: " + migrated + " successful, " + failed + " failed");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Migration failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Set<UUID>> getAllPlayerIds() {
        return CompletableFuture.supplyAsync(() -> {
            Set<UUID> playerIds = new HashSet<>();
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_IDS);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    try {
                        UUID playerId = UUID.fromString(rs.getString("player_id"));
                        playerIds.add(playerId);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in database: " + rs.getString("player_id"));
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get all player IDs: " + e.getMessage());
            }
            
            return playerIds;
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Integer> repairCorruptedData() {
        return CompletableFuture.supplyAsync(() -> {
            int repaired = 0;
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement selectStmt = conn.prepareStatement(SELECT_CORRUPTED);
                 PreparedStatement repairStmt = conn.prepareStatement(REPAIR_CORRUPTED)) {
                
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        String playerIdStr = rs.getString("player_id");
                        String corruptedJson = rs.getString("data_json");
                        
                        try {
                            UUID playerId = UUID.fromString(playerIdStr);
                            
                            // Attempt to repair the data
                            DataPersistence.PlayerDataContainer data;
                            try {
                                data = gson.fromJson(corruptedJson, DataPersistence.PlayerDataContainer.class);
                                if (data == null) {
                                    data = createDefaultData(playerId);
                                } else {
                                    data = repairData(data, playerId);
                                }
                            } catch (Exception e) {
                                data = createDefaultData(playerId);
                            }
                            
                            // Save repaired data
                            String repairedJson = gson.toJson(data);
                            repairStmt.setString(1, repairedJson);
                            repairStmt.setString(2, playerIdStr);
                            repairStmt.executeUpdate();
                            
                            repaired++;
                            
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid UUID in corrupted data: " + playerIdStr);
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to repair corrupted data: " + e.getMessage());
            }
            
            if (repaired > 0) {
                plugin.getLogger().info("Repaired " + repaired + " corrupted data entries");
            }
            
            return repaired;
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<StorageStats> getStats() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SELECT_STATS);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int totalPlayers = rs.getInt("total_players");
                    long totalSize = rs.getLong("total_size");
                    int corruptedEntries = rs.getInt("corrupted_entries");
                    long lastBackup = getLastBackupTime();
                    boolean healthy = isHealthy().join();
                    
                    return new StorageStats(totalPlayers, totalSize, corruptedEntries, lastBackup, healthy);
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get storage stats: " + e.getMessage());
            }
            
            return new StorageStats(0, 0, 0, 0, false);
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                plugin.getLogger().info("SQLite database connection closed");
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public StorageType getType() {
        return StorageType.SQLITE;
    }
    
    @Override
    public CompletableFuture<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                
                return rs.next();
                
            } catch (SQLException e) {
                plugin.getLogger().warning("Database health check failed: " + e.getMessage());
                return false;
            }
        }, asyncExecutor.getExecutor());
    }
    
    // Helper methods
    
    private void markCorrupted(UUID playerId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(MARK_CORRUPTED)) {
            
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to mark data as corrupted for player " + playerId);
        }
    }
    
    private boolean validateData(DataPersistence.PlayerDataContainer data) {
        if (data == null) return false;
        if (data.playerId == null || data.playerId.isEmpty()) return false;
        if (data.fragments == null) return false;
        if (data.characterLevel < 1 || data.characterLevel > 100) return false;
        if (data.characterXp < 0) return false;
        return true;
    }
    
    private DataPersistence.PlayerDataContainer repairData(DataPersistence.PlayerDataContainer data, UUID playerId) {
        if (data == null) {
            return createDefaultData(playerId);
        }
        
        // Repair basic fields
        if (data.playerId == null || data.playerId.isEmpty()) {
            data.playerId = playerId.toString();
        }
        
        if (data.fragments == null) {
            data.fragments = new java.util.HashMap<>();
        }
        
        // Clamp values to valid ranges
        data.characterLevel = Math.max(1, Math.min(100, data.characterLevel));
        data.characterXp = Math.max(0, data.characterXp);
        
        // Initialize missing fields
        if (data.completedRituals == null) {
            data.completedRituals = new java.util.ArrayList<>();
        }
        
        if (data.unlockedAbilitySlots == null) {
            data.unlockedAbilitySlots = new java.util.HashMap<>();
        }
        
        if (data.uiMode == null) {
            data.uiMode = "STANDARD";
        }
        
        return data;
    }
    
    private DataPersistence.PlayerDataContainer createDefaultData(UUID playerId) {
        DataPersistence.PlayerDataContainer data = new DataPersistence.PlayerDataContainer();
        data.playerId = playerId.toString();
        data.activeFragment = null;
        data.uiMode = "STANDARD";
        data.lastFragmentChange = 0;
        data.fragments = new java.util.HashMap<>();
        data.completedRituals = new java.util.ArrayList<>();
        data.characterLevel = 1;
        data.characterXp = 0;
        data.unlockedAbilitySlots = new java.util.HashMap<>();
        return data;
    }
    
    private long getLastBackupTime() {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) return 0;
        
        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("playerdata_") && name.endsWith(".db"));
        if (backups == null || backups.length == 0) return 0;
        
        long latest = 0;
        for (File backup : backups) {
            latest = Math.max(latest, backup.lastModified());
        }
        
        return latest;
    }
}