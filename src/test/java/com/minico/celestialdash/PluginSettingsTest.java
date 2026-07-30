package com.minico.celestialdash;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginSettingsTest {

    private static final Logger LOGGER = Logger.getLogger(PluginSettingsTest.class.getName());

    @Test
    void clampsValuesAndNormalizesBlacklistedWorldNames() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("drop-chance-per-second", 2.0);
        config.set("drop-cooldown-seconds", -1);
        config.set("drop-blacklist-worlds", List.of("World_Nether", "WORLD_THE_END"));
        config.set("celestial-amulet.uses", 0);
        config.set("give-max-amount", 5_000);

        PluginSettings settings = PluginSettings.load(config, LOGGER, null);

        assertEquals(1.0, settings.drops().chance());
        assertEquals(0L, settings.drops().cooldownMs());
        assertTrue(settings.drops().isWorldBlacklisted("world_nether"));
        assertTrue(settings.drops().isWorldBlacklisted("world_the_end"));
        assertFalse(settings.drops().isWorldBlacklisted("world"));
        assertEquals(1, settings.amulet().uses());
        assertEquals(2_304, settings.giveMaxAmount());
    }

    @Test
    void keepsPreviousScalarValuesButClearsAMissingWorldBlacklistOnReload() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("dash-cooldown-seconds", 30);
        config.set("drop-blacklist-worlds", List.of("world_nether"));
        PluginSettings initial = PluginSettings.load(config, LOGGER, null);

        config.set("dash-cooldown-seconds", null);
        config.set("drop-blacklist-worlds", null);
        PluginSettings reloaded = PluginSettings.load(config, LOGGER, initial);

        assertEquals(30_000L, reloaded.dash().cooldownMs());
        assertFalse(reloaded.drops().isWorldBlacklisted("world_nether"));
    }

    @Test
    void loadsDropDeliveryDashWorldBlacklistAndSecondDashMultipliers() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("drop-delivery", "inventory");
        config.set("dash-blacklist-worlds", List.of("Spawn", "PVP_ARENA"));
        config.set("double-dash.strength-multiplier", 1.75);
        config.set("double-dash.lift-multiplier", 0.8);

        PluginSettings settings = PluginSettings.load(config, LOGGER, null);

        assertEquals(PluginSettings.DropDeliveryMode.INVENTORY, settings.drops().deliveryMode());
        assertTrue(settings.dash().isWorldBlacklisted("spawn"));
        assertTrue(settings.dash().isWorldBlacklisted("pvp_arena"));
        assertFalse(settings.dash().isWorldBlacklisted("world"));
        assertEquals(1.75, settings.dash().doubleDash().strengthMultiplier());
        assertEquals(0.8, settings.dash().doubleDash().liftMultiplier());
    }

    @Test
    void fallsBackToGroundForAnInvalidDropDeliveryMode() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("drop-delivery", "mailbox");

        PluginSettings settings = PluginSettings.load(config, LOGGER, null);

        assertEquals(PluginSettings.DropDeliveryMode.GROUND, settings.drops().deliveryMode());
    }

    @Test
    void resetsInvalidParticleAndSoundNamesToTheirEstablishedFallbacks() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("dash-particle-type", "FLAME");
        config.set("dash-sound-name", "BLOCK_NOTE_BLOCK_HARP");
        PluginSettings initial = PluginSettings.load(config, LOGGER, null);

        config.set("dash-particle-type", "not-a-particle");
        config.set("dash-sound-name", "not-a-sound");
        PluginSettings reloaded = PluginSettings.load(config, LOGGER, initial);

        assertEquals(Particle.CLOUD, reloaded.dash().impactParticle().type());
        assertEquals(Sound.ENTITY_PHANTOM_FLAP, reloaded.dash().sound().sound());
    }

    @Test
    void loadsResourcePackSettingsAndDisablesDeliveryWhenTheSectionIsRemoved() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("resource-pack.enabled", true);
        config.set("resource-pack.url", "https://cdn.example.com/CelestialDash-Resource-Pack.zip");
        config.set("resource-pack.sha1", "0123456789abcdef0123456789abcdef01234567");
        config.set("resource-pack.required", true);
        config.set("resource-pack.prompt", "&bDownload the resource pack.");

        PluginSettings initial = PluginSettings.load(config, LOGGER, null);

        assertTrue(initial.resourcePack().enabled());
        assertEquals("https://cdn.example.com/CelestialDash-Resource-Pack.zip", initial.resourcePack().url());
        assertEquals("0123456789abcdef0123456789abcdef01234567", initial.resourcePack().sha1());
        assertTrue(initial.resourcePack().required());
        assertEquals("&bDownload the resource pack.", initial.resourcePack().prompt());

        config.set("resource-pack", null);
        PluginSettings reloaded = PluginSettings.load(config, LOGGER, initial);

        assertFalse(reloaded.resourcePack().enabled());
    }

    @Test
    void usesTheEstablishedAmuletEffectsWhenTheConfiguredListIsMissing() {
        PluginSettings settings = PluginSettings.load(new YamlConfiguration(), LOGGER, null);

        assertEquals(Set.of(
                PotionEffectType.POISON,
                PotionEffectType.WITHER,
                PotionEffectType.SLOW,
                PotionEffectType.WEAKNESS,
                PotionEffectType.BLINDNESS,
                PotionEffectType.HUNGER,
                PotionEffectType.LEVITATION,
                PotionEffectType.DARKNESS,
                PotionEffectType.UNLUCK,
                PotionEffectType.BAD_OMEN
        ), settings.amulet().purifiableEffects());
    }

    @Test
    void ignoresInvalidConfiguredAmuletEffectsAndKeepsTheSnapshotImmutable() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("celestial-amulet.purifiable-effects", List.of("poison", "SLOW", "not-an-effect", 42, ""));
        WarningCapturingHandler handler = new WarningCapturingHandler();
        Logger logger = Logger.getLogger(PluginSettingsTest.class.getName() + ".amulet-effects-" + System.nanoTime());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        PluginSettings settings;
        try {
            settings = PluginSettings.load(config, logger, null);
        } finally {
            logger.removeHandler(handler);
        }

        Set<PotionEffectType> effects = settings.amulet().purifiableEffects();
        assertEquals(Set.of(PotionEffectType.POISON, PotionEffectType.SLOW), effects);
        assertThrows(UnsupportedOperationException.class, () -> effects.add(PotionEffectType.WITHER));
        assertTrue(handler.messages().stream().anyMatch(message -> message.contains("not-an-effect")));
        assertTrue(handler.messages().stream().anyMatch(message -> message.contains("42")));
    }

    @Test
    void fallsBackToTheDefaultAmuletEffectsWhenTheConfiguredValueIsNotAList() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("celestial-amulet.purifiable-effects", "POISON");
        WarningCapturingHandler handler = new WarningCapturingHandler();
        Logger logger = Logger.getLogger(PluginSettingsTest.class.getName() + ".amulet-effects-type-" + System.nanoTime());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        PluginSettings settings;
        try {
            settings = PluginSettings.load(config, logger, null);
        } finally {
            logger.removeHandler(handler);
        }

        assertTrue(settings.amulet().purifiableEffects().contains(PotionEffectType.WITHER));
        assertTrue(handler.messages().stream()
                .anyMatch(message -> message.contains("celestial-amulet.purifiable-effects")));
    }

    private static final class WarningCapturingHandler extends Handler {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
            // No buffered output.
        }

        @Override
        public void close() {
            // No external resources.
        }

        List<String> messages() {
            return List.copyOf(messages);
        }
    }
}
