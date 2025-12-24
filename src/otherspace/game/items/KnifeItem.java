package otherspace.game.items;

import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.items.tools.ToolMaterial;

/**
 * Represents a knife, used both for crafting and as an early weapon.
 */
public class KnifeItem extends ToolItem {
    public KnifeItem(String name, ToolMaterial material) {
        super(name, material, null);
    }
}
