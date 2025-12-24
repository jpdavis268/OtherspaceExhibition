package otherspace.core.session.scenes.settingsmenu;

import com.google.gson.JsonPrimitive;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.Component;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Allows the user to change graphical settings.
 */
public class GraphicsSettings extends Menu {
    Component[] graphicsOptions = new Component[] {
            new Button(256, 64, "menu_settings_g_fullscreen", (_) -> {
                SettingsManager.set("fullscreen", new JsonPrimitive(!SettingsManager.get("fullscreen").getAsBoolean()));
                Window.setFullscreen(SettingsManager.get("fullscreen").getAsBoolean());
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
        d.drawText(gw / 2, 25, SettingsManager.getText("menu_settings_graphics"));

        // Options
        // Just draw the fullscreen toggle for now
        graphicsOptions[0].draw(d, gw / 2, 132);

        // Can't resolve starting states at compile time, so we have to keep button text updated like this.
        String state = SettingsManager.getText("menu_settings_" + (SettingsManager.get("fullscreen").getAsBoolean() ? "on" : "off"));
        ((Button) graphicsOptions[0]).setText(SettingsManager.getText("menu_settings_g_fullscreen") + state);

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
