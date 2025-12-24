package otherspace.core.engine.world.crafting;

import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;

import java.util.Set;

/**
 * Recipes that are crafted using the basic crafting menu (or a workbench).
 */
public class HandRecipe extends Recipe {
    public final ItemStack[] INPUTS;
    public final ItemStack[] OUTPUTS;
    public final Set<Class<? extends ToolItem>> REQUIRED_TOOLS;
    public final int CRAFTING_TIME;

    public HandRecipe(ItemStack[] inputs, ItemStack[] outputs, Set<Class<? extends ToolItem>> requiredTools, int craftingTime) {
        INPUTS = inputs;
        OUTPUTS = outputs;
        REQUIRED_TOOLS = requiredTools == null ? Set.of() : requiredTools;
        CRAFTING_TIME = craftingTime;
    }

    public HandRecipe(ItemStack[] inputs, ItemStack[] outputs, int craftingTime) {
        this(inputs, outputs, null, craftingTime);
    }
}
