package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Sound;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropHandler {

    private final CelestialDash plugin;
    private final Map<UUID, Long> lastDrop = new HashMap<>();
    private BukkitTask dropTask;

    // Configuration constants
    private static final double DROP_HEIGHT_ABOVE_PLAYER = 10.0;
    private static final double DROP_RADIUS = 5.0; // blocks
    private static final int CHECK_INTERVAL_TICKS = 20; // 1 second

    public DropHandler(CelestialDash plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the drop checking task
     */
    public void start() {
        if (dropTask != null && !dropTask.isCancelled()) {
            plugin.getLogger().warning("DropHandler task already running!");
            return;
        }

        dropTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkAndDropTear(player);
            }

            // Clean up disconnected players periodically
            cleanupDisconnectedPlayers();

        }, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);

        plugin.getLogger().info("DropHandler task started.");
    }

    /**
     * Stops the drop checking task
     */
    public void stop() {
        if (dropTask != null && !dropTask.isCancelled()) {
            dropTask.cancel();
            dropTask = null;
            plugin.getLogger().info("DropHandler task stopped.");
        }
        lastDrop.clear();
    }

    /**
     * Checks if a tear should drop for a player
     */
    private void checkAndDropTear(Player player) {
        // Permission check
        if (!player.hasPermission("celestialdash.receive")) {
            return;
        }

        World world = player.getWorld();

        // Only drop in overworld during storms
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!world.hasStorm()) {
            return;
        }

        // Check cooldown
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastDrop.getOrDefault(uuid, 0L);

        CelestialDash.ConfigSettings config = plugin.getConfigSettings();
        if (now - last < config.dropCooldownMs) {
            return;
        }

        // Check drop chance
        if (Math.random() > config.dropChance) {
            return;
        }

        // Try to find a safe drop location
        Location dropLocation = findSafeDropLocation(player);
        if (dropLocation == null) {
            return; // No safe location found
        }

        // Drop the tear
        world.dropItem(dropLocation, TearUtils.createCelestialTear());
        lastDrop.put(uuid, now);

        // Notify player
        notifyPlayer(player);
    }

    /**
     * Finds a safe location to drop a tear near the player
     * Returns null if no safe location found
     */
    private Location findSafeDropLocation(Player player) {
        Location playerLoc = player.getLocation();

        // Random offset within radius
        double offsetX = (Math.random() * DROP_RADIUS * 2) - DROP_RADIUS;
        double offsetZ = (Math.random() * DROP_RADIUS * 2) - DROP_RADIUS;

        Location dropLoc = playerLoc.clone().add(offsetX, DROP_HEIGHT_ABOVE_PLAYER, offsetZ);

        // Find the highest solid block at this location
        Block highestBlock = player.getWorld().getHighestBlockAt(dropLoc);

        // If the highest block is higher than our drop location, adjust
        if (highestBlock.getY() > dropLoc.getY()) {
            dropLoc.setY(highestBlock.getY() + 2); // 2 blocks above highest block
        }

        // Make sure the drop location is not inside a block
        Block blockAtDrop = dropLoc.getBlock();
        if (blockAtDrop.getType().isSolid()) {
            // Try to find air above
            for (int i = 0; i < 10; i++) {
                dropLoc.add(0, 1, 0);
                if (!dropLoc.getBlock().getType().isSolid()) {
                    break;
                }
            }

            // If still solid, give up
            if (dropLoc.getBlock().getType().isSolid()) {
                return null;
            }
        }

        return dropLoc;
    }

    /**
     * Notifies the player that a tear dropped nearby
     */
    private void notifyPlayer(Player player) {
        String message = ChatColor.AQUA + "✦ " + ChatColor.WHITE +
                "A Celestial Tear falls from the stormy sky nearby...";
        player.sendMessage(message);

        // Optional: Play a subtle sound
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 1.8f);
    }

    /**
     * Removes disconnected players from the cooldown map
     */
    private void cleanupDisconnectedPlayers() {
        lastDrop.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            return player == null || !player.isOnline();
        });
    }
}