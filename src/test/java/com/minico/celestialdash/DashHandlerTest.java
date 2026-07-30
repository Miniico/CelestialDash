package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashHandlerTest {

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
    void reportsOneSecondWhileAnyCooldownFractionRemains() {
        assertEquals(1L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 1_001L, 1_000L));
        assertEquals(1L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 1_999L, 1_000L));
    }

    @Test
    void reportsZeroWhenCooldownHasExpiredOrIsDisabled() {
        assertEquals(0L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 2_000L, 1_000L));
        assertEquals(0L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 1_001L, 0L));
    }

    @Test
    void consumesTheTearInTheMainHandInsteadOfAnotherInventoryStack() {
        plugin.getConfig().set("dash-cooldown-seconds", 0);
        plugin.getConfig().set("dash-particle-enabled", false);
        plugin.getConfig().set("dash-sound-enabled", false);
        plugin.getConfig().set("trail-enabled", false);
        plugin.getConfig().set("regen-duration-seconds", 0);
        plugin.loadSettings();

        PlayerMock player = MockBukkit.getMock().addPlayer();
        player.addAttachment(plugin, "celestialdash.use", true);
        player.getInventory().setHeldItemSlot(4);

        ItemStack firstInventoryTear = TearUtils.createCelestialTear(2);
        ItemStack mainHandTear = TearUtils.createCelestialTear(3);
        player.getInventory().setItem(0, firstInventoryTear);
        player.getInventory().setItem(4, mainHandTear);

        plugin.getDashHandler().onPlayerUseTear(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_AIR,
                mainHandTear,
                null,
                BlockFace.SELF,
                EquipmentSlot.HAND
        ));

        assertEquals(2, player.getInventory().getItem(0).getAmount());
        assertEquals(2, player.getInventory().getItem(4).getAmount());
    }

    @Test
    void honorsTheDashWorldBlacklistUnlessThePlayerHasTheBypassPermission() {
        PlayerMock player = MockBukkit.getMock().addPlayer();
        player.addAttachment(plugin, "celestialdash.use", true);
        player.getInventory().setHeldItemSlot(0);

        plugin.getConfig().set("dash-cooldown-seconds", 0);
        plugin.getConfig().set("dash-blacklist-worlds", List.of(player.getWorld().getName()));
        plugin.getConfig().set("dash-particle-enabled", false);
        plugin.getConfig().set("dash-sound-enabled", false);
        plugin.getConfig().set("trail-enabled", false);
        plugin.getConfig().set("regen-duration-seconds", 0);
        plugin.loadSettings();

        ItemStack tear = TearUtils.createCelestialTear(2);
        player.getInventory().setItemInMainHand(tear);

        PlayerInteractEvent interaction = new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_AIR,
                tear,
                null,
                BlockFace.SELF,
                EquipmentSlot.HAND
        );
        plugin.getDashHandler().onPlayerUseTear(interaction);
        assertEquals(2, player.getInventory().getItemInMainHand().getAmount());

        player.addAttachment(plugin, "celestialdash.bypass-dash-blacklist", true);
        plugin.getDashHandler().onPlayerUseTear(interaction);
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }
}
