package otherspace.game.items;

import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.items.tools.ToolMaterial;

public class MalletItem extends ToolItem {
    /**
     * Create a new tool item.
     *
     * @param name     Lang key identifying item name.
     * @param material Material tool is made of.
     */
    public MalletItem(String name, ToolMaterial material) {
        super(name, material, null);
    }
}
