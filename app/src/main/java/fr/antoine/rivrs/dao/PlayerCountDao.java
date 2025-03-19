package fr.antoine.rivrs.dao;

import fr.antoine.rivrs.Main;
import fr.antoine.rivrs.persist.PersistManager;
import fr.antoine.rivrs.redis.RedisManager;

import java.util.Optional;
import java.util.logging.Level;

public class PlayerCountDao {

    private static final String COUNT_CHANNEL = "count";

    private final Main plugin;
    private final PersistManager persistManager;
    private final RedisManager redisManager;

    public PlayerCountDao(Main plugin) {
        this.plugin = plugin;
        this.persistManager = plugin.getPersistManager();
        this.redisManager = plugin.getRedisManager();
    }

    /**
     * Get the player count for the specified player from the cache or database if not in cache
     *
     * @param playerName The name of the player
     * @return The player count if found or empty if not found
     */
    public Optional<Long> getPlayerCount(String playerName) {
        var cacheKey = getKeyName(playerName);
        var cacheValue = redisManager.getValue(cacheKey);
        if (cacheValue != null) { // Already in cache so return it
            return Optional.of(Long.parseLong(cacheValue));
        }

        var query = "SELECT count FROM player_counts WHERE player_name = ?";
        try (var connection = persistManager.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.setString(1, playerName);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                var count = resultSet.getLong("count");
                redisManager.setValue(cacheKey, String.valueOf(count));
                return Optional.of(count);
            }
        } catch (Exception exception) {
            plugin.log("Error getting player count: " + exception.getMessage(), Level.SEVERE);
        }
        return Optional.empty();
    }

    /**
     * Upsert the player count for the specified player
     *
     * @param playerName The name of the player
     * @param count      The count to set
     */
    public void upsertPlayerCount(String playerName, long count) {
        var query = "INSERT INTO player_counts (player_name, count) VALUES (?, ?) ON DUPLICATE KEY UPDATE count = ?";
        try (var connection = persistManager.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.setString(1, playerName);
            statement.setLong(2, count);
            statement.setLong(3, count);
            statement.executeUpdate();

            // Update the cache with the new count
            var cacheKey = getKeyName(playerName);
            redisManager.setValue(cacheKey, String.valueOf(count));
        } catch (Exception exception) {
            plugin.log("Error upserting player count: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Delete the player count for the specified player from the database and cache
     *
     * @param playerName The name of the player
     */
    public void deletePlayerCount(String playerName) {
        var query = "DELETE FROM player_counts WHERE player_name = ?";
        try (var connection = persistManager.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.setString(1, playerName);
            statement.executeUpdate();

            // Remove the count from the cache
            var cacheKey = getKeyName(playerName);
            redisManager.deleteKey(cacheKey);
        } catch (Exception exception) {
            plugin.log("Error deleting player count: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Create the player_counts table if it does not exist
     *
     * @return The PlayerCountDao instance
     */
    public PlayerCountDao createTableIfNotExists() {
        var query = "CREATE TABLE IF NOT EXISTS player_counts (player_name VARCHAR(255) PRIMARY KEY, count BIGINT NOT NULL)";
        try (var connection = persistManager.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (Exception exception) {
            plugin.log("Error creating table: " + exception.getMessage(), Level.SEVERE);
        }
        return this;
    }

    /**
     * Get the key name for the player count
     *
     * @param playerName The name of the player
     * @return The key name
     */
    private String getKeyName(String playerName) {
        return COUNT_CHANNEL + ":" + playerName;
    }

}
