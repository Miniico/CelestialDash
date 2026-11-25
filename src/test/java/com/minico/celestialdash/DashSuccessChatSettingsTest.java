package com.minico.celestialdash;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashSuccessChatSettingsTest {
    private static final Logger LOGGER = Logger.getLogger(DashSuccessChatSettingsTest.class.getName());

    @Test
    void keepsThePreviousPublicConstructorWithDefaultSuccessChatBehavior() {
        PluginSettings.DashSettings defaults = PluginSettings.defaults().dash();
        PluginSettings.DashSettings compatible = new PluginSettings.DashSettings(
                defaults.cooldownMs(), defaults.strength(), defaults.lift(),
                defaults.regenerationDurationTicks(), defaults.regenerationAmplifier(),
                defaults.impactParticle(), defaults.sound(), defaults.trail(), defaults.doubleDash(),
                defaults.blacklistedWorlds());

        assertEquals(defaults, compatible);
        assertFalse(compatible.successChatEnabled());
    }

    @Test
    void defaultsToQuietChatForOldConfigurations() {
        assertFalse(PluginSettings.defaults().dash().successChatEnabled());
        assertFalse(PluginSettings.load(new YamlConfiguration(), LOGGER, null).dash().successChatEnabled());
    }

    @Test
    void reloadsExplicitValuesAndRetainsTheLastValueWhenTheKeyIsMissing() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("dash-success-chat-enabled", true);
        PluginSettings enabled = PluginSettings.load(config, LOGGER, null);
        assertTrue(enabled.dash().successChatEnabled());

        config.set("dash-success-chat-enabled", null);
        PluginSettings missing = PluginSettings.load(config, LOGGER, enabled);
        assertTrue(missing.dash().successChatEnabled());

        config.set("dash-success-chat-enabled", false);
        PluginSettings disabled = PluginSettings.load(config, LOGGER, missing);
        assertFalse(disabled.dash().successChatEnabled());
        assertTrue(enabled.dash().successChatEnabled(), "Reload must leave the old snapshot immutable");
    }
}
