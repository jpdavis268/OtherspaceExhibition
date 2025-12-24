package otherspace.core.session.scenes.mainmenu;

import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.registry.EntityRegistry;
import otherspace.core.session.Drawer;
import otherspace.core.session.GameSession;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.settingsmenu.SettingsMenu;
import otherspace.core.session.window.Window;
import otherspace.game.Assets;
import otherspace.game.entities.Player;

/**
 * Main title screen.
 */
public class TitleScreen extends Menu {
    Button spButton = new Button(256, 64, "menu_sp", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new WorldSelect())
    );
    Button mpButton = new Button(256, 64, "menu_mp", (_) -> {});
    Button modsButton = new Button(256, 64, "menu_mods", (_) -> {});
    Button settingsButton = new Button(256, 64, "menu_settings", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new SettingsMenu()));
    Button quitButton = new Button(256, 64, "menu_exit_game", (_) -> Window.closeWindow());
    Button profileButton = new Button(128, 32, "menu_edit_profile", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new ProfileEditor())
    );

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();
        int offset = 72;

        int y = gh / 2 - offset;

        // Logo
        d.drawSprite(Assets.otherspaceLogo, gw / 2, y - offset * 2, 0);

        // Buttons
        spButton.draw(d, gw / 2, y); y += offset;
        mpButton.draw(d, gw / 2, y); y += offset;
        modsButton.draw(d, gw / 2, y); y += offset;
        settingsButton.draw(d, gw / 2, y); y += offset;
        quitButton.draw(d, gw / 2, y);

        // Profile View
        int pOffset = gw / 2 - 400;
        d.drawText(pOffset, gh / 2 - 60, SettingsManager.get("username").getAsString());
        Color playerCol = new Color(SettingsManager.get("player_color").getAsInt());
        d.drawSpriteExt(EntityRegistry.getSprite(Player.class), pOffset, gh / 2 + 40, 0, playerCol, 2, 2, 0);
        // TODO: Draw player sprite
        profileButton.draw(d, pOffset, gh / 2 + 80);

        // Version
        d.setHalign(Drawer.H_RIGHT);
        d.setValign(Drawer.V_BOTTOM);
        d.drawText(gw, gh, GameSession.GAME_VERSION);
    }
}
