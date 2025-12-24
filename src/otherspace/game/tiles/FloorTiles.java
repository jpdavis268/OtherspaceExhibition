package otherspace.game.tiles;

import org.joml.Vector2d;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.tiles.AutoTileStrategy;
import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.registry.TileRegistry;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.game.Assets;
import otherspace.game.items.Items;

/**
 * Register base game floor tiles.
 */
public class FloorTiles {
    private static final TileRegistry floorRegister = new TileRegistry("base");

    // Auto tile strategy.
    public static final AutoTileStrategy defaultStrategy = new AutoTileStrategy() {
        @Override
        public void autoTile(int base, double x, double y, boolean recursive) {
            // Get surrounding tiles.
            int top = Chunk.getTileAt(Chunk.FTM, new Vector2d(x, y - 1));
            int left = Chunk.getTileAt(Chunk.FTM, new Vector2d(x - 1, y));
            int right = Chunk.getTileAt(Chunk.FTM, new Vector2d(x + 1, y));
            int bottom = Chunk.getTileAt(Chunk.FTM, new Vector2d(x, y + 1));

            // Set tile bitmask accordingly
            int mask = (top > -1 ? 0 : 8) + (bottom > -1 ? 0 : 4) + (left > -1 ? 0 : 2) + (right > -1 ? 0 : 1);
            int frame = draw(mask);
            frame <<= 22;
            frame &= 0x7FC00000;
            Chunk.setTileAt(Chunk.FTM, new Vector2d(x, y), base | frame, false);

            if (!recursive) {
                autoTile(top, x, y - 1, true);
                autoTile(left, x - 1, y, true);
                autoTile(right, x + 1, y, true);
                autoTile(bottom, x, y + 1, true);
            }
        }

        @Override
        public int draw(int state) {
            return switch (state) {
                case 0x0 -> 0;
                case 0x1 -> -1;
                case 0x2 -> 1;
                case 0x3 -> 2;
                case 0x4 -> 3;
                case 0x5 -> -4;
                case 0x6 -> 4;
                case 0x7 -> 5;
                case 0x8 -> 6;
                case 0x9 -> -7;
                case 0xa -> 7;
                case 0xb -> 8;
                case 0xc -> 9;
                case 0xd -> -10;
                case 0xe -> 10;
                case 0xf -> 11;
                default -> throw new IllegalStateException("Unexpected value: " + state);
            };
        }
    };

    // Register floor tiles
    public static final int OAK_PLANKS = floorRegister.register(new FloorTile("tile_oak_floor", 1, new ItemDrop[]{
            new ItemDrop(new ItemStack(Items.OAK_PLANKS, 1), 1)
    }, Assets.footstepsWood, defaultStrategy));

    /**
     * Invoke above static initializers.
     */
    public static void register() {}
}
