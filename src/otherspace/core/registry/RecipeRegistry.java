package otherspace.core.registry;

import otherspace.core.engine.world.crafting.Recipe;

import java.util.LinkedList;

/**
 * Handles the registration of crafting recipes.
 */
public class RecipeRegistry {
    private static boolean initialized = false;
    private static LinkedList<Recipe> registryBuffer = new LinkedList<>();
    private static Recipe[] recipeList;

    /**
     * Register a new recipe.
     *
     * @param recipe Recipe to register.
     * @return Recipe ID.
     */
    public int register(Recipe recipe) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add recipe after registry phase has been completed!");
        }

        registryBuffer.add(recipe);
        return registryBuffer.size() - 1;
    }

    /**
     * Retrieve a recipe from the recipe registry.
     *
     * @param id ID of recipe to get.
     * @return Recipe with ID.
     */
    public static Recipe get(int id) {
        if (id < 0 || id >= recipeList.length) {
            throw new IndexOutOfBoundsException("ERROR: Attempted to query data for nonexistent recipe ID " + id);
        }

        return recipeList[id];
    }

    /**
     * Get how many recipes have been registered.
     *
     * @return Number of existing recipes.
     */
    public static int getRegistrySize() {
        return recipeList.length;
    }

    /**
     * Complete registry of recipes. Must be done before recipe data can be queried.
     */
    public static void registerRecipes() {
        if (initialized) {
            return;
        }

        recipeList = new Recipe[registryBuffer.size()];
        registryBuffer.toArray(recipeList);
        registryBuffer = null;

        initialized = true;
    }

    String modHandle;

    public RecipeRegistry(String modHandle) {
        this.modHandle = modHandle;
    }
}
