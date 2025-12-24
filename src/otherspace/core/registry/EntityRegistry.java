package otherspace.core.registry;

import otherspace.core.engine.Sprite;
import otherspace.core.engine.world.entities.Entity;

import java.util.HashMap;

/**
 * Handles the registration of game entities and their required assets.
 */
public class EntityRegistry {
    private static boolean initialized = false;
    private static final HashMap<String, Class<? extends Entity>> nameMapping = new HashMap<>();
    private static final HashMap<Class<? extends Entity>, Sprite> spriteMapping = new HashMap<>();

    /**
     * Complete entity registry (does nothing but set a flag at the moment).
     */
    public static void registerEntities() {
        initialized = true;
    }

    /**
     * Get an entity class using its handle.
     *
     * @param handle Mod and name of entity.
     * @return Class of entity.
     */
    public static Class<? extends Entity> get(String handle) {
        return nameMapping.get(handle);
    }

    /**
     * Get the sprite of an entity.
     *
     * @param entity Entity class.
     * @return Sprite associated with entity.
     */
    public static Sprite getSprite(Class<? extends Entity> entity) {
        return spriteMapping.get(entity);
    }

    String modHandle;
    SpriteRegistry entitySpriteRegistry;

    public EntityRegistry(String modHandle) {
        this.modHandle = modHandle;
        entitySpriteRegistry = new SpriteRegistry();
    }

    public void registerEntity(String name, Class<? extends Entity> entity, Sprite entitySprite) {
        if (initialized) {
            throw new IllegalStateException("ERROR: Cannot add entity after registry phase has been completed!");
        }

        String entityHandle = modHandle + "/" + name;
        nameMapping.put(entityHandle, entity);
        spriteMapping.put(entity, entitySprite);
        entitySpriteRegistry.register(entitySprite);
    }
}
