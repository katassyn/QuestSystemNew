package org.maks.questsystem.database;

import org.maks.questsystem.QuestSystem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    
    private final QuestSystem plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(QuestSystem plugin) {
        this.plugin = plugin;
    }
    
    public void connect() throws SQLException {
        HikariConfig config = new HikariConfig();
        
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String database = plugin.getConfig().getString("database.name");
        String username = plugin.getConfig().getString("database.user");
        String password = plugin.getConfig().getString("database.password");
        
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
        config.setUsername(username);
        config.setPassword(password);
        
        // Optimization settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        
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
        try (Connection connection = dataSource.getConnection()) {
            plugin.getLogger().info("Database connection established successfully!");
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }
    
    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}