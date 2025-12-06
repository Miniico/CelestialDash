package com.minico.celestialdash;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for CelestialDash.
 * Provides placeholders related to dash state and cooldown.

 * Identifiers:
 * - %celestialdash_cooldown%      → remaining cooldown (seconds)
 * - %celestialdash_double_ready%  → true / false if second dash window is active
 */
public class CelestialPlaceholders extends PlaceholderExpansion {

    private final CelestialDash plugin;

    // Main constructor
    public CelestialPlaceholders(CelestialDash plugin) {
        this.plugin = plugin;
    }

    // Expansion identifier: %celestialdash_...%
    @Override
    public @NotNull String getIdentifier() {
        return "celestialdash";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Miinico";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    // Keep the expansion registered on reload
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    // Placeholder handling
    @Override
    public @NotNull String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "cooldown" ->
                    String.valueOf(plugin.getDashHandler().getRemainingCooldownSeconds(player));
            case "double_ready" ->
                    String.valueOf(plugin.getDashHandler().isInDoubleDashWindow(player));
            default -> "";
        };
    }
}
