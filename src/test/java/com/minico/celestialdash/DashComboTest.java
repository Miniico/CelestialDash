package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class DashComboTest {
    private ServerMock server;
    private CelestialDash plugin;
    private DashHandler handler;
    private PlayerMock player;
    private final AtomicLong now = new AtomicLong(1_000_000L);

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        plugin.getConfig().set("dash-cooldown-seconds", 10);
        plugin.getConfig().set("double-dash.enabled", true);
        plugin.getConfig().set("double-dash.window-ms", 4000);
        plugin.getConfig().set("double-dash.fall-immunity-ticks", 40);
        plugin.getConfig().set("dash-particle-enabled", false);
        plugin.getConfig().set("dash-sound-enabled", false);
        plugin.getConfig().set("trail-enabled", false);
        plugin.getConfig().set("regen-duration-seconds", 0);
        plugin.loadSettings();
        handler = new DashHandler(plugin, plugin.getMessages(), now::get);
        server.getPluginManager().registerEvents(handler, plugin);
        player = server.addPlayer();
        player.getInventory().setItemInMainHand(TearUtils.createCelestialTear(8));
        player.addAttachment(plugin, "celestialdash.use", true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void thirdClickIsBlockedUntilCooldownFromSecondDashExpires() {
        click();
        now.addAndGet(1000);
        click();
        assertEquals(6, tears());
        assertFalse(handler.isInDoubleDashWindow(player));
        assertEquals(Event.Result.DEFAULT, click().useItemInHand());
        assertEquals(6, tears());
        now.addAndGet(9999);
        click();
        assertEquals(6, tears());
        now.incrementAndGet();
        click();
        assertEquals(5, tears());
    }

    @Test
    void secondDashIsAllowedAtWindowBoundary() {
        click();
        now.addAndGet(4000);
        assertTrue(handler.isInDoubleDashWindow(player));
        click();
        assertEquals(6, tears());
    }

    @Test
    void expiredWindowDoesNotBypassCooldownOrConsumeTears() {
        click();
        now.addAndGet(4001);
        assertEquals(Event.Result.DEFAULT, click().useItemInHand());
        assertFalse(handler.isInDoubleDashWindow(player));
        assertEquals(7, tears());
    }

    @Test
    void firstDashDoesNotGrantImmunityAndSecondProtectsOnlyOneFall() {
        click();
        assertFalse(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
        click();
        assertFalse(damage(EntityDamageEvent.DamageCause.FIRE).isCancelled());
        assertTrue(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
        assertFalse(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
    }

    @Test
    void immunityIncludesItsBoundaryButExpiresAfterwards() {
        click();
        click();
        now.addAndGet(2000);
        assertTrue(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
        now.addAndGet(10000);
        click();
        click();
        now.addAndGet(2001);
        assertFalse(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
    }

    @Test
    void zeroImmunityDurationDoesNotProtectFalls() {
        plugin.getConfig().set("double-dash.fall-immunity-ticks", 0);
        plugin.loadSettings();
        click();
        click();
        assertFalse(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
    }

    @ParameterizedTest
    @ValueSource(strings = {"reconnect", "death", "teleport", "world"})
    void contextChangeClearsComboAndRetainsCooldown(String change) {
        click();
        changeContext(change);
        assertFalse(handler.isInDoubleDashWindow(player));
        assertEquals(10, handler.getRemainingCooldownSeconds(player));
        click();
        assertEquals(7, tears());
    }

    @ParameterizedTest
    @ValueSource(strings = {"reconnect", "death", "teleport", "world"})
    void contextChangeClearsImmunityAndRetainsCooldown(String change) {
        click();
        click();
        changeContext(change);
        assertFalse(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
        assertEquals(10, handler.getRemainingCooldownSeconds(player));
    }

    @Test
    void cancelledTeleportAndRotationOnlyTeleportPreserveComboAndImmunity() {
        click();
        teleport(true, false);
        teleport(false, true);
        assertTrue(handler.isInDoubleDashWindow(player));
        click();
        teleport(true, false);
        teleport(false, true);
        assertTrue(damage(EntityDamageEvent.DamageCause.FALL).isCancelled());
    }

    private void changeContext(String change) {
        switch (change) {
            case "reconnect" -> {
                assertTrue(player.disconnect());
                assertTrue(player.reconnect());
            }
            case "death" -> server.getPluginManager().callEvent(
                    new PlayerDeathEvent(player, new ArrayList<>(), 0, "test"));
            case "teleport" -> teleport(false, false);
            case "world" -> assertTrue(player.teleport(server.addSimpleWorld("other").getSpawnLocation()));
            default -> fail("Unknown change");
        }
    }

    private void teleport(boolean cancelled, boolean rotationOnly) {
        Location from = player.getLocation();
        Location to = from.clone();
        if (rotationOnly) to.setYaw(to.getYaw() + 90); else to.add(10, 0, 0);
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);
    }

    private PlayerInteractEvent click() {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
                player.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND);
        handler.onPlayerUseTear(event);
        return event;
    }

    private EntityDamageEvent damage(EntityDamageEvent.DamageCause cause) {
        EntityDamageEvent event = new EntityDamageEvent(player, cause, 4);
        handler.onFallDamage(event);
        return event;
    }

    private int tears() {
        return TearUtils.countTears(player);
    }
}
