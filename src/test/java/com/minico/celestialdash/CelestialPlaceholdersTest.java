package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CelestialPlaceholdersTest {

    private CelestialDash plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void cachesTearCountsUntilTheShortCacheIntervalExpires() {
        AtomicLong clock = new AtomicLong();
        CelestialPlaceholders placeholders = new CelestialPlaceholders(plugin, clock::get, 100L, 8);
        PlayerMock player = addPlayer();
        player.getInventory().setItem(0, TearUtils.createCelestialTear(3));

        assertEquals("3", placeholders.onPlaceholderRequest(player, "tears"));

        player.getInventory().setItem(0, TearUtils.createCelestialTear(1));
        assertEquals("3", placeholders.onPlaceholderRequest(player, "tears"));

        clock.addAndGet(100L);
        assertEquals("1", placeholders.onPlaceholderRequest(player, "tears"));
    }

    @Test
    void boundsTheCacheAndPurgesExpiredEntriesWhenItReachesCapacity() {
        AtomicLong clock = new AtomicLong();
        CelestialPlaceholders placeholders = new CelestialPlaceholders(plugin, clock::get, 100L, 2);

        PlayerMock first = playerWithTears(1);
        PlayerMock second = playerWithTears(2);
        assertEquals("1", placeholders.onPlaceholderRequest(first, "tears"));
        assertEquals("2", placeholders.onPlaceholderRequest(second, "tears"));
        assertEquals(2, placeholders.tearCacheSize());

        clock.addAndGet(100L);
        PlayerMock third = playerWithTears(3);
        assertEquals("3", placeholders.onPlaceholderRequest(third, "tears"));

        assertEquals(1, placeholders.tearCacheSize());
    }

    private PlayerMock playerWithTears(int amount) {
        PlayerMock player = addPlayer();
        player.getInventory().setItem(0, TearUtils.createCelestialTear(amount));
        return player;
    }

    private PlayerMock addPlayer() {
        return Objects.requireNonNull(
                MockBukkit.getMock(),
                "MockBukkit server must be initialized before adding a player"
        ).addPlayer();
    }
}
