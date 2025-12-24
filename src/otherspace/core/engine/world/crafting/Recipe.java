package otherspace.core.engine.world.crafting;

import otherspace.core.registry.RecipeRegistry;

/**
 * Parent class for crafting recipes.
 */
public abstract class Recipe {
    /**
     * Get a recipe using its ID.
     *
     * @param id ID of recipe.
     * @return Recipe with ID.
     */
    public static Recipe get(int id) {
        return RecipeRegistry.get(id);
    }
}
