package otherspace.core.engine;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.primitives.Rectangled;
import otherspace.core.session.window.MouseListener;
import otherspace.core.session.window.RenderHandler;
import otherspace.core.session.window.Window;

/**
 * Defines the viewport in an attached scene.
 */
public class Camera {
    // Camera data
    // Going to use doubles instead of floats to prevent issues far from the origin,
    // may have to change it for rendering though.
    private Scene myScene;
    private Matrix4f projectionMatrix;
    private Matrix4f guiProjMat;
    private Rectangled viewBounds;
    private float zoom;
    private Vector2d position;
    private int viewWidth = 0;
    private int viewHeight = 0;

    public Camera(Scene myScene) {
        position = new Vector2d();
        zoom = 1;
        this.myScene = myScene;
        this.projectionMatrix = new Matrix4f();
        this.guiProjMat = new Matrix4f();
        adjustProjection(Window.getWidth(), Window.getHeight());
    }

    /**
     * Adjust the coordinate projection matrix.
     */
    public void adjustProjection(int width, int height) {
        if (width != viewWidth * zoom || height != viewHeight * zoom) {
            projectionMatrix.identity();
            guiProjMat.identity();
            float xDist = (float) width / (2 * zoom);
            float yDist = (float) height / (2 * zoom);
            projectionMatrix.ortho(-xDist, xDist, yDist, -yDist, 0, 100);
            guiProjMat.ortho(0, width, height, 0, 0, 100);
            viewWidth = (int) (width / zoom);
            viewHeight = (int) (height / zoom);
            viewBounds = new Rectangled(-xDist, -yDist, xDist, yDist);
        }
    }

    /**
     * Get the camera position in the scene.
     *
     * @return Camera position.
     */
    public Vector2d getPosition() {
        return position;
    }

    /**
     * Set the camera position in the scene.
     *
     * @param position New camera position.
     */
    public void setPosition(Vector2d position) {
        this.position = position;
    }

    /**
     * Get the location of the mouse within the game world.
     *
     * @return Mouse position in game world.
     */
    public Vector2d getMouseWorldPosition() {
        return new Vector2d(MouseListener.getMouseX(), MouseListener.getMouseY())
                .sub(Window.getWidth() / 2d, Window.getHeight() / 2d)
                .div(32)
                .div(zoom)
                .add(position);
    }

    /**
     * Get a location in the world relative to the screen.
     *
     * @param worldCoords World coordinates to convert.
     * @return Coordinates on screen.
     */
    public Vector2d worldCoordsToScreen(Vector2d worldCoords) {
        return worldCoords.sub(position)
                .mul(32)
                .mul(zoom)
                .add(Window.getWidth() / 2d, Window.getHeight() / 2d);
    }

    /**
     * Get the projection matrix.
     *
     * @return Projection matrix.
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Get the GUI projection matrix.
     *
     * @return GUI projection matrix.
     */
    public Matrix4f getGuiProjMat() {
        return guiProjMat;
    }

    /**
     * Get the current camera viewport width.
     *
     * @return Viewport width.
     */
    public int getViewWidth() {
        return viewWidth;
    }

    /**
     * Get the current camera viewport height.
     *
     * @return Viewport height.
     */
    public int getViewHeight() {
        return viewHeight;
    }

    /**
     * Get the current camera zoom.
     *
     * @return Current camera zoom.
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Check if an area is within the camera viewport.
     *
     * @param area Area to check.
     * @return Whether area overlaps with viewport.
     */
    public boolean isAreaVisible(Rectangled area) {
        if (RenderHandler.getInstance().inGuiMode()) {
            return true;
        }
        return viewBounds.intersectsRectangle(area);
    }

    /**
     * Get the current viewport of the camera.
     *
     * @return Camera viewport.
     */
    public Rectangled getViewBounds() {
        return viewBounds;
    }

    /**
     * Set the current camera zoom.
     */
    public void setZoom(float zoom) {
        this.zoom = Math.clamp(zoom, 0.5f, 2);
    }
}
