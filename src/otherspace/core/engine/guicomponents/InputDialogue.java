package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;

import java.util.function.Consumer;

/**
 * Asks the user to input a string of text.
 */
public class InputDialogue extends Component {
    private final String prompt;

    private final Button enterButton;
    private final Button cancelButton;
    private final InputField inputField;

    public InputDialogue(int width, int height, String prompt, Consumer<Component> acceptAction, Consumer<Component> cancelAction) {
        super(width, height);

        this.prompt = prompt;
        this.enterButton = new Button(56, 40, "menu_enter", acceptAction, Color.DARK_GREEN, Color.GREEN);
        this.cancelButton = new Button(56, 40, "menu_cancel", cancelAction, Color.DARK_RED, Color.RED);
        this.inputField = new InputField(width - 8);

        enterButton.setParent(this);
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        int xOffset = x - width / 2;
        int yOffset = y - height / 2;

        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(xOffset, yOffset, xOffset + width, yOffset + height));
        d.setColor(Color.WHITE);
        d.setHalign(Drawer.H_LEFT);
        d.setValign(Drawer.V_TOP);
        d.drawText(xOffset + 4, yOffset + 4, SettingsManager.getText(prompt));

        enterButton.draw(d, xOffset + 32, yOffset + height - 24);
        cancelButton.draw(d, xOffset + width - 32, yOffset + height - 24);
        inputField.draw(d, x, y - 24);
    }

    /**
     * Get the text held within the input field.
     *
     * @return Text within input field.
     */
    public String getStoredInput() {
        return inputField.getStoredText();
    }

    /**
     * Invoke this dialogue's enter action.
     */
    public void invokeEnterAction() {
        enterButton.invokeCallback();
    }

    /**
     * Invoke this dialogue's cancel action.
     */
    public void invokeCancelAction() {
        cancelButton.invokeCallback();
    }
}
