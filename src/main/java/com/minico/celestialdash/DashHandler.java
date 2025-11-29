package com.minico.celestialdash;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class DashHandler implements Listener {

    private final CelestialDash plugin;
    private final Messages messages;
    private final Map<UUID, Long> lastDash = new HashMap<>();
    private final Set<BukkitTask> activeTasks = new HashSet<>();

    // Configuration constants
    private static final double TRAIL_BEHIND_DISTANCE = 0.5;
    private static final int MAX_WORLD_HEIGHT = 320; // 1.18+ limit

    public DashHandler(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only trigger on right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // Check if player has ANY item in either hand
        boolean hasMainHandItem = mainHand != null && mainHand.getType() != Material.AIR;
        boolean hasOffHandItem = offHand != null && offHand.getType() != Material.AIR;

        // If both hands are empty, don't trigger
        if (!hasMainHandItem && !hasOffHandItem) {
            return;
        }

        // Permission check early
        if (!player.hasPermission("celestialdash.use")) {
            return;
        }

        // Check if player has a Celestial Tear in inventory
        if (!TearUtils.hasTear(player)) {
            return;
        }

        // Check which hand triggered the event
        EquipmentSlot hand = event.getHand();

        // Prevent processing if hand is null
        if (hand == null) {
            return;
        }

        // Check if offhand has Celestial Tear
        boolean offHandIsTear = TearUtils.isCelestialTear(offHand);

        // Special handling for Celestial Tear in off-hand
        if (hand == EquipmentSlot.OFF_HAND && offHandIsTear) {
            if (attemptDash(player)) {
                event.setCancelled(true);
            }
            return;
        }

        // Only process once per click (main hand has priority for non-tear items)
        if (hand == EquipmentSlot.OFF_HAND && hasMainHandItem) {
            return;
        }

        // Try to perform dash
        if (attemptDash(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up cooldown map to prevent memory leak
        lastDash.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Attempts to perform a dash for the player
     * Returns true if dash was successful, false otherwise
     */
    private boolean attemptDash(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if player has a Celestial Tear in inventory
        int tearSlot = TearUtils.findTearSlot(player);
        if (tearSlot == -1) {
            return false; // No tear found
        }

        // Cooldown check
        long now = System.currentTimeMillis();
        long last = lastDash.getOrDefault(uuid, 0L);

        if (now - last < plugin.getConfigSettings().dashCooldownMs) {
            sendCooldownMessage(player, now, last);
            return false;
        }

        // Height limit check
        if (player.getLocation().getY() >= MAX_WORLD_HEIGHT) {
            player.sendMessage(messages.getHeightLimitMessage());
            return false;
        }

        // Consume one Celestial Tear
        TearUtils.consumeTear(player, tearSlot);

        // Perform the dash
        performDash(player);
        lastDash.put(uuid, now);

        return true;
    }

    /**
     * Sends cooldown message to player
     */
    private void sendCooldownMessage(Player player, long now, long last) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();
        long remaining = (config.dashCooldownMs - (now - last)) / 1000L;
        if (remaining < 1) {
            remaining = 1;
        }

        String msg = messages.formatCooldown(remaining);
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(msg)
        );
    }

    /**
     * Performs the dash action with effects
     */
    private void performDash(Player player) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();
        Location loc = player.getLocation();
        Vector dir = loc.getDirection().normalize();

        // Apply velocity
        Vector velocity = dir.multiply(config.dashStrength);
        velocity.setY(config.dashLift);
        player.setVelocity(velocity);

        // Apply regeneration effect
        if (config.regenDurationTicks > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    config.regenDurationTicks,
                    config.regenAmplifier,
                    false,
                    true,
                    true
            ));
        }

        // Spawn impact particles
        if (config.dashParticleEnabled) {
            player.getWorld().spawnParticle(
                    config.dashParticle,
                    player.getLocation(),
                    config.dashParticleCount,
                    config.dashParticleOffsetX,
                    config.dashParticleOffsetY,
                    config.dashParticleOffsetZ,
                    config.dashParticleSpeed
            );
        }

        // Play dash sound
        if (config.dashSoundEnabled) {
            player.getWorld().playSound(
                    player.getLocation(),
                    config.dashSound,
                    config.dashSoundVolume,
                    config.dashSoundPitch
            );
        }

        // Spawn trail effect
        spawnTrailEffect(player);

        // Send confirmation message
        player.sendMessage(messages.getDashUsedMessage());
    }

    /**
     * Spawns trailing particle effect behind the player
     */
    private void spawnTrailEffect(Player player) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();

        if (!config.trailEnabled
                || config.trailDurationTicks <= 0
                || config.trailIntervalTicks <= 0) {
            return;
        }

        UUID uuid = player.getUniqueId();

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);

                // Stop if player is offline or duration exceeded
                if (p == null || !p.isOnline() || ticks >= config.trailDurationTicks) {
                    cancel();
                    activeTasks.remove(this);
                    return;
                }

                // Calculate trail position behind player
                Location playerLoc = p.getLocation();
                Location trailLoc = playerLoc.clone()
                        .subtract(playerLoc.getDirection().normalize().multiply(TRAIL_BEHIND_DISTANCE));

                // Spawn trail particles
                p.getWorld().spawnParticle(
                        config.trailParticle,
                        trailLoc,
                        config.trailParticleCount,
                        config.trailOffsetX,
                        config.trailOffsetY,
                        config.trailOffsetZ,
                        config.trailSpeed
                );

                ticks += config.trailIntervalTicks;
            }
        }.runTaskTimer(plugin, 0L, config.trailIntervalTicks);

        activeTasks.add(task);
    }

    /**
     * Cancels all active trail tasks
     * Should be called when plugin is disabled
     */
    public void cancelAllTasks() {
        for (BukkitTask task : activeTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();
        lastDash.clear();
    }
}