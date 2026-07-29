package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messages {

    private final CelestialDash plugin;

    private String cooldownTemplate;
    private String noAdminPermissionMessage;
    private String noUsePermissionMessage;
    private String noAmuletPermissionMessage;
    private String noTearsMessage;
    private String dashUsedMessage;
    private String secondDashMessage;
    private String tearDropMessage;
    private String configReloadedMessage;
    private String playerNotFoundTemplate;
    private String invalidAmountMessage;
    private String giveSuccessTemplate;
    private String giveOverflowTemplate;
    private String usageHeaderMessage;
    private String usageReloadTemplate;
    private String usageGiveTemplate;
    private String amuletDisabledMessage;
    private String amuletNoEffectsMessage;
    private String amuletCooldownTemplate;
    private String amuletUsedTemplate;
    private String amuletDepletedMessage;

    public Messages(CelestialDash plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        cooldownTemplate = color(plugin.getConfig().getString(
                "messages.cooldown",
                "&7Celestial Dash ready in &b%seconds%s&7."
        ));

        noAdminPermissionMessage = color(plugin.getConfig().getString(
                "messages.no-admin-permission",
                "&cYou don't have permission to use this command."
        ));

        noUsePermissionMessage = color(plugin.getConfig().getString(
                "messages.no-use-permission",
                "&cYou don't have permission to use Celestial Dash."
        ));

        noAmuletPermissionMessage = color(plugin.getConfig().getString(
                "messages.no-amulet-permission",
                "&cYou don't have permission to use the Celestial Amulet."
        ));

        noTearsMessage = color(plugin.getConfig().getString(
                "messages.no-tears",
                "&cYou need at least one &bCelestial Tear &cto use Celestial Dash."
        ));

        dashUsedMessage = color(plugin.getConfig().getString(
                "messages.dash-used",
                "&bThe celestial wind pushes you forward!"
        ));

        secondDashMessage = color(plugin.getConfig().getString(
                "messages.second-dash",
                "&bYou unleash a second celestial dash!"
        ));

        tearDropMessage = color(plugin.getConfig().getString(
                "messages.tear-drop",
                "&bA celestial tear materializes from the storm..."
        ));

        configReloadedMessage = color(plugin.getConfig().getString(
                "messages.config-reloaded",
                "&aCelestialDash configuration reloaded."
        ));

        playerNotFoundTemplate = color(plugin.getConfig().getString(
                "messages.player-not-found",
                "&cPlayer not found: %player%"
        ));

        invalidAmountMessage = color(plugin.getConfig().getString(
                "messages.invalid-amount",
                "&cInvalid amount. Must be a number > 0."
        ));

        giveSuccessTemplate = color(plugin.getConfig().getString(
                "messages.give-success",
                "&bGave %amount% Celestial Tears to %player%"
        ));

        giveOverflowTemplate = color(plugin.getConfig().getString(
                "messages.give-overflow",
                "&eThe inventory was full; the remaining Tears were dropped at %player%'s location."
        ));

        usageHeaderMessage = color(plugin.getConfig().getString(
                "messages.usage-header",
                "&eUsage:"
        ));

        usageReloadTemplate = color(plugin.getConfig().getString(
                "messages.usage-reload",
                "&7 /%label% reload"
        ));

        usageGiveTemplate = color(plugin.getConfig().getString(
                "messages.usage-give",
                "&7 /%label% give <player> <amount>"
        ));

        amuletDisabledMessage = color(plugin.getConfig().getString(
                "messages.amulet-disabled",
                "&cThe Celestial Amulet is disabled."
        ));

        amuletNoEffectsMessage = color(plugin.getConfig().getString(
                "messages.amulet-no-effects",
                "&7The amulet finds nothing to purify."
        ));

        amuletCooldownTemplate = color(plugin.getConfig().getString(
                "messages.amulet-cooldown",
                "&7Celestial Amulet ready in &b%seconds%s&7."
        ));

        amuletUsedTemplate = color(plugin.getConfig().getString(
                "messages.amulet-used",
                "&bCelestial energy purifies you. &7Uses left: &f%uses%"
        ));

        amuletDepletedMessage = color(plugin.getConfig().getString(
                "messages.amulet-depleted",
                "&7The Celestial Amulet has lost its light."
        ));
    }

    public String formatCooldown(long seconds) {
        return cooldownTemplate.replace("%seconds%", String.valueOf(seconds));
    }

    public String getNoTearsMessage() {
        return noTearsMessage;
    }

    public String getNoAdminPermissionMessage() {
        return noAdminPermissionMessage;
    }

    public String getNoUsePermissionMessage() {
        return noUsePermissionMessage;
    }

    public String getNoAmuletPermissionMessage() {
        return noAmuletPermissionMessage;
    }

    public String getDashUsedMessage() {
        return dashUsedMessage;
    }

    public String getSecondDashMessage() {
        return secondDashMessage;
    }

    public String getConfigReloadedMessage() {
        return configReloadedMessage;
    }

    public String formatPlayerNotFound(String player) {
        return playerNotFoundTemplate.replace("%player%", player);
    }

    public String getInvalidAmountMessage() {
        return invalidAmountMessage;
    }

    public String formatGiveSuccess(String player, int amount) {
        return giveSuccessTemplate
                .replace("%player%", player)
                .replace("%amount%", String.valueOf(amount));
    }

    public String formatGiveOverflow(String player) {
        return giveOverflowTemplate.replace("%player%", player);
    }

    public String getUsageHeaderMessage() {
        return usageHeaderMessage;
    }

    public String formatUsageReload(String label) {
        return usageReloadTemplate.replace("%label%", label);
    }

    public String formatUsageGive(String label) {
        return usageGiveTemplate.replace("%label%", label);
    }

    public String getAmuletDisabledMessage() {
        return amuletDisabledMessage;
    }

    public String getAmuletNoEffectsMessage() {
        return amuletNoEffectsMessage;
    }

    public String formatAmuletCooldown(long seconds) {
        return amuletCooldownTemplate.replace("%seconds%", String.valueOf(seconds));
    }

    public String formatAmuletUsed(int uses) {
        return amuletUsedTemplate.replace("%uses%", String.valueOf(uses));
    }

    public String getAmuletDepletedMessage() {
        return amuletDepletedMessage;
    }

    public void sendTearDropMessage(Player player) {
        if (tearDropMessage == null || tearDropMessage.isEmpty()) {
            return;
        }
        player.sendMessage(tearDropMessage);
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
