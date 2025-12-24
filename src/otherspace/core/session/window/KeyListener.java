package otherspace.core.session.window;

import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Key listener singleton that is attached to a window to read keyboard input.
 */
public class KeyListener {
    // Singleton
    private static KeyListener singleton;

    private final boolean[] keyHeld = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] keyReleased = new boolean[GLFW_KEY_LAST + 1];

    private char lastChar = '\0';
    private int lastKey = GLFW_KEY_UNKNOWN;
    private int keyMods = 0;

    public KeyListener() {
        singleton = this;
    }

    /**
     * Handle key input.
     *
     * @param windowHandle GLFW window.
     * @param key Key pressed.
     * @param scancode Code of key pressed.
     * @param action What the user did.
     * @param mods Other keys being held (ctrl, alt, etc.)
     */
    public void keyCallback(long windowHandle, int key, int scancode, int action, int mods) {
        key = Math.max(key, 0);

        switch (action) {
            case GLFW_PRESS:
                keyPressed[key] = true;
            case GLFW_REPEAT:
                keyHeld[key] = true;
                lastKey = key;
                break;
            case GLFW_RELEASE:
                keyHeld[key] = false;
                keyReleased[key] = true;
                break;
        }

        // Update key mods.
        keyMods = mods;
    }

    /**
     * Handle text input
     *
     * @param window GLFW window (for initialization only).
     * @param codepoint UTF code of input character
     */
    public void charCallback(long window, int codepoint) {
        lastChar = Character.toChars(codepoint)[0];
    }

    /**
     * Clear pressed and released keys.
     */
    public void clear() {
        keyPressed = new boolean[GLFW_KEY_LAST + 1];
        keyReleased = new boolean[GLFW_KEY_LAST + 1];
    }

    /**
     * Check if a key was pressed this frame.
     *
     * @param keycode Key to check.
     * @return Whether key was pressed.
     */
    public static boolean checkPressed(int keycode) {
        return singleton.keyPressed[keycode];
    }

    /**
     * Check if a key is currently held down.
     *
     * @param keycode Key to check.
     * @return Whether key is currently held.
     */
    public static boolean checkHeld(int keycode) {
        return singleton.keyHeld[keycode];
    }

    /**
     * Check if a key was released this frame.
     *
     * @param keycode Key to check.
     * @return Whether key was released.
     */
    public static boolean checkReleased(int keycode) {
        return singleton.keyReleased[keycode];
    }

    /**
     * Clear the state of a key.
     *
     * @param keycode Key to clear.
     */
    public static void clearKey(int keycode) {
        singleton.keyPressed[keycode] = false;
        singleton.keyHeld[keycode] = false;
        singleton.keyReleased[keycode] = false;
    }

    /**
     * Get the last character typed.
     *
     * @return Last character typed.
     */
    public static char getLastChar() {
        return singleton.lastChar;
    }

    /**
     * Reset the last character typed.
     */
    public static void clearLastChar() {
        singleton.lastChar = '\0';
    }


    /**
     * Return the last key pressed.
     *
     * @return Last key pressed.
     */
    public static int getLastKey() {
        return singleton.lastKey;
    }


    /**
     * Clear the last key pressed.
     */
    public static void clearLastKey() {
        singleton.lastKey = GLFW_KEY_UNKNOWN;
    }

    /**
     * Get whether the control modifier is active
     *
     * @return Whether control modifier is active.
     */
    public static boolean isCtrlPressed() {
        return (singleton.keyMods & GLFW_MOD_CONTROL) >= 1;
    }

    /**
     * Return the current clipboard string.
     *
     * @return Current clipboard string.
     */
    public static String getClipboardString() {
        // Temporarily set the GLFW error callback to null to avoid the console getting flooded if nothing is in the
        // clipboard.
        glfwSetErrorCallback(null);
        String clipboard = glfwGetClipboardString(0);
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        return clipboard;
    }
}
