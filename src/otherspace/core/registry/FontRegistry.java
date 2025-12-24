package otherspace.core.registry;

import otherspace.core.engine.Font;

import java.util.LinkedList;

/**
 * Handles the registration and loading of font resources when the game starts.
 */
public class FontRegistry {
    private static boolean initialized = false;
    private static LinkedList<Font> registryBuffer = new LinkedList<>();

    /**
     * Complete sprite registry. Must be done before any sprite can be queried.
     */
    public static LinkedList<Font> registerFonts() {
        if (initialized) {
            return null;
        }

        LinkedList<Font> finished = registryBuffer;
        registryBuffer = null;
        initialized = true;
        return finished;
    }

    /**
     * Register a new font.
     *
     * @param font Font to register.
     */
    public Font register(Font font) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add sprite after registry phase has been completed.");
        }

        registryBuffer.add(font);
        return font;
    }
}
