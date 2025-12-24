package otherspace.core.session.scenes.world.ui;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.Surface;
import otherspace.core.engine.guicomponents.BindButton;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.Slider;
import otherspace.core.engine.guicomponents.Toolbar;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.engine.world.crafting.HandRecipe;
import otherspace.core.engine.world.crafting.Recipe;
import otherspace.core.engine.world.entities.components.GUI;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.engine.world.tiles.Tile;
import otherspace.core.engine.world.tiles.WallTile;
import otherspace.core.registry.ItemRegistry;
import otherspace.core.session.Drawer;
import otherspace.core.session.GameSession;
import otherspace.core.session.Session;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.scenes.world.InputHandler;
import otherspace.core.session.scenes.world.World;
import otherspace.core.session.window.MouseListener;
import otherspace.core.session.window.Window;
import otherspace.game.Assets;
import otherspace.game.entities.Player;

import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * Shows information to the player while in game, and displays any active GUIs.
 */
public class HUD extends Menu {
    private static HUD singleton;

    // Sandbox Elements
    Button tileBrushButton;
    Toolbar tileBrushToolbar;
    Slider tileBrushSlider;
    GUI<?> tileBrushMenu;

    // Death screen elements
    Button respawnButton;
    Button exitButton;

    private GUI<?> currentInteraction = null;

    public HUD() {
        singleton = this;

        tileBrushButton = new Button(64, 64, "", (_) -> {
            if (Player.getHeldItem().isEmpty()) {
                // Only allow brush menu to open if player isn't holding anything.
                currentInteraction = tileBrushMenu;
            }
        }, "sb_tile_hover");

        tileBrushToolbar = new Toolbar(426, 32, new String[] {
                "sb_tile_ground",
                "sb_tile_floor",
                "sb_tile_solid"
        });

        tileBrushSlider = new Slider(362, "sb_tile_brush", 0, (c) -> {
            Slider self = (Slider) c;
            int brushSize = Math.round(self.getPosition() * 63);

            // Update brush size.
            self.setPosition(brushSize / 63f);
            InputHandler.setSandboxBrushSize(brushSize + 1);
        });

        tileBrushMenu = new GUI<>(null, new Rectanglei(-213, -108, 213, 108), (d) -> {
           tileBrushToolbar.draw(d, 0, 0);

           Tile[] curSet = switch (tileBrushToolbar.getSelectedTab()) {
                case 0 -> GroundTile.getTileset();
                case 1 -> FloorTile.getTileset();
                case 2 -> WallTile.getTileset();
                default -> throw new IllegalStateException("Unexpected value: " + tileBrushToolbar.getSelectedTab());
            };

            // Draw selection options.
            int leftX = 4;
            int topY = 36;
            int index = -1;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 10; j++) {
                    int cx = leftX + j * 42;
                    int cy = topY + i * 42;
                    Rectanglei cellArea = new Rectanglei(cx, cy, cx + 40, cy + 40);
                    d.setColor(Color.GRAY);
                    d.drawRect(cellArea);
                    int numTiles;
                    byte tileSet;
                    if (curSet == GroundTile.getTileset()) {
                        numTiles = GroundTile.getCount();
                        tileSet = Chunk.GTM;
                    }
                    else if (curSet == FloorTile.getTileset()) {
                        numTiles = FloorTile.getCount();
                        tileSet = Chunk.FTM;
                    }
                    else {
                        numTiles = WallTile.getCount();
                        tileSet = Chunk.STM;
                    }

                    if (index < curSet.length) {
                        if (index >= 0) {
                            curSet[index].drawTile(cx + 4, cy + 4, 0);
                        }

                        if (GenUtils.isMouseOver(cellArea)) {
                            d.setColor(new Color(1, 1, 1, 0.5f));
                            d.drawRect(cellArea);
                            if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                                int tile = index < numTiles ? index : -1;
                                InputHandler.setSandboxBrushTileset(tileSet);
                                InputHandler.setSandboxBrushTile(tile);
                            }
                        }
                    }

                    index++;
                }
            }

            tileBrushSlider.setLabel(SettingsManager.getText("sb_tile_brush") + InputHandler.getSandboxBrushSize());
            tileBrushSlider.draw(d, 213, 168);
        });

        respawnButton = new Button(128, 32, "death_respawn", (_) -> new Player(true));
        exitButton = new Button(128, 32, "death_quit", (_) -> GameScene.exitGame());
    }

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();

        int mouseX = (int) MouseListener.getMouseX();
        int mouseY = (int) MouseListener.getMouseY();

        if (Player.getOwnPlayer() != null) {
            /* HUD */
            // Health Bar.
            Player ownPlayer = Player.getOwnPlayer();
            if (ownPlayer.getGamemode() == 0) {
                d.drawValueBar(
                        new Rectanglei(83, gh - 75, 243, gh - 53),
                        ownPlayer.getHP(),
                        ownPlayer.getMaxHP(),
                        Color.BLACK,
                        Color.RED,
                        Color.GREEN,
                        false,
                        true,
                        false
                );
                d.drawSprite(Assets.healthBar, 148, gh - 64, 0);
                d.setHalign(Drawer.H_CENTER);
                d.setValign(Drawer.V_MIDDLE);
                d.setColor(Color.WHITE);
                d.drawText(162, gh - 64, String.format("%d/%d", ownPlayer.getHP(), ownPlayer.getMaxHP()));
            }

            // Hotbar
            Hotbar.getSingleton().draw(d);

            // Draw GUI if we are supposed to.
            if (currentInteraction != null) {
                int guiWidth = currentInteraction.getBounds().lengthX();
                int guiHeight = currentInteraction.getBounds().lengthY();
                int guiLeft = gw / 2 + currentInteraction.getBounds().minX;
                int guiTop = gh / 2 + currentInteraction.getBounds().minY;

                new Surface(guiWidth, guiHeight) {
                    @Override
                    public void draw(Drawer d) {
                        currentInteraction.drawBack(d);
                        currentInteraction.draw(d);
                    }
                }.render(d, guiLeft, guiTop);

                // Draw held item if there is one.
                if (!Player.getHeldItem().isEmpty()) {
                    Player.getHeldInventory().draw(d, new Vector2i(mouseX, mouseY), 1, true);
                }

                // Draw recipe info if over a recipe.
                if (InputHandler.getCurrentRecipeSel() != -1) {
                    Recipe recipe = Recipe.get(InputHandler.getCurrentRecipeSel());
                    // This will need to be revisited later.
                    if (recipe instanceof HandRecipe hr) {
                        drawRecipeInfo(d, mouseX, mouseY + 16, hr);
                    }
                }

                // Draw item name box if over one.
                ItemStack curSel = InputHandler.getMouseSelection();
                if (curSel != null && !curSel.isEmpty()) {
                    d.drawTextBox(new Vector2i(mouseX, mouseY + 16), SettingsManager.getText(curSel.getItem().NAME));
                }
            } else {
                // Draw held item if there is one.
                if (!Player.getHeldItem().isEmpty()) {
                    Player.getHeldInventory().draw(d, new Vector2i(mouseX, mouseY), 1, true);
                }
            }

            // Draw crafting progress if we are crafting something.
            if (Player.getOwnPlayer().getCraftProgress() > 0) {
                d.drawValueBar(
                        new Rectanglei(mouseX, mouseY, mouseX + 50, mouseY + 10),
                        Player.getOwnPlayer().getCraftProgress(),
                        1,
                        Color.BLACK,
                        Color.GREEN,
                        Color.GREEN,
                        false,
                        true,
                        true
                );
            }

            // Game info
            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.setColor(Color.WHITE);
            int y = 0;

            d.drawText(0, y, "Otherspace " + Session.GAME_VERSION);
            y += 20;
            d.drawText(0, y, GameScene.getPlayerUsername());
            y += 20;
            d.drawText(0, y, String.format("FPS/UPS: %.1f", Session.getFPS()));
            y += 20;
            d.drawText(0, y, String.format("%s %s, %s, %s", SettingsManager.getText("hud_day"), World.getDay(), World.getTime(), World.getDayPhase()));
            y += 20;
            String layer = SettingsManager.getText(Player.inFloorMode() ? "hud_floor" : "hud_normal");
            d.drawText(
                    0,
                    y,
                    String.format(
                            "%s: %s (%s %s %s)",
                            SettingsManager.getText("hud_build_mode"),
                            layer, SettingsManager.getText("hud_bm_press"),
                            BindButton.getKeyName(SettingsManager.getKeybind("build_mode_bind")),
                            SettingsManager.getText("hud_bm_to_toggle")
                    )
            );
            y += 20;

            // Debug information
            if (GameScene.isDebugEnabled()) {
                d.setHalign(Drawer.H_CENTER);
                d.drawText(gw / 2, 0, SettingsManager.getText("hud_debug_info_enabled"));
                d.setHalign(Drawer.H_LEFT);
                y += 20;

                d.drawText(0, y, String.format("%s: %.2f", SettingsManager.getText("hud_camera_zoom"), SceneManager.getCurrentScene().getCamera().getZoom()));
                y += 20;
                d.drawText(0, y, String.format("%s: %.1f / 16.7ms", SettingsManager.getText("hud_frame_time"), Session.getLastUpdateTime()));
                y += 20;
                d.drawText(0, y, String.format("%s: %d", SettingsManager.getText("hud_loaded_chunks"), World.getChunkMap().size()));
                y += 20;
                d.drawText(0, y, String.format("%s: %s", SettingsManager.getText("hud_currentsave"), World.getSavePath().getName()));
                y += 20;
                y += 20;

                d.drawText(0, y, SettingsManager.getText("hud_frame_time_breakdown"));
                y += 20;
                d.drawText(0, y, String.format("  %s: %.2fms", "game_update", GameSession.getLastGameUpdateTime()));
                y += 20;
                d.drawText(0, y, String.format("  %s: %.2fms", "game_draw", GameSession.getLastDrawTime()));
                y += 20;
                d.drawText(0, y, String.format("    %s: %.2fms", "draw_scene", SceneManager.getDrawSceneTime()));
                y += 20;
                d.drawText(0, y, String.format("    %s: %.2fms", "draw_gui", SceneManager.getDrawGUITime()));
                y += 20;
            }

            // Player coordinates
            d.setHalign(Drawer.H_RIGHT);
            Vector2d cameraPos = SceneManager.getCurrentScene().getCamera().getPosition();
            d.drawText(gw, 0, String.format("x: %.2f, y: %.2f", cameraPos.x, cameraPos.y));

            // Sandbox tile brush menu button (only draw if in sandbox mode)
            if (currentInteraction == null && Player.getOwnPlayer().getGamemode() == 1) {
                tileBrushButton.draw(d, 64, gh - 64);
                d.drawSprite(Assets.sandboxBrushIcon, 64, gh - 64, 0);
            }
        }
        else {
            /* Death Screen */
            // Disable input
            currentInteraction = null;
            InputHandler.setPlayerControlEnabled(false);

            // Darken screen
            d.setColor(new Color(0.5f, 0, 0, 0.5f));
            d.drawRect(new Rectanglei(0, 0, gw, gh));

            // Draw death menu
            d.setColor(Color.WHITE);
            d.setHalign(Drawer.H_CENTER);
            d.setValign(Drawer.V_MIDDLE);
            d.drawText(gw / 2, gh / 2 - 100, SettingsManager.getText("death_header"));
            respawnButton.draw(d, gw / 2 - 68, gh / 2);
            exitButton.draw(d, gw / 2 + 68, gh / 2);
        }
    }

    /**
     * Get what the player is currently interacting with, if anything.
     *
     * @return Current interaction, or null if there is none.
     */
    public static GUI<?> getCurrentInteraction() {
        return singleton.currentInteraction;
    }

    /**
     * Set the interactable the player is currently interacting with.
     *
     * @param interactable Interactable object.
     */
    public static void setCurrentInteraction(GUI<?> interactable) {
        singleton.currentInteraction = interactable;
    }

    /**
     * Draw info about a recipe.
     *
     * @param x X Position to draw info at.
     * @param y Y Position to draw info at.
     * @param recipe Recipe to draw.
     */
    @SuppressWarnings("unchecked")
    private void drawRecipeInfo(Drawer d, int x, int y, HandRecipe recipe) {
        boolean needsTools = !recipe.REQUIRED_TOOLS.isEmpty();

        // Build header
        String header = String.format("%dx %s", recipe.OUTPUTS[0].stackSize, SettingsManager.getText(recipe.OUTPUTS[0].getItem().NAME));

        // Calculate needed width (height is constant for now, may change when larger recipes are implemented).
        int maxInfoWidth = Math.max(recipe.REQUIRED_TOOLS.size() * 42, Math.max(recipe.INPUTS.length * 42, recipe.OUTPUTS.length * 42));
        int width = Math.max(96, Math.max(d.getFont().getWidth(header), maxInfoWidth)) + 4;
        int height = needsTools ? 205 : 143;

        // Draw info box.
        d.setColor(new Color(0, 0, 0, 0.8f));
        d.drawRect(new Rectanglei(x, y, x + width, y + height));

        int headerY = y + 2;
        int inputsY = y + 18;
        int toolsY = needsTools ? y + 80 : -1;
        int outputsY = y + (needsTools ? 142 : 80);

        // Draw text.
        d.setColor(Color.WHITE);
        d.setHalign(Drawer.H_LEFT);
        d.setValign(Drawer.V_TOP);
        d.drawText(x + 2, headerY, header);
        d.drawText(x + 2, inputsY, SettingsManager.getText("ui_crafting_requires"));
        if (needsTools) {
            d.drawText(x + 2, toolsY, SettingsManager.getText("ui_crafting_tools"));
        }
        d.drawText(x + 2, outputsY, SettingsManager.getText("ui_crafting_outputs"));

        d.setHalign(Drawer.H_RIGHT);
        d.setValign(Drawer.V_BOTTOM);

        // Draw input icons.
        Color missing = new Color(0.32f, 0, 0);
        for (int i = 0; i < recipe.INPUTS.length; i++) {
            boolean hasItem = Player.getPlayerInventory().contains(new ItemStack[]{recipe.INPUTS[i]});
            d.setColor(hasItem ? Color.DARK_GRAY : missing);

            int drawX = 42 * i + x + 3;
            int drawY = inputsY + 19;
            d.drawRoundRect(new Rectanglei(drawX, drawY, drawX + 40, drawY + 40));
            recipe.INPUTS[i].draw(d, drawX, drawY);
        }

        // Draw tool requirements.
        Object[] toolClasses = recipe.REQUIRED_TOOLS.toArray();
        for (int i = 0; i < toolClasses.length; i++) {
            // Find a "mascot" for this tool type.
            Class<? extends ToolItem> toolClass = (Class<? extends ToolItem>) toolClasses[i];
            ItemStack mascot = null;
            // FIXME: This is somewhat inefficient and displays an arbitrary item, a better way of handling "mascot" items needs to be implemented.
            for (int j = 0; j < ItemRegistry.getRegistrySize(); j++) {
                if (toolClass.isInstance(ItemRegistry.get(j))) {
                    ToolItem toolItem = (ToolItem) ItemRegistry.get(j);
                    mascot = new ItemStack(j, 1, toolItem.getMaxDurability());
                }
            }

            if (mascot == null) {
                // It may be better to have this display some kind of warning marker in the tool slot and continue.
                throw new IllegalStateException("ERROR: Recipe requests a tool type for which no item exists.");
            }

            boolean hasTool = Player.getPlayerInventory().contains(Set.of(toolClass));
            d.setColor(hasTool ? Color.DARK_GRAY : missing);

            int drawX = 42 * i + x + 3;
            int drawY = toolsY + 19;
            d.drawRoundRect(new Rectanglei(drawX, drawY, drawX + 40, drawY + 40));
            mascot.draw(d, drawX, drawY);
        }

        // Draw output icons.
        for (int i = 0; i < recipe.OUTPUTS.length; i++) {
            int drawX = 42 * i + x + 3;
            int drawY = outputsY + 19;

            d.setColor(Color.DARK_GRAY);
            d.drawRoundRect(new Rectanglei(drawX, drawY, drawX + 40, drawY + 40));
            recipe.OUTPUTS[i].draw(d, drawX, drawY);
        }
    }
}
