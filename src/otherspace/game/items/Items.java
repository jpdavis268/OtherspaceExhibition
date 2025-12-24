package otherspace.game.items;

import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemTag;
import otherspace.core.engine.world.items.TileEntityItem;
import otherspace.core.engine.world.items.TileItem;
import otherspace.core.engine.world.items.tools.ToolMaterial;
import otherspace.core.registry.ItemRegistry;
import otherspace.game.entities.Firepit;
import otherspace.game.entities.WoodenCrate;

/**
 * Register base game items.
 */
public class Items {
    private static final ItemRegistry itemRegistry = new ItemRegistry("base");

    public static final ToolMaterial FLINT_MATERIAL = new ToolMaterial(64);
    public static final ToolMaterial WOOD_MATERIAL = new ToolMaterial(32);

    // Register items.
    public static final int DIRT = itemRegistry.register(new TileItem(50, "item_dirt", "base/tile_dirt_wall", null));
    public static final int BRANCH = itemRegistry.register(new Item(50, "item_branch", new ItemTag[]{(IBurnable) () -> 30}));
    public static final int FIREPIT = itemRegistry.register(new TileEntityItem(10, "item_firepit", Firepit.class, null));
    public static final int WOODEN_CRATE = itemRegistry.register(new TileEntityItem(20, "item_wooden_crate", WoodenCrate.class, new ItemTag[]{(IBurnable) () -> 180}));
    public static final int LOOSE_STONE = itemRegistry.register(new Item(50, "item_loose_stone", null));
    public static final int FLINT = itemRegistry.register(new Item(50, "item_flint", null));
    public static final int OAK_LOG = itemRegistry.register(new Item(50, "item_oak_log", new ItemTag[]{(IBurnable) () -> 60}));
    public static final int OAK_PLANKS = itemRegistry.register(new TileItem(50, "item_oak_planks", "base/tile_oak_planks_wall", "base/tile_oak_floor", new ItemTag[]{(IBurnable) () -> 30}));
    // TODO: May want to generate some of this based on templates once more material types are added.
    public static final int WOOD_ROD = itemRegistry.register(new Item(50, "item_wood_rod", new ItemTag[]{(IBurnable) () -> 20}));
    public static final int FLINT_KNIFE = itemRegistry.register(new KnifeItem("item_flint_knife", FLINT_MATERIAL));
    public static final int FLINT_PICKAXE = itemRegistry.register(new PickaxeItem("item_flint_pickaxe", FLINT_MATERIAL));
    public static final int FLINT_AXE = itemRegistry.register(new AxeItem("item_flint_axe", FLINT_MATERIAL));
    public static final int WOODEN_MALLET = itemRegistry.register(new MalletItem("item_wooden_mallet", WOOD_MATERIAL));
    public static final int WOODEN_NEEDLE = itemRegistry.register(new NeedleItem("item_wooden_needle", WOOD_MATERIAL));
    public static final int GRANITE_CHUNK = itemRegistry.register(new Item(50, "item_granite_chunk", null));
    public static final int BASALT_CHUNK = itemRegistry.register(new Item(50, "item_basalt_chunk", null));
    public static final int LIMESTONE_CHUNK = itemRegistry.register(new Item(50, "item_limestone_chunk", null));
    public static final int SANDSTONE_CHUNK = itemRegistry.register(new Item(50, "item_sandstone_chunk", null));
    public static final int MARBLE_CHUNK = itemRegistry.register(new Item(50, "item_marble_chunk", null));
    public static final int SLATE_CHUNK = itemRegistry.register(new Item(50, "item_slate_chunk", null));
    public static final int CHARCOAL = itemRegistry.register(new Item(50, "item_charcoal", new ItemTag[]{(IBurnable) () -> 240}));

    /**
     * Invoke above static initializers.
     */
    public static void register() {}
}
