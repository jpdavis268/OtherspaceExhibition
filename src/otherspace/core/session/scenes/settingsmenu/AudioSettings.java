package otherspace.core.session.scenes.settingsmenu;

import com.google.gson.JsonPrimitive;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.Slider;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;
import otherspace.game.Assets;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Allows the user to tweak the volume of different audio groups.
 */
public class AudioSettings extends Menu {
    Slider[] audioOptions = new Slider[] {
            new Slider(256, "menu_settings_a_master", SettingsManager.get("master_volume").getAsFloat(), (e) -> {
                Slider s = (Slider) e;
                SoundManager.setMasterVolume(s.getPosition());
                SettingsManager.set("master_volume", new JsonPrimitive(s.getPosition()));
            }),
            new Slider(256, "menu_settings_a_ambience", SettingsManager.get("ambience_volume").getAsFloat(), (e) -> {
                Slider s = (Slider) e;
                Assets.ambienceGroup.setGain(s.getPosition());
                SettingsManager.set("ambience_volume", new JsonPrimitive(s.getPosition()));
            }),
            new Slider(256, "menu_settings_a_entities", SettingsManager.get("entity_volume").getAsFloat(), (e) -> {
                Slider s = (Slider) e;
                Assets.entityGroup.setGain(s.getPosition());
                SettingsManager.set("entity_volume", new JsonPrimitive(s.getPosition()));
            }),
            new Slider(256, "menu_settings_a_tile", SettingsManager.get("tile_volume").getAsFloat(), (e) -> {
                Slider s = (Slider) e;
                Assets.tileGroup.setGain(s.getPosition());
                SettingsManager.set("tile_volume", new JsonPrimitive(s.getPosition()));
            }),
            new Slider(256, "menu_settings_a_ui", SettingsManager.get("ui_volume").getAsFloat(), (e) -> {
                Slider s = (Slider) e;
                Assets.uiGroup.setGain(s.getPosition());
                SettingsManager.set("ui_volume", new JsonPrimitive(s.getPosition()));
            }),
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
        d.drawText(gw / 2, 25, SettingsManager.getText("menu_settings_audio"));

        // Draw volume sliders
        int y = 96;
        for (Slider s : audioOptions) {
            s.draw(d, gw / 2, y);
            y += 72;
        }

        // Update sliders
        audioOptions[0].setLabel(SettingsManager.getText("menu_settings_a_master") + Math.round(SettingsManager.get("master_volume").getAsFloat() * 100));
        audioOptions[1].setLabel(SettingsManager.getText("menu_settings_a_ambience") + Math.round(SettingsManager.get("ambience_volume").getAsFloat() * 100));
        audioOptions[2].setLabel(SettingsManager.getText("menu_settings_a_entities") + Math.round(SettingsManager.get("entity_volume").getAsFloat() * 100));
        audioOptions[3].setLabel(SettingsManager.getText("menu_settings_a_tile") + Math.round(SettingsManager.get("tile_volume").getAsFloat() * 100));
        audioOptions[4].setLabel(SettingsManager.getText("menu_settings_a_ui") + Math.round(SettingsManager.get("ui_volume").getAsFloat() * 100));

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
