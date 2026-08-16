package com.minico.celestialdash;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * PlaceholderAPI expansion for CelestialDash.
 * Provides:
 *   %celestialdash_tears%        -> total Celestial Tears in player's inventory
 *   %celestialdash_cooldown%    -> remaining dash cooldown in seconds
 *   %celestialdash_double_ready% -> whether player is inside the double-dash combo window
 */
public class CelestialPlaceholders extends PlaceholderExpansion {

    /**
     * Scoreboards and tab lists can resolve the same placeholder many times per second.
     * A short cache avoids repeated full-inventory scans without making inventory changes
     * visibly stale.
     */
    private static final long TEAR_CACHE_TTL_NANOS = TimeUnit.MILLISECONDS.toNanos(250L);
    private static final long TEAR_CACHE_CLEANUP_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(5L);

    // Main plugin reference (used to access DashHandler and version info)
    private final CelestialDash plugin;
    private final LongSupplier nanoTime;
    private final long tearCacheTtlNanos;
    private final IntSupplier maxTearCacheEntries;

    /**
     * Access-order map gives the bounded cache an LRU eviction policy. It stores UUIDs,
     * never Player objects, so it cannot keep disconnected player instances alive.
     */
    private final Map<UUID, CachedTearCount> tearCountCache;
    private long lastTearCacheCleanupNanos = Long.MIN_VALUE;

    /**
     * Main constructor.
     *
     * @param plugin CelestialDash instance
     */
    public CelestialPlaceholders(CelestialDash plugin) {
        this(plugin, System::nanoTime, TEAR_CACHE_TTL_NANOS,
                () -> plugin.getSettings().placeholders().tearCacheMaxEntries());
    }

    /**
     * Package-private constructor with a controllable clock and size limit for focused tests.
     */
    CelestialPlaceholders(
            CelestialDash plugin,
            LongSupplier nanoTime,
            @SuppressWarnings("SameParameterValue") long tearCacheTtlNanos,
            int maxTearCacheEntries
    ) {
        this(plugin, nanoTime, tearCacheTtlNanos, () -> maxTearCacheEntries);
    }

    private CelestialPlaceholders(
            CelestialDash plugin,
            LongSupplier nanoTime,
            long tearCacheTtlNanos,
            IntSupplier maxTearCacheEntries
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        this.maxTearCacheEntries = Objects.requireNonNull(maxTearCacheEntries, "maxTearCacheEntries");
        if (tearCacheTtlNanos <= 0L) {
            throw new IllegalArgumentException("tearCacheTtlNanos must be positive");
        }
        if (maxTearCacheEntries.getAsInt() <= 0) {
            throw new IllegalArgumentException("maxTearCacheEntries must be positive");
        }

        this.tearCacheTtlNanos = tearCacheTtlNanos;
        this.tearCountCache = new LinkedHashMap<>(Math.min(maxTearCacheEntries.getAsInt(), 16), 0.75f, true);
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
    @SuppressWarnings("deprecation") // Uses the legacy Bukkit descriptor API for broad compatibility.
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
            case "tears" -> getCachedTearCount(player);

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

    private String getCachedTearCount(Player player) {
        UUID playerId = player.getUniqueId();
        long now = nanoTime.getAsLong();

        synchronized (tearCountCache) {
            cleanupExpiredTearCounts(now);

            CachedTearCount cached = tearCountCache.get(playerId);
            if (cached != null) {
                if (!isExpired(cached, now)) {
                    return cached.value();
                }
                tearCountCache.remove(playerId);
            }
        }

        String count = String.valueOf(TearUtils.countTears(player));
        long cachedAtNanos = nanoTime.getAsLong();

        synchronized (tearCountCache) {
            if (tearCountCache.size() >= maxTearCacheEntries.getAsInt()) {
                removeExpiredTearCounts(cachedAtNanos);
            }
            tearCountCache.put(playerId, new CachedTearCount(count, cachedAtNanos));
            trimTearCacheToMaximumSize();
        }
        return count;
    }

    private boolean isExpired(CachedTearCount entry, long now) {
        return now - entry.cachedAtNanos() >= tearCacheTtlNanos;
    }

    private void cleanupExpiredTearCounts(long now) {
        if (lastTearCacheCleanupNanos != Long.MIN_VALUE
                && now - lastTearCacheCleanupNanos < TEAR_CACHE_CLEANUP_INTERVAL_NANOS) {
            return;
        }
        removeExpiredTearCounts(now);
        lastTearCacheCleanupNanos = now;
    }

    private void removeExpiredTearCounts(long now) {
        tearCountCache.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
    }

    private void trimTearCacheToMaximumSize() {
        while (tearCountCache.size() > maxTearCacheEntries.getAsInt()) {
            Iterator<UUID> iterator = tearCountCache.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    int tearCacheSize() {
        synchronized (tearCountCache) {
            return tearCountCache.size();
        }
    }

    private record CachedTearCount(String value, long cachedAtNanos) {
    }
}
