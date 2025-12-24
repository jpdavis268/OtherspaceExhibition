package otherspace.game.entities;

import org.joml.Vector2d;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.entities.components.Container;
import otherspace.core.engine.world.items.Inventory;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;

/**
 * Gravestone that holds onto a player's item should they decide to take a dirt nap.
 */
public class Gravestone extends TileEntity {
    public Gravestone(Vector2d position) {
        super(position);
        addComponent(new Container<>(this, new Inventory(40)));
    }

    @Override
    public float getHardness() {
        return 2;
    }

    @Override
    public ItemDrop[] returns() {
        return new ItemDrop[0];
    }

    /**
     * Transfer the entirety of an inventory into this gravestone.
     *
     * @param other Inventory to dump.
     */
    @SuppressWarnings("unchecked")
    public void dumpInventory(Inventory other) {
        Inventory contents = getComponent(Container.class).getInventory(0);
        for (int i = 0; i < other.getSize(); i++) {
            contents.add(other.get(i), position);
            other.set(i, new ItemStack());
        }
    }
}
