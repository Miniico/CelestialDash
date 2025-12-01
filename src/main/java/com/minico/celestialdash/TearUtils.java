package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TearUtils {

    private static final String TEAR_NAME = ChatColor.AQUA + "Celestial Tear";

    // CustomModelData from config (0 = disabled)
    private static int customModelData = 0;

    public static void setCustomModelData(int cmd) {
        customModelData = cmd;
    }

    public static String getTearName() {
        return TEAR_NAME;
    }

    public static ItemStack createCelestialTear() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TEAR_NAME);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "A tear formed within a storm cloud,",
                    ChatColor.GRAY + "charged with celestial wind energy."
            ));

            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCelestialTear(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.GHAST_TEAR) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        if (!TEAR_NAME.equals(meta.getDisplayName())) return false;

        if (customModelData > 0) {
            if (!meta.hasCustomModelData()) return false;
            return meta.getCustomModelData() == customModelData;
        }

        return true;
    }

    public static int findTearSlot(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isCelestialTear(item)) {
                return i;
            }
        }
        return -1;
    }

    public static void consumeTear(Player player, int slot) {
        ItemStack tear = player.getInventory().getItem(slot);
        if (tear == null) return;

        if (tear.getAmount() <= 1) {
            player.getInventory().setItem(slot, null);
        } else {
            tear.setAmount(tear.getAmount() - 1);
        }
    }
}
