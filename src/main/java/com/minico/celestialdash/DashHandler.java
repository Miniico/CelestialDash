package com.minico.celestialdash;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DashHandler implements Listener {

    private final CelestialDash plugin;
    private final Messages messages;

    // Last dash time per player (for normal cooldown)
    private final Map<UUID, Long> lastDash = new HashMap<>();
    // Window for performing the second dash
    private final Map<UUID, Long> comboWindowEnd = new HashMap<>();
    // Fall-damage immunity after second dash
    private final Map<UUID, Long> fallImmunityUntil = new HashMap<>();
    // One active particle-trail task per player.
    private final Map<UUID, BukkitTask> trailTasks = new HashMap<>();
    private final BukkitTask stateCleanupTask;

    public DashHandler(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.stateCleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredState();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    @SuppressWarnings({"unused", "deprecation"})
    public void onPlayerUseTear(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Only allow dashing if the main-hand item IS a Celestial Tear
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!TearUtils.isCelestialTear(mainHand)) {
            return;
        }

        if (!player.hasPermission("celestialdash.use")) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(messages.getNoUsePermissionMessage())
            );
            return;
        }

        PluginSettings.DashSettings dashSettings = plugin.getSettings().dash();
        if (dashSettings.isWorldBlacklisted(player.getWorld().getName())
                && !player.hasPermission("celestialdash.bypass-dash-blacklist")) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(messages.getDashWorldDisabledMessage())
            );
            return;
        }

        long now = System.currentTimeMillis();

        boolean doubleDashEnabled = dashSettings.doubleDash().enabled();
        boolean isSecondDash = false;

        // Check if this click should count as second dash
        if (doubleDashEnabled) {
            Long windowEnd = comboWindowEnd.get(uuid);
            if (windowEnd != null) {
                if (now <= windowEnd) {
                    isSecondDash = true;
                } else {
                    // window expired
                    comboWindowEnd.remove(uuid);
                }
            }
        }

        // Cooldown only blocks the FIRST dash, never the second
        if (!isSecondDash) {
            long last = lastDash.getOrDefault(uuid, 0L);
            long cd = dashSettings.cooldownMs();
            long diff = now - last;

            if (diff < cd) {
                long remaining = (cd - diff) / 1000L;
                if (remaining < 1) remaining = 1;

                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(messages.formatCooldown(remaining))
                );
                return;
            }
        }

        // Consume the exact tear that triggered the interaction. Searching the
        // whole inventory here could otherwise spend a different stack first.
        int slot = player.getInventory().getHeldItemSlot();
        if (slot == -1) {
            // The held slot is unavailable; do not consume an unrelated stack.
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(messages.getNoTearsMessage())
            );
            return;
        }

        // Consume 1 tear
        TearUtils.consumeTear(player, slot);

        if (isSecondDash) {
            // Second dash: stronger + fall-damage immunity
            performDash(player, true);
            applyFallImmunity(uuid);
            comboWindowEnd.remove(uuid);
        } else {
            // First dash: normal dash + open combo window
            performDash(player, false);

            if (doubleDashEnabled) {
                long windowMs = dashSettings.doubleDash().windowMs();
                comboWindowEnd.put(uuid, now + windowMs);
            }
        }

        // Update last dash for cooldown
        lastDash.put(uuid, now);
    }

    private void applyFallImmunity(UUID uuid) {
        int ticks = plugin.getSettings().dash().doubleDash().fallImmunityTicks();
        if (ticks <= 0) return;

        long durationMs = ticks * 50L;
        fallImmunityUntil.put(uuid, System.currentTimeMillis() + durationMs);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        UUID uuid = player.getUniqueId();
        Long until = fallImmunityUntil.get(uuid);
        if (until == null) return;

        long now = System.currentTimeMillis();
        if (now <= until) {
            event.setCancelled(true);
        }
        // Always clear stored immunity once it's checked
        fallImmunityUntil.remove(uuid);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Keep the normal dash cooldown across reconnects.
        comboWindowEnd.remove(uuid);
        fallImmunityUntil.remove(uuid);
        cancelTrail(uuid);
    }

    public void stop() {
        stateCleanupTask.cancel();
        lastDash.clear();
        comboWindowEnd.clear();
        fallImmunityUntil.clear();
        trailTasks.values().forEach(BukkitTask::cancel);
        trailTasks.clear();
    }

    private void cleanupExpiredState() {
        long now = System.currentTimeMillis();
        long cooldown = plugin.getSettings().dash().cooldownMs();

        lastDash.entrySet().removeIf(entry -> now - entry.getValue() >= cooldown);
        comboWindowEnd.entrySet().removeIf(entry -> now > entry.getValue());
        fallImmunityUntil.entrySet().removeIf(entry -> now > entry.getValue());
    }

    private void performDash(Player player, boolean secondDash) {
        // Direction and base strength
        Vector dir = player.getLocation().getDirection().normalize();

        PluginSettings.DashSettings dashSettings = plugin.getSettings().dash();
        double strength = dashSettings.strength();
        double lift = dashSettings.lift();

        if (secondDash) {
            PluginSettings.DoubleDashSettings doubleDashSettings = dashSettings.doubleDash();
            strength *= doubleDashSettings.strengthMultiplier();
            lift *= doubleDashSettings.liftMultiplier();
        }

        Vector velocity = dir.multiply(strength);
        velocity.setY(lift);
        player.setVelocity(velocity);

        // Regeneration
        if (dashSettings.regenerationDurationTicks() > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    dashSettings.regenerationDurationTicks(),
                    dashSettings.regenerationAmplifier(),
                    false,
                    true,
                    true
            ));
        }

        // Impact particle
        PluginSettings.ParticleSettings impactParticle = dashSettings.impactParticle();
        if (impactParticle.enabled()) {
            player.getWorld().spawnParticle(
                    impactParticle.type(),
                    player.getLocation(),
                    impactParticle.count(),
                    impactParticle.offsetX(),
                    impactParticle.offsetY(),
                    impactParticle.offsetZ(),
                    impactParticle.speed()
            );
        }

        // Sound
        PluginSettings.SoundSettings sound = dashSettings.sound();
        if (sound.enabled()) {
            player.getWorld().playSound(
                    player.getLocation(),
                    sound.sound(),
                    sound.volume(),
                    sound.pitch()
            );
        }

        startTrail(player);

        // Different messages for first and second dash
        if (secondDash) {
            player.sendMessage(messages.getSecondDashMessage());
        } else {
            player.sendMessage(messages.getDashUsedMessage());
        }
    }

    private void startTrail(Player player) {
        PluginSettings.TrailSettings trailSettings = plugin.getSettings().dash().trail();
        if (!trailSettings.enabled()
                || trailSettings.durationTicks() <= 0
                || trailSettings.intervalTicks() <= 0) {
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelTrail(uuid);

        BukkitRunnable trail = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                Player currentPlayer = Bukkit.getPlayer(uuid);
                PluginSettings.TrailSettings activeTrailSettings = plugin.getSettings().dash().trail();
                if (currentPlayer == null || !currentPlayer.isOnline() || ticks >= activeTrailSettings.durationTicks()) {
                    cancel();
                    trailTasks.remove(uuid);
                    return;
                }

                Location back = currentPlayer.getLocation().clone()
                        .subtract(currentPlayer.getLocation().getDirection().normalize().multiply(0.5));

                currentPlayer.getWorld().spawnParticle(
                        activeTrailSettings.particle(),
                        back,
                        activeTrailSettings.count(),
                        activeTrailSettings.offsetX(),
                        activeTrailSettings.offsetY(),
                        activeTrailSettings.offsetZ(),
                        activeTrailSettings.speed()
                );

                ticks += activeTrailSettings.intervalTicks();
            }
        };
        trailTasks.put(uuid, trail.runTaskTimer(plugin, 0L, trailSettings.intervalTicks()));
    }

    private void cancelTrail(UUID uuid) {
        BukkitTask trailTask = trailTasks.remove(uuid);
        if (trailTask != null) {
            trailTask.cancel();
        }
    }

    // ===== Helper methods for placeholders =====

    public long getRemainingCooldownSeconds(Player player) {
        UUID uuid = player.getUniqueId();
        long last = lastDash.getOrDefault(uuid, 0L);
        return calculateRemainingCooldownSeconds(last, System.currentTimeMillis(), plugin.getSettings().dash().cooldownMs());
    }

    static long calculateRemainingCooldownSeconds(long lastDashMs, long nowMs, long cooldownMs) {
        long elapsed = nowMs - lastDashMs;
        if (cooldownMs <= 0 || elapsed >= cooldownMs) {
            return 0L;
        }
        return (long) Math.ceil((cooldownMs - elapsed) / 1000.0);
    }

    public boolean isInDoubleDashWindow(Player player) {
        UUID uuid = player.getUniqueId();
        Long windowEnd = comboWindowEnd.get(uuid);
        if (windowEnd == null) {
            return false;
        }
        if (System.currentTimeMillis() <= windowEnd) {
            return true;
        }
        comboWindowEnd.remove(uuid);
        return false;
    }
}
