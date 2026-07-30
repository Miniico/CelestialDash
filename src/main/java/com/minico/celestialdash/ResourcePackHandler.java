package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Delivers the configured server resource pack to joining players.
 */
public final class ResourcePackHandler implements Listener {

    private static final Pattern SHA1_PATTERN = Pattern.compile("[0-9a-fA-F]{40}");

    private final CelestialDash plugin;
    private final Set<UUID> playersAwaitingStatus = ConcurrentHashMap.newKeySet();
    private String lastConfigurationWarning;

    ResourcePackHandler(CelestialDash plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                sendTo(player);
            }
        });
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersAwaitingStatus.remove(event.getPlayer().getUniqueId());
    }

    void stop() {
        playersAwaitingStatus.clear();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        if (!playersAwaitingStatus.contains(player.getUniqueId())) {
            return;
        }

        switch (event.getStatus()) {
            case ACCEPTED -> plugin.getLogger().fine(player.getName()
                    + " accepted the CelestialDash resource pack request.");
            case SUCCESSFULLY_LOADED -> {
                plugin.getLogger().fine(player.getName()
                        + " successfully loaded the CelestialDash resource pack.");
                playersAwaitingStatus.remove(player.getUniqueId());
            }
            case DECLINED -> {
                plugin.getLogger().warning(player.getName()
                        + " declined the CelestialDash resource pack.");
                playersAwaitingStatus.remove(player.getUniqueId());
            }
            case FAILED_DOWNLOAD -> {
                plugin.getLogger().warning(player.getName()
                        + " failed to download the CelestialDash resource pack.");
                playersAwaitingStatus.remove(player.getUniqueId());
            }
        }
    }

    @SuppressWarnings("deprecation") // The String prompt overload remains compatible with Spigot and Purpur.
    public SendResult sendTo(Player player) {
        if (player == null || !player.isOnline()) {
            return SendResult.OFFLINE;
        }

        PluginSettings.ResourcePackSettings settings = plugin.getSettings().resourcePack();
        if (!settings.enabled()) {
            return SendResult.DISABLED;
        }

        String configurationError = validate(settings);
        if (configurationError != null) {
            warnOnce(configurationError);
            return SendResult.INVALID_CONFIGURATION;
        }

        lastConfigurationWarning = null;
        byte[] hash = decodeSha1(settings.sha1());
        String prompt = ChatColor.translateAlternateColorCodes('&', settings.prompt());
        UUID playerId = player.getUniqueId();
        playersAwaitingStatus.add(playerId);
        try {
            player.setResourcePack(settings.url(), hash, prompt, settings.required());
            return SendResult.SENT;
        } catch (IllegalArgumentException exception) {
            playersAwaitingStatus.remove(playerId);
            plugin.getLogger().warning("Could not send the resource pack to " + player.getName() + ": "
                    + exception.getMessage());
            return SendResult.FAILED;
        }
    }

    static boolean isFailureStatus(PlayerResourcePackStatusEvent.Status status) {
        return status == PlayerResourcePackStatusEvent.Status.DECLINED
                || status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD;
    }

    private String validate(PluginSettings.ResourcePackSettings settings) {
        if (!isValidHttpsUrl(settings.url())) {
            return "resource-pack.url must be a valid public HTTPS URL. The resource pack was not sent.";
        }
        if (!isValidSha1(settings.sha1())) {
            return "resource-pack.sha1 must contain exactly 40 hexadecimal characters. "
                    + "The resource pack was not sent.";
        }
        return null;
    }

    private void warnOnce(String warning) {
        if (!warning.equals(lastConfigurationWarning)) {
            plugin.getLogger().warning(warning);
            lastConfigurationWarning = warning;
        }
    }

    static boolean isValidHttpsUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(url);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    static boolean isValidSha1(String sha1) {
        return sha1 != null && SHA1_PATTERN.matcher(sha1).matches();
    }

    static byte[] decodeSha1(String sha1) {
        return HexFormat.of().parseHex(sha1);
    }

    public enum SendResult {
        SENT,
        DISABLED,
        INVALID_CONFIGURATION,
        OFFLINE,
        FAILED
    }
}
