package otherspace.game.tiles;

import org.joml.Vector2d;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.tiles.AutoTileStrategy;
import otherspace.core.engine.world.tiles.WallTile;
import otherspace.core.registry.TileRegistry;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.game.items.AxeItem;
import otherspace.game.items.Items;
import otherspace.game.items.PickaxeItem;

import java.util.Set;

/**
 * Register base game wall tiles.
 */
public class WallTiles {
    private static final TileRegistry wallRegister = new TileRegistry("base");

    private static final AutoTileStrategy defaultSchema = new AutoTileStrategy() {
        @Override
        public void autoTile(int base, double x, double y, boolean recursive) {
            // Get side tiles.
            int top = Chunk.getTileAt(Chunk.STM, new Vector2d(x, y - 1));
            int left = Chunk.getTileAt(Chunk.STM, new Vector2d(x - 1, y));
            int right = Chunk.getTileAt(Chunk.STM, new Vector2d(x + 1, y));
            int bottom = Chunk.getTileAt(Chunk.STM, new Vector2d(x, y + 1));

            // Get corner tiles.
            int topLeft = Chunk.getTileAt(Chunk.STM, new Vector2d(x - 1, y - 1));
            int topRight = Chunk.getTileAt(Chunk.STM, new Vector2d(x + 1, y - 1));
            int bottomLeft = Chunk.getTileAt(Chunk.STM, new Vector2d(x - 1, y + 1));
            int bottomRight = Chunk.getTileAt(Chunk.STM, new Vector2d(x + 1, y + 1));

            if (base >= 0) {
                int sideMask = (top > -1 ? 0 : 8) + (bottom > -1 ? 0 : 4) + (left > -1 ? 0 : 2) + (right > -1 ? 0 : 1);
                int cornerMask = (topLeft > -1 ? 0 : 128) + (topRight > -1 ? 0 : 64) + (bottomLeft > -1 ? 0 : 32) + (bottomRight > -1 ? 0 : 16);
                int frame = draw(sideMask + cornerMask);
                frame <<= 22;
                frame &= 0x7FC00000;
                Chunk.setTileAt(Chunk.STM, new Vector2d(x, y), base | frame, false);
            }

            if (!recursive) {
                autoTile(top, x, y - 1, true);
                autoTile(left, x - 1, y, true);
                autoTile(right, x + 1, y, true);
                autoTile(bottom, x, y + 1, true);
                autoTile(topLeft, x - 1, y - 1, true);
                autoTile(topRight, x + 1, y - 1, true);
                autoTile(bottomLeft, x - 1, y + 1, true);
                autoTile(bottomRight, x + 1, y + 1, true);
            }
        }

        @Override
        public int draw(int state) {
            int sideMask = state & 0xF;
            int cornerMask = (state >> 4) & 0xF;

            // If any form of programming could be considered heresy, it would probably be this.
            return switch (sideMask) {
                case 0x0 ->
                        switch (cornerMask) {
                            case 0x0 -> 0;
                            case 0x1 -> -1;
                            case 0x2 -> 1;
                            case 0x3 -> 2;
                            case 0x4 -> -3;
                            case 0x8 -> 3;
                            case 0x5 -> -4;
                            case 0xa -> 4;
                            case 0x6 -> -5;
                            case 0x9 -> 5;
                            case 0x7 -> 6;
                            case 0xb -> -6;
                            case 0xc -> 7;
                            case 0xe -> -8;
                            case 0xd -> 8;
                            case 0xf -> 9;
                            default -> throw new IllegalStateException("Unexpected value: " + cornerMask);
                        };
                case 0x1 -> -(10 + ((cornerMask & 8) >> 3) + ((cornerMask & 2)));
                case 0x2 -> 10 + ((cornerMask & 4) >> 2) + ((cornerMask & 1) << 1);
                case 0x3 -> 14;
                case 0x4 -> (15 + ((cornerMask & 8) >> 3) + ((cornerMask & 4) >> 2)) * ((cornerMask & 4) > 0 ? -1 : 1);
                case 0x5 -> -(18 + ((cornerMask & 8) >> 3));
                case 0x6 -> 18 + ((cornerMask & 4) >> 2);
                case 0x7 -> 20;
                case 0x8 -> (21 + ((cornerMask & 2) >> 1) + ((cornerMask & 1))) * ((cornerMask & 1) > 0 ? -1 : 1);
                case 0x9 -> -(24 + ((cornerMask & 2) >> 1));
                case 0xa -> 24 + (cornerMask & 1);
                case 0xb -> 26;
                case 0xc -> 27;
                case 0xd -> -28;
                case 0xe -> 28;
                case 0xf -> 29;
                default -> throw new IllegalStateException("Unexpected value: " + sideMask);
            };
        }
    };
    
    public static final int OAK_PLANKS = wallRegister.register(new WallTile(
            "tile_oak_planks_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.OAK_PLANKS, 1), 1)},
            Set.of(AxeItem.class),
            0.5f,
            defaultSchema
    ));
    public static final int DIRT = wallRegister.register(new WallTile(
            "tile_dirt_wall",
            0.25f,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.DIRT, 1), 1)},
            Set.of(PickaxeItem.class),
            0.5f,
            defaultSchema
    ));
    public static final int GRANITE = wallRegister.register(new WallTile(
            "tile_granite_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.GRANITE_CHUNK, 1), 1)},
            Set.of(PickaxeItem.class),
            0,
            defaultSchema
    ));
    public static final int BASALT = wallRegister.register(new WallTile(
            "tile_basalt_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.BASALT_CHUNK, 1), 1)},
            Set.of(PickaxeItem.class),
            0,
            defaultSchema
    ));
    public static final int LIMESTONE = wallRegister.register(new WallTile(
            "tile_limestone_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.LIMESTONE_CHUNK, 1), 1)},
            Set.of(PickaxeItem.class),
            0,
            defaultSchema
    ));
    public static final int SANDSTONE = wallRegister.register(new WallTile(
            "tile_sandstone_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.SANDSTONE_CHUNK, 1), 1)},
            Set.of(PickaxeItem.class),
            0,
            defaultSchema
    ));
    public static final int MARBLE = wallRegister.register(new WallTile(
            "tile_marble_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.MARBLE_CHUNK, 1), 1)},
            Set.of(PickaxeItem.class),
            0,
            defaultSchema
    ));
    public static final int SLATE = wallRegister.register(new WallTile(
            "tile_slate_wall",
            1,
            new ItemDrop[]{new ItemDrop(new ItemStack(Items.SLATE_CHUNK, 1), 1)},
            Set.of(PickaxeItem.class),
            0,
            defaultSchema
    ));

    /**
     * Invoke above static initializers.
     */
    public static void register() {}
}
