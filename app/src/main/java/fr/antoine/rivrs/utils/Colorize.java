package fr.antoine.rivrs.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

/**
 * Utility class for colorizing text with HTML color codes
 */
public class Colorize {

    /**
     * MiniMessage serializer for colorizing text
     */
    private static final MiniMessage serializer = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .build())
            .build();

    /**
     * Convert HTML color codes to Minecraft color codes
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static Component colorize(String text) {
        if (text == null) return null;
        return serializer.deserialize(text);
    }
}
