package otherspace.core.session.scenes.world.layers;

import otherspace.core.engine.Layer;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.scenes.world.World;

/**
 * Layer for rendering solid wall tiles.
 */
public class SolidTileLayer extends Layer {
    /**
     * Draw ground tiles.
     *
     * @param d Drawer to use.
     */
    @Override
    public void draw(Drawer d) {
        for (Chunk c : World.getChunkMap().values()) {
            if (c.getState() == Chunk.CHUNK_STATE.NEARBY) {
                c.getTileData(Chunk.STM).draw(true, 0.5f);
            }
        }
    }
}
