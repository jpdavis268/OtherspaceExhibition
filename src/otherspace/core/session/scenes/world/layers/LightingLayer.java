package otherspace.core.session.scenes.world.layers;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.*;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.World;
import otherspace.core.session.window.RenderHandler;
import otherspace.core.session.window.Window;

import java.util.LinkedList;

/**
 * Layer that handles the lighting system.
 */
public class LightingLayer extends Layer {
    private static final Shader LIGHTING_SHADER = new Shader("resources/shaders/lighting/");

    private static LightingLayer singleton;
    private float ambientLightLevel;
    private final LinkedList<LightSource> lightSources;

    private final Surface LIGHTING_SURFACE;

    public LightingLayer() {
        singleton = this;
        ambientLightLevel = 1;
        lightSources = new LinkedList<>();

        LIGHTING_SURFACE = new Surface(1, 1) {
            @Override
            public void draw(Drawer d) {
                d.setColor(new Color(0, 0, 0, 1 - ambientLightLevel));

                // Draw ambient lighting mask.
                Camera camera = SceneManager.getCurrentScene().getCamera();
                d.drawRect(new Rectanglei(0, 0, width, height));

                // Draw lights.
                d.setBlendMode(Drawer.BLEND_REVERSE_SUBTRACT);
                RenderHandler.getInstance().setActiveShader(LIGHTING_SHADER);
                for (LightSource s : lightSources) {
                    int screenX = (int) ((width / 2f) - (camera.getPosition().x - s.location.x) * 32 * camera.getZoom());
                    int screenY = (int) ((height / 2f) - (camera.getPosition().y - s.location.y) * 32 * camera.getZoom());
                    int radius = (int) (s.radius * 32 * camera.getZoom());

                    // hack hack hack hack hack hack hack hack hack
                    int scaleFactor = Math.max(Window.getWidth(), Window.getHeight());
                    Color c = new Color(1, 1, (float) radius / scaleFactor, s.strength);
                    RenderHandler.getInstance().drawTexture(
                            screenX - radius,
                            screenY - radius,
                            screenX + radius,
                            screenY + radius,
                            new Vector2f[]{
                                    new Vector2f(screenX, screenY),
                                    new Vector2f(screenX, screenY),
                                    new Vector2f(screenX, screenY),
                                    new Vector2f(screenX, screenY)
                            },
                            c,
                            scaleFactor
                    );
                }
                d.setBlendMode(Drawer.BLEND_ADD);
                RenderHandler.getInstance().setActiveShader(null);
            }
        };
    }

    @Override
    public void draw(Drawer d) {
        super.draw(d);
        ambientLightLevel = Math.min(World.getDayFactor() + 0.05f, 1);

        LIGHTING_SURFACE.setWidth(Window.getWidth());
        LIGHTING_SURFACE.setHeight(Window.getHeight());
        LIGHTING_SURFACE.render(d, 0, 0);

        // Cleanup
        lightSources.clear();
    }

    /**
     * Add a new light source to the current frame.
     *
     * @param location Location of light source.
     * @param radius Radius of light source.
     * @param strength How strong light source is, from 0 to 1.
     */
    public static void drawLight(Vector2d location, float radius, float strength) {
        radius = Math.max(0, radius);
        strength = Math.clamp(strength, 0, 1);
        singleton.lightSources.add(new LightSource(location, radius, strength));
    }

    private record LightSource(Vector2d location, float radius, float strength) {}
}
