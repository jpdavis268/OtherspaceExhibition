package otherspace.core.session.window;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;
import otherspace.core.engine.Color;
import otherspace.core.engine.Shader;
import otherspace.core.engine.TextureAtlas;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.engine.world.tiles.Tile;
import otherspace.core.session.scenes.SceneManager;
import otherspace.game.tiles.GroundTiles;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Low-level class for handling graphics rendering.
 */
public class RenderHandler {
    private static RenderHandler singleton;

    private final long windowHandle;
    private Matrix4f defaultProjMat = null;
    private Shader activeShader = Shader.defaultShader;

    private final FloatBuffer vertices;
    private final IntBuffer elements;
    private int numVertices;
    private int numElements;

    private boolean drawing;
    private boolean guiMode;

    public RenderHandler(long windowHandle) {
        singleton = this;
        this.windowHandle = windowHandle;

        // Initialize rendering buffers.
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        int eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);

        // Offset values.
        int posSize = 2;
        int colSize = 4;
        int texCoordSize = 2;
        int modsSize = 1;
        int posOffset = 0;
        int colOffset = posOffset + posSize * Float.BYTES;
        int texCoordOffset = colOffset + colSize * Float.BYTES;
        int modsOffset = texCoordOffset + texCoordSize * Float.BYTES;
        int vertexSize = 9;
        int vertexSizeBytes = vertexSize * Float.BYTES;

        // Enable attribute pointers.
        glVertexAttribPointer(0, posSize, GL_FLOAT, false, vertexSizeBytes, posOffset);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colSize, GL_FLOAT, false, vertexSizeBytes, colOffset);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, texCoordSize, GL_FLOAT, false, vertexSizeBytes, texCoordOffset);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, modsSize, GL_FLOAT, false, vertexSizeBytes, modsOffset);
        glEnableVertexAttribArray(3);

        // Create buffers
        int batchSize = 32768;
        vertices = MemoryUtil.memAllocFloat(batchSize);
        elements = MemoryUtil.memAllocInt(batchSize);

        // Variable initialization
        numVertices = 0;
        numElements = 0;
        drawing = false;

        // Enable blending and depth testing.
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Prepare to draw the next frame.
     */
    public void preDraw() {
        // Check that we have not received an illegal call.
        if (drawing) {
            throw new IllegalStateException("ERROR: Illegal call to RenderHandler pre-draw method!");
        }
        drawing = true;
        guiMode = false;

        // Clear background.
        Color bgColor = SceneManager.getCurrentScene().getBackground();
        glClearColor(bgColor.r(), bgColor.g(), bgColor.b(), bgColor.a());
        glClear(GL_COLOR_BUFFER_BIT);
    }

    /**
     * Prepare for GUI rendering.
     */
    public void guiPreDraw() {
        flush();
        guiMode = true;
    }

    /**
     * Finish drawing, clean up, and then push the frame.
     */
    public void pushFrame() {
        // Check for illegal call.
        if (!drawing) {
            throw new IllegalStateException("ERROR: Illegal call to RenderHandler frame push method!");
        }

        drawing = false;
        flush();

        glfwSwapBuffers(windowHandle);
    }

    /**
     * Set the default projection matrix to use (Set to null for normal GUI/Screen Rendering)
     *
     * @param projMat Projection matrix to use.
     */
    public void setDefaultProjMat(Matrix4f projMat) {
        defaultProjMat = projMat;
    }

    /**
     * Set the active shader for rendering.
     *
     * @param shader Shader to use.
     */
    public void setActiveShader(Shader shader) {
        activeShader.detach();
        if (shader != null) {
            activeShader = shader;
        }
        else {
            activeShader = Shader.defaultShader;
        }
    }

    /**
     * Send data to GPU and draw it.
     */
    public void flush() {
        if (defaultProjMat != null) {
            flush(defaultProjMat);
        }
        else if (guiMode) {
            flush(SceneManager.getCurrentScene().getCamera().getGuiProjMat());
        }
        else {
            flush(SceneManager.getCurrentScene().getCamera().getProjectionMatrix());
        }
    }

    /**
     * Send data to GPU and draw it.
     *
     * @param projMat Projection matrix to use.
     */
    public void flush(Matrix4f projMat) {
        if (numVertices > 0) {
            // Flip buffers
            vertices.flip();
            elements.flip();

            // Prepare shader
            activeShader.use();
            activeShader.uploadMat4f("uProjection", projMat);

            // Buffer data
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, GL_DYNAMIC_DRAW);

            // Draw elements
            glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_INT, 0);

            // Clear buffers for next batch
            vertices.clear();
            elements.clear();
            numVertices = 0;
            numElements = 0;
        }
    }

    /**
     * Draw a section of a texture atlas to the screen.
     *
     * @param x1 X-Coordinate to draw top left corner.
     * @param y1 Y-Coordinate to draw top left corner.
     * @param x2 X-Coordinate to draw bottom right corner.
     * @param y2 Y-Coordinate to draw bottom right corner.
     * @param texCoords Array of vectors containing texture coordinates.
     */
    public void drawTexture(int x1, int y1, int x2, int y2, Vector2f[] texCoords, Color c, int mod) {
        drawTexture(x1, y1, x2, y2, texCoords, c, c, c, c, mod);
    }

    /**
     * Draw a section of a texture atlas to the screen.
     *
     * @param x1 X-Coordinate to draw top left corner.
     * @param y1 Y-Coordinate to draw top left corner.
     * @param x2 X-Coordinate to draw bottom right corner.
     * @param y2 Y-Coordinate to draw bottom right corner.
     * @param texCoords Array of vectors containing texture coordinates.
     */
    public void drawTexture(int x1, int y1, int x2, int y2, Vector2f[] texCoords, Color c1, Color c2, Color c3, Color c4, int mod) {
        // Check vertex buffer capacity.
        if (vertices.capacity() - vertices.position() < 40) {
            flush();
        }

        // Vertices
        float[] vertexArray = {
                x2, y2, c3.r(), c3.g(), c3.b(), c3.a(), texCoords[0].x, texCoords[0].y, mod, // Bottom Right
                x1, y1, c1.r(), c1.g(), c1.b(), c1.a(), texCoords[1].x, texCoords[1].y, mod, // Top Left
                x2, y1, c2.r(), c2.g(), c2.b(), c2.a(), texCoords[2].x, texCoords[2].y, mod, // Top Right
                x1, y2, c4.r(), c4.g(), c4.b(), c4.a(), texCoords[3].x, texCoords[3].y, mod, // Bottom Left
        };
        // Triangles
        int v0 = numVertices;
        int v1 = numVertices + 1;
        int v2 = numVertices + 2;
        int v3 = numVertices + 3;
        int[] elementArray = {
                v2, v1, v0, // Top right
                v0, v1, v3  // Bottom left
        };

        // Buffer vertices
        vertices.put(vertexArray);
        numVertices += 4;

        // Buffer indices
        elements.put(elementArray);
        numElements += elementArray.length;
    }

    /**
     * Draw a filled rectangle to the screen.
     *
     * @param x1 Left boundary of rectangle.
     * @param y1 Top boundary of rectangle.
     * @param x2 Right boundary of rectangle.
     * @param y2 Bottom boundary of rectangle.
     */
    public void drawRect(int x1, int y1, int x2, int y2, Color c1, Color c2, Color c3, Color c4) {
        // Check vertex buffer capacity.
        if (vertices.capacity() - vertices.position() < 40) {
            flush();
        }

        // Vertices
        float[] vertexArray = {
                x2, y2, c3.r(), c3.g(), c3.b(), c3.a(), 0, 0, 0, // Bottom Right
                x1, y1, c1.r(), c1.g(), c1.b(), c1.a(), 0, 0, 0, // Top Left
                x2, y1, c2.r(), c2.g(), c2.b(), c2.a(), 0, 0, 0, // Top Right
                x1, y2, c4.r(), c4.g(), c4.b(), c4.a(), 0, 0, 0, // Bottom Left
        };
        // Triangles
        int v0 = numVertices;
        int v1 = numVertices + 1;
        int v2 = numVertices + 2;
        int v3 = numVertices + 3;
        int[] elementArray = {
                v2, v1, v0, // Top right
                v0, v1, v3  // Bottom left
        };

        // Buffer vertices
        vertices.put(vertexArray);
        numVertices += 4;

        // Buffer indices
        elements.put(elementArray);
        numElements += elementArray.length;
    }

    /**
     * Draw a filled rounded rectangle.
     *
     * @param x1 Leftmost x coordinate.
     * @param y1 Topmost y coordinate.
     * @param x2 Rightmost x coordinate.
     * @param y2 Bottommost y coordinate.
     * @param col1 Color of bottom right corner.
     * @param col2 Color of top left corner.
     * @param col3 Color of Top right corner.
     * @param col4 Color of bottom left corner.
     */
    public void drawRoundRect(int x1, int y1, int x2, int y2, Color col1, Color col2, Color col3, Color col4) {
        // Check vertex buffer capacity.
        if (vertices.capacity() - vertices.position() < 40) {
            flush();
        }

        // Vertices
        int xCenter = x1 + ((x2 - x1) / 2);
        int yCenter = y1 + ((y2 - y1) / 2);
        float[] vertexArray = {
                x2, y2, col1.r(), col1.g(), col1.b(), col1.a(), xCenter, yCenter, -(x2 - x1) / 2f, // Bottom Right
                x1, y1, col2.r(), col2.g(), col2.b(), col2.a(), xCenter, yCenter, -(x2 - x1) / 2f, // Top Left
                x2, y1, col3.r(), col3.g(), col3.b(), col3.a(), xCenter, yCenter, -(x2 - x1) / 2f, // Top Right
                x1, y2, col4.r(), col4.g(), col4.b(), col4.a(), xCenter, yCenter, -(x2 - x1) / 2f, // Bottom Left
        };

        // Triangles
        int v0 = numVertices;
        int v1 = numVertices + 1;
        int v2 = numVertices + 2;
        int v3 = numVertices + 3;
        int[] elementArray = {
                v2, v1, v0, // Top right
                v0, v1, v3  // Bottom left
        };

        // Buffer vertices
        vertices.put(vertexArray);
        numVertices += 4;

        // Buffer indices
        elements.put(elementArray);
        numElements += elementArray.length;
    }

    /**
     * Method for fast tilemap drawing, with texture splatting.
     *
     * @param leftX Top left x coordinate in screen space.
     * @param topY Top left y coordinate in screen space.
     * @param tileset Tileset to use.
     * @param tiledata Tiledata array.
     * @param splat Whether to splat the textures.
     * @param weight If splatting, how heavily to splat the textures.
     */
    public void drawTileMap(int leftX, int topY, Tile[] tileset, int[][] tiledata, boolean splat, float weight) {
        boolean isGround = tileset == GroundTile.getTileset();
        Vector2f[][] baseCoords = new Vector2f[tileset.length][];
        for (int i = 0; i < baseCoords.length; i++) {
            baseCoords[i] = tileset[i].getSrcSprite().getFrameCoords(0);
        }

        int start = splat ? 1 : 0;
        int end = splat ? 17 : 16;
        if (splat) {
            leftX -= 32;
            topY -= 32;
        }
        for (int i = start; i < end; i++) {
            for (int j = start; j < end; j++) {
                int tile = getAbsoluteTile(tiledata[i][j]);
                int x = leftX + i * 32;
                int y1 = topY + j * 32;

                if (tile >= 0) {
                    int state = (byte) ((tiledata[i][j] & 0x7FC00000) >> 22);
                    boolean flip = (state & 0b010000000) > 0;
                    int scale = flip ? -1 : 1;
                    int dWidth = scale < 0 ? -32 : 32;

                    int x1 = x + (scale < 0 ? 32 : 0);
                    int x2 = x1 + dWidth;
                    int y2 = y1 + 32;
                    drawTexture(x1, y1, x2, y2, adjustTileCoords(baseCoords[tile], tileset[tile], tiledata[i][j]), Color.WHITE, 1);

                    // Should probably find a less hacky way of doing this.
                    if (splat && ((tile != GroundTiles.LAB_DARK && tile != GroundTiles.LAB_LIGHT) || !isGround)) {
                        int topLeft = getAbsoluteTile(tiledata[i - 1][j - 1]);
                        int top = getAbsoluteTile(tiledata[i][j - 1]);
                        int topRight = getAbsoluteTile(tiledata[i + 1][j - 1]);
                        int right = getAbsoluteTile(tiledata[i + 1][j]);
                        int bottomRight = getAbsoluteTile(tiledata[i + 1][j + 1]);
                        int bottom = getAbsoluteTile(tiledata[i][j + 1]);
                        int bottomLeft = getAbsoluteTile(tiledata[i - 1][j + 1]);
                        int left = getAbsoluteTile(tiledata[i - 1][j]);

                        Color splatCol = new Color(1, 1, 1, 1);
                        Color fadeOut = new Color(1, 1, 1, 1 - 1 / weight);
                        int stateMask = state << 22;

                        if (top > tile) {
                            Vector2f[] coords = adjustTileCoords(baseCoords[top], tileset[top], top | stateMask);
                            drawTexture(x1, y1, x2, y2, coords, splatCol, splatCol, fadeOut, fadeOut, 1);
                        }
                        if (getAbsoluteTile(right) > tile) {
                            int t = getAbsoluteTile(right);
                            Vector2f[] coords = adjustTileCoords(baseCoords[t], tileset[t], t | stateMask);
                            if (flip) {
                                drawTexture(x1, y1, x2, y2, coords, splatCol, fadeOut, fadeOut, splatCol, 1);
                            }
                            else {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, splatCol, splatCol, fadeOut, 1);
                            }
                        }
                        if (bottom > tile) {
                            Vector2f[] coords = adjustTileCoords(baseCoords[bottom], tileset[bottom], bottom | stateMask);
                            drawTexture(x1, y1, x2, y2, coords, fadeOut, fadeOut, splatCol, splatCol, 1);
                        }
                        if (left > tile) {
                            Vector2f[] coords = adjustTileCoords(baseCoords[left], tileset[left], left | stateMask);
                            if (flip) {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, splatCol, splatCol, fadeOut, 1);
                            }
                            else {
                                drawTexture(x1, y1, x2, y2, coords, splatCol, fadeOut, fadeOut, splatCol, 1);
                            }
                        }

                        if (topLeft > tile) {
                            int t = Math.max(Math.max(top, left), topLeft);
                            Vector2f[] coords = adjustTileCoords(baseCoords[t], tileset[t], t | stateMask);
                            if (flip) {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, splatCol, fadeOut, fadeOut, 1);
                            }
                            else {
                                drawTexture(x1, y1, x2, y2, coords, splatCol, fadeOut, fadeOut, fadeOut, 1);
                            }
                        }
                        if (topRight > tile) {
                            int t = Math.max(Math.max(top, right), topRight);
                            Vector2f[] coords = adjustTileCoords(baseCoords[t], tileset[t], t | stateMask);
                            if (flip) {
                                drawTexture(x1, y1, x2, y2, coords, splatCol, fadeOut, fadeOut, fadeOut, 1);
                            }
                            else {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, splatCol, fadeOut, fadeOut, 1);
                            }
                        }
                        if (bottomRight > tile) {
                            int t = Math.max(Math.max(bottom, right), bottomRight);
                            Vector2f[] coords = adjustTileCoords(baseCoords[t], tileset[t], t | stateMask);
                            if (flip) {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, fadeOut, fadeOut, splatCol, 1);
                            }
                            else {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, fadeOut, splatCol, fadeOut, 1);
                            }
                        }
                        if (bottomLeft > tile) {
                            int t = Math.max(Math.max(bottom, left), bottomLeft);
                            Vector2f[] coords = adjustTileCoords(baseCoords[t], tileset[t], t | stateMask);
                            if (flip) {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, fadeOut, splatCol, fadeOut, 1);
                            }
                            else {
                                drawTexture(x1, y1, x2, y2, coords, fadeOut, fadeOut, fadeOut, splatCol, 1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the coords of a specific frame of a tile.
     *
     * @return Texture coordinates of tile frame.
     */
    private Vector2f[] adjustTileCoords(Vector2f[] frameCoords, Tile tile, int tiledata) {
        float tileOffset = 0.1f / TextureAtlas.ATLAS_SIZE;
        float atlasOffset = 32f / TextureAtlas.ATLAS_SIZE;

        int state = (byte) ((tiledata & 0x7FC00000) >> 22);
        Vector2i coords = tile.getTileCoords(Math.abs(state));

        // Adjust texture coordinates
        float x1 = (float) coords.x / TextureAtlas.ATLAS_SIZE;
        float y1 = (float) coords.y / TextureAtlas.ATLAS_SIZE;
        float x2 = x1 + atlasOffset;
        float y2 = y1 + atlasOffset;

        x1 += tileOffset;
        y1 += tileOffset;
        x2 -= tileOffset;
        y2 -= tileOffset;
        return new Vector2f[] {
                new Vector2f(frameCoords[1].x + x2, frameCoords[1].y + y2),
                new Vector2f(frameCoords[1].x + x1, frameCoords[1].y + y1),
                new Vector2f(frameCoords[3].x + x2, frameCoords[2].y + y1),
                new Vector2f(frameCoords[3].x + x1, frameCoords[2].y + y2)
        };
    }

    /**
     * Get the actual tiledata at this tile, without the offset or shifting due to graphical effects.
     *
     * @param tile Tile with graphical effects.
     * @return Tile ID in registry.
     */
    public int getAbsoluteTile(int tile) {
        return tile & 0x803FFFFF;
    }

    /**
     * Get whether we are currently drawing GUI elements.
     *
     * @return Whether renderer is drawing GUI.
     */
    public boolean inGuiMode() {
        return guiMode;
    }

    /**
     * Get the render handler singleton for low level operations.
     *
     * @return Render handler singleton.
     */
    public static RenderHandler getInstance() {
        return singleton;
    }
}
