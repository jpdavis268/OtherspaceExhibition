package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Surface;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.engine.utils.StringUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.MouseListener;

import static org.lwjgl.glfw.GLFW.*;

public class InputField extends Component {
    private String storedText;
    private final Color fCol;
    private final Color oCol;
    private boolean selected = false;
    private int inputPosition = 0;
    private int inputPositionOffset = 0;
    private final Surface inputSurf;

    public InputField(int width, int height, String startingText, Color unselectedCol, Color outlineCol) {
        // May make height customizable later if need be.
        super(width, height);

        storedText = startingText;
        fCol = unselectedCol;
        oCol = outlineCol;
        inputSurf = new Surface(width, height) {
            @Override
            public void draw(Drawer d) {
                // Draw stored text, pushing out beginning if text exceeds the size of the box.
                int sWidth = d.getFont().getWidth(storedText.substring(0, inputPosition));
                int tx = Math.clamp(480 - sWidth, -1000, 0);
                d.setHalign(Drawer.H_LEFT);
                d.setValign(Drawer.V_MIDDLE);
                d.setColor(Color.WHITE);
                d.drawText(tx, height / 2, storedText);
                if (selected) {
                    d.drawText(tx + sWidth - 1, height / 2, StringUtils.getFieldCarat());
                }
            }
        };
    }

    public InputField(int width) {
        this(width, 24, "", Color.BLACK, Color.LIGHT_GRAY);
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        int xOffset = x - width / 2;

        // Handle selection
        if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT)) {
            boolean alreadySelected = selected;
            selected = GenUtils.isMouseOver(new Rectanglei(xOffset, y, xOffset + width, y + 20));
            if (selected && !alreadySelected) {
                inputPosition = storedText.length();
            }
        }

        // Unselect field if ESC or Enter is pressed
        if (KeyListener.checkHeld(GLFW_KEY_ENTER) || KeyListener.checkHeld(GLFW_KEY_ESCAPE)) {
            selected = false;
        }


        // Draw back
        d.setColor(oCol);
        d.drawRect(new Rectanglei(xOffset - 1, y - 1, xOffset + width + 1, y + height + 1));
        d.setColor(fCol);
        d.drawRect(new Rectanglei(xOffset, y, xOffset + width, y + height));

        // Draw input field
        inputSurf.render(d, xOffset, y);

        // If not selected, do nothing.
        if (!selected) {
            inputPositionOffset = 0;
            return;
        }

        // Handle text input.
        if (KeyListener.getLastChar() != '\0' && storedText.length() < 255) {
            storedText = StringUtils.stringInsert(storedText, KeyListener.getLastChar(), inputPosition);
            KeyListener.clearLastChar();
        }

        // Key processing
        switch (KeyListener.getLastKey()) {
            case GLFW_KEY_BACKSPACE: // Backspace
                if (inputPosition > 0) {
                    storedText = StringUtils.stringRemove(storedText, inputPosition - 1, 1);
                }
                break;
            case GLFW_KEY_LEFT: // Move input position left
                if (-inputPositionOffset < storedText.length()) {
                    inputPositionOffset--;
                }
                break;
            case GLFW_KEY_RIGHT: // Move input position right
                if (inputPositionOffset < 0) {
                    inputPositionOffset++;
                }
                break;
            case GLFW_KEY_V: // Copy string from clipboard
                if (KeyListener.isCtrlPressed()) {
                    String toPaste = KeyListener.getClipboardString();
                    if (toPaste != null) {
                        String in = toPaste.substring(0, Math.min(toPaste.length(), 257 - storedText.length()));
                        storedText = StringUtils.stringInsert(storedText, in, inputPosition);
                    }
                }
                break;
        }

        // Reset input
        KeyListener.clearLastKey();
        inputPosition = storedText.length() + inputPositionOffset;
    }

    /**
     * Get the text stored in this input field.
     *
     * @return Text stored in input field.
     */
    public String getStoredText() {
        return storedText;
    }
}
