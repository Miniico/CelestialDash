package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CelestialTabCompleter implements TabCompleter {

    private final CelestialDash plugin;

    public CelestialTabCompleter() {
        this(null);
    }

    public CelestialTabCompleter(CelestialDash plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        boolean admin = sender.hasPermission("celestialdash.admin");
        if (args.length == 1) {
            List<String> candidates = new ArrayList<>();
            candidates.add("help");
            if (admin || (sender instanceof Player && sender.hasPermission("celestialdash.chronicle"))) {
                candidates.add("chronicle");
            }
            if (admin) {
                candidates.addAll(List.of("give", "reload", "pack"));
            }
            return complete(args[0], candidates);
        }
        if (!admin) {
            return List.of();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return completeOnlinePlayerNames(sender, args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chronicle")) {
            return complete(args[1], List.of("give"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("pack")) {
            return complete(args[1], List.of("send"));
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("pack") && args[1].equalsIgnoreCase("send")) {
            return completeOnlinePlayerNames(sender, args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("chronicle") && args[1].equalsIgnoreCase("give")) {
            return completeOnlinePlayerNames(sender, args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            int maxAmount = plugin == null ? PluginSettings.defaults().giveMaxAmount()
                    : plugin.getSettings().giveMaxAmount();
            return complete(args[2], List.of(1, 16, 64).stream()
                    .filter(amount -> amount <= maxAmount)
                    .map(String::valueOf)
                    .toList());
        }

        return List.of();
    }

    private List<String> completeOnlinePlayerNames(CommandSender sender, String input) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !(sender instanceof Player viewer) || viewer.canSee(player))
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
