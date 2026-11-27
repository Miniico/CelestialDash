package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("deprecation") // Verifies the legacy lore supported by the plugin.
class AmuletChargeCapacityTest {

    private CelestialDash plugin;
    private NamespacedKey capacityKey;
    private NamespacedKey usesKey;
    private NamespacedKey instanceKey;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        capacityKey = new NamespacedKey(plugin, "celestial_amulet_max_uses");
        usesKey = new NamespacedKey(plugin, "celestial_amulet_uses");
        instanceKey = new NamespacedKey(plugin, "celestial_amulet_instance");
        configureCapacity(5);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void craftingPersistsCapacityAndShowsFullCharges() {
        ItemStack amulet = CelestialAmulet.create();

        assertEquals(5, CelestialAmulet.getMaxUses(amulet));
        assertEquals(5, amulet.getItemMeta().getPersistentDataContainer()
                .get(capacityKey, PersistentDataType.INTEGER));
        assertEquals(5, CelestialAmulet.getRemainingUses(amulet));
        assertChargeLore(amulet, "Uses: 5/5");
    }

    @Test
    void configurationChangesAffectOnlyNewAmuletsAndPreserveIdentity() {
        ItemStack original = CelestialAmulet.create();
        String identity = original.getItemMeta().getPersistentDataContainer()
                .get(instanceKey, PersistentDataType.STRING);
        assertNotNull(identity);
        assertEquals(4, CelestialAmulet.consumeUse(original));

        configureCapacity(2);
        assertEquals(4, CelestialAmulet.getRemainingUses(original));
        assertEquals(5, CelestialAmulet.getMaxUses(original));
        assertEquals(2, CelestialAmulet.getMaxUses(CelestialAmulet.create()));
        assertEquals(3, CelestialAmulet.consumeUse(original));
        assertChargeLore(original, "Uses: 3/5");

        configureCapacity(9);
        assertEquals(5, CelestialAmulet.getMaxUses(original));
        assertEquals(2, CelestialAmulet.consumeUse(original));
        assertChargeLore(original, "Uses: 2/5");
        assertEquals(identity, original.getItemMeta().getPersistentDataContainer()
                .get(instanceKey, PersistentDataType.STRING));
    }

    @Test
    void legacyColoredLoreRecoversAndPersistsItsOriginalDenominator() {
        ItemStack amulet = legacyAmulet(2, List.of(ChatColor.GRAY + "Purifies harmful effects",
                ChatColor.DARK_AQUA + "Uses: " + ChatColor.AQUA + "2/7"));
        configureCapacity(3);

        assertEquals(7, CelestialAmulet.getMaxUses(amulet));
        assertEquals(1, CelestialAmulet.consumeUse(amulet));
        assertEquals(7, amulet.getItemMeta().getPersistentDataContainer()
                .get(capacityKey, PersistentDataType.INTEGER));
        assertChargeLore(amulet, "Uses: 1/7");

        configureCapacity(10);
        assertEquals(7, CelestialAmulet.getMaxUses(amulet));
    }

    @Test
    void missingOrInvalidLegacyLoreUsesACoherentCapacityWithoutLosingCharges() {
        configureCapacity(3);
        ItemStack noLore = legacyAmulet(6, null);
        assertEquals(6, CelestialAmulet.getMaxUses(noLore));
        assertEquals(5, CelestialAmulet.consumeUse(noLore));
        assertChargeLore(noLore, "Uses: 5/6");
        configureCapacity(10);
        assertEquals(6, CelestialAmulet.getMaxUses(noLore));
        configureCapacity(3);

        for (String invalid : List.of("Uses: 2/0", "Uses: 2/-1", "Uses: 9/4",
                "Uses: 2/999999999999999999999", "unrelated lore", "Uses: unknown/5")) {
            ItemStack amulet = legacyAmulet(2, List.of(invalid));
            assertEquals(3, CelestialAmulet.getMaxUses(amulet), invalid);
            assertEquals(1, CelestialAmulet.consumeUse(amulet), invalid);
            assertChargeLore(amulet, "Uses: 1/3");
        }
    }

    @Test
    void invalidPersistedCapacityCanRecoverFromLegacyLore() {
        for (int invalid : List.of(-1, 0, 2)) {
            ItemStack amulet = legacyAmulet(4, List.of("Uses: 4/8"));
            ItemMeta meta = amulet.getItemMeta();
            meta.getPersistentDataContainer().set(capacityKey, PersistentDataType.INTEGER, invalid);
            amulet.setItemMeta(meta);

            assertEquals(8, CelestialAmulet.getMaxUses(amulet));
            assertEquals(3, CelestialAmulet.consumeUse(amulet));
            assertChargeLore(amulet, "Uses: 3/8");
        }
    }

    @Test
    void persistedCapacityTakesPrecedenceOverEditedLore() {
        ItemStack amulet = CelestialAmulet.create();
        ItemMeta meta = amulet.getItemMeta();
        meta.setLore(List.of("Uses: 5/99"));
        amulet.setItemMeta(meta);

        assertEquals(5, CelestialAmulet.getMaxUses(amulet));
        assertEquals(4, CelestialAmulet.consumeUse(amulet));
        assertChargeLore(amulet, "Uses: 4/5");
    }

    @Test
    void lastChargeIsPersistedAsZeroAndCannotBeConsumedAgain() {
        configureCapacity(1);
        ItemStack amulet = CelestialAmulet.create();

        assertEquals(0, CelestialAmulet.consumeUse(amulet));
        assertEquals(0, CelestialAmulet.getRemainingUses(amulet));
        assertEquals(0, amulet.getItemMeta().getPersistentDataContainer()
                .get(usesKey, PersistentDataType.INTEGER));
        assertEquals(1, CelestialAmulet.getMaxUses(amulet));
        assertChargeLore(amulet, "Uses: 0/1");
        assertEquals(0, CelestialAmulet.consumeUse(amulet));
        assertChargeLore(amulet, "Uses: 0/1");
    }

    @Test
    void ordinaryItemsHaveNoCapacity() {
        assertEquals(0, CelestialAmulet.getMaxUses(null));
        assertEquals(0, CelestialAmulet.getMaxUses(new ItemStack(Material.NAUTILUS_SHELL)));
    }

    private void configureCapacity(int capacity) {
        plugin.getConfig().set("celestial-amulet.uses", capacity);
        plugin.loadSettings();
        plugin.refreshAmuletRecipe();
    }

    private ItemStack legacyAmulet(int remaining, List<String> lore) {
        ItemStack amulet = CelestialAmulet.create();
        ItemMeta meta = amulet.getItemMeta();
        meta.getPersistentDataContainer().remove(capacityKey);
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, remaining);
        meta.setLore(lore);
        amulet.setItemMeta(meta);
        return amulet;
    }

    private void assertChargeLore(ItemStack amulet, String expected) {
        assertEquals(expected, ChatColor.stripColor(amulet.getItemMeta().getLore().get(2)));
    }
}
