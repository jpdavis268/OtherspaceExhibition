package otherspace.core.engine;

import otherspace.core.session.Drawer;

/**
 * Defines a collection of UI elements and their functionality.
 */
public abstract class Menu {
    /**
     * Draw the contents of this menu. Should typically only be called during the GUI phase.
     *
     * @param d Drawer to use.
     */
    public abstract void draw(Drawer d);
}
