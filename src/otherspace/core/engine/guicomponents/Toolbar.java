package otherspace.core.engine.guicomponents;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.window.MouseListener;
import otherspace.game.Assets;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * Toolbar that allows users to select a list of menus.
 */
public class Toolbar extends Component {
    String[] labels;
    int selectedTab;

    public Toolbar(int width, int height, String[] labels) {
        super(width, height);

        this.labels = labels;
        selectedTab = 0;
    }

    @Override
    public void draw(Drawer d, int x, int y) {
        // Draw background.
        d.setColor(Color.GRAY);
        d.drawRect(new Rectanglei(x, y, x + width, y + height));

        // Draw tabs.
        int tabWidth = width / labels.length;
        for (int i = 0; i < labels.length; i++) {
            Color drawCol = (selectedTab == i) ? Color.LIGHT_GRAY : Color.GRAY;

            // Check each tab to see if mouse is over it.
            int tx = x + tabWidth * i;
            if (GenUtils.isMouseOver(new Rectanglei(tx, y, tx + tabWidth, y + height))) {
                drawCol = Color.LIGHT_GRAY;
                if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                    SoundManager.playSound(Assets.uiSelSound, false);
                    selectedTab = i;
                }
            }

            // Draw tab.
            d.setColor(drawCol);
            d.drawRect(new Rectanglei(tx, y, tx + tabWidth, y + height));
            d.setHalign(Drawer.H_CENTER);
            d.setValign(Drawer.V_MIDDLE);
            d.setColor(Color.WHITE);
            d.drawText(tx + tabWidth / 2, y + height / 2, SettingsManager.getText(labels[i]));
        }
    }

    /**
     * Get the currently selected tab.
     *
     * @return Selected tab.
     */
    public int getSelectedTab() {
        return selectedTab;
    }
}
