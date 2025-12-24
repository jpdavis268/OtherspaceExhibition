package otherspace.core.engine.world.items;

/**
 * Defines an item drop, with an ItemStack to drop and a percent chance of dropping.
 */
public record ItemDrop(ItemStack toDrop, float chance) {
}
