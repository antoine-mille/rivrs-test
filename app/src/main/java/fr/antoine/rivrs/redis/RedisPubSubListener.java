package fr.antoine.rivrs.redis;

import redis.clients.jedis.JedisPubSub;
import fr.antoine.rivrs.Main;

/**
 * Redis PubSub listener for handling channel subscriptions and messages
 */
public class RedisPubSubListener extends JedisPubSub {
    
    private final Main plugin;
    private final MessageHandler messageHandler;
    
    /**
     * Creates a new Redis PubSub listener
     * 
     * @param plugin The main plugin instance
     * @param messageHandler The handler for received messages
     */
    public RedisPubSubListener(Main plugin, MessageHandler messageHandler) {
        this.plugin = plugin;
        this.messageHandler = messageHandler;
    }
    
    /**
     * Called when a message is received from Redis
     * 
     * @param channel The channel the message was received from
     * @param message The message received
     */
    @Override
    public void onMessage(String channel, String message) {
        try {
            messageHandler.handle(channel, message);
        } catch (Exception exception) {
            plugin.getLogger().severe("Error handling Redis message: " + exception.getMessage());
        }
    }
    
} 