package otherspace;

import otherspace.core.session.GameSession;

/**
 * Main entry point for Otherspace.
 */
public class MainLauncher {
    public static void main(String[] args) {
        // Start game session (always a GUI one for now)
        new GameSession();
    }
}
