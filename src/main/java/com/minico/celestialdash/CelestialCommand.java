package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
            sender.sendMessage(messages.getNoAdminPermissionMessage());
            return true;
        }

        // /celestialdash reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadSettings();
            TearUtils.initialize(plugin, plugin.getTearCustomModelData());
            plugin.refreshAmuletRecipe();
            messages.reload();
            sender.sendMessage(messages.getConfigReloadedMessage());
            return true;
        }

        // /celestialdash give <player> <amount>
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(messages.formatPlayerNotFound(args[1]));
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(messages.getInvalidAmountMessage());
                return true;
            }

            Map<Integer, ItemStack> leftovers = target.getInventory().addItem(TearUtils.createCelestialTear(amount));
            for (ItemStack leftover : leftovers.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            }

            sender.sendMessage(messages.formatGiveSuccess(target.getName(), amount));
            if (!leftovers.isEmpty()) {
                sender.sendMessage(messages.formatGiveOverflow(target.getName()));
            }
            return true;
        }

        sender.sendMessage(messages.getUsageHeaderMessage());
        sender.sendMessage(messages.formatUsageReload(label));
        sender.sendMessage(messages.formatUsageGive(label));
        return true;
    }
}
