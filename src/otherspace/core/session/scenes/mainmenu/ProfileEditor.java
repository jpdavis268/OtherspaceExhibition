package otherspace.core.session.scenes.mainmenu;

import com.google.gson.JsonPrimitive;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.InputField;
import otherspace.core.engine.guicomponents.Slider;
import otherspace.core.registry.EntityRegistry;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;
import otherspace.game.entities.Player;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Menu where the player can edit their appearance in game.
 */
public class ProfileEditor extends Menu {
    String playerUsername = SettingsManager.get("username").getAsString();
    Color playerCol = new Color(SettingsManager.get("player_color").getAsInt());

    InputField usernameField = new InputField(200, 24, playerUsername, Color.BLACK, Color.LIGHT_GRAY);

    Slider redSlider = new Slider(200, String.format("R: %d", (int) (playerCol.r() * 255f)), playerCol.r(), (e) -> {
       Slider inst = (Slider) e;
       inst.setLabel(String.format("R: %d", (int) (inst.getPosition() * 255)));
    });
    Slider greenSlider = new Slider(200, String.format("G: %d", (int) (playerCol.g() * 255f)), playerCol.g(), (e) -> {
       Slider inst = (Slider) e;
       inst.setLabel(String.format("G: %d", (int) (inst.getPosition() * 255)));
    });
    Slider blueSlider = new Slider(200, String.format("B: %d", (int) (playerCol.b() * 255f)), playerCol.b(), (e) -> {
       Slider inst = (Slider) e;
       inst.setLabel(String.format("B: %d", (int) (inst.getPosition() * 255)));
    });
    Button backButton = new Button(256, 64, "menu_back", (_) -> {
        // Save configuration
        SettingsManager.set("username", new JsonPrimitive(usernameField.getStoredText()));
        SettingsManager.set("player_color", new JsonPrimitive(playerCol.toRGBA()));
        SceneManager.getCurrentScene().setCurrentMenu(new TitleScreen());
    });

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();
        int offset = 72;

        // Draw current player appearance
        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        d.drawText(gw / 2, gh / 2 - offset / 2 * 5 - 100, usernameField.getStoredText());
        playerCol = new Color(redSlider.getPosition(), greenSlider.getPosition(), blueSlider.getPosition());
        d.drawSpriteExt(EntityRegistry.getSprite(Player.class), gw / 2, gh / 2 - offset * 5 / 2, 0, playerCol, 2, 2, 0);

        // Draw options
        int y = gh / 2 - offset * 2;
        usernameField.draw(d, gw / 2, y); y += offset;
        redSlider.draw(d, gw / 2, y); y += offset;
        greenSlider.draw(d, gw / 2, y); y += offset;
        blueSlider.draw(d, gw / 2, y); y += offset * 2;
        backButton.draw(d, gw / 2, y);

        // Return to title menu if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            backButton.invokeCallback();
        }
    }
}
