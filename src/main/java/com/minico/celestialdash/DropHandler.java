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
    private BukkitRunnable task;

    private final Map<UUID, Long> lastDrop = new HashMap<>();

    public DropHandler(CelestialDash plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the repeating task that spawns Celestial Tears
     * around players during thunderstorms.
     */
    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                // Iterate over all online players and spawn tears
                // around them if the world is currently in a storm.
                for (Player player : Bukkit.getOnlinePlayers()) {
                    World world = player.getWorld();
                    if (!world.hasStorm()) {
                        continue;
                    }

                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    long last = lastDrop.getOrDefault(uuid, 0L);

                    // Per-player cooldown between storm drops
                    if (now - last < plugin.getDropCooldownMs()) {
                        continue;
                    }

                    // Random chance per second for a tear to drop
                    if (Math.random() < plugin.getDropChance()) {
                        ItemStack tear = TearUtils.createCelestialTear();
                        world.dropItemNaturally(player.getLocation(), tear);

                        // Optional message when a tear drops
                        if (plugin.getMessages() != null) {
                            player.sendMessage(plugin.getMessages().getTearDropMessage());
                        }

                        lastDrop.put(uuid, now);
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
