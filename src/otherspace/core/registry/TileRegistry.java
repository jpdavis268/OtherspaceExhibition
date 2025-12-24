package otherspace.core.registry;

import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.engine.world.tiles.Tile;
import otherspace.core.engine.world.tiles.WallTile;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Handles the registration of tiles and their required assets.
 */
public class TileRegistry {
    public static final int NULL = -2;
    public static final int EMPTY = -1;

    private static boolean initialized = false;
    private static Tile[] groundTileset;
    private static Tile[] floorTileset;
    private static Tile[] wallTileset;

    // Ground
    private static final HashMap<String, Integer> gtRegister = new HashMap<>();
    private static LinkedList<GroundTile> gtBuffer = new LinkedList<>();
    private static GroundTile[] gtList;

    // Floor
    private static final HashMap<String, Integer> ftRegister = new HashMap<>();
    private static LinkedList<FloorTile> ftBuffer = new LinkedList<>();
    private static FloorTile[] ftList;

    // Wall
    private static final HashMap<String, Integer> wtRegister = new HashMap<>();
    private static LinkedList<WallTile> wtBuffer = new LinkedList<>();
    private static WallTile[] wtList;

    /**
     * Complete registry of tiles. Must be done before tile data can be queried.
     */
    public static void registerTiles() {
        if (initialized) {
            return;
        }

        gtList = new GroundTile[gtBuffer.size()];
        gtBuffer.toArray(gtList);
        gtBuffer = null;
        groundTileset = new Tile[gtList.length];
        for (int i = 0; i < gtList.length; i++) {
            groundTileset[i] = gtList[i].TILE;
        }

        ftList = new FloorTile[ftBuffer.size()];
        ftBuffer.toArray(ftList);
        ftBuffer = null;
        floorTileset = new Tile[ftList.length];
        for (int i = 0; i < ftList.length; i++) {
            floorTileset[i] = ftList[i].TILE;
        }

        wtList = new WallTile[wtBuffer.size()];
        wtBuffer.toArray(wtList);
        wtBuffer = null;
        wallTileset = new Tile[wtList.length];
        for (int i = 0; i < wtList.length; i++) {
            wallTileset[i] = wtList[i].TILE;
        }

        initialized = true;
    }

    /**
     * Get a ground tile by its ID.
     *
     * @param id ID of ground tile.
     * @return Ground tile with corresponding ID.
     */
    public static GroundTile getGT(int id) {
        if (id < 0 || id >= gtList.length) {
            return null;
        }

        return gtList[id];
    }

    /**
     * Get the ID of a ground tile using its handle.
     *
     * @param tileHandle Handle of ground tile.
     * @return ID of ground tile.
     */
    public static int getGroundID(String tileHandle) {
        return gtRegister.get(tileHandle);
    }

    /**
     * Get a floor tile by its ID.
     *
     * @param id ID of floor tile.
     * @return Floor tile with corresponding ID.
     */
    public static FloorTile getFT(int id) {
        if (id < 0 || id >= ftList.length) {
            return null;
        }

        return ftList[id];
    }

    /**
     * Get the ID of a floor tile using its handle.
     *
     * @param tileHandle Handle of floor tile.
     * @return ID of floor tile.
     */
    public static int getFloorID(String tileHandle) {
        return ftRegister.get(tileHandle);
    }

    /**
     * Get a wall tile by its ID.
     *
     * @param id ID of wall tile.
     * @return Wall tile with corresponding ID.
     */
    public static WallTile getWT(int id) {
        if (id < 0 || id >= wtList.length) {
            return null;
        }

        return wtList[id];
    }

    /**
     * Get the ID of a wall tile using its handle.
     *
     * @param tileHandle Handle of wall tile.
     * @return ID of wall tile.
     */
    public static int getWallID(String tileHandle) {
        return wtRegister.get(tileHandle);
    }

    public static Tile[] getGroundTileset() {return groundTileset;}
    public static Tile[] getFloorTileset() {return floorTileset;}
    public static Tile[] getWallTileset() {return wallTileset;}

    public static int getGTCount() {return gtList.length;}
    public static int getFTCount() {return ftList.length;}
    public static int getWTCount() {return wtList.length;}

    String modHandle;

    public TileRegistry(String modHandle) {
        this.modHandle = modHandle;
    }

    /**
     * Register a new ground tile.
     *
     * @param tile Ground tile to register.
     * @return ID of tile.
     */
    public int register(GroundTile tile) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add ground tile after registry phase has been completed!");
        }

        String tileHandle = modHandle + "/" + tile.NAME;
        gtRegister.put(tileHandle, gtBuffer.size());
        gtBuffer.add(tile);
        tile.setMod(modHandle);
        return gtBuffer.size() - 1;
    }

    /**
     * Register a new floor tile.
     *
     * @param tile Floor tile to register.
     * @return ID of tile.
     */
    public int register(FloorTile tile) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add floor tile after registry phase has been completed!");
        }

        String tileHandle = modHandle + "/" + tile.NAME;
        ftRegister.put(tileHandle, ftBuffer.size());
        ftBuffer.add(tile);
        tile.setMod(modHandle);
        return ftBuffer.size() - 1;
    }

    /**
     * Register a new wall tile.
     *
     * @param tile Wall tile to register.
     * @return ID of tile.
     */
    public int register(WallTile tile) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add wall tile after registry phase has been completed!");
        }

        String tileHandle = modHandle + "/" + tile.NAME;
        wtRegister.put(tileHandle, wtBuffer.size());
        wtBuffer.add(tile);
        tile.setMod(modHandle);
        return wtBuffer.size() - 1;
    }
}
