package otherspace.core.session.scenes.world;

import org.joml.Vector2d;
import org.joml.Vector2i;
import otherspace.core.engine.Camera;
import otherspace.core.engine.Scene;
import otherspace.game.entities.Player;

import static otherspace.core.session.scenes.world.Chunk.CHUNK_STATE;

/**
 * Camera that follows player throughout game and handles location-specific events.
 */
public class GameCamera extends Camera {
    private static final int LOAD_RADIUS = 5;
    private static final int LOAD_DIAMETER = LOAD_RADIUS * 2 + 1;

    public GameCamera(Scene myScene) {
        super(myScene);
    }

    /**
     * Update the camera for this frame.
     */
    public void updateCamera() {
        // Track player.
        if (Player.getOwnPlayer() != null) {
            setPosition(new Vector2d(Player.getOwnPlayer().position).add(0, -0.625));
        }

        // Chunk management
        int cX = (int) getPosition().x / 16;
        int cY = (int) getPosition().y / 16;

        for (int x = 0; x < LOAD_DIAMETER; x++) {
            int xOffset = cX - LOAD_RADIUS + x;

            for (int y = 0; y < LOAD_DIAMETER; y++) {
                int yOffset = cY - LOAD_RADIUS + y;
                Vector2i chunkCoords = new Vector2i(xOffset, yOffset);
                Chunk localChunk = Chunk.getChunk(chunkCoords);

                // If there is not a chunk here, load the chunk.
                if (localChunk == null) {
                    Chunk.loadChunk(chunkCoords);
                }

                if (localChunk != null) {
                    localChunk.setState(CHUNK_STATE.NEARBY);
                }
            }
        }
    }
}
