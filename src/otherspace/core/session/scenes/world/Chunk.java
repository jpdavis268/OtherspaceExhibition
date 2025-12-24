package otherspace.core.session.scenes.world;

import com.google.gson.*;
import org.joml.Vector2d;
import org.joml.Vector2i;
import otherspace.core.engine.utils.IOUtils;
import otherspace.core.engine.utils.NoiseUtils;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.engine.world.tiles.Tilemap;
import otherspace.core.engine.world.tiles.WallTile;
import otherspace.core.registry.TileRegistry;
import otherspace.game.entities.LooseBranch;
import otherspace.game.entities.OakTree;
import otherspace.game.entities.Player;
import otherspace.game.entities.RockPile;
import otherspace.game.tiles.GroundTiles;
import otherspace.game.tiles.WallTiles;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * 16x16 "Chunk" of the world, contains a set of tilemaps and local entities.
 */
public class Chunk {
    public enum CHUNK_STATE {
        INACTIVE,
        BACKGROUND,
        NEARBY
    }

    public static final byte GTM = 0;
    public static final byte FTM = 1;
    public static final byte STM = 2;

    public static final int WORLD_MAX_DIST = 1000016;

    public final Vector2i chunkCoords;
    public final HashSet<Entity> localEntities;
    private final Tilemap gtm;
    private final Tilemap ftm;
    private final Tilemap stm;

    private CHUNK_STATE chunkState;

    private Chunk(Vector2i chunkCoords) {
        World.getChunkMap().put(chunkCoords, this);
        this.chunkCoords = chunkCoords;
        localEntities = new HashSet<>();
        gtm = new Tilemap(GroundTile.getTileset(), new Vector2i(chunkCoords).mul(16), 16, 16, Chunk.GTM);
        ftm = new Tilemap(FloorTile.getTileset(), new Vector2i(chunkCoords).mul(16), 16, 16, Chunk.FTM);
        stm = new Tilemap(WallTile.getTileset(), new Vector2i(chunkCoords).mul(16), 16, 16, Chunk.STM);
        chunkState = CHUNK_STATE.BACKGROUND;

        // Load chunk data if it exists.
        File chunkFile = new File(World.getSavePath(), String.format("/world/%d_%d", chunkCoords.x, chunkCoords.y));
        if (chunkFile.exists()) {
            ByteBuffer[] chunkData = IOUtils.loadCompressedBuffers(chunkFile);
            JsonObject dataMapping = new Gson().fromJson(String.valueOf(Charset.defaultCharset().decode(chunkData[0])), JsonObject.class);

            // Load tile data.
            HashMap<Integer, Integer> gtmMapping = new HashMap<>();
            HashMap<Integer, Integer> ftmMapping = new HashMap<>();
            HashMap<Integer, Integer> stmMapping = new HashMap<>();

            Map<String, JsonElement> gModSet = dataMapping.getAsJsonObject("g").asMap();
            for (String mod : gModSet.keySet()) {
                Map<String, JsonElement> tiles = gModSet.get(mod).getAsJsonObject().asMap();
                for (String tile : tiles.keySet()) {
                    String tileHandle = mod + "/" + tile;
                    int savedID = tiles.get(tile).getAsInt();
                    int sessionID = GroundTile.getID(tileHandle);
                    gtmMapping.put(savedID, sessionID);
                }
            }

            Map<String, JsonElement> fModSet = dataMapping.getAsJsonObject("f").asMap();
            for (String mod : fModSet.keySet()) {
                Map<String, JsonElement> tiles = fModSet.get(mod).getAsJsonObject().asMap();
                for (String tile : tiles.keySet()) {
                    String tileHandle = mod + "/" + tile;
                    int savedID = tiles.get(tile).getAsInt();
                    int sessionID = FloorTile.getID(tileHandle);
                    ftmMapping.put(savedID, sessionID);
                }
            }

            Map<String, JsonElement> sModSet = dataMapping.getAsJsonObject("s").asMap();
            for (String mod : sModSet.keySet()) {
                Map<String, JsonElement> tiles = sModSet.get(mod).getAsJsonObject().asMap();
                for (String tile : tiles.keySet()) {
                    String tileHandle = mod + "/" + tile;
                    int savedID = tiles.get(tile).getAsInt();
                    int sessionID = WallTile.getID(tileHandle);
                    stmMapping.put(savedID, sessionID);
                }
            }

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    int groundTile = chunkData[1].get(x * 16 + y);
                    int floorTile = chunkData[2].get(x * 16 + y);
                    int wallTile = chunkData[3].get(x * 16 + y);

                    gtm.setTile(x, y, groundTile == TileRegistry.EMPTY ? TileRegistry.EMPTY : gtmMapping.get(groundTile), true);
                    ftm.setTile(x, y, floorTile == TileRegistry.EMPTY ? TileRegistry.EMPTY : ftmMapping.get(floorTile), true);
                    stm.setTile(x, y, wallTile == TileRegistry.EMPTY ? TileRegistry.EMPTY : stmMapping.get(wallTile), true);
                }
            }

            // Load entity data.
            JsonArray entityData = new Gson().fromJson(String.valueOf(Charset.defaultCharset().decode(chunkData[4])), JsonArray.class);
            for (JsonElement e : entityData) {
                JsonObject serial = e.getAsJsonObject();
                Entity.deserialize(serial);
            }
        }
        else {
            generateChunk();
        }
    }

    /**
     * Generate terrain for this chunk.
     */
    public void generateChunk() {
        switch (World.getMapType()) {
            case 0: { // Default
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        // Noise Generation
                        float nx = (x / 16f) + chunkCoords.x;
                        float ny = (y / 16f) + chunkCoords.y;

                        // Features
                        float elev = NoiseUtils.perlinNoise(nx / 4, ny / 4) // Base
                                + 0.2f * NoiseUtils.perlinNoise(nx * 2, ny * 2); // Roughness
                        elev /= 1.2f; // Octave Correction

                        // Level out spawn to ensure players don't appear in a wall.
                        float dist = (float) Math.sqrt(Math.pow(nx, 2) + Math.pow(ny, 2));
                        float diff = elev - 0.5f;
                        float adj = diff / (dist + 1);
                        elev -= adj;

                        // Generate tiles
                        int gt;
                        int st = TileRegistry.EMPTY;

                        if (elev < 0.3f) { // Deep Water
                            gt = GroundTiles.DEEP_WATER;
                        }
                        else if (elev < 0.35f) { // Shallow Water
                            gt = GroundTiles.SHALLOW_WATER;
                        }
                        else if (elev < 0.4f) { // Sand
                            gt = GroundTiles.SAND;
                        }
                        else if (elev < 0.6f) { // Grass
                            gt = GroundTiles.GRASS;
                        }
                        else if (elev < 0.65f) { // Dirt
                            gt = GroundTiles.DIRT;
                            st = WallTiles.DIRT;
                        }
                        else { // Stone
                            // Generate Rocks
                            float rockGen = NoiseUtils.perlinNoise(nx / 2, ny / 2);
                            int rockVal = (int) (rockGen * 6);

                            st = switch (rockVal) {
                                case 0 -> {
                                    gt = GroundTiles.GRANITE;
                                    yield WallTiles.GRANITE;
                                }
                                case 1 -> {
                                    gt = GroundTiles.BASALT;
                                    yield WallTiles.BASALT;
                                }
                                case 2 -> {
                                    gt = GroundTiles.LIMESTONE;
                                    yield WallTiles.LIMESTONE;
                                }
                                case 3 -> {
                                    gt = GroundTiles.SANDSTONE;
                                    yield WallTiles.SANDSTONE;
                                }
                                case 4 -> {
                                    gt = GroundTiles.MARBLE;
                                    yield WallTiles.MARBLE;
                                }
                                default -> {
                                    gt = GroundTiles.SLATE;
                                    yield WallTiles.SLATE;
                                }
                            };
                        }

                        // Set tiles
                        gtm.setTile(x, y, gt, true);
                        ftm.setTile(x, y, TileRegistry.EMPTY, true);
                        stm.setTile(x, y, st, true);

                        // Resource Spawning
                        boolean resourceAtLocation = false;
                        if (gt == GroundTiles.GRASS || gt == GroundTiles.SAND) {
                            float spawnResource = NoiseUtils.perlinNoise(nx * 50, ny * 50);
                            if (spawnResource > 0.83f) {
                                if (x % 2 == y % 2) {
                                    new LooseBranch(new Vector2d(chunkCoords.x * 16 + x + 0.5f, chunkCoords.y * 16 + y + 1));
                                }
                                else {
                                    new RockPile(new Vector2d(chunkCoords.x * 16 + x + 0.5f, chunkCoords.y * 16 + y + 1));
                                }
                                resourceAtLocation = true;
                            }
                        }

                        // Generate Foliage
                        if (gt == GroundTiles.GRASS && st == TileRegistry.EMPTY && !resourceAtLocation) {
                            float foliageMap = (NoiseUtils.perlinNoise(nx, ny) + NoiseUtils.perlinNoise(nx * 25, ny * 25)) / 2;
                            if (foliageMap > 0.76f) {
                                new OakTree(new Vector2d(chunkCoords.x * 16 + x + 0.5f, chunkCoords.y * 16 + y + 1));
                            }
                        }
                    }
                }
            } break;
            case 1: { // Lab Tile
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        // Generate checkerboard pattern
                        gtm.setTile(x, y, (x % 2 == y % 2) ? GroundTiles.LAB_DARK : GroundTiles.LAB_LIGHT, true);
                        ftm.setTile(x, y, TileRegistry.EMPTY, true);
                        stm.setTile(x, y, TileRegistry.EMPTY, true);
                    }
                }
            }
        }
    }

    /**
     * Get a specific set of tiledata for this chunk.
     *
     * @param layer Layer constant.
     * @return Desired tiledata grid.
     */
    public Tilemap getTileData(byte layer) {
        return switch (layer) {
            case GTM -> gtm;
            case FTM -> ftm;
            case STM -> stm;
            default -> null;
        };
    }

    /**
     * Get the chunk at the specified position, or null if no chunk exists.
     *
     * @param chunkCoords Chunk coordinate vector to check.
     * @return Chunk at location, or null if none was found.
     */
    public static Chunk getChunk(Vector2i chunkCoords) {
        return World.getChunkMap().get(chunkCoords);
    }

    /**
     * Get the chunk at a given global position.
     *
     * @param position Coordinates in world to get chunk at.
     * @return Chunk at position, or null if it does not exist.
     */
    public static Chunk getChunkAt(Vector2d position) {
        Vector2i chunkCoords = new Vector2i((int) Math.floor(position.x / 16), (int) Math.floor(position.y / 16));
        return getChunk(chunkCoords);
    }

    /**
     * Get the tile at a given global location, if it exists.
     *
     * @param layer Tiledata set to check.
     * @param position Coordinate point to check.
     * @return Tile at location, or -2 if no chunk exists here.
     */
    public static int getTileAt(byte layer, Vector2d position) {
        return getTileAt(layer, position, false);
    }

    /**
     * Get the tile at a given global location, if it exists.
     *
     * @param layer Tiledata set to check.
     * @param position Coordinate point to check.
     * @param includeMask Whether to include the bitmask used for graphical effects (FOR RENDERING ONLY).
     * @return Tile at location, or -1 if no chunk exists here.
     */
    public static int getTileAt(byte layer, Vector2d position, boolean includeMask) {
        Vector2i chunkCoords = new Vector2i((int) Math.floor(position.x / 16), (int) Math.floor(position.y / 16));
        Chunk chunk = getChunk(chunkCoords);
        if (chunk != null) {
            Tilemap tiledata = chunk.getTileData(layer);
            if (tiledata != null) {
                int tx = (int) (Math.floor(position.x) - chunkCoords.x * 16);
                int ty = (int) (Math.floor(position.y) - chunkCoords.y * 16);
                return includeMask ? tiledata.getTile(tx, ty) : tiledata.getAbsoluteTile(tx, ty);
            }
        }

        return TileRegistry.NULL;
    }

    /**
     * Set the tile at a given global location to a specified value, if it exists.
     *
     * @param layer Tiledata set to check.
     * @param position Coordinate point to check.
     * @param tile Tile to put at location.
     */
    public static void setTileAt(byte layer, Vector2d position, int tile) {
        setTileAt(layer, position, tile, true);
    }

    /**
     * Set the tile at a given global location to a specified value, if it exists.
     *
     * @param layer Tiledata set to check.
     * @param position Coordinate point to check.
     * @param tile Tile to put at location.
     * @param autotile Whether to run an autotile schema (CLIENT SIDE ONLY).
     */
    public static void setTileAt(byte layer, Vector2d position, int tile, boolean autotile) {
        Vector2i chunkCoords = new Vector2i((int) Math.floor(position.x / 16), (int) Math.floor(position.y / 16));
        Chunk chunk = getChunk(chunkCoords);
        if (chunk != null) {
            Tilemap tiledata = chunk.getTileData(layer);
            int tx = (int) (Math.floor(position.x) - chunkCoords.x * 16);
            int ty = (int) (Math.floor(position.y) - chunkCoords.y * 16);
            tiledata.setTile(tx, ty, tile, autotile);
        }
    }

    /**
     * Attempt to load a chunk at the given chunk coordinates.
     *
     * @param chunkCoords Coordinates of chunk to load.
     * @return Loaded/Generated chunk, or null if it was not initialized (e.g. was outside of world).
     */
    public static Chunk loadChunk(Vector2i chunkCoords) {
        if (Math.abs(chunkCoords.x * 16) > WORLD_MAX_DIST || Math.abs(chunkCoords.y * 16) > WORLD_MAX_DIST) {
            return null;
        }
        return new Chunk(chunkCoords);
    }

    /**
     * Save the contents of a chunk to its corresponding file.
     */
    public void saveChunk() {
        // Get chunk file path.
        File worldData = new File(World.getSavePath(), "/world/");
        File chunkFile = new File(worldData, String.format("%d_%d", chunkCoords.x, chunkCoords.y));
        if (!worldData.exists()) {
            try {
                Files.createDirectory(worldData.toPath());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Save tiledata.
        JsonObject gtmMeta = new JsonObject();
        JsonObject ftmMeta = new JsonObject();
        JsonObject stmMeta = new JsonObject();
        ByteBuffer gtmData = memAlloc(256);
        ByteBuffer ftmData = memAlloc(256);
        ByteBuffer stmData = memAlloc(256);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                // Ground
                GroundTile gt = GroundTile.get(gtm.getAbsoluteTile(x, y));
                if (gt != null) {
                    if (!gtmMeta.has(gt.getMod())) {
                        gtmMeta.add(gt.getMod(), new JsonObject());
                    }
                    JsonObject gtModGroup = gtmMeta.getAsJsonObject(gt.getMod());
                    JsonElement gtID = gtModGroup.get(gt.NAME);
                    if (gtID != null) {
                        gtmData.put(gtID.getAsByte());
                    }
                    else {
                        byte rel = (byte) gtModGroup.size();
                        gtModGroup.add(gt.NAME, new JsonPrimitive(rel));
                        gtmData.put(rel);
                    }
                }
                else {
                    gtmData.put((byte) TileRegistry.EMPTY);
                }

                // Floor
                FloorTile ft = FloorTile.get(ftm.getAbsoluteTile(x, y));
                if (ft != null) {
                    if (!ftmMeta.has(ft.getMod())) {
                        ftmMeta.add(ft.getMod(), new JsonObject());
                    }
                    JsonObject ftModGroup = ftmMeta.getAsJsonObject(ft.getMod());
                    JsonElement ftID = ftModGroup.get(ft.NAME);
                    if (ftID != null) {
                        ftmData.put(ftID.getAsByte());
                    }
                    else {
                        byte rel = (byte) ftModGroup.size();
                        ftModGroup.add(ft.NAME, new JsonPrimitive(rel));
                        ftmData.put(rel);
                    }
                }
                else {
                    ftmData.put((byte) TileRegistry.EMPTY);
                }

                // Wall
                WallTile st = WallTile.get(stm.getAbsoluteTile(x, y));
                if (st != null) {
                    if (!stmMeta.has(st.getMod())) {
                        stmMeta.add(st.getMod(), new JsonObject());
                    }
                    JsonObject stModGroup = stmMeta.getAsJsonObject(st.getMod());
                    JsonElement stID = stModGroup.get(st.NAME);
                    if (stID != null) {
                        stmData.put(stID.getAsByte());
                    } else {
                        byte rel = (byte) stModGroup.size();
                        stModGroup.add(st.NAME, new JsonPrimitive(rel));
                        stmData.put(rel);
                    }
                }
                else {
                    stmData.put((byte) TileRegistry.EMPTY);
                }
            }
        }

        // Get chunk entities.
        JsonArray entityList = new JsonArray();
        for (Entity e : localEntities) {
            if (!(e instanceof Player)) {
                entityList.add(e.serialize());
            }
        }

        JsonObject tileMeta = new JsonObject();
        tileMeta.add("g", gtmMeta);
        tileMeta.add("f", ftmMeta);
        tileMeta.add("s", stmMeta);

        ByteBuffer metaBuffer = IOUtils.compressJson(tileMeta);
        ByteBuffer gtmComp = IOUtils.compressBuffer(gtmData);
        ByteBuffer ftmComp = IOUtils.compressBuffer(ftmData);
        ByteBuffer stmComp = IOUtils.compressBuffer(stmData);
        ByteBuffer entityComp = IOUtils.compressJson(entityList);

        memFree(gtmData);
        memFree(ftmData);
        memFree(stmData);

        try {
            ByteBuffer chunkData = ByteBuffer.allocate(metaBuffer.capacity() + gtmComp.capacity() + ftmComp.capacity() + stmComp.capacity() + entityComp.capacity());
            chunkData.put(metaBuffer);
            chunkData.put(gtmComp);
            chunkData.put(ftmComp);
            chunkData.put(stmComp);
            chunkData.put(entityComp);
            Files.write(chunkFile.toPath(), chunkData.array());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the state of this chunk.
     *
     * @return Activity state of this chunk.
     */
    public CHUNK_STATE getState() {
        return chunkState;
    }

    /**
     * Set the state of this chunk.
     *
     * @param chunkState New chunk state.
     */
    public void setState(CHUNK_STATE chunkState) {
        this.chunkState = chunkState;
    }
}
