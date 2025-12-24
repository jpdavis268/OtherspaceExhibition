package otherspace.core.session;

import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Font;
import otherspace.core.engine.Sprite;
import otherspace.core.session.window.RenderHandler;
import otherspace.game.Assets;

import static org.joml.Math.lerp;
import static org.lwjgl.opengl.GL14.*;

/**
 * High level wrapper for graphics rendering, to be passed down and used for drawing things to the screen.
 */
public class Drawer {
    private final RenderHandler r;

    // Alignment constants
    public static final byte H_LEFT = 0;
    public static final byte H_CENTER = 1;
    public static final byte H_RIGHT = 2;
    public static final byte V_TOP = 2;
    public static final byte V_MIDDLE = 1;
    public static final byte V_BOTTOM = 0;

    // Blending Constants
    public static final int BLEND_ADD = GL_FUNC_ADD;
    public static final int BLEND_SUBTRACT = GL_FUNC_SUBTRACT;
    public static final int BLEND_REVERSE_SUBTRACT = GL_FUNC_REVERSE_SUBTRACT;

    // Drawing preferences
    private Color color = Color.WHITE;
    private Font font = Assets.DEFAULT_FONT;
    private int halign = H_LEFT;
    private int valign = V_TOP;

    public Drawer(RenderHandler renderer) {
        r = renderer;
    }

    /**
     * Draw a rectangle using the current draw color.
     *
     * @param rect Rectangle to draw.
     */
    public void drawRect(Rectanglei rect) {
        r.drawRect(rect.minX, rect.minY, rect.maxX, rect.maxY, color, color, color, color);
    }

    /**
     * Draw a rounded rectangle using the current draw color.
     *
     * @param rect Area to draw in.
     */
    public void drawRoundRect(Rectanglei rect) {
        r.drawRoundRect(rect.minX, rect.minY, rect.maxX, rect.maxY, color, color, color, color);
    }

    /**
     * Draw a bar showing the amount of a value in comparison to a maximum value.
     *
     * @param area Rectangle defining bar bounds.
     * @param value Value to display.
     * @param maxValue Maximum value of bar.
     * @param backCol Color to use for background, if applicable.
     * @param lowCol Color to fade towards when value is low.
     * @param highCol Color to fade towards when value is high.
     * @param vertical Whether to draw bar from left to right (false) or bottom to top (true)
     * @param showBack Whether to show the background of the bar.
     * @param showBorder Whether to show the border of the bar.
     */
    public void drawValueBar(Rectanglei area, float value, float maxValue, Color backCol, Color lowCol, Color highCol, boolean vertical, boolean showBack, boolean showBorder) {
        // Store current color
        Color cache = this.color;

        // Draw border
        if (showBorder) {
            setColor(Color.BLACK);
            drawRect(new Rectanglei(area.minX - 1, area.minY - 1, area.maxX + 1, area.maxY + 1));
        }

        // Draw back
        if (showBack) {
            setColor(backCol);
            drawRect(area);
        }

        // Draw bar
        float pos = Math.clamp(value / maxValue, 0, 1);
        setColor(Color.lerpColor(lowCol, highCol, pos));
        if (vertical) {
            drawRect(new Rectanglei(area.minX, area.minY, area.maxX, (int) lerp(area.minY, area.maxY, pos)));
        }
        else {
            drawRect(new Rectanglei(area.minX, area.minY, (int) lerp(area.minX, area.maxX, pos), area.maxY));
        }

        // Revert to previous color.
        setColor(cache);
    }

    /**
     * Set default horizontal alignment for drawing.
     *
     * @param halign New alignment.
     */
    public void setHalign(int halign) {
        this.halign = Math.clamp(halign, 0, 2);
    }

    /**
     * Set default vertical alignment for drawing.
     *
     * @param valign New alignment.
     */
    public void setValign(int valign) {
        this.valign = Math.clamp(valign, 0, 2);
    }

    /**
     * Draw a sprite to the screen.
     *
     * @param toDraw Sprite to draw.
     * @param x X-Coordinate of origin.
     * @param y Y-Coordinate of origin.
     * @param frame Frame to draw.
     */
    public void drawSprite(Sprite toDraw, int x, int y, int frame) {
        drawSpriteExt(toDraw, x, y, 0, Color.WHITE, 1, 1, frame);
    }

    /**
     * Draw a sprite to the screen with additional options.
     *
     * @param toDraw Sprite to draw.
     * @param x X-Coordinate of origin.
     * @param y Y-Coordinate of origin.
     * @param color Color to mask sprite with.
     * @param frame Frame to draw.
     */
    public void drawSpriteExt(Sprite toDraw, int x, int y, int depth, Color color, float xScale, float yScale, int frame) {
        toDraw.draw(x, y, depth, color, xScale, yScale, frame);
    }

    /**
     * Draw text to the screen using the current draw color and alignment.
     *
     * @param x X Position to draw at.
     * @param y Y Position to draw at.
     * @param text Text to draw.
     */
    public void drawText(int x, int y, String text) {
        // Split off newlines.
        String[] lines = text.split("\n");

        // Calculate text width.
        int[] strWidth = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            int width = font.getWidth(lines[i]);
            strWidth[i] = width;
        }

        // Get y-offset based on alignment settings.
        int yOffset = (int) (font.fontSize * Font.PIXEL_RATIO / 2 * valign);

        for (int i = 0; i < lines.length; i++) {
            // Calculate x-offset.
            int xOffset = (strWidth[i] / 2) * halign + 1;

            // Draw this line at calculated location.
            font.drawText(lines[i], x - xOffset, y + yOffset + (font.fontSize * i), color);
        }
    }

    /**
     * Helper method to draw text with a background.
     *
     * @param location Position to draw at.
     * @param text Text to draw.
     */
    public void drawTextBox(Vector2i location, String text) {
        String toDraw = font.formatText(SettingsManager.getText(text), 512);
        int boxWidth = font.getWidth(toDraw) + 4;
        int boxHeight = font.getHeight(toDraw) + 2;

        setColor(new Color(0, 0, 0, 200));
        drawRect(new Rectanglei(location, new Vector2i(location).add(boxWidth, boxHeight)));

        setHalign(H_LEFT);
        setValign(V_TOP);
        setColor(Color.WHITE);
        drawText(location.x + 2, location.y + 2, toDraw);
    }

    /**
     * Get the current font.
     *
     * @return Current font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Set the default color for drawing.
     *
     * @param color Color to use.
     */
    public void setColor(Color color) {
        this.color = color;
    }


    /**
     * Set the blend mode for rendering. One of three static constants:
     * - Drawer.BLEND_ADD
     * - Drawer.BLEND_SUBTRACT
     * - Drawer.BLEND_REVERSE_SUBTRACT
     * The default mode is BLEND_ADD.
     *
     * @param mode Mode to use for blending.
     */
    public void setBlendMode(int mode) {
        r.flush();
        glBlendEquation(mode);
    }

    /**
     * Convert a position in the world to a position on the screen for rendering.
     *
     * @param worldPos Position in world.
     * @return Position for rendering.
     */
    public static int pos(double worldPos) {
        return (int) (worldPos * 32);
    }
}
