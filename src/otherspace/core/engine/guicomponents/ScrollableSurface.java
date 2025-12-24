package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Surface;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.window.MouseListener;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

/**
 * Scrollable panel that holds a surface and displays a section of it at a time.
 */
public class ScrollableSurface extends Component {
    private final Surface surface;
    private final boolean defaultToBottom;
    private int maxHeight;
    private int ceiling;
    private boolean scrollMouseSelected = false;

    public ScrollableSurface(int width, int height, int maxHeight, boolean defaultToBottom, Surface surface) {
        super(width, height);

        this.maxHeight = maxHeight;
        this.surface = surface;
        this.defaultToBottom = defaultToBottom;
        resetScroll();
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        // Draw contents
        surface.render(d, x, y, new Rectanglei(0, ceiling, width, ceiling + maxHeight));

        // Draw scrollbar
        if (height > maxHeight) {
            Color scrollColor;
            int barHeight = (int) ((float) maxHeight / (float) height * (float) maxHeight);
            int barPos = (int) (((float) ceiling / (float) height) * (float) maxHeight);
            int scrollX = x + width;

            // Handle mouse wheel input
            if (MouseListener.getScroll() > 0 && ceiling > 0) {
                ceiling -= Math.min(50, ceiling);
            }

            if (MouseListener.getScroll() < 0 && ceiling + maxHeight < height) {
                ceiling += Math.min(50, height - ceiling - maxHeight);
            }

            if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_MIDDLE)) {
                resetScroll();
            }

            // Handle mouse dragging
            if (GenUtils.isMouseOver(new Rectanglei(scrollX, y, scrollX + 10, y + maxHeight)) || scrollMouseSelected) {
                scrollColor = Color.LIGHT_GRAY;
                if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT)) {
                    scrollMouseSelected = true;
                    ceiling = (int) (Math.clamp(MouseListener.getMouseY() - y - (double) barHeight / 2, 0, maxHeight - barHeight) / maxHeight * height);
                }
                else {
                    scrollMouseSelected = false;
                }
            }
            else {
                scrollColor = Color.GRAY;
            }

            // Draw scroll bar
            d.setColor(scrollColor);
            d.drawRect(new Rectanglei(scrollX, y + barPos, scrollX + 10, y + barPos + barHeight));
        }
    }

    /**
     * Set the height of the internal surface (NOT the viewport height).
     *
     * @param height New height for surface.
     */
    public void setHeight(int height) {
        this.height = height;
        surface.height = height;
    }

    /**
     * Get the total height of the internal surface.
     *
     * @return Internal surface height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the height of the internal surface (NOT the viewport height).
     *
     * @param height New height for surface.
     */
    public void setMaxHeight(int height) {
        if (maxHeight != height) {
            maxHeight = height;
            ceiling = 0;
        }
    }

    /**
     * Get the total height of the internal surface.
     *
     * @return Internal surface height.
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Reset the scroll offset to its base.
     */
    public void resetScroll() {
        ceiling = defaultToBottom ? (int) (height - maxHeight) : 0;
    }

    /**
     * Get the "ceiling" of the surface viewport.
     *
     * @return Viewport "ceiling".
     */
    public int getCeiling() {
        return ceiling;
    }
}
