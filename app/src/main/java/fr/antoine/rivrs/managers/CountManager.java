package fr.antoine.rivrs.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import fr.antoine.rivrs.Main;
import fr.antoine.rivrs.redis.RedisManager;
import fr.antoine.rivrs.utils.Colorize;

/**
 * Manager for counting player interactions
 */
public class CountManager {

    private static final String COUNT_CHANNEL = "count";
    private static final String DEFAULT_COUNT_WIN_MESSAGE = "<red>Player %player% just finished!</red>";
    private static final String DEFAULT_COUNT_NOTIFY_MESSAGE = "<red>Progression: %count%/%maxcount%</red>";

    private final Main plugin;
    private final RedisManager redisManager;
    private final int maxCount;
    private final String countWinMessage;
    private final String countNotifyMessage;

    /**
     * Constructor for the CountManager class
     *
     * @param plugin       The main plugin instance
     * @param redisManager The RedisManager instance
     */
    public CountManager(Main plugin, RedisManager redisManager) {
        this.plugin = plugin;
        this.redisManager = redisManager;
        this.maxCount = getConfigInt("max-count", 10);
        this.countWinMessage = getConfigMessage("COUNT_WIN", DEFAULT_COUNT_WIN_MESSAGE);
        this.countNotifyMessage = getConfigMessage("COUNT_NOTIFY", DEFAULT_COUNT_NOTIFY_MESSAGE);
        subscribe();
    }

    /**
     * Retrieves an integer from the configuration
     *
     * @param path         The path to the configuration value
     * @param defaultValue The default value if the path is not found
     * @return The integer value from the configuration
     */
    private int getConfigInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
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
            String[] data = value.split(":");
            String playerName = data[0];
            long count = Long.parseLong(data[1]);
            if (count >= maxCount) {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    spawnFirework(player.getLocation());
                    Component parsed = Colorize.colorize(countWinMessage.replace("%player%", playerName));
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
            meta.addEffect(
                    FireworkEffect
                            .builder()
                            .with(FireworkEffect.Type.STAR)
                            .withColor(Color.RED)
                            .withFade(Color.GREEN)
                            .build()
            );
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
            String key = getKey(playerName);
            long count = redisManager.incrementAndGet(key);
            redisManager.publish(COUNT_CHANNEL, playerName + ":" + count);
            if (count >= maxCount) {
                redisManager.deleteKey(key);
            }
        });
    }

    /**
     * Notifies all players of the current count
     */
    public void notifyPlayers() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            String count = redisManager.getValue(getKey(player.getName()));
            String notifyMessage = countNotifyMessage.replace("%count%", count == null ? "0" : count).replace("%maxcount%", String.valueOf(maxCount));
            Component parsed = Colorize.colorize(notifyMessage);
            player.sendMessage(parsed);
        });
    }

    /**
     * Get the key for the count of a player
     *
     * @param playerName The name of the player
     * @return The key for the count of the player
     */
    private String getKey(String playerName) {
        return COUNT_CHANNEL + ":" + playerName;
    }
}
