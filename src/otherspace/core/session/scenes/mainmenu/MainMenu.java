package otherspace.core.session.scenes.mainmenu;

import otherspace.core.engine.Camera;
import otherspace.core.engine.Color;
import otherspace.core.engine.Scene;
import otherspace.core.session.Drawer;

/**
 * Starting menu where players can modify settings and choose which save to load.
 */
public class MainMenu extends Scene {
    public MainMenu() {
        super();
        setBackground(Color.DARK_BROWN);
        setCamera(new Camera(this));
        setCurrentMenu(new TitleScreen());
    }

    @Override
    public void drawGUI(Drawer d) {
        super.drawGUI(d);
    }
}
