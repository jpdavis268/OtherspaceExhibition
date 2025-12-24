package otherspace.core.engine.world.items;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.session.Drawer;

import java.text.NumberFormat;

/**
 * Stack of an item. Represents the actual in game "item" seen in inventories.
 */
public class ItemStack {
    public int itemID;
    public int stackSize;
    // Temporary form of storing stack-specific data such as tool durability.
    // Will be replaced with a more robust system in the future.
    public int state;

    public ItemStack(int itemID, int stackSize, int state) {
        this.itemID = itemID;
        this.stackSize = stackSize;
        this.state = state;
    }

    public ItemStack(int itemID, int stackSize) {
        this(itemID, stackSize, 0);
    }

    public ItemStack() {
        this(0, 0, 0);
    }

    /**
     * Get the item properties of the Item ID this stack contains.
     *
     * @return Item properties of stack's item.
     */
    public Item getItem() {
        return Item.get(itemID);
    }

    /**
     * Check if two item stacks are equivalent. Only checks ID and properties, ignores stack size.
     *
     * @param obj Object (Presumably an item stack).
     * @return Whether item stacks are combinable, or just false if the argument isn't an item stack.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStack s) {
            if ((s.isEmpty() && !isEmpty()) || (!s.isEmpty() && isEmpty())) {
                return false;
            }

            return itemID == s.itemID && state == s.state;
        }
        return false;
    }

    /**
     * Check if this item has a particular tag.
     *
     * @param itemTag Tag to check for.
     * @return Whether this item has the corresponding tag.
     */
    public boolean hasTag(Class<? extends ItemTag> itemTag) {
        return itemTag.isInstance(getItem());
    }

    /**
     * Draw a given item stack at the provided location.
     *
     * @param d Drawer to use.
     * @param x X Position on screen to draw at.
     * @param y Y Position on screen to draw at.
     */
    public void draw(Drawer d, int x, int y) {
        if (!isEmpty()) {
            d.drawSprite(getItem().SPRITE, x + 4, y + 4, 0);

            if (getItem() instanceof ToolItem ti && state < ti.getMaxDurability()) {
                d.drawValueBar(
                        new Rectanglei(x + 2, y + 34, x + 38, y + 38),
                        state,
                        ti.getMaxDurability(),
                        Color.BLACK,
                        Color.RED,
                        Color.GREEN,
                        false,
                        true,
                        false
                );
            }

            if (stackSize > 1) {
                d.setHalign(Drawer.H_RIGHT);
                d.setValign(Drawer.V_BOTTOM);
                d.setColor(Color.WHITE);
                NumberFormat formatter = NumberFormat.getCompactNumberInstance();
                formatter.setMinimumFractionDigits(1);
                d.drawText(x + 38, y + 38, formatter.format(stackSize).toLowerCase());
            }
        }
    }

    /**
     * Create a copy of this item stack.
     *
     * @return Copy of item stack.
     */
    public ItemStack copy() {
        return new ItemStack(itemID, stackSize, state);
    }

    /**
     * Check whether this item stack is empty.
     *
     * @return Whether stack is empty (size < 1).
     */
    public boolean isEmpty() {
        return stackSize < 1;
    }
}
