package otherspace.core.session.scenes.world.layers;

import otherspace.core.engine.Layer;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.scenes.world.World;

/**
 * Layer for rendering floor tiles, which sit above ground tiles.
 */
public class FloorTileLayer extends Layer {
    /**
     * Draw floor tiles.
     *
     * @param d Drawer to use.
     */
    @Override
    public void draw(Drawer d) {
        for (Chunk c : World.getChunkMap().values()) {
            if (c.getState() == Chunk.CHUNK_STATE.NEARBY) {
                c.getTileData(Chunk.FTM).draw(false, 0);
            }
        }
    }
}
