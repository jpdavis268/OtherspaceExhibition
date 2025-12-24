package otherspace.core.engine.world.tiles;

/**
 * Defines how the engine will render a tile based on its surroundings.
 */
public interface AutoTileStrategy {
    /**
     * Adjust bitmask of this tile and surrounding tiles to create auto tile pattern.
     * NOTE: THE TILEMAP RENDERER WILL ALWAYS USE THE RIGHTMOST BIT OF THE MASK TO DETERMINE HORIZONTAL FLIP.
     *
     * @param base Tile ID on server.
     * @param x X Position of tile in world.
     * @param y Y Position of tile in world.
     * @param recursive Whether this has been called recursively (SET THIS TO FALSE).
     */
    void autoTile(int base, double x, double y, boolean recursive);

    /**
     * Get the frame for this tile using the data bitmask.
     *
     * @param state Bitmask flags, use to determine which tile frame should be used.
     */
    int draw(int state);
}
