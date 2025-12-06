package com.minico.celestialdash;

import org.bukkit.ChatColor;

public class Messages {

    private final CelestialDash plugin;

    private String cooldownTemplate;
    private String noTearsMessage;
    private String dashUsedMessage;
    private String secondDashMessage;
    private String tearDropMessage;

    public Messages(CelestialDash plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        cooldownTemplate = color(plugin.getConfig().getString(
                "messages.cooldown",
                "&7Celestial Dash ready in &b%seconds%s&7."
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
    }

    public String formatCooldown(long seconds) {
        return cooldownTemplate.replace("%seconds%", String.valueOf(seconds));
    }

    public String getNoTearsMessage() {
        return noTearsMessage;
    }

    public String getDashUsedMessage() {
        return dashUsedMessage;
    }

    public String getSecondDashMessage() {
        return secondDashMessage;
    }

    public String getTearDropMessage() {
        return tearDropMessage;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
