package com.muzlik.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * MySQL implementation of DataStorage
 * Recommended for large networks (500+ players)
 * Requires external MySQL server
 */
public class MySQLStorage implements DataStorage {
    
    private final JavaPlugin plugin;
    private final AsyncExecutor asyncExecutor;
    private final Gson gson;
    private HikariDataSource dataSource;
    
    // Configuration
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;
    
    // SQL Queries
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS %s_player_data (
            player_id VARCHAR(36) PRIMARY KEY,
            data_json LONGTEXT NOT NULL,
            last_updated BIGINT NOT NULL,
            data_version INT DEFAULT 1,
            is_corrupted TINYINT(1) DEFAULT 0,
            INDEX idx_last_updated (last_updated),
            INDEX idx_corrupted (is_corrupted)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
    
    private static final String INSERT_OR_UPDATE = """
        INSERT INTO %s_player_data (player_id, data_json, last_updated, data_version)
        VALUES (?, ?, ?, 1)
        ON DUPLICATE KEY UPDATE
        data_json = VALUES(data_json),
        last_updated = VALUES(last_updated),
        data_version = VALUES(data_version),
        is_corrupted = 0
        """;
    
    private static final String SELECT_DATA = """
        SELECT data_json, is_corrupted FROM %s_player_data WHERE player_id = ?
        """;
    
    private static final String SELECT_EXISTS = """
        SELECT 1 FROM %s_player_data WHERE player_id = ? LIMIT 1
        """;
    
    private static final String DELETE_DATA = """
        DELETE FROM %s_player_data WHERE player_id = ?
        """;
    
    private static final String SELECT_ALL_IDS = """
        SELECT player_id FROM %s_player_data
        """;
    
    private static final String SELECT_STATS = """
        SELECT 
            COUNT(*) as total_players,
            SUM(CHAR_LENGTH(data_json)) as total_size,
            SUM(is_corrupted) as corrupted_entries
        FROM %s_player_data
        """;
    
    private static final String MARK_CORRUPTED = """
        UPDATE %s_player_data SET is_corrupted = 1 WHERE player_id = ?
        """;
    
    private static final String SELECT_CORRUPTED = """
        SELECT player_id, data_json FROM %s_player_data WHERE is_corrupted = 1
        """;
    
    private static final String REPAIR_CORRUPTED = """
        UPDATE %s_player_data SET data_json = ?, is_corrupted = 0 WHERE player_id = ?
        """;
    
    public MySQLStorage(JavaPlugin plugin, AsyncExecutor asyncExecutor, 
                       String host, int port, String database, String username, String password, String tablePrefix) {
        this.plugin = plugin;
        this.asyncExecutor = asyncExecutor;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix != null ? tablePrefix : "frostsmp";
    }
    
    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Setup HikariCP connection pool
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + 
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8");
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                
                // Connection pool settings
                config.setMaximumPoolSize(20);
                config.setMinimumIdle(5);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.setLeakDetectionThreshold(60000);
                
                // MySQL specific settings
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                
                dataSource = new HikariDataSource(config);
                
                // Test connection
                try (Connection conn = dataSource.getConnection()) {
                    plugin.getLogger().info("MySQL connection established successfully");
                }
                
                // Create tables
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    
                    stmt.execute(String.format(CREATE_TABLE, tablePrefix));
                    
                    plugin.getLogger().info("MySQL database initialized successfully");
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize MySQL database: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(UUID playerId, DataPersistence.PlayerDataContainer data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(String.format(INSERT_OR_UPDATE, tablePrefix))) {
                
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
                 PreparedStatement stmt = conn.prepareStatement(String.format(SELECT_DATA, tablePrefix))) {
                
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
                 PreparedStatement stmt = conn.prepareStatement(String.format(SELECT_EXISTS, tablePrefix))) {
                
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
                 PreparedStatement stmt = conn.prepareStatement(String.format(DELETE_DATA, tablePrefix))) {
                
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
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                String timestamp = String.valueOf(System.currentTimeMillis());
                String backupTable = tablePrefix + "_player_data_backup_" + timestamp;
                
                // Create backup table
                stmt.execute("CREATE TABLE " + backupTable + " LIKE " + tablePrefix + "_player_data");
                stmt.execute("INSERT INTO " + backupTable + " SELECT * FROM " + tablePrefix + "_player_data");
                
                plugin.getLogger().info("MySQL backup created: " + backupTable);
                
                // Clean old backups (keep last 5)
                try (ResultSet rs = stmt.executeQuery(
                    "SELECT TABLE_NAME FROM information_schema.TABLES " +
                    "WHERE TABLE_SCHEMA = '" + database + "' " +
                    "AND TABLE_NAME LIKE '" + tablePrefix + "_player_data_backup_%' " +
                    "ORDER BY TABLE_NAME DESC")) {
                    
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        if (count > 5) {
                            String oldBackup = rs.getString("TABLE_NAME");
                            stmt.execute("DROP TABLE " + oldBackup);
                            plugin.getLogger().info("Removed old backup: " + oldBackup);
                        }
                    }
                }
                
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to create MySQL backup: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> migrateFrom(DataStorage fromStorage) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting migration from " + fromStorage.getType() + " to MySQL...");
                
                Set<UUID> playerIds = fromStorage.getAllPlayerIds().join();
                int migrated = 0;
                int failed = 0;
                
                // Use batch processing for better performance
                try (Connection conn = dataSource.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    try (PreparedStatement stmt = conn.prepareStatement(String.format(INSERT_OR_UPDATE, tablePrefix))) {
                        
                        for (UUID playerId : playerIds) {
                            try {
                                DataPersistence.PlayerDataContainer data = fromStorage.loadPlayerData(playerId).join();
                                
                                String json = gson.toJson(data);
                                stmt.setString(1, playerId.toString());
                                stmt.setString(2, json);
                                stmt.setLong(3, System.currentTimeMillis());
                                stmt.addBatch();
                                
                                migrated++;
                                
                                // Execute batch every 100 records
                                if (migrated % 100 == 0) {
                                    stmt.executeBatch();
                                    conn.commit();
                                    plugin.getLogger().info("Migrated " + migrated + "/" + playerIds.size() + " players...");
                                }
                                
                            } catch (Exception e) {
                                plugin.getLogger().warning("Failed to migrate player " + playerId + ": " + e.getMessage());
                                failed++;
                            }
                        }
                        
                        // Execute remaining batch
                        stmt.executeBatch();
                        conn.commit();
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
                 PreparedStatement stmt = conn.prepareStatement(String.format(SELECT_ALL_IDS, tablePrefix));
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
                 PreparedStatement selectStmt = conn.prepareStatement(String.format(SELECT_CORRUPTED, tablePrefix));
                 PreparedStatement repairStmt = conn.prepareStatement(String.format(REPAIR_CORRUPTED, tablePrefix))) {
                
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
                 PreparedStatement stmt = conn.prepareStatement(String.format(SELECT_STATS, tablePrefix));
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
                plugin.getLogger().info("MySQL database connection closed");
            }
        }, asyncExecutor.getExecutor());
    }
    
    @Override
    public StorageType getType() {
        return StorageType.MYSQL;
    }
    
    @Override
    public CompletableFuture<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                
                return rs.next();
                
            } catch (SQLException e) {
                plugin.getLogger().warning("MySQL health check failed: " + e.getMessage());
                return false;
            }
        }, asyncExecutor.getExecutor());
    }
    
    // Helper methods (same as SQLite implementation)
    
    private void markCorrupted(UUID playerId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(String.format(MARK_CORRUPTED, tablePrefix))) {
            
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
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT CREATE_TIME FROM information_schema.TABLES " +
                 "WHERE TABLE_SCHEMA = '" + database + "' " +
                 "AND TABLE_NAME LIKE '" + tablePrefix + "_player_data_backup_%' " +
                 "ORDER BY CREATE_TIME DESC LIMIT 1")) {
            
            if (rs.next()) {
                Timestamp createTime = rs.getTimestamp("CREATE_TIME");
                return createTime != null ? createTime.getTime() : 0;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get last backup time: " + e.getMessage());
        }
        
        return 0;
    }
}