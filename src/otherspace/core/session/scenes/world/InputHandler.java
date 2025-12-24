package otherspace.core.session.scenes.world;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import otherspace.core.engine.Camera;
import otherspace.core.engine.utils.CollisionUtils;
import otherspace.core.engine.world.crafting.HandRecipe;
import otherspace.core.engine.world.crafting.Recipe;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.entities.components.Container;
import otherspace.core.engine.world.entities.components.GUI;
import otherspace.core.engine.world.items.InputInventory;
import otherspace.core.engine.world.items.Inventory;
import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.registry.ItemRegistry;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.layers.DebugLayer;
import otherspace.core.session.scenes.world.ui.ChatBox;
import otherspace.core.session.scenes.world.ui.HUD;
import otherspace.core.session.scenes.world.ui.Hotbar;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.MouseListener;
import otherspace.game.entities.DroppedItem;
import otherspace.game.entities.Player;

import java.util.HashSet;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Game scene component responsible for handling player input.
 */
public class InputHandler {
    private static InputHandler singleton;

    private boolean playerControl;
    private int currentSBItemSel = -1;
    private int lastRecipeSel = -1;
    private int currentRecipeSel = -1;
    private byte sandboxBrushTileSet = Chunk.GTM;
    private int sandboxBrushTile = -2;
    private int sandboxBrushSize = 1;

    private Inventory currentMouseInv;
    private int currentMouseSlot;
    private int lastMouseSlot;

    public InputHandler() {
        singleton = this;
        playerControl = true;

        currentMouseInv = null;
        currentMouseSlot = -1;
        lastMouseSlot = currentMouseSlot;
    }

    /**
     * Check for and handle player input.
     */
    public void update() {
        checkCameraInput();
        checkDebugInput();
        checkTileGridInput();
        checkChatToggleInput();
        checkFloorModeInput();
        checkMoveInput();
        checkGUIInput();
        checkBuildInput();
        checkBreakInput();
        checkSweepInput();
        checkDropInput();
        inventoryManagement();
        checkCraftInput();
        checkSBItemInput();
        checkBrushInput();
        checkHotbarInput();
        checkPauseInput();
    }

    /**
     * Check if player is trying to adjust camera zoom.
     */
    private void checkCameraInput() {
        if (playerControl) {
            Camera camera = SceneManager.getCurrentScene().getCamera();

            if (MouseListener.getScroll() > 0 || KeyListener.checkPressed(SettingsManager.getKeybind("camera_zoom_out_bind"))) {
                // Zoom out
                camera.setZoom(camera.getZoom() + 0.1f);
            }
            if (MouseListener.getScroll() < 0 || KeyListener.checkPressed(SettingsManager.getKeybind("camera_zoom_in_bind"))) {
                // Zoom in
                camera.setZoom(camera.getZoom() - 0.1f);
            }
            if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_MIDDLE) || KeyListener.checkPressed(SettingsManager.getKeybind("camera_zoom_reset_bind"))) {
                // Reset zoom
                camera.setZoom(1);
            }

        }
    }

    /**
     * Debug mode toggle check.
     */
    private void checkDebugInput() {
        if (KeyListener.checkPressed(GLFW_KEY_F3) && playerControl) {
            GameScene.setDebugEnabled(!GameScene.isDebugEnabled());
        }
    }

    /**
     * Debug tile grid display input.
     */
    private void checkTileGridInput() {
        if (KeyListener.checkPressed(GLFW_KEY_F5) && playerControl) {
            DebugLayer.toggleTileGrid();
        }
    }

    /**
     * Check if the player is attempting to toggle chat.
     */
    private void checkChatToggleInput() {
        if (KeyListener.checkPressed(SettingsManager.getKeybind("chat_bind")) && playerControl) {
            ChatBox.setChatEnabled(true);
            playerControl = false;
        }
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE) && ChatBox.isChatEnabled()) {
            ChatBox.setChatEnabled(false);
            playerControl = true;
            KeyListener.clearKey(GLFW_KEY_ESCAPE);
        }
    }

    /**
     * Handle floor mode toggling.
     */
    private void checkFloorModeInput() {
        if (KeyListener.checkPressed(SettingsManager.getKeybind("build_mode_bind")) && playerControl) {
            Player.setFloorMode(!Player.inFloorMode());
        }
    }

    /**
     * Handle player movement.
     */
    private void checkMoveInput() {
        if (playerControl) {
            int moveUp = KeyListener.checkHeld(SettingsManager.getKeybind("move_up_bind")) ? 1 : 0;
            int moveLeft = KeyListener.checkHeld(SettingsManager.getKeybind("move_left_bind")) ? 1 : 0;
            int moveDown = KeyListener.checkHeld(SettingsManager.getKeybind("move_down_bind")) ? 1 : 0;
            int moveRight = KeyListener.checkHeld(SettingsManager.getKeybind("move_right_bind")) ? 1 : 0;

            Player.getOwnPlayer().setMoveVec(new Vector2i(moveRight - moveLeft, moveDown - moveUp));
        }
    }

    /**
     * Handle player interaction with GUIs.
     */
    @SuppressWarnings("unchecked")
    private void checkGUIInput() {
        // Player Inventory
        if (KeyListener.checkPressed(SettingsManager.getKeybind("inventory_bind")) && playerControl) {
            if (HUD.getCurrentInteraction() == null) {
                // If we aren't in another GUI, open player inventory.
                HUD.setCurrentInteraction(Player.getOwnPlayer().getComponent(GUI.class));
                KeyListener.clearKey(SettingsManager.getKeybind("inventory_bind"));
            }
        }

        // Click on entity with GUI.
        if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT) && playerControl && Player.getSelector().isWithinRange()) {
            Entity entity = CollisionUtils.collisionPoint(Entity.class, Player.getSelector().getPosition());
            if (entity != null && !(entity instanceof Player) && entity.hasA(GUI.class)) {
                // If this entity has a GUI, open it.
                HUD.setCurrentInteraction(entity.getComponent(GUI.class));
                MouseListener.clearButton(GLFW_MOUSE_BUTTON_LEFT);
            }
        }

        // Clear held item.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE) && playerControl && !Player.getHeldItem().isEmpty() && HUD.getCurrentInteraction() == null) {
            Player.getPlayerInventory().transferFromSlot(Player.getHeldInventory(), 0, Player.getHeldItem());
            KeyListener.clearKey(GLFW_KEY_ESCAPE);
        }

        // Manage GUI State
        if (HUD.getCurrentInteraction() != null) {
            playerControl = false;
            if (KeyListener.checkPressed(GLFW_KEY_ESCAPE) || KeyListener.checkPressed(SettingsManager.getKeybind("inventory_bind"))) {
                HUD.setCurrentInteraction(null);
                playerControl = true;
                KeyListener.clearKey(GLFW_KEY_ESCAPE);
                KeyListener.clearKey(SettingsManager.getKeybind("inventory_bind"));
            }
        }
    }

    /**
     * Check if the player is trying to build something.
     */
    private void checkBuildInput() {
        if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT) && playerControl) {
            Player.buildThings();
        }
    }

    /**
     * Check if the player is trying to break something.
     */
    private void checkBreakInput() {
        if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_RIGHT) && playerControl) {
            Player.breakThings();
        }
        else {
            if (Player.getOwnPlayer() != null) {
                Player.getOwnPlayer().setDestroyProgress(0);
            }
        }
    }

    /**
     * Check if the player is trying to pick up items.
     */
    private void checkSweepInput() {
        // Pick up items directly
        if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_RIGHT) && playerControl && Player.getSelector().isWithinRange()) {
            DroppedItem toPickup = CollisionUtils.collisionPoint(DroppedItem.class, Player.getSelector().getPosition());
            if (toPickup != null) {
                // Try to transfer item to the held slot first, then the player inventory.
                if (!Player.getHeldItem().isEmpty() && toPickup.getMyItem().equals(Player.getHeldItem())) {
                    toPickup.getMyItem().stackSize = Player.getHeldInventory().add(toPickup.getMyItem(), null, true);
                }
                toPickup.getMyItem().stackSize = Player.getPlayerInventory().add(toPickup.getMyItem(), null, true);
            }
        }

        // Pick up nearby items
        if (KeyListener.checkHeld(SettingsManager.getKeybind("sweep_bind")) && playerControl) {
            Vector2d playerPos = Player.getOwnPlayer().position;
            Rectangled nearby = new Rectangled(playerPos.x - 2, playerPos.y - 2, playerPos.x + 2, playerPos.y + 2);
            HashSet<DroppedItem> nearbyItems = CollisionUtils.collisionRectList(CollisionUtils.getNearbyEntities(playerPos), DroppedItem.class, nearby);
            for (DroppedItem d : nearbyItems) {
                // Try to transfer item to the held slot first, then the player inventory.
                if (!Player.getHeldItem().isEmpty() && d.getMyItem().equals(Player.getHeldItem())) {
                    d.getMyItem().stackSize = Player.getHeldInventory().add(d.getMyItem(), null, true);
                }
                d.getMyItem().stackSize = Player.getPlayerInventory().add(d.getMyItem(), null, true);
            }
        }
    }

    /**
     * Check if the player is trying to drop items.
     */
    private void checkDropInput() {
        if (KeyListener.checkPressed(SettingsManager.getKeybind("drop_bind")) && playerControl && Player.getSelector().isWithinRange() && !Player.getHeldItem().isEmpty()) {
            SelectorHandler sel = Player.getSelector();
            boolean noBlockingTile = Chunk.getTileAt(Chunk.STM, sel.getPosition()) < 0;
            boolean noBlockingEntity = CollisionUtils.collisionPoint(TileEntity.class, sel.getPosition()) == null;
            if (noBlockingTile && noBlockingEntity) {
                ItemStack transfer = Player.getHeldItem().copy();
                int transferSize = KeyListener.checkHeld(GLFW_KEY_LEFT_SHIFT) ? transfer.stackSize : 1;
                transfer.stackSize = transferSize;
                new DroppedItem(sel.getPosition(), transfer);
                Player.getHeldItem().stackSize -= transferSize;
            }
        }
    }

    /**
     * Check and manage input for player inventory.
     */
    @SuppressWarnings("unchecked")
    private void inventoryManagement() {
        if (HUD.getCurrentInteraction() != null && getMouseSelection() != null) {
            if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT)) {
                // Left click held
                if (KeyListener.checkHeld(GLFW_KEY_LEFT_SHIFT)) {
                    // Shift left held
                    Entity parent = HUD.getCurrentInteraction().getParent();
                    if (parent != null && parent.hasA(Container.class)) {
                        Container<?> c = parent.getComponent(Container.class);
                        LinkedList<Inventory> available = new LinkedList<>();
                        for (int i = 0; i < c.getInventoryCount(); i++) {
                            Inventory next = c.getInventory(i);
                            if (next != currentMouseInv) {
                                // TODO: This will need to be adjusted once output inventories are added.
                                if (next instanceof InputInventory) {
                                    available.addFirst(next);
                                }
                                else {
                                    available.addLast(next);
                                }
                            }
                        }

                        int remainder = getMouseSelection().stackSize;
                        while (!available.isEmpty() && remainder > 0) {
                            Inventory inv = available.pop();
                            if (inv instanceof InputInventory inInv) {
                                if (inInv.isAllowed(getMouseSelection().itemID)) {
                                    remainder = inInv.transferFromSlot(currentMouseInv, currentMouseSlot, getMouseSelection());
                                }
                            }
                            else {
                                remainder = inv.transferFromSlot(currentMouseInv, currentMouseSlot, getMouseSelection());
                            }
                        }
                    }
                }
                else if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                    // Left click
                    swapStacks();
                }
            }
            else if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_RIGHT)) {
                // Right click held
                ItemStack held = Player.getHeldItem();
                ItemStack over = getMouseSelection();
                if (held.isEmpty() && MouseListener.checkPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                    // If we right-click on a stack while holding nothing, try to grab half of it.
                    int split = Math.ceilDiv(over.stackSize, 2);
                    Player.getHeldInventory().transferFromSlot(currentMouseInv, currentMouseSlot, new ItemStack(getMouseSelection().itemID, split));
                }
                else if (held.stackSize > 1) {
                    // If we are holding something, try to deposit one of it.
                    boolean cursorMoved = currentMouseSlot != lastMouseSlot;

                    // If the slot is empty or has the same item, put one in.
                    if (over.isEmpty() || held.equals(over) && cursorMoved) {
                        currentMouseInv.slotTransfer(Player.getHeldInventory(), 0, currentMouseSlot, 1);
                    }
                }
                else if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                    // If none of the above apply, do the same thing as left click.
                    swapStacks();
                }
            }
            else {
                // Reset current selection
                currentMouseInv = null;
                currentMouseSlot = -1;
            }
        }
        lastMouseSlot = currentMouseSlot;
    }

    /**
     * Check if the player is trying to craft something.
     */
    private void checkCraftInput() {
        if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT)
                && currentRecipeSel != -1
                && currentRecipeSel == lastRecipeSel
                && Recipe.get(currentRecipeSel) instanceof HandRecipe hr
                && Player.getPlayerInventory().contains(hr.INPUTS)
                && Player.getPlayerInventory().contains(hr.REQUIRED_TOOLS)
        ) {
            Player.craftThings(currentRecipeSel);
        }
        else {
            if (Player.getOwnPlayer() != null) {
                Player.getOwnPlayer().setCraftProgress(0);
            }
        }
        lastRecipeSel = currentRecipeSel;
        currentRecipeSel = -1;
    }

    /**
     * Handle player selection of items in the sandbox menu.
     */
    private void checkSBItemInput() {
        if (HUD.getCurrentInteraction() != null && Player.getOwnPlayer().getGamemode() == 1 && MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT) && currentSBItemSel != -1) {
            if (currentSBItemSel >= 0 && currentSBItemSel < ItemRegistry.getRegistrySize()) {
                int state = 0;
                if (Item.get(currentSBItemSel) instanceof ToolItem ti) {
                    state = ti.getMaxDurability();
                }
                int maxSize = Item.get(currentSBItemSel).MAX_SIZE;
                int nextHeldSize = Player.getHeldItem().stackSize + Math.min(1, maxSize - Player.getHeldItem().stackSize);
                boolean sameAsHeld = Player.getHeldItem().itemID == currentSBItemSel;
                if (KeyListener.checkHeld(GLFW_KEY_LEFT_SHIFT)) {
                    Player.getHeldInventory().set(0, new ItemStack(currentSBItemSel, maxSize, state));
                }
                else {
                    Player.getHeldInventory().set(0, new ItemStack(currentSBItemSel, sameAsHeld ? nextHeldSize : 1, state));
                }
            }
            else {
                Player.getHeldInventory().set(0, new ItemStack());
            }
        }
        currentSBItemSel = -1;
    }

    /**
     * Handle the manipulation of the sandbox brush.
     */
    private void checkBrushInput() {
        // Place sandbox tile.
        if (MouseListener.checkHeld(GLFW_MOUSE_BUTTON_LEFT) && playerControl && Player.getOwnPlayer().getGamemode() == 1 && sandboxBrushTile != -2) {
            Player.useSandboxBrush(sandboxBrushTileSet, sandboxBrushTile, sandboxBrushSize);
        }

        // Clear sandbox tile.
        if (((KeyListener.checkPressed(GLFW_KEY_ESCAPE) || MouseListener.checkPressed(GLFW_MOUSE_BUTTON_RIGHT)) && sandboxBrushTile != -2) || (playerControl && !Player.getHeldItem().isEmpty())) {
            sandboxBrushTile = -2;
            KeyListener.clearKey(GLFW_KEY_ESCAPE);
        }

        // Force tile brush selection to nothing if we are in survival mode.
        if (Player.getOwnPlayer() != null && Player.getOwnPlayer().getGamemode() == 0) {
            sandboxBrushTile = -2;
        }
    }

    /**
     * Check hotbar input.
     */
    private void checkHotbarInput() {
        if (playerControl) {
            if (KeyListener.checkPressed(GLFW_KEY_1)) {
                Hotbar.loadSlot(0);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_2)) {
                Hotbar.loadSlot(1);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_3)) {
                Hotbar.loadSlot(2);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_4)) {
                Hotbar.loadSlot(3);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_5)) {
                Hotbar.loadSlot(4);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_6)) {
                Hotbar.loadSlot(5);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_7)) {
                Hotbar.loadSlot(6);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_8)) {
                Hotbar.loadSlot(7);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_9)) {
                Hotbar.loadSlot(8);
            }
            else if (KeyListener.checkPressed(GLFW_KEY_0)) {
                Hotbar.loadSlot(9);
            }
        }
    }

    /**
     * Check pause input.
     */
    private void checkPauseInput() {
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE) && playerControl) {
            // Unpausing is handled by the pause menu itself.
            KeyListener.clearKey(GLFW_KEY_ESCAPE);
            GameScene.setPaused(true);
        }
    }

    /**
     * Helper method to swap items in an inventory.
     */
    private void swapStacks() {
        ItemStack held = Player.getHeldItem();
        ItemStack over = getMouseSelection();
        if (over != null) {
            int space = over.getItem().MAX_SIZE - over.stackSize;

            if (held.equals(over)) {
                // If the item we are holding and the one in the slot is the same, try to combine the stacks.
                int delta = Math.min(held.stackSize, space);
                held.stackSize -= delta;
                over.stackSize += delta;
            }
            else {
                // Otherwise, swap the stacks.
                if (currentMouseInv instanceof InputInventory inv) {
                    if (inv.isAllowed(held.itemID) || held.isEmpty()) {
                        Player.getHeldInventory().swap(currentMouseInv, 0, currentMouseSlot);
                    }
                }
                else {
                    Player.getHeldInventory().swap(currentMouseInv, 0, currentMouseSlot);
                }

            }
        }
    }

    /**
     * Get what the player currently has their mouse over in an inventory, if anything.
     *
     * @return Return current mouse selection, or null if none exists.
     */
    public static ItemStack getMouseSelection() {
        if (singleton.currentMouseInv != null && singleton.currentMouseSlot != -1) {
            return singleton.currentMouseInv.get(singleton.currentMouseSlot);
        }

        return null;
    }

    /**
     * Set what inventory slot the player is currently hovering over.
     *
     * @param inventory Inventory.
     * @param slot Index in inventory.
     */
    public static void setMouseSelection(Inventory inventory, int slot) {
        singleton.currentMouseInv = inventory;
        singleton.currentMouseSlot = slot;
    }

    /**
     * Set what the player is currently over in the sandbox item selection menu, if anything.
     *
     * @param index Index in item selection menu, or -1 if nothing is selected.
     */
    public static void setCurrentSBItemSel(int index) {
        singleton.currentSBItemSel = index;
    }

    /**
     * Set what the player is currently over in the recipe selection menu, if anything.
     *
     * @param index Index in recipe menu, or -1 if nothing is selected.
     */
    public static void setCurrentRecipeSel(int index) {
        singleton.currentRecipeSel = index;
    }

    /**
     * Set the size of the sandbox tile brush.
     *
     * @param size Brush size.
     */
    public static void setSandboxBrushSize(int size) {
        singleton.sandboxBrushSize = size;
    }

    /**
     * Set the tileset the sandbox brush will use.
     *
     * @param setIndex Tileset to use.
     */
    public static void setSandboxBrushTileset(byte setIndex) {
        singleton.sandboxBrushTileSet = setIndex;
    }

    /**
     * Set the tile in the current tileset the sandbox brush will use.
     *
     * @param tile Tile index to use.
     */
    public static void setSandboxBrushTile(int tile) {
        singleton.sandboxBrushTile = tile;
    }

    /**
     * Get the size of the sandbox brush.
     *
     * @return Current sandbox brush size.
     */
    public static int getSandboxBrushSize() {
        return singleton.sandboxBrushSize;
    }

    /**
     * Get the current tileset of the sandbox brush.
     *
     * @return Current sandbox brush tileset.
     */
    public static byte getSandboxBrushTileset() {
        return singleton.sandboxBrushTileSet;
    }

    /**
     * Get the tile of the sandbox brush.
     *
     * @return Current sandbox brush tile.
     */
    public static int getSandboxBrushTile() {
        return singleton.sandboxBrushTile;
    }

    /**
     * Get the recipe the player is currently hovering over, if any.
     *
     * @return Current recipe, or -1 if the player is not over a recipe.
     */
    public static int getCurrentRecipeSel() {
        return singleton.currentRecipeSel;
    }

    /**
     * Set whether the player should be able to perform certain input actions.
     *
     * @param shouldEnable Whether player input should be restricted.
     */
    public static void setPlayerControlEnabled(boolean shouldEnable) {
        singleton.playerControl = shouldEnable;
    }

    /**
     * Get whether the player should be able to do certain actions.
     *
     * @return Whether player input is currently restricted.
     */
    public static boolean playerControlEnabled() {
        return singleton.playerControl;
    }
}
