package otherspace.core.session.scenes.world.layers;

import otherspace.core.engine.Layer;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.scenes.world.World;

import static otherspace.core.session.scenes.world.Chunk.CHUNK_STATE;

/**
 * Layer for rendering "ground" tiles, such as grass and water.
 */
public class GroundTileLayer extends Layer {
    /**
     * Draw ground tiles.
     *
     * @param d Drawer to use.
     */
    @Override
    public void draw(Drawer d) {
        for (Chunk c : World.getChunkMap().values()) {
            if (c.getState() == CHUNK_STATE.NEARBY) {
                c.getTileData(Chunk.GTM).draw(true, 1);
            }
        }
    }
}
