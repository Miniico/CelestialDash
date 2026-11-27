package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class Messages {

    private final CelestialDash plugin;

    private String cooldownTemplate;
    private String noAdminPermissionMessage;
    private String noUsePermissionMessage;
    private String dashWorldDisabledMessage;
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
    private String chronicleGivenTemplate;
    private String chronicleOverflowTemplate;
    private String noChroniclePermissionMessage;
    private String chroniclePlayerOnlyMessage;
    private String chronicleReissueCooldownTemplate;
    private String resourcePackSentTemplate;
    private String resourcePackDisabledMessage;
    private String resourcePackInvalidConfigurationMessage;
    private String resourcePackSendFailedMessage;
    private String usageHeaderMessage;
    private String usageHelpTemplate;
    private String usageExampleTemplate;
    private Map<String, String> usageDescriptions;
    private String usageChronicleTemplate;
    private String usageReloadTemplate;
    private String usageGiveTemplate;
    private String usageChronicleGiveTemplate;
    private String usagePackSendTemplate;
    private String amuletDisabledMessage;
    private String amuletNoEffectsMessage;
    private String amuletCooldownTemplate;
    private String amuletUsedTemplate;
    private String amuletDepletedMessage;
    private String amuletUsedActionBarTemplate;
    private String amuletDepletedActionBarTemplate;

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

        dashWorldDisabledMessage = color(plugin.getConfig().getString(
                "messages.dash-world-disabled",
                "&cCelestial Dash is disabled in this world."
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
                "&cInvalid amount. Must be a number between 1 and %max%."
        ));

        giveSuccessTemplate = color(plugin.getConfig().getString(
                "messages.give-success",
                "&bGave %amount% Celestial Tears to %player%"
        ));

        giveOverflowTemplate = color(plugin.getConfig().getString(
                "messages.give-overflow",
                "&eThe inventory was full; the remaining Tears were dropped at %player%'s location."
        ));

        chronicleGivenTemplate = color(plugin.getConfig().getString(
                "messages.chronicle-given",
                "&bGave The Falling Sky to %player%."
        ));

        chronicleOverflowTemplate = color(plugin.getConfig().getString(
                "messages.chronicle-overflow",
                "&eThe inventory was full; the Chronicle was dropped at %player%'s location."
        ));

        noChroniclePermissionMessage = color(plugin.getConfig().getString(
                "messages.no-chronicle-permission",
                "&cYou don't have permission to recover The Falling Sky."
        ));

        chroniclePlayerOnlyMessage = color(plugin.getConfig().getString(
                "messages.chronicle-player-only",
                "&cOnly players can recover The Falling Sky."
        ));

        chronicleReissueCooldownTemplate = color(plugin.getConfig().getString(
                "messages.chronicle-reissue-cooldown",
                "&7You can recover another Chronicle in &b%seconds%s&7."
        ));

        resourcePackSentTemplate = color(plugin.getConfig().getString(
                "messages.resource-pack-sent",
                "&aThe resource-pack request was sent to %player%."
        ));

        resourcePackDisabledMessage = color(plugin.getConfig().getString(
                "messages.resource-pack-disabled",
                "&cResource-pack delivery is disabled in the configuration."
        ));

        resourcePackInvalidConfigurationMessage = color(plugin.getConfig().getString(
                "messages.resource-pack-invalid-configuration",
                "&cThe resource-pack configuration is invalid. Check the server log."
        ));

        resourcePackSendFailedMessage = color(plugin.getConfig().getString(
                "messages.resource-pack-send-failed",
                "&cThe resource-pack request could not be sent. Check the server log."
        ));

        usageHeaderMessage = color(plugin.getConfig().getString(
                "messages.usage-header",
                "&eUsage:"
        ));

        // Runtime defaults keep help available on installations with older config files.
        usageHelpTemplate = color(plugin.getConfig().getString(
                "messages.usage-help",
                "&7 /%label% help &8- &fShow commands you can use"
        ));

        usageExampleTemplate = color(plugin.getConfig().getString(
                "messages.usage-example",
                "&8   Example: &f/%label% %command%"
        ));

        usageDescriptions = Map.of(
                "chronicle", usageDescription("chronicle", "Recover your Chronicle"),
                "reload", usageDescription("reload", "Reload the plugin configuration"),
                "give", usageDescription("give", "Give Celestial Tears to a player"),
                "chronicle-give", usageDescription("chronicle-give", "Give a Chronicle copy to a player"),
                "pack-send", usageDescription("pack-send", "Send the configured resource pack to a player")
        );

        usageChronicleTemplate = color(plugin.getConfig().getString(
                "messages.usage-chronicle",
                "&7 /%label% chronicle"
        ));

        usageReloadTemplate = color(plugin.getConfig().getString(
                "messages.usage-reload",
                "&7 /%label% reload"
        ));

        usageGiveTemplate = color(plugin.getConfig().getString(
                "messages.usage-give",
                "&7 /%label% give <player> <amount>"
        ));

        usageChronicleGiveTemplate = color(plugin.getConfig().getString(
                "messages.usage-chronicle-give",
                "&7 /%label% chronicle give <player>"
        ));

        usagePackSendTemplate = color(plugin.getConfig().getString(
                "messages.usage-pack-send",
                "&7 /%label% pack send <player>"
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
                "&bCelestial energy purifies you."
        ));

        amuletDepletedMessage = color(plugin.getConfig().getString(
                "messages.amulet-depleted",
                "&7The Celestial Amulet has lost its light."
        ));

        // Runtime fallbacks also give existing server configurations charge feedback.
        amuletUsedActionBarTemplate = color(plugin.getConfig().getString(
                "messages.amulet-used-action-bar",
                "&bPurified! &7Celestial Amulet: &f%uses%/%max-uses% &7charges"
        ));
        amuletDepletedActionBarTemplate = color(plugin.getConfig().getString(
                "messages.amulet-depleted-action-bar",
                "&cCelestial Amulet depleted! &f%uses%/%max-uses% &7charges"
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

    public String getDashWorldDisabledMessage() {
        return dashWorldDisabledMessage;
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

    public String formatInvalidAmount(int maxAmount) {
        return invalidAmountMessage.replace("%max%", String.valueOf(maxAmount));
    }

    public String formatGiveSuccess(String player, int amount) {
        return giveSuccessTemplate
                .replace("%player%", player)
                .replace("%amount%", String.valueOf(amount));
    }

    public String formatGiveOverflow(String player) {
        return giveOverflowTemplate.replace("%player%", player);
    }

    public String formatChronicleGiven(String player) {
        return chronicleGivenTemplate.replace("%player%", player);
    }

    public String formatChronicleOverflow(String player) {
        return chronicleOverflowTemplate.replace("%player%", player);
    }

    public String getNoChroniclePermissionMessage() {
        return noChroniclePermissionMessage;
    }

    public String getChroniclePlayerOnlyMessage() {
        return chroniclePlayerOnlyMessage;
    }

    public String formatChronicleReissueCooldown(long seconds) {
        return chronicleReissueCooldownTemplate.replace("%seconds%", String.valueOf(seconds));
    }

    public String formatResourcePackSent(String player) {
        return resourcePackSentTemplate.replace("%player%", player);
    }

    public String getResourcePackDisabledMessage() {
        return resourcePackDisabledMessage;
    }

    public String getResourcePackInvalidConfigurationMessage() {
        return resourcePackInvalidConfigurationMessage;
    }

    public String getResourcePackSendFailedMessage() {
        return resourcePackSendFailedMessage;
    }

    public String getUsageHeaderMessage() {
        return usageHeaderMessage;
    }

    public String formatUsageHelp(String label) {
        return usageHelpTemplate.replace("%label%", label);
    }

    public String formatUsageExample(String label, String command) {
        return usageExampleTemplate.replace("%label%", label).replace("%command%", command);
    }

    public String describeUsage(String command, String usage) {
        return usage + usageDescriptions.getOrDefault(command, "");
    }

    private String usageDescription(String command, String fallback) {
        return color(plugin.getConfig().getString("messages.usage-" + command + "-description",
                " &8- &f" + fallback));
    }

    public String formatUsageChronicle(String label) {
        return usageChronicleTemplate.replace("%label%", label);
    }

    public String formatUsageReload(String label) {
        return usageReloadTemplate.replace("%label%", label);
    }

    public String formatUsageGive(String label) {
        return usageGiveTemplate.replace("%label%", label);
    }

    public String formatUsageChronicleGive(String label) {
        return usageChronicleGiveTemplate.replace("%label%", label);
    }

    public String formatUsagePackSend(String label) {
        return usagePackSendTemplate.replace("%label%", label);
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

    public String formatAmuletUsed(int uses, int maxUses) {
        return formatAmuletUsed(uses).replace("%max-uses%", String.valueOf(maxUses));
    }

    public String formatAmuletUsedActionBar(int uses, int maxUses) {
        return formatAmuletCharges(amuletUsedActionBarTemplate, uses, maxUses);
    }

    public String formatAmuletDepletedActionBar(int maxUses) {
        return formatAmuletCharges(amuletDepletedActionBarTemplate, 0, maxUses);
    }

    private String formatAmuletCharges(String template, int uses, int maxUses) {
        return template.replace("%uses%", String.valueOf(uses))
                .replace("%max-uses%", String.valueOf(maxUses));
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

    @SuppressWarnings("deprecation") // Retains legacy color-code support across the target server range.
    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
