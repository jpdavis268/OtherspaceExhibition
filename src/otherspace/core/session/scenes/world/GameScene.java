package otherspace.core.session.scenes.world;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Scene;
import otherspace.core.engine.utils.SaveUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.mainmenu.MainMenu;
import otherspace.core.session.scenes.world.ui.ChatBox;
import otherspace.core.session.scenes.world.ui.HUD;
import otherspace.core.session.scenes.world.ui.Hotbar;
import otherspace.core.session.scenes.world.ui.PauseMenu;
import otherspace.core.session.window.Window;

import java.io.File;

/**
 * Scene where the player actually plays the game.
 */
public class GameScene extends Scene {
    private static GameScene singleton;

    private boolean debugEnabled;
    private boolean isPaused = false;

    private String playerUsername;
    private Color playerColor;

    private World world;
    private final InputHandler inputHandler;
    private final ChatBox chatBox;
    private final Hotbar hotbar;

    public GameScene(File savePath) {
        super();
        singleton = this;
        setCamera(new GameCamera(this));
        setCurrentMenu(new HUD());

        playerUsername = SettingsManager.get("username").getAsString();
        playerColor = new Color(SettingsManager.get("player_color").getAsInt());

        inputHandler = new InputHandler();
        world = new World(savePath);
        chatBox = new ChatBox();
        hotbar = new Hotbar();
    }

    @Override
    public void update() {
        super.update();

        if (!isPaused()) {
            inputHandler.update();
            world.update();
        }
    }

    @Override
    public void draw(Drawer d) {
        super.draw(d);

        world.draw(d);
    }

    @Override
    public void drawGUI(Drawer d) {
        chatBox.draw(d);

        if (isPaused) {
            int gw = Window.getWidth();
            int gh = Window.getHeight();

            // Darken screen
            d.setColor(new Color(0, 0, 0, 0.5f));
            d.drawRect(new Rectanglei(0, 0, gw, gh));
        }
        super.drawGUI(d);
    }

    /**
     * Get the player's username.
     *
     * @return Player username.
     */
    public static String getPlayerUsername() {
        return singleton.playerUsername;
    }

    /**
     * Get the player's color.
     *
     * @return Player color.
     */
    public static Color getPlayerColor() {
        return singleton.playerColor;
    }

    /**
     * Get whether the player has enabled debug mode.
     *
     * @return Debug mode state.
     */
    public static boolean isDebugEnabled() {
        return singleton.debugEnabled;
    }

    /**
     * Get whether the player has enabled debug mode.
     *
     * @param enable Whether to enable debug mode.
     */
    public static void setDebugEnabled(boolean enable) {
        singleton.debugEnabled = enable;
    }

    /**
     * Pause or unpause the game.
     *
     * @param pause Whether the game should be paused.
     */
    public static void setPaused(boolean pause) {
        if (pause) {
            singleton.isPaused = true;
            singleton.setCurrentMenu(new PauseMenu());
        }
        else {
            singleton.isPaused = false;
            singleton.setCurrentMenu(new HUD());
        }
    }

    /**
     * Get whether the game is currently paused.
     *
     * @return Whether game is paused.
     */
    public static boolean isPaused() {
        return singleton.isPaused;
    }

    /**
     * Exit the game and return to the main title screen.
     */
    public static void exitGame() {
        SaveUtils.saveGame(World.getSavePath());
        SceneManager.changeScene(new MainMenu());
        SoundManager.stopAllAudio();
    }
}
