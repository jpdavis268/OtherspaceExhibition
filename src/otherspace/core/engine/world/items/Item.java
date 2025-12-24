package otherspace.core.engine.world.items;

import org.joml.Vector2i;
import otherspace.core.engine.Sprite;
import otherspace.core.registry.ItemRegistry;

/**
 * Base class for in game items. Not to be confused with an item stack, which is its in game representation.
 */
public class Item implements ItemTag {
    /**
     * Get an item using its ID.
     *
     * @param id ID of item.
     * @return Item with corresponding ID.
     */
    public static Item get(int id) {
        return ItemRegistry.get(id);
    }

    /**
     * Get item using its handle ("modHandle/itemName").
     * @param itemHandle Handle of item.
     * @return Item with corresponding handle.
     */
    public static int getID(String itemHandle) {
        return ItemRegistry.getID(itemHandle);
    }

    private String mod;
    public final int MAX_SIZE;
    public final String NAME;
    public final Sprite SPRITE;
    private final ItemTag[] TAGS;

    /**
     * Create a new item.
     *
     * @param stackSize Stack size of item.
     * @param name Lang key identifying item name.
     * @param tags Array of item tags, or null if no tags should be defined.
     */
    public Item(int stackSize, String name, ItemTag[] tags) {
        MAX_SIZE = stackSize;
        NAME = name;
        // TODO: Need some way to search for assets automatically in mod folders.
        // TODO: Also, there should probably be a way to support animated sprites here.
        SPRITE = new Sprite("resources/sprites/items/" + name + ".png", 1, new Vector2i());
        TAGS = tags;
    }

    /**
     * Attempt to retrieve a tag from this property set.
     *
     * @param tagType Tag to attempt to retrieve.
     * @return Tag, or null if this item does not have it.
     */
    @SuppressWarnings("unchecked")
    public <E> E getTag(Class<E> tagType) {
        if (TAGS == null) {
            return null;
        }

        for (ItemTag t : TAGS) {
            if (tagType.isInstance(t)) {
                return (E) t;
            }
        }

        return null;
    }

    /**
     * Get the mod this item is from.
     *
     * @return Item mod.
     */
    public String getMod() {
        return mod;
    }

    /**
     * Set the mod this item is from if it has not yet been defined.
     *
     * @param mod Item mod.
     */
    public void setMod(String mod) {
        if (this.mod == null) {
            this.mod = mod;
        }
    }
}
