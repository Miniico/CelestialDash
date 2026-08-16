package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TearUtilsTest {

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
    void keepsPreviouslyCreatedTearsValidWhenCustomModelDataChanges() {
        TearUtils.initialize(plugin, 22001);
        ItemStack existingTear = TearUtils.createCelestialTear();

        ItemMeta meta = Objects.requireNonNull(existingTear.getItemMeta());
        assertEquals(22001, meta.getCustomModelData());

        TearUtils.initialize(plugin, 22002);
        assertTrue(TearUtils.isCelestialTear(existingTear));

        TearUtils.initialize(plugin, 0);
        assertTrue(TearUtils.isCelestialTear(existingTear));
    }

    @Test
    void appliesCustomModelDataOnlyToNewTears() {
        TearUtils.initialize(plugin, 22002);
        ItemMeta modeledTearMeta = Objects.requireNonNull(TearUtils.createCelestialTear().getItemMeta());
        assertEquals(22002, modeledTearMeta.getCustomModelData());

        TearUtils.initialize(plugin, 0);
        ItemMeta plainTearMeta = Objects.requireNonNull(TearUtils.createCelestialTear().getItemMeta());
        assertFalse(plainTearMeta.hasCustomModelData());
    }

    @Test
    void consumesAnExistingTearAfterCustomModelDataChanges() {
        TearUtils.initialize(plugin, 22001);
        ItemStack existingTear = TearUtils.createCelestialTear();
        PlayerMock player = addPlayer();
        player.getInventory().setItem(0, existingTear);

        TearUtils.initialize(plugin, 22002);

        assertTrue(TearUtils.tryConsumeTear(player, 0));
        assertNull(player.getInventory().getItem(0));
    }

    @Test
    void doesNotRecognizeAnUnmarkedVanillaGhastTear() {
        TearUtils.initialize(plugin, 22001);

        assertFalse(TearUtils.isCelestialTear(new ItemStack(Material.GHAST_TEAR)));
    }

    @Test
    void doesNotRecognizeACustomModeledGhastTearWithoutThePersistentMarker() {
        TearUtils.initialize(plugin, 22001);
        ItemStack imitation = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = Objects.requireNonNull(imitation.getItemMeta());
        meta.setCustomModelData(22001);
        imitation.setItemMeta(meta);

        assertFalse(TearUtils.isCelestialTear(imitation));
    }

    private PlayerMock addPlayer() {
        return Objects.requireNonNull(
                MockBukkit.getMock(),
                "MockBukkit server must be initialized before adding a player"
        ).addPlayer();
    }
}
