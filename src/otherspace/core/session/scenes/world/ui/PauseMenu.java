package otherspace.core.session.scenes.world.ui;

import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.settingsmenu.SettingsMenu;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Main pause screen.
 */
public class PauseMenu extends Menu {
    Button resumeButton = new Button(256, 64, "pause_resume", (_) -> GameScene.setPaused(false));
    Button settingsButton = new Button(256, 64, "menu_settings", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new SettingsMenu())
    );
    Button exitButton = new Button(256, 64, "pause_sp_exit", (_) -> GameScene.exitGame());

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();

        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        d.setColor(Color.WHITE);
        int y = (gh / 2) - 72;

        d.drawText(gw / 2, y - 72, SettingsManager.getText("pause_header"));
        resumeButton.draw(d, gw / 2, y); y += 72;
        settingsButton.draw(d, gw / 2, y); y += 72;
        exitButton.draw(d, gw / 2, y);

        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            resumeButton.invokeCallback();
        }
    }
}
