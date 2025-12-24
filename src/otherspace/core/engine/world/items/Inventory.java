package otherspace.core.engine.world.items;

import com.google.gson.*;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.world.InputHandler;
import otherspace.game.entities.DroppedItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Defines an inventory, which holds a set of items.
 */
public class Inventory {
    private final ItemStack[] contents;

    public Inventory(int slots) {
        contents = new ItemStack[slots];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = new ItemStack();
        }
    }

    /**
     * Add an item to this inventory.
     *
     * @param toAdd ItemStack to add.
     * @param dropCoords Where to drop any items that could not be added.
     * @param isTransfer Whether these items are coming from another inventory (in which case dropCoords is irrelevant).
     * @return How many items could not be added, if any.
     */
    public int add(ItemStack toAdd, Vector2d dropCoords, boolean isTransfer) {
        // Get total number of items and max size.
        int maxsize = toAdd.getItem().MAX_SIZE;
        int items = toAdd.stackSize;
        int remainder = 0;

        // First Pass: Check for equivalent slots with space to spare.
        for (int s = 0; s < contents.length && items > 0; s++) {
            ItemStack existing = contents[s];

            if (existing.equals(toAdd)) {
                // Try to add as much as we can.
                int delta = Math.min(items, maxsize - existing.stackSize);
                existing.stackSize += delta;
                items -= delta;
            }
        }

        // Second Pass: Check for empty slots.
        for (int s = 0; s < contents.length && items > 0; s++) {
            if (contents[s].stackSize <= 0) {
                // Add what we can.
                contents[s] = toAdd.copy();
                contents[s].stackSize = Math.min(items, maxsize);
                items -= maxsize;
            }
        }

        // Handle remainder, if it exists.
        if (items > 0) {
            // If this was not a transfer, drop the items on the ground.
            if (!isTransfer) {
                toAdd.stackSize = items;
                DroppedItem.dropItems(toAdd, dropCoords);
            }
            else {
                remainder += items;
            }
        }

        return remainder;
    }

    /**
     * Add an item to this inventory.
     *
     * @param toAdd ItemStack to add.
     * @param dropCoords Where to drop items that could not be added.
     * @return How many items could not be added, if any.
     */
    public int add(ItemStack toAdd, Vector2d dropCoords) {
        return this.add(toAdd, dropCoords, false);
    }

    /**
     * Remove items from an inventory.
     *
     * @param toSubtract ItemStack to attempt to remove, if it exists.
     */
    public void subtract(ItemStack toSubtract) {
        int items = toSubtract.stackSize;
        for (int i = contents.length - 1; i >= 0; i--) {
            if (contents[i].equals(toSubtract) && !contents[i].isEmpty()) {
                int delta = Math.min(items, contents[i].stackSize);
                items -= delta;
                contents[i].stackSize -= delta;
            }
        }
    }

    /**
     * Swap the contents of two slots in two inventories.
     *
     * @param other Other inventory.
     * @param slot Slot in this inventory.
     * @param otherSlot Slot in other inventory.
     */
    public void swap(Inventory other, int slot, int otherSlot) {
        ItemStack cache = contents[slot];
        contents[slot] = other.contents[otherSlot];
        other.contents[otherSlot] = cache;
    }

    /**
     * Attempt to transfer items from a slot in another inventory to anywhere in this one.
     *
     * @param source Source inventory.
     * @param sourceSlot Slot in source inventory.
     * @param toTransfer Item stack to transfer.
     * @return Number of items that couldn't be transferred, if any.
     */
    public int transferFromSlot(Inventory source, int sourceSlot, ItemStack toTransfer) {
        // This assumes the requested transfer was validated by the calling code.
        int remainder = add(toTransfer, null, true);
        toTransfer.stackSize -= remainder;
        source.contents[sourceSlot].stackSize -= toTransfer.stackSize;
        return remainder;
    }

    /**
     * Attempt to transfer items from a slot in another inventory to a slot in this one.
     *
     * @param source Source inventory.
     * @param sourceSlot Slot in source inventory.
     * @param slot Slot in this inventory.
     * @param count Number of items to try to transfer.
     */
    public void slotTransfer(Inventory source, int sourceSlot, int slot, int count) {
        ItemStack toTransfer = source.contents[sourceSlot];
        ItemStack existing = contents[slot];
        int space = existing.getItem().MAX_SIZE - existing.stackSize;
        int delta = Math.min(count, Math.min(toTransfer.stackSize, space));

        if (toTransfer.equals(existing)) {
            source.contents[sourceSlot].stackSize -= delta;
            contents[slot].stackSize += delta;
        }
        else if (existing.isEmpty()) {
            ItemStack swapped = toTransfer.copy();
            swapped.stackSize = delta;
            source.contents[sourceSlot].stackSize -= delta;
            contents[slot] = swapped;
        }
    }

    /**
     * Get the item stack at a particular index of this inventory.
     *
     * @return Item stack at index.
     */
    public ItemStack get(int index) {
        return contents[index];
    }

    /**
     * Directly set the item stack at a particular index of this inventory.
     *
     * @param index Index of slot to put stack in.
     * @param stack Stack to put in slot.
     */
    public void set(int index, ItemStack stack) {
        contents[index] = stack;
    }

    /**
     * Get the size of this inventory.
     *
     * @return Inventory size.
     */
    public int getSize() {
        return contents.length;
    }

    /**
     * Draw this inventory.
     *
     * @param d Drawer to use.
     * @param location Location on current surface to draw at.
     * @param rows How many rows to divide slots into.
     * @param clear Whether to draw the cells of this inventory, or just the items.
     */
    public void draw(Drawer d, Vector2i location, int rows, boolean clear) {
        int cols = Math.ceilDiv(contents.length, rows);
        int index = 0;

        // Draw cells in a grid.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (index < contents.length) {
                    int cx = location.x + j * 42;
                    int cy = location.y + i * 42;

                    // If this GUI is not transparent, draw the background of the cell.
                    if (!clear) {
                        d.setColor(Color.GRAY);
                        Rectanglei cellArea = new Rectanglei(cx, cy, cx + 40, cy + 40);
                        d.drawRect(cellArea);
                        if (GenUtils.isMouseOver(cellArea)) {
                            InputHandler.setMouseSelection(this, index);
                            d.setColor(new Color(1, 1, 1, 0.5f));
                            d.drawRect(cellArea);
                        }
                    }

                    // Draw item in this cell and how much of it there is.
                    contents[index].draw(d, cx, cy);
                }
                index++;
            }
        }
    }

    /**
     * Check if this inventory has a set of items.
     *
     * @param items Items to look for.
     * @return Whether a sufficient quantity of the items were found.
     */
    public boolean contains(ItemStack[] items) {
        for (ItemStack stack : items) {
            int remainder = stack.stackSize;
            for (ItemStack held : contents) {
                if (held.equals(stack)) {
                    remainder -= held.stackSize;
                }
            }

            if (remainder > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if this inventory has items from a set of classes.
     *
     * @param itemClasses Set of item classes to look for.
     * @return Whether inventory has an instance of every item in this set.
     */
    public <E extends Item> boolean contains(Set<Class<? extends E>> itemClasses) {
        for (Class<? extends E> itemClass : itemClasses) {
            boolean foundInstance = false;
            for (ItemStack stack : contents) {
                if (itemClass.isInstance(stack.getItem()) && !stack.isEmpty()) {
                    foundInstance = true;
                }
            }

            if (!foundInstance) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find an instance of a particular class of item.
     *
     * @param itemClass Class of item to look for.
     * @return Item stack of class if found, or null if no item of that class is contained.
     */
    public ItemStack findInstance(Class<? extends Item> itemClass) {
        for (ItemStack stack : contents) {
            if (itemClass.isInstance(stack.getItem()) && !stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }

    /**
     * Find the first instance of a particular item.
     *
     * @param itemID Item ID to look for.
     * @return First stack if found, or null if the inventory does not have this item.
     */
    public ItemStack findFirst(int itemID) {
        for (ItemStack stack : contents) {
            if (stack.itemID == itemID && !stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }

    /**
     * Get how much of a specified item is in this inventory.
     *
     * @param itemID ID to look for.
     * @return Sum of all stacks of this item.
     */
    public int getSum(int itemID) {
        int sum = 0;
        for (ItemStack stack : contents) {
            if (itemID == stack.itemID) {
                sum += stack.stackSize;
            }
        }
        return sum;
    }

    /**
     * Create a JSON representation of this inventory.
     *
     * @return JSON representation of inventory.
     */
    public JsonObject serialize() {
        JsonObject itemMapping = new JsonObject();
        JsonArray contentArray = new JsonArray(contents.length);
        Gson gson = new Gson();
        for (ItemStack content : contents) {
            Item item = content.getItem();
            if (!itemMapping.has(item.getMod())) {
                itemMapping.add(item.getMod(), new JsonObject());
            }
            JsonObject itemModGroup = itemMapping.getAsJsonObject(item.getMod());
            JsonElement itemID = itemModGroup.get("itemID");
            if (itemID == null) {
                itemModGroup.add(item.NAME, new JsonPrimitive(content.itemID));
            }
            contentArray.add(gson.toJsonTree(content));
        }

        JsonObject out = new JsonObject();
        out.add("map", itemMapping);
        out.add("contents", contentArray);
        return out;
    }

    /**
     * Fill this inventory with data from a JSON object.
     *
     * @param inventoryData JSON object containing inventory data.
     */
    public void deserialize(JsonObject inventoryData) {
        JsonObject itemMapping = inventoryData.getAsJsonObject("map");
        JsonArray contentArray = inventoryData.getAsJsonArray("contents");

        HashMap<Integer, Integer> idMapping = new HashMap<>();
        Map<String, JsonElement> modMap = itemMapping.asMap();
        for (String mod : modMap.keySet()) {
            Map<String, JsonElement> items = modMap.get(mod).getAsJsonObject().asMap();
            for (String item : items.keySet()) {
                String itemHandle = mod + "/" + item;
                int savedID = items.get(item).getAsInt();
                int sessionID = Item.getID(itemHandle);
                idMapping.put(savedID, sessionID);
            }
        }

        for (int i = 0; i < contentArray.size(); i++) {
            JsonObject oldStack = contentArray.get(i).getAsJsonObject();
            int oldStackID = oldStack.get("itemID").getAsInt();
            int stackSize = oldStack.get("stackSize").getAsInt();
            int state = oldStack.get("state").getAsInt();
            if (idMapping.containsKey(oldStackID)) {
                contents[i] = new ItemStack(idMapping.get(oldStackID), stackSize, state);
            }
        }
    }
}
