package otherspace.core.engine.utils;

import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Surface;
import otherspace.core.session.window.MouseListener;

import java.util.Random;

/**
 * Utility class containing general use functions that don't fit elsewhere.
 */
public final class GenUtils {
    private GenUtils() {}

    /**
     * Check if the cursor is in a given area.
     *
     * @param area Area to check.
     * @return Whether mouse is in area.
     */
    public static boolean isMouseOver(Rectanglei area) {
        Surface curSurf = Surface.getCurrentSurface();
        Vector2i mouseCoords = curSurf == null ?
                new Vector2i((int) MouseListener.getMouseX(), (int) MouseListener.getMouseY()) :
                curSurf.getRelativeMouseLocation();
        return area.containsPoint(mouseCoords);
    }

    /**
     * Perform a "dice roll" using a given chance from 0 to 1.
     *
     * @param chance Chance of roll landing, from 0 (impossible) to 1 (always).
     * @return Whether dice roll landed.
     */
    public static boolean diceRoll(float chance) {
        return new Random().nextFloat(1) <= chance;
    }
}
