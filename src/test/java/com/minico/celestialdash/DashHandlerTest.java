package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

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

        PlayerMock player = addPlayer();
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

        assertEquals(2, Objects.requireNonNull(player.getInventory().getItem(0)).getAmount());
        assertEquals(2, Objects.requireNonNull(player.getInventory().getItem(4)).getAmount());
    }

    @Test
    void honorsTheDashWorldBlacklistUnlessThePlayerHasTheBypassPermission() {
        PlayerMock player = addPlayer();
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

    @Test
    void ignoresAnInteractionCanceledByAnotherPlugin() {
        configureImmediateDash();
        PlayerMock player = eligiblePlayerWithTear();
        PlayerInteractEvent interaction = rightClickAir(player, player.getInventory().getItemInMainHand());
        interaction.setCancelled(true);

        plugin.getDashHandler().onPlayerUseTear(interaction);

        assertEquals(Event.Result.DENY, interaction.useItemInHand());
        assertEquals(2, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void cancelsTheInteractionAfterASuccessfulDash() {
        configureImmediateDash();
        PlayerMock player = eligiblePlayerWithTear();
        PlayerInteractEvent interaction = rightClickBlock(player, player.getInventory().getItemInMainHand(), Material.STONE);

        plugin.getDashHandler().onPlayerUseTear(interaction);

        assertEquals(Event.Result.DENY, interaction.useItemInHand());
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void doesNotConsumeOrCancelWhenUsingVanillaInteractableBlocks() {
        configureImmediateDash();
        PlayerMock player = eligiblePlayerWithTear();

        for (Material material : List.of(Material.CRAFTING_TABLE, Material.CHEST)) {
            player.getInventory().setItemInMainHand(TearUtils.createCelestialTear(2));
            PlayerInteractEvent interaction = rightClickBlock(player, player.getInventory().getItemInMainHand(), material);

            plugin.getDashHandler().onPlayerUseTear(interaction);

            assertEquals(Event.Result.ALLOW, interaction.useInteractedBlock(),
                    material + " interaction must remain available");
            assertEquals(Event.Result.DEFAULT, interaction.useItemInHand(),
                    material + " must not deny use of the held item");
            assertEquals(2, player.getInventory().getItemInMainHand().getAmount(),
                    material + " interaction must not consume a Tear");
        }
    }

    @Test
    void doesNotCancelOrConsumeWhenDashIsOnCooldown() {
        configureImmediateDash();
        plugin.getConfig().set("dash-cooldown-seconds", 60);
        plugin.loadSettings();

        PlayerMock player = eligiblePlayerWithTear();
        PlayerInteractEvent firstInteraction = rightClickBlock(player, player.getInventory().getItemInMainHand(), Material.STONE);
        plugin.getDashHandler().onPlayerUseTear(firstInteraction);

        PlayerInteractEvent cooldownInteraction = rightClickBlock(
                player,
                player.getInventory().getItemInMainHand(),
                Material.STONE
        );
        plugin.getDashHandler().onPlayerUseTear(cooldownInteraction);

        assertEquals(Event.Result.DENY, firstInteraction.useItemInHand());
        assertEquals(Event.Result.DEFAULT, cooldownInteraction.useItemInHand());
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void doesNotCancelOrConsumeWhenThePlayerLacksDashPermission() {
        configureImmediateDash();
        PlayerMock player = addPlayer();
        player.addAttachment(plugin, "celestialdash.use", false);
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItemInMainHand(TearUtils.createCelestialTear(2));
        PlayerInteractEvent interaction = rightClickBlock(player, player.getInventory().getItemInMainHand(), Material.STONE);

        plugin.getDashHandler().onPlayerUseTear(interaction);

        assertEquals(Event.Result.DEFAULT, interaction.useItemInHand());
        assertEquals(2, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void dashesWithATearCreatedBeforeTheCustomModelDataChanged() {
        configureImmediateDash();
        plugin.getConfig().set("tear-custom-model-data", 22001);
        plugin.loadSettings();
        TearUtils.initialize(plugin, plugin.getSettings().tearCustomModelData());
        ItemStack existingTear = TearUtils.createCelestialTear(2);

        plugin.getConfig().set("tear-custom-model-data", 22002);
        plugin.loadSettings();
        TearUtils.initialize(plugin, plugin.getSettings().tearCustomModelData());

        PlayerMock player = addPlayer();
        player.addAttachment(plugin, "celestialdash.use", true);
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItemInMainHand(existingTear);
        PlayerInteractEvent interaction = rightClickBlock(player, existingTear, Material.STONE);

        plugin.getDashHandler().onPlayerUseTear(interaction);

        assertEquals(Event.Result.DENY, interaction.useItemInHand());
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }

    private void configureImmediateDash() {
        plugin.getConfig().set("dash-cooldown-seconds", 0);
        plugin.getConfig().set("dash-particle-enabled", false);
        plugin.getConfig().set("dash-sound-enabled", false);
        plugin.getConfig().set("trail-enabled", false);
        plugin.getConfig().set("regen-duration-seconds", 0);
        plugin.getConfig().set("double-dash.enabled", false);
        plugin.loadSettings();
    }

    private PlayerMock eligiblePlayerWithTear() {
        PlayerMock player = addPlayer();
        player.addAttachment(plugin, "celestialdash.use", true);
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItemInMainHand(TearUtils.createCelestialTear(2));
        return player;
    }

    private PlayerMock addPlayer() {
        return Objects.requireNonNull(
                MockBukkit.getMock(),
                "MockBukkit server must be initialized before adding a player"
        ).addPlayer();
    }

    private PlayerInteractEvent rightClickAir(PlayerMock player, ItemStack item) {
        return new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_AIR,
                item,
                null,
                BlockFace.SELF,
                EquipmentSlot.HAND
        );
    }

    private PlayerInteractEvent rightClickBlock(PlayerMock player, ItemStack item, Material material) {
        Block block = player.getWorld().getBlockAt(0, 64, 0);
        block.setType(material);
        return new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                item,
                block,
                BlockFace.UP,
                EquipmentSlot.HAND
        );
    }
}
