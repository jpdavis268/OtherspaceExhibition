package otherspace.core.engine.world.entities.components;

import com.google.gson.JsonObject;
import otherspace.core.engine.world.entities.Entity;

/**
 * Defines a component of an entity, which can be added to support certain behavior.
 */
public abstract class EntityComponent<E extends Entity> {
    private final E myParent;

    public EntityComponent(E parent) {
        myParent = parent;
    }

    /**
     * Get the entity containing this component.
     *
     * @return Component parent.
     */
    public E getParent() {
        return myParent;
    }

    /**
     * Convert this component into a JSON tree.
     *
     * @return JSON representation of component.
     */
    public abstract JsonObject serialize();

    /**
     * Initialize this component using JSON data.
     *
     * @param json Component JSON data.
     */
    public abstract void deserialize(JsonObject json);
}
