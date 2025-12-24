package otherspace.core.engine;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import org.joml.primitives.Rectanglei;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.RenderHandler;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds a set of vectors defining a loaded image on a texture atlas, and how it should be handled by the game.
 */
public class Sprite implements Bitmap {
    public static final String MISSING_TEXTURE = "resources/sprites/missingTexture.png";

    public final String srcPath;
    private int frames;
    private Vector2i origin;
    private Rectangled boundingBox;
    private int width;
    private int height;
    private Vector2f[] texCoords;
    private int frameWidth;
    private float frameXOffset;

    public Sprite(String srcPath, int frames, Vector2i origin) {
        this(srcPath, frames, origin, null);
    }

    public Sprite(String srcPath, int frames, Vector2i origin, Rectangled boundingBox) {
        if (srcPath != null && Files.exists(Path.of(srcPath))) {
            this.srcPath = srcPath;
        }
        else {
            this.srcPath = MISSING_TEXTURE;
        }
        this.frames = frames;
        this.origin = origin;
        this.boundingBox = boundingBox;
    }

    /**
     * Load texture atlas information into this sprite.
     *
     * @param width Width of sprite.
     * @param height Height of sprite.
     * @param texCoords Location of sprite on atlas.
     */
    public void loadAtlasData(int width, int height, Vector2f[] texCoords) {
        this.width = width;
        this.height = height;
        this.texCoords = texCoords;
        frameWidth = width / frames;
        frameXOffset = (float) frameWidth / TextureAtlas.ATLAS_SIZE;
    }

    /**
     * Get the texture coordinates for a specified frame of the sprite.
     *
     * @param frame Frame of sprite.
     * @return Adjusted texture coordinates.
     */
    public Vector2f[] getFrameCoords(int frame) {
        float left = frameXOffset * frame;
        float right = left + frameXOffset;

        return new Vector2f[] {
                new Vector2f(texCoords[1].x + right, texCoords[0].y),
                new Vector2f(texCoords[1].x + left, texCoords[1].y),
                new Vector2f(texCoords[3].x + right, texCoords[2].y),
                new Vector2f(texCoords[3].x + left, texCoords[3].y)
        };
    }

    /**
     * Draw this sprite.
     *
     * @param x X-Position to draw at on screen.
     * @param y Y-Position to draw at on screen.
     * @param depth How far this sprite should be from the camera.
     * @param color Color to mix into sprite.
     * @param xScale How much to scale this sprite on the x-axis.
     * @param yScale How much to scale this sprite on the y-axis.
     * @param frame Frame of sprite to draw.
     */
    public void draw(int x, int y, int depth, Color color, float xScale, float yScale, int frame) {
        drawPart(x, y, color, xScale, yScale, frame, new Rectanglei(0, 0, frameWidth, height));
    }

    /**
     * Draw a section of this sprite.
     *
     * @param x X Coordinate to draw at.
     * @param y Y Coordinate to draw at.
     * @param color Color to mix with sprite.
     * @param xScale How much to scale sprite horizontally.
     * @param yScale How much to scale sprite vertically.
     * @param frame Frame of sprite to draw.
     * @param section Rectangle defining section of sprite to draw.
     */
    public void drawPart(int x, int y, Color color, float xScale, float yScale, int frame, Rectanglei section) {
        drawPart(x, y, color, color, color, color, xScale, yScale, frame, section);
    }

    /**
     * Draw a section of this sprite.
     *
     * @param x X Coordinate to draw at.
     * @param y Y Coordinate to draw at.
     * @param c1 First color to mix with sprite.
     * @param c2 Second color to mix with sprite.
     * @param c3 Third color to mix with sprite.
     * @param c4 Fourth color to mix with sprite.
     * @param xScale How much to scale sprite horizontally.
     * @param yScale How much to scale sprite vertically.
     * @param frame Frame of sprite to draw.
     * @param section Rectangle defining section of sprite to draw.
     */
    public void drawPart(int x, int y, Color c1, Color c2, Color c3, Color c4, float xScale, float yScale, int frame, Rectanglei section) {
        int dWidth = (int) (section.lengthX() * xScale);
        int dHeight = (int) (section.lengthY() * yScale);

        if (!RenderHandler.getInstance().inGuiMode()) {
            Vector2d cameraPos = SceneManager.getCurrentScene().getCamera().getPosition();

            x -= (int) (cameraPos.x * 32);
            y -= (int) (cameraPos.y * 32);
        }

        int xOffset = (int) (x - origin.x * xScale);
        int yOffset = (int) (y - origin.y * yScale);

        if (SceneManager.getCurrentScene().getCamera().isAreaVisible(new Rectangled(xOffset, yOffset, xOffset + dWidth, yOffset + dHeight).scale(0.9))) {
            // Adjust texture coordinates
            float x1 = (float) Math.clamp(section.minX, 0, width) / TextureAtlas.ATLAS_SIZE;
            float y1 = (float) Math.clamp(section.minY, 0, height) / TextureAtlas.ATLAS_SIZE;
            float x2 = (float) Math.clamp(section.maxX, 0, width) / TextureAtlas.ATLAS_SIZE;
            float y2 = (float) Math.clamp(section.maxY, 0, height) / TextureAtlas.ATLAS_SIZE;

            x1 += 0.1f / TextureAtlas.ATLAS_SIZE;
            y1 += 0.1f / TextureAtlas.ATLAS_SIZE;
            x2 -= 0.1f / TextureAtlas.ATLAS_SIZE;
            y2 -= 0.1f / TextureAtlas.ATLAS_SIZE;

            Vector2f[] frameCoords = getFrameCoords(frame);
            Vector2f[] adjCoords = {
                    new Vector2f(frameCoords[1].x + x2, frameCoords[1].y + y2),
                    new Vector2f(frameCoords[1].x + x1, frameCoords[1].y + y1),
                    new Vector2f(frameCoords[3].x + x2, frameCoords[2].y + y1),
                    new Vector2f(frameCoords[3].x + x1, frameCoords[2].y + y2)
            };

            RenderHandler.getInstance().drawTexture(xOffset, yOffset, xOffset + dWidth, yOffset + dHeight, adjCoords, c1, c2, c3, c4, 1);
        }
    }

    /**
     * Get the width of this sprite.
     *
     * @return Sprite width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the width of this sprite.
     *
     * @return Sprite width.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get how many frames this sprite has.
     *
     * @return Number of sprite frames.
     */
    public int getFrames() {
        return frames;
    }

    /**
     * Get the bounding box of this sprite (for use in client-side collision detection).
     */
    public Rectangled getBoundingBox() {
        return boundingBox;
    }
}
