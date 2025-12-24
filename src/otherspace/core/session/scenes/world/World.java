package otherspace.core.session.scenes.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joml.Vector2i;
import otherspace.core.engine.Layer;
import otherspace.core.engine.utils.IOUtils;
import otherspace.core.engine.utils.SaveUtils;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.layers.*;
import otherspace.game.Assets;
import otherspace.game.entities.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains and manages the game world, and renders all chunks and entities.
 */
public class World {
    private static World singleton;

    // World data
    private final LinkedList<Layer> layers;
    private final HashMap<Vector2i, Chunk> chunkMap;
    private final Set<Entity> entities;

    // World info
    private final File savePath;
    private int seed;
    private int defaultGM;
    private int mapType;

    // Time
    private int day;
    private String time;
    private String dayPhase;
    private long sec;
    private long playTime;
    private byte tick;

    // Ambience
    private SoundManager.AudioSource dayAmbienceID = null;
    private SoundManager.AudioSource nightAmbienceID = null;

    public World(File savePath) {
        singleton = this;

        // Initialize world
        layers = new LinkedList<>();
        layers.add(new GroundTileLayer());
        layers.add(new FloorTileLayer());
        layers.add(new SolidTileLayer());
        layers.add(new EntityLayer());
        layers.add(new LightingLayer());
        layers.add(new DebugLayer());
        layers.add(new UILayer());

        chunkMap = new HashMap<>();
        entities = ConcurrentHashMap.newKeySet();

        // Load world data
        this.savePath = savePath;
        loadWorld();

        // Spawn player
        new Player(false);
    }

    /**
     * Update world and entities for this frame.
     */
    public void update() {
        updateTime();

        // Update entities
        for (Entity e : entities) {
            e.update();
        }

        // Update camera
        ((GameCamera) SceneManager.getCurrentScene().getCamera()).updateCamera();
    }

    /**
     * Update the in-game time and its related elements.
     */
    private void updateTime() {
        // Time management.
        tick++;
        if (tick >= 60) {
            tick = 0;
            sec++;
            // This is going to get inaccurate over time, especially on a laggy world, but it does allow for real-time playtime statistics.
            playTime++;
            int autosaveTimer = switch (SettingsManager.get("autosave_interval").getAsInt()) {
                case 0 -> 360;
                case 1 -> 720;
                case 3 -> 1440;
                case 4 -> 2160;
                case 5 -> 4320;
                default -> 1080;
            };
            if (sec % autosaveTimer == 0) {
                // Autosave
                SaveUtils.saveGame(savePath);
            }
        }

        // Get clock values.
        day = (int) ((sec / 1440) + 1);
        int cMin = (int) (sec / 60 % 24);
        int cSec = (int) (sec % 60);

        // Format time
        boolean _24hr = SettingsManager.get("time_format").getAsBoolean();
        if (_24hr) {
            // 24 hour time
            time = String.format("%02d:%02d", cMin, cSec);
        }
        else {
            // AM/PM
            String suffix = cMin < 12 ? "AM" : "PM";
            int apMin = cMin % 12;
            apMin = apMin == 0 ? 12 : apMin;
            time = String.format("%02d:%02d %s", apMin, cSec, suffix);
        }

        // Day phase
        if (cMin < 6 || cMin >= 22) dayPhase = SettingsManager.getText("hud_night");
        else if (cMin < 9) dayPhase = SettingsManager.getText("hud_early_morning");
        else if (cMin < 12) dayPhase = SettingsManager.getText("hud_late_morning");
        else if (cMin < 15) dayPhase = SettingsManager.getText("hud_early_afternoon");
        else if (cMin < 18) dayPhase = SettingsManager.getText("hud_late_afternoon");
        else if (cMin < 20) dayPhase = SettingsManager.getText("hud_evening");
        else dayPhase = SettingsManager.getText("hud_dusk");


        // Ambience
        if (dayAmbienceID == null) {
            dayAmbienceID = SoundManager.playSound(Assets.forestAmbienceDay, true);
            nightAmbienceID = SoundManager.playSound(Assets.forestAmbienceNight, true);
        }

        // Dynamic Ambience Controls
        SoundManager.setGain(dayAmbienceID, getDayFactor());
        SoundManager.setGain(nightAmbienceID, 1 - getDayFactor());
    }


    /**
     * Calculate the time of day as a range between 0 (night) and 1 (broad daylight).
     * Used for factors such as calculating ambient light or sound.
     *
     * @return Time of day.
     */
    public static float getDayFactor() {
        int cMin = (int) (singleton.sec / 60 % 24);
        int cSec = (int) (singleton.sec % 60);

        if (cMin >= 20 && cMin <= 21) {
            // Dusk
            float seconds = (cMin - 20) * 60 + cSec;
            return 1 - (seconds * 60 + singleton.tick) / (120 * 60);
        }
        else if (cMin < 6 || cMin >= 22) {
            // Night
            return 0;
        }
        else if (cMin < 9) {
            // Dawn
            float seconds = (cMin - 6) * 60 + cSec;
            return (seconds * 60 + singleton.tick) / (180 * 60);
        }
        else {
            // Day
            return 1;
        }
    }

    /**
     * Draw game world layers.
     *
     * @param d Drawer to use.
     */
    public void draw(Drawer d) {
        for (Layer l : layers) {
            l.draw(d);
        }
    }

    /**
     * SAVE the World!
     */
    public static void saveWorld() {
        // Save general world info.
        JsonObject worldInfo = new JsonObject();
        worldInfo.add("defaultGM", new JsonPrimitive(singleton.defaultGM));
        worldInfo.add("mapType", new JsonPrimitive(singleton.mapType));
        worldInfo.add("seed", new JsonPrimitive(singleton.seed));
        worldInfo.add("time", new JsonPrimitive(singleton.sec));
        worldInfo.add("playTime", new JsonPrimitive(singleton.playTime));
        IOUtils.saveJson(worldInfo, singleton.savePath.getPath() + "/worldInfo.json");

        // Save player data
        Player.savePlayerData();

        // Save chunks
        for (Chunk c : getChunkMap().values()) {
            c.saveChunk();
        }
    }

    /**
     * Load data from our save file when the player opens the world.
     */
    private void loadWorld() {
        // Load world info.
        if (new File(getSavePath(), "/worldInfo.json").exists()) {
            JsonObject worldInfo = IOUtils.loadJson(getSavePath() + "/worldInfo.json");
            JsonElement defaultGM = worldInfo.get("defaultGM");
            JsonElement mapType = worldInfo.get("mapType");
            JsonElement seed = worldInfo.get("seed");
            JsonElement time = worldInfo.get("time");
            JsonElement playtime = worldInfo.get("playTime");

            this.defaultGM = defaultGM == null ? 0 : defaultGM.getAsInt();
            this.mapType = mapType == null ? 0 : mapType.getAsInt();
            this.seed = seed == null ? new Random().nextInt() : seed.getAsInt();
            this.sec = time == null ? 480 : time.getAsLong();
            this.playTime = playtime == null ? 0 : playtime.getAsLong();
        }

        // Load chunk data
        // TODO: Implement lazy loading, and a flag system to prevent useless wilderness chunks from being loaded.
        File worldData = new File(getSavePath(), "/world/");
        if (!worldData.exists()) {
            try {
                Files.createDirectory(worldData.toPath());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File[] chunkFiles = worldData.listFiles();
        if (chunkFiles != null) {
            for (File file : chunkFiles) {
                String name = file.getName();
                String[] coords = name.split("_");
                Vector2i chunkCoords = new Vector2i(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                Chunk.loadChunk(chunkCoords);
            }
        }
    }

    /**
     * Get the path of this world's corresponding save file.
     *
     * @return Save file path.
     */
    public static File getSavePath() {
        return singleton.savePath;
    }

    /**
     * Get the current day in game.
     *
     * @return Current day.
     */
    public static int getDay() {
        return singleton.day;
    }

    /**
     * Get a formatted string detailing the current time.
     *
     * @return Current time.
     */
    public static String getTime() {
        return singleton.time;
    }

    /**
     * Get the current period of the current day.
     *
     * @return String detailing what time of day it is.
     */
    public static String getDayPhase() {
        return singleton.dayPhase;
    }

    /**
     * Get the total playtime on this save.
     *
     * @return Total playtime in seconds.
     */
    public static long getPlayTime() {
        return singleton.playTime;
    }

    /**
     * Get the list of all entities currently loaded into the world.
     *
     * @return All currently loaded entities.
     */
    public static Set<Entity> getEntityList() {
        return singleton.entities;
    }

    /**
     * Get the map of all currently loaded chunks.
     *
     * @return Hashmap matching chunk coordinates to loaded chunks.
     */
    public static HashMap<Vector2i, Chunk> getChunkMap() {
        return singleton.chunkMap;
    }

    /**
     * Get the default gamemode.
     *
     * @return Default gamemode.
     */
    public static int getDefaultGM() {
        return singleton.defaultGM;
    }

    /**
     * Get the generation type of the map.
     *
     * @return Generation map type.
     */
    public static int getMapType() {
        return singleton.mapType;
    }

    /**
     * Get the current world seed.
     *
     * @return Current world seed.
     */
    public static int getSeed() {
        return singleton.seed;
    }

    /**
     * Set the current in-game time.
     *
     * @param sec Time, as number of in game seconds after midnight on day 1.
     */
    public static void setTime(long sec) {
        singleton.sec = sec;
    }

    /**
     * Set the default gamemode for the world.
     *
     * @param gm New default gamemode.
     */
    public static void setDefaultGM(int gm) {
        singleton.defaultGM = gm;
    }
}
