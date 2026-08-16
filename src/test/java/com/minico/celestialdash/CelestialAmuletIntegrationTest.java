package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CelestialAmuletIntegrationTest {

    private CelestialDash plugin;
    private AmuletHandler amuletHandler;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        amuletHandler = plugin.getAmuletHandler();
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
    void acceptsExistingTearsForTheAmuletRecipeAfterCustomModelDataChanges() {
        TearUtils.initialize(plugin, 22001);
        ItemStack existingTear = TearUtils.createCelestialTear();
        TearUtils.initialize(plugin, 22002);

        ShapedRecipe recipe = (ShapedRecipe) Bukkit.getRecipe(CelestialAmulet.getRecipeKey());
        RecipeChoice tearChoice = Objects.requireNonNull(recipe).getChoiceMap().get('T');

        assertInstanceOf(RecipeChoice.MaterialChoice.class, tearChoice);
        assertTrue(tearChoice.test(existingTear));
        assertTrue(AmuletHandler.hasValidAmuletTearIngredients(amuletRecipeMatrix(existingTear)));
    }

    @Test
    void rejectsVanillaTearsForTheAmuletRecipe() {
        assertFalse(AmuletHandler.hasValidAmuletTearIngredients(
                amuletRecipeMatrix(new ItemStack(Material.GHAST_TEAR))
        ));
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

    @Test
    void ignoresAnInteractionCanceledByAnotherPlugin() {
        configureImmediateAmulet();
        PlayerMock player = eligiblePlayerWithAmulet();
        ItemStack amulet = player.getInventory().getItemInMainHand();
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        PlayerInteractEvent interaction = rightClickBlock(player, amulet, Material.STONE);
        interaction.setCancelled(true);

        invokeAmuletUse(amuletHandler, interaction);

        assertEquals(Event.Result.DENY, interaction.useItemInHand());
        assertEquals(3, CelestialAmulet.getRemainingUses(amulet));
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
    }

    @Test
    void consumesAChargeAndCancelsOnlyAfterSuccessfulPurification() {
        configureImmediateAmulet();
        PlayerMock player = eligiblePlayerWithAmulet();
        ItemStack amulet = player.getInventory().getItemInMainHand();
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        PlayerInteractEvent interaction = rightClickBlock(player, amulet, Material.STONE);

        invokeAmuletUse(amuletHandler, interaction);

        assertEquals(Event.Result.DENY, interaction.useItemInHand());
        assertEquals(2, CelestialAmulet.getRemainingUses(amulet));
        assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
    }

    @Test
    void purifiesOnRightClickAirDespiteTheVanillaNoOpPrediction() {
        configureImmediateAmulet();
        PlayerMock player = eligiblePlayerWithAmulet();
        ItemStack amulet = player.getInventory().getItemInMainHand();
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        PlayerInteractEvent interaction = rightClickAir(player, amulet);

        invokeAmuletUse(amuletHandler, interaction);

        assertEquals(Event.Result.DENY, interaction.useItemInHand());
        assertEquals(2, CelestialAmulet.getRemainingUses(amulet));
        assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
    }

    @Test
    void leavesVanillaInteractableBlockClicksUntouched() {
        configureImmediateAmulet();
        PlayerMock player = eligiblePlayerWithAmulet();

        for (Material material : java.util.List.of(Material.CRAFTING_TABLE, Material.CHEST)) {
            ItemStack amulet = CelestialAmulet.create();
            player.getInventory().setItemInMainHand(amulet);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
            PlayerInteractEvent interaction = rightClickBlock(player, amulet, material);

            amuletHandler.onUseAmulet(interaction);

            assertEquals(Event.Result.ALLOW, interaction.useInteractedBlock(),
                    material + " interaction must remain available");
            assertEquals(Event.Result.DEFAULT, interaction.useItemInHand(),
                    material + " must not deny use of the held item");
            assertEquals(3, CelestialAmulet.getRemainingUses(amulet),
                    material + " interaction must not consume an Amulet charge");
            assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
            player.removePotionEffect(PotionEffectType.POISON);
        }
    }

    @Test
    void doesNotCancelOrConsumeWhenThereIsNothingToPurify() {
        configureImmediateAmulet();
        PlayerMock player = eligiblePlayerWithAmulet();
        ItemStack amulet = player.getInventory().getItemInMainHand();
        PlayerInteractEvent interaction = rightClickBlock(player, amulet, Material.STONE);

        amuletHandler.onUseAmulet(interaction);

        assertEquals(Event.Result.DEFAULT, interaction.useItemInHand());
        assertEquals(3, CelestialAmulet.getRemainingUses(amulet));
    }

    @Test
    void doesNotCancelOrConsumeWhenThePlayerLacksAmuletPermission() {
        configureImmediateAmulet();
        PlayerMock player = addPlayer();
        player.addAttachment(plugin, "celestialdash.amulet", false);
        player.getInventory().setHeldItemSlot(0);
        ItemStack amulet = CelestialAmulet.create();
        player.getInventory().setItemInMainHand(amulet);
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        PlayerInteractEvent interaction = rightClickBlock(player, amulet, Material.STONE);

        amuletHandler.onUseAmulet(interaction);

        assertEquals(Event.Result.DEFAULT, interaction.useItemInHand());
        assertEquals(3, CelestialAmulet.getRemainingUses(amulet));
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
    }

    @Test
    void keepsTheAmuletCooldownAfterReconnect() {
        plugin.getConfig().set("celestial-amulet.cooldown-seconds", 60);
        plugin.loadSettings();
        PlayerMock player = eligiblePlayerWithAmulet();
        ItemStack amulet = player.getInventory().getItemInMainHand();
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));

        invokeAmuletUse(amuletHandler, rightClickBlock(player, amulet, Material.STONE));
        assertEquals(2, CelestialAmulet.getRemainingUses(amulet));

        assertTrue(player.disconnect());
        assertTrue(player.reconnect());
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        PlayerInteractEvent cooldownInteraction = rightClickBlock(player, amulet, Material.STONE);

        invokeAmuletUse(amuletHandler, cooldownInteraction);

        assertEquals(Event.Result.DEFAULT, cooldownInteraction.useItemInHand());
        assertEquals(2, CelestialAmulet.getRemainingUses(amulet));
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
    }

    @Test
    void removesExpiredAmuletCooldowns() {
        plugin.getConfig().set("celestial-amulet.cooldown-seconds", 60);
        plugin.loadSettings();
        PlayerMock player = eligiblePlayerWithAmulet();
        ItemStack amulet = player.getInventory().getItemInMainHand();
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));

        invokeAmuletUse(amuletHandler, rightClickBlock(player, amulet, Material.STONE));
        amuletHandler.cleanupExpiredCooldowns(System.currentTimeMillis() + 60_000L);
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));

        invokeAmuletUse(amuletHandler, rightClickBlock(player, amulet, Material.STONE));

        assertEquals(1, CelestialAmulet.getRemainingUses(amulet));
        assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
    }

    private void configureImmediateAmulet() {
        plugin.getConfig().set("celestial-amulet.cooldown-seconds", 0);
        plugin.loadSettings();
    }

    private PlayerMock eligiblePlayerWithAmulet() {
        PlayerMock player = addPlayer();
        player.addAttachment(plugin, "celestialdash.amulet", true);
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItemInMainHand(CelestialAmulet.create());
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

    private ItemStack[] amuletRecipeMatrix(ItemStack tear) {
        ItemStack[] matrix = new ItemStack[9];
        matrix[1] = tear;
        matrix[3] = tear;
        matrix[4] = new ItemStack(Material.NETHERITE_INGOT);
        matrix[5] = tear;
        matrix[7] = tear;
        return matrix;
    }

    private void invokeAmuletUse(AmuletHandler handler, PlayerInteractEvent interaction) {
        try {
            handler.onUseAmulet(interaction);
        } catch (UnimplementedOperationException ignored) {
            // MockBukkit does not implement World#spawnParticle. The interaction
            // state changes occur before the cosmetic effects in the handler.
        }
    }
}
