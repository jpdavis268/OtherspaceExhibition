package otherspace.core.engine.world.entities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import otherspace.core.engine.Sprite;
import otherspace.core.engine.utils.IOUtils;
import otherspace.core.engine.world.entities.components.EntityComponent;
import otherspace.core.engine.world.entities.components.GUI;
import otherspace.core.registry.EntityRegistry;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.scenes.world.InputHandler;
import otherspace.core.session.scenes.world.World;
import otherspace.core.session.scenes.world.ui.HUD;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for in game entities.
 */
public abstract class Entity {
    private final HashMap<Class<EntityComponent<? extends Entity>>, EntityComponent<? extends Entity>> components;

    public Vector2d position;
    protected Chunk myChunk;

    protected int depth;
    protected int spriteFrame;
    protected int spriteSpeed;
    private int spriteTimer;

    public Entity(Vector2d position) {
        World.getEntityList().add(this);

        this.position = position;
        depth = 0;
        spriteFrame = 0;
        spriteSpeed = 0;
        spriteTimer = 0;

        // Component initialization.
        components = new HashMap<>();

        // Add self to local chunk.
        int cX = (int) (position.x / 16);
        int cY = (int) (position.y / 16);
        myChunk = Chunk.getChunk(new Vector2i(cX, cY));

        if (myChunk == null) {
            myChunk = Chunk.loadChunk(new Vector2i(cX, cY));
        }

        if (myChunk != null) {
            myChunk.localEntities.add(this);
        }
        else {
            // If the chunk is still null, that means this entity should not exist (may modify this behavior later).
            destroy();
        }
    }

    /**
     * Update this entity during a game update.
     */
    public void update() {
        // Handle sprite animations (done here so they freeze when the game pauses)
        if (getSprite() != null && spriteSpeed > 0) {
            spriteTimer++;
            if (spriteTimer > 60d / spriteSpeed) {
                spriteFrame++;
                spriteTimer = 0;
            }
        }
        depth = (int) ((SceneManager.getCurrentScene().getCamera().getPosition().y - position.y) * 32);
    }

    /**
     * Render self to the game world during the draw phase.
     */
    public void drawSelf(Drawer d) {
        d.drawSprite(getSprite(), (int) (position.x * 32), (int) (position.y * 32), spriteFrame);
    }

    /**
     * Destroy this entity.
     */
    public void destroy() {
        World.getEntityList().remove(this);
        if (myChunk != null) {
            myChunk.localEntities.remove(this);
        }

        // If we have a GUI and the player is interacting with it, clear it.
        GUI<?> myGUI = getComponent(GUI.class);
        if (HUD.getCurrentInteraction() == myGUI) {
            HUD.setCurrentInteraction(null);
            InputHandler.setPlayerControlEnabled(true);
        }
    }

    /**
     * Get the sprite attached to this entity, if there is one.
     *
     * @return Entity sprite.
     */
    public Sprite getSprite() {
        return EntityRegistry.getSprite(getClass());
    }

    /**
     * Get the bounding box of this entity.
     *
     * @return Entity bounding box, or null if it has none.
     */
    public Rectangled getBounds() {
        Rectangled bounds = getSprite().getBoundingBox();
        if (bounds == null) {
            return null;
        }

        return new Rectangled(bounds).translate(position);
    }

    /**
     * Get the draw depth of this entity.
     *
     * @return Drawing depth of entity. Entities with a lower depth are rendered last.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Add a component to this entity.
     *
     * @param component Component to add.
     */
    @SuppressWarnings("unchecked")
    public <E extends EntityComponent<? extends Entity>> void addComponent(E component) {
        components.put((Class<EntityComponent<? extends Entity>>) component.getClass(), component);
    }

    /**
     * Attempt to fetch a component from this entity.
     *
     * @param componentType Component type.
     * @return Component, or null if it is not present.
     */
    @SuppressWarnings("unchecked")
    public <E extends EntityComponent<?>> E getComponent(Class<E> componentType) {
        return (E) components.get(componentType);
    }

    /**
     * Check if an entity has a specific component.
     *
     * @param componentType Type of component to look for.
     * @return Whether component exists.
     */
    public boolean hasA(Class<?> componentType) {
        return components.containsKey(componentType);
    }

    /**
     * Create a JSON object that stores this entities' data.
     * NOTE: In order to encourage composition, this cannot be directly overridden.
     * New entity data to save must be defined in components.
     *
     * @return JSON representation of entity.
     */
    public final JsonObject serialize() {
        JsonObject base = new JsonObject();
        base.add("type", new JsonPrimitive(getClass().getName()));
        base.add("position", new Gson().toJsonTree(position));
        JsonObject componentTree = new JsonObject();
        for (EntityComponent<? extends Entity> component : components.values()) {
            JsonObject componentJSON = component.serialize();
            if (componentJSON != null) {
                componentTree.add(component.getClass().getName(), componentJSON);
            }
        }
        base.add("components", componentTree);

        return base;
    }

    /**
     * Create an entity using a JSON representation.
     *
     * @param serial JSON representation of entity.
     */
    @SuppressWarnings("unchecked")
    public static void deserialize(JsonObject serial) {
        try {
            Class<?> entityType = Class.forName(serial.get("type").getAsString());
            Vector2d position = IOUtils.jsonToObject(serial.get("position"), Vector2d.class);
            Entity created = (Entity) entityType.getConstructor(Vector2d.class).newInstance(position);
            JsonObject componentTree = serial.getAsJsonObject("components");
            for (Map.Entry<String, JsonElement> obj : componentTree.entrySet()) {
                Class<? extends EntityComponent<?>> componentType = (Class<? extends EntityComponent<?>>) Class.forName(obj.getKey());
                EntityComponent<?> component = created.getComponent(componentType);
                component.deserialize(obj.getValue().getAsJsonObject());
            }
        }
        catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
               NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
