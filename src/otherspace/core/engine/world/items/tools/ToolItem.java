package otherspace.core.engine.world.items.tools;

import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.ItemTag;

/**
 * Item representation of tools, be they crafting tools, hand tools, or both.
 */
public abstract class ToolItem extends Item {
    private final ToolMaterial material;

    /**
     * Create a new tool item.
     *
     * @param name      Lang key identifying item name.
     * @param material  Material tool is made of.
     * @param tags      Array of item tags, or null if no tags should be defined.
     */
    public ToolItem(String name, ToolMaterial material, ItemTag[] tags) {
        super(1, name, tags);
        this.material = material;
    }

    /**
     * Get the maximum durability of this tool.
     *
     * @return Tool's maximum durability.
     */
    public int getMaxDurability() {
        return material.durability();
    }

    /**
     * Damage a tool item by a set amount.
     *
     * @param stack Stack to damage.
     * @param damage Damage to inflict.
     */
    public static void damage(ItemStack stack, int damage) {
        damage = Math.max(damage, 0);
        if (stack.getItem() instanceof ToolItem) {
            stack.state -= damage;
            if (stack.state <= 0) {
                stack.stackSize = 0;
            }
        }
    }
}
