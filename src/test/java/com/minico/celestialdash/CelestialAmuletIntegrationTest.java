package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CelestialAmuletIntegrationTest {

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
    void registersTheConfiguredAmuletRecipe() {
        assertNotNull(Bukkit.getRecipe(CelestialAmulet.getRecipeKey()));

        plugin.getConfig().set("celestial-amulet.uses", 5);
        plugin.loadSettings();
        plugin.refreshAmuletRecipe();

        ItemStack amulet = CelestialAmulet.create();
        assertTrue(CelestialAmulet.isCelestialAmulet(amulet));
        assertEquals(5, CelestialAmulet.getRemainingUses(amulet));
    }

    @Test
    @SuppressWarnings("deprecation") // Mirrors the legacy metadata used by supported server versions.
    void onlyInternallyMarkedNautilusShellsAreAmulets() {
        ItemStack renamedShell = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = renamedShell.getItemMeta();
        meta.setDisplayName("Celestial Amulet");
        renamedShell.setItemMeta(meta);

        assertFalse(CelestialAmulet.isCelestialAmulet(renamedShell));
    }

    @Test
    void consumesChargesUntilTheAmuletIsDepleted() {
        ItemStack amulet = CelestialAmulet.create();
        int initialUses = CelestialAmulet.getRemainingUses(amulet);

        assertEquals(initialUses - 1, CelestialAmulet.consumeUse(amulet));
        assertEquals(initialUses - 1, CelestialAmulet.getRemainingUses(amulet));

        for (int remaining = initialUses - 1; remaining > 0; remaining--) {
            assertEquals(remaining - 1, CelestialAmulet.consumeUse(amulet));
        }
    }

    @Test
    @SuppressWarnings("deprecation") // Mirrors the legacy metadata used by supported server versions.
    void appliesConfiguredCustomModelDataToNewAmulets() {
        plugin.getConfig().set("celestial-amulet.custom-model-data", 22002);
        plugin.loadSettings();
        plugin.refreshAmuletRecipe();

        ItemMeta meta = CelestialAmulet.create().getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasCustomModelData());
        assertEquals(22002, meta.getCustomModelData());
    }

    @Test
    void reloadsAndBoundsTheGiveAmountLimit() {
        plugin.getConfig().set("give-max-amount", 48);
        plugin.loadSettings();
        assertEquals(48, plugin.getSettings().giveMaxAmount());

        plugin.getConfig().set("give-max-amount", 5_000);
        plugin.loadSettings();
        assertEquals(2_304, plugin.getSettings().giveMaxAmount());
    }
}
