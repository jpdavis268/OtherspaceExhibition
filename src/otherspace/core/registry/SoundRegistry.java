package otherspace.core.registry;

import otherspace.core.engine.Sound;

import java.util.LinkedList;

public class SoundRegistry {
    private static boolean initialized = false;
    private static LinkedList<Sound> registryBuffer = new LinkedList<>();

    /**
     * Complete sound registry. Must be done before any sound can be queried.
     */
    public static LinkedList<Sound> registerSounds() {
        if (initialized) {
            return null;
        }

        LinkedList<Sound> finished = registryBuffer;
        registryBuffer = null;
        initialized = true;
        return finished;
    }

    /**
     * Register a new sound.
     *
     * @param sound Sound to register.
     */
    public Sound register(Sound sound) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add sound after registry phase has been completed.");
        }

        registryBuffer.add(sound);
        return sound;
    }
}
