package otherspace.core.engine.world.items;

/**
 * Specialized form of inventory that only allows certain items. Commonly used for machine inputs.
 */
public class InputInventory extends Inventory {
    private final Class<? extends ItemTag>[] whitelist;

    @SafeVarargs
    public InputInventory(int slots, Class<? extends ItemTag>... whitelist) {
        super(slots);
        this.whitelist = whitelist;
    }

    /**
     * Check if a specific item is allowed.
     *
     * @param itemID ID of item to check.
     * @return Whether this item is allowed.
     */
    public boolean isAllowed(int itemID) {
        Item toCheck = Item.get(itemID);

        for (Class<? extends ItemTag> t : whitelist) {
            if (toCheck.getClass() == t || toCheck.getTag(t) != null) {
                return true;
            }
        }

        return false;
    }
}
