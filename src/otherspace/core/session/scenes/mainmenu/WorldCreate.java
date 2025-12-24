package otherspace.core.session.scenes.mainmenu;

import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.InputField;
import otherspace.core.engine.utils.SaveUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import java.io.File;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Menu for creating and entering a new world.
 */
public class WorldCreate extends Menu {
    int defaultGM = 0;
    int mapType = 0;

    InputField worldNameField = new InputField(524);
    InputField worldSeedField = new InputField(524);

    Button worldCreateGamemodeToggle = new Button(524, 64, "menu_worldcreate_gmtoggle", (_) -> defaultGM = defaultGM == 0 ? 1 : 0);
    Button worldCreateMapTypeToggle = new Button(524, 64, "menu_worldcreate_mttoggle", (_) -> mapType = mapType == 0 ? 1 : 0);

    Button worldCreateButton = new Button(256, 64, "menu_worldcreate_create", (_) -> {
        // Create a new save file
        File savePath = SaveUtils.createSave(worldNameField.getStoredText(), worldSeedField.getStoredText(), defaultGM, mapType);
        SceneManager.changeScene(new GameScene(savePath));
    });

    Button backButton = new Button(256, 64, "menu_back", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new WorldSelect())
    );

    @Override
    public void draw(Drawer d) {
        int gw = otherspace.core.session.window.Window.getWidth();
        int gh = Window.getHeight();

        // Draw input fields and text
        d.setColor(Color.WHITE);
        d.setHalign(Drawer.H_LEFT);
        d.setValign(Drawer.V_TOP);
        d.drawText(gw / 2 - 262, gh / 2 - 190, SettingsManager.getText("menu_worldcreate_name"));
        worldNameField.draw(d, gw / 2, gh / 2 - 170);
        d.drawText(gw / 2 - 262, gh / 2 - 108, SettingsManager.getText("menu_worldcreate_seed"));
        worldSeedField.draw(d, gw / 2, gh / 2 - 88);

        // Draw buttons
        worldCreateGamemodeToggle.draw(d, gw / 2, gh / 2);
        worldCreateMapTypeToggle.draw(d, gw / 2, gh / 2 + 72);
        backButton.draw(d, gw / 2 - 134, gh / 2 + 144);
        worldCreateButton.draw(d, gw / 2 + 134, gh / 2 + 144);

        // Update text
        String gmToggleText = SettingsManager.getText("menu_worldcreate_gmtoggle");
        String curGM = SettingsManager.getText("menu_worldcreate_" + (defaultGM == 1 ? "sandbox" : "survival"));
        worldCreateGamemodeToggle.setText(gmToggleText + curGM);

        String mtToggleText = SettingsManager.getText("menu_worldcreate_mttoggle");
        String curMapType = SettingsManager.getText("menu_worldcreate_" + (mapType == 1 ? "lab" : "normal"));
        worldCreateMapTypeToggle.setText(mtToggleText + curMapType);

        // Update hover text
        worldCreateGamemodeToggle.setHoverText("menu_worldcreate_" + (defaultGM == 1 ? "sandbox" : "survival") + "_hover");
        worldCreateMapTypeToggle.setHoverText("menu_worldcreate_" + (mapType == 1 ? "lab" : "normal") + "_hover");

        // Return to world selection if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            backButton.invokeCallback();
        }
    }
}
