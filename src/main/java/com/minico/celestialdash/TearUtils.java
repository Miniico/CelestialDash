package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TearUtils {

    private static int customModelData = 0;

    /**
     * Sets the CustomModelData used for Celestial Tears.
     * Called from CelestialDash.loadSettings().
     */
    public static void setCustomModelData(int data) {
        customModelData = data;
    }

    /**
     * Creates a single Celestial Tear item.
     */
    public static ItemStack createCelestialTear() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.AQUA + "Celestial Tear");
        meta.setLore(List.of(
                ChatColor.GRAY + "Forged by storm winds",
                ChatColor.GRAY + "A source of celestial mobility"
        ));

        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates multiple Celestial Tears with the given amount.
     */
    public static ItemStack createCelestialTear(int amount) {
        ItemStack item = createCelestialTear();
        item.setAmount(amount);
        return item;
    }

    /**
     * Returns true if the given item is a Celestial Tear.
     */
    public static boolean isCelestialTear(ItemStack item) {
        if (item == null || item.getType() != Material.GHAST_TEAR) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        // Basic name check
        String stripped = ChatColor.stripColor(meta.getDisplayName());
        if (!"Celestial Tear".equalsIgnoreCase(stripped)) {
            return false;
        }

        // Optional CustomModelData check
        if (customModelData > 0) {
            if (!meta.hasCustomModelData()) {
                return false;
            }
            return meta.getCustomModelData() == customModelData;
        }

        return true;
    }

    /**
     * Finds the first inventory slot containing a Celestial Tear.
     *
     * @return slot index or -1 if none found
     */
    public static int findTearSlot(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isCelestialTear(item)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Consumes one Celestial Tear from the given slot, if valid.
     */
    public static void consumeTear(Player player, int slot) {
        PlayerInventory inv = player.getInventory();
        if (slot < 0 || slot >= inv.getSize()) {
            return;
        }

        ItemStack item = inv.getItem(slot);
        if (!isCelestialTear(item)) {
            return;
        }

        int newAmount = item.getAmount() - 1;
        if (newAmount <= 0) {
            inv.setItem(slot, null);
        } else {
            item.setAmount(newAmount);
            inv.setItem(slot, item);
        }
    }
}
