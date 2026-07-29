package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    // Celestial Amulet
    private boolean celestialAmuletEnabled = true;
    private boolean celestialAmuletRecipeEnabled = true;
    private int celestialAmuletUses = 3;
    private long celestialAmuletCooldownMs = 60_000L;

    // World blacklist for drops
    private final Set<String> dropBlacklistWorlds = new HashSet<>();

    // Services
    private Messages messages;
    private DashHandler dashHandler;
    private DropHandler dropHandler;
    private AmuletHandler amuletHandler;

    private static final long MAX_COOLDOWN_SECONDS = 86_400L;
    private static final long MAX_DOUBLE_DASH_WINDOW_MS = 60_000L;
    private static final int MAX_REGEN_DURATION_SECONDS = 3_600;
    private static final int MAX_PARTICLE_COUNT = 500;
    private static final double MAX_PARTICLE_OFFSET = 10.0;
    private static final double MAX_PARTICLE_SPEED = 10.0;
    private static final int MAX_TRAIL_DURATION_TICKS = 1_200;
    private static final int MAX_TRAIL_INTERVAL_TICKS = 200;
    private static final int MAX_FALL_IMMUNITY_TICKS = 1_200;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        // Push CustomModelData to TearUtils
        TearUtils.initialize(this, tearCustomModelData);
        CelestialAmulet.initialize(this, celestialAmuletUses);

        messages = new Messages(this);
        messages.reload();

        dashHandler = new DashHandler(this, messages);
        dropHandler = new DropHandler(this);
        amuletHandler = new AmuletHandler(this, messages);

        Bukkit.getPluginManager().registerEvents(dashHandler, this);
        Bukkit.getPluginManager().registerEvents(amuletHandler, this);
        registerAmuletRecipe();

        PluginCommand cmd = getCommand("celestialdash");
        if (cmd != null) {
            cmd.setExecutor(new CelestialCommand(this, messages));
            cmd.setTabCompleter(new CelestialTabCompleter());
        } else {
            getLogger().severe("Command 'celestialdash' is not defined in plugin.yml!");
        }

        // PlaceholderAPI hook
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CelestialPlaceholders(this).register();
            getLogger().info("PlaceholderAPI hook enabled.");
        }

        dropHandler.start();
        getLogger().info("CelestialDash enabled.");
    }

    @Override
    public void onDisable() {
        if (dropHandler != null) {
            dropHandler.stop();
        }
        if (dashHandler != null) {
            dashHandler.stop();
        }
        if (amuletHandler != null) {
            amuletHandler.stop();
        }
        getLogger().info("CelestialDash disabled.");
    }

    public void loadSettings() {
        // Tear drops
        dropChance = getBoundedDouble("drop-chance-per-second", dropChance, 0.0, 1.0);
        dropCooldownMs = getBoundedLong("drop-cooldown-seconds", dropCooldownMs / 1000L, 0, MAX_COOLDOWN_SECONDS) * 1000L;

        // Dash core
        dashCooldownMs = getBoundedLong("dash-cooldown-seconds", dashCooldownMs / 1000L, 0, MAX_COOLDOWN_SECONDS) * 1000L;
        dashStrength = getBoundedDouble("dash-strength", dashStrength, 0.0, 10.0);
        dashLift = getBoundedDouble("dash-vertical-lift", dashLift, 0.0, 5.0);

        // Regeneration
        regenDurationTicks = getBoundedInt("regen-duration-seconds", regenDurationTicks / 20, 0, MAX_REGEN_DURATION_SECONDS) * 20;
        regenAmplifier = getBoundedInt("regen-amplifier", regenAmplifier, 0, 255);

        // Impact particle
        dashParticleEnabled = getConfig().getBoolean("dash-particle-enabled", dashParticleEnabled);
        String dashParticleName = getConfig().getString("dash-particle-type", dashParticle.name());
        try {
            dashParticle = Particle.valueOf(dashParticleName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            getLogger().warning("Invalid dash-particle-type: " + dashParticleName + ", using CLOUD");
            dashParticle = Particle.CLOUD;
        }
        dashParticleCount = getBoundedInt("dash-particle-count", dashParticleCount, 0, MAX_PARTICLE_COUNT);
        dashParticleOffsetX = getBoundedDouble("dash-particle-offset-x", dashParticleOffsetX, 0.0, MAX_PARTICLE_OFFSET);
        dashParticleOffsetY = getBoundedDouble("dash-particle-offset-y", dashParticleOffsetY, 0.0, MAX_PARTICLE_OFFSET);
        dashParticleOffsetZ = getBoundedDouble("dash-particle-offset-z", dashParticleOffsetZ, 0.0, MAX_PARTICLE_OFFSET);
        dashParticleSpeed = getBoundedDouble("dash-particle-speed", dashParticleSpeed, 0.0, MAX_PARTICLE_SPEED);

        // Dash sound
        dashSoundEnabled = getConfig().getBoolean("dash-sound-enabled", dashSoundEnabled);
        String soundName = getConfig().getString("dash-sound-name", dashSound.name());
        try {
            dashSound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            getLogger().warning("Invalid dash-sound-name: " + soundName + ", using ENTITY_PHANTOM_FLAP");
            dashSound = Sound.ENTITY_PHANTOM_FLAP;
        }
        dashSoundVolume = (float) getBoundedDouble("dash-sound-volume", dashSoundVolume, 0.0, 10.0);
        dashSoundPitch = (float) getBoundedDouble("dash-sound-pitch", dashSoundPitch, 0.0, 2.0);

        // Trail
        trailEnabled = getConfig().getBoolean("trail-enabled", trailEnabled);
        String trailName = getConfig().getString("trail-particle-type", trailParticle.name());
        try {
            trailParticle = Particle.valueOf(trailName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            getLogger().warning("Invalid trail-particle-type: " + trailName + ", using CLOUD");
            trailParticle = Particle.CLOUD;
        }
        trailParticleCount = getBoundedInt("trail-particle-count", trailParticleCount, 0, MAX_PARTICLE_COUNT);
        trailOffsetX = getBoundedDouble("trail-offset-x", trailOffsetX, 0.0, MAX_PARTICLE_OFFSET);
        trailOffsetY = getBoundedDouble("trail-offset-y", trailOffsetY, 0.0, MAX_PARTICLE_OFFSET);
        trailOffsetZ = getBoundedDouble("trail-offset-z", trailOffsetZ, 0.0, MAX_PARTICLE_OFFSET);
        trailSpeed = getBoundedDouble("trail-speed", trailSpeed, 0.0, MAX_PARTICLE_SPEED);
        trailDurationTicks = getBoundedInt("trail-duration-ticks", trailDurationTicks, 0, MAX_TRAIL_DURATION_TICKS);
        trailIntervalTicks = getBoundedInt("trail-interval-ticks", trailIntervalTicks, 1, MAX_TRAIL_INTERVAL_TICKS);

        // Double dash
        doubleDashEnabled = getConfig().getBoolean("double-dash.enabled", doubleDashEnabled);
        doubleDashWindowMs = getBoundedLong("double-dash.window-ms", doubleDashWindowMs, 0, MAX_DOUBLE_DASH_WINDOW_MS);
        doubleDashFallImmunityTicks = getBoundedInt("double-dash.fall-immunity-ticks", doubleDashFallImmunityTicks, 0, MAX_FALL_IMMUNITY_TICKS);

        // Tear CustomModelData
        tearCustomModelData = getBoundedInt("tear-custom-model-data", tearCustomModelData, 0, Integer.MAX_VALUE);

        // Celestial Amulet
        celestialAmuletEnabled = getConfig().getBoolean("celestial-amulet.enabled", celestialAmuletEnabled);
        celestialAmuletRecipeEnabled = getConfig().getBoolean("celestial-amulet.recipe-enabled", celestialAmuletRecipeEnabled);
        celestialAmuletUses = getBoundedInt("celestial-amulet.uses", celestialAmuletUses, 1, 64);
        celestialAmuletCooldownMs = getBoundedLong("celestial-amulet.cooldown-seconds",
                celestialAmuletCooldownMs / 1000L, 0, MAX_COOLDOWN_SECONDS) * 1000L;

        // Drop blacklist worlds
        List<String> blacklist = getConfig().getStringList("drop-blacklist-worlds");
        dropBlacklistWorlds.clear();
        for (String name : blacklist) {
            if (name != null && !name.isEmpty()) {
                dropBlacklistWorlds.add(name.toLowerCase());
            }
        }
    }

    private int getBoundedInt(String path, int fallback, int min, int max) {
        int value = getConfig().getInt(path, fallback);
        int bounded = ConfigValueValidator.clamp(value, min, max);
        logClampedValue(path, value, bounded, min, max);
        return bounded;
    }

    private long getBoundedLong(String path, long fallback, long min, long max) {
        long value = getConfig().getLong(path, fallback);
        long bounded = ConfigValueValidator.clamp(value, min, max);
        logClampedValue(path, value, bounded, min, max);
        return bounded;
    }

    private double getBoundedDouble(String path, double fallback, double min, double max) {
        double value = getConfig().getDouble(path, fallback);
        if (!Double.isFinite(value)) {
            getLogger().warning("Invalid value for '" + path + "': " + value + ". Using " + fallback + ".");
            return fallback;
        }
        double bounded = ConfigValueValidator.clamp(value, min, max);
        logClampedValue(path, value, bounded, min, max);
        return bounded;
    }

    private void logClampedValue(String path, Object value, Object bounded, Object min, Object max) {
        if (!value.equals(bounded)) {
            getLogger().warning("Value for '" + path + "' must be between " + min + " and " + max
                    + ". Using " + bounded + ".");
        }
    }

    public void refreshAmuletRecipe() {
        CelestialAmulet.initialize(this, celestialAmuletUses);
        registerAmuletRecipe();
    }

    private void registerAmuletRecipe() {
        if (CelestialAmulet.getRecipeKey() == null) {
            return;
        }
        Bukkit.removeRecipe(CelestialAmulet.getRecipeKey());
        if (!celestialAmuletRecipeEnabled) {
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(CelestialAmulet.getRecipeKey(), CelestialAmulet.create());
        recipe.shape(" T ", "TGT", " T ");
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(TearUtils.createCelestialTear()));
        recipe.setIngredient('G', org.bukkit.Material.NETHERITE_INGOT);
        Bukkit.addRecipe(recipe);
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

    public int getTearCustomModelData() {
        return tearCustomModelData;
    }

    public boolean isCelestialAmuletEnabled() {
        return celestialAmuletEnabled;
    }

    public int getCelestialAmuletUses() {
        return celestialAmuletUses;
    }

    public long getCelestialAmuletCooldownMs() {
        return celestialAmuletCooldownMs;
    }

    public boolean isWorldBlacklistedForDrops(String worldName) {
        if (worldName == null) return false;
        return dropBlacklistWorlds.contains(worldName.toLowerCase());
    }
}
