package com.minico.celestialdash;

import org.bukkit.ChatColor;

public class Messages {

    private final CelestialDash plugin;

    private String cooldownTemplate;
    private String noTearsMessage;
    private String dashUsedMessage;

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

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
