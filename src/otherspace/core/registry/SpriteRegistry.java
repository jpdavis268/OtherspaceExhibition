package otherspace.core.registry;

import otherspace.core.engine.Sprite;

import java.util.LinkedList;

/**
 * Handles the registration and loading of sprite resources when the game starts.
 */
public class SpriteRegistry {
    private static boolean initialized = false;
    private static LinkedList<Sprite> registryBuffer = new LinkedList<>();

    /**
     * Complete sprite registry. Must be done before any sprite can be queried.
     */
    public static LinkedList<Sprite> registerSprites() {
        if (initialized) {
            return null;
        }

        LinkedList<Sprite> finished = registryBuffer;
        registryBuffer = null;
        initialized = true;
        return finished;
    }

    /**
     * Register a new sprite.
     *
     * @param sprite Sprite to register.
     */
    public Sprite register(Sprite sprite) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add sprite after registry phase has been completed.");
        }

        registryBuffer.add(sprite);
        return sprite;
    }
}
