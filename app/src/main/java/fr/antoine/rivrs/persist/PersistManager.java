package fr.antoine.rivrs.persist;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.antoine.rivrs.Main;

import java.sql.Connection;

/**
 * Manager for handling persistence of data
 */
public class PersistManager {

    private final Main plugin;

    private HikariDataSource dataSource;

    /**
     * Constructor for the PersistManager class
     *
     * @param plugin The main plugin instance
     */
    public PersistManager(Main plugin) {
        this.plugin = plugin;
        setupDataSource();
    }

    /**
     * Sets up the data source for the database connection pool using the configuration in config.yml
     */
    private void setupDataSource() {
        var config = plugin.getConfig().getConfigurationSection("database");
        if (config == null) {
            throw new IllegalArgumentException("No database config found in config.yml!");
        }

        try {
            // Load the MariaDB driver class
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            plugin.getLogger().severe("Error loading MariaDB driver: " + exception.getMessage());
            return;
        }

        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("url", "jdbc:mariadb://localhost:3306/database"));
        hikariConfig.setUsername(config.getString("username", "root"));
        hikariConfig.setPassword(config.getString("password", "password"));
        hikariConfig.setMaximumPoolSize(config.getInt("max-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("min-idle", 2));
        hikariConfig.setIdleTimeout(config.getLong("idle-timeout", 30000));
        hikariConfig.setMaxLifetime(config.getLong("max-lifetime", 1800000));
        hikariConfig.setConnectionTimeout(config.getLong("connection-timeout", 5000));

        dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * Retrieves a connection to the database from the connection pool
     *
     * @return The database connection or null if an error occurred
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception exception) {
            plugin.getLogger().severe("Error getting database connection: " + exception.getMessage());
            return null;
        }
    }

    /**
     * Closes the data source and releases all connections
     */
    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
