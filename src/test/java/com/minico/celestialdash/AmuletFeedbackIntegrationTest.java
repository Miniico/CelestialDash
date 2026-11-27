package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AmuletFeedbackIntegrationTest {
    private CelestialDash plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        plugin.getConfig().set("celestial-amulet.cooldown-seconds", 0);
        plugin.getConfig().set("chronicle.enabled", false);
        plugin.loadSettings();
        player = Objects.requireNonNull(MockBukkit.getMock()).addPlayer();
        player.addAttachment(plugin, "celestialdash.amulet", true);
        player.getInventory().setHeldItemSlot(0);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void successfulUsesShowUpdatedCountsAndKeepCustomizedChatAndApi() {
        plugin.getConfig().set("messages.amulet-used-action-bar", "&bPurified %uses%/%max-uses%");
        plugin.getConfig().set("messages.amulet-used", "&aCustom chat: %uses% left");
        plugin.getMessages().reload();
        ItemStack amulet = CelestialAmulet.create();
        player.getInventory().setItemInMainHand(amulet);

        for (int remaining = 2; remaining >= 1; remaining--) {
            purifyWith(amulet);

            assertEquals(remaining, CelestialAmulet.getRemainingUses(amulet));
            assertEquals(ChatColor.AQUA + "Purified " + remaining + "/3", player.nextMessage());
            assertEquals(ChatColor.GREEN + "Custom chat: " + remaining + " left", player.nextMessage());
            assertNull(player.nextMessage(), "Each use sends only one HUD and one existing chat message");
        }
        assertEquals(ChatColor.GREEN + "Custom chat: 2 left", plugin.getMessages().formatAmuletUsed(2));
    }

    @Test
    void lastChargeShowsOnlyDepletedFeedbackAndRemovesTheAmulet() {
        configureCapacity(1);
        plugin.getConfig().set("messages.amulet-depleted-action-bar", "&cDepleted! %uses%/%max-uses%");
        plugin.getConfig().set("messages.amulet-depleted", "&eCustom farewell");
        plugin.getMessages().reload();
        ItemStack amulet = CelestialAmulet.create();
        player.getInventory().setItemInMainHand(amulet);

        purifyWith(amulet);

        assertEquals(Material.AIR, player.getInventory().getItemInMainHand().getType());
        assertEquals(0, CelestialAmulet.getRemainingUses(amulet));
        assertEquals(ChatColor.RED + "Depleted! 0/1", player.nextMessage());
        assertEquals(ChatColor.YELLOW + "Custom farewell", player.nextMessage());
        assertNull(player.nextMessage(), "The last charge must not also send ordinary success feedback");
        assertEquals(ChatColor.YELLOW + "Custom farewell", plugin.getMessages().getAmuletDepletedMessage());
        assertTrue(amulet.getItemMeta().getLore().stream()
                .anyMatch(line -> ChatColor.stripColor(line).contains("0/1")));
    }

    @Test
    void oldConfigurationGetsChargeAndDepletedFallbacksWithoutBeingModified() {
        // Remove bundled defaults too, so this represents an actual pre-HUD configuration.
        plugin.getConfig().setDefaults(new YamlConfiguration());
        plugin.getConfig().set("messages.amulet-used-action-bar", null);
        plugin.getConfig().set("messages.amulet-depleted-action-bar", null);
        plugin.getConfig().set("messages.amulet-used", "&aLegacy chat %uses%");
        plugin.getConfig().set("messages.amulet-depleted", "&eLegacy farewell");
        plugin.getMessages().reload();
        configureCapacity(2);
        ItemStack amulet = CelestialAmulet.create();
        player.getInventory().setItemInMainHand(amulet);
        String configurationBefore = plugin.getConfig().saveToString();

        purifyWith(amulet);

        assertEquals("Purified! Celestial Amulet: 1/2 charges", ChatColor.stripColor(player.nextMessage()));
        assertEquals(ChatColor.GREEN + "Legacy chat 1", player.nextMessage());
        assertNull(player.nextMessage());

        purifyWith(amulet);

        assertEquals("Celestial Amulet depleted! 0/2 charges", ChatColor.stripColor(player.nextMessage()));
        assertEquals(ChatColor.YELLOW + "Legacy farewell", player.nextMessage());
        assertNull(player.nextMessage());
        assertEquals(configurationBefore, plugin.getConfig().saveToString());
        assertFalse(plugin.getConfig().contains("messages.amulet-used-action-bar"));
        assertFalse(plugin.getConfig().contains("messages.amulet-depleted-action-bar"));
    }

    @Test
    void feedbackUsesTheHeldAmuletsCapacityAfterReloadAndLeavesAnotherAmuletUntouched() {
        ItemStack first = CelestialAmulet.create();
        configureCapacity(5);
        ItemStack second = CelestialAmulet.create();
        assertFalse(first.isSimilar(second));
        player.getInventory().setItemInMainHand(first);
        player.getInventory().setItem(1, second);

        purifyWith(first);

        assertEquals("Purified! Celestial Amulet: 2/3 charges", ChatColor.stripColor(player.nextMessage()));
        assertEquals(plugin.getMessages().formatAmuletUsed(2), player.nextMessage());
        assertNull(player.nextMessage());
        assertEquals(2, CelestialAmulet.getRemainingUses(first));
        assertEquals(3, CelestialAmulet.getMaxUses(first));
        assertEquals(5, CelestialAmulet.getRemainingUses(second));
        assertEquals(second, player.getInventory().getItem(1));

        player.getInventory().setItemInMainHand(second);
        purifyWith(second);

        assertEquals("Purified! Celestial Amulet: 4/5 charges", ChatColor.stripColor(player.nextMessage()));
        assertEquals(plugin.getMessages().formatAmuletUsed(4), player.nextMessage());
        assertNull(player.nextMessage());
        assertEquals(2, CelestialAmulet.getRemainingUses(first));
    }

    @Test
    void failedPurificationKeepsChargesAndDoesNotSendSuccessOrDepletedMessages() {
        ItemStack amulet = CelestialAmulet.create();
        player.getInventory().setItemInMainHand(amulet);
        PlayerInteractEvent interaction = rightClick(amulet);

        plugin.getAmuletHandler().onUseAmulet(interaction);

        assertEquals(Event.Result.DEFAULT, interaction.useItemInHand());
        assertEquals(3, CelestialAmulet.getRemainingUses(amulet));
        assertEquals(plugin.getMessages().getAmuletNoEffectsMessage(), player.nextMessage());
        assertNull(player.nextMessage());
    }

    private void configureCapacity(int uses) {
        plugin.getConfig().set("celestial-amulet.uses", uses);
        plugin.loadSettings();
        plugin.refreshAmuletRecipe();
    }

    private void purifyWith(ItemStack amulet) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        PlayerInteractEvent interaction = rightClick(amulet);
        try {
            plugin.getAmuletHandler().onUseAmulet(interaction);
        } catch (UnimplementedOperationException ignored) {
            // MockBukkit does not implement the cosmetic World#spawnParticle call.
        }
        assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
        assertEquals(Event.Result.DENY, interaction.useItemInHand());
    }

    private PlayerInteractEvent rightClick(ItemStack amulet) {
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, amulet,
                null, BlockFace.SELF, EquipmentSlot.HAND);
    }
}
