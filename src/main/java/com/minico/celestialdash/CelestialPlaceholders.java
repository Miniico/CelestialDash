package com.minico.celestialdash;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class CelestialPlaceholders extends PlaceholderExpansion {

    private final CelestialDash plugin;

    // Constructor principal
    public CelestialPlaceholders(CelestialDash plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "celestialdash"; // Placeholder será %celestialdash_%
    }

    @Override
    public String getAuthor() {
        return "Miinico";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // mantiene el registro incluso tras /reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    // Manejo de placeholders
    @Override
    public String onPlaceholderRequest(Player player, String params) {

        if (player == null) return "";

        switch (params.toLowerCase()) {

            case "tears":
                return String.valueOf(TearUtils.countTears(player)); // %celestialdash_tears%

            case "cooldown":
                return String.valueOf(plugin.getDashHandler().getRemainingCooldownSeconds(player)); // %celestialdash_cooldown%

            case "double_ready":
                return String.valueOf(plugin.getDashHandler().isInDoubleDashWindow(player)); // %celestialdash_double_ready%

            default:
                return "";
        }
    }
}
