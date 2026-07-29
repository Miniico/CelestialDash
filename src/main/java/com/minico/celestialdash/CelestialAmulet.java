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

/**
 * Creates and identifies the craftable Celestial Amulet.
 */
public final class CelestialAmulet {

    private static NamespacedKey amuletKey;
    private static NamespacedKey usesKey;
    private static NamespacedKey instanceKey;
    private static NamespacedKey recipeKey;
    private static int maxUses = 3;

    private CelestialAmulet() {
    }

    public static void initialize(JavaPlugin plugin, int configuredMaxUses) {
        amuletKey = new NamespacedKey(plugin, "celestial_amulet");
        usesKey = new NamespacedKey(plugin, "celestial_amulet_uses");
        instanceKey = new NamespacedKey(plugin, "celestial_amulet_instance");
        recipeKey = new NamespacedKey(plugin, "celestial_amulet_recipe");
        maxUses = configuredMaxUses;
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        if (meta == null || amuletKey == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.AQUA + "Celestial Amulet");
        updateLore(meta, maxUses);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(amuletKey, PersistentDataType.BYTE, (byte) 1);
        data.set(usesKey, PersistentDataType.INTEGER, maxUses);
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
     * Consumes one use and updates the visible lore. The caller removes the item
     * from the inventory when this method returns zero.
     */
    public static int consumeUse(ItemStack item) {
        int remaining = getRemainingUses(item);
        if (remaining <= 0) {
            return 0;
        }

        remaining--;
        if (remaining == 0) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, remaining);
        updateLore(meta, remaining);
        item.setItemMeta(meta);
        return remaining;
    }

    public static NamespacedKey getRecipeKey() {
        return recipeKey;
    }

    private static void updateLore(ItemMeta meta, int remainingUses) {
        meta.setLore(List.of(
                ChatColor.GRAY + "Purifies harmful effects",
                ChatColor.GRAY + "and extinguishes fire.",
                ChatColor.DARK_AQUA + "Uses: " + ChatColor.AQUA + remainingUses + "/" + maxUses
        ));
    }
}
