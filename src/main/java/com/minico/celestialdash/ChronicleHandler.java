package com.minico.celestialdash;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gives each player the v1.1.7 chronicle once, including players who joined
 * before the marker existed.
 */
public final class ChronicleHandler implements Listener {

    private final CelestialDash plugin;
    private final NamespacedKey receivedChronicleKey;
    private final NamespacedKey chronicleKey;
    private final Map<UUID, Long> lastSelfReissue = new HashMap<>();
    private BukkitTask cleanupTask;

    ChronicleHandler(CelestialDash plugin) {
        this.plugin = plugin;
        receivedChronicleKey = new NamespacedKey(plugin, "received_1_1_7_chronicle");
        chronicleKey = new NamespacedKey(plugin, "celestial_chronicle");
    }

    void start() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredSelfReissueCooldowns(System.currentTimeMillis());
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    void stop() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        lastSelfReissue.clear();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSettings().chronicle().enabled()) {
            return;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();
        if (data.has(receivedChronicleKey, PersistentDataType.BYTE)) {
            return;
        }

        deliverChronicle(player);

        // Mark only after the book was given to the inventory or safely dropped as overflow.
        data.set(receivedChronicleKey, PersistentDataType.BYTE, (byte) 1);
    }

    /**
     * Delivers a new localized copy without changing the player's one-time delivery marker.
     *
     * @return {@code true} when the inventory overflow was dropped at the player's location
     */
    boolean deliverChronicle(Player player) {
        String locale = getClientLocale(player);
        ItemStack chronicle = CelestialChronicle.create(locale, chronicleKey);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(chronicle);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
        notifyDelivery(player, locale);
        return !leftovers.isEmpty();
    }

    /**
     * Reissues the Chronicle to its reader without changing the one-time delivery marker.
     *
     * @return seconds remaining on the recovery cooldown, or {@code 0} after a new copy was given
     */
    long reissueToSelf(Player player) {
        long now = System.currentTimeMillis();
        long cooldownMs = plugin.getSettings().chronicle().selfReissueCooldownMs();
        Long lastReissue = lastSelfReissue.get(player.getUniqueId());
        if (lastReissue != null) {
            long remainingMs = cooldownMs - (now - lastReissue);
            if (remainingMs > 0L) {
                return (long) Math.ceil(remainingMs / 1_000.0);
            }
        }

        deliverChronicle(player);
        if (cooldownMs > 0L) {
            lastSelfReissue.put(player.getUniqueId(), now);
        } else {
            lastSelfReissue.remove(player.getUniqueId());
        }
        return 0L;
    }

    boolean isChronicle(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.WRITTEN_BOOK || !item.hasItemMeta()) {
            return false;
        }
        return Byte.valueOf((byte) 1).equals(item.getItemMeta().getPersistentDataContainer()
                .get(chronicleKey, PersistentDataType.BYTE));
    }

    void cleanupExpiredSelfReissueCooldowns(long nowMs) {
        long cooldownMs = plugin.getSettings().chronicle().selfReissueCooldownMs();
        lastSelfReissue.entrySet().removeIf(entry -> nowMs - entry.getValue() >= cooldownMs);
    }

    private void notifyDelivery(Player player, String locale) {
        PluginSettings.ChronicleSettings settings = plugin.getSettings().chronicle();
        if (settings.notificationEnabled()) {
            player.sendMessage("§b" + CelestialChronicle.deliveryMessage(locale));
        }
        if (settings.deliverySoundEnabled()) {
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
        }
    }

    @SuppressWarnings("deprecation") // getLocale supports the oldest target server APIs.
    private static String getClientLocale(Player player) {
        try {
            return player.getLocale();
        } catch (RuntimeException ignored) {
            // Locale selection is optional; use the English Chronicle if an implementation cannot report it.
            return null;
        }
    }
}
