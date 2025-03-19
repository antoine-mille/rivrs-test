package fr.antoine.rivrs.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.bukkit.configuration.ConfigurationSection;
import fr.antoine.rivrs.Main;

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
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("redis");

        if (config == null) {
            throw new IllegalArgumentException("No redis config found in config.yml!");
        }

        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 6379);
        String password = config.getString("password", "");
        int database = config.getInt("database", 0);
        int timeout = config.getInt("timeout", 2000);

        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(128);
            poolConfig.setMaxIdle(64);
            poolConfig.setMinIdle(16);

            if (!password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout);
            }

            plugin.getLogger().info("Redis connection successfully established.");
        } catch (Exception exception) {
            plugin.getLogger().severe("Error connecting to Redis: " + exception.getMessage());
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
            plugin.getLogger().severe("Error in Redis subscription: " + exception.getMessage());
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
            plugin.getLogger().severe("Error in Redis publication: " + exception.getMessage());
        }
    }

    /**
     * Increments the value of a key in Redis and returns the new value
     *
     * @param key The key to increment
     * @return The new value of the key
     */
    public long incrementAndGet(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        } catch (Exception exception) {
            plugin.getLogger().severe("Error in Redis increment: " + exception.getMessage());
        }
        return 0;
    }

    /**
     * Checks if a key exists in Redis
     *
     * @param key The key to check
     * @return True if the key exists, false otherwise
     */
    public boolean existsKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (Exception exception) {
            plugin.getLogger().severe("Error in Redis exists: " + exception.getMessage());
        }
        return false;
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
            plugin.getLogger().severe("Error in Redis delete: " + exception.getMessage());
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
            plugin.getLogger().severe("Error in Redis get: " + exception.getMessage());
        }
        return null;
    }

    /**
     * Closes the Jedis connection pool
     */
    public void closePool() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            plugin.getLogger().info("Redis connection closed.");
        }
    }
}