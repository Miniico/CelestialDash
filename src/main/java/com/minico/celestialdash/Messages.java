package com.minico.celestialdash;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

    private final CelestialDash plugin;

    // Pattern for hex colors (&#RRGGBB)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Message fields
    private String prefix;
    private String cooldownMessage;
    private String noTearsMessage;
    private String dashUsedMessage;
    private String heightLimitMessage;
    private String noPermissionMessage;
    private String playerNotFoundMessage;
    private String tearReceivedMessage;
    private String configReloadedMessage;
    private String invalidAmountMessage;
    private String inventoryFullMessage;
    private String tearGivenMessage;
    private String tearGivenTargetMessage;

    public Messages(CelestialDash plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads all messages from the configuration file
     */
    public void reload() {
        FileConfiguration cfg = plugin.getConfig();

        prefix = color(cfg.getString(
                "messages.prefix",
                "&8[&bCelestialDash&8]&r "
        ));

        cooldownMessage = color(cfg.getString(
                "messages.cooldown",
                "&7Celestial Dash ready in &b%seconds%&7."
        ));

        noTearsMessage = color(cfg.getString(
                "messages.no-tears",
                "&cYou need at least one &bCelestial Tear &cto use Celestial Dash."
        ));

        dashUsedMessage = color(cfg.getString(
                "messages.dash-used",
                "&bThe celestial wind pushes you forward!"
        ));

        heightLimitMessage = color(cfg.getString(
                "messages.height-limit",
                "&cYou cannot dash at this height!"
        ));

        noPermissionMessage = color(cfg.getString(
                "messages.no-permission",
                "&cYou don't have permission to do that."
        ));

        playerNotFoundMessage = color(cfg.getString(
                "messages.player-not-found",
                "&cPlayer '&e%player%&c' not found or is offline."
        ));

        tearReceivedMessage = color(cfg.getString(
                "messages.tear-received",
                "&b✦ &fA Celestial Tear falls from the stormy sky nearby..."
        ));

        configReloadedMessage = color(cfg.getString(
                "messages.config-reloaded",
                "&aConfiguration reloaded successfully."
        ));

        invalidAmountMessage = color(cfg.getString(
                "messages.invalid-amount",
                "&cAmount must be a valid number greater than 0."
        ));

        inventoryFullMessage = color(cfg.getString(
                "messages.inventory-full",
                "&e⚠ &6%player%'s inventory was full. %amount% item(s) dropped on the ground."
        ));

        tearGivenMessage = color(cfg.getString(
                "messages.tear-given",
                "&a✓ Gave &b%amount% Celestial Tear(s) &ato &b%player%&a."
        ));

        tearGivenTargetMessage = color(cfg.getString(
                "messages.tear-given-target",
                "&a✓ You received &b%amount% Celestial Tear(s)&a."
        ));
    }

    /**
     * Translates color codes including hex colors (1.16+)
     * Supports both & codes and &#RRGGBB hex format
     */
    private String color(String input) {
        if (input == null) {
            return "";
        }

        // Translate hex colors (&#RRGGBB -> §x§R§R§G§G§B§B)
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder magic = new StringBuilder("§x");

            for (char c : hexCode.toCharArray()) {
                magic.append('§').append(c);
            }

            matcher.appendReplacement(buffer, magic.toString());
        }
        matcher.appendTail(buffer);

        // Translate standard & codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Formats the cooldown message with proper singular/plural
     */
    public String formatCooldown(long seconds) {
        String timeText = seconds == 1 ? "1 second" : seconds + " seconds";
        return cooldownMessage.replace("%seconds%", timeText);
    }

    /**
     * Formats a message with the plugin prefix
     */
    public String withPrefix(String message) {
        return prefix + message;
    }

    // === Getters ===

    public String getPrefix() {
        return prefix;
    }

    public String getNoTearsMessage() {
        return noTearsMessage;
    }

    public String getDashUsedMessage() {
        return dashUsedMessage;
    }

    public String getHeightLimitMessage() {
        return heightLimitMessage;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getPlayerNotFoundMessage(String playerName) {
        return playerNotFoundMessage.replace("%player%", playerName);
    }

    public String getTearReceivedMessage() {
        return tearReceivedMessage;
    }

    public String getConfigReloadedMessage() {
        return configReloadedMessage;
    }

    public String getInvalidAmountMessage() {
        return invalidAmountMessage;
    }

    public String getInventoryFullMessage(String playerName, int amount) {
        return inventoryFullMessage
                .replace("%player%", playerName)
                .replace("%amount%", String.valueOf(amount));
    }

    public String getTearGivenMessage(int amount, String playerName) {
        return tearGivenMessage
                .replace("%amount%", String.valueOf(amount))
                .replace("%player%", playerName);
    }

    public String getTearGivenTargetMessage(int amount) {
        return tearGivenTargetMessage
                .replace("%amount%", String.valueOf(amount));
    }
}