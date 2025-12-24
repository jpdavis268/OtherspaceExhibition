package otherspace.core.engine;

import otherspace.core.session.Drawer;

/**
 * Defines a layer of objects within the game world, and handles their rendering.
 */
public abstract class Layer {
    private boolean visible = false;

    /**
     * Draw the contents of this layer to the world.
     *
     * @param d Drawer to use.
     */
    public void draw(Drawer d) {

    }

    /**
     * Get whether this layer is currently visible.
     *
     * @return Layer visibility.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set the visibility of this layer.
     *
     * @param visible Whether to render this layer.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
