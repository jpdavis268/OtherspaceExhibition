package otherspace.core.session.scenes.settingsmenu;

import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.Scene;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.mainmenu.MainMenu;
import otherspace.core.session.scenes.mainmenu.TitleScreen;
import otherspace.core.session.scenes.world.ui.PauseMenu;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Main menu for settings menu subtree.
 */
public class SettingsMenu extends Menu {
    Button generalSettings = new Button(256, 64, "menu_settings_general", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new GeneralSettings())
    );

    Button graphicsSettings = new Button(256, 64, "menu_settings_graphics", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new GraphicsSettings())
    );

    Button audioSettings = new Button(256, 64, "menu_settings_audio", (_) ->
        SceneManager.getCurrentScene().setCurrentMenu(new AudioSettings())
    );

    Button controlSettings = new Button(256, 64, "menu_settings_controls", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new ControlSettings())
    );

    Button langSettings = new Button(256, 64, "menu_settings_lang", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new LanguageSettings())
    );

    Button exitButton = new Button(256, 64, "menu_settings_exit", (_) -> {
        Scene activeScene = SceneManager.getCurrentScene();
        if (activeScene instanceof MainMenu) {
            activeScene.setCurrentMenu(new TitleScreen());
        }
        else {
            activeScene.setCurrentMenu(new PauseMenu());
        }
    });

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();
        int offset = 72;
        int y = (gh / 2) - offset * 2;

        // Draw header
        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        d.setColor(Color.WHITE);
        d.drawText(gw / 2, y - 72, SettingsManager.getText("menu_settings"));

        // Draw buttons
        generalSettings.draw(d, gw / 2, y); y += offset;
        graphicsSettings.draw(d, gw / 2, y); y += offset;
        audioSettings.draw(d, gw / 2, y); y += offset;
        controlSettings.draw(d, gw / 2, y); y += offset;
        langSettings.draw(d, gw / 2, y); y += offset;
        exitButton.draw(d, gw / 2, y);

        // Return to parent menu if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            exitButton.invokeCallback();
        }
    }
}
