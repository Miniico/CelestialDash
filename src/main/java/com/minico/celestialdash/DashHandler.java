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
import org.bukkit.event.player.PlayerMoveEvent;
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

    // Cooldown tracking
    private final Map<UUID, Long> lastDash = new HashMap<>();
    private final Set<BukkitTask> activeTasks = new HashSet<>();

    // Combo tracking
    private final Map<UUID, Long> lastComboTime = new HashMap<>();
    private final Map<UUID, Integer> comboLevel = new HashMap<>();

    // Air dash tracking
    private final Map<UUID, Integer> airDashCount = new HashMap<>();
    private final Map<UUID, Boolean> wasOnGround = new HashMap<>();

    // Configuration constants
    private static final double TRAIL_BEHIND_DISTANCE = 0.5;
    private static final int MAX_WORLD_HEIGHT = 320;

    public DashHandler(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean hasMainHandItem = mainHand.getType() != Material.AIR;
        boolean hasOffHandItem = offHand.getType() != Material.AIR;

        if (!hasMainHandItem && !hasOffHandItem) {
            return;
        }

        if (!player.hasPermission("celestialdash.use")) {
            return;
        }

        if (!TearUtils.hasTear(player)) {
            return;
        }

        EquipmentSlot hand = event.getHand();
        if (hand == null) {
            return;
        }

        boolean offHandIsTear = TearUtils.isCelestialTear(offHand);

        if (hand == EquipmentSlot.OFF_HAND && offHandIsTear) {
            if (attemptDash(player)) {
                event.setCancelled(true);
            }
            return;
        }

        if (hand == EquipmentSlot.OFF_HAND && hasMainHandItem) {
            return;
        }

        if (attemptDash(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean onGround = player.isOnGround();
        Boolean wasGround = wasOnGround.get(uuid);

        // Reset air dash count when landing
        if (onGround && (wasGround == null || !wasGround)) {
            airDashCount.remove(uuid);
        }

        wasOnGround.put(uuid, onGround);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastDash.remove(uuid);
        lastComboTime.remove(uuid);
        comboLevel.remove(uuid);
        airDashCount.remove(uuid);
        wasOnGround.remove(uuid);
    }

    private boolean attemptDash(Player player) {
        UUID uuid = player.getUniqueId();
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();

        // Check if player has tears
        int tearSlot = TearUtils.findTearSlot(player);
        if (tearSlot == -1) {
            return false;
        }

        // Determine dash type
        boolean isOnGround = player.isOnGround();
        boolean isAirDash = !isOnGround && config.airDashEnabled;
        boolean isCombo = false;
        int currentCombo = 0;

        // Check combo status
        if (config.comboEnabled && isOnGround) {
            long now = System.currentTimeMillis();
            Long lastCombo = lastComboTime.get(uuid);

            if (lastCombo != null) {
                long timeSinceLastDash = now - lastCombo;
                long comboWindow = (long) (config.comboWindowSeconds * 1000);

                if (timeSinceLastDash <= comboWindow) {
                    isCombo = true;
                    currentCombo = comboLevel.getOrDefault(uuid, 0) + 1;
                    comboLevel.put(uuid, currentCombo);
                } else {
                    // Combo expired, reset
                    comboLevel.remove(uuid);
                    currentCombo = 0;
                }
            }

            lastComboTime.put(uuid, now);
        }

        // Check air dash limit
        if (isAirDash) {
            int airDashes = airDashCount.getOrDefault(uuid, 0);
            if (airDashes >= config.maxAirDashes) {
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(messages.getAirDashLimitMessage())
                );
                return false;
            }
            airDashCount.put(uuid, airDashes + 1);
        }

        // Cooldown check
        long now = System.currentTimeMillis();
        long last = lastDash.getOrDefault(uuid, 0L);

        if (now - last < config.dashCooldownMs) {
            sendCooldownMessage(player, now, last);
            return false;
        }

        // Height limit check
        if (player.getLocation().getY() >= MAX_WORLD_HEIGHT) {
            player.sendMessage(messages.getHeightLimitMessage());
            return false;
        }

        // Calculate tear cost
        int tearCost = 1;
        if (isAirDash) {
            tearCost += config.airDashExtraCost;
        }

        // Check if player has enough tears
        if (TearUtils.countTears(player) < tearCost) {
            player.sendMessage(messages.getNotEnoughTearsMessage());
            return false;
        }

        // Consume tears
        for (int i = 0; i < tearCost; i++) {
            int slot = TearUtils.findTearSlot(player);
            if (slot != -1) {
                TearUtils.consumeTear(player, slot);
            }
        }

        // Perform the dash
        performDash(player, isCombo, isAirDash, currentCombo);
        lastDash.put(uuid, now);

        return true;
    }

    private void performDash(Player player, boolean isCombo, boolean isAirDash, int comboLevel) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();
        Location loc = player.getLocation();
        Vector dir = loc.getDirection().normalize();

        // Calculate multipliers
        double strengthMultiplier = 1.0;
        double liftMultiplier = 1.0;

        if (isCombo && comboLevel >= 2) {
            strengthMultiplier = config.comboStrengthMultiplier;
            liftMultiplier = config.comboLiftMultiplier;
        }

        if (isAirDash) {
            strengthMultiplier *= config.airDashStrengthReduction;
        }

        // Apply velocity
        Vector velocity = dir.multiply(config.dashStrength * strengthMultiplier);
        velocity.setY(config.dashLift * liftMultiplier);
        player.setVelocity(velocity);

        // Apply potion effects
        if (config.regenDurationTicks > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    config.regenDurationTicks,
                    config.regenAmplifier,
                    false, true, true
            ));
        }

        // Combo effects
        if (isCombo && comboLevel >= 2) {
            // Slow Falling (no fall damage)
            if (config.comboSlowFallingDuration > 0) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        config.comboSlowFallingDuration,
                        0,
                        false, true, true
                ));
                player.sendMessage(messages.getFallProtectionMessage());
            }

            // Speed boost
            if (config.comboSpeedDuration > 0) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        config.comboSpeedDuration,
                        config.comboSpeedAmplifier,
                        false, true, true
                ));
            }
        }

        // Spawn particles
        spawnDashParticles(player, isCombo, isAirDash, comboLevel);

        // Play sound
        playDashSound(player, isCombo, isAirDash);

        // Spawn trail
        spawnTrailEffect(player);

        // Send message
        sendDashMessage(player, isCombo, isAirDash, comboLevel);
    }

    private void spawnDashParticles(Player player, boolean isCombo, boolean isAirDash, int comboLevel) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();

        if (isAirDash) {
            player.getWorld().spawnParticle(
                    config.airDashParticle,
                    player.getLocation(),
                    config.airDashParticleCount,
                    0.4, 0.5, 0.4,
                    0.05
            );
        } else if (isCombo && comboLevel >= 2) {
            player.getWorld().spawnParticle(
                    config.comboParticle,
                    player.getLocation(),
                    config.comboParticleCount,
                    0.5, 0.6, 0.5,
                    0.1
            );
        } else if (config.dashParticleEnabled) {
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
    }

    private void playDashSound(Player player, boolean isCombo, boolean isAirDash) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();

        if (isAirDash) {
            player.getWorld().playSound(
                    player.getLocation(),
                    config.airDashSound,
                    config.airDashSoundVolume,
                    config.airDashSoundPitch
            );
        } else if (isCombo) {
            player.getWorld().playSound(
                    player.getLocation(),
                    config.comboSound,
                    config.comboSoundVolume,
                    config.comboSoundPitch
            );
        } else if (config.dashSoundEnabled) {
            player.getWorld().playSound(
                    player.getLocation(),
                    config.dashSound,
                    config.dashSoundVolume,
                    config.dashSoundPitch
            );
        }
    }

    private void sendDashMessage(Player player, boolean isCombo, boolean isAirDash, int comboLevel) {
        if (isAirDash) {
            player.sendMessage(messages.getAirDashMessage());
        } else if (isCombo && comboLevel >= 2) {
            player.sendMessage(messages.getComboDashMessage(comboLevel));
        } else {
            player.sendMessage(messages.getDashUsedMessage());
        }
    }

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

    private void spawnTrailEffect(Player player) {
        CelestialDash.ConfigSettings config = plugin.getConfigSettings();

        if (!config.trailEnabled || config.trailDurationTicks <= 0 || config.trailIntervalTicks <= 0) {
            return;
        }

        UUID uuid = player.getUniqueId();

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);

                if (p == null || !p.isOnline() || ticks >= config.trailDurationTicks) {
                    cancel();
                    activeTasks.remove(this);
                    return;
                }

                Location playerLoc = p.getLocation();
                Location trailLoc = playerLoc.clone()
                        .subtract(playerLoc.getDirection().normalize().multiply(TRAIL_BEHIND_DISTANCE));

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

    public void cancelAllTasks() {
        for (BukkitTask task : activeTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();
        lastDash.clear();
        lastComboTime.clear();
        comboLevel.clear();
        airDashCount.clear();
        wasOnGround.clear();
    }
}