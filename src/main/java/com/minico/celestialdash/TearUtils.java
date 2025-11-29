package com.minico.celestialdash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TearUtils {

    // Prevent instantiation of utility class
    private TearUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Pattern for hex colors (&#RRGGBB)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Default values (can be overridden by config)
    private static String tearName = ChatColor.AQUA + "Celestial Tear";
    private static List<String> tearLore = new ArrayList<>();
    private static int customModelData = 0;
    private static NamespacedKey tearKey;

    /**
     * Initializes the TearUtils with configuration values
     */
    public static void initialize(CelestialDash plugin, String name, List<String> lore, int modelData) {
        tearName = colorize(name);
        tearLore = new ArrayList<>();
        for (String line : lore) {
            tearLore.add(colorize(line));
        }
        customModelData = modelData;
        tearKey = new NamespacedKey(plugin, "celestial_tear");
    }

    /**
     * Sets the custom model data value
     */
    public static void setCustomModelData(int value) {
        customModelData = value;
    }

    /**
     * Gets the tear name
     */
    public static String getTearName() {
        return tearName;
    }

    /**
     * Creates a Celestial Tear item with proper metadata and PDC
     */
    public static ItemStack createCelestialTear() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item; // Shouldn't happen, but safety first
        }

        // Set display name
        meta.setDisplayName(tearName);

        // Set lore
        if (!tearLore.isEmpty()) {
            meta.setLore(new ArrayList<>(tearLore));
        }

        // Set custom model data if enabled
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        // Add PDC marker for extra security
        if (tearKey != null) {
            meta.getPersistentDataContainer().set(tearKey, PersistentDataType.BYTE, (byte) 1);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Checks if an item is a valid Celestial Tear
     * Uses multiple validation layers for security
     */
    public static boolean isCelestialTear(ItemStack item) {
        if (item == null || item.getType() != Material.GHAST_TEAR) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check display name
        if (!meta.hasDisplayName() || !tearName.equals(meta.getDisplayName())) {
            return false;
        }

        // Check custom model data if configured
        if (customModelData > 0) {
            if (!meta.hasCustomModelData() || meta.getCustomModelData() != customModelData) {
                return false;
            }
        }

        // Check PDC marker (most secure validation)
        if (tearKey != null && meta.getPersistentDataContainer().has(tearKey, PersistentDataType.BYTE)) {
            return true;
        }

        // Fallback: check lore if PDC is not set (for backwards compatibility)
        if (!tearLore.isEmpty() && meta.hasLore()) {
            List<String> itemLore = meta.getLore();
            if (itemLore != null && itemLore.equals(tearLore)) {
                return true;
            }
        }

        // If lore is empty in config, accept items without lore
        return tearLore.isEmpty();
    }

    /**
     * Finds the first slot containing a Celestial Tear in player's inventory
     * Checks main inventory (0-35) AND offhand (40)
     * Returns -1 if no tear is found
     */
    public static int findTearSlot(Player player) {
        PlayerInventory inv = player.getInventory();

        // Check hotbar and main inventory (slots 0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (isCelestialTear(item)) {
                return i;
            }
        }

        // Check offhand (slot 40)
        ItemStack offHand = inv.getItemInOffHand();
        if (isCelestialTear(offHand)) {
            return 40; // Offhand slot
        }

        return -1;
    }

    /**
     * Counts how many Celestial Tears a player has in their inventory
     * Includes offhand
     */
    public static int countTears(Player player) {
        int count = 0;
        PlayerInventory inv = player.getInventory();

        // Count in main inventory
        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (isCelestialTear(item)) {
                count += item.getAmount();
            }
        }

        // Count in offhand
        ItemStack offHand = inv.getItemInOffHand();
        if (isCelestialTear(offHand)) {
            count += offHand.getAmount();
        }

        return count;
    }

    /**
     * Checks if a player has at least one Celestial Tear
     * Searches in inventory AND offhand
     */
    public static boolean hasTear(Player player) {
        PlayerInventory inv = player.getInventory();

        // Check main inventory
        for (int i = 0; i < 36; i++) {
            if (isCelestialTear(inv.getItem(i))) {
                return true;
            }
        }

        // Check offhand
        if (isCelestialTear(inv.getItemInOffHand())) {
            return true;
        }

        return false;
    }

    /**
     * Consumes one Celestial Tear from the specified slot
     * Handles both regular inventory slots and offhand (slot 40)
     * Only consumes if the item is actually a Celestial Tear
     */
    public static boolean consumeTear(Player player, int slot) {
        PlayerInventory inv = player.getInventory();
        ItemStack tear;

        // Check if it's the offhand slot
        if (slot == 40) {
            tear = inv.getItemInOffHand();
        } else {
            tear = inv.getItem(slot);
        }

        if (tear == null || !isCelestialTear(tear)) {
            return false;
        }

        if (tear.getAmount() <= 1) {
            if (slot == 40) {
                inv.setItemInOffHand(null);
            } else {
                inv.setItem(slot, null);
            }
        } else {
            tear.setAmount(tear.getAmount() - 1);
        }

        return true;
    }

    /**
     * Gives a player a specified amount of Celestial Tears
     * Returns the amount that couldn't be added (if inventory is full)
     */
    public static int giveTears(Player player, int amount) {
        int remaining = amount;

        while (remaining > 0) {
            ItemStack tear = createCelestialTear();
            tear.setAmount(Math.min(remaining, 64));

            var leftover = player.getInventory().addItem(tear);

            if (leftover.isEmpty()) {
                remaining -= tear.getAmount();
            } else {
                // Inventory is full, return how many couldn't be added
                int notAdded = leftover.values().stream()
                        .mapToInt(ItemStack::getAmount)
                        .sum();
                return notAdded;
            }
        }

        return 0; // All items added successfully
    }

    /**
     * Translates color codes including hex colors (1.16+)
     * Supports both & codes and &#RRGGBB hex format
     */
    private static String colorize(String input) {
        if (input == null) {
            return "";
        }

        // Translate hex colors
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder magic = new StringBuilder("§x");

            for (char c : hexCode.toCharArray()) {
                magic.append('§').append(c);
            }

            matcher.appendReplacement(buffer, magic.toString());
        }
        matcher.appendTail(buffer);

        // Translate standard & codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}