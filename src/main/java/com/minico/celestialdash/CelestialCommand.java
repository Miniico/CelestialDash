package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CelestialCommand implements CommandExecutor {

    private final CelestialDash plugin;
    private final Messages messages;

    public CelestialCommand(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("celestialdash.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "CelestialDash Commands:");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " give <player> <amount>");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadSettings();

            // IMPORTANT: update TearUtils with current CustomModelData value
            TearUtils.setCustomModelData(plugin.getConfig().getInt("tear-custom-model-data", 0));

            messages.reload();
            sender.sendMessage(ChatColor.GREEN + "CelestialDash configuration reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " give <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return true;
            }

            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
                return true;
            }

            for (int i = 0; i < amount; i++) {
                target.getInventory().addItem(TearUtils.createCelestialTear());
            }

            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " Celestial Tears to " + target.getName() + ".");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /" + label + " for help.");
        return true;
    }
}
