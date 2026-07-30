package com.minico.celestialdash;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Immutable snapshot of the active plugin settings.
 *
 * <p>A new snapshot is created whenever the plugin configuration is loaded or reloaded.
 * Event handlers read the current snapshot from {@link CelestialDash}, so future events use
 * reloaded values without retaining mutable configuration state.</p>
 */
public final class PluginSettings {

    private static final long MAX_COOLDOWN_SECONDS = 86_400L;
    private static final long MAX_DOUBLE_DASH_WINDOW_MS = 60_000L;
    private static final int MAX_REGEN_DURATION_SECONDS = 3_600;
    private static final int MAX_PARTICLE_COUNT = 500;
    private static final double MAX_PARTICLE_OFFSET = 10.0;
    private static final double MAX_PARTICLE_SPEED = 10.0;
    private static final int MAX_TRAIL_DURATION_TICKS = 1_200;
    private static final int MAX_TRAIL_INTERVAL_TICKS = 200;
    private static final int MAX_FALL_IMMUNITY_TICKS = 1_200;
    private static final int MAX_GIVE_AMOUNT = 2_304;
    private static final String DEFAULT_RESOURCE_PACK_URL = "https://your-domain/CelestialDash-Resource-Pack.zip";
    private static final String DEFAULT_RESOURCE_PACK_PROMPT = "&bThis server uses the CelestialDash resource pack.";
    private static final String AMULET_PURIFIABLE_EFFECTS_PATH = "celestial-amulet.purifiable-effects";
    private static final Set<PotionEffectType> DEFAULT_PURIFIABLE_EFFECTS = Set.of(
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
    );

    private final DropSettings drops;
    private final DashSettings dash;
    private final int tearCustomModelData;
    private final int giveMaxAmount;
    private final AmuletSettings amulet;
    private final ResourcePackSettings resourcePack;

    private PluginSettings(DropSettings drops,
                           DashSettings dash,
                           int tearCustomModelData,
                           int giveMaxAmount,
                           AmuletSettings amulet,
                           ResourcePackSettings resourcePack) {
        this.drops = drops;
        this.dash = dash;
        this.tearCustomModelData = tearCustomModelData;
        this.giveMaxAmount = giveMaxAmount;
        this.amulet = amulet;
        this.resourcePack = resourcePack;
    }

    public static PluginSettings defaults() {
        return new PluginSettings(
                new DropSettings(0.03, 60_000L, Set.of(), DropDeliveryMode.GROUND),
                new DashSettings(
                        10_000L,
                        1.8,
                        0.4,
                        5 * 20,
                        0,
                        new ParticleSettings(true, Particle.CLOUD, 40, 0.4, 0.5, 0.4, 0.02),
                        new SoundSettings(true, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 0.6f),
                        new TrailSettings(true, Particle.CLOUD, 20, 0.3, 0.4, 0.3, 0.01, 10, 1),
                        new DoubleDashSettings(true, 4_000L, 40, 1.2, 1.1),
                        Set.of()
                ),
                0,
                MAX_GIVE_AMOUNT,
                new AmuletSettings(true, true, 3, 0, 60_000L, DEFAULT_PURIFIABLE_EFFECTS),
                new ResourcePackSettings(
                        false,
                        DEFAULT_RESOURCE_PACK_URL,
                        "",
                        false,
                        DEFAULT_RESOURCE_PACK_PROMPT
                )
        );
    }

    /**
     * Loads a new immutable settings snapshot.
     * Missing gameplay scalar values retain their previous loaded value to preserve reload behavior.
     * Resource pack delivery is always read fresh, so removing that section disables it safely.
     */
    public static PluginSettings load(FileConfiguration config, Logger logger, PluginSettings previous) {
        PluginSettings fallback = previous == null ? defaults() : previous;

        DropSettings previousDrops = fallback.drops;
        DashSettings previousDash = fallback.dash;
        ParticleSettings previousImpactParticle = previousDash.impactParticle();
        SoundSettings previousSound = previousDash.sound();
        TrailSettings previousTrail = previousDash.trail();
        DoubleDashSettings previousDoubleDash = previousDash.doubleDash();
        AmuletSettings previousAmulet = fallback.amulet;

        DropSettings drops = new DropSettings(
                getBoundedDouble(config, logger, "drop-chance-per-second", previousDrops.chance(), 1.0),
                getBoundedLong(config, logger, "drop-cooldown-seconds", previousDrops.cooldownMs() / 1_000L,
                        MAX_COOLDOWN_SECONDS) * 1_000L,
                loadBlacklistedWorlds(config, "drop-blacklist-worlds"),
                getDropDeliveryMode(config, logger)
        );

        DashSettings dash = new DashSettings(
                getBoundedLong(config, logger, "dash-cooldown-seconds", previousDash.cooldownMs() / 1_000L,
                        MAX_COOLDOWN_SECONDS) * 1_000L,
                getBoundedDouble(config, logger, "dash-strength", previousDash.strength(), 10.0),
                getBoundedDouble(config, logger, "dash-vertical-lift", previousDash.lift(), 5.0),
                getBoundedInt(config, logger, "regen-duration-seconds",
                        previousDash.regenerationDurationTicks() / 20, 0, MAX_REGEN_DURATION_SECONDS) * 20,
                getBoundedInt(config, logger, "regen-amplifier", previousDash.regenerationAmplifier(), 0, 255),
                new ParticleSettings(
                        config.getBoolean("dash-particle-enabled", previousImpactParticle.enabled()),
                        getParticle(config, logger, "dash-particle-type", previousImpactParticle.type()),
                        getBoundedInt(config, logger, "dash-particle-count", previousImpactParticle.count(),
                                0, MAX_PARTICLE_COUNT),
                        getBoundedDouble(config, logger, "dash-particle-offset-x", previousImpactParticle.offsetX(),
                                MAX_PARTICLE_OFFSET),
                        getBoundedDouble(config, logger, "dash-particle-offset-y", previousImpactParticle.offsetY(),
                                MAX_PARTICLE_OFFSET),
                        getBoundedDouble(config, logger, "dash-particle-offset-z", previousImpactParticle.offsetZ(),
                                MAX_PARTICLE_OFFSET),
                        getBoundedDouble(config, logger, "dash-particle-speed", previousImpactParticle.speed(),
                                MAX_PARTICLE_SPEED)
                ),
                new SoundSettings(
                        config.getBoolean("dash-sound-enabled", previousSound.enabled()),
                        getDashSound(config, logger, previousSound.sound()),
                        (float) getBoundedDouble(config, logger, "dash-sound-volume", previousSound.volume(), 10.0),
                        (float) getBoundedDouble(config, logger, "dash-sound-pitch", previousSound.pitch(), 2.0)
                ),
                new TrailSettings(
                        config.getBoolean("trail-enabled", previousTrail.enabled()),
                        getParticle(config, logger, "trail-particle-type", previousTrail.particle()),
                        getBoundedInt(config, logger, "trail-particle-count", previousTrail.count(),
                                0, MAX_PARTICLE_COUNT),
                        getBoundedDouble(config, logger, "trail-offset-x", previousTrail.offsetX(), MAX_PARTICLE_OFFSET),
                        getBoundedDouble(config, logger, "trail-offset-y", previousTrail.offsetY(), MAX_PARTICLE_OFFSET),
                        getBoundedDouble(config, logger, "trail-offset-z", previousTrail.offsetZ(), MAX_PARTICLE_OFFSET),
                        getBoundedDouble(config, logger, "trail-speed", previousTrail.speed(), MAX_PARTICLE_SPEED),
                        getBoundedInt(config, logger, "trail-duration-ticks", previousTrail.durationTicks(),
                                0, MAX_TRAIL_DURATION_TICKS),
                        getBoundedInt(config, logger, "trail-interval-ticks", previousTrail.intervalTicks(),
                                1, MAX_TRAIL_INTERVAL_TICKS)
                ),
                new DoubleDashSettings(
                        config.getBoolean("double-dash.enabled", previousDoubleDash.enabled()),
                        getBoundedLong(config, logger, "double-dash.window-ms", previousDoubleDash.windowMs(),
                                MAX_DOUBLE_DASH_WINDOW_MS),
                        getBoundedInt(config, logger, "double-dash.fall-immunity-ticks",
                                previousDoubleDash.fallImmunityTicks(), 0, MAX_FALL_IMMUNITY_TICKS),
                        getBoundedDouble(config, logger, "double-dash.strength-multiplier",
                                previousDoubleDash.strengthMultiplier(), 10.0),
                        getBoundedDouble(config, logger, "double-dash.lift-multiplier",
                                previousDoubleDash.liftMultiplier(), 10.0)
                ),
                loadBlacklistedWorlds(config, "dash-blacklist-worlds")
        );

        int tearCustomModelData = getBoundedInt(config, logger, "tear-custom-model-data",
                fallback.tearCustomModelData, 0, Integer.MAX_VALUE);
        int giveMaxAmount = getBoundedInt(config, logger, "give-max-amount", fallback.giveMaxAmount,
                1, MAX_GIVE_AMOUNT);
        AmuletSettings amulet = new AmuletSettings(
                config.getBoolean("celestial-amulet.enabled", previousAmulet.enabled()),
                config.getBoolean("celestial-amulet.recipe-enabled", previousAmulet.recipeEnabled()),
                getBoundedInt(config, logger, "celestial-amulet.uses", previousAmulet.uses(), 1, 64),
                getBoundedInt(config, logger, "celestial-amulet.custom-model-data",
                        previousAmulet.customModelData(), 0, Integer.MAX_VALUE),
                getBoundedLong(config, logger, "celestial-amulet.cooldown-seconds",
                        previousAmulet.cooldownMs() / 1_000L, MAX_COOLDOWN_SECONDS) * 1_000L,
                loadPurifiableEffects(config, logger)
        );
        ResourcePackSettings resourcePack = new ResourcePackSettings(
                config.getBoolean("resource-pack.enabled", false),
                getString(config, "resource-pack.url", DEFAULT_RESOURCE_PACK_URL),
                getString(config, "resource-pack.sha1", ""),
                config.getBoolean("resource-pack.required", false),
                getString(config, "resource-pack.prompt", DEFAULT_RESOURCE_PACK_PROMPT)
        );

        return new PluginSettings(drops, dash, tearCustomModelData, giveMaxAmount, amulet, resourcePack);
    }

    public DropSettings drops() {
        return drops;
    }

    public DashSettings dash() {
        return dash;
    }

    public int tearCustomModelData() {
        return tearCustomModelData;
    }

    public int giveMaxAmount() {
        return giveMaxAmount;
    }

    public AmuletSettings amulet() {
        return amulet;
    }

    public ResourcePackSettings resourcePack() {
        return resourcePack;
    }

    private static String getString(FileConfiguration config, String path, String fallback) {
        String value = config.getString(path);
        return value == null ? fallback : value;
    }

    private static Set<String> loadBlacklistedWorlds(FileConfiguration config, String path) {
        List<String> names = config.getStringList(path);
        Set<String> worlds = new HashSet<>();
        for (String name : names) {
            if (name != null && !name.isEmpty()) {
                worlds.add(name.toLowerCase(Locale.ROOT));
            }
        }
        return worlds;
    }

    private static DropDeliveryMode getDropDeliveryMode(FileConfiguration config, Logger logger) {
        String value = config.getString("drop-delivery", DropDeliveryMode.GROUND.name());
        try {
            return DropDeliveryMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            logger.warning("Invalid drop-delivery: " + value + ", using GROUND");
            return DropDeliveryMode.GROUND;
        }
    }

    private static Set<PotionEffectType> loadPurifiableEffects(FileConfiguration config, Logger logger) {
        if (!config.contains(AMULET_PURIFIABLE_EFFECTS_PATH)) {
            return DEFAULT_PURIFIABLE_EFFECTS;
        }
        if (!config.isList(AMULET_PURIFIABLE_EFFECTS_PATH)) {
            logger.warning("Value for '" + AMULET_PURIFIABLE_EFFECTS_PATH
                    + "' must be a list of potion effect names. Using the default effects.");
            return DEFAULT_PURIFIABLE_EFFECTS;
        }

        List<?> configuredEffects = config.getList(AMULET_PURIFIABLE_EFFECTS_PATH);
        if (configuredEffects == null) {
            return Set.of();
        }

        Set<PotionEffectType> effects = new HashSet<>();
        for (Object configuredEffect : configuredEffects) {
            if (!(configuredEffect instanceof String effectName) || effectName.isBlank()) {
                logger.warning("Invalid potion effect in '" + AMULET_PURIFIABLE_EFFECTS_PATH + "': "
                        + configuredEffect + ". Ignoring it.");
                continue;
            }

            String normalizedName = effectName.trim().toUpperCase(Locale.ROOT);
            PotionEffectType effect = PotionEffectType.getByName(normalizedName);
            if (effect == null && normalizedName.equals("SLOW")) {
                // Bukkit's established constant is SLOW, while newer registries expose SLOWNESS.
                effect = PotionEffectType.SLOW;
            }
            if (effect == null) {
                logger.warning("Invalid potion effect in '" + AMULET_PURIFIABLE_EFFECTS_PATH + "': "
                        + effectName + ". Ignoring it.");
                continue;
            }
            effects.add(effect);
        }
        return Set.copyOf(effects);
    }

    private static int getBoundedInt(FileConfiguration config,
                                     Logger logger,
                                     String path,
                                     int fallback,
                                     int min,
                                     int max) {
        int value = config.getInt(path, fallback);
        int bounded = ConfigValueValidator.clamp(value, min, max);
        logClampedValue(logger, path, value, bounded, min, max);
        return bounded;
    }

    private static long getBoundedLong(FileConfiguration config,
                                       Logger logger,
                                       String path,
                                       long fallback,
                                       long max) {
        long value = config.getLong(path, fallback);
        long bounded = ConfigValueValidator.clampNonNegative(value, max);
        logClampedValue(logger, path, value, bounded, 0, max);
        return bounded;
    }

    private static double getBoundedDouble(FileConfiguration config,
                                           Logger logger,
                                           String path,
                                           double fallback,
                                           double max) {
        double value = config.getDouble(path, fallback);
        if (!Double.isFinite(value)) {
            logger.warning("Invalid value for '" + path + "': " + value + ". Using " + fallback + ".");
            return fallback;
        }
        double bounded = ConfigValueValidator.clampNonNegative(value, max);
        logClampedValue(logger, path, value, bounded, 0.0, max);
        return bounded;
    }

    private static Particle getParticle(FileConfiguration config,
                                        Logger logger,
                                        String path,
                                        Particle fallback) {
        String value = config.getString(path, fallback.name());
        try {
            return Particle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            logger.warning("Invalid " + path + ": " + value + ", using CLOUD");
            return Particle.CLOUD;
        }
    }

    private static Sound getDashSound(FileConfiguration config, Logger logger, Sound fallback) {
        String value = config.getString("dash-sound-name", fallback.name());
        try {
            return Sound.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            logger.warning("Invalid dash-sound-name: " + value + ", using ENTITY_PHANTOM_FLAP");
            return Sound.ENTITY_PHANTOM_FLAP;
        }
    }

    private static void logClampedValue(Logger logger,
                                        String path,
                                        Object value,
                                        Object bounded,
                                        Object min,
                                        Object max) {
        if (!value.equals(bounded)) {
            logger.warning("Value for '" + path + "' must be between " + min + " and " + max
                    + ". Using " + bounded + ".");
        }
    }

    public enum DropDeliveryMode {
        GROUND,
        INVENTORY
    }

    public record DropSettings(double chance,
                               long cooldownMs,
                               Set<String> blacklistedWorlds,
                               DropDeliveryMode deliveryMode) {

        public DropSettings {
            blacklistedWorlds = Set.copyOf(blacklistedWorlds);
            deliveryMode = deliveryMode == null ? DropDeliveryMode.GROUND : deliveryMode;
        }

        public boolean isWorldBlacklisted(String worldName) {
            return worldName != null && blacklistedWorlds.contains(worldName.toLowerCase(Locale.ROOT));
        }
    }

    public record DashSettings(long cooldownMs,
                               double strength,
                               double lift,
                               int regenerationDurationTicks,
                               int regenerationAmplifier,
                               ParticleSettings impactParticle,
                               SoundSettings sound,
                               TrailSettings trail,
                               DoubleDashSettings doubleDash,
                               Set<String> blacklistedWorlds) {

        public DashSettings {
            blacklistedWorlds = Set.copyOf(blacklistedWorlds);
        }

        public boolean isWorldBlacklisted(String worldName) {
            return worldName != null && blacklistedWorlds.contains(worldName.toLowerCase(Locale.ROOT));
        }
    }

    public record ParticleSettings(boolean enabled,
                                   Particle type,
                                   int count,
                                   double offsetX,
                                   double offsetY,
                                   double offsetZ,
                                   double speed) {
    }

    public record SoundSettings(boolean enabled, Sound sound, float volume, float pitch) {
    }

    public record TrailSettings(boolean enabled,
                                Particle particle,
                                int count,
                                double offsetX,
                                double offsetY,
                                double offsetZ,
                                double speed,
                                int durationTicks,
                                int intervalTicks) {
    }

    public record DoubleDashSettings(boolean enabled,
                                     long windowMs,
                                     int fallImmunityTicks,
                                     double strengthMultiplier,
                                     double liftMultiplier) {
    }

    public record AmuletSettings(boolean enabled,
                                 boolean recipeEnabled,
                                 int uses,
                                 int customModelData,
                                 long cooldownMs,
                                 Set<PotionEffectType> purifiableEffects) {

        public AmuletSettings {
            purifiableEffects = Set.copyOf(purifiableEffects);
        }
    }

    public record ResourcePackSettings(boolean enabled,
                                       String url,
                                       String sha1,
                                       boolean required,
                                       String prompt) {

        public ResourcePackSettings {
            url = url == null ? "" : url.trim();
            sha1 = sha1 == null ? "" : sha1.trim();
            prompt = prompt == null ? "" : prompt;
        }
    }
}
