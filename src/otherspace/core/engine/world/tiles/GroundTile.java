package otherspace.core.engine.world.tiles;

import org.joml.Vector2i;
import otherspace.core.engine.Sound;
import otherspace.core.engine.Sprite;
import otherspace.core.registry.SpriteRegistry;
import otherspace.core.registry.TileRegistry;

/**
 * Properties for a tile on the ground layer.
 */
public class GroundTile {
    /**
     * Get the ground tile with a specific ID.
     *
     * @param id ID of tile to get properties of.
     * @return Tile properties, or null if no such tile exists.
     */
    public static GroundTile get(int id) {
        return TileRegistry.getGT(id);
    }

    /**
     * Get the ID of a ground tile using its handle.
     *
     * @param handle Handle of ground tile.
     * @return ID of corresponding tile.
     */
    public static int getID(String handle) {
        return TileRegistry.getGroundID(handle);
    }

    /**
     * Get the total number of ground tiles.
     *
     * @return Number of registered ground tiles.
     */
    public static int getCount() {
        return TileRegistry.getGTCount();
    }

    /**
     * Get the tileset for ground tiles.
     *
     * @return Ground tileset.
     */
    public static Tile[] getTileset() {
        return TileRegistry.getGroundTileset();
    }

    private String mod;
    public final String NAME;
    public final float SPEED_MODIFIER;
    public final boolean SOLID_GROUND;
    public final Tile TILE;
    public final Sound FOOTSTEP_SOUND;

    public GroundTile(String name, float speedModifier, boolean solidGround, Sound footstepSound, AutoTileStrategy strategy) {
        NAME = name;
        SPEED_MODIFIER = speedModifier;
        SOLID_GROUND = solidGround;
        TILE = new Tile(new SpriteRegistry().register(new Sprite("resources/sprites/tiles/ground/" + name + ".png", 1, new Vector2i())), strategy);
        FOOTSTEP_SOUND = footstepSound;
    }

    public GroundTile(String name, Sound footstepSound, AutoTileStrategy strategy) {
        this(name, 1, true, footstepSound, strategy);
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
