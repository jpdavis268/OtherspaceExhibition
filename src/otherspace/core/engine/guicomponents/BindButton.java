package otherspace.core.engine.guicomponents;

import com.google.gson.JsonPrimitive;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.engine.utils.StringUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.MouseListener;
import otherspace.game.Assets;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Variant of buttons used for keybinds.
 */
public class BindButton extends Component {
    static boolean otherSelected = false;
    boolean selected = false;
    int defaultBind;
    String text;
    String bind;

    public BindButton(int width, int height, String text, String bind, int defaultBind) {
        super(width, height);

        this.text = text;
        this.bind = bind;
        this.defaultBind = defaultBind;
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        int xDist = width / 2;
        int yDist = height / 2;

        // Draw button and handle mouse actions.
        String bindText = getKeyName(SettingsManager.get(bind).getAsInt());
        Color toUse = Color.GRAY;
        if (GenUtils.isMouseOver(new Rectanglei(x - xDist, y - yDist, x + xDist, y + yDist)) || selected) {
            toUse = Color.LIGHT_GRAY;

            // Toggle Selection
            if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT) && !otherSelected) {
                SoundManager.playSound(Assets.uiSelSound, false);
                selected = true;
                otherSelected = true;
                KeyListener.clearLastKey();
                bindText = StringUtils.getFieldCarat();
            }

            // Get keyboard input
            if (selected && KeyListener.getLastKey() != GLFW_KEY_UNKNOWN) {
                SettingsManager.set(bind, new JsonPrimitive(KeyListener.getLastKey()));
                selected = false;
                otherSelected = false;
                bindText = getKeyName(KeyListener.getLastKey());
                KeyListener.clearKey(KeyListener.getLastKey());
            }
        }

        // Draw button
        d.setColor(toUse);
        d.drawRect(new Rectanglei(x - xDist, y - yDist, x + xDist, y + yDist));
        d.setColor(Color.WHITE);
        d.drawText(x, y, SettingsManager.getText(text) + bindText);
    }

    /**
     * Reset this keybind to its default value.
     */
    public void resetBind() {
        SettingsManager.set(bind, new JsonPrimitive(defaultBind));
    }

    /**
     * Get whether a bind is currently awaiting input.
     *
     * @return Whether a bind is being set.
     */
    public static boolean isBindSelected() {
        return otherSelected;
    }

    /**
     * Get a name for a specific keycode.
     *
     * @param keycode Keycode in question.
     * @return What this keycode is.
     */
    public static String getKeyName(int keycode) {
        if (keycode > 32 && keycode < 127) {
            // If this is a key or number, return that.
            return String.valueOf((char) keycode);
        }

        return switch (keycode) {
            // Otherwise, compare it to this table.
            case GLFW_KEY_UNKNOWN -> "No Key";
            case GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW_KEY_TAB -> "Tab";
            case GLFW_KEY_ENTER -> "Enter";
            case GLFW_KEY_LEFT_SHIFT -> "Left Shift";
            case GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
            case GLFW_KEY_LEFT_CONTROL -> "Left Ctrl";
            case GLFW_KEY_RIGHT_CONTROL -> "Right Ctrl";
            case GLFW_KEY_LEFT_ALT -> "Left Alt";
            case GLFW_KEY_RIGHT_ALT -> "Right Alt";
            case GLFW_KEY_PAUSE -> "Pause/Break";
            case GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW_KEY_ESCAPE -> "Esc";
            case GLFW_KEY_SPACE -> "Space";
            case GLFW_KEY_PAGE_UP -> "Page Up";
            case GLFW_KEY_PAGE_DOWN -> "Page Down";
            case GLFW_KEY_END -> "End";
            case GLFW_KEY_HOME -> "Home";
            case GLFW_KEY_LEFT -> "Left Arrow";
            case GLFW_KEY_UP -> "Up Arrow";
            case GLFW_KEY_RIGHT -> "Right Arrow";
            case GLFW_KEY_DOWN -> "Down Arrow";
            case GLFW_KEY_INSERT -> "Insert";
            case GLFW_KEY_DELETE -> "Delete";
            case GLFW_KEY_KP_0 -> "Numpad 0";
            case GLFW_KEY_KP_1 -> "Numpad 1";
            case GLFW_KEY_KP_2 -> "Numpad 2";
            case GLFW_KEY_KP_3 -> "Numpad 3";
            case GLFW_KEY_KP_4 -> "Numpad 4";
            case GLFW_KEY_KP_5 -> "Numpad 5";
            case GLFW_KEY_KP_6 -> "Numpad 6";
            case GLFW_KEY_KP_7 -> "Numpad 7";
            case GLFW_KEY_KP_8 -> "Numpad 8";
            case GLFW_KEY_KP_9 -> "Numpad 9";
            case GLFW_KEY_KP_MULTIPLY -> "Numpad *";
            case GLFW_KEY_KP_ADD -> "Numpad +";
            case GLFW_KEY_KP_SUBTRACT -> "Numpad -";
            case GLFW_KEY_KP_DECIMAL -> "Numpad .";
            case GLFW_KEY_KP_DIVIDE -> "Numpad /";
            case GLFW_KEY_F1 -> "F1";
            case GLFW_KEY_F2 -> "F2";
            case GLFW_KEY_F3 -> "F3";
            case GLFW_KEY_F4 -> "F4";
            case GLFW_KEY_F5 -> "F5";
            case GLFW_KEY_F6 -> "F6";
            case GLFW_KEY_F7 -> "F7";
            case GLFW_KEY_F8 -> "F8";
            case GLFW_KEY_F9 -> "F9";
            case GLFW_KEY_F10 -> "F10";
            case GLFW_KEY_F11 -> "F11";
            case GLFW_KEY_F12 -> "F12";
            case GLFW_KEY_NUM_LOCK -> "Num Lock";
            case GLFW_KEY_SCROLL_LOCK -> "Scroll Lock";
            case GLFW_KEY_PRINT_SCREEN -> "Print Screen";
            default -> "Unknown";
        };
    }
}
