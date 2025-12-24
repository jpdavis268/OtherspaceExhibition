package otherspace.core.engine.world.entities;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Sound;
import otherspace.core.engine.Sprite;
import otherspace.core.engine.utils.CollisionUtils;
import otherspace.core.engine.world.tiles.FloorTile;
import otherspace.core.engine.world.tiles.GroundTile;
import otherspace.core.session.Drawer;
import otherspace.core.session.SoundManager;
import otherspace.core.session.scenes.world.Chunk;
import otherspace.game.entities.Player;
import otherspace.game.tiles.GroundTiles;

/**
 * Parent for entities capable of moving through the world.
 */
public abstract class MobileEntity extends Entity {
    protected Vector2i moveVec;
    double metersPerSecond;
    Vector2d lastPosition;

    protected boolean stepPattern;
    protected SoundManager.AudioSource footstepSound;

    public MobileEntity(Vector2d position) {
        super(position);

        lastPosition = new Vector2d(position);
        moveVec = new Vector2i();
        metersPerSecond = 6;

        stepPattern = false;
        footstepSound = null;
    }

    @Override
    public void update() {
        super.update();

        lastPosition = new Vector2d(position);
        Vector2i curCC = new Vector2i((int) (position.x / 16), (int) (position.y / 16));
        Chunk curChunk = Chunk.getChunk(curCC);

        // Handle sandbox mode.
        if (this instanceof Player p && p.getGamemode() == 1) {
            position.add(moveVec.x * 0.25, moveVec.y * 0.25);
        }
        else {
            // Adjust movement speed based on terrain.
            double curMoveSpeed = metersPerSecond;
            int floorID = Chunk.getTileAt(Chunk.FTM, position);
            int groundID = Chunk.getTileAt(Chunk.GTM, position);
            FloorTile floorTile = FloorTile.get(floorID);
            GroundTile groundTile = GroundTile.get(groundID);
            if (floorTile != null) {
                curMoveSpeed *= floorTile.SPEED_MODIFIER;
            }
            else if (groundTile != null) {
                curMoveSpeed *= groundTile.SPEED_MODIFIER;
            }

            // Move and collide with terrain and objects.
            if (moveVec != null && !moveVec.equals(0, 0) && getBounds() != null) {
                double destX = position.x + moveVec.x * curMoveSpeed / 60;
                double destY = position.y + moveVec.y * curMoveSpeed / 60;
                CollisionUtils.moveAndCollide(this, new Vector2d(destX, destY), metersPerSecond);
            }

            // If we moved, update our chunk.
            if (!position.equals(lastPosition) && curChunk != null && myChunk != curChunk) {
                myChunk.localEntities.remove(this);
                curChunk.localEntities.add(this);
                myChunk = curChunk;
            }

            // If this entity moves, play a footstep sound.
            if (!isSilent() && !position.equals(lastPosition) && curChunk != null) {
                Sound toPlay;
                if (floorTile != null) {
                    toPlay = floorTile.FOOTSTEP_SOUND;
                }
                else if (groundTile != null) {
                    toPlay = groundTile.FOOTSTEP_SOUND;
                }
                else {
                    toPlay = null;
                }

                if (toPlay != null && (footstepSound == null || !SoundManager.isPlaying(footstepSound))) {
                    footstepSound = SoundManager.playSoundAt(position, toPlay, false, 0.8f + (0.2f * (stepPattern ? 1 : 0)), 8, 32, 1);
                    stepPattern = !stepPattern;
                }
            }
        }
        moveVec = new Vector2i();
    }

    @Override
    public void drawSelf(Drawer d) {
        Sprite sprite = getSprite();

        // Handle water
        int tile = Chunk.getTileAt(Chunk.GTM, position);
        if (tile == GroundTiles.SHALLOW_WATER) {
            Color bottom = new Color(0.8f, 0.8f, 1, 0.8f);
            sprite.drawPart((int) (position.x * 32), (int) (position.y * 32), Color.WHITE, Color.WHITE, bottom, bottom, 1, 1, spriteFrame, new Rectanglei(0, 0, sprite.getWidth(), sprite.getHeight()));
        }
        else if (tile == GroundTiles.DEEP_WATER) {
            Color bottom = new Color(0.5f, 0.5f, 1, 0.5f);
            sprite.drawPart((int) (position.x * 32), (int) (position.y * 32), Color.WHITE, Color.WHITE, bottom, bottom, 1, 1, spriteFrame, new Rectanglei(0, 0, sprite.getWidth(), sprite.getHeight()));
        }
        else {
            super.drawSelf(d);
        }
    }

    /**
     * Set movement directions for this entity.
     *
     * @param moveVec Vector detailing which direction to attempt to move in.
     */
    public void setMoveVec(Vector2i moveVec) {
        this.moveVec = moveVec;
    }

    /**
     * Whether this entity makes noise while moving.
     *
     * @return Whether this entity makes noise.
     */
    public abstract boolean isSilent();
}
