package moe.niso.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import moe.niso.NisoPlugin;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final NisoPlugin plugin = NisoPlugin.getInstance();
    private HikariDataSource dataSource;

    /**
     * Returns the HikariDataSource object.
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets up the connection pool to the PostgreSQL database.
     */
    public void setupDatabase() {
        try {
            // Load database settings from config.yml
            final String host = plugin.getConfig().getString("database.host");
            final String database = plugin.getConfig().getString("database.database");
            final String username = plugin.getConfig().getString("database.username");
            final String password = plugin.getConfig().getString("database.password");
            final int port = plugin.getConfig().getInt("database.port", 5432);
            final int poolSize = plugin.getConfig().getInt("database.pool-size", 10);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);

            // PostgreSQL optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            // Initialize the connection pool
            dataSource = new HikariDataSource(config);

            // Try getting a connection to verify that the pool is set up correctly
            try (Connection ignored = dataSource.getConnection()) {
                plugin.getLogger().info(plugin.logPrefixDatabase + "PostgreSQL pool initialized successfully!");
            } catch (SQLException e) {
                plugin.getLogger().severe(plugin.logPrefixDatabase + "Error establishing connection to PostgreSQL: " + e.getMessage());
                plugin.getLogger().severe(plugin.logPrefixDatabase + "Disabling plugin...");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        } catch (Exception e) {
            plugin.getLogger().severe(plugin.logPrefixDatabase + "Error initializing PostgreSQL pool: " + e.getMessage());
            plugin.getLogger().severe(plugin.logPrefixDatabase + "Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Closes the connection pool to the PostgreSQL database.
     */
    public void closeDatabase() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                plugin.getLogger().info(plugin.logPrefixDatabase + "PostgreSQL pool closed.");
            } catch (Exception e) {
                plugin.getLogger().warning(plugin.logPrefixDatabase + "Error closing PostgreSQL pool: " + e.getMessage());
            }
        }
    }

    /**
     * Creates the homes table in the database.
     */
    public void createHomesTable() {
        try (Connection connection = dataSource.getConnection()) {
            // Create the homes table if it doesn't exist
            final String homesTableSQL = "CREATE TABLE IF NOT EXISTS homes (" + "creator_uuid UUID NOT NULL, " + "home_name VARCHAR(255) NOT NULL, " + "world VARCHAR(255) NOT NULL, " + "x DOUBLE PRECISION NOT NULL, " + "y DOUBLE PRECISION NOT NULL, " + "z DOUBLE PRECISION NOT NULL, " + "pitch FLOAT NOT NULL, " + "yaw FLOAT NOT NULL, " + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "PRIMARY KEY (creator_uuid, home_name)" + ");";
            try (PreparedStatement ps = connection.prepareStatement(homesTableSQL)) {
                ps.executeUpdate();
                plugin.getLogger().info(plugin.logPrefixDatabase + "Homes table created (if it didn't exist).");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(plugin.logPrefixDatabase + "Error creating Homes table: " + e.getMessage());
        }
    }

    /**
     * Creates the warps table in the database.
     */
    public void createWarpsTable() {
        try (Connection connection = dataSource.getConnection()) {
            // Create the warps table if it doesn't exist
            final String warpsTableSQL = "CREATE TABLE IF NOT EXISTS warps (" + "warp_name VARCHAR(255) NOT NULL, " + "creator_uuid UUID NOT NULL, " + "world VARCHAR(255) NOT NULL, " + "x DOUBLE PRECISION NOT NULL, " + "y DOUBLE PRECISION NOT NULL, " + "z DOUBLE PRECISION NOT NULL, " + "pitch FLOAT NOT NULL, " + "yaw FLOAT NOT NULL, " + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "PRIMARY KEY (warp_name)" + ");";
            try (PreparedStatement ps = connection.prepareStatement(warpsTableSQL)) {
                ps.executeUpdate();
                plugin.getLogger().info(plugin.logPrefixDatabase + "Warps table created (if it didn't exist).");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(plugin.logPrefixDatabase + "Error creating Warps table: " + e.getMessage());
        }
    }
}
