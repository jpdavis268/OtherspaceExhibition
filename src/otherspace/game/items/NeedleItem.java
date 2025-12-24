package otherspace.game.items;

import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.items.tools.ToolMaterial;

public class NeedleItem extends ToolItem {
    /**
     * Create a new tool item.
     *
     * @param name     Lang key identifying item name.
     * @param material Material tool is made of.
     * */
    public NeedleItem(String name, ToolMaterial material) {
        super(name, material, null);
    }
}
