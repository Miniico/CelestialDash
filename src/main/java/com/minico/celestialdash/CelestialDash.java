package com.minico.celestialdash;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CelestialDash extends JavaPlugin {

    private PluginSettings settings = PluginSettings.defaults();

    private Messages messages;
    private DashHandler dashHandler;
    private DropHandler dropHandler;
    private AmuletHandler amuletHandler;
    private ResourcePackHandler resourcePackHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        TearUtils.initialize(this, settings.tearCustomModelData());
        initializeAmulet();

        messages = new Messages(this);
        messages.reload();

        dashHandler = new DashHandler(this, messages);
        dropHandler = new DropHandler(this);
        amuletHandler = new AmuletHandler(this, messages);
        resourcePackHandler = new ResourcePackHandler(this);

        Bukkit.getPluginManager().registerEvents(dashHandler, this);
        Bukkit.getPluginManager().registerEvents(dropHandler, this);
        Bukkit.getPluginManager().registerEvents(amuletHandler, this);
        Bukkit.getPluginManager().registerEvents(resourcePackHandler, this);
        registerAmuletRecipe();

        PluginCommand command = getCommand("celestialdash");
        if (command != null) {
            command.setExecutor(new CelestialCommand(this, messages));
            command.setTabCompleter(new CelestialTabCompleter());
        } else {
            getLogger().severe("Command 'celestialdash' is not defined in plugin.yml!");
        }

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
        if (resourcePackHandler != null) {
            resourcePackHandler.stop();
        }
        getLogger().info("CelestialDash disabled.");
    }

    public void loadSettings() {
        settings = PluginSettings.load(getConfig(), getLogger(), settings);
        warnAboutMissingV116Configuration();
    }

    public void refreshAmuletRecipe() {
        initializeAmulet();
        registerAmuletRecipe();
    }

    private void initializeAmulet() {
        PluginSettings.AmuletSettings amulet = settings.amulet();
        CelestialAmulet.initialize(this, amulet.uses(), amulet.customModelData());
    }

    private void registerAmuletRecipe() {
        if (CelestialAmulet.getRecipeKey() == null) {
            return;
        }

        Bukkit.removeRecipe(CelestialAmulet.getRecipeKey());
        if (!settings.amulet().recipeEnabled()) {
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(CelestialAmulet.getRecipeKey(), CelestialAmulet.create());
        recipe.shape(" T ", "TGT", " T ");
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(TearUtils.createCelestialTear()));
        recipe.setIngredient('G', org.bukkit.Material.NETHERITE_INGOT);
        Bukkit.addRecipe(recipe);
    }

    public Messages getMessages() {
        return messages;
    }

    public DashHandler getDashHandler() {
        return dashHandler;
    }

    public ResourcePackHandler getResourcePackHandler() {
        return resourcePackHandler;
    }

    public PluginSettings getSettings() {
        return settings;
    }

    private void warnAboutMissingV116Configuration() {
        if (!getConfig().isConfigurationSection("resource-pack")) {
            getLogger().warning("Configuration is missing the 'resource-pack' section. "
                    + "Resource-pack delivery is disabled until the v1.1.6 block is added.");
        }
        if (!getConfig().contains("tear-custom-model-data")) {
            getLogger().warning("Configuration is missing 'tear-custom-model-data'. "
                    + "Add the v1.1.6 value before enabling the bundled resource pack.");
        }
        if (!getConfig().contains("celestial-amulet.custom-model-data")) {
            getLogger().warning("Configuration is missing 'celestial-amulet.custom-model-data'. "
                    + "Add the v1.1.6 value before enabling the bundled resource pack.");
        }
    }

    // Legacy configuration accessors retained for source and binary compatibility.

    @Deprecated
    public double getDropChance() {
        return settings.drops().chance();
    }

    @Deprecated
    public long getDropCooldownMs() {
        return settings.drops().cooldownMs();
    }

    @Deprecated
    public long getDashCooldownMs() {
        return settings.dash().cooldownMs();
    }

    @Deprecated
    public double getDashStrength() {
        return settings.dash().strength();
    }

    @Deprecated
    public double getDashLift() {
        return settings.dash().lift();
    }

    @Deprecated
    public int getRegenDurationTicks() {
        return settings.dash().regenerationDurationTicks();
    }

    @Deprecated
    public int getRegenAmplifier() {
        return settings.dash().regenerationAmplifier();
    }

    @Deprecated
    public boolean isDashParticleEnabled() {
        return settings.dash().impactParticle().enabled();
    }

    @Deprecated
    public Particle getDashParticle() {
        return settings.dash().impactParticle().type();
    }

    @Deprecated
    public int getDashParticleCount() {
        return settings.dash().impactParticle().count();
    }

    @Deprecated
    public double getDashParticleOffsetX() {
        return settings.dash().impactParticle().offsetX();
    }

    @Deprecated
    public double getDashParticleOffsetY() {
        return settings.dash().impactParticle().offsetY();
    }

    @Deprecated
    public double getDashParticleOffsetZ() {
        return settings.dash().impactParticle().offsetZ();
    }

    @Deprecated
    public double getDashParticleSpeed() {
        return settings.dash().impactParticle().speed();
    }

    @Deprecated
    public boolean isDashSoundEnabled() {
        return settings.dash().sound().enabled();
    }

    @Deprecated
    public Sound getDashSound() {
        return settings.dash().sound().sound();
    }

    @Deprecated
    public float getDashSoundVolume() {
        return settings.dash().sound().volume();
    }

    @Deprecated
    public float getDashSoundPitch() {
        return settings.dash().sound().pitch();
    }

    @Deprecated
    public boolean isTrailEnabled() {
        return settings.dash().trail().enabled();
    }

    @Deprecated
    public Particle getTrailParticle() {
        return settings.dash().trail().particle();
    }

    @Deprecated
    public int getTrailParticleCount() {
        return settings.dash().trail().count();
    }

    @Deprecated
    public double getTrailOffsetX() {
        return settings.dash().trail().offsetX();
    }

    @Deprecated
    public double getTrailOffsetY() {
        return settings.dash().trail().offsetY();
    }

    @Deprecated
    public double getTrailOffsetZ() {
        return settings.dash().trail().offsetZ();
    }

    @Deprecated
    public double getTrailSpeed() {
        return settings.dash().trail().speed();
    }

    @Deprecated
    public int getTrailDurationTicks() {
        return settings.dash().trail().durationTicks();
    }

    @Deprecated
    public int getTrailIntervalTicks() {
        return settings.dash().trail().intervalTicks();
    }

    @Deprecated
    public boolean isDoubleDashEnabled() {
        return settings.dash().doubleDash().enabled();
    }

    @Deprecated
    public long getDoubleDashWindowMs() {
        return settings.dash().doubleDash().windowMs();
    }

    @Deprecated
    public int getDoubleDashFallImmunityTicks() {
        return settings.dash().doubleDash().fallImmunityTicks();
    }

    @Deprecated
    public int getTearCustomModelData() {
        return settings.tearCustomModelData();
    }

    @Deprecated
    public int getGiveMaxAmount() {
        return settings.giveMaxAmount();
    }

    @Deprecated
    public boolean isCelestialAmuletEnabled() {
        return settings.amulet().enabled();
    }

    @Deprecated
    public int getCelestialAmuletUses() {
        return settings.amulet().uses();
    }

    @Deprecated
    public long getCelestialAmuletCooldownMs() {
        return settings.amulet().cooldownMs();
    }

    @Deprecated
    public boolean isWorldBlacklistedForDrops(String worldName) {
        return settings.drops().isWorldBlacklisted(worldName);
    }
}
