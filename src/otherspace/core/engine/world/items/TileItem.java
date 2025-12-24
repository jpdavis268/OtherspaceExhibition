package otherspace.core.engine.world.items;

import otherspace.core.registry.TileRegistry;

/**
 * Item representation of a wall and/or floor tile.
 */
public class TileItem extends Item {
    private int wallTileID;
    private int floorTileID;

    private String tempWallID;
    private String tempFloorID;

    /**
     * Create a new tile item.
     *
     * @param stackSize Stack size of item.
     * @param name      Lang key identifying item name.
     * @param wallTileID Handle of wall tile associated with this item, or null if nothing is.
     * @param floorTileID Handle of floor tile associated with this item, or null if nothing is.
     * @param tags      Array of item tags, or null if no tags should be defined.
     */
    public TileItem(int stackSize, String name, String wallTileID, String floorTileID, ItemTag[] tags) {
        super(stackSize, name, tags);
        tempWallID = wallTileID;
        tempFloorID = floorTileID;
    }

    public void register() {
        wallTileID = tempWallID == null ? TileRegistry.NULL : TileRegistry.getWallID(tempWallID);
        floorTileID = tempFloorID == null ? TileRegistry.NULL : TileRegistry.getFloorID(tempFloorID);
        tempFloorID = null;
        tempWallID = null;
    }

    /**
     * Create a new tile item with no corresponding floor tile.
     *
     * @param stackSize Stack size of item.
     * @param name      Lang key identifying item name.
     * @param wallTileID ID of wall tile associated with this item (-2 if none).
     * @param tags      Array of item tags, or null if no tags should be defined.
     */
    public TileItem(int stackSize, String name, String wallTileID, ItemTag[] tags) {
        this(stackSize, name, wallTileID, null, tags);
    }

    public int getWallID() {
        return wallTileID;
    }

    public int getFloorID() {
        return floorTileID;
    }
}
