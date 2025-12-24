package otherspace.core.engine.world.tiles;

import org.joml.Vector2i;
import otherspace.core.engine.Sprite;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.registry.SpriteRegistry;
import otherspace.core.registry.TileRegistry;

import java.util.Set;

/**
 * Properties for a solid tile on the wall layer.
 */
public class WallTile {
    /**
     * Get the wall tile with a specific ID.
     *
     * @param id ID of tile to get properties of.
     * @return Tile properties, or null if no such tile exists.
     */
    public static WallTile get(int id) {
        return TileRegistry.getWT(id);
    }

    /**
     * Get the ID of a wall tile using its handle.
     *
     * @param handle Handle of ground tile.
     * @return ID of corresponding tile.
     */
    public static int getID(String handle) {
        return TileRegistry.getWallID(handle);
    }

    /**
     * Get the total number of wall tiles.
     *
     * @return Number of registered wall tiles.
     */
    public static int getCount() {
        return TileRegistry.getWTCount();
    }

    /**
     * Get the tileset for wall tiles.
     *
     * @return Wall tileset.
     */
    public static Tile[] getTileset() {
        return TileRegistry.getWallTileset();
    }

    private String mod;
    public final String NAME;
    public final float HARDNESS;
    public final float WRONG_TOOL_PENALTY;
    public final ItemDrop[] RETURNS;
    public final Tile TILE;
    private final Set<Class<? extends ToolItem>> effectiveTools;

    public WallTile(String name, float hardness, ItemDrop[] returns, Set<Class<? extends ToolItem>> effectiveTools, float wrongToolPenalty, AutoTileStrategy strategy) {
        NAME = name;
        HARDNESS = hardness;
        RETURNS = returns;
        WRONG_TOOL_PENALTY = wrongToolPenalty;
        TILE = new Tile(new SpriteRegistry().register(new Sprite("resources/sprites/tiles/wall/" + name + ".png", 1, new Vector2i())), strategy);
        this.effectiveTools = effectiveTools;
    }

    public WallTile(String name, float hardness, ItemDrop[] returns, AutoTileStrategy strategy) {
        this(name, hardness, returns, null, 1f, strategy);
    }

    /**
     * Get the mod this tile is from.
     *
     * @return Tile mod.
     */
    public String getMod() {
        return mod;
    }

    /**
     * Set the mod this tile is from if it has not yet been defined.
     *
     * @param mod Tile mod.
     */
    public void setMod(String mod) {
        if (this.mod == null) {
            this.mod = mod;
        }
    }

    /**
     * Get whether a given tool can break this tile.
     *
     * @param item Tool item in question.
     * @return Whether this tool is valid.
     */
    public boolean isToolEffective(ItemStack item) {
        if (effectiveTools != null && item.getItem() instanceof ToolItem ti && !item.isEmpty()) {
            return effectiveTools.contains(ti.getClass());
        }
        return false;
    }
}
