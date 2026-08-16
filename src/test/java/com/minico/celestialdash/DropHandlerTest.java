package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropHandlerTest {

    private CelestialDash plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        plugin.getConfig().set("drop-chance-per-second", 1.0);
        plugin.getConfig().set("drop-cooldown-seconds", 60);
        plugin.getConfig().set("drop-delivery", "INVENTORY");
        plugin.loadSettings();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void keepsTheStormDropCooldownAfterReconnect() {
        PlayerMock player = eligiblePlayer();
        player.getWorld().setStorm(true);

        server().getScheduler().performTicks(20L);
        assertEquals(1, TearUtils.countTears(player));

        assertTrue(player.disconnect());
        assertTrue(player.reconnect());
        server().getScheduler().performTicks(20L);

        assertEquals(1, TearUtils.countTears(player));
    }

    @Test
    void removesExpiredStormDropCooldowns() {
        PlayerMock player = eligiblePlayer();
        player.getWorld().setStorm(true);

        server().getScheduler().performTicks(20L);
        assertEquals(1, TearUtils.countTears(player));

        plugin.getDropHandler().cleanupExpiredCooldowns(System.currentTimeMillis() + 60_000L);
        server().getScheduler().performTicks(20L);

        assertEquals(2, TearUtils.countTears(player));
    }

    private PlayerMock eligiblePlayer() {
        PlayerMock player = server().addPlayer();
        player.addAttachment(plugin, "celestialdash.receive", true);
        return player;
    }

    private ServerMock server() {
        return Objects.requireNonNull(
                MockBukkit.getMock(),
                "MockBukkit server must be initialized before using the mock server"
        );
    }
}
