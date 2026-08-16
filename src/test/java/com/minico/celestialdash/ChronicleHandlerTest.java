package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChronicleHandlerTest {

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
    void givesEveryPlayerTheChronicleOnceAfterTheUpdate() {
        PlayerMock player = server().addPlayer();
        ItemStack chronicle = findChronicle(player);

        assertNotNull(chronicle);
        assertTrue(player.disconnect());
        assertTrue(player.reconnect());

        assertEquals(1, countChronicles(player));
    }

    @Test
    void doesNotGiveOrMarkTheChronicleWhileAutomaticDeliveryIsDisabled() {
        plugin.getConfig().set("chronicle.enabled", false);
        plugin.loadSettings();
        PlayerMock player = server().addPlayer("ChronicleDisabled");

        assertEquals(0, countChronicles(player));

        plugin.getConfig().set("chronicle.enabled", true);
        plugin.loadSettings();
        assertTrue(player.disconnect());
        assertTrue(player.reconnect());
        assertEquals(1, countChronicles(player));
    }

    @Test
    void adminCanReissueTheChronicleWithoutResettingOneTimeDelivery() {
        PlayerMock player = server().addPlayer("ChronicleReader");

        assertEquals(1, countChronicles(player));
        assertTrue(server().dispatchCommand(
                server().getConsoleSender(),
                "celestialdash chronicle give " + player.getName()
        ));
        assertEquals(2, countChronicles(player));

        assertTrue(player.disconnect());
        assertTrue(player.reconnect());
        assertEquals(2, countChronicles(player));
    }

    @Test
    void playerCanRecoverTheChronicleOnlyAfterTheConfiguredCooldown() {
        PlayerMock player = server().addPlayer("ChronicleRecovery");
        player.addAttachment(plugin, "celestialdash.chronicle", true);

        assertTrue(server().dispatchCommand(player, "celestialdash chronicle"));
        assertEquals(2, countChronicles(player));

        assertTrue(server().dispatchCommand(player, "celestialdash chronicle"));
        assertEquals(2, countChronicles(player));
    }

    @Test
    void doesNotReissueTheChronicleForNonAdminSenders() {
        PlayerMock sender = server().addPlayer("NoChronicleAdmin");
        PlayerMock target = server().addPlayer("ChronicleTarget");
        sender.setOp(false);

        assertFalse(sender.hasPermission("celestialdash.admin"));
        assertEquals(1, countChronicles(target));
        assertTrue(server().dispatchCommand(
                sender,
                "celestialdash chronicle give " + target.getName()
        ));
        assertEquals(1, countChronicles(target));
    }

    @Test
    void createsANonEditableStoryBookWithoutRealEnchantments() {
        PlayerMock player = server().addPlayer();
        ItemStack chronicle = Objects.requireNonNull(findChronicle(player));

        assertEquals(Material.WRITTEN_BOOK, chronicle.getType());
        assertTrue(plugin.getChronicleHandler().isChronicle(chronicle));
        assertFalse(plugin.getChronicleHandler().isChronicle(new ItemStack(Material.WRITTEN_BOOK)));
        assertFalse(chronicle.getItemMeta().hasEnchants());
        BookMeta meta = (BookMeta) chronicle.getItemMeta();
        assertEquals("The Falling Sky", meta.getTitle());
        assertEquals("Dr. Elian Voss", meta.getAuthor());
        assertTrue(meta.getPageCount() > 2);
        assertTrue(allPages(meta).replace('\n', ' ').contains("four Tears around a core of Netherite"));
        assertPagesFit(meta);
    }

    @Test
    void usesSupportedClientLanguagesAndFallsBackToEnglish() {
        assertChronicleLocale("en_us", "The Falling Sky", "When the old");
        assertChronicleLocale("es_mx", "El cielo que cae", "Cuando el viejo");
        assertChronicleLocale("pt_br", "O Céu que Cai", "Quando o velho");
        assertChronicleLocale("it_it", "Il cielo che cade", "Quando il");
        assertChronicleLocale("fr_fr", "Le ciel qui tombe", "Quand le vieil");
        assertChronicleLocale("ru_ru", "Падающее небо", "Когда старая");
        assertChronicleLocale("de_de", "The Falling Sky", "When the old");
        assertEquals("Has recibido El cielo que cae.",
                CelestialChronicle.deliveryMessage("es_mx"));
        assertEquals("You received The Falling Sky.",
                CelestialChronicle.deliveryMessage("de_de"));
    }

    private ItemStack findChronicle(PlayerMock player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.getChronicleHandler().isChronicle(item)) {
                return item;
            }
        }
        return null;
    }

    private int countChronicles(PlayerMock player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.getChronicleHandler().isChronicle(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @SuppressWarnings("deprecation") // Verifies localized String pages for the oldest supported server API.
    private void assertChronicleLocale(String clientLocale, String title, String firstPageStart) {
        ItemStack chronicle = CelestialChronicle.create(clientLocale, new NamespacedKey(plugin, "test_chronicle"));
        BookMeta meta = (BookMeta) chronicle.getItemMeta();

        assertEquals(title, meta.getTitle());
        assertTrue(meta.getPage(1).startsWith(firstPageStart));
        assertPagesFit(meta);
    }

    @SuppressWarnings("deprecation") // Verifies the legacy String pages for the oldest supported server API.
    private String allPages(BookMeta meta) {
        return String.join("\n", meta.getPages());
    }

    @SuppressWarnings("deprecation") // Verifies the conservative layout used by the written book.
    private void assertPagesFit(BookMeta meta) {
        for (String page : meta.getPages()) {
            assertTrue(page.lines().count() <= 11, "A Chronicle page must not exceed eleven lines");
            for (String line : page.split("\\n", -1)) {
                assertTrue(line.length() <= 16, "A Chronicle line must not exceed sixteen characters: " + line);
            }
        }
    }

    private ServerMock server() {
        return Objects.requireNonNull(
                MockBukkit.getMock(),
                "MockBukkit server must be initialized before using the mock server"
        );
    }
}
