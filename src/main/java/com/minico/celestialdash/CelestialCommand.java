package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CelestialCommand implements CommandExecutor {

    private final CelestialDash plugin;
    private final Messages messages;

    // Message constants
    private static final String USAGE = ChatColor.RED + "Usage: /celestialdash <give|reload> ...";
    private static final String NO_PERMISSION = ChatColor.RED + "You don't have permission to use this command.";
    private static final String PREFIX = ChatColor.GREEN + "[CelestialDash] ";

    public CelestialCommand(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("celestialdash")) return false;

        if (args.length < 1) {
            sender.sendMessage(USAGE);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "give" -> handleGive(sender, args);
            default -> sender.sendMessage(USAGE);
        }

        return true;
    }

    /**
     * Handles the configuration reload command
     */
    private void handleReload(CommandSender sender) {
        if (!hasPermission(sender, "celestialdash.admin")) return;

        plugin.reloadConfig();
        plugin.loadSettings();
        if (plugin.getMessages() != null) {
            plugin.getMessages().reload();
        }

        plugin.getLogger().info(sender.getName() + " reloaded CelestialDash configuration.");
        sender.sendMessage(PREFIX + "Configuration reloaded.");

        if (sender instanceof Player p) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.6f);
        }
    }

    /**
     * Handles the give items command
     */
    private void handleGive(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "celestialdash.admin")) return;

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /celestialdash give <player> <amount>");
            return;
        }

        // Find player (more flexible than getPlayerExact)
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found or is offline.");
            return;
        }

        // Validate amount
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a valid number.");
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
            return;
        }

        if (amount > 2304) { // 64 * 36 (full inventory)
            sender.sendMessage(ChatColor.YELLOW + "Warning: Amount is very large (" + amount + ").");
        }

        // Create and give the item
        ItemStack tear = TearUtils.createCelestialTear();
        tear.setAmount(Math.min(amount, 64)); // First stack

        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(tear);

        // If there are more items to give
        int remaining = amount - 64;
        while (remaining > 0) {
            ItemStack additionalTear = TearUtils.createCelestialTear();
            additionalTear.setAmount(Math.min(remaining, 64));
            leftover.putAll(target.getInventory().addItem(additionalTear));
            remaining -= 64;
        }

        // Check if items were left without adding
        if (!leftover.isEmpty()) {
            int droppedAmount = leftover.values().stream()
                    .mapToInt(ItemStack::getAmount)
                    .sum();
            sender.sendMessage(ChatColor.YELLOW + "⚠ " + target.getName() + "'s inventory was full. " +
                    droppedAmount + " item(s) dropped on the ground.");
        }

        // Confirmation messages
        sender.sendMessage(ChatColor.GREEN + "✓ Gave " + ChatColor.AQUA + amount +
                ChatColor.GREEN + " Celestial Tear" + (amount > 1 ? "s" : "") +
                " to " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + ".");

        target.sendMessage(ChatColor.GREEN + "✓ You received " + ChatColor.AQUA + amount +
                ChatColor.GREEN + " Celestial Tear" + (amount > 1 ? "s" : "") + ".");

        // Sound for the receiver
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
    }

    /**
     * Checks if the sender has the required permission
     */
    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender.isOp() || sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(NO_PERMISSION);
        return false;
    }
}