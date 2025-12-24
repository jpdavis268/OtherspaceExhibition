package otherspace.core.session.scenes.world;

import com.google.gson.JsonObject;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.utils.CollisionUtils;
import otherspace.core.engine.world.entities.components.EntityComponent;
import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.TileEntityItem;
import otherspace.core.engine.world.items.TileItem;
import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.engine.world.tiles.Tile;
import otherspace.core.engine.world.tiles.WallTile;
import otherspace.core.registry.EntityRegistry;
import otherspace.core.registry.TileRegistry;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.ui.HUD;
import otherspace.game.Assets;
import otherspace.game.entities.Player;

/**
 * Handles player interaction with the world.
 */
public class SelectorHandler extends EntityComponent<Player> {
    private Vector2d lastPosition;
    private Vector2d position;
    private boolean withinRange;

    public SelectorHandler(Player myPlayer) {
        super(myPlayer);

        position = new Vector2d();
        lastPosition = position;
        withinRange = false;
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public void deserialize(JsonObject json) {}

    /**
     * Selector management.
     */
    public void update() {
        // Put selector at in-world mouse coordinates.
        lastPosition = position;
        position = new Vector2d(SceneManager.getCurrentScene().getCamera().getMouseWorldPosition());
        position.floor();
        position.add(0.5, 0.5);

        // Determine if selector is in range.
        withinRange = getParent().position.distance(position) <= 8 || getParent().getGamemode() == 1;
    }

    /**
     * Render tile selector.
     *
     * @param d Drawer to use.
     */
    public void draw(Drawer d) {
        if (HUD.getCurrentInteraction() != null || GameScene.isPaused()) {
            return;
        }

        // Draw selector if the chunk it is on exists.
        if (Chunk.getChunk(new Vector2i((int) Math.floor(position.x / 16), (int) Math.floor(position.y / 16))) != null) {
            ItemStack held = Player.getHeldItem();
            Item heldInfo = held.getItem();
            boolean isPlaceable;
            if (!held.isEmpty()) {
                if (heldInfo instanceof TileItem ti) {
                    isPlaceable = Player.inFloorMode() ? ti.getFloorID() != TileRegistry.NULL : ti.getWallID() != TileRegistry.NULL;
                }
                else {
                    isPlaceable = heldInfo instanceof TileEntityItem;
                }
            }
            else {
                isPlaceable = false;
            }

            boolean locationIsFree = false;
            if (isPlaceable) {
                Rectangled buildArea = heldInfo instanceof TileEntityItem te ?
                        new Rectangled(EntityRegistry.getSprite(te.MY_ENTITY).getBoundingBox()).translate(position.x, position.y + 0.5) :
                        new Rectangled(position.x - 0.5, position.y - 0.5, position.x + 0.5, position.y + 0.5);
                boolean noBlockingTile = Chunk.getTileAt(Player.inFloorMode() ? Chunk.FTM : Chunk.STM, position) < 0;
                boolean noBlockingEntity = !CollisionUtils.checkEntityCollision(CollisionUtils.getNearbyEntities(position), buildArea, true);

                GroundTile props = GroundTile.get(Chunk.getTileAt(Chunk.GTM, position));
                boolean solidGround = props != null && props.SOLID_GROUND;

                locationIsFree = noBlockingTile && noBlockingEntity && solidGround;
            }

            // Set color based on whether operation is performable (yellow - yes, red - no)
            Color drawCol;
            if (withinRange && (!isPlaceable || locationIsFree)) {
                drawCol = Color.YELLOW;
            }
            else {
                drawCol = Color.RED;
            }

            // Draw tile selector.
            Vector2i screenPos = new Vector2i((int) (position.x * 32), (int) (position.y * 32));
            Color overlay = new Color(drawCol.r(), drawCol.g(), drawCol.b(), 0.2f);
            if (isPlaceable) {
                if (heldInfo instanceof TileItem tileItem) {
                    Color alphaBlend = new Color(1, 1, 1, 0.5f);
                    if (Player.inFloorMode()) {
                        FloorTile.getTileset()[tileItem.getFloorID()].drawTileExt(screenPos.x - 16, screenPos.y - 16, 0, alphaBlend);
                    }
                    else {
                        WallTile.getTileset()[tileItem.getWallID()].drawTileExt(screenPos.x - 16, screenPos.y - 16, 0, alphaBlend);
                    }

                    d.setColor(overlay);
                    Vector2d cameraPos = SceneManager.getCurrentScene().getCamera().getPosition();
                    Rectanglei rect = new Rectanglei(screenPos.x - 16, screenPos.y - 16, screenPos.x + 16, screenPos.y + 16);
                    rect.translate((int) (-cameraPos.x * 32), (int) (-cameraPos.y * 32));
                    d.drawRect(rect);
                }
                else {
                    TileEntityItem tileEntityItem = (TileEntityItem) heldInfo;
                    d.drawSpriteExt(EntityRegistry.getSprite(tileEntityItem.MY_ENTITY), screenPos.x, screenPos.y + 16, 0, drawCol, 1, 1, 0);
                }
            }
            else if (InputHandler.getSandboxBrushTile() != -2) {
                int diameter = InputHandler.getSandboxBrushSize();
                double radius = diameter / 2d;
                boolean evenRadius = (int) radius == radius;
                double cx = evenRadius ? position.x - 0.5 : position.x;
                double cy = evenRadius ? position.y - 0.5 : position.y;

                for (int i = 0; i < diameter; i++) {
                    double xOffset = position.x - Math.floor(radius) + i;
                    for (int j = 0; j < diameter; j++) {
                        double yOffset = position.y - Math.floor(radius) + j;
                        double distance = new Vector2d(cx, cy).distance(xOffset, yOffset);
                        if (distance <= radius) {
                            Tile[] toDraw = switch (InputHandler.getSandboxBrushTileset()) {case Chunk.GTM -> GroundTile.getTileset();
                                case Chunk.FTM -> FloorTile.getTileset();
                                case Chunk.STM -> WallTile.getTileset();
                                default -> throw new IllegalStateException("Unexpected value: " + InputHandler.getSandboxBrushTileset());
                            };

                            Color alphaBlend = new Color(1, 1, 1, 0.5f);
                            Vector2i offsetScreenPos = new Vector2i((int) (xOffset * 32) - 16, (int) (yOffset * 32) - 16);
                            if (InputHandler.getSandboxBrushTile() >= 0) {
                                toDraw[InputHandler.getSandboxBrushTile()].drawTileExt(offsetScreenPos.x, offsetScreenPos.y, 0, alphaBlend);
                            }

                            d.setColor(overlay);
                            Vector2d cameraPos = SceneManager.getCurrentScene().getCamera().getPosition();
                            Rectanglei rect = new Rectanglei(offsetScreenPos.x, offsetScreenPos.y, offsetScreenPos.x + 32, offsetScreenPos.y + 32);
                            rect.translate((int) (-cameraPos.x * 32), (int) (-cameraPos.y * 32));
                            d.drawRect(rect);
                        }
                    }
                }
            }
            else {
                d.drawSpriteExt(Assets.selector, screenPos.x - 16, screenPos.y - 16, 0, drawCol, 1, 1, 0);
            }
        }

        // Render destroy progress.
        if (getParent().getDestroyProgress() != 0) {
            Vector2d cameraPos = SceneManager.getCurrentScene().getCamera().getPosition();
            Rectanglei rect = new Rectanglei((int) (position.x * 32) - 25, (int) (position.y * 32) + 18, (int) (position.x * 32) + 25, (int) (position.y * 32) + 28);
            rect.translate((int) (-cameraPos.x * 32), (int) (-cameraPos.y * 32));
            d.drawValueBar(
                    rect,
                    getParent().getDestroyProgress(),
                    100,
                    Color.BLACK,
                    Color.GREEN,
                    Color.GREEN,
                    false,
                    true,
                    true
            );
        }
    }

    /**
     * Get the position of the selector.
     *
     * @return Selector position.
     */
    public Vector2d getPosition() {
        return position;
    }

    /**
     * Get whether the selector is currently in range.
     *
     * @return Whether selector is in range.
     */
    public boolean isWithinRange() {
        return withinRange;
    }

    /**
     * Check if the selector moved since the last frame.
     *
     * @return Whether selector was moved.
     */
    public boolean selectorMoved() {
        return !position.equals(lastPosition);
    }
}
