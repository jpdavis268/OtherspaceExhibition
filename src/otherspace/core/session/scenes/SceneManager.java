package otherspace.core.session.scenes;

import org.joml.Vector2i;
import otherspace.core.engine.Scene;
import otherspace.core.session.Drawer;
import otherspace.core.session.window.MouseListener;

/**
 * Controls the active game scene, and stores global information needed for transitioning between states.
 */
public class SceneManager {
    private static SceneManager singleton;

    private Scene currentScene;
    private String hoverText = "";

    private static double drawSceneTime = 0;
    private static double drawGUITime = 0;

    public SceneManager() {
        singleton = this;
    }

    /**
     * Draw the current scene to the screen.
     */
    public void drawCurrentScene(Drawer d) {
        long drawStart = System.nanoTime();
        currentScene.draw(d);
        long drawTime = System.nanoTime() - drawStart;
        drawSceneTime = (double) drawTime / 1000000;
    }

    /**
     * Draw the current scene's UI elements.
     */
    public void drawSceneGUI(Drawer d) {
        long drawStart = System.nanoTime();
        currentScene.drawGUI(d);

        if (!hoverText.isEmpty()) {
            d.drawTextBox(new Vector2i((int) MouseListener.getMouseX(), (int) MouseListener.getMouseY() + 12), hoverText);
            hoverText = "";
        }

        long drawTime = System.nanoTime() - drawStart;
        drawGUITime = (double) drawTime / 1000000;
    }

    /**
     * Change the active scene.
     *
     * @param nextScene New scene to change to.
     */
    public static void changeScene(Scene nextScene) {
        singleton.currentScene = nextScene;
    }

    /**
     * Get the current game scene.
     *
     * @return Current game scene.
     */
    public static Scene getCurrentScene() {
        return singleton.currentScene;
    }

    /**
     * Set a text key to render in a hovering text box.
     *
     * @param text Lang key for text to put in box.
     */
    public static void setHoverText(String text) {
        singleton.hoverText = text;
    }

    public static double getDrawSceneTime() {
        return drawSceneTime;
    }

    public static double getDrawGUITime() {
        return drawGUITime;
    }
}
