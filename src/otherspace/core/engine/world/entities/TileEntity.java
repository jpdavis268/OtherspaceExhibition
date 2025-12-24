package otherspace.core.engine.world.entities;

import org.joml.Vector2d;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;

import java.util.Set;

/**
 * Super class for static entities.
 */
public abstract class TileEntity extends Entity {
    public TileEntity(Vector2d position) {
        super(position);
    }

    /**
     * Retrieve the hardness of this tile entity.
     *
     * @return Tile entity hardness.
     */
    public abstract float getHardness();

    /**
     * Return the set of tool types that are effective on this tile entity.
     *
     * @return Set of effective tools.
     */
    public Set<Class<? extends ToolItem>> getEffectiveTools() {
        return null;
    }

    /**
     * Get how much slower mining should be if the player is using the wrong tool.
     *
     * @return What value to multiply the player's mining speed by.
     */
    public float getWrongToolPenalty() {
        return 1;
    }

    /**
     * Defines what this tile entity returns when broken.
     *
     * @return Array of item drops, defining a set of items and their drop chances.
     */
    public abstract ItemDrop[] returns();

    /**
     * Get whether a given tool can effectively break this tile entity.
     *
     * @param item Item in question.
     * @return Whether tool is effective.
     */
    public boolean isToolEffective(ItemStack item) {
        if (getEffectiveTools() != null && item.getItem() instanceof ToolItem ti && !item.isEmpty()) {
            return getEffectiveTools().contains(ti.getClass());
        }
        return false;
    }
}
