package fr.antoine.rivrs.redis;

import fr.antoine.rivrs.Main;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.logging.Level;

/**
 * Manages the Redis connection and provides methods for subscribing to channels and publishing messages
 */
public class RedisManager {

    private final Main plugin;

    private JedisPool jedisPool;

    /**
     * Constructor for the RedisManager class
     *
     * @param plugin The main plugin instance
     */
    public RedisManager(Main plugin) {
        this.plugin = plugin;
        setupRedisConnection();
    }

    /**
     * Setup the Redis connection
     */
    private void setupRedisConnection() {
        var config = plugin.getConfig().getConfigurationSection("redis");

        if (config == null) {
            throw new IllegalArgumentException("No redis config found in config.yml!");
        }

        var host = config.getString("host", "localhost");
        var port = config.getInt("port", 6379);
        var password = config.getString("password", "");
        var database = config.getInt("database", 0);
        var timeout = config.getInt("timeout", 2000);

        try {
            var poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(128);
            poolConfig.setMaxIdle(64);
            poolConfig.setMinIdle(16);

            if (!password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout);
            }

            plugin.log("Connected to Redis at " + host + ":" + port, Level.INFO);
        } catch (Exception exception) {
            plugin.log("Error connecting to Redis: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Subscribe to multiple channels and execute an action for each received message
     *
     * @param messageHandler The action to execute when a message is received
     * @param channels       The channels to subscribe to
     */
    public void subscribe(MessageHandler messageHandler, String... channels) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(new RedisPubSubListener(plugin, messageHandler), channels);
        } catch (Exception exception) {
            plugin.log("Error subscribing to Redis channels: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Publishes a message to a Redis channel
     *
     * @param channel The channel to publish the message to
     * @param message The message to publish
     */
    public void publish(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, message);
        } catch (Exception exception) {
            plugin.log("Error publishing to Redis: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Sets the value of a key in Redis
     *
     * @param key   The key to set
     * @param value The value to set
     */
    public void setValue(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        } catch (Exception exception) {
            plugin.log("Error in Redis setValue: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Deletes a key from Redis
     *
     * @param key The key to delete
     */
    public void deleteKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception exception) {
            plugin.log("Error in Redis deleteKey: " + exception.getMessage(), Level.SEVERE);
        }
    }

    /**
     * Gets the value of a key in Redis
     *
     * @param key The key to get
     * @return The value of the key
     */
    public String getValue(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception exception) {
            plugin.log("Error in Redis getValue: " + exception.getMessage(), Level.SEVERE);
        }
        return null;
    }

    /**
     * Closes the Jedis connection pool
     */
    public void closePool() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            plugin.log("Closed Redis connection pool", Level.INFO);
        }
    }
}