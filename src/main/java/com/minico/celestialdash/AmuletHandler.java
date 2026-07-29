package com.minico.celestialdash;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Keyed;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AmuletHandler implements Listener {

    private static final Set<PotionEffectType> PURIFIABLE_EFFECTS = Set.of(
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.SLOW,
            PotionEffectType.WEAKNESS,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.DARKNESS,
            PotionEffectType.UNLUCK,
            PotionEffectType.BAD_OMEN
    );

    private final CelestialDash plugin;
    private final Messages messages;
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public AmuletHandler(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof Keyed keyed)
                || !CelestialAmulet.getRecipeKey().equals(keyed.getKey())) {
            return;
        }

        // Give every crafting result its own persistent identity, so amulets do not stack.
        event.getInventory().setResult(CelestialAmulet.create());
    }

    @EventHandler
    public void onUseAmulet(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!CelestialAmulet.isCelestialAmulet(item)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!plugin.isCelestialAmuletEnabled()) {
            sendActionBar(player, messages.getAmuletDisabledMessage());
            return;
        }
        if (!player.hasPermission("celestialdash.amulet")) {
            sendActionBar(player, messages.getNoAmuletPermissionMessage());
            return;
        }

        long now = System.currentTimeMillis();
        long cooldown = plugin.getCelestialAmuletCooldownMs();
        long last = lastUse.getOrDefault(player.getUniqueId(), 0L);
        long elapsed = now - last;
        if (elapsed < cooldown) {
            long remaining = (long) Math.ceil((cooldown - elapsed) / 1000.0);
            sendActionBar(player, messages.formatAmuletCooldown(remaining));
            return;
        }

        if (!purify(player)) {
            sendActionBar(player, messages.getAmuletNoEffectsMessage());
            return;
        }

        int remainingUses = CelestialAmulet.consumeUse(item);
        if (remainingUses == 0) {
            player.getInventory().setItemInMainHand(null);
            player.sendMessage(messages.getAmuletDepletedMessage());
        } else {
            player.sendMessage(messages.formatAmuletUsed(remainingUses));
        }

        lastUse.put(player.getUniqueId(), now);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 24, 0.35, 0.6, 0.35, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.4f);
    }

    public void stop() {
        lastUse.clear();
    }

    private boolean purify(Player player) {
        boolean purified = false;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (PURIFIABLE_EFFECTS.contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
                purified = true;
            }
        }

        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
            purified = true;
        }
        return purified;
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
