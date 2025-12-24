package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.window.MouseListener;
import otherspace.game.Assets;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * Clickable button that runs an action when pressed.
 */
public class Button extends Component {
    private final Color defaultColor;
    private final Color overColor;

    private String text;
    private String hoverText;
    private Consumer<Component> action;

    public Button(int width, int height, String text, Consumer<Component> action, Color defaultColor, Color overColor) {
        super(width, height);

        this.text = text;
        this.hoverText = "";
        this.action = action;
        this.defaultColor = defaultColor;
        this.overColor = overColor;
    }

    public Button(int width, int height, String text, Consumer<Component> action, String hoverText) {
        this(width, height, text, action, Color.GRAY, Color.LIGHT_GRAY);

        this.text = text;
        this.hoverText = hoverText;
        this.action = action;
    }

    public Button(int width, int height, String text, Consumer<Component> action) {
        this(width, height, text, action, Color.GRAY, Color.LIGHT_GRAY);

        this.text = text;
        this.action = action;
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        // Prepare drawing configuration.
        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        int xDist = width / 2;
        int yDist = height / 2;
        Rectanglei area = new Rectanglei(x - xDist, y - yDist, x + xDist, y + yDist);

        // Check for selection
        Color toUse = defaultColor;
        if (GenUtils.isMouseOver(area)) {
            SceneManager.setHoverText(hoverText);

            toUse = overColor;
            if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                SoundManager.playSound(Assets.uiSelSound, false);
                invokeCallback();
            }
        }

        // Draw button
        d.setColor(toUse);
        d.drawRect(area);
        d.setColor(Color.WHITE);
        d.drawText(x, y, SettingsManager.getText(text));
    }



    /**
     * Set this button's text.
     *
     * @param text New button text.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Set this button's hover text.
     *
     * @param hoverText Button hover text.
     */
    public void setHoverText(String hoverText) {
        this.hoverText = hoverText;
    }

    /**
     * Invoke this button's callback action.
     */
    public void invokeCallback() {
        action.accept(this);
    }
}
