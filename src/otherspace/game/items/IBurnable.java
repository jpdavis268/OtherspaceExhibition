package otherspace.game.items;

import otherspace.core.engine.world.items.ItemTag;

/**
 * Defines an item that can be used as a fuel source.
 */
public interface IBurnable extends ItemTag {
    /**
     * Get how long this item will burn.
     *
     * @return Amount of time item can act as fuel before being consumed, in seconds.
     */
    float getBurntime();
}
