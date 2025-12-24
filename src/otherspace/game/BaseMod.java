package otherspace.game;

import otherspace.game.entities.EntityRegistryHandler;
import otherspace.game.items.Items;
import otherspace.game.recipes.Recipes;
import otherspace.game.tiles.FloorTiles;
import otherspace.game.tiles.GroundTiles;
import otherspace.game.tiles.WallTiles;

public class BaseMod {
    /**
     * Initialize base game content.
     */
    public static void init() {
        // Resources
        Assets.register();

        // Items
        Items.register();

        // Tiles
        GroundTiles.register();
        FloorTiles.register();
        WallTiles.register();

        // Entities
        EntityRegistryHandler.register();

        // Recipes
        Recipes.register();
    }
}
