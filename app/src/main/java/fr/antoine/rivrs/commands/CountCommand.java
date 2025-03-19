package fr.antoine.rivrs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.antoine.rivrs.managers.CountManager;
import org.jetbrains.annotations.NotNull;

/**
 * Command to count the number of times a player has been counted
 */
public class CountCommand implements CommandExecutor {

    private final CountManager countManager;

    /**
     * Constructor for the CountCommand
     *
     * @param countManager The count manager
     */
    public CountCommand(CountManager countManager) {
        this.countManager = countManager;
    }

    /**
     * Execute the command
     *
     * @param sender  The command sender
     * @param command The command
     * @param label   The label
     * @param args    The arguments
     * @return True if the command was successful, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Check if there's exactly one argument (player name)
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /count <playerName>");
            return true;
        }
        String playerName = args[0];
        countManager.handle(playerName);
        return true;
    }
} 