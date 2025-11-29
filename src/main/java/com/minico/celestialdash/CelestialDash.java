package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CelestialDash extends JavaPlugin {

    // Configuration holder
    private ConfigSettings config;

    // Services
    private Messages messages;
    private DashHandler dashHandler;
    private DropHandler dropHandler;

    @Override
    public void onEnable() {
        getLogger().info("CelestialDash is starting...");

        saveDefaultConfig();
        loadSettings();

        // Initialize TearUtils with config values
        List<String> lore = getConfig().getStringList("tear-lore");
        if (lore.isEmpty()) {
            lore.add("&7A tear formed within a storm cloud,");
            lore.add("&7charged with celestial wind energy.");
        }

        TearUtils.initialize(
                this,
                getConfig().getString("tear-name", "&bCelestial Tear"),
                lore,
                config.tearCustomModelData
        );

        messages = new Messages(this);
        messages.reload();

        dashHandler = new DashHandler(this, messages);
        dropHandler = new DropHandler(this);

        Bukkit.getPluginManager().registerEvents(dashHandler, this);

        PluginCommand cmd = getCommand("celestialdash");
        if (cmd != null) {
            cmd.setExecutor(new CelestialCommand(this, messages));
        } else {
            getLogger().severe("Failed to register command 'celestialdash'! Check plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dropHandler.start();
        getLogger().info("CelestialDash enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (dashHandler != null) {
            dashHandler.cancelAllTasks();
        }
        if (dropHandler != null) {
            dropHandler.stop();
        }
        getLogger().info("CelestialDash disabled.");
    }

    public void loadSettings() {
        config = new ConfigSettings();

        // Core settings
        config.dropChance = getValidDouble("drop-chance-per-second", 0.03, 0.0, 1.0);
        config.dropCooldownMs = getValidLong("drop-cooldown-seconds", 60, 0, Long.MAX_VALUE) * 1000L;
        config.dashCooldownMs = getValidLong("dash-cooldown-seconds", 10, 0, Long.MAX_VALUE) * 1000L;
        config.dashStrength = getValidDouble("dash-strength", 1.8, 0.0, 10.0);
        config.dashLift = getValidDouble("dash-vertical-lift", 0.4, 0.0, 5.0);

        // Tear settings
        config.tearCustomModelData = getConfig().getInt("tear-custom-model-data", 0);

        // Regeneration
        config.regenDurationTicks = getValidInt("regen-duration-seconds", 5, 0, 600) * 20;
        config.regenAmplifier = getValidInt("regen-amplifier", 0, 0, 255);

        // Dash particle
        config.dashParticleEnabled = getConfig().getBoolean("dash-particle-enabled", true);
        config.dashParticle = getParticle("dash-particle-type", Particle.CLOUD);
        config.dashParticleCount = getValidInt("dash-particle-count", 40, 0, 1000);
        config.dashParticleOffsetX = getValidDouble("dash-particle-offset-x", 0.4, 0.0, 10.0);
        config.dashParticleOffsetY = getValidDouble("dash-particle-offset-y", 0.5, 0.0, 10.0);
        config.dashParticleOffsetZ = getValidDouble("dash-particle-offset-z", 0.4, 0.0, 10.0);
        config.dashParticleSpeed = getValidDouble("dash-particle-speed", 0.02, 0.0, 10.0);

        // Dash sound
        config.dashSoundEnabled = getConfig().getBoolean("dash-sound-enabled", true);
        config.dashSound = getSound("dash-sound-name", Sound.ENTITY_PHANTOM_FLAP);
        config.dashSoundVolume = (float) getValidDouble("dash-sound-volume", 1.2, 0.0, 10.0);
        config.dashSoundPitch = (float) getValidDouble("dash-sound-pitch", 0.6, 0.0, 2.0);

        // Trail
        config.trailEnabled = getConfig().getBoolean("trail-enabled", true);
        config.trailParticle = getParticle("trail-particle-type", Particle.CLOUD);
        config.trailParticleCount = getValidInt("trail-particle-count", 20, 0, 1000);
        config.trailOffsetX = getValidDouble("trail-offset-x", 0.3, 0.0, 10.0);
        config.trailOffsetY = getValidDouble("trail-offset-y", 0.4, 0.0, 10.0);
        config.trailOffsetZ = getValidDouble("trail-offset-z", 0.3, 0.0, 10.0);
        config.trailSpeed = getValidDouble("trail-speed", 0.01, 0.0, 10.0);
        config.trailDurationTicks = getValidInt("trail-duration-ticks", 10, 0, 1200);
        config.trailIntervalTicks = getValidInt("trail-interval-ticks", 1, 1, 100);
    }

    // Helper methods for validation
    private double getValidDouble(String path, double defaultValue, double min, double max) {
        double value = getConfig().getDouble(path, defaultValue);
        if (value < min || value > max) {
            getLogger().warning(String.format("Invalid value for '%s': %.2f (must be between %.2f and %.2f). Using default: %.2f",
                    path, value, min, max, defaultValue));
            return defaultValue;
        }
        return value;
    }

    private int getValidInt(String path, int defaultValue, int min, int max) {
        int value = getConfig().getInt(path, defaultValue);
        if (value < min || value > max) {
            getLogger().warning(String.format("Invalid value for '%s': %d (must be between %d and %d). Using default: %d",
                    path, value, min, max, defaultValue));
            return defaultValue;
        }
        return value;
    }

    private long getValidLong(String path, long defaultValue, long min, long max) {
        long value = getConfig().getLong(path, defaultValue);
        if (value < min || value > max) {
            getLogger().warning(String.format("Invalid value for '%s': %d (must be between %d and %d). Using default: %d",
                    path, value, min, max, defaultValue));
            return defaultValue;
        }
        return value;
    }

    private Particle getParticle(String path, Particle defaultParticle) {
        String name = getConfig().getString(path, defaultParticle.name());
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning(String.format("Invalid particle type '%s' for '%s'. Using default: %s",
                    name, path, defaultParticle.name()));
            return defaultParticle;
        }
    }

    private Sound getSound(String path, Sound defaultSound) {
        String name = getConfig().getString(path, defaultSound.name());
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning(String.format("Invalid sound '%s' for '%s'. Using default: %s",
                    name, path, defaultSound.name()));
            return defaultSound;
        }
    }

    // Getters
    public ConfigSettings getConfigSettings() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }

    // Configuration holder class
    public static class ConfigSettings {
        // Core
        public double dropChance;
        public long dropCooldownMs;
        public long dashCooldownMs;
        public double dashStrength;
        public double dashLift;

        // Tear
        public int tearCustomModelData;

        // Regeneration
        public int regenDurationTicks;
        public int regenAmplifier;

        // Dash particle
        public boolean dashParticleEnabled;
        public Particle dashParticle;
        public int dashParticleCount;
        public double dashParticleOffsetX;
        public double dashParticleOffsetY;
        public double dashParticleOffsetZ;
        public double dashParticleSpeed;

        // Dash sound
        public boolean dashSoundEnabled;
        public Sound dashSound;
        public float dashSoundVolume;
        public float dashSoundPitch;

        // Trail
        public boolean trailEnabled;
        public Particle trailParticle;
        public int trailParticleCount;
        public double trailOffsetX;
        public double trailOffsetY;
        public double trailOffsetZ;
        public double trailSpeed;
        public int trailDurationTicks;
        public int trailIntervalTicks;
    }
}