package otherspace.core.engine.world.items;

import otherspace.core.engine.world.entities.TileEntity;

/**
 * Item representation of a tile entity.
 */
public class TileEntityItem extends Item {
    public final Class<? extends TileEntity> MY_ENTITY;

    /**
     * Create a new tile entity item.
     *
     * @param stackSize Stack size of item.
     * @param name      Lang key identifying item name.
     * @param tileEntity Tile entity class associated with this item.
     * @param tags      Array of item tags, or null if no tags should be defined.
     */
    public TileEntityItem(int stackSize, String name, Class<? extends TileEntity> tileEntity, ItemTag[] tags) {
        super(stackSize, name, tags);
        MY_ENTITY = tileEntity;
    }
}
