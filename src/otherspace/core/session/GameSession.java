package otherspace.core.session;

import otherspace.core.engine.*;
import otherspace.core.registry.*;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.mainmenu.MainMenu;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.window.RenderHandler;
import otherspace.core.session.window.Window;
import otherspace.game.BaseMod;

import java.util.LinkedList;

/**
 * Session manager for the Otherspace client.
 */
public class GameSession extends Session {
    private Window window;
    private SceneManager sceneManager;
    private SettingsManager settingsManager;
    private SoundManager soundManager;

    private static double lastGameUpdateTime = 0;
    private static double lastDrawTime = 0;

    /**
     * Initialize game and game window.
     */
    @Override
    public void init() {
        super.init();
        long start = System.currentTimeMillis();
        System.out.println("Session is client, initializing game window.");

        // Initialize components.
        window = new Window(1280, 720, "Otherspace " + Session.GAME_VERSION);
        sceneManager = new SceneManager();
        settingsManager = new SettingsManager();
        soundManager = new SoundManager();

        // Initialize game content.
        // TODO: Set up mod detection and have this handled automatically through that.
        BaseMod.init();
        ItemRegistry.registerItems();
        TileRegistry.registerTiles();
        RecipeRegistry.registerRecipes();
        EntityRegistry.registerEntities();

        // Complete Registry
        // Sprites and Fonts
        LinkedList<Bitmap> toBatch = new LinkedList<>();
        LinkedList<Sprite> sprites = SpriteRegistry.registerSprites();
        LinkedList<Font> fonts = FontRegistry.registerFonts();
        toBatch.addAll(sprites);
        toBatch.addAll(fonts);

        if (sprites != null && fonts != null) {
            TextureAtlas.batch(toBatch, sprites.size(), fonts.size());
        }
        else {
            throw new IllegalStateException("ERROR: Could not generate texture atlas due to null resource registry.");
        }

        // Sounds
        LinkedList<Sound> sounds = SoundRegistry.registerSounds();
        soundManager.init(sounds);

        // Finish initialization.
        long initTime = System.currentTimeMillis() - start;
        System.out.println("Finished initialization in " + initTime + "ms.");

        // Go to main menu.
        SceneManager.changeScene(new MainMenu());
    }

    @Override
    protected void doGameUpdate() {
        // Read input and check for changes.
        window.pollEvents();

        // Perform game update and redraw.
        long updateStart = System.nanoTime();
        SceneManager.getCurrentScene().update();
        long updateTime = System.nanoTime() - updateStart;
        lastGameUpdateTime = (double) updateTime / 1000000;

        long drawStart = System.nanoTime();
        renderFrame();
        long drawTime = System.nanoTime() - drawStart;
        lastDrawTime = (double) drawTime / 1000000;
    }

    /**
     * Render the next frame.
     */
    private void renderFrame() {
        SceneManager.getCurrentScene().getCamera().adjustProjection(Window.getWidth(), Window.getHeight());
        RenderHandler renderHandler = window.getRenderHandler();
        Drawer d = new Drawer(renderHandler);

        // Draw scene.
        renderHandler.preDraw();
        sceneManager.drawCurrentScene(d);

        // Draw GUI.
        renderHandler.guiPreDraw();
        sceneManager.drawSceneGUI(d);

        // Finish drawing.
        renderHandler.pushFrame();
    }

    @Override
    public void dispose() {
        super.dispose();

        // If we are still in game, attempt to save.
        if (SceneManager.getCurrentScene() instanceof GameScene) {
            GameScene.exitGame();
        }

        // Close game window.
        window.dispose();

        // Finish disposal.
        System.out.println("Program termination complete.");
        System.exit(0);
    }

    @Override
    protected boolean hasTerminated() {
        return window.isWindowClosed();
    }

    public static double getLastGameUpdateTime() {
        return lastGameUpdateTime;
    }

    public static double getLastDrawTime() {
        return lastDrawTime;
    }
}
