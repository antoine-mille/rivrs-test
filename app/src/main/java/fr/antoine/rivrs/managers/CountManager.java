package fr.antoine.rivrs.managers;

import fr.antoine.rivrs.Main;
import fr.antoine.rivrs.dao.PlayerCountDao;
import fr.antoine.rivrs.redis.RedisManager;
import fr.antoine.rivrs.utils.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Manager for counting player interactions
 */
public class CountManager {

    private static final String COUNT_CHANNEL = "count";
    private static final String DEFAULT_COUNT_WIN_MESSAGE = "<red>Player %player% just finished!</red>";
    private static final String DEFAULT_COUNT_NOTIFY_MESSAGE = "<red>Progression: %count%/%maxcount%</red>";

    private final Main plugin;
    private final RedisManager redisManager;
    private final PlayerCountDao playerCountDao;

    private int maxCount;
    private String countWinMessage;
    private String countNotifyMessage;

    /**
     * Constructor for the CountManager class
     *
     * @param plugin The main plugin instance
     */
    public CountManager(Main plugin) {
        this.plugin = plugin;
        this.redisManager = plugin.getRedisManager();
        this.playerCountDao = plugin.getPlayerCountDao();

        initialize();
        subscribe();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::notifyPlayers, 0, 20);
    }

    /**
     * Initializes the count manager
     */
    private void initialize() {
        this.maxCount = plugin.getConfig().getInt("max-count", 10);
        this.countWinMessage = getConfigMessage("COUNT_WIN", DEFAULT_COUNT_WIN_MESSAGE);
        this.countNotifyMessage = getConfigMessage("COUNT_NOTIFY", DEFAULT_COUNT_NOTIFY_MESSAGE);
    }

    /**
     * Retrieves a message from the configuration and colorizes it
     *
     * @param path         The path to the message
     * @param defaultValue The default message if the path is not found
     * @return The colorized message
     */
    private String getConfigMessage(String path, String defaultValue) {
        ConfigurationSection messages = plugin.getConfig().getConfigurationSection("messages");
        if (messages == null) {
            throw new IllegalArgumentException("messages section not found in config.yml");
        }
        return messages.getString(path, defaultValue);
    }

    /**
     * Subscribes to the count channel
     */
    private void subscribe() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> redisManager.subscribe((channel, value) -> {
            var data = value.split(":");
            var playerName = data[0];
            var count = Long.parseLong(data[1]);
            if (count >= maxCount) {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    spawnFirework(player.getLocation());
                    var parsed = Colorize.colorize(countWinMessage.replace("%player%", playerName));
                    player.sendMessage(parsed);
                });
            }
        }, COUNT_CHANNEL));
    }

    /**
     * Spawns a firework at a location
     *
     * @param location The location to spawn the firework at
     */
    private void spawnFirework(Location location) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(2);
            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.RED).withFade(Color.GREEN).build());
            firework.setFireworkMeta(meta);
        });
    }

    /**
     * Handles the counting of a player
     *
     * @param playerName The name of the player
     */
    public void handle(String playerName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            var count = playerCountDao.getPlayerCount(playerName).orElse(0L) + 1;
            playerCountDao.upsertPlayerCount(playerName, count);
            redisManager.publish(COUNT_CHANNEL, playerName + ":" + count);
            if (count >= maxCount) {
                playerCountDao.deletePlayerCount(playerName);
            }
        });
    }

    /**
     * Notifies all players of the current count
     */
    private void notifyPlayers() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            var count = playerCountDao.getPlayerCount(player.getName()).map(String::valueOf).orElse("0");
            var parsed = Colorize.colorize(countNotifyMessage.replace("%count%", count).replace("%maxcount%", String.valueOf(maxCount)));
            player.sendMessage(parsed);
        });
    }
}
