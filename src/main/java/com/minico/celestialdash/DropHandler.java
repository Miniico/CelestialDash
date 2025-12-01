package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropHandler {

    private final CelestialDash plugin;
    private final Messages messages; // NEW

    private BukkitRunnable task;
    private final Map<UUID, Long> lastDrop = new HashMap<>();

    public DropHandler(CelestialDash plugin, Messages messages) { // CHANGED CONSTRUCTOR
        this.plugin = plugin;
        this.messages = messages;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    World world = player.getWorld();
                    if (!world.hasStorm()) {
                        continue;
                    }

                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    long last = lastDrop.getOrDefault(uuid, 0L);

                    if (now - last < plugin.getDropCooldownMs()) {
                        continue;
                    }

                    if (Math.random() < plugin.getDropChance()) {
                        ItemStack tear = TearUtils.createCelestialTear();
                        world.dropItemNaturally(player.getLocation(), tear);
                        lastDrop.put(uuid, now);

                        // Send drop message to the player
                        player.sendMessage(messages.getTearDropMessage());
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
}