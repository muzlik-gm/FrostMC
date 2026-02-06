package com.muzlik.data;

/**
 * Enum representing different data storage backends
 */
public enum StorageType {
    /**
     * JSON file storage - suitable for small servers (< 50 players)
     * Fast for small datasets, but doesn't scale well
     */
    JSON("json"),
    
    /**
     * SQLite database storage - recommended default for most servers
     * Good performance up to ~500 players, no external dependencies
     */
    SQLITE("sqlite"),
    
    /**
     * MySQL database storage - for large networks and high-performance needs
     * Requires external MySQL server, best for 500+ players
     */
    MYSQL("mysql");
    
    private final String configName;
    
    StorageType(String configName) {
        this.configName = configName;
    }
    
    public String getConfigName() {
        return configName;
    }
    
    /**
     * Parse storage type from config string
     */
    public static StorageType fromConfig(String config) {
        if (config == null) return SQLITE; // Default to SQLite
        
        for (StorageType type : values()) {
            if (type.configName.equalsIgnoreCase(config)) {
                return type;
            }
        }
        
        return SQLITE; // Default fallback
    }
    
    /**
     * Get recommended storage type based on expected player count
     */
    public static StorageType getRecommended(int expectedPlayers) {
        if (expectedPlayers < 50) {
            return JSON;
        } else if (expectedPlayers < 500) {
            return SQLITE;
        } else {
            return MYSQL;
        }
    }
}