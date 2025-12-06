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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
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

    public DashHandler(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerUseTear(PlayerInteractEvent event) {
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

        long now = System.currentTimeMillis();

        boolean doubleDashEnabled = plugin.isDoubleDashEnabled();
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
            long cd = plugin.getDashCooldownMs();
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

        // Find a tear in inventory
        int slot = TearUtils.findTearSlot(player);
        if (slot == -1) {
            // No Celestial Tears → action bar message
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
                long windowMs = plugin.getDoubleDashWindowMs();
                comboWindowEnd.put(uuid, now + windowMs);
            }
        }

        // Update last dash for cooldown
        lastDash.put(uuid, now);
    }

    private void applyFallImmunity(UUID uuid) {
        int ticks = plugin.getDoubleDashFallImmunityTicks();
        if (ticks <= 0) return;

        long durationMs = ticks * 50L;
        fallImmunityUntil.put(uuid, System.currentTimeMillis() + durationMs);
    }

    @EventHandler
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

    private void performDash(Player player, boolean secondDash) {
        // Direction and base strength
        Vector dir = player.getLocation().getDirection().normalize();

        double strength = plugin.getDashStrength();
        double lift = plugin.getDashLift();

        if (secondDash) {
            // Slight buff on second dash (tweak if needed)
            strength *= 1.2;
            lift *= 1.1;
        }

        Vector velocity = dir.multiply(strength);
        velocity.setY(lift);
        player.setVelocity(velocity);

        // Regeneration
        if (plugin.getRegenDurationTicks() > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    plugin.getRegenDurationTicks(),
                    plugin.getRegenAmplifier(),
                    false,
                    true,
                    true
            ));
        }

        // Impact particle
        if (plugin.isDashParticleEnabled()) {
            player.getWorld().spawnParticle(
                    plugin.getDashParticle(),
                    player.getLocation(),
                    plugin.getDashParticleCount(),
                    plugin.getDashParticleOffsetX(),
                    plugin.getDashParticleOffsetY(),
                    plugin.getDashParticleOffsetZ(),
                    plugin.getDashParticleSpeed()
            );
        }

        // Sound
        if (plugin.isDashSoundEnabled()) {
            player.getWorld().playSound(
                    player.getLocation(),
                    plugin.getDashSound(),
                    plugin.getDashSoundVolume(),
                    plugin.getDashSoundPitch()
            );
        }

        // Trail effect
        if (plugin.isTrailEnabled()
                && plugin.getTrailDurationTicks() > 0
                && plugin.getTrailIntervalTicks() > 0) {

            UUID uuid = player.getUniqueId();

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isOnline() || ticks >= plugin.getTrailDurationTicks()) {
                        cancel();
                        return;
                    }

                    Location back = p.getLocation().clone()
                            .subtract(p.getLocation().getDirection().normalize().multiply(0.5));

                    p.getWorld().spawnParticle(
                            plugin.getTrailParticle(),
                            back,
                            plugin.getTrailParticleCount(),
                            plugin.getTrailOffsetX(),
                            plugin.getTrailOffsetY(),
                            plugin.getTrailOffsetZ(),
                            plugin.getTrailSpeed()
                    );

                    ticks += plugin.getTrailIntervalTicks();
                }
            }.runTaskTimer(plugin, 0L, plugin.getTrailIntervalTicks());
        }

        // Different messages for first and second dash
        if (secondDash) {
            player.sendMessage(messages.getSecondDashMessage());
        } else {
            player.sendMessage(messages.getDashUsedMessage());
        }
    }

    // ===== Helper methods for placeholders =====

    public long getRemainingCooldownSeconds(Player player) {
        UUID uuid = player.getUniqueId();
        long last = lastDash.getOrDefault(uuid, 0L);
        long cd = plugin.getDashCooldownMs();
        long now = System.currentTimeMillis();

        long diff = now - last;
        if (diff >= cd) {
            return 0L;
        }
        return (cd - diff) / 1000L;
    }

    public boolean isInDoubleDashWindow(Player player) {
        UUID uuid = player.getUniqueId();
        Long windowEnd = comboWindowEnd.get(uuid);
        if (windowEnd == null) {
            return false;
        }
        return System.currentTimeMillis() <= windowEnd;
    }
}
