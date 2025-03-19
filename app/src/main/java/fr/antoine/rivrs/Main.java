package fr.antoine.rivrs;

import fr.antoine.rivrs.commands.CountCommand;
import fr.antoine.rivrs.dao.PlayerCountDao;
import fr.antoine.rivrs.managers.CountManager;
import fr.antoine.rivrs.persist.PersistManager;
import fr.antoine.rivrs.redis.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main class for the Rivrs plugin
 *
 * @author Antoine
 */
public class Main extends JavaPlugin {

    private RedisManager redisManager;
    private PersistManager persistManager;
    private PlayerCountDao playerCountDao;

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize the Redis and Persist managers
        redisManager = new RedisManager(this);
        persistManager = new PersistManager(this);

        // Initialize the PlayerCountDao
        playerCountDao = new PlayerCountDao(this).createTableIfNotExists();

        // Initialize the CountManager
        var countManager = new CountManager(this);

        getCommand("count").setExecutor(new CountCommand(countManager));
        log("Rivrs has been enabled!", Level.INFO);
    }

    /**
     * Called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        redisManager.closePool();
        persistManager.closeDataSource();
        log("Rivrs has been disabled!", Level.INFO);
    }

    /**
     * Logs a message with the specified level
     *
     * @param message The message to log
     * @param level   The level to log the message at
     */
    public void log(String message, Level level) {
        getLogger().log(level, message);
    }

    /**
     * Gets the Redis manager
     *
     * @return The Redis manager
     */
    public RedisManager getRedisManager() {
        return redisManager;
    }

    /**
     * Gets the Persist manager
     *
     * @return The Persist manager
     */
    public PersistManager getPersistManager() {
        return persistManager;
    }

    /**
     * Gets the PlayerCountDao
     *
     * @return The PlayerCountDao
     */
    public PlayerCountDao getPlayerCountDao() {
        return playerCountDao;
    }

}
