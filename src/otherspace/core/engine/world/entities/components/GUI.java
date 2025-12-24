package otherspace.core.engine.world.entities.components;

import com.google.gson.JsonObject;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.session.Drawer;

import java.util.function.Consumer;

/**
 * Defines a GUI element for an entity, which a player can use to interact with it.
 */
public class GUI<E extends Entity> extends EntityComponent<E> {
    private final Rectanglei bounds;
    private final Consumer<Drawer> draw;

    /**
     * Create a new GUI with the specified bounds ((0, 0) is the screen center).
     *
     * @param bounds Bounds defining dimensions and offset of GUI panel.
     * @param draw Function defining what will be drawn to this GUI.
     */
    public GUI(E parent, Rectanglei bounds, Consumer<Drawer> draw) {
        super(parent);

        this.bounds = bounds;
        this.draw = draw;
    }

    /**
     * Get the GUI draw area.
     *
     * @return Draw area of GUI.
     */
    public Rectanglei getBounds() {
        return bounds;
    }

    /**
     * Draw this GUI.
     *
     * @param d Drawer to use.
     */
    public void draw(Drawer d) {
        draw.accept(d);
    }

    /**
     * Utility method to draw a back panel for a GUI.
     *
     * @param d Drawer to use.
     */
    public void drawBack(Drawer d) {
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(0, 0, bounds.lengthX(), bounds.lengthY()));
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public void deserialize(JsonObject json) {}
}
