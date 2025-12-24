package otherspace.game.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.SoundListener;
import otherspace.core.engine.Sprite;
import otherspace.core.engine.guicomponents.Toolbar;
import otherspace.core.engine.utils.CollisionUtils;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.engine.utils.IOUtils;
import otherspace.core.engine.world.crafting.HandRecipe;
import otherspace.core.engine.world.crafting.Recipe;
import otherspace.core.engine.world.entities.LivingEntity;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.entities.components.Container;
import otherspace.core.engine.world.entities.components.EntityComponent;
import otherspace.core.engine.world.entities.components.GUI;
import otherspace.core.engine.world.items.*;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.engine.world.tiles.WallTile;
import otherspace.core.registry.EntityRegistry;
import otherspace.core.registry.ItemRegistry;
import otherspace.core.registry.RecipeRegistry;
import otherspace.core.registry.TileRegistry;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.core.session.scenes.world.InputHandler;
import otherspace.core.session.scenes.world.SelectorHandler;
import otherspace.core.session.scenes.world.World;
import otherspace.core.session.scenes.world.layers.LightingLayer;
import otherspace.core.session.scenes.world.ui.ChatBox;
import otherspace.core.session.scenes.world.ui.Hotbar;
import otherspace.game.tiles.GroundTiles;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Player character entity.
 * <p>
 * NOTE: This contains the information for the player character that the player will directly control,
 * NOT the object representing other players in future multiplayer builds.
 */
public class Player extends LivingEntity {
    private static Player ownPlayer;
    private static final Toolbar toggleSBInventory = new Toolbar(426, 20, new String[] {
            "inventory_crafting_header",
            "inventory_sb_item_header"
    });

    private final Color playerColor;

    private int gamemode;
    private boolean floorMode;
    private float destroyProgress;
    private float craftProgress;

    private final SoundListener soundListener;

    @SuppressWarnings("unchecked")
    public Player(boolean respawned) {
        super(new Vector2d());

        ownPlayer = this;
        playerColor = new Color(SettingsManager.get("player_color").getAsInt());
        addComponent(new SelectorHandler(this));
        addComponent(new Container<>(this, new Inventory(30), new Inventory(1)));
        addComponent(new GUI<>(this, new Rectanglei(-213, -152, 213, 152), (d) -> {
            d.setColor(new Color(0.1875f, 0.1875f, 0.1875f));
            d.drawRect(new Rectanglei(0, 0, 426, 152));

            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.setColor(Color.WHITE);
            boolean sbMenu = false;
            if (gamemode == 1) {
                toggleSBInventory.draw(d, 0, 0);
                sbMenu = toggleSBInventory.getSelectedTab() == 1;
            }
            else {
                d.drawText(4, 4, SettingsManager.getText("inventory_crafting_header"));
            }

            int leftX = 4;
            int topY = 24;
            int index = 0;
            if (sbMenu) {
                // Creative Item Menu
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 10; j++) {
                        int cx = leftX + j * 42;
                        int cy = topY + i * 42;
                        d.setColor(Color.GRAY);
                        Rectanglei cellArea = new Rectanglei(cx, cy, cx + 40, cy + 40);
                        d.drawRect(cellArea);
                        if (GenUtils.isMouseOver(cellArea)) {
                            d.setColor(new Color(1, 1, 1, 0.5f));
                            d.drawRect(cellArea);
                            InputHandler.setCurrentSBItemSel(index);
                        }
                        if (index < ItemRegistry.getRegistrySize()) {
                            d.drawSprite(Item.get(index).SPRITE, cx + 4, cy + 4, 0);
                        }
                        index++;
                    }
                }
            }
            else {
                // Crafting Menu
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 10; j++) {
                        // Check that we are still in the grid.
                        if (index < 30) {
                            int cx = leftX + j * 42;
                            int cy = topY + i * 42;
                            Rectanglei cellArea = new Rectanglei(cx, cy, cx + 40, cy + 40);

                            if (index < RecipeRegistry.getRegistrySize()) {
                                // TODO: Once more recipe types are implemented this will need to be reworked.
                                if (Recipe.get(index) instanceof HandRecipe recipe) {
                                    int toDraw = recipe.OUTPUTS[0].itemID;
                                    boolean hasInputs = getPlayerInventory().contains(recipe.INPUTS);
                                    boolean hasTools = getPlayerInventory().contains(recipe.REQUIRED_TOOLS);
                                    d.setColor(hasInputs && hasTools ? Color.GRAY : Color.DARK_GRAY);

                                    // Draw square and recipe icon.
                                    d.drawRoundRect(cellArea);

                                    if (GenUtils.isMouseOver(cellArea)) {
                                        d.setColor(new Color(1, 1, 1, 0.5f));
                                        d.drawRoundRect(cellArea);
                                        InputHandler.setCurrentRecipeSel(index);
                                    }

                                    d.drawSprite(Item.get(toDraw).SPRITE, cx + 4, cy + 4, 0);
                                }
                            }
                            else {
                                d.setColor(new Color(0.125f, 0.125f, 0.125f));
                                d.drawRoundRect(cellArea);
                            }

                            index++;
                        }
                    }
                }
            }

            // Player Inventory
            d.setColor(Color.WHITE);
            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.drawText(4, 156, SettingsManager.getText("player_inventory"));
            getPlayerInventory().draw(d, new Vector2i(4, 176), 3, false);
        }));

        soundListener = new SoundListener(this);
        gamemode = World.getDefaultGM();
        floorMode = false;
        destroyProgress = 0;
        craftProgress = 0;

        InputHandler.setPlayerControlEnabled(true);

        // Load player data.
        if (!respawned) {
            File playerData = new File(World.getSavePath(), "/playerdata.json");
            if (playerData.exists()) {
                // General data.
                // Load data.
                JsonObject info = IOUtils.loadJson(playerData.getPath());
                position = IOUtils.jsonToObject(info.get("position"), Vector2d.class);
                gamemode = info.get("gamemode").getAsInt();

                JsonObject componentTree = info.getAsJsonObject("components");
                for (Map.Entry<String, JsonElement> obj : componentTree.entrySet()) {
                    Class<? extends EntityComponent<?>> componentType;
                    try {
                        componentType = (Class<? extends EntityComponent<?>>) Class.forName(obj.getKey());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    EntityComponent<?> component = getComponent(componentType);
                    component.deserialize(obj.getValue().getAsJsonObject());
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        getSelector().update();
        soundListener.setPosition(position);
        if (getSelector().selectorMoved()) {
            destroyProgress = 0;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        ownPlayer = null;
    }

    @Override
    public void drawSelf(Drawer d) {
        if (gamemode != 1) {
            Sprite sprite = getSprite();

            // Handle water
            int tile = Chunk.getTileAt(Chunk.GTM, position);
            if (tile == GroundTiles.SHALLOW_WATER) {
                Color bottom = new Color(0.8f * playerColor.r(), 0.8f * playerColor.g(), 1 * playerColor.b(), 0.8f);
                sprite.drawPart((int) (position.x * 32), (int) (position.y * 32), playerColor, playerColor, bottom, bottom, 1, 1, spriteFrame, new Rectanglei(0, 0, sprite.getWidth(), sprite.getHeight()));
            } else if (tile == GroundTiles.DEEP_WATER) {
                Color bottom = new Color(0.5f * playerColor.r(), 0.5f * playerColor.g(), 1 * playerColor.b(), 0.5f);
                sprite.drawPart((int) (position.x * 32), (int) (position.y * 32), playerColor, playerColor, bottom, bottom, 1, 1, spriteFrame, new Rectanglei(0, 0, sprite.getWidth(), sprite.getHeight()));
            } else {
                sprite.draw((int) (position.x * 32), (int) (position.y * 32), 0, playerColor, 1, 1, spriteFrame);
            }

            // Draw night light
            LightingLayer.drawLight(new Vector2d(position.x, position.y - (double) getSprite().getHeight() / 64), 1, 0.1f);
        }
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void kill() {
        ChatBox.message(String.format("Player was killed at x: %.2f, y: %.2f", position.x, position.y));
        Gravestone playerGrave = new Gravestone(new Vector2d(Math.floor(position.x) + 0.5, Math.ceil(position.y)));
        playerGrave.dumpInventory(getPlayerInventory());
        playerGrave.dumpInventory(getHeldInventory());
        ownPlayer = null;
        destroy();
    }

    /**
     * Get this player's current gamemode (0 - survival, 1 - sandbox).
     *
     * @return Player gamemode.
     */
    public int getGamemode() {
        return gamemode;
    }

    /**
     * Get the player's current gamemode (0 - survival, 1 - sandbox).
     *
     * @param gamemode New player gamemode.
     */
    public void setGamemode(int gamemode) {
        this.gamemode = gamemode;
    }

    /**
     * Set the player's destroy progress.
     *
     * @param destroyProgress New destroy progress.
     */
    public void setDestroyProgress(float destroyProgress) {
        this.destroyProgress = destroyProgress;
    }

    /**
     * Set the player's craft progress.
     *
     * @param craftProgress New craft progress.
     */
    public void setCraftProgress(float craftProgress) {
        this.craftProgress = craftProgress;
    }

    /**
     * Get the current destroy progress.
     *
     * @return How close player is to breaking something, from 0 to 100.
     */
    public float getDestroyProgress() {
        return destroyProgress;
    }

    /**
     * Get the current craft progress.
     *
     * @return How close player is to crafting something, from 0 to 1.
     */
    public float getCraftProgress() {
        return craftProgress;
    }

    /**
     * Get the current player character.
     *
     * @return Player character.
     */
    public static Player getOwnPlayer() {
        return ownPlayer;
    }

    /**
     * Get the selector attached to this player.
     *
     * @return Player selector.
     */
    public static SelectorHandler getSelector() {
        return ownPlayer.getComponent(SelectorHandler.class);
    }

    /**
     * Get the player inventory.
     *
     * @return Main player inventory.
     */
    public static Inventory getPlayerInventory() {
        return ownPlayer.getComponent(Container.class).getInventory(0);
    }

    /**
     * Get the held item inventory.
     *
     * @return Held item inventory.
     */
    public static Inventory getHeldInventory() {
        return ownPlayer.getComponent(Container.class).getInventory(1);
    }

    /**
     * Get the player's currently held item.
     *
     * @return Held item stack.
     */
    public static ItemStack getHeldItem() {
        return ownPlayer.getComponent(Container.class).getInventory(1).get(0);
    }

    /**
     * Get whether we are currently building in "floor mode".
     *
     * @return Whether we are part of the floorboard gang.
     */
    public static boolean inFloorMode() {
        return ownPlayer.floorMode;
    }

    /**
     * Set whether to build on the floor layer or not.
     *
     * @param floorMode Whether to build floor tiles (true) or wall tiles (false).
     */
    public static void setFloorMode(boolean floorMode) {
        ownPlayer.floorMode = floorMode;
    }

    /**
     * Attempt to build using whatever the player is holding.
     */
    public static void buildThings() {
        SelectorHandler sel = getSelector();
        Vector2d selPos = sel.getPosition();

        if (sel.isWithinRange() && !getHeldItem().isEmpty()) {
            Item buildItem = getHeldItem().getItem();
            Rectangled buildArea = buildItem instanceof TileEntityItem te ?
                    new Rectangled(EntityRegistry.getSprite(te.MY_ENTITY).getBoundingBox()).translate(selPos.x, selPos.y + 0.5) :
                    new Rectangled(selPos.x - 0.5, selPos.y - 0.5, selPos.x + 0.5, selPos.y + 0.5);
            boolean noBlockingTile = Chunk.getTileAt(inFloorMode() ? Chunk.FTM : Chunk.STM, sel.getPosition()) < 0;
            boolean noBlockingEntity = !CollisionUtils.checkEntityCollision(CollisionUtils.getNearbyEntities(selPos), buildArea, true);

            GroundTile props = GroundTile.get(Chunk.getTileAt(Chunk.GTM, selPos));
            boolean solidGround = props != null && props.SOLID_GROUND;

            if (noBlockingTile && noBlockingEntity && solidGround) {
                if (buildItem instanceof TileItem tileItem) {
                    int tileID = inFloorMode() ? tileItem.getFloorID() : tileItem.getWallID();
                    if (tileID != TileRegistry.NULL) {
                        Chunk.setTileAt(inFloorMode() ? Chunk.FTM : Chunk.STM, selPos, tileID);

                        if (ownPlayer.gamemode == 0) {
                            getHeldItem().stackSize--;
                        }
                    }
                }
                else if (buildItem instanceof TileEntityItem tileEntityItem) {
                    Class<?> tileEntity = tileEntityItem.MY_ENTITY;
                    try {
                        tileEntity.getConstructor(Vector2d.class).newInstance(new Vector2d(selPos).add(0, 0.5));
                    }
                    catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                    if (ownPlayer.gamemode == 0) {
                        getHeldItem().stackSize--;
                    }
                }
            }
        }
    }

    /**
     * Attempt to break whatever is under the selector.
     */
    public static void breakThings() {
        SelectorHandler sel = getSelector();

        if (sel.isWithinRange()) {
            int tile = Chunk.getTileAt(inFloorMode() ? Chunk.FTM : Chunk.STM, sel.getPosition());
            TileEntity entity = CollisionUtils.collisionPoint(TileEntity.class, sel.getPosition());

            if (entity != null && !inFloorMode()) {
                // Calculate destroy progress
                boolean rightTool = entity.isToolEffective(getHeldItem());
                float miningSpeed = (1 / entity.getHardness()) * (rightTool ? 1 : entity.getWrongToolPenalty());
                ownPlayer.destroyProgress += (ownPlayer.gamemode == 1) ? 100 : miningSpeed;

                // If destroy progress reaches 100, break this tile entity.
                if (ownPlayer.destroyProgress >= 100) {
                    ItemDrop[] drops = entity.returns();
                    for (ItemDrop d : drops) {
                        // Add this drop to inventory if drop rate is 100% or "dice roll" lands.
                        if (d.chance() >= 1 || GenUtils.diceRoll(d.chance())) {
                            ItemStack held = getHeldItem();
                            Item heldProps = held.getItem();

                            if (d.toDrop().equals(held) && held.stackSize < heldProps.MAX_SIZE) {
                                d.toDrop().stackSize = getHeldInventory().add(d.toDrop(), null, true);
                            }

                            if (d.toDrop().stackSize > 0) {
                                getPlayerInventory().add(d.toDrop(), ownPlayer.position);
                            }
                        }
                    }

                    // Attempt to put everything that was in the tile entity's inventories into the player inventory.
                    Container<?> c = entity.getComponent(Container.class);
                    if (c != null) {
                        for (int i = 0; i < c.getInventoryCount(); i++) {
                            for (int j = 0; j < c.getInventory(i).getSize(); j++) {
                                getPlayerInventory().add(c.getInventory(i).get(j), ownPlayer.position);
                            }
                        }
                    }

                    // Remove tile entity and reset destroy progress.
                    entity.destroy();
                    ownPlayer.destroyProgress = 0;
                    if (getOwnPlayer().gamemode != 1) {
                        ToolItem.damage(getHeldItem(), 1);
                    }
                }
            }
            else if (tile > TileRegistry.EMPTY) {
                // Calculate destroy progress.
                ItemDrop[] drops;
                if (inFloorMode()) {
                    FloorTile tileProps = FloorTile.get(tile);
                    drops = tileProps.RETURNS;
                    ownPlayer.destroyProgress += (ownPlayer.gamemode == 1) ? 100 : 1 / tileProps.HARDNESS;
                }
                else {
                    WallTile tileProps = WallTile.get(tile);
                    drops = tileProps.RETURNS;
                    boolean rightTool = tileProps.isToolEffective(getHeldItem());
                    float miningSpeed = (1 / tileProps.HARDNESS) * (rightTool ? 1 : tileProps.WRONG_TOOL_PENALTY);
                    ownPlayer.destroyProgress += (ownPlayer.gamemode == 1) ? 100 : miningSpeed;
                }

                // If destroy progress reaches 100, destroy this tile.
                if (ownPlayer.destroyProgress >= 100) {
                    // Set tile to empty.
                    Chunk.setTileAt(inFloorMode() ? Chunk.FTM : Chunk.STM, sel.getPosition(), TileRegistry.EMPTY);

                    // Get tile drops and attempt to add them to inventory.
                    if (drops != null) {
                        for (ItemDrop d : drops) {
                            if (d.chance() >= 1 || GenUtils.diceRoll(d.chance())) {
                                ItemStack held = getHeldItem();
                                Item heldProps = held.getItem();

                                if (d.toDrop().equals(held) && held.stackSize < heldProps.MAX_SIZE) {
                                    d.toDrop().stackSize = getHeldInventory().add(d.toDrop(), null, true);
                                }

                                if (d.toDrop().stackSize > 0) {
                                    getPlayerInventory().add(d.toDrop(), ownPlayer.position);
                                }
                            }
                        }
                    }

                    // Reset destroy progress.
                    ownPlayer.destroyProgress = 0;
                    if (getOwnPlayer().gamemode != 1) {
                        ToolItem.damage(getHeldItem(), 1);
                    }
                }
            }
        }
    }

    /**
     * Attempt to craft something.
     *
     * @param recipeID ID of crafting recipe to use.
     */
    public static void craftThings(int recipeID) {
        Recipe recipe = Recipe.get(recipeID);
        if (recipe instanceof HandRecipe handRecipe) {
            int time = handRecipe.CRAFTING_TIME;
            ownPlayer.craftProgress += 1f / time;

            // If craft is finished, and we have the items, complete the recipe.
            if (ownPlayer.craftProgress >= 1 && getPlayerInventory().contains(handRecipe.INPUTS) && getPlayerInventory().contains(handRecipe.REQUIRED_TOOLS)) {
                // Remove inputs and give outputs.
                for (ItemStack s : handRecipe.INPUTS) {
                    getPlayerInventory().subtract(s);
                }
                for (Class<? extends ToolItem> toolType : handRecipe.REQUIRED_TOOLS) {
                    ToolItem.damage(getPlayerInventory().findInstance(toolType), 1);
                }
                for (ItemStack s : handRecipe.OUTPUTS) {
                    getPlayerInventory().add(s, ownPlayer.position);
                }

                ownPlayer.craftProgress = 0;
            }
        }
    }

    /**
     * Use the sandbox brush to place tiles.
     * May want to extract this, and other similar methods, into a component.
     */
    public static void useSandboxBrush(byte tileSet, int tile, int size) {
        double radius = size / 2d;
        boolean evenRadius = (int) radius == radius;
        Vector2d selPos = Player.getSelector().getPosition();
        double cx = evenRadius ? selPos.x - 0.5 : selPos.x;
        double cy = evenRadius ? selPos.y - 0.5 : selPos.y;

        for (int i = 0; i < size; i++) {
            double xOffset = selPos.x - Math.floor(radius) + i;
            for (int j = 0; j < size; j++) {
                double yOffset = selPos.y - Math.floor(radius) + j;
                double distance = new Vector2d(cx, cy).distance(xOffset, yOffset);
                if (distance <= radius && CollisionUtils.collisionPoint(TileEntity.class, new Vector2d(xOffset, yOffset)) == null) {
                    int curTile = Chunk.getTileAt(tileSet, new Vector2d(xOffset, yOffset));
                    if (curTile != tile) {
                        Chunk.setTileAt(tileSet, new Vector2d(xOffset, yOffset), tile, true);
                    }
                }
            }
        }
    }

    /**
     * Save the player's data. Unlike most save data, this will not be compressed to allow for easy modification.
     */
    public static void savePlayerData() {
        // Save player information.
        Player player = Player.getOwnPlayer();
        JsonObject playerInfo = player.serialize();
        playerInfo.add("gamemode", new JsonPrimitive(player.gamemode));
        playerInfo.add("hotbarSlots", Hotbar.serialize());
        IOUtils.saveJson(playerInfo, World.getSavePath() + "/playerdata.json");
    }
}
