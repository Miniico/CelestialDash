package com.minico.celestialdash;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CelestialDash extends JavaPlugin {

    // Tear drops
    private double dropChance = 0.03;
    private long dropCooldownMs = 60_000L;

    // Dash core
    private long dashCooldownMs = 10_000L;
    private double dashStrength = 1.8;
    private double dashLift = 0.4;

    // Regeneration
    private int regenDurationTicks = 5 * 20;
    private int regenAmplifier = 0;

    // Impact particle
    private boolean dashParticleEnabled = true;
    private Particle dashParticle = Particle.CLOUD;
    private int dashParticleCount = 40;
    private double dashParticleOffsetX = 0.4;
    private double dashParticleOffsetY = 0.5;
    private double dashParticleOffsetZ = 0.4;
    private double dashParticleSpeed = 0.02;

    // Dash sound
    private boolean dashSoundEnabled = true;
    private Sound dashSound = Sound.ENTITY_PHANTOM_FLAP;
    private float dashSoundVolume = 1.2f;
    private float dashSoundPitch = 0.6f;

    // Trail
    private boolean trailEnabled = true;
    private Particle trailParticle = Particle.CLOUD;
    private int trailParticleCount = 20;
    private double trailOffsetX = 0.3;
    private double trailOffsetY = 0.4;
    private double trailOffsetZ = 0.3;
    private double trailSpeed = 0.01;
    private int trailDurationTicks = 10;
    private int trailIntervalTicks = 1;

    // Double dash
    private boolean doubleDashEnabled = true;
    private long doubleDashWindowMs = 4000L;        // 4 seconds
    private int doubleDashFallImmunityTicks = 40;   // 2 seconds

    // Tear CustomModelData
    private int tearCustomModelData = 0;

    // Services
    private Messages messages;
    private DashHandler dashHandler;
    private DropHandler dropHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        // Push CustomModelData to TearUtils
        TearUtils.setCustomModelData(tearCustomModelData);

        messages = new Messages(this);
        messages.reload();

        dashHandler = new DashHandler(this, messages);
        dropHandler = new DropHandler(this);

        getServer().getPluginManager().registerEvents(dashHandler, this);

        PluginCommand cmd = getCommand("celestialdash");
        if (cmd != null) {
            cmd.setExecutor(new CelestialCommand(this, messages));
        } else {
            getLogger().severe("Command 'celestialdash' is not defined in plugin.yml!");
        }

        // PlaceholderAPI support (optional)
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CelestialPlaceholders(this).register();
            getLogger().info("PlaceholderAPI detected — Celestial placeholders enabled.");
        } else {
            getLogger().info("PlaceholderAPI not found — placeholders disabled.");
        }

        dropHandler.start();
        getLogger().info("CelestialDash enabled.");
    }

    @Override
    public void onDisable() {
        if (dropHandler != null) {
            dropHandler.stop();
        }
        getLogger().info("CelestialDash disabled.");
    }

    public void loadSettings() {
        // Tear drops
        dropChance = getConfig().getDouble("drop-chance-per-second", dropChance);
        dropCooldownMs = getConfig().getLong("drop-cooldown-seconds", dropCooldownMs / 1000L) * 1000L;

        // Dash core
        dashCooldownMs = getConfig().getLong("dash-cooldown-seconds", dashCooldownMs / 1000L) * 1000L;
        dashStrength = getConfig().getDouble("dash-strength", dashStrength);
        dashLift = getConfig().getDouble("dash-vertical-lift", dashLift);

        // Regeneration
        regenDurationTicks = getConfig().getInt("regen-duration-seconds", regenDurationTicks / 20) * 20;
        regenAmplifier = getConfig().getInt("regen-amplifier", regenAmplifier);

        // Impact particle
        dashParticleEnabled = getConfig().getBoolean("dash-particle-enabled", dashParticleEnabled);
        String dashParticleName = getConfig().getString("dash-particle-type", dashParticle.name());
        try {
            dashParticle = Particle.valueOf(dashParticleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid dash-particle-type: " + dashParticleName + ", using CLOUD");
            dashParticle = Particle.CLOUD;
        }
        dashParticleCount = getConfig().getInt("dash-particle-count", dashParticleCount);
        dashParticleOffsetX = getConfig().getDouble("dash-particle-offset-x", dashParticleOffsetX);
        dashParticleOffsetY = getConfig().getDouble("dash-particle-offset-y", dashParticleOffsetY);
        dashParticleOffsetZ = getConfig().getDouble("dash-particle-offset-z", dashParticleOffsetZ);
        dashParticleSpeed = getConfig().getDouble("dash-particle-speed", dashParticleSpeed);

        // Dash sound
        dashSoundEnabled = getConfig().getBoolean("dash-sound-enabled", dashSoundEnabled);
        String soundName = getConfig().getString("dash-sound-name", dashSound.name());
        try {
            dashSound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid dash-sound-name: " + soundName + ", using ENTITY_PHANTOM_FLAP");
            dashSound = Sound.ENTITY_PHANTOM_FLAP;
        }
        dashSoundVolume = (float) getConfig().getDouble("dash-sound-volume", dashSoundVolume);
        dashSoundPitch = (float) getConfig().getDouble("dash-sound-pitch", dashSoundPitch);

        // Trail
        trailEnabled = getConfig().getBoolean("trail-enabled", trailEnabled);
        String trailName = getConfig().getString("trail-particle-type", trailParticle.name());
        try {
            trailParticle = Particle.valueOf(trailName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid trail-particle-type: " + trailName + ", using CLOUD");
            trailParticle = Particle.CLOUD;
        }
        trailParticleCount = getConfig().getInt("trail-particle-count", trailParticleCount);
        trailOffsetX = getConfig().getDouble("trail-offset-x", trailOffsetX);
        trailOffsetY = getConfig().getDouble("trail-offset-y", trailOffsetY);
        trailOffsetZ = getConfig().getDouble("trail-offset-z", trailOffsetZ);
        trailSpeed = getConfig().getDouble("trail-speed", trailSpeed);
        trailDurationTicks = getConfig().getInt("trail-duration-ticks", trailDurationTicks);
        trailIntervalTicks = getConfig().getInt("trail-interval-ticks", trailIntervalTicks);

        // Double dash
        doubleDashEnabled = getConfig().getBoolean("double-dash.enabled", doubleDashEnabled);
        doubleDashWindowMs = getConfig().getLong("double-dash.window-ms", doubleDashWindowMs);
        doubleDashFallImmunityTicks = getConfig().getInt("double-dash.fall-immunity-ticks", doubleDashFallImmunityTicks);

        // Tear CustomModelData
        tearCustomModelData = getConfig().getInt("tear-custom-model-data", tearCustomModelData);
    }

    // Getters used by other classes

    public Messages getMessages() {
        return messages;
    }

    public DashHandler getDashHandler() {
        return dashHandler;
    }

    public double getDropChance() {
        return dropChance;
    }

    public long getDropCooldownMs() {
        return dropCooldownMs;
    }

    public long getDashCooldownMs() {
        return dashCooldownMs;
    }

    public double getDashStrength() {
        return dashStrength;
    }

    public double getDashLift() {
        return dashLift;
    }

    public int getRegenDurationTicks() {
        return regenDurationTicks;
    }

    public int getRegenAmplifier() {
        return regenAmplifier;
    }

    public boolean isDashParticleEnabled() {
        return dashParticleEnabled;
    }

    public Particle getDashParticle() {
        return dashParticle;
    }

    public int getDashParticleCount() {
        return dashParticleCount;
    }

    public double getDashParticleOffsetX() {
        return dashParticleOffsetX;
    }

    public double getDashParticleOffsetY() {
        return dashParticleOffsetY;
    }

    public double getDashParticleOffsetZ() {
        return dashParticleOffsetZ;
    }

    public double getDashParticleSpeed() {
        return dashParticleSpeed;
    }

    public boolean isDashSoundEnabled() {
        return dashSoundEnabled;
    }

    public Sound getDashSound() {
        return dashSound;
    }

    public float getDashSoundVolume() {
        return dashSoundVolume;
    }

    public float getDashSoundPitch() {
        return dashSoundPitch;
    }

    public boolean isTrailEnabled() {
        return trailEnabled;
    }

    public Particle getTrailParticle() {
        return trailParticle;
    }

    public int getTrailParticleCount() {
        return trailParticleCount;
    }

    public double getTrailOffsetX() {
        return trailOffsetX;
    }

    public double getTrailOffsetY() {
        return trailOffsetY;
    }

    public double getTrailOffsetZ() {
        return trailOffsetZ;
    }

    public double getTrailSpeed() {
        return trailSpeed;
    }

    public int getTrailDurationTicks() {
        return trailDurationTicks;
    }

    public int getTrailIntervalTicks() {
        return trailIntervalTicks;
    }

    public boolean isDoubleDashEnabled() {
        return doubleDashEnabled;
    }

    public long getDoubleDashWindowMs() {
        return doubleDashWindowMs;
    }

    public int getDoubleDashFallImmunityTicks() {
        return doubleDashFallImmunityTicks;
    }
}
