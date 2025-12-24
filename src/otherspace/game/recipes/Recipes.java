package otherspace.game.recipes;

import otherspace.core.engine.world.crafting.HandRecipe;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.registry.RecipeRegistry;
import otherspace.game.items.AxeItem;
import otherspace.game.items.Items;
import otherspace.game.items.KnifeItem;
import otherspace.game.items.MalletItem;

import java.util.Set;

import static otherspace.game.items.Items.FLINT_MATERIAL;
import static otherspace.game.items.Items.WOOD_MATERIAL;

/**
 * Register base game recipes.
 */
public class Recipes {
    private static final RecipeRegistry recipeRegistry = new RecipeRegistry("base");

    // Register recipes.
    public static final int FIREPIT_BRANCH = recipeRegistry.register(new HandRecipe(
            new ItemStack[]{
                    new ItemStack(Items.LOOSE_STONE, 3),
                    new ItemStack(Items.BRANCH, 3),
                    new ItemStack(Items.FLINT, 2)
            },
            new ItemStack[]{
                    new ItemStack(Items.FIREPIT, 1)
            },
            120)
    );
    public static final int OAK_PLANKS = recipeRegistry.register(new HandRecipe(
            new ItemStack[]{
                    new ItemStack(Items.OAK_LOG, 1),
            },
            new ItemStack[]{
                    new ItemStack(Items.OAK_PLANKS, 2)
            },
            Set.of(AxeItem.class),
            60)
    );
    public static final int WOODEN_CRATE = recipeRegistry.register(new HandRecipe(
            new ItemStack[]{
                    new ItemStack(Items.OAK_PLANKS, 8),
                    new ItemStack(Items.FLINT, 2)
            },
            new ItemStack[]{
                    new ItemStack(Items.WOODEN_CRATE, 1)
            },
            Set.of(AxeItem.class, MalletItem.class),
            120)
    );
    public static final int FLINT_KNIFE = recipeRegistry.register(new HandRecipe(
            new ItemStack[] {
                    new ItemStack(Items.FLINT, 3),
                    new ItemStack(Items.BRANCH, 1)
            },
            new ItemStack[] {
                    new ItemStack(Items.FLINT_KNIFE, 1, FLINT_MATERIAL.durability())
            },
            120
    ));
    public static final int WOOD_ROD_BRANCH = recipeRegistry.register(new HandRecipe(
            new ItemStack[] {
                    new ItemStack(Items.BRANCH, 1),
            },
            new ItemStack[] {
                    new ItemStack(Items.WOOD_ROD, 1)
            },
            Set.of(KnifeItem.class),
            60
    ));
    public static final int WOOD_ROD_PLANKS = recipeRegistry.register(new HandRecipe(
            new ItemStack[] {
                    new ItemStack(Items.OAK_PLANKS, 1),
            },
            new ItemStack[] {
                    new ItemStack(Items.WOOD_ROD, 1)
            },
            Set.of(KnifeItem.class),
            60
    ));
    public static final int FLINT_PICKAXE = recipeRegistry.register(new HandRecipe(
            new ItemStack[]{
                    new ItemStack(Items.FLINT, 3),
                    new ItemStack(Items.WOOD_ROD, 1),
            },
            new ItemStack[]{
                    new ItemStack(Items.FLINT_PICKAXE, 1, FLINT_MATERIAL.durability())
            },
            120
    ));
    public static final int FLINT_AXE = recipeRegistry.register(new HandRecipe(
            new ItemStack[]{
                    new ItemStack(Items.FLINT, 3),
                    new ItemStack(Items.WOOD_ROD, 1),
            },
            new ItemStack[]{
                    new ItemStack(Items.FLINT_AXE, 1, FLINT_MATERIAL.durability())
            },
            120
    ));
    public static final int WOODEN_MALLET = recipeRegistry.register(new HandRecipe(
            new ItemStack[] {
                    new ItemStack(Items.OAK_PLANKS, 1),
                    new ItemStack(Items.WOOD_ROD, 1),
            },
            new ItemStack[] {
                    new ItemStack(Items.WOODEN_MALLET, 1, WOOD_MATERIAL.durability())
            },
            Set.of(KnifeItem.class),
            120
    ));
    public static final int WOODEN_NEEDLE = recipeRegistry.register(new HandRecipe(
            new ItemStack[] {
                    new ItemStack(Items.WOOD_ROD, 1),
            },
            new ItemStack[] {
                    new ItemStack(Items.WOODEN_NEEDLE, 1, WOOD_MATERIAL.durability())
            },
            Set.of(KnifeItem.class),
            240
    ));

    /**
     * Invoke above static initializers.
     */
    public static void register() {}
}
