package otherspace.core.engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.MouseListener;
import otherspace.core.session.window.RenderHandler;
import otherspace.core.session.window.Window;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Defines a surface, a framebuffer that can be drawn to in isolation and rendered on the main window.
 */
public abstract class Surface {
    public static final int FBO_ID = glGenFramebuffers();
    public static final int TEX_ID = glGenTextures();

    private static Surface currentSurface = null;
    private final Vector2i currentPos = new Vector2i();
    public int width;
    public int height;

    public Surface(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Render this surface.
     *
     * @param d Drawer to use.
     * @param x X position to draw at.
     * @param y Y position to draw at.
     */
    public void render(Drawer d, int x, int y) {
        render(d, x, y, new Rectanglei(0, 0, width, height));
    }

    /**
     * Render a section of this surface.
     *
     * @param d Drawer to use.
     * @param x X Position to draw at.
     * @param y Y Position to draw at.
     * @param section Section of surface to draw.
     */
    public void render(Drawer d, int x, int y, Rectanglei section) {
        // Prepare for drawing
        RenderHandler r = RenderHandler.getInstance();
        r.flush();
        currentSurface = this;
        currentPos.x = x;
        currentPos.y = y;

        // Cull anything outside of this area
        r.setDefaultProjMat(new Matrix4f().ortho(section.minX, section.maxX, section.maxY, section.minY, 0, 100));
        glViewport(0, 0, section.lengthX(), section.lengthY());

        // Bind framebuffer.
        glBindTexture(GL_TEXTURE_2D, TEX_ID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, section.lengthX(), section.lengthY(), 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 1);

        glBindFramebuffer(GL_FRAMEBUFFER, FBO_ID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, TEX_ID, 0);
        Color background = SceneManager.getCurrentScene().getBackground();
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw
        draw(d);

        // End drawing
        r.flush();
        r.setDefaultProjMat(SceneManager.getCurrentScene().getCamera().getGuiProjMat());
        glViewport(0, 0, Window.getWidth(), Window.getHeight());
        glClearColor(background.r(), background.g(), background.b(), background.a());
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, TEX_ID);

        r.drawTexture(x, y, x + section.lengthX(), y + section.lengthY(),
                new Vector2f[] {
                        new Vector2f(1, 0),
                        new Vector2f(0, 1),
                        new Vector2f(1, 1),
                        new Vector2f(0, 0)
                }, Color.WHITE, 1);

        r.flush();
        r.setDefaultProjMat(null);
        glBindTexture(GL_TEXTURE_2D, 1);

        currentSurface = null;
    }

    /**
     * Draw orders for this surface, to be defined in an anonymous inner class.
     *
     * @param d Passed in drawer.
     */
    public abstract void draw(Drawer d);

    /**
     * Get the current drawing surface.
     *
     * @return Current drawing surface.
     */
    public static Surface getCurrentSurface() {
        return currentSurface;
    }

    /**
     * Get where the mouse is on screen in relation to this surface.
     *
     * @return Mouse location relative to this surface.
     */
    public Vector2i getRelativeMouseLocation() {
        int mX = (int) (MouseListener.getMouseX() - currentPos.x);
        int mY = (int) (MouseListener.getMouseY() - currentPos.y);
        return new Vector2i(mX, mY);
    }

    /**
     * Set this surface's width.
     *
     * @param width New width for surface.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Set this surface's height.
     *
     * @param height New height for surface.
     */
    public void setHeight(int height) {
        this.height = height;
    }
}
