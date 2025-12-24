package otherspace.core.engine;

import org.joml.Vector2f;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackedchar;
import otherspace.core.session.window.RenderHandler;

import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;

/**
 * Stores a font object that can be used to render text.
 */
public class Font implements Bitmap {
    public static final float PIXEL_RATIO = 0.7f;

    public final String srcPath;
    public final int fontSize;
    private Vector2f[] texCoords;
    private STBTTPackedchar.Buffer chars;

    /**
     * Initialize a font.
     *
     * @param srcPath Path to source (ttf).
     * @param fontSize Size of font.
     */
    public Font(String srcPath, int fontSize) {
        this.srcPath = srcPath;
        this.fontSize = fontSize;
    }

    /**
     * Load texture atlas information into this sprite.
     *
     * @param texCoords Location of sprite on atlas.
     * @param chars Character rendering information.
     */
    public void loadAtlasData(Vector2f[] texCoords, STBTTPackedchar.Buffer chars) {
        this.texCoords = texCoords;
        this.chars = chars;
    }

    /**
     * Draw a string to the screen. Formatting and line breaks are handled in the drawer wrapper method.
     *
     * @param x X position to draw at.
     * @param y Y position to draw at.
     * @param color Color to use.
     */
    public void drawText(String text, int x, int y, Color color) {
        float[] xpos = {x};
        float[] ypos = {y};
        STBTTAlignedQuad q = STBTTAlignedQuad.create();

        for (int i = 0; i < text.length(); i++) {
            stbtt_GetPackedQuad(chars, TextureAtlas.ATLAS_SIZE, TextureAtlas.ATLAS_SIZE, text.charAt(i), xpos, ypos, q, true);

            float charX1 = q.x0();
            float charY1 = q.y0();
            float charX2 = q.x1();
            float charY2 = q.y1();
            float leftX = texCoords[1].x + q.s0();
            float rightX = texCoords[1].x + q.s1();
            float topY = texCoords[1].y + q.t1();
            float bottomY = texCoords[1].y + q.t0();
            // Load data to font.
            Vector2f[] tex = {
                    new Vector2f(rightX, topY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, topY)
            };

            RenderHandler.getInstance().drawTexture((int) charX1, (int) charY1, (int) charX2, (int) charY2, tex, color, 1);
        }
    }

    /**
     * Insert line breaks into a provided string using a given width,
     * allowing for automated formatting in text displays.
     *
     * @param text Input text.
     * @param lineWidth How wide lines should be, in pixels.
     * @return Formatted text.
     */
    public String formatText(String text, float lineWidth) {
        StringBuilder out = new StringBuilder(text);

        STBTTAlignedQuad q = STBTTAlignedQuad.create();
        float[] width = {0};

        int breaks = 0;
        int lastSpaceIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            stbtt_GetPackedQuad(chars, 4096, 4096, text.charAt(i), width, new float[1], q, true);
            if (text.charAt(i) == ' ') {
                lastSpaceIndex = i;
            }
            if (width[0] >= lineWidth) {
                if (lastSpaceIndex == -1 || lastSpaceIndex < i - 16) {
                    out.insert(i - 1 + breaks, '\n');
                    width[0] = 0;
                }
                else {
                    String gap = out.substring(lastSpaceIndex, i);
                    out.insert(lastSpaceIndex + breaks + 1, '\n');
                    width[0] = getWidth(gap);
                }
                breaks++;
            }
        }

        return out.toString();
    }

    /**
     * Return the width of a string in pixels.
     *
     * @param text Text to get width of.
     * @return Width of string.
     */
    public int getWidth(String text) {
        STBTTAlignedQuad q = STBTTAlignedQuad.create();
        float[] width = {0};
        int highestWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            // This probably isn't optimal.
            stbtt_GetPackedQuad(chars, 4096, 4096, text.charAt(i), width, new float[1], q, true);
            if (text.charAt(i) == '\n') {
                highestWidth = Math.max(highestWidth, (int) width[0]);
                width[0] = 0;
            }
        }
        return Math.max(highestWidth, (int) width[0]);
    }

    /**
     * Return the total height of a string in pixels.
     *
     * @param text Text to get height of.
     * @return Height of string.
     */
    public int getHeight(String text) {
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lines++;
            }
        }

        return lines * fontSize;
    }
}
