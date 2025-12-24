package otherspace.core.session.scenes.world.layers;

import org.joml.Vector2d;
import otherspace.core.engine.Camera;
import otherspace.core.engine.Layer;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.SceneManager;
import otherspace.game.Assets;

/**
 * Layer for rendering the debug tile grid.
 */
public class DebugLayer extends Layer {
    private static DebugLayer singleton;

    public DebugLayer() {
        singleton = this;
    }

    @Override
    public void draw(Drawer d) {
        super.draw(d);
        if (!isVisible()) return;

        // Get camera properties
        Camera camera = SceneManager.getCurrentScene().getCamera();
        Vector2d position = camera.getPosition();

        int cX = (int) Math.floor(position.x / 16);
        int cY = (int) Math.floor(position.y / 16);
        float cW = camera.getViewWidth() / 32f;
        float cH = camera.getViewHeight() / 32f;
        int gW = (int) Math.ceil(cW / 16) + 2;
        int gH = (int) Math.ceil(cH / 16) + 2;
        int wC = gW / 2;
        int hC = gH / 2;

        // Draw grid over visible chunks.
        for (int i = 0; i < gW; i++) {
            for (int j = 0; j < gH; j++) {
                int xOffset = i - wC;
                int yOffset = j - hC;
                d.drawSprite(Assets.tileGrid, (cX - xOffset) * 16 * 32, (cY - yOffset) * 16 * 32, 0);
            }
        }
    }

    /**
     * Toggle visibility of tile grid.
     */
    public static void toggleTileGrid() {
        singleton.setVisible(!singleton.isVisible());
    }
}
