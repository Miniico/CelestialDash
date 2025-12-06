package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Main command executor for /celestialdash
 * Subcommands:
 *   /celestialdash give <player> <amount>
 *   /celestialdash reload
 */
public class CelestialCommand implements CommandExecutor {

    private final CelestialDash plugin;
    private final Messages messages;

    public CelestialCommand(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission("celestialdash.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // /celestialdash reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadSettings();
            messages.reload();
            sender.sendMessage(ChatColor.GREEN + "CelestialDash configuration reloaded.");
            return true;
        }

        // /celestialdash give <player> <amount>
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount. Must be a number > 0.");
                return true;
            }

            target.getInventory().addItem(TearUtils.createCelestialTear(amount));
            sender.sendMessage(ChatColor.AQUA + "Gave " + amount + " Celestial Tears to " + target.getName());
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage:");
        sender.sendMessage(ChatColor.GRAY + " /" + label + " reload");
        sender.sendMessage(ChatColor.GRAY + " /" + label + " give <player> <amount>");
        return true;
    }
}
