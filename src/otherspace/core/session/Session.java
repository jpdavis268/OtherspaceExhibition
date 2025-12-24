package otherspace.core.session;

/**
 * Abstract session class, handles main program loop.
 * Will only be extended by GameSession for now.
 */
public abstract class Session {
    public static final String GAME_VERSION = "0.1a-dev";

    private static float FPS = 60;
    private static double lastUpdateTime = 0;

    public Session() {
        init();
        gameLoop();
        dispose();
    }

    /**
     * Initialize this session.
     */
    public void init() {
        System.out.println("Beginning initialization.");
    }

    /**
     * Enter game loop.
     */
    public final void gameLoop() {
        System.out.println("Entering main loop.");

        // Setup
        float targetFPS = 60;

        float updateTime = 1000000000f / targetFPS;
        float delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (!hasTerminated()) {
            // Update delta and timer.
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / updateTime;
            lastTime = currentTime;

            // Perform update if enough time has passed.
            if (delta >= 0) {
                long start = System.nanoTime();
                doGameUpdate();
                long time = System.nanoTime() - start;
                lastUpdateTime = (double) time / 1000000;

                FPS = targetFPS / Math.max(1, delta + 1);
                delta = -1;
            }
        }
    }

    /**
     * Closing operations.
     */
    public void dispose() {
        System.out.println("Closing game.");
    }

    /**
     * What should be done during a game update.
     */
    protected abstract void doGameUpdate();

    /**
     * Get whether the game has been ordered to close.
     *
     * @return Whether the game is to be terminated.
     */
    protected abstract boolean hasTerminated();

    /**
     * Get the current running FPS.
     *
     * @return FPS, as calculated last frame.
     */
    public static float getFPS() {
        return FPS;
    }

    /**
     * Get how long it took for the last frame to process.
     *
     * @return How long the last frame took to process, in milliseconds.
     */
    public static double getLastUpdateTime() {
        return lastUpdateTime;
    }
}
