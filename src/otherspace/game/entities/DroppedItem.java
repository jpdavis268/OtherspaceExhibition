package otherspace.game.entities;

import org.joml.Vector2d;
import org.joml.primitives.Rectangled;
import otherspace.core.engine.Color;
import otherspace.core.engine.Sprite;
import otherspace.core.engine.utils.CollisionUtils;
import otherspace.core.engine.world.entities.MobileEntity;
import otherspace.core.engine.world.entities.components.Container;
import otherspace.core.engine.world.items.Inventory;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.session.Drawer;

import java.util.HashSet;
import java.util.Random;

import static otherspace.core.session.Drawer.pos;

/**
 * Entity representation of an item in the world.
 */
public class DroppedItem extends MobileEntity {
    public DroppedItem(Vector2d position, ItemStack myItem) {
        super(position);
        Inventory itemInv = new Inventory(1);
        itemInv.set(0, myItem);
        addComponent(new Container<>(this, itemInv));

        // Look for nearby items of the same type and try to combine if one is found.
        HashSet<DroppedItem> nearby = CollisionUtils.collisionRectList(CollisionUtils.getNearbyEntities(position), DroppedItem.class, new Rectangled(position.x - 1, position.y - 1, position.x + 1, position.y + 1));
        for (DroppedItem d : nearby) {
            ItemStack otherItem = d.getMyItem();
            if (otherItem.equals(myItem)) {
                int myCapacity = myItem.getItem().MAX_SIZE - myItem.stackSize;
                int otherCapacity = otherItem.getItem().MAX_SIZE - otherItem.stackSize;
                if (myCapacity > 0 && otherCapacity > 0) {
                    // If a transfer can happen, attempt it.
                    int amount = Math.min(myCapacity, otherItem.stackSize);
                    myItem.stackSize += amount;
                    otherItem.stackSize -= amount;
                }
            }
        }
    }

    public DroppedItem(Vector2d position) {
        this(position, new ItemStack());
    }

    @Override
    public void update() {
        super.update();
        // If stack is empty, destroy it.
        if (getMyItem().isEmpty()) {
            destroy();
        }
    }

    @Override
    public void drawSelf(Drawer d) {
        ItemStack myItem = getMyItem();
        Sprite sprite = myItem.getItem().SPRITE;

        if (myItem.stackSize == 1) {
            d.drawSpriteExt(sprite, pos(position.x - 0.25), pos(position.y - 0.25), 0, Color.WHITE, 0.5f, 0.5f, 0);
        }
        else if (myItem.stackSize == 2) {
            d.drawSpriteExt(sprite, pos(position.x - 0.125), pos(position.y - 0.25), 0, Color.WHITE, 0.5f, 0.5f, 0);
            d.drawSpriteExt(sprite, pos(position.x - 0.375), pos(position.y - 0.25), 0, Color.WHITE, 0.5f, 0.5f, 0);
        }
        else if (myItem.stackSize >= 3) {
            d.drawSpriteExt(sprite, pos(position.x - 0.25), pos(position.y - 0.125), 0, Color.WHITE, 0.5f, 0.5f, 0);
            d.drawSpriteExt(sprite, pos(position.x - 0.125), pos(position.y - 0.375), 0, Color.WHITE, 0.5f, 0.5f, 0);
            d.drawSpriteExt(sprite, pos(position.x - 0.375), pos(position.y - 0.375), 0, Color.WHITE, 0.5f, 0.5f, 0);
        }
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    /**
     * Spawn dropped items.
     *
     * @param toDrop Item stack to drop.
     * @param dropCoords Location to drop items
     */
    public static void dropItems(ItemStack toDrop, Vector2d dropCoords) {
        int maxSize = toDrop.getItem().MAX_SIZE;
        int stacks = Math.ceilDiv(toDrop.stackSize, maxSize);
        int items = toDrop.stackSize;

        Random scatter = new Random();
        for (int i = 0; i < stacks && items > 0; i++) {
            double x = dropCoords.x + scatter.nextDouble(1) - 0.5;
            double y = dropCoords.y + scatter.nextDouble(1) - 0.5;

            ItemStack dropped = toDrop.copy();
            dropped.stackSize = Math.min(items, maxSize);

            new DroppedItem(new Vector2d(x, y), dropped);

            items -= Math.min(items, maxSize);
        }
    }

    /**
     * Get the stack held in this item.
     *
     * @return Item stack represented by this stack of items.
     */
    public ItemStack getMyItem() {
        return getComponent(Container.class).getInventory(0).get(0);
    }
}
