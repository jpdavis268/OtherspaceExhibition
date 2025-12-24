package otherspace.core.engine;

import otherspace.core.session.Drawer;

/**
 * Defines a "scene", which determines what is currently happening in the game.
 */
public abstract class Scene {
    private Menu currentMenu;
    private Color background = Color.BLACK;
    private Camera camera;

    /**
     * Set this scene's active menu.
     *
     * @param currentMenu New current menu.
     */
    public void setCurrentMenu(Menu currentMenu) {
        this.currentMenu = currentMenu;
    }

    /**
     * Get this scene's active menu.
     *
     * @return Current scene's menu.
     */
    public Menu getCurrentMenu() {
        return currentMenu;
    }

    /**
     * Set the camera for this scene.
     *
     * @param camera Camera to attach to scene.
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Get the camera attached to this scene.
     *
     * @return Scene camera.
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Get this scene's background color.
     *
     * @return Scene background color.
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Set this scene's background color.
     *
     * @param background New background color.
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
     * What happens in this scene during a game update.
     */
    public void update() {

    }

    /**
     * Draw scene to the screen.
     */
    public void draw(Drawer d) {

    }

    /**
     * Draw scene's UI to the screen.
     */
    public void drawGUI(Drawer d) {
        getCurrentMenu().draw(d);
    }
}
