package otherspace.core.engine.world.tiles;

import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Sprite;

/**
 * Tile that makes up part of a layer of the world. Comes in a tileset that includes variants
 * needed for auto tiling.
 */
public class Tile {
    private final Sprite srcSprite;
    private final int tileWidth;
    private final int tileHeight;
    private final AutoTileStrategy autoTileStrategy;

    public Tile(Sprite srcSprite, int tileWidth, int tileHeight, AutoTileStrategy autoTileStrategy) {
        this.srcSprite = srcSprite;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.autoTileStrategy = autoTileStrategy;
    }

    public Tile(Sprite srcSprite, AutoTileStrategy autoTileStrategy) {
        this(srcSprite, 32, 32, autoTileStrategy);
    }


    /**
     * Draw this tile.
     *
     * @param x X Coordinate to draw at.
     * @param y Y Coordinate to draw at.
     * @param frame What frame of this tile to draw (Negative frame will cause a horizontal flip).
     */
    public void drawTile(int x, int y, int frame) {
        Vector2i coords = getTileCoords(Math.abs(frame));
        boolean flip = frame < 0;
        Rectanglei spriteCoords = new Rectanglei(coords.x, coords.y, coords.x + tileWidth, coords.y + tileHeight);
        int scale = flip ? -1 : 1;
        srcSprite.drawPart(x + (scale < 0 ? 32 : 0), y, Color.WHITE, scale, 1, 0, spriteCoords);
    }

    /**
     * Draw a tile with a color mixed in.
     *
     * @param x X Coordinate to draw at.
     * @param y Y Coordinate to draw at.
     * @param frame What frame of this tile to draw (Negative frame will cause a horizontal flip).
     * @param color Color to mix in to tile.
     */
    public void drawTileExt(int x, int y, int frame, Color color) {
        Vector2i coords = getTileCoords(Math.abs(frame));
        boolean flip = frame < 0;
        Rectanglei spriteCoords = new Rectanglei(coords.x, coords.y, coords.x + tileWidth, coords.y + tileHeight);
        int scale = flip ? -1 : 1;
        srcSprite.drawPart(x + (scale < 0 ? 32 : 0), y, color, scale, 1, 0, spriteCoords);
    }


    /**
     * Get the location of a tile state on this tileset.
     *
     * @param state Tile state to get.
     * @return Vector containing tile x and y coordinate on tileset image.
     */
    public Vector2i getTileCoords(int state) {
        int cols = getCols();
        int row = state / cols;
        int col = state % cols;
        return new Vector2i(col * tileWidth, row * tileHeight);
    }

    /**
     * Get the width of a single tile.
     *
     * @return Width of a single tile.
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Get the height of a single tile.
     *
     * @return Height of a single tile.
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Get the width of the whole tileset.
     *
     * @return Tileset width.
     */
    public int getWidth() {
        return srcSprite.getWidth();
    }

    /**
     * Get the height of the whole tileset.
     *
     * @return Tileset height.
     */
    public int getHeight() {
        return srcSprite.getHeight();
    }

    /**
     * How many rows of tiles this set has.
     *
     * @return Number of rows.
     */
    public int getRows() {
        return srcSprite.getHeight() / tileHeight;
    }

    /**
     * How many columns of tiles this set has.
     *
     * @return Number of columns.
     */
    public int getCols() {
        return srcSprite.getWidth() / tileWidth;
    }

    /**
     * How many possible tile IDs exist within this set.
     *
     * @return Number of valid tiles.
     */
    public int getSize() {
        return getRows() * getCols();
    }

    /**
     * Get the source sprite of this tilemap.
     *
     * @return Source tilemap sprite.
     */
    public Sprite getSrcSprite() {
        return srcSprite;
    }

    /**
     * Get this tile's auto tile strategy, or null if it has none.
     *
     * @return Tile's auto tiling strategy.
     */
    public AutoTileStrategy getAutoTileSchema() {
        return autoTileStrategy;
    }
}
