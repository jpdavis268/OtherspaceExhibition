package otherspace.core.session.scenes.settingsmenu;

import com.google.gson.JsonPrimitive;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.Component;
import otherspace.core.engine.guicomponents.Slider;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Allows user to modify general game options, such as date and time format and autosave interval.
 */
public class GeneralSettings extends Menu {
    Component[] generalOptions = new Component[] {
            // Date Format
            new Button(256, 64, "menu_settings_gen_dateformat", (_) ->
                SettingsManager.set("date_format", new JsonPrimitive(!SettingsManager.get("date_format").getAsBoolean()))
            ),

            // Time Format
            new Button(256, 64, "menu_settings_gen_timeformat", (_) ->
                SettingsManager.set("time_format", new JsonPrimitive(!SettingsManager.get("time_format").getAsBoolean()))
            ),

            // Autosave Interval
            new Slider(256, "menu_settings_gen_autosave_interval", 0, (e) -> {
                Slider s = (Slider) e;
                int intervalPreset = Math.round(s.getPosition() * 5);
                SettingsManager.set("autosave_interval", new JsonPrimitive(intervalPreset));
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
        d.drawText(gw / 2, 25, SettingsManager.getText("menu_settings_general"));

        // Draw options
        int y = 132;
        for (Component generalOption : generalOptions) {
            generalOption.draw(d, gw / 2, y);
            y += 72;
        }


        // Update text
        ((Button) generalOptions[0]).setText(
                SettingsManager.getText("menu_settings_gen_dateformat")
                        + SettingsManager.getText(SettingsManager.get("date_format").getAsBoolean() ? "mm/dd/yyyy" : "dd/mm/yyyy")
        );
        ((Button) generalOptions[1]).setText(
                SettingsManager.getText("menu_settings_gen_timeformat")
                        + SettingsManager.getText(SettingsManager.get("time_format").getAsBoolean() ? "menu_settings_gen_24hr" : "menu_settings_gen_ampm")
        );
        ((Slider) generalOptions[2]).setLabel(SettingsManager.getText("menu_settings_gen_autosave_interval") +
                switch (SettingsManager.get("autosave_interval").getAsInt()) {
                    case 0 -> "5 " + SettingsManager.getText("menu_settings_gen_minutes");
                    case 1 -> "10 " + SettingsManager.getText("menu_settings_gen_minutes");
                    case 2 -> "15 " + SettingsManager.getText("menu_settings_gen_minutes");
                    case 3 -> "20 " + SettingsManager.getText("menu_settings_gen_minutes");
                    case 4 -> "30 " + SettingsManager.getText("menu_settings_gen_minutes");
                    case 5 -> "1 " + SettingsManager.getText("menu_settings_gen_hour");
                    default -> "null";
                }
        );

        // Update autosave slider.
        ((Slider) generalOptions[2]).setPosition(SettingsManager.get("autosave_interval").getAsFloat() / 5);

        // Draw footer
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(9, gh - 80, gw, gh));
        backButton.draw(d, gw / 2, gh - 40);

        // Return to main settings menu if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            backButton.invokeCallback();
        }
    }
}
