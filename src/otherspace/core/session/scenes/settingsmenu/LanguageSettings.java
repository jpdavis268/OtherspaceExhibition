package otherspace.core.session.scenes.settingsmenu;

import com.google.gson.JsonPrimitive;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Allows user to change the game's localization.
 */
public class LanguageSettings extends Menu {
    Button[] langButtons = new Button[] {
            new Button(256, 64, "English (US)", (_) -> {
                SettingsManager.set("language", new JsonPrimitive("en_us"));

                // Reload text map
                SettingsManager.reloadTextMap();
            })
    };

    Button backButton = new Button(256, 64, "menu_settings_back", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new SettingsMenu())
    );

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();

        // Draw header
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(0, 0, gw, 50));
        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        d.setColor(Color.WHITE);
        d.drawText(gw / 2, 25, SettingsManager.getText("menu_settings_lang"));

        // Draw buttons
        int xOffset = 264;
        int yOffset = 72;
        int perRow = gw / xOffset;
        int x = (gw / 2) - ((perRow / 2) * xOffset - xOffset / 2);
        int y = xOffset / 2;
        int leftBound = x;
        int rightBound = (gw / 2) + ((perRow / 2) * xOffset - xOffset / 2);

        for (Button b : langButtons) {
            b.draw(d, x, y);
            if (x < rightBound) {
                x += 264;
            }
            else {
                x = leftBound;
                y += yOffset;
            }
        }

        // Draw footer
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(0, gh - 80, gw, gh));
        backButton.draw(d, gw / 2, gh - 40);

        // Return to main settings menu if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            backButton.invokeCallback();
        }
    }
}
