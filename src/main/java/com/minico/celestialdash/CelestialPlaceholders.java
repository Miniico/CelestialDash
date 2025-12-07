package com.minico.celestialdash;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * PlaceholderAPI expansion for CelestialDash.
 *
 * Provides:
 *   %celestialdash_tears%        -> total Celestial Tears in player's inventory
 *   %celestialdash_cooldown%    -> remaining dash cooldown in seconds
 *   %celestialdash_double_ready% -> whether player is inside the double-dash combo window
 */
public class CelestialPlaceholders extends PlaceholderExpansion {

    // Main plugin reference (used to access DashHandler and version info)
    private final CelestialDash plugin;

    /**
     * Main constructor.
     *
     * @param plugin CelestialDash instance
     */
    public CelestialPlaceholders(CelestialDash plugin) {
        this.plugin = plugin;
    }

    /**
     * Placeholder root identifier.
     * All placeholders start with %celestialdash_*%
     */
    @Override
    public @NotNull String getIdentifier() {
        return "celestialdash";
    }

    /**
     * Author name for PAPI metadata.
     */
    @Override
    public @NotNull String getAuthor() {
        return "Miniico";
    }

    /**
     * Version string taken from plugin.yml.
     */
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Keep the expansion registered across /papi reload and server restarts.
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Allow registration if PlaceholderAPI is present.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * Handles placeholder resolution.
     *
     * @param player player requesting the placeholder (may be null in some PAPI contexts)
     * @param params placeholder argument (e.g. "tears", "cooldown", "double_ready")
     * @return resolved value or empty string if unsupported
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            // PAPI may call this without a player context; return empty to avoid NPEs.
            return "";
        }

        // Normalize to lower-case for case-insensitive matching.
        String key = params.toLowerCase(Locale.ROOT);

        // Modern switch expression for clarity and extensibility.
        return switch (key) {
            case "tears" -> String.valueOf(TearUtils.countTears(player));

            case "cooldown" -> String.valueOf(
                    plugin.getDashHandler().getRemainingCooldownSeconds(player)
            );

            case "double_ready" -> String.valueOf(
                    plugin.getDashHandler().isInDoubleDashWindow(player)
            );

            // Unknown placeholder -> return empty string (safe default).
            default -> "";
        };
    }
}
