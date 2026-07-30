package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePackHandlerTest {

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
    void acceptsDirectHttpsUrlsOnly() {
        assertTrue(ResourcePackHandler.isValidHttpsUrl("https://cdn.example.com/resource-pack.zip"));
        //noinspection HttpUrlsUsage
        assertFalse(ResourcePackHandler.isValidHttpsUrl("http://cdn.example.com/resource-pack.zip"));
        assertFalse(ResourcePackHandler.isValidHttpsUrl("not a url"));
        assertFalse(ResourcePackHandler.isValidHttpsUrl(""));
    }

    @Test
    void acceptsAndDecodesOnlyACompleteSha1() {
        String sha1 = "0123456789abcdef0123456789abcdef01234567";

        assertTrue(ResourcePackHandler.isValidSha1(sha1));
        assertFalse(ResourcePackHandler.isValidSha1("0123456789abcdef"));
        assertFalse(ResourcePackHandler.isValidSha1("not-a-sha1"));
        assertFalse(ResourcePackHandler.isValidSha1(""));
        assertArrayEquals(new byte[]{
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
                0x01, 0x23, 0x45, 0x67
        }, ResourcePackHandler.decodeSha1(sha1));
    }

    @Test
    void doesNotSendADisabledOrInvalidlyConfiguredPack() {
        PlayerMock player = MockBukkit.getMock().addPlayer();

        assertEquals(ResourcePackHandler.SendResult.DISABLED, plugin.getResourcePackHandler().sendTo(player));

        plugin.getConfig().set("resource-pack.enabled", true);
        plugin.getConfig().set("resource-pack.sha1", "not-a-sha1");
        plugin.loadSettings();

        assertEquals(ResourcePackHandler.SendResult.INVALID_CONFIGURATION,
                plugin.getResourcePackHandler().sendTo(player));
    }

    @Test
    void treatsDeclinesAndDownloadFailuresAsFailureStatuses() {
        assertTrue(ResourcePackHandler.isFailureStatus(PlayerResourcePackStatusEvent.Status.DECLINED));
        assertTrue(ResourcePackHandler.isFailureStatus(PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD));
        assertFalse(ResourcePackHandler.isFailureStatus(PlayerResourcePackStatusEvent.Status.ACCEPTED));
        assertFalse(ResourcePackHandler.isFailureStatus(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED));
    }
}
