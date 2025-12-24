package otherspace.core.session.scenes.world.ui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.engine.utils.IOUtils;
import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.world.World;
import otherspace.core.session.window.MouseListener;
import otherspace.core.session.window.Window;
import otherspace.game.entities.Player;

import java.io.File;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * Hotbar that players can use to quickly select items or perform common actions.
 */
public class Hotbar {
    private static Hotbar singleton;
    public final int[] hotbarSelections;

    public Hotbar() {
        singleton = this;
        hotbarSelections = new int[10];
        // This could become a problem if we start to store a ton of player data, but it's insignificant at the moment.
        if (new File(World.getSavePath(), "/playerdata.json").exists()) {
            JsonElement hotbarHandleElement = IOUtils.loadJson(World.getSavePath() + "/playerdata.json").get("hotbarSlots");
            String[] hotbarHandles = IOUtils.jsonToObject(hotbarHandleElement.getAsJsonArray(), String[].class);
            for (int i = 0; i < hotbarHandles.length; i++) {
                if (hotbarHandles[i] == null) {
                    hotbarSelections[i] = -1;
                } else {
                    try {
                        hotbarSelections[i] = Item.getID(hotbarHandles[i]);
                    } catch (IndexOutOfBoundsException e) {
                        hotbarSelections[i] = -1;
                    }
                }
            }
        }
        else {
            Arrays.fill(hotbarSelections, -1);
        }
    }

    /**
     * Draw the hotbar.
     *
     * @param d Drawer to use.
     */
    public void draw(Drawer d) {
        d.setColor(Color.DARK_GRAY);
        int wWidth = Window.getWidth();
        int wHeight = Window.getHeight();
        d.drawRect(new Rectanglei(wWidth / 2 - 213, wHeight - 62, wWidth / 2 + 213, wHeight - 14));

        int leftX = wWidth / 2 - 209;
        int cy = wHeight - 58;
        for (int i = 0; i < 10; i++) {
            int cx = leftX + i * 42;
            Rectanglei cellArea = new Rectanglei(cx, cy, cx + 40, cy + 40);

            // Draw back
            d.setColor(Color.GRAY);
            d.drawRect(cellArea);

            // Handle mouse input
            if (GenUtils.isMouseOver(cellArea)) {
                d.setColor(new Color(1, 1, 1, 0.5f));
                d.drawRect(cellArea);
                if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                    if (hotbarSelections[i] < 0 && !Player.getHeldItem().isEmpty()) {
                        hotbarSelections[i] = Player.getHeldItem().itemID;
                    }
                    else if (hotbarSelections[i] >= 0) {
                        loadSlot(i);
                    }
                    MouseListener.clearButton(GLFW_MOUSE_BUTTON_LEFT);
                }

                if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                    hotbarSelections[i] = -1;
                    MouseListener.clearButton(GLFW_MOUSE_BUTTON_RIGHT);
                }
            }

            // Draw hotbar items
            if (hotbarSelections[i] >= 0) {
                Item itemRef = Item.get(hotbarSelections[i]);

                ItemStack refItem = null;
                boolean noItems = false;
                if (itemRef instanceof ToolItem) {
                    refItem = Player.getHeldInventory().findFirst(hotbarSelections[i]);
                    if (refItem == null) {
                        refItem = Player.getPlayerInventory().findFirst(hotbarSelections[i]);
                        if (refItem == null) {
                            noItems = true;
                        }
                    }
                }
                else {
                    int sum = Player.getHeldInventory().getSum(hotbarSelections[i]) + Player.getPlayerInventory().getSum(hotbarSelections[i]);
                    if (sum == 0) {
                        noItems = true;
                    }
                    else {
                        refItem = new ItemStack(hotbarSelections[i], sum);
                    }
                }

                if (noItems) {
                    d.drawSpriteExt(itemRef.SPRITE, cx + 4, cy + 4, 0, new Color(1, 1, 1, 0.5f), 1, 1, 0);
                }
                else {
                    refItem.draw(d, cx, cy);
                }
            }
        }
    }

    /**
     * Attempt to load the player's held item with an item stack from a hotbar slot.
     *
     * @param hotbarSlot Hotbar index, from 0 to 9.
     */
    public static void loadSlot(int hotbarSlot) {
        // Move item into held inventory if possible
        Player.getPlayerInventory().transferFromSlot(Player.getHeldInventory(), 0, Player.getHeldItem());
        ItemStack toTransfer = Player.getPlayerInventory().findFirst(singleton.hotbarSelections[hotbarSlot]);
        if (toTransfer != null) {
            toTransfer = toTransfer.copy();
            int remainder = Player.getHeldInventory().add(toTransfer, null, true);
            toTransfer.stackSize -= remainder;
            Player.getPlayerInventory().subtract(toTransfer);
        }
    }

    /**
     * Get the current hotbar instance.
     *
     * @return Current hotbar instance.
     */
    public static Hotbar getSingleton() {
        return singleton;
    }

    /**
     * Convert this hotbar into a JSON Object.
     *
     * @return JSON representation of hotbar configuration.
     */
    public static JsonElement serialize() {
        String[] handles = new String[10];
        for (int i = 0; i < 10; i++) {
            if (singleton.hotbarSelections[i] != -1) {
                Item info = Item.get(singleton.hotbarSelections[i]);
                handles[i] = info.getMod() + "/" + info.NAME;
            }
            else {
                handles[i] = null;
            }
        }
        return new Gson().toJsonTree(handles);
    }
}
