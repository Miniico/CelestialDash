package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates and identifies the craftable Celestial Amulet.
 */
public final class CelestialAmulet {

    private static NamespacedKey amuletKey;
    private static NamespacedKey usesKey;
    private static NamespacedKey maxUsesKey;
    private static NamespacedKey instanceKey;
    private static NamespacedKey recipeKey;
    private static int maxUses = 3;
    private static int customModelData = 0;
    private static final Pattern LEGACY_USES = Pattern.compile("^Uses:\\s*(\\d+)\\s*/\\s*(\\d+)$");

    private CelestialAmulet() {
    }

    public static void initialize(JavaPlugin plugin, int configuredMaxUses, int configuredCustomModelData) {
        amuletKey = new NamespacedKey(plugin, "celestial_amulet");
        usesKey = new NamespacedKey(plugin, "celestial_amulet_uses");
        maxUsesKey = new NamespacedKey(plugin, "celestial_amulet_max_uses");
        instanceKey = new NamespacedKey(plugin, "celestial_amulet_instance");
        recipeKey = new NamespacedKey(plugin, "celestial_amulet_recipe");
        maxUses = Math.max(1, configuredMaxUses);
        customModelData = configuredCustomModelData;
    }

    @SuppressWarnings("deprecation") // Keeps item metadata compatible with the supported server range.
    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        if (meta == null || amuletKey == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.AQUA + "Celestial Amulet");
        updateLore(meta, maxUses, maxUses);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Optional CustomModelData for the bundled resource pack.
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(amuletKey, PersistentDataType.BYTE, (byte) 1);
        data.set(usesKey, PersistentDataType.INTEGER, maxUses);
        data.set(maxUsesKey, PersistentDataType.INTEGER, maxUses);
        // A distinct marker prevents individually crafted amulets from stacking.
        data.set(instanceKey, PersistentDataType.STRING, UUID.randomUUID().toString());

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isCelestialAmulet(ItemStack item) {
        if (item == null || item.getType() != Material.NAUTILUS_SHELL || !item.hasItemMeta() || amuletKey == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        Byte marker = meta.getPersistentDataContainer().get(amuletKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    public static int getRemainingUses(ItemStack item) {
        if (!isCelestialAmulet(item)) {
            return 0;
        }
        Integer uses = item.getItemMeta().getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);
        return uses == null ? 0 : Math.max(uses, 0);
    }

    /**
     * Returns this amulet's original capacity, independent of later configuration
     * changes. Legacy items recover it from their visible charge denominator.
     */
    @SuppressWarnings("deprecation") // Reads legacy lore from supported server versions.
    public static int getMaxUses(ItemStack item) {
        if (!isCelestialAmulet(item)) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        int remaining = getRemainingUses(item);
        Integer capacity = meta.getPersistentDataContainer().get(maxUsesKey, PersistentDataType.INTEGER);
        if (capacity != null && capacity > 0 && capacity >= remaining) {
            return capacity;
        }
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (String line : lore) {
                if (line == null) {
                    continue;
                }
                Matcher matcher = LEGACY_USES.matcher(ChatColor.stripColor(line).trim());
                if (!matcher.matches()) {
                    continue;
                }
                try {
                    int shownRemaining = Integer.parseInt(matcher.group(1));
                    int shownCapacity = Integer.parseInt(matcher.group(2));
                    if (shownCapacity > 0 && shownCapacity >= remaining && shownRemaining <= shownCapacity) {
                        return shownCapacity;
                    }
                } catch (NumberFormatException ignored) {
                    // Malformed or overflowing legacy values use the safe fallback below.
                }
            }
        }
        // Never show fewer total charges than the item still owns.
        return Math.max(maxUses, remaining);
    }

    /**
     * Consumes one use and updates the visible lore. The caller removes the item
     * from the inventory when this method returns zero.
     */
    public static int consumeUse(ItemStack item) {
        int remaining = getRemainingUses(item);
        if (remaining <= 0) {
            return 0;
        }

        int capacity = getMaxUses(item);
        remaining--;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, remaining);
        meta.getPersistentDataContainer().set(maxUsesKey, PersistentDataType.INTEGER, capacity);
        updateLore(meta, remaining, capacity);
        item.setItemMeta(meta);
        return remaining;
    }

    public static NamespacedKey getRecipeKey() {
        return recipeKey;
    }

    @SuppressWarnings("deprecation") // Keeps item metadata compatible with the supported server range.
    private static void updateLore(ItemMeta meta, int remainingUses, int capacity) {
        meta.setLore(List.of(
                ChatColor.GRAY + "Purifies harmful effects",
                ChatColor.GRAY + "and extinguishes fire.",
                ChatColor.DARK_AQUA + "Uses: " + ChatColor.AQUA + remainingUses + "/" + capacity
        ));
    }
}
