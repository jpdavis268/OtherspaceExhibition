package otherspace.game.entities;

import org.joml.Vector2d;
import otherspace.core.engine.Color;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.session.Drawer;
import otherspace.game.items.AxeItem;
import otherspace.game.items.Items;

import java.util.Set;

/**
 * Adult oak tree that spawns naturally or grows from a sapling.
 */
public class OakTree extends TileEntity {
    public OakTree(Vector2d position) {
        super(position);
    }

    @Override
    public float getHardness() {
        return 4;
    }

    @Override
    public float getWrongToolPenalty() {
        return 0;
    }

    @Override
    public Set<Class<? extends ToolItem>> getEffectiveTools() {
        return Set.of(AxeItem.class);
    }

    @Override
    public void drawSelf(Drawer d) {
        boolean flip = (Math.floor(position.x) % 2 == Math.floor(position.y) % 2);
        d.drawSpriteExt(getSprite(), (int) (position.x * 32) + (flip ? 1 : 0), (int) (position.y * 32), 0, Color.WHITE, flip ? -1 : 1, 1, spriteFrame);
    }

    @Override
    public ItemDrop[] returns() {
        return new ItemDrop[] {
                new ItemDrop(new ItemStack(Items.OAK_LOG, 4), 1)
        };
    }
}
