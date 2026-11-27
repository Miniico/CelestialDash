package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class DashActionBarHandlerTest {
    private ServerMock server;
    private CelestialDash plugin;
    private DashHandler dash;
    private DashActionBarHandler hud;
    private PlayerMock player;
    private PermissionAttachment permissions;
    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final List<Packet> packets = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        plugin.getConfig().set("dash-cooldown-seconds", 10);
        plugin.getConfig().set("double-dash.enabled", true);
        plugin.getConfig().set("double-dash.window-ms", 4000);
        plugin.getConfig().set("dash-particle-enabled", false);
        plugin.getConfig().set("dash-sound-enabled", false);
        plugin.getConfig().set("trail-enabled", false);
        plugin.getConfig().set("regen-duration-seconds", 0);
        plugin.getConfig().set("dash-action-bar.enabled", true);
        plugin.getConfig().set("dash-action-bar.only-while-holding-tear", true);
        plugin.getConfig().set("dash-action-bar.update-interval-ticks", 5);
        plugin.getConfig().set("dash-action-bar.ready", "&aReady");
        plugin.getConfig().set("dash-action-bar.cooldown", "&eCooldown %seconds%s");
        plugin.getConfig().set("dash-action-bar.second-dash", "&bSecond %seconds%s");
        plugin.loadSettings();
        dash = new DashHandler(plugin, plugin.getMessages(), now::get);
        hud = new DashActionBarHandler(plugin, dash, now::get,
                (recipient, text) -> packets.add(new Packet(recipient, text)));
        server.getPluginManager().registerEvents(dash, plugin);
        server.getPluginManager().registerEvents(hud, plugin);
        player = server.addPlayer();
        player.getInventory().setItemInMainHand(TearUtils.createCelestialTear(8));
        permissions = player.addAttachment(plugin, "celestialdash.use", true);
    }

    @AfterEach
    void tearDown() {
        if (hud != null) hud.stop();
        if (dash != null) dash.stop();
        MockBukkit.unmock();
    }

    @Test
    void followsReadySecondDashCooldownAndReadyTransitions() {
        hud.update(player);
        assertLast("&aReady");

        click();
        hud.update(player);
        assertLast("&bSecond 4.0s");
        assertEquals(10, dash.getRemainingCooldownSeconds(player),
                "The second-dash window takes priority over an active cooldown");

        now.addAndGet(1000);
        click();
        hud.update(player);
        assertLast("&eCooldown 10s");
        assertEquals(6, TearUtils.countTears(player));

        now.addAndGet(9001);
        hud.update(player);
        assertLast("&eCooldown 1s");
        now.addAndGet(999);
        hud.update(player);
        assertLast("&aReady");
    }

    @Test
    void windowUsesCeilingTenthsAndInclusiveBoundaryRegardlessOfLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            click();
            now.addAndGet(1);
            hud.update(player);
            assertLast("&bSecond 4.0s");
            now.addAndGet(100);
            hud.update(player);
            assertLast("&bSecond 3.9s");
            now.addAndGet(3899);
            assertTrue(dash.isInDoubleDashWindow(player));
            assertEquals(0, dash.getRemainingDoubleDashWindowMillis(player));
            hud.update(player);
            assertLast("&bSecond 0.0s");
            now.incrementAndGet();
            hud.update(player);
            assertLast("&eCooldown 6s");
            assertFalse(dash.isInDoubleDashWindow(player));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void expiringAnUnusedWindowKeepsCooldownFromFirstDash() {
        click();
        now.addAndGet(4001);
        hud.update(player);
        assertLast("&eCooldown 6s");
        now.addAndGet(5999);
        hud.update(player);
        assertLast("&aReady");
        assertEquals(7, TearUtils.countTears(player));
    }

    @Test
    void disablingDoubleDashImmediatelySelectsCooldownInsteadOfStoredWindow() {
        click();
        hud.update(player);
        assertLast("&bSecond 4.0s");
        plugin.getConfig().set("double-dash.enabled", false);
        plugin.loadSettings();
        hud.update(player);
        assertLast("&eCooldown 10s");
    }

    @Test
    void holdingRestrictionCanBeReloadedWithoutLosingInventoryRequirement() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));
        player.getInventory().setItem(9, TearUtils.createCelestialTear(2));
        hud.update(player);
        assertTrue(packets.isEmpty(), "An unseen HUD must not send an empty action bar");

        plugin.getConfig().set("dash-action-bar.only-while-holding-tear", false);
        plugin.loadSettings();
        hud.update(player);
        assertLast("&aReady");

        player.getInventory().setItem(9, null);
        hud.update(player);
        assertLast("");
        int count = packets.size();
        hud.update(player);
        assertEquals(count, packets.size(), "A hidden HUD is cleared only once");
    }

    @Test
    void offHandTearDoesNotMeetDefaultHoldingRestriction() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));
        player.getInventory().setItemInOffHand(TearUtils.createCelestialTear(1));
        hud.update(player);
        assertTrue(packets.isEmpty());
        plugin.getConfig().set("dash-action-bar.only-while-holding-tear", false);
        plugin.loadSettings();
        hud.update(player);
        assertLast("&aReady");
    }

    @Test
    void losingPermissionClearsOnceAndRegainingItShowsHudAgain() {
        hud.update(player);
        permissions.setPermission("celestialdash.use", false);
        hud.update(player);
        assertLast("");
        int count = packets.size();
        hud.update(player);
        assertEquals(count, packets.size());
        permissions.setPermission("celestialdash.use", true);
        hud.update(player);
        assertLast("&aReady");
    }

    @Test
    void blacklistedWorldRequiresItsSpecificBypassPermission() {
        plugin.getConfig().set("dash-blacklist-worlds", List.of(player.getWorld().getName().toUpperCase(Locale.ROOT)));
        plugin.loadSettings();
        hud.update(player);
        assertTrue(packets.isEmpty());
        permissions.setPermission("celestialdash.bypass-dash-blacklist", true);
        hud.update(player);
        assertLast("&aReady");
        permissions.setPermission("celestialdash.bypass-dash-blacklist", false);
        hud.update(player);
        assertLast("");
    }

    @Test
    void spectatorAndDeadPlayersDoNotRetainHud() {
        hud.update(player);
        player.setGameMode(GameMode.SPECTATOR);
        hud.update(player);
        assertLast("");
        player.setGameMode(GameMode.SURVIVAL);
        hud.update(player);
        assertLast("&aReady");
        player.setHealth(0);
        assertTrue(player.isDead());
        hud.update(player);
        assertLast("");
        int count = packets.size();
        hud.update(player);
        assertEquals(count, packets.size());
    }

    @Test
    void pauseProtectsFeedbackForTwoSecondsAndReleasesOwnershipWithoutBlank() {
        hud.update(player);
        hud.showFeedback(player, color("&cDash blocked"));
        assertEquals(2, packets.size(), "Feedback must replace the HUD without an intervening blank");
        assertLast("&cDash blocked");
        player.getInventory().clear();
        hud.update(player);
        now.addAndGet(1999);
        hud.update(player);
        assertEquals(2, packets.size());
        now.incrementAndGet();
        hud.update(player);
        assertEquals(2, packets.size(), "A paused HUD no longer owns the error bar");

        player.getInventory().setItemInMainHand(TearUtils.createCelestialTear(2));
        hud.update(player);
        assertLast("&aReady");
    }

    @Test
    void pauseExpiresAtTwoSecondsAndStopDoesNotEraseItsFeedback() {
        hud.update(player);
        hud.pause(player);
        now.addAndGet(1999);
        hud.update(player);
        assertEquals(1, packets.size());
        now.incrementAndGet();
        hud.update(player);
        assertEquals(2, packets.size());
        assertLast("&aReady");
        hud.pause(player);
        hud.stop();
        assertEquals(2, packets.size());
    }

    @Test
    void quittingDropsPauseAndHudOwnershipBeforeReconnect() {
        hud.update(player);
        hud.pause(player);
        assertTrue(player.disconnect());
        hud.update(player);
        assertEquals(1, packets.size(), "An offline player must receive no HUD packets");
        assertTrue(player.reconnect());
        hud.update(player);
        assertLast("&aReady");
        assertEquals(2, packets.size(), "The old connection's pause must not affect a new connection");

        assertTrue(player.disconnect());
        hud.stop();
        assertEquals(2, packets.size(), "Quitting relinquishes ownership without sending a blank");
    }

    @Test
    void reloadDisablesTimerClearsOwnedBarOnceAndAllowsReenable() {
        hud.start();
        server.getScheduler().performTicks(1);
        assertLast("&aReady");
        plugin.getConfig().set("dash-action-bar.enabled", false);
        plugin.loadSettings();
        hud.reload();
        assertLast("");
        int count = packets.size();
        hud.reload();
        hud.update(player);
        server.getScheduler().performTicks(20);
        assertEquals(count, packets.size());

        plugin.getConfig().set("dash-action-bar.enabled", true);
        plugin.getConfig().set("dash-action-bar.ready", "&dReloaded");
        plugin.loadSettings();
        hud.reload();
        server.getScheduler().performTicks(1);
        assertLast("&dReloaded");
    }

    @Test
    void reloadChangesIntervalWithoutLeavingOldRepeatingTask() {
        hud.start();
        server.getScheduler().performTicks(1);
        assertEquals(1, packets.size());
        plugin.getConfig().set("dash-action-bar.update-interval-ticks", 2);
        plugin.loadSettings();
        hud.reload();
        assertLast("");
        packets.clear();
        server.getScheduler().performTicks(6);
        assertEquals(3, packets.size(), "Only the replacement task should run, every two ticks");
        assertTrue(packets.stream().allMatch(packet -> packet.text().equals(color("&aReady"))));
    }

    @Test
    void startIsIdempotentAndStopCancelsTimerAndClearsOnlyOnce() {
        hud.start();
        hud.start();
        server.getScheduler().performTicks(6);
        assertEquals(2, packets.size(), "Repeated start must leave only one active HUD task");
        hud.stop();
        assertLast("");
        int count = packets.size();
        hud.stop();
        server.getScheduler().performTicks(20);
        assertEquals(count, packets.size());
    }

    private void click() {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
                player.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND);
        dash.onPlayerUseTear(event);
        assertTrue(event.isCancelled(), "The real dash handler must accept the test interaction");
    }

    private void assertLast(String message) {
        assertFalse(packets.isEmpty());
        Packet last = packets.get(packets.size() - 1);
        assertSame(player, last.player());
        assertEquals(color(message), last.text());
    }

    private static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    private record Packet(Player player, String text) {
    }
}
