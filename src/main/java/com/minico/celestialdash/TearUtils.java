package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Utility methods for working with Celestial Tears.
 *
 * Responsibilities:
 *  - Create Celestial Tear items
 *  - Identify whether an ItemStack is a Celestial Tear
 *  - Count and consume tears from player inventories
 *  - Handle optional CustomModelData support
 */
public class TearUtils {

    /**
     * CustomModelData value used for Celestial Tears.
     * <p>
     * 0 means "disabled": no CustomModelData check will be applied.
     */
    private static int customModelData = 0;
    private static NamespacedKey celestialTearKey;

    /**
     * Initializes the item marker and CustomModelData used for Celestial Tears.
     * Called after the plugin settings are loaded and after a configuration reload.
     *
     * @param data CustomModelData value (0 disables the check)
     */
    public static void initialize(JavaPlugin plugin, int data) {
        celestialTearKey = new NamespacedKey(plugin, "celestial_tear");
        customModelData = data;
    }

    /**
     * Creates a single Celestial Tear with all metadata applied.
     *
     * @return new ItemStack representing 1 Celestial Tear
     */
    public static ItemStack createCelestialTear() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            // Extremely rare but safe-guard: return raw tear without meta.
            return item;
        }

        // Display name and lore for identification and resource pack hints.
        meta.setDisplayName(ChatColor.AQUA + "Celestial Tear");
        meta.setLore(List.of(
                ChatColor.GRAY + "Forged by storm winds",
                ChatColor.GRAY + "A source of celestial mobility"
        ));

        // Optional CustomModelData for resource-pack integration.
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        meta.getPersistentDataContainer().set(
                celestialTearKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates multiple Celestial Tears with a given amount.
     *
     * @param amount stack size
     * @return ItemStack with the specified amount
     */
    public static ItemStack createCelestialTear(int amount) {
        ItemStack item = createCelestialTear();
        item.setAmount(amount);
        return item;
    }

    /**
     * Checks whether an ItemStack is considered a Celestial Tear.
     * This method is intentionally strict so we don't accidentally match
     * unrelated ghast tears from other plugins.
     *
     * Conditions:
     *  - Material must be GHAST_TEAR
     *  - Must contain this plugin's PersistentDataContainer marker
     *  - If CustomModelData is enabled (>0), item must have the same value
     *
     * @param item ItemStack to inspect
     * @return true if the stack is a Celestial Tear
     */
    public static boolean isCelestialTear(ItemStack item) {
        if (item == null || item.getType() != Material.GHAST_TEAR) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || celestialTearKey == null) {
            return false;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        Byte marker = data.get(celestialTearKey, PersistentDataType.BYTE);
        if (marker == null || marker != (byte) 1) {
            return false;
        }

        // Optional CustomModelData check for resource packs.
        if (customModelData > 0) {
            if (!meta.hasCustomModelData()) {
                return false;
            }
            return meta.getCustomModelData() == customModelData;
        }

        // No CustomModelData requirement -> the persistent marker is enough.
        return true;
    }

    /**
     * Counts how many Celestial Tears the player has in their inventory.
     * Used by PlaceholderAPI ( %celestialdash_tears% ).
     *
     * @param player player whose inventory will be scanned
     * @return total number of Celestial Tears across all slots
     */
    public static int countTears(Player player) {
        int count = 0;
        PlayerInventory inv = player.getInventory();

        for (ItemStack item : inv.getContents()) {
            if (isCelestialTear(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Finds the first inventory slot containing a Celestial Tear.
     *
     * @param player player whose inventory will be scanned
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
     * Consumes one Celestial Tear from a given slot, if it still contains a valid tear.
     * <p>
     * If the amount reaches 0, the slot is cleared (set to null).
     *
     * @param player player whose inventory will be modified
     * @param slot   inventory index to consume from
     */
    public static void consumeTear(Player player, int slot) {
        PlayerInventory inv = player.getInventory();
        if (slot < 0 || slot >= inv.getSize()) {
            return;
        }

        ItemStack item = inv.getItem(slot);
        if (!isCelestialTear(item)) {
            // Slot was modified or no longer contains a Celestial Tear.
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
