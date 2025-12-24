package otherspace.core.registry;

import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.TileItem;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Handles the registration of in game items and their required assets.
 */
public class ItemRegistry {
    private static boolean initialized = false;
    private static final HashMap<String, Integer> itemRegistry = new HashMap<>();
    private static LinkedList<Item> registryBuffer = new LinkedList<>();
    private static Item[] itemList;

    /**
     * Register a new item. Must be done at the start of the game.
     *
     * @param item Item to add to registry.
     * @return Item ID of registered item.
     */
    public int register(Item item) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add item after registry phase has been completed!");
        }

        String itemHandle = modHandle + "/" + item.NAME;
        itemRegistry.put(itemHandle, itemRegistry.size());
        registryBuffer.add(item);
        itemSpriteRegistry.register(item.SPRITE);
        item.setMod(modHandle);
        return itemRegistry.size() - 1;
    }

    /**
     * Complete registry of items. Must be done before item data can be queried.
     */
    public static void registerItems() {
        if (initialized) {
            return;
        }

        itemList = new Item[registryBuffer.size()];
        registryBuffer.toArray(itemList);
        registryBuffer = null;
        // Link tile items to their tiles.
        for (Item i : itemList) {
            if (i instanceof TileItem ti) {
                ti.register();
            }
        }

        initialized = true;
    }

    /**
     * Get how many items have been registered.
     *
     * @return Number of registered items.
     */
    public static int getRegistrySize() {
        return itemList.length;
    }

    /**
     * Get the ID of an item using its handle.
     *
     * @param itemHandle Handle of item.
     * @return ID of item.
     */
    public static int getID(String itemHandle) {
        return itemRegistry.get(itemHandle);
    }

    /**
     * Retrieve an item from the item registry.
     *
     * @param id ID of item to get.
     * @return Item with corresponding ID.
     */
    public static Item get(int id) {
        if (id < 0 || id >= itemRegistry.size()) {
            throw new IndexOutOfBoundsException("ERROR: Attempted to query data for nonexistent item ID " + id);
        }

        return itemList[id];
    }

    String modHandle;
    SpriteRegistry itemSpriteRegistry;

    public ItemRegistry(String modHandle) {
        this.modHandle = modHandle;
        this.itemSpriteRegistry = new SpriteRegistry();
    }
}
