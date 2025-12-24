package otherspace.game.items;

import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.items.tools.ToolMaterial;

/**
 * Represents an axe of a given material.
 */
public class AxeItem extends ToolItem {
    public AxeItem(String name, ToolMaterial material) {
        super(name, material, null);
    };
}
