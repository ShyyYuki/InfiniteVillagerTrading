package de.shyyyuki.infiniteVillagerTrading.commands;

import de.shyyyuki.infiniteVillagerTrading.InfiniteVillagerTrading;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final InfiniteVillagerTrading plugin;

    public ReloadCommand(InfiniteVillagerTrading plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, String @NonNull [] args) {
        if (command.getName().equalsIgnoreCase("infinitevillagertrading")) {

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

                if (!sender.hasPermission("ivt.reload")) {
                    sender.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                plugin.reloadConfig();
                sender.sendMessage("§aThe InfiniteVillagerTrading configuration has been successfully reloaded!");
            } else {
                sender.sendMessage("§cUsage: /" + label + " reload");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("infinitevillagertrading") && sender.hasPermission("ivt.reload")) {

            if (args.length == 1) {
                List<String> options = new ArrayList<>();
                options.add("reload");

                StringUtil.copyPartialMatches(args[0], options, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}