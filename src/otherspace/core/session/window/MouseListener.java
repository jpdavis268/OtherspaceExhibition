package otherspace.core.session.window;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Mouse listener singleton that is attached to a window to read mouse input.
 */
public class MouseListener {
    // Singleton
    private static MouseListener singleton;

    private final Window window;

    private final boolean[] mouseButtonHeld = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private boolean[] mouseButtonPressed = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private boolean[] mouseButtonReleased = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

    private double mouseX = 0;
    private double mouseY = 0;
    private double lastX = 0;
    private double lastY = 0;
    private double scrollY = 0;

    public MouseListener(Window window) {
        singleton = this;
        this.window = window;
    }

    /**
     * Handle mouse movement.
     *
     * @param window Window (for GLFW initialization only).
     * @param xPos X-Position of mouse.
     * @param yPos Y-Position of mouse.
     */
    public void mousePosCallback(long window, double xPos, double yPos) {
        mouseX = xPos;
        mouseY = yPos;
    }

    /**
     * Handle mouse input.
     *
     * @param window Window (for GLFW initialization only).
     * @param button Button in question.
     * @param action What was done to the button in question.
     * @param mods What was done while what was done to the button in question was done.
     */
    public void mouseButtonCallback(long window, int button, int action, int mods) {
        switch (action) {
            case GLFW_PRESS:
                mouseButtonHeld[button] = true;
                mouseButtonPressed[button] = true;
                break;
            case GLFW_RELEASE:
                mouseButtonHeld[button] = false;
                mouseButtonReleased[button] = true;
                break;
        }
    }

    /**
     * Handle mouse scrolling.
     *
     * @param window Window (for GLFW initialization only).
     * @param xOffset How far we scrolled on the... x-axis?
     * @param yOffset How far we scrolled.
     */
    public void mouseScrollCallback(long window, double xOffset, double yOffset) {
        scrollY = yOffset;
    }

    /**
     * Clear data from this frame and prepare for the next.
     */
    public void clear() {
        scrollY = 0;
        lastX = mouseX;
        lastY = mouseY;

        // Clear pressed and released buttons.
        mouseButtonPressed = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
        mouseButtonReleased = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    }


    /**
     * Get the x-coordinate of the mouse.
     *
     * @return x-coordinate of the mouse.
     */
    public static double getMouseX() {
        return Math.clamp(singleton.mouseX, 0, singleton.window.getWidth());
    }

    /**
     * Get the y-coordinate of the mouse.
     *
     * @return y-coordinate of the mouse.
     */
    public static double getMouseY() {
        return Math.clamp(singleton.mouseY, 0, singleton.window.getHeight());
    }

    /**
     * Get the last x-coordinate of the mouse.
     *
     * @return Last x-coordinate of the mouse.
     */
    public static double getLastMouseX() {
        return singleton.lastX;
    }

    /**
     * Get the last y-coordinate of the mouse.
     *
     * @return Last y-coordinate of the mouse.
     */
    public static double getLastMouseY() {
        return singleton.lastY;
    }

    /**
     * Get the scroll distance for this frame.
     *
     * @return Scroll distance for this frame.
     */
    public static double getScroll() {
        return singleton.scrollY;
    }

    /**
     * Check if a specific button was pressed this frame.
     *
     * @param button Button to check.
     * @return Whether button was pressed.
     */
    public static boolean checkPressed(int button) {
        return singleton.mouseButtonPressed[button];
    }

    /**
     * Check if a specific button is held down on the mouse.
     *
     * @param button Button to check.
     * @return Whether button is held.
     */
    public static boolean checkHeld(int button) {
        return singleton.mouseButtonHeld[button];
    }

    /**
     * Check if a specific button was released this frame.
     *
     * @param button Button to check.
     * @return Whether button was released.
     */
    public static boolean checkReleased(int button) {
        return singleton.mouseButtonReleased[button];
    }

    /**
     * Clear a mouse button.
     *
     * @param button Button to clear.
     */
    public static void clearButton(int button) {
        singleton.mouseButtonPressed[button] = false;
        singleton.mouseButtonHeld[button] = false;
        singleton.mouseButtonReleased[button] = false;
    }
}
