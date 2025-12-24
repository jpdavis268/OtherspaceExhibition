package otherspace.core.engine.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.lwjgl.system.MemoryUtil.memSlice;
import static org.lwjgl.util.zstd.Zstd.*;

/**
 * Utility class to simplify IO operations.
 */
public final class IOUtils {
    private IOUtils() {}

    /**
     * Ensure that a given file name is valid.
     *
     * @param givenName String provided by player.
     * @return String adjusted to ensure it can be created by the file system.
     */
    public static String verifyFileName(String givenName) {
        // Trim outer spaces.
        givenName = givenName.trim();

        // Determine illegal characters and words.
        Pattern invalidChars = Pattern.compile(("[\\\\/:.\0*?\"<>|]"));
        String[] reservedWords;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // A regex would be better, but I can't be bothered and this will almost never be executed anyway.
            reservedWords = new String[]{"CON", "PRN", "AUX", "NUL",
                    "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                    "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT7=8", "LPT9"
            };
        }
        else {
            reservedWords = new String[0];
        }

        givenName = invalidChars.matcher(givenName).replaceAll("");
        for (String r : reservedWords) {
            if (givenName.equalsIgnoreCase(r)) {
                givenName = "";
                break;
            }
        }

        return givenName.trim();
    }

    /**
     * Delete a directory and everything inside of it.
     *
     * @param toDelete Directory to delete.
     */
    public static void deleteDirectory(File toDelete) {
        if (toDelete != null && toDelete.isDirectory()) {
            try {
                // Loop through directory and recursively delete anything inside.
                File[] contents = toDelete.listFiles();
                if (contents != null) {
                    for (File f : contents) {
                        if (f.isFile()) {
                            Files.delete(f.toPath());
                        }
                        else {
                            deleteDirectory(f);
                        }
                    }
                }

                // Delete emptied parent directory.
                Files.delete(toDelete.toPath());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Copy a directory and its contents.
     *
     * @param source Source directory.
     * @param destination Desired path of new directory.
     */
    public static void copyDirectory(File source, File destination) {
        if (source != null && source.isDirectory()) {
            try {
                // Copy root
                Files.copy(source.toPath(), destination.toPath(), REPLACE_EXISTING);

                // Recursively copy anything inside.
                File[] contents = source.listFiles();
                if (contents != null) {
                    for (File f : contents) {
                        File fileDestination = new File(destination, f.getName());
                        if (f.isFile()) {
                            Files.copy(f.toPath(), fileDestination.toPath(), REPLACE_EXISTING);
                        }
                        else {
                            copyDirectory(f, fileDestination);
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Load a json object from a given JSON file.
     *
     * @param filepath JSON file to load.
     * @return Created JSON object.
     */
    public static JsonObject loadJson(String filepath) {
        try {
            return new Gson().fromJson(Files.readString(new File(filepath).toPath()), JsonObject.class);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file at " + filepath);
        }
    }

    /**
     * Save a json object to a given JSON file.
     *
     * @param jsonObject JSON Object to save.
     * @param filePath File to save to.
     */
    public static void saveJson(JsonObject jsonObject, String filePath) {
        saveJson(jsonObject, filePath, false);
    }

    /**
     * Save a json object to a given JSON file.
     *
     * @param jsonObject JSON Object to save.
     * @param filePath File to save to.
     * @param prettify Whether to format the JSON string for better human readability.
     */
    public static void saveJson(JsonObject jsonObject, String filePath, boolean prettify) {
        // Create JSON
        Gson gson;
        if (prettify) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        else {
            gson = new Gson();
        }
        String json = gson.toJson(jsonObject);

        // Save JSON to file
        try {
            Files.writeString(Paths.get(filePath), json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save JSON file to " + filePath);
        }
    }

    /**
     * Construct an object from a JSON element.
     *
     * @param jsonElement JSON element to use.
     * @param objClass Class of target object.
     * @return Instance of target object created from JSON object.
     */
    public static <T> T jsonToObject(JsonElement jsonElement, Class<T> objClass) {
        return new Gson().fromJson(jsonElement, objClass);
    }

    /**
     * Compress a JSON object into a byte buffer.
     *
     * @param json Input json object.
     * @return Bytebuffer containing compressed data.
     */
    public static ByteBuffer compressJson(JsonElement json) {
        // Create JSON
        String jsonString = new Gson().toJson(json);
        byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = BufferUtils.createByteBuffer(jsonBytes.length);
        buffer.put(jsonBytes);
        return compressBuffer(buffer);
    }

    /**
     * Compress a bytebuffer using Zlib.
     *
     * @param in Uncompressed buffer.
     * @return Compressed buffer, with an integer at the start detailing its size.
     */
    public static ByteBuffer compressBuffer(ByteBuffer in) {
        in.clear();

        ByteBuffer compressed = BufferUtils.createByteBuffer((int) ZSTD_compressBound(in.remaining()));
        long compressedSize = ZSTD_compress(compressed, in, ZSTD_CLEVEL_DEFAULT);
        compressed.limit((int) compressedSize);

        ByteBuffer out = BufferUtils.createByteBuffer(compressed.limit() + Integer.BYTES);
        out.putInt(compressed.limit());
        out.put(compressed);
        out.flip();
        return out;
    }

    /**
     * Load one or more blobs of compressed data from a file.
     *
     * @param compressedData File containing compressed data.
     * @return Array of output data buffers.
     */
    public static ByteBuffer[] loadCompressedBuffers(File compressedData) {
        try (SeekableByteChannel in = Files.newByteChannel(compressedData.toPath())) {
            ByteBuffer data = BufferUtils.createByteBuffer((int) in.size() + 1);
            while (in.read(data) != -1) {
                ;
            }
            return loadCompressedBuffers(data);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load one or more blobs of compressed data from a buffer.
     *
     * @param compressed Compressed data.
     * @return Array of output data buffers.
     */
    public static ByteBuffer[] loadCompressedBuffers(ByteBuffer compressed) {
        compressed.clear();
        LinkedList<ByteBuffer> buffers = new LinkedList<>();

        while (compressed.remaining() > 1) {
            int frameSize = compressed.getInt();
            ByteBuffer frame = memSlice(compressed, 0, frameSize);
            ByteBuffer decomp = BufferUtils.createByteBuffer((int) ZSTD_getFrameContentSize(frame));
            ZSTD_decompress(decomp, frame);
            buffers.add(decomp);
            compressed.position(compressed.position() + frameSize);
        }

        return buffers.toArray(new ByteBuffer[0]);
    }
}
