package otherspace.core.engine.world.tiles;

import org.joml.Vector2i;
import otherspace.core.engine.Sound;
import otherspace.core.engine.Sprite;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.registry.SpriteRegistry;
import otherspace.core.registry.TileRegistry;

/**
 * Properties for a tile on the floor layer.
 */
public class FloorTile {
    /**
     * Get the floor tile with a specific ID.
     *
     * @param id ID of tile to get properties of.
     * @return Tile properties, or null if no such tile exists.
     */
    public static FloorTile get(int id) {
        return TileRegistry.getFT(id);
    }

    /**
     * Get the ID of a floor tile using its handle.
     *
     * @param handle Handle of floor tile.
     * @return ID of corresponding tile.
     */
    public static int getID(String handle) {
        return TileRegistry.getFloorID(handle);
    }

    /**
     * Get the total number of floor tiles.
     *
     * @return Number of registered floor tiles.
     */
    public static int getCount() {
        return TileRegistry.getFTCount();
    }

    /**
     * Get the tileset for floor tiles.
     *
     * @return Floor tileset.
     */
    public static Tile[] getTileset() {
        return TileRegistry.getFloorTileset();
    }

    private String mod;
    public final String NAME;
    public final float HARDNESS;
    public final float SPEED_MODIFIER;
    public final Tile TILE;
    public final ItemDrop[] RETURNS;
    public final Sound FOOTSTEP_SOUND;

    public FloorTile(String name, float hardness, float speedModifier, ItemDrop[] returns, Sound footstepSound, AutoTileStrategy strategy) {
        NAME = name;
        HARDNESS = hardness;
        SPEED_MODIFIER = speedModifier;
        RETURNS = returns;
        TILE = new Tile(new SpriteRegistry().register(new Sprite("resources/sprites/tiles/floor/" + name + ".png", 1, new Vector2i())), strategy);
        FOOTSTEP_SOUND = footstepSound;
    }

    public FloorTile(String name, float hardness, ItemDrop[] returns, Sound footstepSound, AutoTileStrategy strategy) {
        this(name, hardness, 1, returns, footstepSound, strategy);
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
}
