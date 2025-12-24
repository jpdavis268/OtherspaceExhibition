package otherspace.game.items;

import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.items.tools.ToolMaterial;

/**
 * Represents a pickaxe of a given material.
 */
public class PickaxeItem extends ToolItem {
    public PickaxeItem(String name, ToolMaterial material) {
        super(name, material, null);
    }
}
