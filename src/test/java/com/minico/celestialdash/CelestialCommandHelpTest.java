package com.minico.celestialdash;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CelestialCommandHelpTest {

    private ServerMock server;
    private CelestialDash plugin;
    private PluginCommand command;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CelestialDash.class);
        command = Objects.requireNonNull(plugin.getCommand("celestialdash"));
        // Keep command output independent of welcome delivery and resource-pack requests.
        plugin.getConfig().set("chronicle.enabled", false);
        plugin.getConfig().set("chronicle.notification-enabled", false);
        plugin.getConfig().set("chronicle.delivery-sound-enabled", false);
        plugin.getConfig().set("resource-pack.enabled", false);
        plugin.loadSettings();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void bareCommandAndExplicitHelpAreIdenticalForEveryPermissionProfileAndConsole() {
        for (boolean admin : List.of(false, true)) {
            for (boolean chronicle : List.of(false, true)) {
                PlayerMock player = player("Profile" + admin + chronicle, admin, chronicle);
                List<String> bare = run(player, "celestialdash");
                assertFalse(bare.isEmpty());
                assertEquals(bare, run(player, "celestialdash help"));
                assertEquals(bare, run(player, "celestialdash HeLp"));
            }
        }
        assertEquals(run(server.getConsoleSender(), "celestialdash"),
                run(server.getConsoleSender(), "celestialdash help"));
    }

    @Test
    void playerWithoutCommandPermissionsSeesOnlyHelp() {
        PlayerMock player = player("Visitor", false, false);

        assertEquals(Set.of("/celestialdash help"), usages(run(player, "celestialdash")));
        assertEquals(List.of("help"), complete(player, ""));
        assertEquals(List.of("help"), complete(player, "H"));
    }

    @Test
    void ordinaryPlayerSeesHelpAndSelfChronicleWithoutAdminCommandsOrExamples() {
        PlayerMock player = player("Reader", false, true);
        List<String> help = run(player, "celestialdash help");

        assertEquals(Set.of("/celestialdash help", "/celestialdash chronicle"), usages(help));
        assertTrue(examples(help).isEmpty());
        assertEquals(0, countChronicles(player), "Viewing help must not recover a book");
        assertEquals(Set.of("help", "chronicle"), Set.copyOf(complete(player, "")));
        assertTrue(complete(player, "chronicle", "").isEmpty());
        assertTrue(complete(player, "give", "").isEmpty());
        assertTrue(complete(player, "pack", "").isEmpty());
    }

    @Test
    void adminSeesEveryCommandAndAdminPermissionDoesNotGrantSelfChronicle() {
        PlayerMock admin = player("AdminReader", true, true);
        Set<String> expected = Set.of(
                "/celestialdash help", "/celestialdash chronicle",
                "/celestialdash give <player> <amount>", "/celestialdash chronicle give <player>",
                "/celestialdash pack send <player>", "/celestialdash reload");

        List<String> help = run(admin, "celestialdash");
        assertEquals(expected, usages(help));
        assertTrue(help.stream().map(String::trim).filter(line -> line.startsWith("/"))
                .allMatch(line -> line.matches(".+ - .+")), "Each usage must explain what the command does");
        assertEquals(Set.of("help", "chronicle", "give", "pack", "reload"),
                Set.copyOf(complete(admin, "")));

        PlayerMock adminOnly = player("AdminOnly", true, false);
        assertEquals(expected.stream().filter(line -> !line.equals("/celestialdash chronicle"))
                .collect(Collectors.toSet()), usages(run(adminOnly, "celestialdash help")));
        assertEquals(List.of("chronicle"), complete(adminOnly, "chr"));
        assertEquals(List.of("give"), complete(adminOnly, "chronicle", ""));
    }

    @Test
    void consoleHelpOmitsPlayerOnlyChronicleButKeepsAdminCommands() {
        List<String> help = run(server.getConsoleSender(), "celestialdash");

        assertEquals(Set.of("/celestialdash help", "/celestialdash give <player> <amount>",
                "/celestialdash chronicle give <player>", "/celestialdash pack send <player>",
                "/celestialdash reload"), usages(help));
        assertEquals(Set.of("help", "give", "chronicle", "pack", "reload"),
                Set.copyOf(complete(server.getConsoleSender(), "")));
    }

    @Test
    void aliasesUseTheInvokedLabelInEveryUsageAndExample() {
        PlayerMock admin = player("AliasAdmin", true, true);

        for (String alias : List.of("cdash", "celestial")) {
            List<String> help = run(admin, alias);
            assertEquals(help, run(admin, alias + " help"));
            assertEquals(6, usages(help).size());
            assertTrue(usages(help).stream().allMatch(line -> line.startsWith("/" + alias + " ")));
            assertEquals(3, examples(help).size());
            assertTrue(examples(help).stream().allMatch(line -> line.startsWith(alias + " ")));
            assertTrue(help.stream().noneMatch(line -> line.contains("/celestialdash ")));
        }
    }

    @Test
    void examplesTargetTheAdminHimselfAndRemainExecutableWithTheSmallestGiveLimit() {
        player("AnotherOnlinePlayer", false, false);
        PlayerMock admin = player("ExampleAdmin", true, true);
        plugin.getConfig().set("give-max-amount", 1);
        plugin.loadSettings();

        List<String> examples = examples(run(admin, "cdash help"));
        assertEquals(Set.of("cdash give ExampleAdmin 1", "cdash chronicle give ExampleAdmin",
                "cdash pack send ExampleAdmin"), Set.copyOf(examples));
        assertEquals(List.of(plain(plugin.getMessages().formatGiveSuccess(admin.getName(), 1))),
                run(admin, "cdash give ExampleAdmin 1"));
        assertEquals(1, countTears(admin));
        assertEquals(List.of(plain(plugin.getMessages().formatChronicleGiven(admin.getName()))),
                run(admin, "cdash chronicle give ExampleAdmin"));
        assertEquals(1, countChronicles(admin));
        assertEquals(List.of(plain(plugin.getMessages().getResourcePackDisabledMessage())),
                run(admin, "cdash pack send ExampleAdmin"));
        assertEquals(List.of(plain(plugin.getMessages().getConfigReloadedMessage())), run(admin, "cdash reload"));
    }

    @Test
    void inheritedUsageConfigurationStillControlsEachLine() {
        PlayerMock admin = player("CustomAdmin", true, true);
        plugin.getConfig().set("messages.usage-header", "&aLegacy header");
        plugin.getConfig().set("messages.usage-chronicle", "&bLegacy self /%label% chronicle");
        plugin.getConfig().set("messages.usage-reload", "&cLegacy reload /%label% reload");
        plugin.getConfig().set("messages.usage-give", "&dLegacy give /%label% give <player> <amount>");
        plugin.getConfig().set("messages.usage-chronicle-give", "&eLegacy book /%label% chronicle give <player>");
        plugin.getConfig().set("messages.usage-pack-send", "&fLegacy pack /%label% pack send <player>");
        plugin.getConfig().set("messages.usage-help", "Custom help /%label% help");
        plugin.getConfig().set("messages.usage-example", "Try /%label% %command%");
        plugin.getMessages().reload();

        List<String> help = run(admin, "celestial help");
        for (String expected : List.of("Legacy header", "Legacy self /celestial chronicle",
                "Legacy reload /celestial reload", "Legacy give /celestial give <player> <amount>",
                "Legacy book /celestial chronicle give <player>",
                "Legacy pack /celestial pack send <player>", "Custom help /celestial help",
                "Try /celestial give CustomAdmin 1", "Try /celestial chronicle give CustomAdmin",
                "Try /celestial pack send CustomAdmin")) {
            assertTrue(help.stream().anyMatch(line -> line.startsWith(expected)), expected);
        }
        assertEquals("Legacy self /celestial chronicle", plain(plugin.getMessages().formatUsageChronicle("celestial")));
        assertEquals("Legacy reload /celestial reload", plain(plugin.getMessages().formatUsageReload("celestial")));
        assertEquals("Legacy give /celestial give <player> <amount>", plain(plugin.getMessages().formatUsageGive("celestial")));
        assertEquals("Legacy book /celestial chronicle give <player>", plain(plugin.getMessages().formatUsageChronicleGive("celestial")));
        assertEquals("Legacy pack /celestial pack send <player>", plain(plugin.getMessages().formatUsagePackSend("celestial")));
        assertTrue(help.stream().noneMatch(line -> line.contains("%label%") || line.contains("%command%")));
    }

    @Test
    void configurationsWithoutNewHelpKeysReceiveRuntimeFallbacks() {
        PlayerMock admin = player("LegacyAdmin", true, true);
        plugin.getConfig().setDefaults(new YamlConfiguration());
        plugin.getConfig().set("messages.usage-help", null);
        plugin.getConfig().set("messages.usage-example", null);
        for (String name : List.of("chronicle", "give", "chronicle-give", "pack-send", "reload")) {
            plugin.getConfig().set("messages.usage-" + name + "-description", null);
        }
        plugin.getMessages().reload();

        List<String> help = run(admin, "celestialdash");
        assertTrue(usages(help).contains("/celestialdash help"));
        assertEquals(3, examples(help).size());
        assertTrue(examples(help).contains("celestialdash give LegacyAdmin 1"));
        assertTrue(help.stream().anyMatch(line -> line.contains(" - Recover your Chronicle")));
        assertTrue(help.stream().anyMatch(line -> line.contains(" - Give Celestial Tears to a player")));
    }

    @Test
    void helpAccessDoesNotAuthorizeExecutingProtectedCommands() {
        PlayerMock visitor = player("Restricted", false, false);
        PlayerMock target = player("Target", false, false);
        assertFalse(run(visitor, "celestialdash help").isEmpty());

        for (String args : List.of("give Target 1", "chronicle give Target", "pack send Target", "reload")) {
            assertEquals(List.of(plain(plugin.getMessages().getNoAdminPermissionMessage())),
                    run(visitor, "celestialdash " + args));
        }
        assertEquals(0, countTears(target));
        assertEquals(0, countChronicles(target));
        assertEquals(List.of(plain(plugin.getMessages().getNoChroniclePermissionMessage())),
                run(visitor, "celestialdash chronicle"));
        assertEquals(List.of(plain(plugin.getMessages().getChroniclePlayerOnlyMessage())),
                run(server.getConsoleSender(), "celestialdash chronicle"));
    }

    @Test
    void chroniclePermissionAllowsOnlyThePlayerRecoveryCommand() {
        PlayerMock reader = player("AuthorizedReader", false, true);

        run(reader, "celestialdash chronicle");
        assertEquals(1, countChronicles(reader));
        assertEquals(List.of(plain(plugin.getMessages().getNoAdminPermissionMessage())),
                run(reader, "celestialdash chronicle give AuthorizedReader"));
        assertEquals(1, countChronicles(reader));
    }

    @Test
    void adminCompletionRespectsPrefixesOnlinePlayersAndTheCurrentGiveLimit() {
        PlayerMock admin = player("TabAdmin", true, true);
        player("TabTarget", false, false);
        assertEquals(List.of("send"), complete(admin, "pack", "S"));
        assertEquals(List.of("TabTarget"), complete(admin, "give", "TabT"));
        assertEquals(List.of("TabTarget"), complete(admin, "chronicle", "give", "TabT"));
        assertEquals(List.of("TabTarget"), complete(admin, "pack", "send", "TabT"));
        assertEquals(List.of("1", "16", "64"), complete(admin, "give", "TabTarget", ""));

        plugin.getConfig().set("give-max-amount", 16);
        plugin.loadSettings();
        assertEquals(List.of("1", "16"), complete(admin, "give", "TabTarget", ""));
        assertTrue(complete(admin, "give", "TabTarget", "6").isEmpty());
        plugin.getConfig().set("give-max-amount", 1);
        plugin.loadSettings();
        assertEquals(List.of("1"), complete(admin, "give", "TabTarget", ""));
        assertTrue(complete(admin, "help", "").isEmpty());
    }

    private PlayerMock player(String name, boolean admin, boolean chronicle) {
        PlayerMock player = server.addPlayer(name);
        player.setOp(false);
        player.addAttachment(plugin, "celestialdash.admin", admin);
        player.addAttachment(plugin, "celestialdash.chronicle", chronicle);
        drain(player);
        return player;
    }

    private List<String> complete(CommandSender sender, String... args) {
        return command.tabComplete(sender, "celestialdash", args);
    }

    private List<String> run(CommandSender sender, String input) {
        drain(sender);
        assertTrue(server.dispatchCommand(sender, input), "Command must be handled: " + input);
        return drain(sender);
    }

    private List<String> drain(CommandSender sender) {
        List<String> result = new ArrayList<>();
        String message;
        while ((message = sender instanceof PlayerMock player
                ? player.nextMessage() : server.getConsoleSender().nextMessage()) != null) {
            result.add(plain(message));
        }
        return result;
    }

    private Set<String> usages(List<String> messages) {
        return messages.stream().map(String::trim).filter(line -> line.startsWith("/"))
                .map(line -> line.split("\\s+-\\s+", 2)[0]).collect(Collectors.toSet());
    }

    private List<String> examples(List<String> messages) {
        return messages.stream().map(String::trim).filter(line -> line.startsWith("Example: /"))
                .map(line -> line.substring("Example: /".length())).toList();
    }

    private int countTears(PlayerMock player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (TearUtils.isCelestialTear(item)) count += item.getAmount();
        }
        return count;
    }

    private int countChronicles(PlayerMock player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.getChronicleHandler().isChronicle(item)) count += item.getAmount();
        }
        return count;
    }

    @SuppressWarnings("deprecation") // Command output uses the legacy String sender API.
    private String plain(String message) {
        return ChatColor.stripColor(message);
    }
}
