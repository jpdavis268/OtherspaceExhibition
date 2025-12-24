package otherspace.core.engine;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBRectPack.stbrp_init_target;
import static org.lwjgl.stb.STBRectPack.stbrp_pack_rects;
import static org.lwjgl.stb.STBTruetype.*;

public final class TextureAtlas {
    private TextureAtlas() {}

    public static final int ATLAS_SIZE = 4096;

    /**
     * Batch a list of image-backed resources into a texture atlas.
     *
     * @param resources Resources to batch.
     */
    public static void batch(LinkedList<Bitmap> resources, int spriteCount, int fontCount) {
        ByteBuffer[] spriteImages = new ByteBuffer[spriteCount];
        ByteBuffer[] fontImages = new ByteBuffer[fontCount];
        Sprite[] sprites = new Sprite[spriteCount];
        Font[] fonts = new Font[fontCount];
        Vector2i[] spriteSizes = new Vector2i[spriteCount];
        Vector2i[] fontSizes = new Vector2i[fontCount];
        STBTTPackedchar.Buffer[] chars = new STBTTPackedchar.Buffer[fontCount];

        // Load sprite images and font bitmaps.
        int s = 0;
        int f = 0;
        for (Bitmap b : resources) {
            if (b instanceof Sprite sprite) {
                int[] w = new int[1];
                int[] h = new int[1];
                spriteImages[s] = stbi_load(sprite.srcPath, w, h, new int[1], 4);
                spriteSizes[s] = new Vector2i(w[0], h[0]);
                sprites[s] = sprite;
                s++;
            }
            else if (b instanceof Font font) {
                ByteBuffer bitmap;
                try (SeekableByteChannel fc = Files.newByteChannel(Paths.get(font.srcPath))) {
                    // Get font data
                    bitmap = BufferUtils.createByteBuffer((int) fc.size() + 1);

                    boolean finished = false;
                    while (!finished) {
                        finished = fc.read(bitmap) == -1;
                    }

                    bitmap.flip();
                }
                catch (IOException e) {
                    throw new RuntimeException("ERROR: Failed to load font " + font.srcPath);
                }

                // Pack font
                try (STBTTPackContext pc = STBTTPackContext.malloc()) {
                    STBTTPackedchar.Buffer charData = STBTTPackedchar.malloc(384);

                    int width = 256 * font.fontSize / 10;
                    int height = 64 * font.fontSize / 10;
                    ByteBuffer fontImage = BufferUtils.createByteBuffer(width * height);
                    stbtt_PackBegin(pc, fontImage, width, height, 0, 1, 0);
                    int p = 32;
                    charData.limit(p + 95);
                    charData.position(p);
                    stbtt_PackFontRange(pc, bitmap, 0, font.fontSize, 32, charData);
                    charData.clear();
                    stbtt_PackEnd(pc);
                    fontImages[f] = fontImage;
                    fontSizes[f] = new Vector2i(width, height);
                    chars[f] = charData;
                }

                fonts[f] = font;
                f++;
            }
        }

        // Pack bitmap dimensions.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int totalLength = spriteImages.length + fontImages.length;
            STBRPNode.Buffer nodes = STBRPNode.calloc(totalLength, stack);
            STBRPContext context = STBRPContext.calloc(stack);

            stbrp_init_target(context, ATLAS_SIZE, ATLAS_SIZE, nodes);

            STBRPRect.Buffer rectangles = STBRPRect.calloc(totalLength, stack);

            for (int i = 0; i < spriteImages.length; i++) {
                STBRPRect rect = rectangles.get(i);
                rect.w(spriteSizes[i].x);
                rect.h(spriteSizes[i].y);
            }
            for (int i = 0; i < fontImages.length; i++) {
                STBRPRect rect = rectangles.get(i + spriteImages.length);
                rect.w(fontSizes[i].x);
                rect.h(fontSizes[i].y);
            }

            if (stbrp_pack_rects(context, rectangles) != 1) {
                throw new RuntimeException(
                        "ERROR: Somebody was too lazy to make the texture atlas generator automatically resize!");
            }

            // Put images on atlas.
            ByteBuffer out = BufferUtils.createByteBuffer(ATLAS_SIZE * ATLAS_SIZE * 4);
            for (int i = 0; i < spriteImages.length; i++) {
                ByteBuffer buf = spriteImages[i];
                STBRPRect r = rectangles.get(i);
                int width = spriteSizes[i].x;
                int height = spriteSizes[i].y;
                for (int y = r.y(); y < r.y() + height; y++) {
                    out.put((y * ATLAS_SIZE + r.x()) * 4, buf, (y - r.y()) * width * 4, width * 4);
                }
            }

            for (int i = 0; i < fontImages.length; i++) {
                ByteBuffer buf = fontImages[i];
                STBRPRect r = rectangles.get(i + spriteImages.length);
                int width = fontSizes[i].x;
                int height = fontSizes[i].y;
                for (int y = r.y(); y < r.y() + height; y++) {
                    for (int x = 0; x < width; x++) {
                        byte colorVal = (byte) 0xFF;
                        byte alpha = buf.get(x + (y - r.y()) * width);
                        byte[] data = new byte[] {colorVal, colorVal, colorVal, alpha};
                        out.put((y * ATLAS_SIZE + r.x() + x) * 4, data, 0, data.length);
                    }
                }
            }

            // Create atlas.
            // Generate GPU texture.
            int texID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texID);

            // Set texture parameters
            // Pixelate when scaling
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            // Load texture
            out.flip();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, TextureAtlas.ATLAS_SIZE, TextureAtlas.ATLAS_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, out);

            // Initialize sprites
            int i = 0;
            for (Sprite sprite : sprites) {
                STBRPRect r = rectangles.get(i);
                float size = (float) TextureAtlas.ATLAS_SIZE;
                float topY = (r.y() + spriteSizes[i].y) / size;
                float rightX = (r.x() + spriteSizes[i].x) / size;
                float leftX = r.x() / size;
                float bottomY = r.y() / size;

                // Load data into sprite.
                Vector2f[] texCoords = {
                        new Vector2f(rightX, topY),
                        new Vector2f(leftX, bottomY),
                        new Vector2f(rightX, bottomY),
                        new Vector2f(leftX, topY)
                };
                sprite.loadAtlasData(spriteSizes[i].x, spriteSizes[i].y, texCoords);
                i++;
            }

            // Initialize fonts
            i = 0;
            for (Font font : fonts) {
                STBRPRect r = rectangles.get(i + spriteCount);
                float size = (float) TextureAtlas.ATLAS_SIZE;
                float topY = (r.y() + fontSizes[i].y) / size;
                float rightX = (r.x() + fontSizes[i].x) / size;
                float leftX = r.x() / size;
                float bottomY = r.y() / size;

                // Load data to font.
                Vector2f[] texCoords = {
                        new Vector2f(rightX, topY),
                        new Vector2f(leftX, bottomY),
                        new Vector2f(rightX, bottomY),
                        new Vector2f(leftX, topY)
                };
                font.loadAtlasData(texCoords, chars[i]);
            }
        }
    }
}
