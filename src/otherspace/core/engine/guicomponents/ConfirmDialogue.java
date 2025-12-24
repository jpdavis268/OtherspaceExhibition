package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;

import java.util.function.Consumer;

/**
 * Asks the user to confirm an action.
 */
public class ConfirmDialogue extends Component {
    private final String prompt;

    private final Button enterButton;
    private final Button cancelButton;

    public ConfirmDialogue(String prompt, Consumer<Component> acceptAction, Consumer<Component> cancelAction) {
        super(0, 0);

        this.prompt = prompt;
        int buttonWidth = 56;
        int buttonHeight = 40;
        enterButton = new Button(buttonWidth, buttonHeight, "menu_yes", acceptAction, Color.DARK_GREEN, Color.GREEN);
        cancelButton = new Button(buttonWidth, buttonHeight, "menu_no", cancelAction, Color.DARK_RED, Color.RED);
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        // Calculate width
        int minWidth = 136;
        int width = Math.max(d.getFont().getWidth(SettingsManager.getText(prompt)) + 8, minWidth);

        // Draw field
        d.setColor(Color.DARK_GRAY);
        int halfHeight = 50;
        d.drawRect(new Rectanglei(x - width / 2, y - halfHeight, x + width / 2, y + halfHeight));
        d.setColor(Color.WHITE);
        d.setHalign(Drawer.H_LEFT);
        d.setValign(Drawer.V_TOP);
        d.drawText(x - width / 2 + 4, y - halfHeight + 4, SettingsManager.getText(prompt));

        // Draw buttons
        int buttonDistX = 36;
        int buttonDistY = 26;
        enterButton.draw(d, x - width / 2 + buttonDistX, y + buttonDistY);
        cancelButton.draw(d, x + width / 2 - buttonDistX, y + buttonDistY);
    }

    /**
     * Invoke the accept action for this dialogue.
     */
    public void invokeAccept() {
        enterButton.invokeCallback();
    }

    /**
     * Invoke the cancel action for this dialogue.
     */
    public void invokeCancel() {
        cancelButton.invokeCallback();
    }
}
