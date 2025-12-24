package otherspace.game.entities;

import org.joml.Vector2d;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.game.items.Items;

/**
 * Loose rock found on ground.
 */
public class RockPile extends TileEntity {
    public RockPile(Vector2d position) {
        super(position);
    }

    @Override
    public float getHardness() {
        return 0.5f;
    }

    @Override
    public ItemDrop[] returns() {
        return new ItemDrop[] {
                new ItemDrop(new ItemStack(Items.LOOSE_STONE, 1), 1),
                new ItemDrop(new ItemStack(Items.FLINT, 1), 0.2f)
        };
    }
}
