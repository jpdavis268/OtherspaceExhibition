package otherspace.core.engine.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import otherspace.core.session.Session;
import otherspace.core.session.scenes.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility class for managing save files.
 */
public final class SaveUtils {
    private SaveUtils() {}

    /**
     * Save the current game.
     *
     * @param savePath Path to save file.
     */
    public static void saveGame(File savePath) {
        // Start saving process.
        System.out.println("Saving world...");
        long start = System.currentTimeMillis();

        // Update manifest.json
        long playtime = World.getPlayTime();
        String menuPlaytime = String.format("%dhr %dmin", playtime / 3600, playtime / 60 % 60);

        LocalDateTime currentTime = LocalDateTime.now();

        String date = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = currentTime.format(DateTimeFormatter.ofPattern("HH-mm"));

        JsonObject manifest = new JsonObject();
        manifest.add("date", new JsonPrimitive(date));
        manifest.add("time", new JsonPrimitive(time));
        manifest.add("version", new JsonPrimitive(Session.GAME_VERSION));
        manifest.add("playTime", new JsonPrimitive(menuPlaytime));
        IOUtils.saveJson(manifest, savePath.getPath() + "/manifest.json");

        // Save world
        World.saveWorld();

        // End saving process.
        long saveTime = System.currentTimeMillis() - start;
        System.out.println("World save finished in " + saveTime + "ms.");
    }

    /**
     * Retrieve save files.
     *
     * @return Array containing all save files within the saves directory.
     */
    public static File[] getSaves() {
        Path saves = Paths.get("saves");
        if (!Files.exists(saves)) {
            try {
                Files.createDirectory(saves);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new File("saves").listFiles();
    }

    /**
     * Get the manifest data from a list of save files.
     *
     * @param saveFiles List of save files.
     * @return Save manifest JSON Objects.
     */
    public static JsonObject[] getSaveManifests(File[] saveFiles) {
        JsonObject[] out = new JsonObject[saveFiles.length];

        for (int i = 0; i < out.length; i++) {
            out[i] = IOUtils.loadJson(saveFiles[i].getPath() + "/manifest.json");
        }

        return out;
    }


    /**
     * Get the total size of a save file.
     *
     * @param savePath Path of save file.
     * @return Total size, in bytes.
     */
    public static long getSaveSize(File savePath) {
        long bytes = 0;

        File[] contents = savePath.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isDirectory()) {
                    bytes += getSaveSize(f);
                }
                else {
                    bytes += f.length();
                }
            }
        }

        return bytes;
    }


    /**
     * Get size of save files in bulk to avoid constant potentially expensive tree navigation.
     *
     * @param saveFiles List of save files to get sizes of.
     * @return Array containing file sizes, in bytes.
     */
    public static long[] getSaveSizes(File[] saveFiles) {
        long[] out = new long[saveFiles.length];

        for (int i = 0; i < out.length; i++) {
            out[i] = getSaveSize(saveFiles[i]);
        }

        return out;
    }

    /**
     * Create a new save file.
     *
     * @param name Name of save file.
     * @param seed Seed of save file.
     * @return Path to created save file.
     */
    public static File createSave(String name, String seed, int defaultGM, int mapType) {
        // If the player input a seed, hash it and set the random seed to it.
        int saveSeed;
        if (!seed.isBlank()) {
            saveSeed = seed.hashCode();
        }
        else {
            saveSeed = new Random().nextInt();
        }

        // Create a new save file.
        String saveName = IOUtils.verifyFileName(name);
        String newSave = "saves/" + saveName;

        boolean created = false;
        while (!created) {
            if (Files.exists(Paths.get(newSave))) {
                newSave += "_";
            }
            else {
                try {
                    Files.createDirectory(Paths.get(newSave));
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                created = true;
            }
        }

        // Generate manifest
        // Get current date and time.
        LocalDateTime currentTime = LocalDateTime.now();

        String date = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = currentTime.format(DateTimeFormatter.ofPattern("HH-mm"));

        JsonObject manifest = new JsonObject();
        manifest.add("date", new JsonPrimitive(date));
        manifest.add("time", new JsonPrimitive(time));
        manifest.add("version", new JsonPrimitive(Session.GAME_VERSION));
        manifest.add("playTime", new JsonPrimitive("0hr 0min"));
        IOUtils.saveJson(manifest, newSave + "/manifest.json");

        JsonObject worldInfo = new JsonObject();
        worldInfo.add("defaultGM", new JsonPrimitive(defaultGM));
        worldInfo.add("mapType", new JsonPrimitive(mapType));
        worldInfo.add("seed", new JsonPrimitive(saveSeed));
        worldInfo.add("time", new JsonPrimitive(480));
        worldInfo.add("playTime", new JsonPrimitive(0));
        IOUtils.saveJson(worldInfo, newSave + "/worldInfo.json");

        return new File(newSave);
    }
}
