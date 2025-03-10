package fr.antoine.rivrs.utils;

/**
 * Utility class for colorizing text with HTML color codes
 */
public class Colorize {
    
    /**
     * Convert HTML color codes to Minecraft color codes
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String colorize(String text) {
        if (text == null) return null;
        
        // Convert HTML color tags to Minecraft color codes
        text = text.replaceAll("<black>(.*?)</black>", "§0$1§r");
        text = text.replaceAll("<dark_blue>(.*?)</dark_blue>", "§1$1§r");
        text = text.replaceAll("<dark_green>(.*?)</dark_green>", "§2$1§r");
        text = text.replaceAll("<dark_aqua>(.*?)</dark_aqua>", "§3$1§r");
        text = text.replaceAll("<dark_red>(.*?)</dark_red>", "§4$1§r");
        text = text.replaceAll("<dark_purple>(.*?)</dark_purple>", "§5$1§r");
        text = text.replaceAll("<gold>(.*?)</gold>", "§6$1§r");
        text = text.replaceAll("<gray>(.*?)</gray>", "§7$1§r");
        text = text.replaceAll("<dark_gray>(.*?)</dark_gray>", "§8$1§r");
        text = text.replaceAll("<blue>(.*?)</blue>", "§9$1§r");
        text = text.replaceAll("<green>(.*?)</green>", "§a$1§r");
        text = text.replaceAll("<aqua>(.*?)</aqua>", "§b$1§r");
        text = text.replaceAll("<red>(.*?)</red>", "§c$1§r");
        text = text.replaceAll("<light_purple>(.*?)</light_purple>", "§d$1§r");
        text = text.replaceAll("<yellow>(.*?)</yellow>", "§e$1§r");
        text = text.replaceAll("<white>(.*?)</white>", "§f$1§r");
        
        // Formats
        text = text.replaceAll("<bold>(.*?)</bold>", "§l$1§r");
        text = text.replaceAll("<italic>(.*?)</italic>", "§o$1§r");
        text = text.replaceAll("<underline>(.*?)</underline>", "§n$1§r");
        text = text.replaceAll("<strikethrough>(.*?)</strikethrough>", "§m$1§r");
        text = text.replaceAll("<reset>(.*?)</reset>", "§r$1");
        
        // Simplified versions for common colors
        text = text.replaceAll("<b>(.*?)</b>", "§l$1§r");
        text = text.replaceAll("<i>(.*?)</i>", "§o$1§r");
        text = text.replaceAll("<u>(.*?)</u>", "§n$1§r");
        text = text.replaceAll("<s>(.*?)</s>", "§m$1§r");
        
        return text;
    }
}
