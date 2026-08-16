package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class CelestialTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        if (args.length == 1
                && sender.hasPermission("celestialdash.chronicle")
                && startsWith("chronicle", args[0])) {
            if (!sender.hasPermission("celestialdash.admin")) {
                return List.of("chronicle");
            }
        }
        if (!sender.hasPermission("celestialdash.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return complete(args[0], List.of("give", "chronicle", "reload", "pack"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return completeOnlinePlayerNames(args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chronicle")) {
            return complete(args[1], List.of("give"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("pack")) {
            return complete(args[1], List.of("send"));
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("pack") && args[1].equalsIgnoreCase("send")) {
            return completeOnlinePlayerNames(args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("chronicle") && args[1].equalsIgnoreCase("give")) {
            return completeOnlinePlayerNames(args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return complete(args[2], List.of("1", "16", "64"));
        }

        return List.of();
    }

    private List<String> completeOnlinePlayerNames(String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> startsWith(name, input))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> complete(String input, List<String> candidates) {
        return candidates.stream()
                .filter(candidate -> startsWith(candidate, input))
                .toList();
    }

    private boolean startsWith(String candidate, String input) {
        return candidate.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT));
    }
}
