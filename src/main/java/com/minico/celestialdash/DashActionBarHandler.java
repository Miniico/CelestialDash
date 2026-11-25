package com.minico.celestialdash;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;

public final class DashActionBarHandler implements Listener {
    private final CelestialDash plugin;
    private final DashHandler dash;
    private final LongSupplier clock;
    private final BiConsumer<Player, String> sender;
    private final Set<UUID> visible = new HashSet<>();
    private final Map<UUID, Long> pausedUntil = new HashMap<>();
    private BukkitTask task;

    DashActionBarHandler(CelestialDash plugin, DashHandler dash) {
        this(plugin, dash, System::currentTimeMillis, DashActionBarHandler::send);
    }

    DashActionBarHandler(CelestialDash plugin, DashHandler dash, LongSupplier clock,
                         BiConsumer<Player, String> sender) {
        this.plugin = plugin;
        this.dash = dash;
        this.clock = clock;
        this.sender = sender;
    }

    @SuppressWarnings("deprecation")
    private static void send(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    void start() {
        reload();
    }

    void reload() {
        stop();
        PluginSettings.ActionBarSettings settings = plugin.getSettings().actionBar();
        if (settings.enabled()) {
            task = plugin.getServer().getScheduler().runTaskTimer(plugin,
                    () -> plugin.getServer().getOnlinePlayers().forEach(this::update),
                    1L, settings.updateIntervalTicks());
        }
    }

    void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (UUID id : visible) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) {
                sender.accept(player, "");
            }
        }
        visible.clear();
        pausedUntil.clear();
    }

    void showFeedback(Player player, String message) {
        pause(player);
        sender.accept(player, message);
    }

    void pause(Player player) {
        if (plugin.getSettings().actionBar().enabled()) {
            UUID id = player.getUniqueId();
            visible.remove(id);
            pausedUntil.put(id, clock.getAsLong() + 2000L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        visible.remove(id);
        pausedUntil.remove(id);
    }

    void update(Player player) {
        UUID id = player.getUniqueId();
        Long until = pausedUntil.get(id);
        if (until != null && clock.getAsLong() < until) {
            return;
        }
        pausedUntil.remove(id);

        PluginSettings.ActionBarSettings settings = plugin.getSettings().actionBar();
        String message = "";
        if (settings.enabled() && eligible(player, settings)) {
            message = status(player, settings);
        }
        if (!message.isEmpty()) {
            sender.accept(player, ChatColor.translateAlternateColorCodes('&', message));
            visible.add(id);
        } else if (visible.remove(id) && player.isOnline()) {
            sender.accept(player, "");
        }
    }

    private boolean eligible(Player player, PluginSettings.ActionBarSettings settings) {
        if (!player.isOnline() || player.isDead() || player.getGameMode() == GameMode.SPECTATOR
                || !player.hasPermission("celestialdash.use")) {
            return false;
        }
        if (plugin.getSettings().dash().isWorldBlacklisted(player.getWorld().getName())
                && !player.hasPermission("celestialdash.bypass-dash-blacklist")) {
            return false;
        }
        return settings.onlyWhileHoldingTear()
                ? TearUtils.isCelestialTear(player.getInventory().getItemInMainHand())
                : TearUtils.findTearSlot(player) >= 0;
    }

    private String status(Player player, PluginSettings.ActionBarSettings settings) {
        if (plugin.getSettings().dash().doubleDash().enabled() && dash.isInDoubleDashWindow(player)) {
            long remainingMs = dash.getRemainingDoubleDashWindowMillis(player);
            long tenths = (remainingMs + 99L) / 100L;
            return settings.secondDashMessage().replace("%seconds%", tenths / 10 + "." + tenths % 10);
        }
        long seconds = dash.getRemainingCooldownSeconds(player);
        return seconds > 0
                ? settings.cooldownMessage().replace("%seconds%", Long.toString(seconds))
                : settings.readyMessage();
    }
}
