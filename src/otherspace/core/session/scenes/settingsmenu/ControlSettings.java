package otherspace.core.session.scenes.settingsmenu;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.BindButton;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Allows user to remap keyboard actions.
 */
public class ControlSettings extends Menu {
    BindButton[] bindButtons = new BindButton[] {
            new BindButton(256, 64, "menu_settings_c_chat", "chat_bind", GLFW_KEY_C),
            new BindButton(256, 64, "menu_settings_c_fullscreen", "fullscreen_bind", GLFW_KEY_F12),
            new BindButton(256, 64, "menu_settings_c_zoom_out", "camera_zoom_out_bind", GLFW_KEY_X),
            new BindButton(256, 64, "menu_settings_c_zoom_in", "camera_zoom_in_bind", GLFW_KEY_Z),
            new BindButton(256, 64, "menu_settings_c_zoom_reset", "camera_zoom_reset_bind", GLFW_KEY_R),
            new BindButton(256, 64, "menu_settings_c_up", "move_up_bind", GLFW_KEY_W),
            new BindButton(256, 64, "menu_settings_c_left", "move_left_bind", GLFW_KEY_A),
            new BindButton(256, 64, "menu_settings_c_down", "move_down_bind", GLFW_KEY_S),
            new BindButton(256, 64, "menu_settings_c_right", "move_right_bind", GLFW_KEY_D),
            new BindButton(256, 64, "menu_settings_c_buildmode", "build_mode_bind", GLFW_KEY_T),
            new BindButton(256, 64, "menu_settings_c_inventory", "inventory_bind", GLFW_KEY_E),
            new BindButton(256, 64, "menu_settings_c_drop", "drop_bind", GLFW_KEY_Q),
            new BindButton(256, 64, "menu_settings_c_sweep", "sweep_bind", GLFW_KEY_F)
    };

    Button bindResetButton = new Button(256, 64, "menu_settings_c_reset", (_) -> {
        for (BindButton b : bindButtons) {
            b.resetBind();
        }
    });

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
        d.drawText(gw / 2, 25, SettingsManager.getText("menu_settings_controls"));

        // Keybinds
        int perRow = gw / 264;
        int x = (gw / 2) - ((perRow / 2) * 264 - 132);
        int y = 132;
        int leftBound = x;
        int rightBound = (gw / 2) + ((perRow / 2) * 264 - 132);

        for (BindButton b : bindButtons) {
            b.draw(d, x, y);
            if (x < rightBound) {
                x += 264;
            }
            else {
                x = leftBound;
                y += 72;
            }
        }

        // Draw footer
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(0, gh - 80, gw, gh));
        if (!BindButton.isBindSelected()) {
            backButton.draw(d, gw / 2 - 256, gh - 40);
            bindResetButton.draw(d, gw / 2 + 256, gh - 40);
        }

        // Return to main settings menu if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE) && !BindButton.isBindSelected()) {
            backButton.invokeCallback();
        }
    }
}
