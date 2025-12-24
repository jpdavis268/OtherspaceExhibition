package otherspace.core.engine.guicomponents;

import otherspace.core.session.Drawer;

/**
 * Super class defining GUI components.
 */
public abstract class Component {
    int width;
    int height;
    Component parent;

    public Component(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Draw this component
     *
     * @param d Drawer to use.
     * @param x X Position to draw at.
     * @param y Y Position to draw at.
     */
    public abstract void draw(Drawer d, int x, int y);

    /**
     * Set a parent component for this one.
     *
     * @param parent New parent for this component.
     */
    public void setParent(Component parent) {
        this.parent = parent;
    }

    /**
     * Get the parent of this component, or null if none was ever defined.
     *
     * @return Component parent.
     */
    public Component getParent() {
        return parent;
    }
}
