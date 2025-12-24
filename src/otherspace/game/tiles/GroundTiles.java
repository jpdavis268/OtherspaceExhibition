package otherspace.game.tiles;

import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.registry.TileRegistry;
import otherspace.game.Assets;

/**
 * Register base game ground tiles.
 */
public class GroundTiles {
    private static final TileRegistry groundRegister = new TileRegistry("base");

    public static final int LAB_DARK = groundRegister.register(new GroundTile("tile_lab_dark_ground", Assets.footstepsStone, null));
    public static final int LAB_LIGHT = groundRegister.register(new GroundTile("tile_lab_light_ground", Assets.footstepsStone, null));
    public static final int DEEP_WATER = groundRegister.register(new GroundTile("tile_deep_water_ground", 0.33f, false, Assets.footstepsDeepWater, null));
    public static final int SHALLOW_WATER = groundRegister.register(new GroundTile("tile_shallow_water_ground", 0.66f, false, Assets.footstepsShallowWater, null));
    public static final int SAND = groundRegister.register(new GroundTile("tile_sand_ground", Assets.footstepsSandDirt, null));
    public static final int GRASS = groundRegister.register(new GroundTile("tile_grass_ground", Assets.footstepsGrass, null));
    public static final int DIRT = groundRegister.register(new GroundTile("tile_dirt_ground", Assets.footstepsSandDirt, null));
    public static final int GRANITE = groundRegister.register(new GroundTile("tile_granite_ground", Assets.footstepsStone, null));
    public static final int BASALT = groundRegister.register(new GroundTile("tile_basalt_ground", Assets.footstepsStone, null));
    public static final int LIMESTONE = groundRegister.register(new GroundTile("tile_limestone_ground", Assets.footstepsStone, null));
    public static final int SANDSTONE = groundRegister.register(new GroundTile("tile_sandstone_ground", Assets.footstepsStone, null));
    public static final int MARBLE = groundRegister.register(new GroundTile("tile_marble_ground", Assets.footstepsStone, null));
    public static final int SLATE = groundRegister.register(new GroundTile("tile_slate_ground", Assets.footstepsStone, null));

    /**
     * Invoke above static initializers.
     */
    public static void register() {}
}
