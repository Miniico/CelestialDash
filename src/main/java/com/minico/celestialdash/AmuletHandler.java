package com.minico.celestialdash;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AmuletHandler implements Listener {

    private static final int[] AMULET_TEAR_SLOTS = {1, 3, 5, 7};

    private final CelestialDash plugin;
    private final Messages messages;
    private final Map<UUID, Long> lastUse = new HashMap<>();
    private BukkitTask cleanupTask;

    public AmuletHandler(CelestialDash plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    /**
     * Starts periodic cleanup while retaining active cooldowns across reconnects.
     */
    public void start() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }

        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredCooldowns(System.currentTimeMillis());
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof Keyed keyed)
                || !CelestialAmulet.getRecipeKey().equals(keyed.getKey())) {
            return;
        }

        if (!hasValidAmuletTearIngredients(event.getInventory().getMatrix())) {
            event.getInventory().setResult(null);
            return;
        }

        // Give every crafting result its own persistent identity, so amulets do not stack.
        event.getInventory().setResult(CelestialAmulet.create());
    }

    /**
     * Verifies the four Tear ingredients after the material-only recipe has matched.
     *
     * <p>CustomModelData is intentionally not checked here. A Tear's persistent
     * marker is its identity, so an existing Tear remains usable after its visual
     * model configuration changes.</p>
     *
     * @param matrix crafting matrix from the matched Amulet recipe
     * @return whether every Tear position contains a valid Celestial Tear
     */
    static boolean hasValidAmuletTearIngredients(ItemStack[] matrix) {
        if (matrix == null || matrix.length != 9) {
            return false;
        }

        for (int slot : AMULET_TEAR_SLOTS) {
            if (!TearUtils.isCelestialTear(matrix[slot])) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onUseAmulet(PlayerInteractEvent event) {
        if (InteractionUtils.isDeniedByAnotherListener(event)) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (InteractionUtils.isRightClickOnInteractableBlock(event)) {
            return;
        }

        ItemStack item = event.getItem();
        if (!CelestialAmulet.isCelestialAmulet(item)) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.getSettings().amulet().enabled()) {
            sendActionBar(player, messages.getAmuletDisabledMessage());
            return;
        }
        if (!player.hasPermission("celestialdash.amulet")) {
            sendActionBar(player, messages.getNoAmuletPermissionMessage());
            return;
        }

        long now = System.currentTimeMillis();
        long cooldown = plugin.getSettings().amulet().cooldownMs();
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

        // Only consume the Amulet and block the vanilla interaction after it purified something.
        event.setCancelled(true);
        int remainingUses = CelestialAmulet.consumeUse(item);
        if (remainingUses == 0) {
            player.getInventory().setItemInMainHand(null);
            player.sendMessage(messages.getAmuletDepletedMessage());
        } else {
            player.sendMessage(messages.formatAmuletUsed(remainingUses));
        }

        lastUse.put(player.getUniqueId(), now);
        Location location = player.getLocation();
        player.getWorld().spawnParticle(Particle.END_ROD, location.clone().add(0, 1, 0),
                24, 0.35, 0.6, 0.35, 0.02);
        player.getWorld().playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.4f);
    }

    public void stop() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        lastUse.clear();
    }

    /**
     * Removes only cooldowns that have expired. Active cooldowns intentionally
     * remain after a player reconnects.
     *
     * @param nowMs current time in milliseconds
     */
    void cleanupExpiredCooldowns(long nowMs) {
        if (lastUse.isEmpty()) {
            return;
        }

        long cooldownMs = plugin.getSettings().amulet().cooldownMs();
        lastUse.entrySet().removeIf(entry -> nowMs - entry.getValue() >= cooldownMs);
    }

    private boolean purify(Player player) {
        boolean purified = false;
        Set<PotionEffectType> purifiableEffects = plugin.getSettings().amulet().purifiableEffects();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (purifiableEffects.contains(effect.getType())) {
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

    @SuppressWarnings("deprecation") // Required for action-bar support on older target servers.
    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
