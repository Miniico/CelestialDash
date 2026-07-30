package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropHandler implements Listener {

    private final CelestialDash plugin;
    private BukkitRunnable task;

    private final Map<UUID, Long> lastDrop = new HashMap<>();

    public DropHandler(CelestialDash plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("celestialdash.receive")) {
                        continue;
                    }

                    World world = player.getWorld();

                    // Skip blacklisted worlds
                    PluginSettings.DropSettings dropSettings = plugin.getSettings().drops();
                    if (dropSettings.isWorldBlacklisted(world.getName())) {
                        continue;
                    }

                    // Only drop during storms in allowed worlds
                    if (!world.hasStorm()) {
                        continue;
                    }

                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    long last = lastDrop.getOrDefault(uuid, 0L);

                    if (now - last < dropSettings.cooldownMs()) {
                        continue;
                    }

                    if (Math.random() < dropSettings.chance()) {
                        ItemStack tear = TearUtils.createCelestialTear();
                        if (dropSettings.deliveryMode() == PluginSettings.DropDeliveryMode.INVENTORY) {
                            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(tear);
                            for (ItemStack leftover : leftovers.values()) {
                                world.dropItemNaturally(player.getLocation(), leftover);
                            }
                        } else {
                            world.dropItemNaturally(player.getLocation(), tear);
                        }
                        lastDrop.put(uuid, now);

                        // Notify only the player who received the tear
                        plugin.getMessages().sendTearDropMessage(player);
                    }
                }
            }
        };

        // Run every second
        task.runTaskTimer(plugin, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastDrop.clear();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastDrop.remove(event.getPlayer().getUniqueId());
    }
}
