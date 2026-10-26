
package de.shyyyuki.infiniteVillagerTrading.commands;

import de.shyyyuki.infiniteVillagerTrading.InfiniteVillagerTrading;
import de.shyyyuki.infiniteVillagerTrading.utils.MessagesUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the main plugin command and tab completion.
 *
 * @author Shyyyuki
 * @since 1.0
 */
public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final InfiniteVillagerTrading plugin;

    /**
     * Creates a new command handler.
     *
     * @param plugin the main plugin instance
     */
    public ReloadCommand(InfiniteVillagerTrading plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the main plugin command.
     *
     * @param sender the command sender
     * @param command the executed command
     * @param label the command label
     * @param args the command arguments
     * @return true if the command was handled
     */
    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {
        if (!command.getName().equalsIgnoreCase(
                "infinitevillagertrading")) {
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ivt.reload")) {
                sender.sendMessage(MessagesUtil.get("no-permission"));
                return true;
            }

            plugin.getConfigManager().reload();
            MessagesUtil.reload();

            sender.sendMessage(MessagesUtil.get("reload-success"));
        } else {
            sender.sendMessage(
                    MessagesUtil.get("reload-usage", "label", label)
            );
        }

        return true;
    }

    /**
     * Provides tab completions for the main plugin command.
     *
     * @param sender the command sender
     * @param command the executed command
     * @param label the command label
     * @param args the command arguments
     * @return matching command completions
     */
    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase(
                "infinitevillagertrading")
                && sender.hasPermission("ivt.reload")
                && args.length == 1) {

            StringUtil.copyPartialMatches(
                    args[0],
                    Collections.singletonList("reload"),
                    completions
            );
        }

        Collections.sort(completions);
        return completions;
    }
}