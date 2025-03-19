package fr.antoine.rivrs.redis;

/**
 * Interface for handling messages received from Redis
 */
@FunctionalInterface
public interface MessageHandler {
    /**
     * Handles a message received from Redis
     *
     * @param channel The channel the message was received from
     * @param message The message received
     */
    void handle(String channel, String message);
}
