package otherspace.core.session.scenes.world.layers;

import otherspace.core.engine.Layer;
import otherspace.core.session.Drawer;
import otherspace.game.entities.Player;

/**
 * Handle drawing of in-world UI elements.
 */
public class UILayer extends Layer {
    @Override
    public void draw(Drawer d) {
        super.draw(d);

        if (Player.getOwnPlayer() != null) {
            Player.getSelector().draw(d);
        }
    }
}
