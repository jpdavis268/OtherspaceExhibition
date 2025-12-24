package otherspace.core.engine.world.tiles;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import otherspace.core.engine.Camera;
import otherspace.core.engine.Color;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.window.RenderHandler;

/**
 * Defines a grid holding a set of tiles, stored using their runtime ID.
 */
public class Tilemap {
    private final Tile[] tileSet;
    private final Vector2i position;
    private final int width;
    private final int height;
    private final byte layer;

    private final int[][] tiledata;

    public Tilemap(Tile[] tileSet, Vector2i position, int width, int height, byte layer) {
        this.tileSet = tileSet;
        this.position = position;
        this.width = width;
        this.height = height;
        this.layer = layer;

        this.tiledata = new int[width][height];
    }

    /**
     * Draw this tilemap at its current coordinates.
     */
    public void draw(boolean splat, float weight) {
        Camera worldCam = SceneManager.getCurrentScene().getCamera();
        // The extra 0.01 is to prevent rounding errors from causing occasional one pixel gaps.
        int screenX = (int) ((position.x - worldCam.getPosition().x) * 32 + 0.01);
        int screenY = (int) ((position.y - worldCam.getPosition().y) * 32 + 0.01);
        if (worldCam.isAreaVisible(new Rectangled(screenX, screenY, screenX + 512, screenY + 512))) {
            if (splat) {
                int[][] paddedData = getTileDataPadding();
                RenderHandler.getInstance().drawTileMap(screenX, screenY, tileSet, paddedData, true, weight);
            }
            else {
                RenderHandler.getInstance().drawTileMap(screenX, screenY, tileSet, tiledata, false, weight);
            }
        }
    }

    /**
     * Get the tiledata as well as the tiles immediately surrounding it.
     *
     * @return Padded tiledata.
     */
    private int[][] getTileDataPadding() {
        int[][] padded = new int[18][18];

        Vector2i ownChunkPos = Chunk.getChunkAt(new Vector2d(position.x, position.y)).chunkCoords;
        Chunk topLeftC = Chunk.getChunk(new Vector2i(ownChunkPos.x - 1, ownChunkPos.y - 1));
        Chunk topC = Chunk.getChunk(new Vector2i(ownChunkPos.x, ownChunkPos.y - 1));
        Chunk topRightC = Chunk.getChunk(new Vector2i(ownChunkPos.x + 1, ownChunkPos.y - 1));
        Chunk rightC = Chunk.getChunk(new Vector2i(ownChunkPos.x + 1, ownChunkPos.y));
        Chunk bottomRightC = Chunk.getChunk(new Vector2i(ownChunkPos.x + 1, ownChunkPos.y + 1));
        Chunk bottomC = Chunk.getChunk(new Vector2i(ownChunkPos.x, ownChunkPos.y + 1));
        Chunk bottomLeftC = Chunk.getChunk(new Vector2i(ownChunkPos.x - 1, ownChunkPos.y + 1));
        Chunk leftC = Chunk.getChunk(new Vector2i(ownChunkPos.x - 1, ownChunkPos.y));

        int[][] topLeft = topLeftC == null ? null : topLeftC.getTileData(layer).tiledata;
        int[][] top = topC == null ? null : topC.getTileData(layer).tiledata;
        int[][] topRight = topRightC == null ? null : topRightC.getTileData(layer).tiledata;
        int[][] right = rightC == null ? null : rightC.getTileData(layer).tiledata;
        int[][] bottomRight = bottomRightC == null ? null : bottomRightC.getTileData(layer).tiledata;
        int[][] bottom = bottomC == null ? null : bottomC.getTileData(layer).tiledata;
        int[][] bottomLeft = bottomLeftC == null ? null : bottomLeftC.getTileData(layer).tiledata;
        int[][] left = leftC == null ? null : leftC.getTileData(layer).tiledata;

        for (int i = 0; i < padded.length; i++) {
            for (int j = 0; j < padded[i].length; j++) {
                padded[i][j] = switch (i) {
                    case 0 -> switch (j) {
                        case 0 -> topLeft == null ? -1 : topLeft[15][15];
                        case 17 -> bottomLeft == null ? -1 : bottomLeft[15][0];
                        default -> left == null ? -1 : left[15][j - 1];
                    };
                    case 17 -> switch (j) {
                        case 0 -> topRight == null ? -1 : topRight[0][15];
                        case 17 -> bottomRight == null ? -1 : bottomRight[0][0];
                        default -> right == null ? -1 : right[0][j - 1];
                    };
                    default -> switch (j) {
                        case 0 -> top == null ? -1 : top[i - 1][15];
                        case 17 -> bottom == null ? -1 : bottom[i - 1][0];
                        default -> tiledata[i - 1][j - 1];
                    };
                };
            }
        }
        return padded;
    }

    /**
     * Replace the tiledata array with a provided one.
     *
     * @param tiledata Data to use.
     */
    public void fillTiles(int[][] tiledata) {
        if (tiledata.length != width || tiledata[0].length != height) {
            throw new IllegalStateException("ERROR: Invalid fill array provided to tilemap.");
        }
        for (int i = 0; i < tiledata.length; i++) {
            for (int j = 0; j < tiledata[i].length; j++) {
                setTile(i, j, tiledata[i][j]);
            }
        }
    }

    /**
     * Set the tile at a given location on the tilemap.
     *
     * @param x X Position in tilemap array to set.
     * @param y Y Position in tilemap array to set.
     * @param tile Tile to set.
     */
    public void setTile(int x, int y, int tile, boolean autotile) {
        tiledata[x][y] = tile;
        if (autotile) {
            int index = tile < 0 ? tileSet.length - 1 : tile;
            AutoTileStrategy strategy = tileSet[index].getAutoTileSchema();
            if (strategy != null) {
                strategy.autoTile(tile, position.x + x + 0.5f, position.y + y + 0.5f, false);
            }
        }
    }

    /**
     * Set the tile at a given location on the tilemap.
     *
     * @param x X Position in tilemap array to set.
     * @param y Y Position in tilemap array to set.
     * @param tile Tile to set.
     */
    public void setTile(int x, int y, int tile) {
        this.setTile(x, y, tile, true);
    }

    /**
     * Get the tiledata at this tile. Note that this includes the offset and bit shifting
     * used for connected textures.
     *
     * @param x X index in tiledata.
     * @param y Y index in tiledata.
     * @return Tile at location.
     */
    public int getTile(int x, int y) {
        return tiledata[x][y];
    }

    /**
     * Get the actual tiledata at this tile, without the offset or shifting due to graphical effects.
     *
     * @param x X index in tiledata.
     * @param y Y index in tiledata.
     * @return Tile at location.
     */
    public int getAbsoluteTile(int x, int y) {
        return tiledata[x][y] & 0x803FFFFF;
    }
}
