package com.minico.celestialdash;

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Shared interaction guards for Celestial Dash items.
 */
final class InteractionUtils {

    private InteractionUtils() {
    }

    /**
     * Returns whether a right-click should remain available to the clicked vanilla block.
     *
     * <p>Celestial items must not be consumed when a player is trying to use an
     * interactable block such as a crafting table, chest, door, or button.</p>
     */
    static boolean isRightClickOnInteractableBlock(PlayerInteractEvent event) {
        return event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType().isInteractable();
    }

    /**
     * Returns whether another listener has denied this interaction.
     *
     * <p>The general cancellation flag cannot be used here: Bukkit deliberately
     * marks some right-click-air events as canceled when vanilla predicts no
     * action. A Celestial Tear uses exactly that kind of click. For air
     * interactions, only a denied item use represents a cancellation from
     * another listener. A clicked block has an allowed default result, so a
     * denial of either the block or the item is respected.</p>
     */
    static boolean isDeniedByAnotherListener(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            return event.useItemInHand() == Event.Result.DENY;
        }

        return event.useInteractedBlock() == Event.Result.DENY
                || event.useItemInHand() == Event.Result.DENY;
    }
}
