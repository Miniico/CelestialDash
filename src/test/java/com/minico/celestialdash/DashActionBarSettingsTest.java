package com.minico.celestialdash;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashActionBarSettingsTest {

    private static final Logger LOGGER = Logger.getLogger(DashActionBarSettingsTest.class.getName());

    @Test
    void enablesTheHudForAnOldConfigurationWithoutChangingGameplaySettings() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("dash-cooldown-seconds", 24);
        config.set("double-dash.enabled", false);

        PluginSettings settings = PluginSettings.load(config, LOGGER, null);

        assertEquals(new PluginSettings.ActionBarSettings(true, true, 5,
                "&bCelestial Dash &7» &aReady", "&bCelestial Dash &7» &e%seconds%s",
                "&bSecond dash &7» &f%seconds%s"), settings.actionBar());
        assertEquals(24_000L, settings.dash().cooldownMs());
        assertFalse(settings.dash().doubleDash().enabled());
    }

    @Test
    void reloadsExplicitChangesAndKeepsMissingScalarSettings() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("dash-action-bar.enabled", false);
        config.set("dash-action-bar.only-while-holding-tear", false);
        config.set("dash-action-bar.update-interval-ticks", 13);
        PluginSettings disabled = PluginSettings.load(config, LOGGER, null);
        assertFalse(disabled.actionBar().enabled());

        config.set("dash-action-bar", null);
        PluginSettings missingSection = PluginSettings.load(config, LOGGER, disabled);
        assertFalse(missingSection.actionBar().enabled());
        assertFalse(missingSection.actionBar().onlyWhileHoldingTear());
        assertEquals(13, missingSection.actionBar().updateIntervalTicks());

        config.set("dash-action-bar.enabled", true);
        config.set("dash-action-bar.only-while-holding-tear", true);
        config.set("dash-action-bar.update-interval-ticks", 2);
        PluginSettings enabled = PluginSettings.load(config, LOGGER, missingSection);
        assertTrue(enabled.actionBar().enabled());
        assertTrue(enabled.actionBar().onlyWhileHoldingTear());
        assertEquals(2, enabled.actionBar().updateIntervalTicks());
        assertFalse(disabled.actionBar().enabled());
        assertEquals(13, disabled.actionBar().updateIntervalTicks());
    }

    @Test
    void boundsTheRefreshPeriodAndWarnsOnlyWhenClamped() {
        YamlConfiguration config = new YamlConfiguration();
        List<String> warnings = new ArrayList<>();
        Logger logger = Logger.getLogger(DashActionBarSettingsTest.class.getName() + ".bounds-" + System.nanoTime());
        logger.setUseParentHandlers(false);
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        try {
            config.set("dash-action-bar.update-interval-ticks", 0);
            assertEquals(1, PluginSettings.load(config, logger, null).actionBar().updateIntervalTicks());
            config.set("dash-action-bar.update-interval-ticks", 100);
            assertEquals(20, PluginSettings.load(config, logger, null).actionBar().updateIntervalTicks());
            assertEquals(2, warnings.size());
            assertTrue(warnings.stream().allMatch(message -> message.contains("dash-action-bar.update-interval-ticks")
                    && message.contains("between 1 and 20")));

            config.set("dash-action-bar.update-interval-ticks", 1);
            assertEquals(1, PluginSettings.load(config, logger, null).actionBar().updateIntervalTicks());
            config.set("dash-action-bar.update-interval-ticks", 20);
            assertEquals(20, PluginSettings.load(config, logger, null).actionBar().updateIntervalTicks());
            assertEquals(2, warnings.size());
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void preservesCustomAndEmptyTemplatesWithoutTranslatingTheirColors() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("dash-action-bar.ready", "&aGo!");
        config.set("dash-action-bar.cooldown", "");
        config.set("dash-action-bar.second-dash", "&fAgain in %seconds%");

        PluginSettings settings = PluginSettings.load(config, LOGGER, null);

        assertEquals("&aGo!", settings.actionBar().readyMessage());
        assertEquals("", settings.actionBar().cooldownMessage());
        assertEquals("&fAgain in %seconds%", settings.actionBar().secondDashMessage());

        config.set("dash-action-bar", null);
        PluginSettings reloaded = PluginSettings.load(config, LOGGER, settings);
        assertEquals(PluginSettings.defaults().actionBar(), reloaded.actionBar());
        assertEquals("&aGo!", settings.actionBar().readyMessage());
        assertEquals("", settings.actionBar().cooldownMessage());
    }
}
