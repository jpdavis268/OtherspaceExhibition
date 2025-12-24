package otherspace.core.engine.world.entities.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.engine.world.items.Inventory;

/**
 * Holds one or more inventories within an entity.
 *
 * @param <E> Parent entity.
 */
public class Container<E extends Entity> extends EntityComponent<E> {
    private final Inventory[] inventories;

    public Container(E parent, Inventory... inventories) {
        super(parent);
        this.inventories = inventories;
    }

    /**
     * Get how many inventories this container holds.
     *
     * @return Inventory count.
     */
    public int getInventoryCount() {
        return inventories.length;
    }

    /**
     * Get a specific inventory within this container.
     *
     * @param index Index of inventory.
     * @return Inventory at index.
     */
    public Inventory getInventory(int index) {
        return inventories[index];
    }

    @Override
    public JsonObject serialize() {
        JsonArray invArray = new JsonArray();
        for (Inventory inv : inventories) {
            invArray.add(inv.serialize());
        }

        JsonObject serial = new JsonObject();
        serial.add("inventories", invArray);
        return serial;
    }

    @Override
    public void deserialize(JsonObject json) {
        JsonArray invArray = json.getAsJsonArray("inventories");
        for (int i = 0; i < invArray.size(); i++) {
            inventories[i].deserialize(invArray.get(i).getAsJsonObject());
        }
    }
}
