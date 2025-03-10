package fr.antoine.rivrs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import fr.antoine.rivrs.commands.CountCommand;
import fr.antoine.rivrs.managers.CountManager;
import fr.antoine.rivrs.redis.RedisManager;

/**
 * Main class for the Rivrs plugin
 * @author Antoine
 */
public class Main extends JavaPlugin {
    
    private RedisManager redisManager;

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        var countManager = new CountManager(this, redisManager = new RedisManager(this));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> countManager.notifyPlayers(), 0, 20);
        getCommand("count").setExecutor(new CountCommand(countManager));
        getLogger().info("Rivrs has been enabled!");
    }

    /**
     * Called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        redisManager.closePool();
        getLogger().info("Rivrs has been disabled!");
    }

}
