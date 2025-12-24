package otherspace.game.entities;

import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import otherspace.core.engine.Sprite;
import otherspace.core.registry.EntityRegistry;

/**
 * Registers game entities.
 */
public class EntityRegistryHandler {
    private static final EntityRegistry entityRegistry = new EntityRegistry("base");

    /**
     * Register entities.
     */
    public static void register() {
        entityRegistry.registerEntity("player", Player.class, new Sprite("resources/sprites/entities/playerSpriteTest.png", 1, new Vector2i(16, 40), new Rectangled(-0.375f, -0.75, 0.375f, 0)));
        entityRegistry.registerEntity("dropped_item", DroppedItem.class, new Sprite(null, 1, null, new Rectangled(-0.5, -0.5, 0.5, 0.5)));
        entityRegistry.registerEntity("gravestone", Gravestone.class, new Sprite("resources/sprites/entities/gravestone.png", 1, new Vector2i(16, 32), new Rectangled(-0.5, -1, 0.5, 0)));
        entityRegistry.registerEntity("firepit", Firepit.class, new Sprite("resources/sprites/entities/firepit.png", 6, new Vector2i(16, 48), new Rectangled(-0.5, -0.6, 0.5, 0)));
        entityRegistry.registerEntity("wooden_crate", WoodenCrate.class, new Sprite("resources/sprites/entities/woodenCrate.png", 1, new Vector2i(16, 32), new Rectangled(-0.5, -1, 0.5, 0)));
        entityRegistry.registerEntity("rock_pile", RockPile.class, new Sprite("resources/sprites/entities/rockPile.png", 1, new Vector2i(16, 24), new Rectangled(-0.5f, -0.6f, 0.5f, 0f)));
        entityRegistry.registerEntity("loose_branch", LooseBranch.class, new Sprite("resources/sprites/entities/looseBranch.png", 1, new Vector2i(16, 24), new Rectangled(-0.5f, -0.6f, 0.5f, 0f)));
        entityRegistry.registerEntity("oak_tree", OakTree.class, new Sprite("resources/sprites/entities/oakTree.png", 1, new Vector2i(32, 96), new Rectangled(-0.5f, -2f, 0.5f, 0f)));
    }
}
