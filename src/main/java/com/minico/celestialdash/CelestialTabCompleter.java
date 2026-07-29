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
        if (!sender.hasPermission("celestialdash.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return complete(args[0], List.of("give", "reload"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> startsWith(name, args[1]))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return complete(args[2], List.of("1", "16", "64"));
        }

        return List.of();
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
