package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Surface;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.window.MouseListener;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * Draggable slider that modifies a value.
 */
public class Slider extends Component {
    private String label;
    private float position;
    private final Consumer<Component> updateAction;
    private boolean mouseIsDragging = false;
    private static boolean otherSelected = false;

    public Slider(int width, String label, float startingValue, Consumer<Component> updateAction) {
        super(width, 40);

        this.label = label;
        this.position = startingValue;
        this.updateAction = updateAction;
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        // Draw label
        int xOffset = x - width / 2;
        d.setHalign(Drawer.H_LEFT);
        d.setValign(Drawer.V_TOP);
        d.setColor(Color.WHITE);
        d.drawText(xOffset, y, SettingsManager.getText(label));

        // Slide bar
        d.setColor(Color.GRAY);
        d.drawRect(new Rectanglei(xOffset, y + 28, xOffset + width, y + 32));

        // Notch
        int offset = (int) (position * width);
        d.setColor(Color.WHITE);
        d.drawRect(new Rectanglei(xOffset + offset - 3, y + 20, xOffset + offset + 3, y + 40));

        // Handle mouse dragging
        if ((GenUtils.isMouseOver(new Rectanglei(xOffset, y + 20, xOffset + width, y + 40)) && !otherSelected) || mouseIsDragging) {
            // If the player clicks while over the slider, start dragging it.
            if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT)) {
                mouseIsDragging = true;
                otherSelected = true;
                Surface curSurf = Surface.getCurrentSurface();
                float mX = curSurf == null ? (float) MouseListener.getMouseX() : curSurf.getRelativeMouseLocation().x;
                position = Math.clamp(mX - xOffset, 0, width) / width;
                updateAction.accept(this);
            }
            else {
                mouseIsDragging = false;
                otherSelected = false;
            }
        }
    }

    /**
     * Change the label of the slider.
     *
     * @param label New label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the slider's current position.
     *
     * @return Current slider position.
     */
    public float getPosition() {
        return position;
    }

    /**
     * Set the position of this slider.
     *
     * @param position New position (Clamped between 0 and 1).
     */
    public void setPosition(float position) {
        this.position = Math.clamp(position, 0, 1);
    }
}
