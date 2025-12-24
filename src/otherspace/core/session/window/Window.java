package otherspace.core.session.window;

import com.google.gson.JsonPrimitive;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import otherspace.core.session.SettingsManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Holds information about the game window, and provides access to the running GLFW context.
 */
public class Window {
    private static Window singleton;

    private final long windowHandle;
    private final RenderHandler renderHandler;
    private final KeyListener keyListener;
    private final MouseListener mouseListener;
    private boolean isFullscreen;
    private int width;
    private int height;

    private final GLFWWindowSizeCallback windowSizeCallback;
    private final GLFWKeyCallback keyCallback;
    private final GLFWCharCallback charCallback;
    private final GLFWCursorPosCallback posCallback;
    private final GLFWMouseButtonCallback mouseCallback;
    private final GLFWScrollCallback scrollCallback;

    public Window(int width, int height, String title) {
        singleton = this;
        System.out.println("Opening game window.");

        // Set GLFW error callback.
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW.
        if (!glfwInit()) {
            throw new IllegalStateException("ERROR: Failed to open game window: Could not initialize GLFW context.");
        }

        // Create window.
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        this.width = width;
        this.height = height;
        if (windowHandle == 0) {
            glfwTerminate();
            throw new RuntimeException("ERROR: Failed to open game window: GLFW could not create window.");
        }

        // Create OpenGL context.
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        // Disable VSync
        glfwSwapInterval(0);

        // Create callbacks.
        // Window Resize
        GLFWWindowSizeCallbackI resizeCallback = (_, wWidth, wHeight) -> {
            // Adjust window dimensions.
            glViewport(0, 0, wWidth, wHeight);
            this.width = wWidth;
            this.height = wHeight;
        };
        windowSizeCallback = glfwSetWindowSizeCallback(windowHandle, resizeCallback);
        resizeCallback.invoke(windowHandle, width, height);

        // Keyboard
        keyListener = new KeyListener();
        keyCallback = glfwSetKeyCallback(windowHandle, keyListener::keyCallback);
        charCallback = glfwSetCharCallback(windowHandle, keyListener::charCallback);

        // Mouse
        mouseListener = new MouseListener(this);
        posCallback = glfwSetCursorPosCallback(windowHandle, mouseListener::mousePosCallback);
        mouseCallback = glfwSetMouseButtonCallback(windowHandle, mouseListener::mouseButtonCallback);
        scrollCallback = glfwSetScrollCallback(windowHandle, mouseListener::mouseScrollCallback);

        // Finish window creation.
        renderHandler = new RenderHandler(windowHandle);
        System.out.println("Window creation finished.");
    }

    /**
     * Fetch user inputs.
     */
    public void pollEvents() {
        keyListener.clear();
        mouseListener.clear();
        glfwPollEvents();
        if (KeyListener.checkPressed(SettingsManager.getKeybind("fullscreen_bind"))) {
            // Toggle fullscreen
            Window.setFullscreen(!Window.getFullscreen());
            SettingsManager.set("fullscreen", new JsonPrimitive(Window.getFullscreen()));
        }
    }

    /**
     * Toggle fullscreen.
     *
     * @param makeFullscreen Whether to make the window fullscreen (true) or windowed (false).
     */
    public static void setFullscreen(boolean makeFullscreen) {
        // If makeFullscreen is true, make this window fullscreen, otherwise make it windowed.
        if (makeFullscreen) {
            // Get monitor dimensions
            GLFWVidMode monitorProperties = glfwGetVideoMode(glfwGetPrimaryMonitor());

            int mWidth, mHeight;
            if (monitorProperties != null) {
                mWidth = monitorProperties.width();
                mHeight = monitorProperties.height();
            }
            else {
                throw new IllegalStateException("ERROR: Failed to make game window fullscreen: No primary monitor found.");
            }
            glfwSetWindowMonitor(singleton.windowHandle, glfwGetPrimaryMonitor(), 0, 0, mWidth, mHeight, GLFW_DONT_CARE);
        }
        else {
            glfwSetWindowMonitor(singleton.windowHandle, 0, 50, 100, 1280, 720, GLFW_DONT_CARE);
        }
        singleton.isFullscreen = makeFullscreen;
    }

    /**
     * Return whether the current window is fullscreen.
     *
     * @return Whether this window is fullscreen.
     */
    public static boolean getFullscreen() {
        return singleton.isFullscreen;
    }

    /**
     * Get the current window width.
     *
     * @return Window width.
     */
    public static int getWidth() {
        return singleton.width;
    }

    /**
     * Get the current window height.
     *
     * @return Window height.
     */
    public static int getHeight() {
        return singleton.height;
    }

    /**
     * Get the render handler attached to this window.
     *
     * @return Rendering handler.
     */
    public RenderHandler getRenderHandler() {
        return renderHandler;
    }

    /**
     * Get whether the window has been closed by the user.
     *
     * @return If window is closed.
     */
    public boolean isWindowClosed() {
        return glfwWindowShouldClose(windowHandle);
    }

    /**
     * Close the game window.
     */
    public static void closeWindow() {
        glfwSetWindowShouldClose(singleton.windowHandle, true);
    }

    /**
     * Destroy this window.
     */
    public void dispose() {
        if (windowSizeCallback != null) windowSizeCallback.close();
        if (keyCallback != null) keyCallback.close();
        if (charCallback != null) charCallback.close();
        if (posCallback != null) posCallback.close();
        if (mouseCallback != null) mouseCallback.close();
        if (scrollCallback != null) scrollCallback.close();

        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
}
