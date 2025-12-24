package otherspace.core.engine;

import static org.joml.Math.lerp;

/**
 * OpenGL friendly means of storing a color value, using floating points instead of integers.
 */
public record Color(float r, float g, float b, float a) {
    // Color Constants
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color DARK_GRAY = new Color(0.25f, 0.25f, 0.25f);
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f);
    public static final Color LIGHT_GRAY = new Color(0.75f, 0.75f, 0.75f);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color DARK_BROWN = new Color(0.15f, 0.075f, 0);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color DARK_RED = new Color(0.5f, 0, 0);
    public static final Color DARK_GREEN = new Color(0, 0.5f, 0);
    public static final Color YELLOW = new Color(1, 1, 0);
    public static final Color ORANGE = new Color(1, 0.65f, 0);

    public Color {
        r = Math.clamp(r, 0, 1);
        g = Math.clamp(g, 0, 1);
        b = Math.clamp(b, 0, 1);
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1);
    }

    public Color(int rgba) {
        int iR = ((rgba & 0xFF000000) >> 24) & 0xFF;
        int iG = (rgba & 0x00FF0000) >> 16;
        int iB = (rgba & 0x0000FF00) >> 8;
        int iA = (rgba & 0x000000FF);
        this(iR / 255f, iG / 255f, iB / 255f, iA / 255f);
    }

    /**
     * Generate an RGBA int from a color.
     *
     * @return Integer representation of color.
     */
    public int toRGBA() {
        int iR = (int) (r * 255);
        int iG = (int) (g * 255);
        int iB = (int) (b * 255);
        int iA = (int) (a * 255);

        return (iR << 24) + (iG << 16) + (iB << 8) + iA;
    }

    /**
     * Interpolate two colors.
     *
     * @param colA First color.
     * @param colB Second color.
     * @param weight How much "weight" each color should get, with 0 entirely favoring colA and 1 entirely favoring colB.
     * @return Mixed color.
     */
    public static Color lerpColor(Color colA, Color colB, float weight) {
        weight = Math.clamp(weight, 0, 1);
        float r = lerp(colA.r, colB.r, weight);
        float g = lerp(colA.g, colB.g, weight);
        float b = lerp(colA.b, colB.b, weight);
        float a = lerp(colA.a, colB.a, weight);
        return new Color(r, g, b, a);
    }
}
