package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/** Event-contract tests with a simulated protection listener, not a WorldGuard integration test. */
class ProtectionCompatibilityTest {
    private ServerMock server;
    private CelestialDash plugin;
    private SimulatedProtection protection;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        plugin.getConfig().set("dash-cooldown-seconds", 60);
        plugin.getConfig().set("double-dash.enabled", false);
        plugin.getConfig().set("dash-particle-enabled", false);
        plugin.getConfig().set("dash-sound-enabled", false);
        plugin.getConfig().set("trail-enabled", false);
        plugin.getConfig().set("regen-duration-seconds", 0);
        plugin.getConfig().set("celestial-amulet.cooldown-seconds", 60);
        plugin.loadSettings();
        protection = new SimulatedProtection();
        // Register after CelestialDash: priority ordering must still run protection first.
        server.getPluginManager().registerEvents(protection, MockBukkit.createMockPlugin());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @CsvSource({
            "TEAR, RIGHT_CLICK_BLOCK, DENY, DEFAULT",
            "TEAR, RIGHT_CLICK_BLOCK, DENY, ALLOW",
            "TEAR, RIGHT_CLICK_BLOCK, DEFAULT, DENY",
            "TEAR, RIGHT_CLICK_AIR, DENY, DENY",
            "AMULET, RIGHT_CLICK_BLOCK, DENY, DEFAULT",
            "AMULET, RIGHT_CLICK_BLOCK, DENY, ALLOW",
            "AMULET, RIGHT_CLICK_BLOCK, DEFAULT, DENY",
            "AMULET, RIGHT_CLICK_AIR, DENY, DENY"
    })
    void respectsIndependentDenialsWithoutSpendingItemsEffectsOrCooldown(
            ItemKind kind, Action action, Event.Result blockResult, Event.Result itemResult) {
        PlayerMock player = eligiblePlayer(kind);
        ItemStack originalItem = player.getInventory().getItemInMainHand().clone();
        Vector originalVelocity = player.getVelocity().clone();
        protection.blockResult = blockResult;
        protection.itemResult = itemResult;
        PlayerInteractEvent denied = interaction(player, action, Material.STONE);

        dispatch(denied);

        assertEquals(1, protection.calls);
        assertEquals(blockResult, denied.useInteractedBlock());
        assertEquals(itemResult, denied.useItemInHand());
        assertEquals(originalItem, player.getInventory().getItemInMainHand());
        assertEquals(originalVelocity, player.getVelocity());
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
        assertEquals(100, player.getFireTicks());
        assertEquals(0, plugin.getDashHandler().getRemainingCooldownSeconds(player));
        assertFalse(plugin.getDashHandler().isInDoubleDashWindow(player));

        // A denial must not start a hidden cooldown: an immediately allowed click works.
        protection.blockResult = null;
        protection.itemResult = null;
        PlayerInteractEvent allowed = interaction(player, action, Material.STONE);
        dispatch(allowed);
        assertActivated(kind, player, allowed);

        // The successful retry does start the configured cooldown.
        ItemStack afterSuccess = player.getInventory().getItemInMainHand().clone();
        if (kind == ItemKind.AMULET) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        }
        dispatch(interaction(player, Action.RIGHT_CLICK_BLOCK, Material.STONE));
        assertEquals(afterSuccess, player.getInventory().getItemInMainHand());
        assertEquals(3, protection.calls);
        if (kind == ItemKind.AMULET) {
            assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
        } else {
            assertTrue(plugin.getDashHandler().getRemainingCooldownSeconds(player) > 0);
        }
    }

    @ParameterizedTest
    @EnumSource(ItemKind.class)
    @SuppressWarnings("deprecation")
    void acceptsVanillaPredictedAirCancellationWithDefaultItemUse(ItemKind kind) {
        PlayerMock player = eligiblePlayer(kind);
        PlayerInteractEvent event = interaction(player, Action.RIGHT_CLICK_AIR, null);
        assertTrue(event.isCancelled(), "The API predicts a vanilla no-op in air");
        assertEquals(Event.Result.DENY, event.useInteractedBlock());
        assertEquals(Event.Result.DEFAULT, event.useItemInHand());

        dispatch(event);

        assertEquals(1, protection.calls);
        assertActivated(kind, player, event);
    }

    @ParameterizedTest
    @CsvSource({"TEAR, CRAFTING_TABLE", "TEAR, CHEST", "TEAR, OAK_DOOR", "TEAR, STONE_BUTTON",
            "AMULET, CRAFTING_TABLE", "AMULET, CHEST", "AMULET, OAK_DOOR", "AMULET, STONE_BUTTON"})
    void leavesVanillaInteractableBlocksAndItemsUntouched(ItemKind kind, Material material) {
        PlayerMock player = eligiblePlayer(kind);
        ItemStack before = player.getInventory().getItemInMainHand().clone();
        Vector velocity = player.getVelocity().clone();
        PlayerInteractEvent event = interaction(player, Action.RIGHT_CLICK_BLOCK, material);

        dispatch(event);

        assertEquals(1, protection.calls);
        assertEquals(Event.Result.ALLOW, event.useInteractedBlock());
        assertEquals(Event.Result.DEFAULT, event.useItemInHand());
        assertEquals(before, player.getInventory().getItemInMainHand());
        assertEquals(velocity, player.getVelocity());
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
        assertEquals(100, player.getFireTicks());
    }

    private PlayerMock eligiblePlayer(ItemKind kind) {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "celestialdash.use", true);
        player.addAttachment(plugin, "celestialdash.amulet", true);
        player.getInventory().setItemInMainHand(kind == ItemKind.TEAR
                ? TearUtils.createCelestialTear(2) : CelestialAmulet.create());
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
        player.setFireTicks(100);
        return player;
    }

    private PlayerInteractEvent interaction(PlayerMock player, Action action, Material material) {
        Block block = null;
        if (action == Action.RIGHT_CLICK_BLOCK) {
            block = player.getWorld().getBlockAt(0, 64, 0);
            block.setType(material);
        }
        return new PlayerInteractEvent(player, action, player.getInventory().getItemInMainHand(),
                block, block == null ? BlockFace.SELF : BlockFace.UP, EquipmentSlot.HAND);
    }

    private void dispatch(PlayerInteractEvent event) {
        try {
            server.getPluginManager().callEvent(event);
        } catch (UnimplementedOperationException exception) {
            // Amulet state and cooldown are committed before its cosmetic particles.
            // Do not suppress another unimplemented operation earlier in event processing.
            assertTrue(Arrays.stream(exception.getStackTrace()).anyMatch(frame ->
                    frame.getClassName().equals("be.seeseemelk.mockbukkit.WorldMock")
                            && frame.getMethodName().equals("spawnParticle")), exception::toString);
            assertTrue(CelestialAmulet.isCelestialAmulet(event.getItem()));
            assertEquals(Event.Result.DENY, event.useItemInHand());
        }
    }

    private void assertActivated(ItemKind kind, PlayerMock player, PlayerInteractEvent event) {
        assertEquals(Event.Result.DENY, event.useInteractedBlock());
        assertEquals(Event.Result.DENY, event.useItemInHand());
        if (kind == ItemKind.TEAR) {
            assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
            assertTrue(player.getVelocity().lengthSquared() > 0);
        } else {
            assertEquals(2, CelestialAmulet.getRemainingUses(player.getInventory().getItemInMainHand()));
            assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
            assertEquals(0, player.getFireTicks());
        }
    }

    enum ItemKind { TEAR, AMULET }

    public static final class SimulatedProtection implements Listener {
        private Event.Result blockResult;
        private Event.Result itemResult;
        private int calls;

        @EventHandler(priority = EventPriority.LOW)
        public void protect(PlayerInteractEvent event) {
            calls++;
            if (blockResult != null) event.setUseInteractedBlock(blockResult);
            if (itemResult != null) event.setUseItemInHand(itemResult);
        }
    }
}
